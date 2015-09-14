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

    public int getRays(ArrayList<PointF> origins, ArrayList<PointF> dirs, ArrayList<PointF> receptorLinks, ArrayList<ColorF> receptorLinkCol) {
        boolean bTriggered = true;
        //iterate receptors for this laser
        for (Receptor r : m_RequiredTriggers) {
            receptorLinks.add(this.getPosition());
            receptorLinks.add(r.getPosition());

            if (!r.getIsActive()) {
                bTriggered = false;
                receptorLinkCol.add(new ColorF(0.2f, 0.2f, 0.2f));
            }
            else
                receptorLinkCol.add(new ColorF(0.5f, 0.5f, 0.5f));
        }

        if(bTriggered){
            origins.add(m_Position);
            dirs.add(m_Rotation);
            return 1;
        }
        return 0;
    }

    void addTrigger(Receptor trigger) {
        m_RequiredTriggers.add(trigger);
    }
}
