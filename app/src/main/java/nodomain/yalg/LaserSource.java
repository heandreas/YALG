package nodomain.yalg;

import android.graphics.Color;
import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Created by andreas on 11.09.2015.
 */
public class LaserSource extends Physical {

    ArrayList<Receptor> m_RequiredTriggers = new ArrayList<Receptor>();

    LaserSource(){
        refractionIndex = -1;
    }

    public void getRays(ArrayList<PointF> origins, ArrayList<PointF> dirs) {
        for (Receptor r : m_RequiredTriggers) {
            if (!r.getIsActive())
                return;
        }
        origins.add(m_Position);
        dirs.add(m_Rotation);
    }

    void addTrigger(Receptor trigger) {
        m_RequiredTriggers.add(trigger);
    }
}
