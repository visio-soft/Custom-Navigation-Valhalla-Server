package org.maplibre.navigation.android.navigation.ui.v5.instruction;

import android.os.Build;
import android.util.DisplayMetrics;
import android.util.SparseArray;

class UrlDensityMap extends SparseArray<String> {

    private static final String ONE_X = "@1x";
    private static final String TWO_X = "@2x";
    private static final String THREE_X = "@3x";
    private static final String FOUR_X = "@4x";
    private static final String DOT_PNG = ".png";

    private final int displayDensity;

    UrlDensityMap(int displayDensity, int sdkVersion) {
        super(4);
        this.displayDensity = displayDensity;
        put(DisplayMetrics.DENSITY_LOW, ONE_X + DOT_PNG);
        put(DisplayMetrics.DENSITY_MEDIUM, ONE_X + DOT_PNG);
        put(DisplayMetrics.DENSITY_HIGH, TWO_X + DOT_PNG);
        put(DisplayMetrics.DENSITY_XHIGH, THREE_X + DOT_PNG);
        put(DisplayMetrics.DENSITY_XXHIGH, THREE_X + DOT_PNG);
        put(DisplayMetrics.DENSITY_XXXHIGH, FOUR_X + DOT_PNG);
        put(DisplayMetrics.DENSITY_400, THREE_X + DOT_PNG);
        put(DisplayMetrics.DENSITY_560, FOUR_X + DOT_PNG);

        if (sdkVersion >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            put(DisplayMetrics.DENSITY_280, TWO_X + DOT_PNG);
        }

        if (sdkVersion >= Build.VERSION_CODES.M) {
            put(DisplayMetrics.DENSITY_360, THREE_X + DOT_PNG);
            put(DisplayMetrics.DENSITY_420, THREE_X + DOT_PNG);
        }

        if (sdkVersion >= Build.VERSION_CODES.N_MR1) {
            put(DisplayMetrics.DENSITY_260, TWO_X + DOT_PNG);
            put(DisplayMetrics.DENSITY_300, TWO_X + DOT_PNG);
            put(DisplayMetrics.DENSITY_340, THREE_X + DOT_PNG);
        }

        if (sdkVersion >= Build.VERSION_CODES.P) {
            put(DisplayMetrics.DENSITY_440, THREE_X + DOT_PNG);
        }
    }

    public String get(String url) {
        return url + super.get(displayDensity);
    }
}