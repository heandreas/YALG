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
    static final float BAR_WIDTH = 84.0f / 128.0f;
    static final float BAR_X = 24.0f / 128.0f;
    static final float BAR_Y = 22.0f / 128.0f;

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
        PointF minPos = Vec2D.subtract(m_Position, m_Extents);
        minPos.x += scale.x * (BAR_X + BAR_WIDTH * 0.5f);
        minPos.y += scale.y * (BAR_Y + BAR_HEIGHT * 0.5f);

        PointF barExtents = new PointF(m_Extents.x * BAR_WIDTH, m_Extents.y * BAR_HEIGHT);
        statusBarRed.setExtents(barExtents);
        statusBarGreen.setExtents(barExtents);
        statusBarBlue.setExtents(barExtents);

        float yPos = minPos.y;
        statusBarRed.setPosition(minPos.x, yPos);
        yPos += scale.y * BAR_HEIGHT;
        statusBarGreen.setPosition(minPos.x, yPos);
        yPos += scale.y * BAR_HEIGHT;
        statusBarBlue.setPosition(minPos.x, yPos);

        statusBarRed.render(posHandle, uvHandle, laserColHandle);
        statusBarGreen.render(posHandle, uvHandle, laserColHandle);
        statusBarBlue.render(posHandle, uvHandle, laserColHandle);

        super.render(posHandle, uvHandle, laserColHandle);
    }
}
