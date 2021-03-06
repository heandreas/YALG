package nodomain.yalg;

import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Created by andreas on 13.09.2015.
 */
public class Physical extends GameObject {
    ArrayList<PointF> points = new ArrayList<PointF>();

    float refractionIndex = 1.0f;

    public float getRefractionIndex() {
        return refractionIndex;
    }

    public void setRefractionIndex(float refractionIndex) {
        this.refractionIndex = refractionIndex;
    }

    public void getRefractors(ArrayList<PointF> lineSegments, ArrayList<Float> coefficients) {
        PointF scale = Vec2D.mul(2.0f, m_Extents);
        for (int i = 0; i < points.size(); i++) {
            int nextIndex = i+1;
            if (nextIndex == points.size())
                nextIndex = 0;
            lineSegments.add(Vec2D.add(m_Position, Vec2D.scale(Vec2D.rotatePoint(points.get(i), m_Rotation), scale)));
            lineSegments.add(Vec2D.add(m_Position, Vec2D.scale(Vec2D.rotatePoint(points.get(nextIndex), m_Rotation), scale)));
            coefficients.add(refractionIndex);
        }
    }

    public void clearMesh() {
        points.clear();
    }

    public void addMeshPoint(PointF point) {
        points.add(new PointF(point.x - 0.5f, (1.0f - point.y) - 0.5f));
    }
}
