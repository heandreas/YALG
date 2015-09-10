package nodomain.yalg;

import java.lang.Math;
/**
 * Created by user-mutti on 10.09.2015.
 */
public class Vec2D {
    static public float[] normalize(float[] vector2D){
        float fInverseLen = 1.0f / (float)Math.hypot((double) vector2D[0], (double) vector2D[1]);

        float[] output = new float[2];
        output[0] = fInverseLen * vector2D[0];
        output[1] = fInverseLen * vector2D[1];
        return output;
    }

    //A-B
    static public float[] subtract(float[] vectorA, float[] vectorB){
        float[] output = new float[2];
        output[0] = vectorA[0] - vectorB[0];
        output[1] = vectorA[1] - vectorB[1];
        return output;
    }

    //A.B
    static public float dot(float[] vectorA, float[] vectorB){
        return vectorA[0] * vectorB[0] + vectorA[1] * vectorB[1];
    }

    //A-B
    static public float[] perpendicular(float[] vector){
        float[] output = new float[2];
        output[0] = -vector[1];
        output[1] =  vector[0];
        return output;
    }
}
