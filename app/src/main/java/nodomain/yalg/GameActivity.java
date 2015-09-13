package nodomain.yalg;

import nodomain.yalg.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.opengl.GLSurfaceView;

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

    boolean rotationMode_Debug = false;

    GameObject selectedObject = null;

    public void computeLasers(List<PointF> laserSegments, List<Float> laserLengths, List<ColorF> laserColors) {
        if (gameObjects == null)
            return;

        ArrayList<PointF> obstacleLines = new ArrayList<PointF>();
        ArrayList<Float> coefficients = new ArrayList<Float>();
        ArrayList<PointF> origins = new ArrayList<PointF>();
        ArrayList<PointF> dirs = new ArrayList<PointF>();
        for (GameObject go : gameObjects) {
            go.getRefractors(obstacleLines, coefficients);
            go.getRays(origins, dirs);
        }
        PointF[] lineSegmentsArray = new PointF[obstacleLines.size()];
        for (int i = 0; i < obstacleLines.size(); i++)
            lineSegmentsArray[i] = obstacleLines.get(i);
        float[] coefficientsArray = new float[coefficients.size()];
        for (int i = 0; i < coefficients.size(); i++)
            coefficientsArray[i] = coefficients.get(i);

        for (int i = 0; i < origins.size(); i++)
        {
            LaserTracer.Result result = LaserTracer.traceRecursion(origins.get(i), dirs.get(i), 1.0f,
                    lineSegmentsArray, coefficientsArray);

            laserSegments.addAll(result.lineSegments);

            laserLengths.addAll(result.lightLengths);

            for (int j = 0; j < result.intensities.size(); j++) {
                float intensity = result.intensities.get(j);
                laserColors.add(new ColorF(0, intensity, 0));
            }
        }

        laserSegments.addAll(obstacleLines);

        for (int i = 0; i < obstacleLines.size() / 2; i++) {
            laserColors.add(new ColorF(1, 1, 1));

            laserLengths.add(new Float(0));
            laserLengths.add(new Float(0));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            gameObjects = LevelLoader.parse(getResources().openRawResource(R.raw.testlevel));
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
        PointF point = new PointF();
        point.x = MotionEventCompat.getX(event, pointerIndex);
        point.y = MotionEventCompat.getY(event, pointerIndex);

        Display display = getWindowManager().getDefaultDisplay();
        point.x /= display.getWidth();
        point.y /= display.getHeight();
        point.x = point.x * 2 - 1;
        point.y = (1.0f - point.y) * 2 - 1;

        return point;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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