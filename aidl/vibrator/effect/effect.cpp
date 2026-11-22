/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#include <effect.h>

#include <aidl/android/hardware/vibrator/CompositePrimitive.h>
#include <aidl/android/hardware/vibrator/Effect.h>

#include <algorithm>
#include <cmath>
#include <functional>
#include <utility>
#include <vector>

using aidl::android::hardware::vibrator::CompositePrimitive;
using aidl::android::hardware::vibrator::Effect;

namespace {

constexpr uint32_t kPrimitiveBit = 0x8000;
// The FIFO holds each sample until the next, so a low rate leaves audible
// images around it; stock Oplus streams play at 24 kHz too.
constexpr uint32_t kRateHz = 24000;
constexpr float kPi = 3.14159265f;

// Drive levels are fractions of qcom,fifo-vmax-mv, so the device DT sets
// the strength. A device may override the LRA model with its own header.
#if __has_include(<LraConfig.h>)
#include <LraConfig.h>
#else
// The PM8350B driven LRA, qcom,lra-period-us = <5882>.
constexpr float kF0 = 170.0f;
// Loaded quality factor, and the steady drive at resonance for 1 G.
constexpr float kQ = 8.0f;
constexpr float kLevelPerG = 0.22f;
// Drive limit for the long chirps; taps may use the full scale.
constexpr float kSustainMaxLevel = 0.56f;
#endif

constexpr float kW0 = 2 * kPi * kF0;

// Acceleration per volt at f, relative to resonance (second order model).
float response(float f) {
    float x = f / kF0;
    return x * x / std::sqrt((1 - x * x) * (1 - x * x) + (x / kQ) * (x / kQ)) / kQ;
}

// The same model in time, to tell how far the mass still swings.
struct Lra {
    float x = 0;
    float v = 0;

    void step(float drive) {
        v += (drive - kW0 / kQ * v - kW0 * kW0 * x) / kRateHz;
        x += v / kRateHz;
    }
};

// Sine whose frequency glides from f_start to f_end; level(u) gives the
// drive over the segment, u running from 0 to 1. Returns the end phase.
float render(float ms, float f_start, float f_end, const std::function<float(float)>& level,
             float phase, Lra& lra, std::vector<int8_t>* out) {
    size_t n = std::lround(ms * kRateHz / 1000);
    for (size_t i = 0; i < n; i++) {
        float u = (i + 0.5f) / n;
        phase += 2 * kPi * (f_start + (f_end - f_start) * u) / kRateHz;
        float s = std::clamp(std::round(127 * level(u) * std::sin(phase)), -127.0f, 127.0f);
        lra.step(s / 127);
        if (out) out->push_back(static_cast<int8_t>(s));
    }
    return phase;
}

class Waveform {
  public:
    Waveform& tone(float ms, float f_start, float f_end,
                   const std::function<float(float)>& level) {
        phase_ = render(ms, f_start, f_end, level, phase_, lra_, &samples_);
        return *this;
    }

    // Whole cycles at f with the drive moving linearly.
    Waveform& cycles(float count, float f, float start, float end) {
        return tone(count * 1000 / f, f, f, [=](float u) { return start + (end - start) * u; });
    }

    // Sweep that targets an acceleration g(u), compensating for the weaker
    // response away from resonance up to the sustained drive limit.
    Waveform& chirp(float ms, float f_start, float f_end, const std::function<float(float)>& g) {
        return tone(ms, f_start, f_end, [=](float u) {
            float f = f_start + (f_end - f_start) * u;
            return std::min(g(u) * kLevelPerG / response(f), kSustainMaxLevel);
        });
    }

    // Drive in antiphase at resonance, at the level that leaves the mass
    // closest to rest. Anything left rings on audibly and blurs the edge.
    Waveform& brake(float count) {
        float ms = count * 1000 / kF0;
        auto coast = [&](float level) {
            Lra lra = lra_;
            render(ms, kF0, kF0, [=](float) { return level; }, phase_ + kPi, lra, nullptr);
            return lra;
        };
        // The swing left over is affine in the brake level.
        Lra r0 = coast(0), r1 = coast(1);
        float dx = kW0 * (r1.x - r0.x), dv = r1.v - r0.v;
        float level = std::clamp(-(kW0 * r0.x * dx + r0.v * dv) / (dx * dx + dv * dv), 0.0f, 1.0f);
        phase_ += kPi;
        return tone(ms, kF0, kF0, [=](float) { return level; });
    }

    Waveform& silence(float ms) {
        size_t n = std::lround(ms * kRateHz / 1000);
        for (size_t i = 0; i < n; i++) lra_.step(0);
        samples_.resize(samples_.size() + n, 0);
        return *this;
    }

