package proj.research.colortools.util;

import android.graphics.Color;

import proj.research.colortools.bean.ColorValue;

public class ColorUtils {
    private ColorUtils(){}

    /**
     * Convert a color int to its ARGB components.
     * @param red
     * @param green
     * @param blue
     * @return
     */
    public static String rgbToHex(int red, int green, int blue) {
        return String.format("#%02x%02x%02x", red, green, blue);
    }

    /**
     * Convert a color int to its ARGB components.
     * @param red
     * @param green
     * @param blue
     * @return
     */
    public static int rgbToColor(int red, int green, int blue) {
        return 0xff000000 | (red << 16) | (green << 8) | blue;
    }

    /**
     * Convert a color int to its ARGB components.
     * @param red
     * @param green
     * @param blue
     * @param alpha
     * @return
     */
    public static int argbToColor(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * Convert a color int to its ARGB components.
     * @param alpha
     * @param red
     * @param green
     * @param blue
     * @return
     */
    public static String argbToHex(int alpha, int red, int green, int blue) {
        return String.format("#%02x%02x%02x%02x", alpha, red, green, blue);
    }

    public static ColorValue hexToCv(int color){
        ColorValue cv = new ColorValue();
        cv.setAlpha(Color.alpha(color));
        cv.setRed(Color.red(color));
        cv.setGreen(Color.green(color));
        cv.setBlue(Color.blue(color));
        return cv;
    }

}
