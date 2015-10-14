package nodomain.yalg;

import nodomain.yalg.util.SystemUiHider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class GameActivity extends Activity {

    YalgGLSurface glSurface;

    List<GameObject> gameObjects = null;

    PointF selectedPointOffset = new PointF();
    PointF selectedObjectBaseDir = new PointF();
    PointF inverseInitialPointerDir = new PointF();

    int activePrimaryPointerID = -1;
    int activeSecondaryPointerID = -1;

    private Handler handler = new Handler();

    int currLevelID = -1;

    boolean rotationMode_Debug = false;

    GameObject selectedObject = null;

    public void computeLasers(List<PointF> laserSegments, List<Float> laserLengths, List<ColorF> laserColors) {
        if (gameObjects == null)
            return;

        ArrayList<PointF> obstacleLines = new ArrayList<PointF>();
        ArrayList<Integer> obstacleObjectIndices = new ArrayList<Integer>();
        ArrayList<Float> coefficients = new ArrayList<Float>();

        ArrayList<PointF> lLaserOrigins = new ArrayList<PointF>();
        ArrayList<PointF> lLaserDirs = new ArrayList<PointF>();
        ArrayList<ColorF> lLaserColors = new ArrayList<ColorF>();

        ArrayList<PointF> lReceptorLinks = new ArrayList<PointF>();
        ArrayList<ColorF> lReceptorLinkCols = new ArrayList<ColorF>();

        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject go = gameObjects.get(i);
            int numVerticesOld = obstacleLines.size();
            go.getRefractors(obstacleLines, coefficients);

            int nRays = go.getRays(lLaserOrigins, lLaserDirs, lReceptorLinks, lReceptorLinkCols);

            for (int iRay=0; iRay< nRays; iRay++)
                lLaserColors.add(go.getColor());

            int numAddedSegments = (obstacleLines.size() - numVerticesOld) / 2;
            for (int j = 0; j < numAddedSegments; j++)
                obstacleObjectIndices.add(i);

            if (go instanceof Receptor) {
                Receptor r = (Receptor) go;
                r.resetAbsorbedCounters();
            }
        }
        PointF[] lineSegmentsArray = new PointF[obstacleLines.size()];
        for (int i = 0; i < obstacleLines.size(); i++)
            lineSegmentsArray[i] = obstacleLines.get(i);
        float[] coefficientsArray = new float[coefficients.size()];
        for (int i = 0; i < coefficients.size(); i++)
            coefficientsArray[i] = coefficients.get(i);

        for (int i = 0; i < lLaserOrigins.size(); i++)
        {
            //trace every color of the laser
            for(int iColor = 0; iColor < 3; iColor++) {

                float fRefractiveMultiplier = 0.98f + (float)iColor * 0.02f;
                float fIntensity;
                ColorF drawColor = new ColorF(0,0,0);

                if(iColor == 0) {
                    fIntensity = lLaserColors.get(i).getRed();
                    drawColor.setRed(fIntensity);
                }
                else if(iColor == 1) {
                    fIntensity = lLaserColors.get(i).getGreen();
                    drawColor.setGreen(fIntensity);
                }
                else{
                    fIntensity = lLaserColors.get(i).getBlue();
                    drawColor.setBlue(fIntensity);
                }

                LaserTracer.Result result = LaserTracer.traceRecursion(lLaserOrigins.get(i), lLaserDirs.get(i), fRefractiveMultiplier,
                        lineSegmentsArray, coefficientsArray, fIntensity);

                laserSegments.addAll(result.lineSegments);

                laserLengths.addAll(result.lightLengths);

                for (int j = 0; j < result.intensities.size(); j++) {
                    float intensity = result.intensities.get(j);
                    laserColors.add(new ColorF(drawColor.getRed() * intensity,
                                                drawColor.getGreen() * intensity,
                                                drawColor.getBlue() * intensity) );
                }

                for (int j = 0; j < result.hitSegments.size(); j++) {
                    int segmentIndex = result.hitSegments.get(j);
                    int objectIndex = obstacleObjectIndices.get(segmentIndex);
                    GameObject go = gameObjects.get(objectIndex);
                    if (go instanceof Receptor) {
                        Receptor r = (Receptor)go;
                        if(iColor == 0) {
                            r.addRed(result.hitIntensities.get(j));
                        }
                        else if(iColor == 1) {
                            r.addGreen(result.hitIntensities.get(j));
                        }
                        else{
                            r.addBlue(result.hitIntensities.get(j));
                        }
                    }
                }
            }
        }

        //process receptor links
        laserSegments.addAll(lReceptorLinks);
        laserColors.addAll(lReceptorLinkCols);

        for (int iLink = 0; iLink < lReceptorLinks.size() / 2; iLink++){
            laserLengths.add(new Float(0));
            float fDist = Vec2D.subtract(lReceptorLinks.get(iLink * 2 + 1), lReceptorLinks.get(iLink * 2) ).length();
            laserLengths.add(new Float(fDist * 7.0f));
        }

        boolean allActive = true;
        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject go = gameObjects.get(i);
            if (go instanceof Receptor) {
                ((Receptor)go).updateActiveStatus();
                if (!go.getIsActive()) {
                    allActive = false;
                }
            }
        }
        if (allActive && currLevelID >= 0) {
            final int memorizedLevelID = currLevelID;
            currLevelID = -1;

/*            handler.postDelayed(new Runnable() {
                @Override
                public void run() {*/
                    Intent myIntent = new Intent(GameActivity.this, UWonActivity.class);
                    myIntent.putExtra("level", memorizedLevelID);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(myIntent);
/*                }
            }, 1000);
*/
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            gameObjects = new ArrayList<GameObject>();
            GameObject goBackground = new GameObject();

            goBackground.setTextureName("background");
            goBackground.setExtents(new PointF(2.0f, 2.0f));
            goBackground.setUVScale(4.0f);

            gameObjects.add(goBackground);

            Bundle extras = getIntent().getExtras();
            currLevelID = extras.getInt("level");

            gameObjects.addAll(LevelLoader.parse(getResources().openRawResource(YALG.m_Levels[currLevelID])) );
            System.out.println("Read " + gameObjects.size() + " objects.");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO: remove debug call

        ArrayList<PointF> laserSegments = new ArrayList<PointF>();
        ArrayList<Float> laserLengths = new ArrayList<>();
        ArrayList<ColorF> laserColors = new ArrayList<ColorF>();

        computeLasers(laserSegments, laserLengths, laserColors);

        TextureFactory.getInstance().setGameActivity(this);

        glSurface = new YalgGLSurface(this);
        setContentView(glSurface);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT)
            rotationMode_Debug = true;
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT)
            rotationMode_Debug = false;
        return super.onKeyUp(keyCode, event);
    }

    PointF getNdcsFromMotionEvent(MotionEvent event, int pointerIndex) {
        PointF point = new PointF(0,0);

        try {
            point.x = MotionEventCompat.getX(event, pointerIndex);
            point.y = MotionEventCompat.getY(event, pointerIndex);
        } catch (ArrayIndexOutOfBoundsException e) {
            return point;
        }

        Display display = getWindowManager().getDefaultDisplay();
        point.x /= display.getWidth();
        point.y /= display.getHeight();
        point.x = point.x * 2 - 1;
        point.y = (1.0f - point.y) * 2 - 1;

        float aspectRatioInv = (float)display.getHeight() / (float)display.getWidth();
        point.y *= aspectRatioInv;

        return point;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (currLevelID == -1) {
            return false;   // No user input if level is already won.
        }
        final int actionIndex = MotionEventCompat.getActionIndex(event);
        final int action = MotionEventCompat.getActionMasked(event);

        if (action == MotionEvent.ACTION_UP) {
            selectedObject = null;
            activePrimaryPointerID = -1;
            activeSecondaryPointerID = -1;
            return true;
        }
        else if (action == MotionEvent.ACTION_POINTER_UP) {
            final int pointerId = MotionEventCompat.getPointerId(event, actionIndex);
            if (pointerId == activePrimaryPointerID) {
                selectedObject = null;
                activePrimaryPointerID = -1;
            }
            else if (pointerId == activeSecondaryPointerID)
                activeSecondaryPointerID = -1;
            return true;
        }
        else if (action == MotionEvent.ACTION_POINTER_DOWN && selectedObject != null) {
            activeSecondaryPointerID = actionIndex;
            selectedObjectBaseDir.set(selectedObject.getDirection());

            // We need to memorize the inverse initial rotation.
            PointF posIn1 = getNdcsFromMotionEvent(event, MotionEventCompat.findPointerIndex(event, activePrimaryPointerID));
            PointF posIn2 = getNdcsFromMotionEvent(event, MotionEventCompat.findPointerIndex(event, activeSecondaryPointerID));
            PointF diff = Vec2D.subtract(posIn2, posIn1);
            Vec2D.normalize(diff);
            inverseInitialPointerDir = Vec2D.invertRotation(diff);
            return true;
        }
        else if (action == MotionEvent.ACTION_DOWN) {
            PointF posIn = getNdcsFromMotionEvent(event, actionIndex);
            for (GameObject go : gameObjects) {
                if (go.getIsMovable()) {
                    PointF pos = go.getPosition();
                    PointF extents = go.getExtents();
                    if (posIn.x > pos.x - extents.x && posIn.x < pos.x + extents.x
                            && posIn.y > pos.y - extents.y && posIn.y < pos.y + extents.y) {
                        selectedObject = go;
                        selectedPointOffset.set(pos.x - posIn.x, pos.y - posIn.y);
                        activePrimaryPointerID = MotionEventCompat.getPointerId(event, 0);
                        break;
                    }
                }
            }
            return true;
        }
        else if (action == MotionEvent.ACTION_MOVE) {
            if (activePrimaryPointerID >= 0 && selectedObject != null) {
                PointF posIn1 = getNdcsFromMotionEvent(event, MotionEventCompat.findPointerIndex(event, activePrimaryPointerID));
                if (activeSecondaryPointerID >= 0) {
                    PointF posIn2 = getNdcsFromMotionEvent(event, MotionEventCompat.findPointerIndex(event, activeSecondaryPointerID));
                    PointF offset = Vec2D.subtract(posIn2, posIn1);

                    // Transform the offset with the inverse initial pointer rotation.
                    offset = Vec2D.rotatePoint(offset, inverseInitialPointerDir);

                    // Now transform with initial object rotation.
                    PointF newRotation = Vec2D.rotatePoint(offset, selectedObjectBaseDir);
                    Vec2D.normalize(newRotation);
                    selectedObject.setDirection(newRotation);
                }
                selectedObject.setPosition(new PointF(posIn1.x + selectedPointOffset.x, posIn1.y + selectedPointOffset.y));
                return true;
            }
        }
        return false;
    }
}
