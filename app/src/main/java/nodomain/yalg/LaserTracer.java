package nodomain.yalg;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;
import android.graphics.PointF;


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

    //TODO: add color

    //geometry as line segments (two per line segment)
    public static Result
    traceRecursion(PointF vLaserSource, PointF vLaserDir, float fIntensity, PointF[] geometry, float[] afRefractiveIndices){
        List<PointF> lOutLines = new ArrayList<PointF>();
        List<Float> lOutIntensities = new ArrayList<Float>();

        if(fIntensity < 0.01f) {
            Result res = new Result();
            res.intensities = lOutIntensities;
            res.lineSegments = lOutLines;

            return res;
        }

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
            lOutLines.add(Vec2D.add(vLaserSource, Vec2D.mul(2, vLaserDir)) );
        }
        else
        {
            //there was a hit somewhere
            //first re-evaluate
            PointF line0 = geometry[iHitIndex*2];
            PointF line1 = geometry[iHitIndex*2 + 1];

            //calculate normalized surface normal
            PointF vLine = Vec2D.subtract(line1, line0);
            PointF vSurfaceNormal = Vec2D.perpendicular(vLine);
            Vec2D.normalize(vSurfaceNormal);

            //calculate direction of reflection
            PointF vReflected = Vec2D.subtract(Vec2D.mul(2.0f, Vec2D.mul(Vec2D.dot(vSurfaceNormal, vLaserDir), vSurfaceNormal)), vLaserDir);

            //calculate angle of refraction
            double fImpactAngle = Math.acos(Vec2D.dot(vSurfaceNormal, vLaserDir));
            double fRefractionAngle = Math.asin(Math.sin(fImpactAngle) / afRefractiveIndices[iHitIndex]);

            //calculate direction of refraction
            double fSurfaceAngle = Math.atan2(vSurfaceNormal.x, vSurfaceNormal.y);
            PointF vRefracted = new PointF((float)Math.sin(fSurfaceAngle + fRefractionAngle), (float)Math.cos(fSurfaceAngle + fRefractionAngle));

            //calculate amount of light reflected
            float fReflected = - (float) (Math.sin(fImpactAngle - fRefractionAngle) / Math.sin(fImpactAngle + fRefractionAngle) );
            float fRefracted = 1.0f - fReflected;

            //calculate point of impact
            float fIntersection = intersectRayLine(vLaserSource, vLaserDir, line0, line1);
            PointF vIntersection = Vec2D.mul(fIntersection, Vec2D.add(line0 , Vec2D.subtract(line1, line0)) );

            //spam line end
            lOutLines.add(vIntersection);

            //continue with recursion, reflection
            Result res = traceRecursion(vIntersection, vReflected, fReflected * fIntensity, geometry, afRefractiveIndices);
            //merge results
            lOutLines.addAll(res.lineSegments);
            lOutIntensities.addAll(res.intensities);

            //continue with recursion, refraction
            res = traceRecursion(vIntersection, vRefracted, fRefracted*fIntensity, geometry, afRefractiveIndices);
            //merge results
            lOutLines.addAll(res.lineSegments);
            lOutIntensities.addAll(res.intensities);
        }

        Result res = new Result();
        res.intensities = lOutIntensities;
        res.lineSegments = lOutLines;

        return res;
    }
}
