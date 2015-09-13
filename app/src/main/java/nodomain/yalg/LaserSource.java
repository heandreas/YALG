package nodomain.yalg;

import android.graphics.Color;
import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Created by andreas on 11.09.2015.
 */
public class LaserSource extends GameObject {
    public void getRays(ArrayList<PointF> origins, ArrayList<PointF> dirs) {
        origins.add(m_Position);
        dirs.add(m_Rotation);
    }

}
