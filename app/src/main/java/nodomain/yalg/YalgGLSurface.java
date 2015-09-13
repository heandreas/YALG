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

    private final String default_VS =
            "attribute vec3 vPosition;" +
                    "attribute vec2 vUV;" +
                    "varying vec2 uv;" +
                    "void main() {" +
                    "  gl_Position = vec4(vPosition, 1);" +
                    "  uv = vec2(vUV.x, 1 - vUV.y);" +
                    "}";

    private final String default_FS =
            "precision mediump float;" +
                    "uniform sampler2D texture;" +
                    "uniform vec3 colLaser;" +
                    "varying vec2 uv;" +
                    "void main() {" +
                    //"  vec4 texcolor = texture2D(texture, uv);" +
                    //"  vec3 laserBlend = colLaser * texcolor.r" +
                    //"  vec3 colorBlend = vec3(1,1,1) * (texcolor.g)" +
                    //"  gl_FragColor = vec4(laserBlend + colorBlend, texcolor.a)" +
                    //"  gl_FragColor = texcolor;" +
                    "  gl_FragColor = texture2D(texture, uv);" +
                    "}";

    private int m_DefaultProgram;

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    void renderFrame(float fDeltaTime) {
        GLES20.glUseProgram(m_DefaultProgram);

        int posHandle = GLES20.glGetAttribLocation(m_DefaultProgram, "vPosition");
        int uvHandle = GLES20.glGetAttribLocation(m_DefaultProgram, "vUV");
        int textureHandle = GLES20.glGetUniformLocation(m_DefaultProgram, "texture");
        int laserColorHandle = GLES20.glGetUniformLocation(m_DefaultProgram, "colLaser");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(textureHandle, 0);

        for (GameObject go : gameActivity.gameObjects) {
            go.render(posHandle, uvHandle, laserColorHandle);
        }

        GLES20.glDisableVertexAttribArray(posHandle);
        GLES20.glDisableVertexAttribArray(uvHandle);

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
                GLES20.glDisable(GLES20.GL_DEPTH_TEST);

                TextureFactory.getInstance().loadTextures();

                int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                        default_VS);
                int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                        default_FS);

                m_DefaultProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(m_DefaultProgram, vertexShader);
                GLES20.glAttachShader(m_DefaultProgram, fragmentShader);
                GLES20.glLinkProgram(m_DefaultProgram);

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
