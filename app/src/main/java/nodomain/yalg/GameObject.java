package nodomain.yalg;

import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Created by andreas on 10.09.2015.
 */
public abstract class GameObject {
    protected PointF m_Position = new PointF(0, 0);
    protected PointF m_Direction = new PointF(1, 0);

    protected boolean m_IsActive = true;
    protected boolean m_MustBeActiveForWinning = false;

    protected String m_TextureName;

    public String getTextureName() {
        return m_TextureName;
    }

    public void setTextureName(String textureName) {
        m_TextureName = textureName;
    }

    public boolean getIsActive() {
        return m_IsActive;
    }

    public void setIsActive(boolean isActive) {
        m_IsActive = isActive;
    }

    public boolean getMustBeActiveForWinning() {
        return m_MustBeActiveForWinning;
    }

    public void setMustBeActiveForWinning(boolean mustBeActiveForWinning) {
        m_MustBeActiveForWinning = mustBeActiveForWinning;
    }

    public void setPosition(PointF position) {
        m_Position = position;
    }
    PointF getPosition() {
        return m_Position;
    }

    public void setDirection(PointF direction) {
        m_Direction = direction;
        Vec2D.normalize(m_Direction);
    }
    PointF getDirection() {
        return m_Direction;
    }

    public void getRefractors(ArrayList<PointF> lineSegments, ArrayList<Float> coefficients) {}
    public void getRays(ArrayList<PointF> origins, ArrayList<PointF> dirs) {}
    public void render() {}

}
