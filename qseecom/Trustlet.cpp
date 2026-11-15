/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "qseecom.oplus"

#include <Trustlet.h>

#include <BufferAllocator/BufferAllocator.h>
#include <android-base/logging.h>
#include <dlfcn.h>
#include <sys/mman.h>
#include <unistd.h>

#include <string>
#include <type_traits>

namespace oplus::qseecom {

namespace {

constexpr int kMaxStartAttempts = 5;
constexpr useconds_t kStartRetryDelayUs = 50000;

struct Api {
    decltype(&QSEECom_start_app) startApp;
    decltype(&QSEECom_shutdown_app) shutdownApp;
    decltype(&QSEECom_send_cmd) sendCmd;
    decltype(&QSEECom_send_modified_cmd_64) sendModifiedCmd64;
};

const Api* api() {
    static const Api* const sApi = []() -> const Api* {
        void* lib = dlopen("libQSEEComAPI.so", RTLD_NOW);
        if (!lib) {
            LOG(ERROR) << "Failed to load libQSEEComAPI: " << dlerror();
            return nullptr;
        }

        static Api api;
        bool ok = true;
        auto load = [&](auto& fn, const char* name) {
            fn = reinterpret_cast<std::remove_reference_t<decltype(fn)>>(dlsym(lib, name));
            if (!fn) {
                LOG(ERROR) << "Missing symbol " << name;
                ok = false;
            }
        };
        load(api.startApp, "QSEECom_start_app");
        load(api.shutdownApp, "QSEECom_shutdown_app");
        load(api.sendCmd, "QSEECom_send_cmd");
        load(api.sendModifiedCmd64, "QSEECom_send_modified_cmd_64");
        if (!ok) {
            dlclose(lib);
            return nullptr;
        }
        return &api;
    }();
    return sApi;
}

BufferAllocator& allocator() {
    static BufferAllocator sAllocator;
    return sAllocator;
}

// Prefer the heap reserved for trusted apps where the platform has one.
const std::string& heap() {
    static const std::string sHeap = [] {
        return BufferAllocator::GetDmabufHeapList().count("qcom,qseecom-ta")
                       ? "qcom,qseecom-ta"
                       : "qcom,qseecom";
    }();
    return sHeap;
}

}  // namespace

std::unique_ptr<Trustlet> Trustlet::start(const char* name, std::span<const char* const> paths,
                                          uint32_t sharedBufferSize) {
    const Api* qsee = api();
    if (!qsee) return nullptr;

    for (int attempt = 1; attempt <= kMaxStartAttempts; attempt++) {
        for (const char* path : paths) {
            QSEECom_handle* handle = nullptr;
            int ret = qsee->startApp(&handle, path, name, sharedBufferSize);
            if (ret == 0 && handle && handle->ion_sbuffer) {
                return std::unique_ptr<Trustlet>(new Trustlet(handle));
            }
            LOG(WARNING) << "Failed to start " << name << " from " << path << " (attempt "
                         << attempt << "): " << ret;
            if (handle) qsee->shutdownApp(&handle);
        }
        if (attempt < kMaxStartAttempts) usleep(kStartRetryDelayUs);
    }
    LOG(ERROR) << "Giving up starting " << name;
    return nullptr;
}

Trustlet::~Trustlet() {
    api()->shutdownApp(&mHandle);
}

int Trustlet::sendCmd(void* send, uint32_t sendLen, void* rsp, uint32_t rspLen) {
    return api()->sendCmd(mHandle, send, sendLen, rsp, rspLen);
}

int Trustlet::sendModifiedCmd64(void* send, uint32_t sendLen, void* rsp, uint32_t rspLen,
                                QSEECom_ion_fd_info* info) {
    return api()->sendModifiedCmd64(mHandle, send, sendLen, rsp, rspLen, info);
}

DmaBuffer::~DmaBuffer() {
    if (mData) {
        allocator().CpuSyncEnd(mFd, kSyncReadWrite);
        munmap(mData, mSize);
    }
    if (mFd >= 0) close(mFd);
}

bool DmaBuffer::alloc(size_t len) {
    const size_t page = getpagesize();
    mLen = len;
    mSize = (len + page - 1) & ~(page - 1);

    int fd = allocator().Alloc(heap(), mSize);
    if (fd < 0) {
        LOG(ERROR) << "Failed to allocate " << mSize << " bytes on " << heap() << ": " << fd;
        return false;
    }
    mFd = fd;

    void* data = mmap(nullptr, mSize, PROT_READ | PROT_WRITE, MAP_SHARED, mFd, 0);
    if (data == MAP_FAILED) {
        PLOG(ERROR) << "Failed to map " << heap() << " buffer";
        return false;
    }
    mData = static_cast<uint8_t*>(data);
    return allocator().CpuSyncStart(mFd, kSyncReadWrite) == 0;
}

}  // namespace oplus::qseecom
