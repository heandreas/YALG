package nodomain.yalg;

/**
 * Created by andreas on 11.09.2015.
 */
public class ColorF {
    float red;
    float green;
    float blue;

    public float getRed() {
        return red;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public float getGreen() {
        return green;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public float getBlue() {
        return blue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    ColorF(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}
