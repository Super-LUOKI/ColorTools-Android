package proj.research.colortools.bean;

import proj.research.colortools.util.ColorUtils;

public class ColorValue {
    public static final int NONE = -1;
    public int red = NONE;
    public int green = NONE;
    public int blue = NONE;
    public int alpha = NONE;
    public int color = NONE;
    public String colorName;
    public String colorHex;
    public String colorHexAlpha;

    public ColorValue() {
    }

    public ColorValue(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.setColorInfo();
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
        this.setColorInfo();
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
        this.setColorInfo();
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
        this.setColorInfo();
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
        this.setColorInfo();
    }

    public int getColor() {
        return color;
    }

    private void setColorInfo() {
        if (alpha == NONE) {
            this.color = ColorUtils.rgbToColor(red, green, blue);
            this.colorHex = ColorUtils.rgbToHex(red, green, blue);
        } else {
            this.color = ColorUtils.argbToColor(alpha, red, green, blue);
            this.colorHex = ColorUtils.argbToHex(alpha, red, green, blue);
        }
    }


    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getColorHex() {
        return colorHex;
    }


    public String getColorHexAlpha() {
        return colorHexAlpha;
    }

    @Override
    public String toString() {
        return "ColorValue{" +
                "red=" + red +
                ", green=" + green +
                ", blue=" + blue +
                ", alpha=" + alpha +
                ", color=" + color +
                ", colorName='" + colorName + '\'' +
                ", colorHex='" + colorHex + '\'' +
                ", colorHexAlpha='" + colorHexAlpha + '\'' +
                '}';
    }
}
