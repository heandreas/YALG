package nodomain.yalg;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by andreas on 10.09.2015.
 */
public class YalgGLSurface extends GLSurfaceView {

    private LaserRenderer m_LaserRenderer;

    public YalgGLSurface(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        setRenderer(new Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                GLES20.glClearColor(1.0f, 0.0f, 1.0f, 1.0f);
                m_LaserRenderer = new LaserRenderer();
                /*List<PointF> lines = new ArrayList<PointF>();
                lines.add(new PointF(0, 0));
                lines.add(new PointF(1, 1));
                m_LaserRenderer.setLasers(lines);*/
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                GLES20.glViewport(0, 0, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

                PointF[] obstacles = {new PointF(-0.4f, 0.2f), new PointF(-0.5f, 0.0f)};
                float[] coefficients = {1.5f};
                LaserTracer.Result result = LaserTracer.traceRecursion(new PointF(0.1f, 0.1f), new PointF(-1.0f, 0.0f), 1.0f, obstacles, coefficients);
                m_LaserRenderer.setLasers(result.lineSegments);
                m_LaserRenderer.render();
            }
        });
    }
}
