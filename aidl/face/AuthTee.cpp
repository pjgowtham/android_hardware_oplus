/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "android.hardware.biometrics.face-service.oplus"

#include "AuthTee.h"

#include <Trustlet.h>
#include <android-base/logging.h>

#include <cstddef>
#include <cstdint>
#include <cstring>
#include <memory>

namespace aidl::android::hardware::biometrics::face {

namespace {

// faceapp lives in /vendor/etc/faceunlock on sm8150/sm8250/sm8350, or the firmware partition on
// sdm845.
constexpr const char* kAppPaths[] = {"/vendor/etc/faceunlock", "/vendor/firmware_mnt/image"};
constexpr const char* kAppName = "faceapp";
constexpr uint32_t kSharedBufferSize = 0xc8000;

using ::oplus::qseecom::DmaBuffer;
using ::oplus::qseecom::Trustlet;

// The driver patches in the addresses of the DMA buffers.
struct Command {
    uint64_t request;
    uint32_t requestLen;
    uint32_t reserved0;
    uint64_t response;
    uint32_t responseLen;
    uint32_t reserved1;
};
static_assert(sizeof(Command) == 32);

std::unique_ptr<Trustlet> sTrustlet;

}  // namespace

bool AuthTee::open() {
    if (!sTrustlet) sTrustlet = Trustlet::start(kAppName, kAppPaths, kSharedBufferSize);
    return sTrustlet != nullptr;
}

void AuthTee::close() {
    sTrustlet.reset();
}

int AuthTee::communicate(void* send, int sendLen, void* rsp, int rspLen) {
    if (!sTrustlet || sendLen <= 0 || rspLen < 0) return -1;

    DmaBuffer request, response;
    if (!request.alloc(sendLen) || !response.alloc(rspLen ? rspLen : sizeof(uint32_t))) return -1;
    memcpy(request.data(), send, sendLen);

    auto* cmd = reinterpret_cast<Command*>(sTrustlet->buffer());
    *cmd = {.requestLen = request.len(), .responseLen = response.len()};

    QSEECom_ion_fd_info ifd = {};
    ifd.data[0] = {request.fd(), offsetof(Command, request)};
    ifd.data[1] = {response.fd(), offsetof(Command, response)};

    int32_t status = 0;
    if (int ret = sTrustlet->sendModifiedCmd64(cmd, sizeof(*cmd), &status, sizeof(status), &ifd);
        ret != 0) {
        LOG(ERROR) << "Failed to send TEE command: " << ret;
        return -1;
    }
    if (rspLen) memcpy(rsp, response.data(), rspLen);
    return status;
}

}  // namespace aidl::android::hardware::biometrics::face
