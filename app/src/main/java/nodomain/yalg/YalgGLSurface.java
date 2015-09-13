package nodomain.yalg;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.v4.content.res.TypedArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by andreas on 10.09.2015.
 */
public class YalgGLSurface extends GLSurfaceView {

    private LaserRenderer laserRenderer = null;

    GameActivity gameActivity;

    //last frame time
    long m_iLastTime;

    void renderFrame(float fDeltaTime) {
        ArrayList<PointF> laserSegments = new ArrayList<PointF>();
        ArrayList<Float> laserLengths = new ArrayList<>();
        ArrayList<ColorF> laserColors = new ArrayList<ColorF>();

        gameActivity.computeLasers(laserSegments, laserLengths, laserColors);

        laserRenderer.setLasers(laserSegments, laserLengths, laserColors);
        laserRenderer.render(fDeltaTime);
    }

    public YalgGLSurface(GameActivity gameActivity) {
        super(gameActivity);

        this.gameActivity = gameActivity;

        System.out.println("Setting EGL context.");
        setEGLContextClientVersion(2);

        setRenderer(new Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                System.out.println("GL Surface created.");

                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                laserRenderer = new LaserRenderer();

                m_iLastTime = System.nanoTime();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                System.out.println("GL Surface changed.");

                GLES20.glViewport(0, 0, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

                float fDeltaTime = (System.nanoTime()-m_iLastTime) * 1e-9f;
                m_iLastTime = System.nanoTime();

                renderFrame(fDeltaTime);
            }
        });
    }
}
