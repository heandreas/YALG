package nodomain.yalg;

import java.util.Vector;

/**
 * Data format sources:
 * x,y, dx,dy
 *
 * geometry format
 * x0,y0, y1,y1
 *
 * output format
 * x,y, intensity
 */
public class LaserTracer {
    public static float intersectRayLine(float[] rayPos, float[] rayDir,
                                         float[] line0, float[] line1){
        return Vec2D.dot(Vec2D.subtract(rayPos, line0), Vec2D.perpendicular(rayDir)) /
                Vec2D.dot(Vec2D.subtract(line1, line0), Vec2D.perpendicular(rayDir));
    }

    public static Vector<float[]>
    trace(Vector<float[]> laserSources, int[] laserColors, Vector<float[]> geometry, float[] afRefractiveIndices){
        Vector<float[]> vOutput = new Vector<float[]>();

        //trace each source
        for (float[] afSource:
                laserSources) {
            float[] vSource = new float[2];
            vSource[0] = afSource[0];
            vSource[1] = afSource[1];

            float[] vDir = new float[2];
            vDir[0] = afSource[2];
            vDir[1] = afSource[3];

            //check each geometry line against this ray


            vOutput.add(vSource);
        }
        
        return vOutput;
    }
}
