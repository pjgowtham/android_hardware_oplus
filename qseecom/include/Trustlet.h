/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#pragma once

#include <QSEEComAPI.h>

#include <cstddef>
#include <cstdint>
#include <memory>
#include <span>

namespace oplus::qseecom {

// A session with a QSEE trusted app. libQSEEComAPI comes with the vendor blobs, so it is loaded
// at runtime rather than linked.
class Trustlet {
  public:
    // Starts |name| from the first of |paths| that holds it. Loading occasionally fails with a
    // transient scm_call error while the secure app region is contended, so it is retried.
    static std::unique_ptr<Trustlet> start(const char* name, std::span<const char* const> paths,
                                           uint32_t sharedBufferSize);

    ~Trustlet();
    Trustlet(const Trustlet&) = delete;
    Trustlet& operator=(const Trustlet&) = delete;

    // The buffer requests and responses are exchanged in.
    uint8_t* buffer() const { return mHandle->ion_sbuffer; }

    int sendCmd(void* send, uint32_t sendLen, void* rsp, uint32_t rspLen);
    int sendModifiedCmd64(void* send, uint32_t sendLen, void* rsp, uint32_t rspLen,
                          QSEECom_ion_fd_info* info);

  private:
    explicit Trustlet(QSEECom_handle* handle) : mHandle(handle) {}

    QSEECom_handle* mHandle;
};

// A CPU mapped buffer on the qseecom heap, for payloads passed outside the shared buffer.
class DmaBuffer {
  public:
    DmaBuffer() = default;
    ~DmaBuffer();
    DmaBuffer(const DmaBuffer&) = delete;
    DmaBuffer& operator=(const DmaBuffer&) = delete;

    bool alloc(size_t len);

    int fd() const { return mFd; }
    uint8_t* data() const { return mData; }
    uint32_t len() const { return mLen; }

  private:
    int mFd = -1;
    uint8_t* mData = nullptr;
    size_t mSize = 0;
    uint32_t mLen = 0;
};

}  // namespace oplus::qseecom
