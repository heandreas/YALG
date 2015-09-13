package nodomain.yalg;

import android.graphics.Color;
import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Created by andreas on 11.09.2015.
 */
public class LaserSource extends GameObject {
    public void getRay(PointF origin, PointF dir) {
        origin.set(m_Position);
        dir.set(m_Rotation);
    }

}