    Waveform& then(const Waveform& other) {
        for (int8_t s : other.samples_) lra_.step(s / 127.0f);
        samples_.insert(samples_.end(), other.samples_.begin(), other.samples_.end());
        return *this;
    }

    float ms() const { return samples_.size() * 1000.0f / kRateHz; }

    std::vector<int8_t> samples_;

  private:
    float phase_ = 0;
    Lra lra_;
};

// Primitives, after the targets in AOSP's haptics constants and primitives
// guide. Everything is driven near resonance: away from it the LRA turns
// the drive into sound rather than motion, so sweeps stay within 30%.
Waveform click() {
    return Waveform().cycles(1, kF0, 1, 1).brake(1);
}

Waveform lightTick() {
    return Waveform().cycles(0.5f, kF0, 1, 1).brake(0.5f);
}

Waveform lowTick() {
    return Waveform().cycles(1, kF0, 0.5f, 0.5f).brake(1);
}

Waveform thud() {
    return Waveform()
            .chirp(300, kF0, kF0 * 0.7f,
                   [](float u) {
                       return 0.25f * (u < 0.1f ? u / 0.1f : std::exp(-3 * (u - 0.1f)));
                   })
            .brake(1);
}

Waveform spin() {
    Waveform w;
    w.chirp(75, kF0 * 0.75f, kF0, [](float u) { return 0.5f * std::sin(kPi / 2 * u); });
    w.chirp(75, kF0, kF0 * 0.75f, [](float u) { return 0.5f * std::cos(kPi / 2 * u); });
    return w.brake(1);
}

Waveform rise(float ms) {
    return Waveform().chirp(ms, kF0 * 0.7f, kF0, [](float u) { return 0.5f * u * u; }).brake(1);
}

Waveform quickFall() {
    return Waveform()
            .chirp(100, kF0 * 1.3f, kF0,
                   [](float u) {
                       return u < 0.25f ? std::sin(kPi / 2 * u / 0.25f)
                                        : std::cos(kPi / 2 * (u - 0.25f) / 0.75f);
                   })
            .brake(1);
}

// Effects.
Waveform doubleClick() {
    Waveform w = click();
    w.silence(100 - w.ms());
    return w.then(click());
}

Waveform heavyClick() {
    return Waveform().cycles(2, kF0, 1, 1).brake(1.5f);
}

Waveform effectThud() {
    return Waveform()
            .cycles(1, kF0, 1, 1)
            .tone(3000 / kF0, kF0, kF0, [](float u) { return 0.67f * std::exp(-0.7f * u); })
            .brake(1.5f);
}

Waveform pop() {
    return Waveform().cycles(1, kF0, 0.75f, 0.75f).brake(1);
}

Waveform textureTick() {
    return Waveform().cycles(0.5f, kF0, 0.6f, 0.6f).brake(0.5f);
}

class Bank {
  public:
    Bank() {
        add(Effect::CLICK, click());
        add(Effect::DOUBLE_CLICK, doubleClick());
        add(Effect::TICK, lightTick());
        add(Effect::THUD, effectThud());
        add(Effect::POP, pop());
        add(Effect::HEAVY_CLICK, heavyClick());
        add(Effect::TEXTURE_TICK, textureTick());

        add(CompositePrimitive::NOOP, Waveform().silence(1));
        add(CompositePrimitive::CLICK, click());
        add(CompositePrimitive::THUD, thud());
        add(CompositePrimitive::SPIN, spin());
        add(CompositePrimitive::QUICK_RISE, rise(150));
        add(CompositePrimitive::SLOW_RISE, rise(500));
        add(CompositePrimitive::QUICK_FALL, quickFall());
        add(CompositePrimitive::LIGHT_TICK, lightTick());
        add(CompositePrimitive::LOW_TICK, lowTick());

        // Take pointers only once every waveform is in place.
        for (auto& [id, samples] : waveforms_) {
            streams_.push_back({id, static_cast<uint32_t>(samples.size()), kRateHz, samples.data()});
        }
    }

    const effect_stream* find(uint32_t id) const {
        auto it = std::find_if(streams_.begin(), streams_.end(),
                               [=](auto&& s) { return s.effect_id == id; });
        return it != streams_.end() ? &*it : nullptr;
    }

  private:
    void add(Effect effect, Waveform w) {
        waveforms_.emplace_back(static_cast<uint32_t>(effect), std::move(w.samples_));
    }

    void add(CompositePrimitive primitive, Waveform w) {
        waveforms_.emplace_back(static_cast<uint32_t>(primitive) | kPrimitiveBit,
                                std::move(w.samples_));
    }

    std::vector<std::pair<uint32_t, std::vector<int8_t>>> waveforms_;
    std::vector<effect_stream> streams_;
};

}  // anonymous namespace

const struct effect_stream* get_effect_stream(uint32_t effect_id) {
    static const Bank bank;
    return bank.find(effect_id);
}
