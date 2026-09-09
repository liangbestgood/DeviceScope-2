package com.devicescope.app;

import android.opengl.EGL14;
import android.opengl.GLES20;

public class GpuInfo {

    private GpuInfo() {
    }

    public static String renderer() {
        try {
            String renderer = GLES20.glGetString(
                    GLES20.GL_RENDERER
            );

            if (renderer != null && !renderer.isEmpty()) {
                return renderer;
            }
        } catch (Exception ignored) {
        }

        return "Unknown GPU";
    }
}
