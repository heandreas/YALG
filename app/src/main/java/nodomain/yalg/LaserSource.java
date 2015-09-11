package nodomain.yalg;

import android.graphics.Color;

/**
 * Created by andreas on 11.09.2015.
 */
public class LaserSource extends GameObject {
    Color m_Color;

    void setColor(Color color) {
        m_Color = color;
    }

    Color getColor() {
        return m_Color;
    }


}
