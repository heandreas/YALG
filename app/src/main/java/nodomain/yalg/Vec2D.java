package nodomain.yalg;

import android.graphics.PointF;

import java.lang.Math;
/**
 * Created by user-mutti on 10.09.2015.
 */
public class Vec2D {
    //A-B
    static public PointF add(PointF vectorA, PointF vectorB) {
        return new PointF(vectorA.x - vectorB.x, vectorA.y - vectorB.y);
    }

    static public PointF add(PointF vectorA, PointF vectorB) {
        return new PointF(vectorA.x + vectorB.x, vectorA.y + vectorB.y);
    }

    static public PointF mul(float x, PointF vector) {
        return new PointF(x * vector.x, x * vector.y);
    }

    static public float dot(PointF vectorA, PointF vectorB) {
        return vectorA.x * vectorB.x + vectorA.y * vectorB.y;
    }

    static public PointF perpendicular(PointF vector) {
        return new PointF(-vector.y, vector.x);
    }
}
