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
    traceRecursion(laserSource, float fIntensity, Vector<float[]> geometry, float[] afRefractiveIndices){

    }

    public static Vector<float[]>
    trace(Vector<float[]> laserSources, int[] laserColors, Vector<float[]> geometry, float[] afRefractiveIndices){
        Vector<float[]> vOutput = new Vector<float[]>();
        float[] outData = new float[3];

        //trace each source
        for (float[] afSource:
                laserSources) {
            float[] vSource = new float[2];
            vSource[0] = afSource[0];
            vSource[1] = afSource[1];

            outData[0] = vSource.x;
            outData[1] = vSource.y;
            outData[2] = 1.0f;


            vOutput.add(outData);

            float[] vDir = new float[2];
            vDir[0] = afSource[2];
            vDir[1] = afSource[3];

            //check each geometry line against this ray
            for (float[] afGeom:
                    geometry) {
                //check if source on right side
                float[] line0 = new float[2];
                line0[0] = afGeom[0];
                line0[1] = afGeom[1];

                float[] line1 = new float[2];
                line1[0] = afGeom[2];
                line1[1] = afGeom[3];

                if(Vec2D.dot(Vec2D.perpendicular(Vec2D.subtract(line1 - line0) ), Vec2D.subtract(vSource - line0)) < 0)
                    continue;

                float fIntersection = intersectRayLine(vSource, vDir, line0, line1);
                if(fIntersection > 0.0f && fIntersection < 1.0f){
                    //stuff intersects
                    //calculate intersection point
                    vIntersection = line0 + (line1 - line0) fIntersection;
                    //finalize current line segment
                    vOutput.add(vSource);
                }

            }


        }
        
        return vOutput;
    }
}
