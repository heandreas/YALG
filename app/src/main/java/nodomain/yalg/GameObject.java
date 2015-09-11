package nodomain.yalg;

import android.graphics.PointF;

/**
 * Created by andreas on 10.09.2015.
 */
public abstract class GameObject {
    protected PointF m_Position;
    protected PointF m_Direction;

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
    }
    PointF getDirection() {
        return m_Direction;
    }
}
