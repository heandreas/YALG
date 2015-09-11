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


    void renderFrame() {
        ArrayList<PointF> laserSegments = new ArrayList<PointF>();
        ArrayList<ColorF> laserColors = new ArrayList<ColorF>();

        gameActivity.computeLasers(laserSegments, laserColors);

        laserRenderer.setLasers(laserSegments, laserColors);
        laserRenderer.render();
    }

    public YalgGLSurface(GameActivity gameActivity) {
        super(gameActivity);

        this.gameActivity = gameActivity;

        // TODO: remove debug call
        PointF[] obstacles = {new PointF(-0.4f, -0.2f), new PointF(0.4f, -0.2f),
                                new PointF(0.4f, -0.2f), new PointF(0.0f, 0.4f),
                                new PointF(0.0f, 0.4f), new PointF(-0.4f, -0.2f)};
        float[] coefficients = {1.5f, 1.5f, 1.5f};
        LaserTracer.Result result = LaserTracer.traceRecursion(new PointF(0.8f, 0.0f), new PointF(-1.0f, 0.0f), 1.0f, obstacles, coefficients);


        System.out.println("Setting EGL context.");
        setEGLContextClientVersion(2);
        setRenderer(new Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                System.out.println("GL Surface created.");

                GLES20.glClearColor(1.0f, 0.0f, 1.0f, 1.0f);
                laserRenderer = new LaserRenderer();
                /*List<PointF> lines = new ArrayList<PointF>();
                lines.add(new PointF(0, 0));
                lines.add(new PointF(1, 1));
                m_LaserRenderer.setLasers(lines);*/
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                System.out.println("GL Surface changed.");

                GLES20.glViewport(0, 0, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

                renderFrame();

                /*PointF[] obstacles = {new PointF(-0.4f, 0.2f), new PointF(-0.5f, 0.0f)};
                float[] coefficients = {1.5f};
                LaserTracer.Result result = LaserTracer.traceRecursion(new PointF(0.1f, 0.1f), new PointF(-1.0f, 0.0f), 1.0f, obstacles, coefficients);
                laserRenderer.setLasers(result.lineSegments);
                laserRenderer.render();*/
            }
        });
    }
}
