package com.devicescope.app;

import android.app.ActivityManager;
import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Metrics {

    public float cpu = 0f;
    public float ram = 0f;
    public float battery = 0f;
    public float power = 0f;

    public float cpuHeadroom = -1f;
    public float gpuHeadroom = -1f;

    public final List<Float> cores = new ArrayList<>();

    public long storageTotal = 0L;
    public long storageFree = 0L;

    private long lastTotal = 0L;
    private long lastIdle = 0L;

    public void update(Context context) {
        readBattery(context);
        readMemory(context);
        readStorage();
        readCpu();

        if (Build.VERSION.SDK_INT >= 36) {
            readHeadroom(context);
        }
    }

    private void readBattery(Context context) {
        BatteryManager manager =
                (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);

        if (manager == null) {
            return;
        }

        int capacity = manager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
        );

        if (capacity != Integer.MIN_VALUE) {
            battery = capacity;
        }

        int current = manager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CURRENT_NOW
        );

        int voltage = manager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_VOLTAGE_NOW
        );

        if (current != Integer.MIN_VALUE && voltage != Integer.MIN_VALUE) {
            power = Math.abs((long) current)
                    * Math.abs((long) voltage)
                    / 1_000_000_000f;
        } else {
            power = 0f;
        }
    }

    private void readMemory(Context context) {
        ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (manager == null) {
            return;
        }

        ActivityManager.MemoryInfo info =
                new ActivityManager.MemoryInfo();

        manager.getMemoryInfo(info);

        if (info.totalMem > 0) {
            ram = ((float) (info.totalMem - info.availMem)
                    / (float) info.totalMem) * 100f;
        }
    }

    private void readStorage() {
        StatFs statFs = new StatFs(
                android.os.Environment.getDataDirectory().getPath()
        );

        storageTotal = statFs.getTotalBytes();
        storageFree = statFs.getAvailableBytes();
    }

    private void readCpu() {
        cpu = readCpuLoad("/proc/stat");

        cores.clear();

        int coreCount = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < coreCount; i++) {
            float load = readCpuLoad("/proc/stat");
            cores.add(load);
        }
    }

    private float readCpuLoad(String path) {
        long idle = 0L;
        long total = 0L;

        try {
            BufferedReader reader =
                    new BufferedReader(new FileReader(path));

            String line = reader.readLine();
            reader.close();

            if (line != null && line.startsWith("cpu ")) {
                String[] values = line.trim().split("\\s+");

                if (values.length >= 9) {
                    long user = Long.parseLong(values[1]);
                    long nice = Long.parseLong(values[2]);
                    long system = Long.parseLong(values[3]);
                    long idleTime = Long.parseLong(values[4]);
                    long iowait = Long.parseLong(values[5]);
                    long irq = Long.parseLong(values[6]);
                    long softirq = Long.parseLong(values[7]);
                    long steal = Long.parseLong(values[8]);

                    idle = idleTime + iowait;

                    total = user + nice + system + idleTime
                            + iowait + irq + softirq + steal;
                }
            }

        } catch (IOException | NumberFormatException ignored) {
            return 0f;
        }

        if (lastTotal > 0L && total > lastTotal) {
            long totalDelta = total - lastTotal;
            long idleDelta = idle - lastIdle;

            float result =
                    100f * (1f - ((float) idleDelta / totalDelta));

            lastTotal = total;
            lastIdle = idle;

            return Math.max(0f, Math.min(100f, result));
        }

        lastTotal = total;
        lastIdle = idle;

        return cpu;
    }

    private void readHeadroom(Context context) {
        cpuHeadroom = -1f;
        gpuHeadroom = -1f;

        /*
         * Android 16 headroom APIs can vary between preview/API revisions.
         * Keep the fields available while avoiding a hard compile dependency
         * on a potentially unavailable SystemHealthManager API.
         */
    }

    public String summary() {
        float usedStorage = 0f;
        float totalStorage = 0f;

        if (storageTotal > 0L) {
            usedStorage =
                    (storageTotal - storageFree)
                            / 1024f / 1024f / 1024f;

            totalStorage =
                    storageTotal
                            / 1024f / 1024f / 1024f;
        }

        return String.format(
                Locale.US,
                "CPU: %.0f%%\nRAM: %.0f%%\nBattery: %.0f%%\nPower: %.2f W\nStorage: %.1f / %.1f GB",
                cpu,
                ram,
                battery,
                power,
                usedStorage,
                totalStorage
        );
    }
}
