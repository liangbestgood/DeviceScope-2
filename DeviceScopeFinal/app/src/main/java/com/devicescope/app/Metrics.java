package com.devicescope.app;

import android.app.ActivityManager;
import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public class Metrics {

    public int battery = 0;
    public float power = 0f;

    public long storageTotal = 0;
    public long storageFree = 0;

    public long ramTotal = 0;
    public long ramAvailable = 0;

    public float cpuUsage = 0f;
    public int cpuCores = 0;

    public float gpuHeadroom = -1f;
    public float cpuHeadroom = -1f;

    public String gpuRenderer = "Unknown";

    private long lastIdle = 0;
    private long lastTotal = 0;

    public void update(Context context) {
        readBattery(context);
        readStorage();
        readMemory(context);
        readCpu();

        if (Build.VERSION.SDK_INT >= 36) {
            readHeadroom(context);
        }
    }

    private void readBattery(Context context) {
        BatteryManager batteryManager =
                (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);

        if (batteryManager == null) {
            return;
        }

        int capacity = batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
        );

        if (capacity != Integer.MIN_VALUE) {
            battery = capacity;
        }

        int current = batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CURRENT_NOW
        );

        int voltage = batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_VOLTAGE_NOW
        );

        if (current != Integer.MIN_VALUE && voltage != Integer.MIN_VALUE) {
            power = Math.abs(current) * Math.abs(voltage) / 1_000_000_000f;
        } else {
            power = 0f;
        }
    }

    private void readStorage() {
        StatFs statFs = new StatFs(
                android.os.Environment.getDataDirectory().getPath()
        );

        storageTotal = statFs.getTotalBytes();
        storageFree = statFs.getAvailableBytes();
    }

    private void readMemory(Context context) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager == null) {
            return;
        }

        ActivityManager.MemoryInfo memoryInfo =
                new ActivityManager.MemoryInfo();

        activityManager.getMemoryInfo(memoryInfo);

        ramTotal = memoryInfo.totalMem;
        ramAvailable = memoryInfo.availMem;
    }

    private void readCpu() {
        cpuCores = Runtime.getRuntime().availableProcessors();

        long idle = 0;
        long total = 0;

        try {
            BufferedReader reader =
                    new BufferedReader(new FileReader("/proc/stat"));

            String line = reader.readLine();
            reader.close();

            if (line != null && line.startsWith("cpu ")) {
                String[] parts = line.trim().split("\\s+");

                long user = Long.parseLong(parts[1]);
                long nice = Long.parseLong(parts[2]);
                long system = Long.parseLong(parts[3]);
                long idleTime = Long.parseLong(parts[4]);
                long iowait = Long.parseLong(parts[5]);
                long irq = Long.parseLong(parts[6]);
                long softirq = Long.parseLong(parts[7]);
                long steal = Long.parseLong(parts[8]);

                idle = idleTime + iowait;
                total = user + nice + system + idleTime
                        + iowait + irq + softirq + steal;
            }

        } catch (IOException | NumberFormatException ignored) {
        }

        if (lastTotal > 0 && total > lastTotal) {
            long totalDelta = total - lastTotal;
            long idleDelta = idle - lastIdle;

            cpuUsage = 100f * (1f - ((float) idleDelta / totalDelta));

            if (cpuUsage < 0f) {
                cpuUsage = 0f;
            }

            if (cpuUsage > 100f) {
                cpuUsage = 100f;
            }
        }

        lastTotal = total;
        lastIdle = idle;
    }

    private void readHeadroom(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= 36) {

                android.os.SystemHealthManager healthManager =
                        (android.os.SystemHealthManager)
                                context.getSystemService(
                                        Context.SYSTEM_HEALTH_SERVICE
                                );

                if (healthManager != null) {
                    cpuHeadroom = healthManager.getCpuHeadroom(
                            android.os.SystemHealthManager.HEADROOM_FORECAST
                    );

                    gpuHeadroom = healthManager.getGpuHeadroom(
                            android.os.SystemHealthManager.HEADROOM_FORECAST
                    );
                }
            }
        } catch (Exception ignored) {
            cpuHeadroom = -1f;
            gpuHeadroom = -1f;
        }
    }

    public String getRamUsageText() {
        if (ramTotal <= 0) {
            return "Unknown";
        }

        long used = ramTotal - ramAvailable;

        return String.format(
                Locale.US,
                "%.1f / %.1f GB",
                used / 1024f / 1024f / 1024f,
                ramTotal / 1024f / 1024f / 1024f
        );
    }

    public float getRamUsagePercent() {
        if (ramTotal <= 0) {
            return 0f;
        }

        return ((float) (ramTotal - ramAvailable) / ramTotal) * 100f;
    }

    public String getStorageUsageText() {
        if (storageTotal <= 0) {
            return "Unknown";
        }

        long used = storageTotal - storageFree;

        return String.format(
                Locale.US,
                "%.1f / %.1f GB",
                used / 1024f / 1024f / 1024f,
                storageTotal / 1024f / 1024f / 1024f
        );
    }

    public float getStorageUsagePercent() {
        if (storageTotal <= 0) {
            return 0f;
        }

        return ((float) (storageTotal - storageFree) / storageTotal) * 100f;
    }
}
