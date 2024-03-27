/*
 * Copyright (C) 2021-2024 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#pragma once

#include <aidl/vendor/lineage/oplus_als/BnAreaCapture.h>

namespace aidl {
namespace android {
namespace hardware {
namespace sensors {
namespace implementation {

static constexpr int SENSOR_TYPE_QTI_WISE_LIGHT = 33171103;

class AlsCorrection {
  public:
    static void init();
    static void correct(float& light);
};

}  // namespace implementation
}  // namespace sensors
}  // namespace hardware
}  // namespace android
}  // namespace aidl
