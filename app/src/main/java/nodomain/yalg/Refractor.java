package nodomain.yalg;

import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Created by andreas on 11.09.2015.
 */
public class Refractor extends GameObject {
    ArrayList<PointF> points = new ArrayList<PointF>();
    float refractionIndex = 1.0f;

    public float getRefractionIndex() {
        return refractionIndex;
    }

    public void setRefractionIndex(float refractionIndex) {
        this.refractionIndex = refractionIndex;
    }

    public void clearMesh() {
        points.clear();
    }

    public void addMeshPoint(PointF point) {
        points.add(point);
    }

    public void getRefractors(ArrayList<PointF> lineSegments, ArrayList<Float> coefficients) {
        for (int i = 0; i < points.size(); i++) {
            int nextIndex = i+1;
            if (nextIndex == points.size())
                nextIndex = 0;
            lineSegments.add(Vec2D.add(m_Position, Vec2D.rotatePoint(points.get(i), m_Direction)));
            lineSegments.add(Vec2D.add(m_Position, Vec2D.rotatePoint(points.get(nextIndex), m_Direction)));
            coefficients.add(refractionIndex);
        }
    }
}
