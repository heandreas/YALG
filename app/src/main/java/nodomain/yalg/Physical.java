package nodomain.yalg;

import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Created by andreas on 13.09.2015.
 */
public class Physical extends GameObject {
    ArrayList<PointF> points = new ArrayList<PointF>();

    public void clearMesh() {
        points.clear();
    }

    public void addMeshPoint(PointF point) {
        points.add(new PointF(point.x - 0.5f, (1.0f - point.y) - 0.5f));
    }
}
