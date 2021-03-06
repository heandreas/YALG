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
        //two vertices per segment
        public List<PointF> lineSegments;
        //one intensity per segment
        public List<Float> intensities;
        //two flight lengths per segment
        public List<Float> lightLengths;
        //intensities of hits
        public List<Float> hitIntensities;
        //segments of hits
        public List<Integer> hitSegments;
    }

    //geometry as line segments (two per line segment)
    public static Result
    traceRecursion(PointF vLaserSource, PointF vLaserDir, float fRefractionMultiplier, PointF[] geometry, float[] afRefractiveIndices, float fIntensity, int iRecursionDepth, float fFlightLength){

        Result res = new Result();
        //init return lists
        res.lineSegments = new ArrayList<>();
        res.intensities = new ArrayList<>();
        res.lightLengths = new ArrayList<>();
        res.hitIntensities = new ArrayList<>();
        res.hitSegments = new ArrayList<>();

        //important for angle calculation
        Vec2D.normalize(vLaserDir);

        //recursion limiter
        if(fIntensity < 0.05f || iRecursionDepth > 20)
            return res;

        //populate output structure
        res.lineSegments.add(vLaserSource);
        res.intensities.add(fIntensity);
        res.lightLengths.add(fFlightLength);

        //initialize to infinity
        float fNearestHit = Float.MAX_VALUE;
        int iHitIndex = -1;

        //check each geometry line against this ray
        for (int iLine = 0; iLine < geometry.length/2; iLine++) {
            //check if source on right side
            PointF line0 = geometry[iLine*2];
            PointF line1 = geometry[iLine*2 + 1];

            //calculate intersection with geometry line
            float fIntersection = intersectRayLine(vLaserSource, vLaserDir, line0, line1);

            if(fIntersection > 0.0f && fIntersection < 1.0f){
                //stuff intersects
                //calculate intersection PointF
                PointF vIntersection = Vec2D.add(line0, Vec2D.mul(fIntersection, Vec2D.subtract(line1, line0)) );
                //calculate distance to source
                float fHitDistance = Vec2D.subtract(vLaserSource, vIntersection).length();
                if(Vec2D.subtract(vLaserSource, vIntersection).length() < fNearestHit && fHitDistance > 0.001f) {
                    //new minimum distance
                    fNearestHit = fHitDistance;
                    iHitIndex = iLine;
                }
            }
        }
        //check if we hit
        if(iHitIndex == -1)
        {
            //bigger than screen
            final float fInfLength = 3.0f;
            res.lineSegments.add(Vec2D.add(vLaserSource, Vec2D.mul(fInfLength, vLaserDir)) );
            res.lightLengths.add(fFlightLength + fInfLength);
        }
        else
        {
            //there was a hit somewhere
            //first re-evaluate
            PointF line0 = geometry[iHitIndex*2];
            PointF line1 = geometry[iHitIndex*2 + 1];

            res.hitSegments.add(iHitIndex);
            res.hitIntensities.add(fIntensity);
            //calculate point of impact
            float fIntersection = intersectRayLine(vLaserSource, vLaserDir, line0, line1);
            PointF vIntersection = Vec2D.add(line0, Vec2D.mul(fIntersection, Vec2D.subtract(line1, line0)) );

            //spam line end
            res.lineSegments.add(vIntersection);
            float fNextLength = fFlightLength + fNearestHit;
            res.lightLengths.add(fNextLength);

            if(afRefractiveIndices[iHitIndex] < 0.0f)
                return res;

            //calculate normalized surface normal
            PointF vLine = Vec2D.subtract(line1, line0);
            PointF vSurfaceNormal = Vec2D.flip(Vec2D.perpendicular(vLine));
            Vec2D.normalize(vSurfaceNormal);

            //calculate direction of reflection
            PointF vReflected = Vec2D.add(Vec2D.mul(-2.0f, Vec2D.mul(Vec2D.dot(vSurfaceNormal, vLaserDir), vSurfaceNormal)), vLaserDir);

            double fImpactAngle = Math.acos(Vec2D.dot(vSurfaceNormal, Vec2D.flip(vLaserDir)));

            double fRefractionAngle = 0.0f;
            float fRefracted = 0.0f;
            boolean bTotalReflection = false;

            if(afRefractiveIndices[iHitIndex] < 5.0f) {
                //calculate which side of the object we're on
                if (Vec2D.dot(vSurfaceNormal, Vec2D.subtract(vLaserSource, line0)) < 0) {
                    //from medium to air
                    //angle will become bigger
                    double fSinAngle = Math.sin(fImpactAngle) * (afRefractiveIndices[iHitIndex] * fRefractionMultiplier);

                    if (fSinAngle > 1.0f || fSinAngle < -1.0f)
                        //refraction would be back into object
                        bTotalReflection = true;
                    else {
                        //calculate refraction
                        fRefractionAngle = Math.asin(fSinAngle);
                        float fFlippedImpactAngle = (float) Math.asin(Math.sin(fImpactAngle));
                        fRefracted = (float) (2.0f * Math.sin(fFlippedImpactAngle) * Math.cos(fRefractionAngle) / Math.sin(fFlippedImpactAngle + fRefractionAngle));

                        //set refraction angle for direction calculation
                        fRefractionAngle = Math.PI - fRefractionAngle;
                    }
                } else {
                    //from air to medium
                    //angle will become smaller
                    fRefractionAngle = Math.asin(Math.sin(fImpactAngle) / (afRefractiveIndices[iHitIndex] * fRefractionMultiplier));
                    fRefracted = (float) (2.0f * Math.sin(fRefractionAngle) * Math.cos(fImpactAngle) / Math.sin(fImpactAngle + fRefractionAngle));
                }
            }
            else
                bTotalReflection = true;

            //give the refraction angle a sign
            if(Vec2D.dot(vLine, vLaserDir) < 0)
                fRefractionAngle = -fRefractionAngle;

            //calculate direction of refraction
            double fInvertedSurfaceAngle = Math.atan2(-vSurfaceNormal.y, -vSurfaceNormal.x);
            PointF vRefracted = new PointF((float)Math.cos(fInvertedSurfaceAngle - fRefractionAngle), (float)Math.sin(fInvertedSurfaceAngle - fRefractionAngle));

            //calculate amount of light reflected
            float fReflected = 1.0f - fRefracted;

            //continue with recursion, reflection
            Result resReflection = traceRecursion(vIntersection, vReflected, fRefractionMultiplier, geometry, afRefractiveIndices, fReflected * fIntensity, iRecursionDepth+1, fNextLength);
            //merge results
            res.lineSegments.addAll(resReflection.lineSegments);
            res.intensities.addAll(resReflection.intensities);
            res.lightLengths.addAll(resReflection.lightLengths);
            res.hitSegments.addAll(resReflection.hitSegments);
            res.hitIntensities.addAll(resReflection.hitIntensities);

            //continue with recursion, refraction
            if(!bTotalReflection) {
                Result resRefraction = traceRecursion(vIntersection, vRefracted, fRefractionMultiplier, geometry, afRefractiveIndices, fRefracted * fIntensity, iRecursionDepth+1, fNextLength);
                //merge results
                res.lineSegments.addAll(resRefraction.lineSegments);
                res.intensities.addAll(resRefraction.intensities);
                res.lightLengths.addAll(resRefraction.lightLengths);
                res.hitSegments.addAll(resRefraction.hitSegments);
                res.hitIntensities.addAll(resRefraction.hitIntensities);
            }
        }
        return res;
    }

    //method for outside callers
    //occludes iteration setup
    public static Result
    traceRecursion(PointF vLaserSource, PointF vLaserDir, float fRefractionMultiplier, PointF[] geometry, float[] afRefractiveIndices, float fIntensity){
        return traceRecursion(vLaserSource, vLaserDir, fRefractionMultiplier, geometry, afRefractiveIndices, fIntensity, 0, 0.0f);
    }
}