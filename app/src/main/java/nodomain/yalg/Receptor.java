package nodomain.yalg;

import android.graphics.PointF;

/**
 * Created by andreas on 11.09.2015.
 */
public class Receptor extends Physical {
    GameObject statusBarRed = new GameObject();
    GameObject statusBarGreen = new GameObject();
    GameObject statusBarBlue = new GameObject();

    static final float BAR_HEIGHT = 26.0f / 128.0f;
    static final float BAR_WIDTH = 75.0f / 128.0f;
    static final float BAR_X = 24.0f / 128.0f;
    static final float BAR_Y = 22.0f / 128.0f;

    ColorF requiredColor = new ColorF(1, 1, 1);

    float absorbedRed = 0.0f;
    float absorbedGreen = 0.25f;
    float absorbedBlue = 0.0f;

    void resetAbsorbedCounters() {
        absorbedRed = 0.0f;
        absorbedGreen = 0.0f;
        absorbedBlue = 0.0f;
    }

    void addRed(float value) {
        absorbedRed += value;
    }
    void addGreen(float value) {
        absorbedGreen += value;
    }
    void addBlue(float value) {
        absorbedBlue += value;
    }

    void setRequiredColor(ColorF value) {
        requiredColor = value;
    }

    Receptor() {
        refractionIndex = -1;
        statusBarRed.setTextureName("onepx");
        statusBarGreen.setTextureName("onepx");
        statusBarBlue.setTextureName("onepx");

        statusBarRed.setColor(new ColorF(1, 0, 0));
        statusBarGreen.setColor(new ColorF(0, 1, 0));
        statusBarBlue.setColor(new ColorF(0, 0, 1));
    }

    public void render(int posHandle, int uvHandle, int laserColHandle) {
        PointF scale = Vec2D.mul(2.0f, m_Extents);

        float redStatus = 1.0f;
        if (requiredColor.red > 0.0f)
            redStatus = Math.min(1.0f, 1.0f - (requiredColor.red - absorbedRed) / requiredColor.red);
        float greenStatus = 1.0f;
        if (requiredColor.green > 0.0f)
            greenStatus = Math.min(1.0f, 1.0f - (requiredColor.green - absorbedGreen) / requiredColor.green);
        float blueStatus = 1.0f;
        if (requiredColor.blue > 0.0f)
            blueStatus = Math.min(1.0f, 1.0f - (requiredColor.blue - absorbedBlue) / requiredColor.blue);

        float barWidthRed = BAR_WIDTH * redStatus;
        float barWidthGreen = BAR_WIDTH * greenStatus;
        float barWidthBlue = BAR_WIDTH * blueStatus;

        PointF minPos = Vec2D.subtract(m_Position, m_Extents);

        float minY = minPos.y + scale.y * (BAR_Y + BAR_HEIGHT * 0.5f);

        float xRed = minPos.x + scale.x * (BAR_X + barWidthRed * 0.5f);
        float xGreen = minPos.x + scale.x * (BAR_X + barWidthGreen * 0.5f);
        float xBlue = minPos.x + scale.x * (BAR_X + barWidthBlue * 0.5f);

        minPos.x += scale.x * (BAR_X + BAR_WIDTH * 0.5f);
        minPos.y += scale.y * (BAR_Y + BAR_HEIGHT * 0.5f);

        float barExtentsX = m_Extents.x * BAR_WIDTH;
        float barExtentsY = m_Extents.y * BAR_HEIGHT;
        statusBarRed.setExtents(barExtentsX * redStatus, barExtentsY);
        statusBarGreen.setExtents(barExtentsX * greenStatus, barExtentsY);
        statusBarBlue.setExtents(barExtentsX * blueStatus, barExtentsY);

        float yPos = minY;
        statusBarRed.setPosition(xRed, yPos);
        yPos += scale.y * BAR_HEIGHT;
        statusBarGreen.setPosition(xGreen, yPos);
        yPos += scale.y * BAR_HEIGHT;
        statusBarBlue.setPosition(xBlue, yPos);

        statusBarRed.render(posHandle, uvHandle, laserColHandle);
        statusBarGreen.render(posHandle, uvHandle, laserColHandle);
        statusBarBlue.render(posHandle, uvHandle, laserColHandle);

        super.render(posHandle, uvHandle, laserColHandle);
    }
}
