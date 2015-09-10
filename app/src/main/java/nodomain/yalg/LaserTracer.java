package nodomain.yalg;

import java.util.ArrayList;
import java.util.Vector;
import java.util.List;
import android.graphics.PointF;

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
    public static float intersectRayLine(PointF rayPos, PointF rayDir,
                                         PointF line0, PointF line1){
        return Vec2D.dot(Vec2D.subtract(rayPos, line0), Vec2D.perpendicular(rayDir)) /
                Vec2D.dot(Vec2D.subtract(line1, line0), Vec2D.perpendicular(rayDir));
    }

     public static class Result{
        public List<PointF> lineSegments;
        public List<Float> intensities;
    }

    //geometry as line segments (two per line segment)
    public static Result
    traceRecursion(PointF vLaserSource, PointF vLaserDir, float fIntensity, PointF[] geometry, float[] afRefractiveIndices){
        List<PointF> lOutLines = new ArrayList<PointF>();
        List<Float> lOutIntensities = new ArrayList<Float>();

        //populate output structure
        lOutLines.add(vLaserSource);
        lOutIntensities.add(fIntensity);

        float fNearestHit = Float.MAX_VALUE;
        int iHitIndex = -1;
        //check each geometry line against this ray
        for (int iLine = 0; iLine < geometry.length/2; iLine++) {
            //check if source on right side
            PointF line0 = geometry[iLine*2];
            PointF line1 = geometry[iLine*2 + 1];

            if(Vec2D.dot(Vec2D.perpendicular(Vec2D.subtract(line1, line0) ), Vec2D.subtract(vLaserSource, line0)) < 0)
                continue;

            float fIntersection = intersectRayLine(vLaserSource, vLaserDir, line0, line1);
            if(fIntersection > 0.0f && fIntersection < 1.0f){
                //stuff intersects
                //calculate intersection point
                PointF vIntersection = Vec2D.mul(fIntersection, Vec2D.add(line0 , Vec2D.subtract(line1, line0)) );
                //calculate distance to source
                float fHitDistance = Vec2D.subtract(vLaserSource, vIntersection).length();
                if(Vec2D.subtract(vLaserSource, vIntersection).length() < fNearestHit) {
                    fNearestHit = fHitDistance;
                    iHitIndex = iLine;
                }
            }
        }
        //check out if we hit
        if(iHitIndex == -1)
        {
            //bigger than screen
            lOutLines.add(Vec2D.add(vLaserSource, Vec2D.mul(2, vLaserDir));
        }
        else
        {
            //there was a hit somewhere
            //first re-evaluate
            PointF line0 = geometry[iHitIndex*2];
            PointF line1 = geometry[iHitIndex*2 + 1];

            PointF vLine = Vec2D.subtract(line1, line0);
            Vec2D.normalize(vLine);
            PointF vLineSource = Vec2D.subtract(vLaserSource, line0);
            Vec2D.normalize(vLineSource);

            float fAngle = Vec2D.dot(Vec2D.perpendicular(vLine), vLineSource);


            float fIntersection = intersectRayLine(vLaserSource, vLaserDir, line0, line1);
            PointF vIntersection = Vec2D.mul(fIntersection, Vec2D.add(line0 , Vec2D.subtract(line1, line0)) );

            //spam line end
            lOutLines.add(vIntersection);

            //calculate direction of reflected ray
            //calculate inbound angle
        }

        Result res = new Result();
        res.intensities = lOutIntensities;
        res.lineSegments = lOutLines;

        return res;
    }

    public static Result
    trace(Vector<float[]> laserSources, int[] laserColors, Vector<float[]> geometry, float[] afRefractiveIndices){


        //trace each source
        for (float[] afSource:
                laserSources) {
            float[] vSource = new float[2];
            vSource[0] = afSource[0];
            vSource[1] = afSource[1];

            //vOutput.add(outData);

            float[] vDir = new float[2];
            vDir[0] = afSource[2];
            vDir[1] = afSource[3];



        }

        Result res = new Result();
        
        return res;
    }
}
