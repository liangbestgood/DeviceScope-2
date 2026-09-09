package com.devicescope.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.Locale;

public class MainActivity extends Activity {

    private Metrics metrics;
    private DeviceScopeView deviceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        metrics = new Metrics();

        deviceView = new DeviceScopeView();

        setContentView(deviceView);

        updateMetrics();
    }

    private void updateMetrics() {
        metrics.update(getApplicationContext());

        if (deviceView != null) {
            deviceView.invalidate();
        }

        deviceView.postDelayed(
                this::updateMetrics,
                1000L
        );
    }

    private class DeviceScopeView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        DeviceScopeView() {
            super(MainActivity.this);

            paint.setTypeface(
                    android.graphics.Typeface.create(
                            "sans",
                            android.graphics.Typeface.NORMAL
                    )
            );
        }

        private void text(
                Canvas canvas,
                String value,
                float x,
                float y,
                float size,
                int color
        ) {
            paint.setTextSize(size);
            paint.setColor(color);
            canvas.drawText(value, x, y, paint);
        }

        private void card(
                Canvas canvas,
                float left,
                float top,
                float right,
                float bottom
        ) {
            paint.setColor(Color.rgb(25, 30, 38));

            canvas.drawRoundRect(
                    left,
                    top,
                    right,
                    bottom,
                    20f,
                    20f,
                    paint
            );
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float width = getWidth();

            int background = Color.rgb(10, 13, 18);
            int white = Color.rgb(244, 247, 250);
            int muted = Color.rgb(143, 154, 166);
            int accent = Color.rgb(92, 225, 230);

            canvas.drawColor(background);

            text(
                    canvas,
                    "DeviceScope",
                    24,
                    50,
                    28,
                    white
            );

            text(
                    canvas,
                    "Android 16 Device Monitor",
                    24,
                    76,
                    14,
                    muted
            );

            float y = 110;

            card(
                    canvas,
                    20,
                    y,
                    width - 20,
                    y + 120
            );

            text(
                    canvas,
                    "CPU",
                    40,
                    y + 35,
                    14,
                    muted
            );

            text(
                    canvas,
                    String.format(
                            Locale.US,
                            "%.0f%%",
                            metrics.cpu
                    ),
                    40,
                    y + 82,
                    38,
                    accent
            );

            card(
                    canvas,
                    20,
                    y + 135,
                    width - 20,
                    y + 255
            );

            text(
                    canvas,
                    "RAM",
                    40,
                    y + 170,
                    14,
                    muted
            );

            text(
                    canvas,
                    String.format(
                            Locale.US,
                            "%.0f%%",
                            metrics.ram
                    ),
                    40,
                    y + 217,
                    38,
                    accent
            );

            card(
                    canvas,
                    20,
                    y + 270,
                    width - 20,
                    y + 390
            );

            text(
                    canvas,
                    "Battery",
                    40,
                    y + 305,
                    14,
                    muted
            );

            text(
                    canvas,
                    String.format(
                            Locale.US,
                            "%.0f%%",
                            metrics.battery
                    ),
                    40,
                    y + 352,
                    38,
                    accent
            );

            card(
                    canvas,
                    20,
                    y + 405,
                    width - 20,
                    y + 525
            );

            text(
                    canvas,
                    "Power",
                    40,
                    y + 440,
                    14,
                    muted
            );

            text(
                    canvas,
                    String.format(
                            Locale.US,
                            "%.2f W",
                            metrics.power
                    ),
                    40,
                    y + 487,
                    32,
                    accent
            );

            text(
                    canvas,
                    "GPU",
                    40,
                    y + 565,
                    14,
                    muted
            );

            text(
                    canvas,
                    GpuInfo.renderer(),
                    40,
                    y + 595,
                    14,
                    white
            );

            text(
                    canvas,
                    "CPU Headroom",
                    40,
                    y + 640,
                    14,
                    muted
            );

            String headroom =
                    metrics.cpuHeadroom < 0
                            ? "Unavailable"
                            : String.format(
                                    Locale.US,
                                    "%.0f%%",
                                    metrics.cpuHeadroom
                            );

            text(
                    canvas,
                    headroom,
                    40,
                    y + 675,
                    22,
                    accent
            );
        }
    }
}
