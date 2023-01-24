/*
 * Copyright (C) 2022 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include "LineageHealth.h"

#include <android/binder_manager.h>
#include <android/binder_process.h>
#include <android-base/logging.h>

using ::aidl::vendor::lineage::health::LineageHealth;

int main() {
    ABinderProcess_setThreadPoolMaxThreadCount(0);
    std::shared_ptr<LineageHealth> lh = ndk::SharedRefBase::make<LineageHealth>();

    const std::string instance = std::string() + LineageHealth::descriptor + "/default";
    binder_status_t status = AServiceManager_addService(lh->asBinder().get(), instance.c_str());
    CHECK_EQ(status, STATUS_OK);

    ABinderProcess_joinThreadPool();
    return EXIT_FAILURE;  // should not reach
}
