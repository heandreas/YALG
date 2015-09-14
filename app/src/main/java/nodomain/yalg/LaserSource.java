package nodomain.yalg;

import android.graphics.Color;
import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Created by andreas on 11.09.2015.
 */
public class LaserSource extends GameObject {

    ArrayList<Receptor> m_RequiredTriggers = new ArrayList<Receptor>();

    public void getRay(PointF origin, PointF dir) {
        for (Receptor r : m_RequiredTriggers) {
            if (!r.getIsActive())
                return;
        }
        origin.set(m_Position);
        dir.set(m_Rotation);
    }

    void addTrigger(Receptor trigger) {
        m_RequiredTriggers.add(trigger);
    }

}
