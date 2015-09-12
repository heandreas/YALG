package nodomain.yalg;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import android.graphics.PointF;


public class LaserTracer {
    //returns barycentric position on line
    public static float intersectRayOnLine(PointF rayPos, PointF rayDir,
                                           PointF line0, PointF line1){
        PointF v1 = Vec2D.subtract(rayPos, line0);
        PointF v2 = Vec2D.subtract(line1, line0);
        PointF v3 = Vec2D.perpendicular(rayDir);

        return Vec2D.dot(v1, v3) / Vec2D.dot(v2, v3);
    }

    public static float intersectRayLine(PointF rayPos, PointF rayDir,
                                         PointF line0, PointF line1){
        //cull hits on the back side of the ray
        float fPosOnRay = intersectRayOnLine(line0, Vec2D.subtract(line1, line0), rayPos, Vec2D.add(rayPos, rayDir) );

        if(fPosOnRay < 0)
            return Float.NEGATIVE_INFINITY;
        return intersectRayOnLine(rayPos, rayDir, line0, line1);
    }

    public static class Result{
        public List<PointF> lineSegments;
        public List<Float> intensities;
    }

    //TODO: add color

    //geometry as line segments (two per line segment)
    public static Result
    traceRecursion(PointF vLaserSource, PointF vLaserDir, float fRefractionMultiplier, float fIntensity, PointF[] geometry, float[] afRefractiveIndices, int iRecursionDepth){
        List<PointF> lOutLines = new ArrayList<PointF>();
        List<Float> lOutIntensities = new ArrayList<Float>();

        if(fIntensity < 0.05f || iRecursionDepth > 20) {
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

            float fIntersection = intersectRayLine(vLaserSource, vLaserDir, line0, line1);
            if(fIntersection > 0.0f && fIntersection < 1.0f){
                //stuff intersects
                //calculate intersection PointF
                PointF vIntersection = Vec2D.add(line0, Vec2D.mul(fIntersection, Vec2D.subtract(line1, line0)) );
                //calculate distance to source
                float fHitDistance = Vec2D.subtract(vLaserSource, vIntersection).length();
                if(Vec2D.subtract(vLaserSource, vIntersection).length() < fNearestHit && fHitDistance > 0.001f) {
                    fNearestHit = fHitDistance;
                    iHitIndex = iLine;
                }
            }
        }
        //check out if we hit
        if(iHitIndex == -1)
        {
            //bigger than screen
            lOutLines.add(Vec2D.add(vLaserSource, Vec2D.mul(3, vLaserDir)) );
        }
        else
        {
            //there was a hit somewhere
            //first re-evaluate
            PointF line0 = geometry[iHitIndex*2];
            PointF line1 = geometry[iHitIndex*2 + 1];

            //calculate normalized surface normal
            PointF vLine = Vec2D.subtract(line1, line0);
            PointF vSurfaceNormal = Vec2D.flip(Vec2D.perpendicular(vLine));
            Vec2D.normalize(vSurfaceNormal);

            //calculate point of impact
            float fIntersection = intersectRayLine(vLaserSource, vLaserDir, line0, line1);
            PointF vIntersection = Vec2D.add(line0, Vec2D.mul(fIntersection, Vec2D.subtract(line1, line0)) );


            //calculate direction of reflection
            PointF vReflected = Vec2D.add(Vec2D.mul(-2.0f, Vec2D.mul(Vec2D.dot(vSurfaceNormal, vLaserDir), vSurfaceNormal)), vLaserDir);

            double fImpactAngle = Math.acos(Vec2D.dot(vSurfaceNormal, Vec2D.flip(vLaserDir)));
            double fRefractionAngle = 0.0f;
            float fRefracted = 0.0f;
            boolean bTotalReflection = false;

            //calculate which side of the object we're on
            if(Vec2D.dot(vSurfaceNormal, Vec2D.subtract(vLaserSource, line0) ) < 0){
                //from medium to air
                //angle will become bigger
                double fSinAngle = Math.sin(fImpactAngle) * (afRefractiveIndices[iHitIndex] * fRefractionMultiplier);

                if(fSinAngle > 1.0f || fSinAngle < -1.0f)
                    bTotalReflection = true;

                else{
                    fRefractionAngle = Math.asin(fSinAngle);
                    float fFlippedImpactAngle = (float) Math.asin(Math.sin(fImpactAngle));

                    fRefracted = (float) (2.0f * Math.sin(fFlippedImpactAngle)*Math.cos( fRefractionAngle ) / Math.sin(fFlippedImpactAngle + fRefractionAngle) );

                    //set refraction angle for direction calculation
                    fRefractionAngle = Math.PI - fRefractionAngle;
                }
            }
            else{
                //from air to medium
                //angle will become smaller
                fRefractionAngle = Math.asin(Math.sin(fImpactAngle) / (afRefractiveIndices[iHitIndex] * fRefractionMultiplier) );
                fRefracted = (float) (2.0f * Math.sin(fRefractionAngle)*Math.cos( fImpactAngle ) / Math.sin(fImpactAngle + fRefractionAngle) );
            }

            //calculate direction of refraction
            double fInvertedSurfaceAngle = Math.atan2(-vSurfaceNormal.y, -vSurfaceNormal.x);
            PointF vRefracted = new PointF((float)Math.cos(fInvertedSurfaceAngle - fRefractionAngle), (float)Math.sin(fInvertedSurfaceAngle - fRefractionAngle));

            //calculate amount of light reflected
            float fReflected = 1.0f - fRefracted;

            //spam line end
            lOutLines.add(vIntersection);

            //continue with recursion, reflection
            Result res = traceRecursion(vIntersection, vReflected, fRefractionMultiplier, fReflected * fIntensity, geometry, afRefractiveIndices, iRecursionDepth+1);
            //merge results
            lOutLines.addAll(res.lineSegments);
            lOutIntensities.addAll(res.intensities);

            //continue with recursion, refraction
            if(!bTotalReflection) {
                res = traceRecursion(vIntersection, vRefracted, fRefractionMultiplier, fRefracted * fIntensity, geometry, afRefractiveIndices, iRecursionDepth+1);
                //merge results
                lOutLines.addAll(res.lineSegments);
                lOutIntensities.addAll(res.intensities);
            }
        }

        Result res = new Result();
        res.intensities = lOutIntensities;
        res.lineSegments = lOutLines;

        return res;
    }
}