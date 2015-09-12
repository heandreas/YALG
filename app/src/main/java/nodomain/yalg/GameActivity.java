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

    private List<GameObject> gameObjects = null;

    GameObject selectedObject = null;

    public void computeLasers(List<PointF> laserSegments, List<ColorF> laserColors) {
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
            LaserTracer.Result result = LaserTracer.traceRecursion(origins.get(i), dirs.get(i),
                    1.0f, lineSegmentsArray, coefficientsArray, 0);
            for (int j = 0; j < result.lineSegments.size(); j++)
                laserSegments.add(result.lineSegments.get(j));
            for (int j = 0; j < result.intensities.size(); j++) {
                float intensity = result.intensities.get(j);
                laserColors.add(new ColorF(0, intensity, 0));
            }
        }

        // Hack: Add obstacles as lasers. Yeah...
        for (int i = 0; i < obstacleLines.size(); i++) {
            PointF p = obstacleLines.get(i);
            laserSegments.add(p);
            if (i % 2 == 0)
                laserColors.add(new ColorF(0, 0, 1));

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

        ArrayList<PointF> laserSegments = new ArrayList<PointF>();
        ArrayList<ColorF> laserColors = new ArrayList<ColorF>();
        computeLasers(laserSegments, laserColors);

        glSurface = new YalgGLSurface(this);
        setContentView(glSurface);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the pointer ID
        int activePointId = event.getPointerId(0);

        // ... Many touch events later...

        // Use the pointer ID to find the index of the active pointer
        // and fetch its position
        int pointerIndex = event.findPointerIndex(activePointId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        Display display = getWindowManager().getDefaultDisplay();
        x /= display.getWidth();
        y /= display.getHeight();
        x = x * 2 - 1;
        y = (1.0f - y) * 2 - 1;

        int action = MotionEventCompat.getActionMasked(event);

        if (action == MotionEvent.ACTION_UP) {
            selectedObject = null;
            return true;
        }
        else if (action == MotionEvent.ACTION_DOWN) {
            for (GameObject go : gameObjects) {
                if (go.getIsMovable()) {
                    PointF pos = go.getPosition();
                    PointF extents = go.getExtents();
                    if (x > pos.x - extents.x && x < pos.x + extents.x
                            && y > pos.y - extents.y && y < pos.y + extents.y) {
                        selectedObject = go;
                        break;
                    }
                }
            }
            return true;
        }
        else if (action == MotionEvent.ACTION_MOVE) {
            if (selectedObject != null)
                selectedObject.setPosition(new PointF(x, y));
        }

        return false;
    }
}