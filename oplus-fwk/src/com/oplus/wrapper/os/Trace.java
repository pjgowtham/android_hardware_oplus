/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oplus.wrapper.os;

public class Trace {
    public static void asyncTraceBegin(long traceTag, String methodName, int cookie) {
        android.os.Trace.asyncTraceBegin(traceTag, methodName, cookie);
    }

    public static void asyncTraceEnd(long traceTag, String methodName, int cookie) {
        android.os.Trace.asyncTraceEnd(traceTag, methodName, cookie);
    }

    public static void traceBegin(long traceTag, String methodName) {
        android.os.Trace.traceBegin(traceTag, methodName);
    }

    public static void traceCounter(long traceTag, String counterName, int counterValue) {
        android.os.Trace.traceCounter(traceTag, counterName, counterValue);
    }

    public static void traceEnd(long traceTag) {
        android.os.Trace.traceEnd(traceTag);
    }
}
