/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "android.hardware.biometrics.face-service.oplus"

#include "FaceTee.h"

#include <android-base/logging.h>

#include <cstring>

namespace aidl::android::hardware::biometrics::face {

namespace {

// faceapp lives in /vendor/etc/faceunlock on sm8150/sm8250/sm8350, or the firmware partition on
// sdm845.
constexpr const char* kAppPaths[] = {"/vendor/etc/faceunlock", "/vendor/firmware_mnt/image"};
constexpr const char* kAppName = "faceapp";
constexpr uint32_t kSharedBufferSize = 0xc8000;

enum Command : uint32_t {
    SAVE_FEATURE = 1,
    COMPARE_FEATURE = 2,
    REMOVE_FEATURE = 3,
    SAVE_FRAME = 4,
    REMOVE_FRAME = 6,
};

struct Request {
    uint32_t cmd;
    uint32_t size;
    uint32_t index;
    uint8_t flags;
    uint8_t reserved[3];
    float thresholds[8];
    uint8_t data[];
};
static_assert(sizeof(Request) == 48);

struct Response {
    float score;
    uint32_t result;
};

// Maps the raw similarity of feature version 31004 onto [0, 1].
constexpr float kThresholds[8] = {-1.0f, 0.172f, 0.268f, 0.342f, 0.405f, 0.49f, 0.55f, 1.0f};

constexpr size_t kHeaderSize = 64;

// Responses follow the request payload, both are 64 byte aligned.
constexpr size_t requestSize(size_t payload) {
    return (payload + sizeof(Request) + kHeaderSize) & ~(kHeaderSize - 1);
}

}  // namespace

bool FaceTee::start(std::unique_ptr<Trustlet>& trustlet) {
    if (!trustlet) trustlet = Trustlet::start(kAppName, kAppPaths, kSharedBufferSize);
    return trustlet != nullptr;
}

bool FaceTee::open() {
    return start(mFeature) && start(mFrame);
}

uint32_t FaceTee::send(Trustlet& trustlet, size_t reqSize, size_t rspSize) {
    uint8_t* buf = trustlet.buffer();
    auto* rsp = reinterpret_cast<Response*>(buf + reqSize);
    rsp->result = 9;

    uint32_t cmd = reinterpret_cast<Request*>(buf)->cmd;
    int ret = trustlet.sendCmd(buf, reqSize, rsp, rspSize);
    if (ret != 0) {
        LOG(ERROR) << "Command " << cmd << " failed to send: " << ret;
        return ret;
    }
    // Remove commands return non-zero when the slot is empty, which callers treat as benign.
    if (rsp->result != 0) LOG(DEBUG) << "Command " << cmd << " returned " << rsp->result;
    return rsp->result;
}

bool FaceTee::saveFeature(const uint8_t* feature, size_t size) {
    if (!open()) return false;

    auto* req = reinterpret_cast<Request*>(mFeature->buffer());
    memset(req, 0, sizeof(*req));
    req->flags = 1;  // select the /data/vendor/misc/qsee/face storage paths
    req->cmd = SAVE_FEATURE;
    req->size = size;
    memcpy(req->data, feature, size);
    return send(*mFeature, requestSize(size), kHeaderSize) == 0;
}

bool FaceTee::removeFeature() {
    if (!open()) return false;

    auto* req = reinterpret_cast<Request*>(mFeature->buffer());
    memset(req, 0, sizeof(*req));
    req->flags = 1;  // select the /data/vendor/misc/qsee/face storage paths
    req->cmd = REMOVE_FEATURE;
    return send(*mFeature, kHeaderSize, kHeaderSize) == 0;
}

std::optional<float> FaceTee::compare(const uint8_t* feature, size_t size) {
    if (!open()) return std::nullopt;

    auto* req = reinterpret_cast<Request*>(mFeature->buffer());
    memset(req, 0, sizeof(*req));
    req->flags = 1;  // select the /data/vendor/misc/qsee/face storage paths
    req->cmd = COMPARE_FEATURE;
    req->size = size;
    memcpy(req->thresholds, kThresholds, sizeof(kThresholds));
    memcpy(req->data, feature, size);

    size_t reqSize = requestSize(size);
    if (send(*mFeature, reqSize, kHeaderSize) != 0) return std::nullopt;
    return reinterpret_cast<Response*>(mFeature->buffer() + reqSize)->score;
}

bool FaceTee::saveFrame(int index, const uint8_t* nv21) {
    if (!open()) return false;

    auto* req = reinterpret_cast<Request*>(mFrame->buffer());
    memset(req, 0, sizeof(*req));
    req->flags = 1;  // select the /data/vendor/misc/qsee/face storage paths
    req->cmd = SAVE_FRAME;
    req->size = kFrameSize;
    req->index = index;
    memcpy(req->data, nv21, kFrameSize);
    return send(*mFrame, requestSize(kFrameSize), kHeaderSize) == 0;
}

bool FaceTee::removeFrame(int index) {
    if (!open()) return false;

    auto* req = reinterpret_cast<Request*>(mFrame->buffer());
    memset(req, 0, sizeof(*req));
    req->flags = 1;  // select the /data/vendor/misc/qsee/face storage paths
    req->cmd = REMOVE_FRAME;
    req->index = index;
    return send(*mFrame, kHeaderSize, kHeaderSize) == 0;
}

}  // namespace aidl::android::hardware::biometrics::face
