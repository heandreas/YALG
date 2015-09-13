package nodomain.yalg;

import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Vector;

/**
 * Created by andreas on 10.09.2015.
 */
public class LaserRenderer {
    final int m_VBO;

    final int m_Program;

    private int m_NumVertices;

    static final int BYTES_PER_FLOAT = 4;
    static final float LASER_WIDTH = 0.015f;

    private float m_fTime;

    static final int VERTEX_FLOATS = 8;

    private final String vertexShaderCode =
                    "attribute vec3 vPosition;" +
                    "attribute vec3 vColor;" +
                    "attribute vec3 vUV;" +
                     "uniform vec2 windowSize;" +
                    "varying vec3 col;" +
                    "varying vec3 uv;" +
                    "void main() {" +
                    "  float aspectRatio = windowSize.x / windowSize.y;" +
                    "  vec3 pos = vec3(vPosition.x, vPosition.y * aspectRatio, vPosition.z);" +
                    "  gl_Position = vec4(pos, 1.0);" +
                    "  col = vColor;" +
                    "  uv = vUV;" +
                    "}";

    private final String fragmentShaderCode =
                    "precision mediump float;" +
                    "varying vec3 col;" +
                    "varying vec3 uv;" +
                    "uniform float time;" +
                    "void main() {" +
                    //"  gl_FragColor = vec4(col, sin(uv.y * 1.57079633) * fract(uv.x*15.0 + time));" +
                    "  float falloff = 1.0 - 2.0 * abs(0.5 - uv.y);" +
                    "  float fWaveParam = uv.x * 2.5 - 3.0 * time;" +
                    "  float fClampedWave = fract(fWaveParam);" +
                    //"  float wave = 0.5*( 1.0 + sin(fClampedWave * 2.0 * 3.14159);" +
                    "  float wave = 2.0 * abs(0.5 - fClampedWave);" +
                    "  float alpha = falloff * wave;" +
                    "  vec3 colOverdrive = vec3(1.0,1.0,1.0) * max(0.0, alpha * 2.0 - 1.0);" +
                    "  gl_FragColor = vec4(colOverdrive + col, alpha * 2.0 );" + //overdrive alpha value
                    //"  gl_FragColor = vec4(col, sin(uv.y * 1.57079633));" +
                    "}";

    /*private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(0, 1, 0, 1); //vColor;" +
                    "}";*/

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    LaserRenderer()
    {
        int[] tmp = new int[1];
        GLES20.glGenBuffers(1, tmp, 0);
        m_VBO = tmp[0];

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        m_Program = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(m_Program, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(m_Program, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(m_Program);

        m_fTime = 0;
    }

    static void writePosToBuffer(PointF pos, ColorF color, PointF uv, int offset, float[] buffer)
    {
        buffer[offset * VERTEX_FLOATS] = pos.x;
        buffer[offset * VERTEX_FLOATS + 1] = pos.y;
        buffer[offset * VERTEX_FLOATS + 2] = 0.0f;

        buffer[offset * VERTEX_FLOATS + 3] = color.getRed();
        buffer[offset * VERTEX_FLOATS + 4] = color.getGreen();
        buffer[offset * VERTEX_FLOATS + 5] = color.getBlue();

        buffer[offset * VERTEX_FLOATS + 6] = uv.x;
        buffer[offset * VERTEX_FLOATS + 7] = uv.y;
    }

    public void setLasers(List<PointF> linePositions, List<Float> lineLengths, List<ColorF> lineColors)
    {
        m_NumVertices = (linePositions.size() / 2) * 6;

        float[] quadVertices = new float[m_NumVertices * VERTEX_FLOATS];

        int currVertexOffset = 0;
        for (int i = 0; i < linePositions.size() - 1; i += 2)
        {
            PointF p1 = linePositions.get(i);
            PointF p2 = linePositions.get(i + 1);
            PointF dx = Vec2D.subtract(p1, p2);
            PointF perp = Vec2D.perpendicular(dx);
            Vec2D.normalize(perp);
            PointF offset = Vec2D.mul(LASER_WIDTH, perp);

            PointF[] corners = {Vec2D.add(p1, offset), Vec2D.subtract(p1, offset), Vec2D.subtract(p2, offset), Vec2D.add(p2, offset)};

            PointF[] uvs = {new PointF(lineLengths.get(i), 1), new PointF(lineLengths.get(i), 0),
                            new PointF(lineLengths.get(i + 1), 0), new PointF(lineLengths.get(i + 1), 1)};

            ColorF col = lineColors.get(i / 2);

            writePosToBuffer(corners[0], col, uvs[0], currVertexOffset, quadVertices);
            writePosToBuffer(corners[2], col, uvs[2], currVertexOffset + 1, quadVertices);
            writePosToBuffer(corners[1], col, uvs[1], currVertexOffset + 2, quadVertices);

            writePosToBuffer(corners[0], col, uvs[0], currVertexOffset + 3, quadVertices);
            writePosToBuffer(corners[3], col, uvs[3], currVertexOffset + 4, quadVertices);
            writePosToBuffer(corners[2], col, uvs[2], currVertexOffset + 5, quadVertices);

            currVertexOffset += 6;
        }

        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(quadVertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(quadVertices).position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_VBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * BYTES_PER_FLOAT, vertexBuffer, GLES20.GL_STATIC_DRAW);
    }

    public void render(float fDeltaTime)
    {
        final float fTimeOverflow = 10.0f;
        m_fTime += fDeltaTime;
        if(m_fTime > fTimeOverflow)
            m_fTime -= fTimeOverflow;

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_VBO);
        GLES20.glUseProgram(m_Program);

        // get handle to vertex shader's vPosition member
        int posHandle = GLES20.glGetAttribLocation(m_Program, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(posHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(posHandle, 3,
                GLES20.GL_FLOAT, false,
                VERTEX_FLOATS * BYTES_PER_FLOAT, 0);

        //setup color variable
        int colorHandle = GLES20.glGetAttribLocation(m_Program, "vColor");

        // Enable a handle to the triangle colors
        GLES20.glEnableVertexAttribArray(colorHandle);

        // Prepare the triangle color
        GLES20.glVertexAttribPointer(colorHandle, 3,
                GLES20.GL_FLOAT, false,
                VERTEX_FLOATS * BYTES_PER_FLOAT, 3 * BYTES_PER_FLOAT);

        //setup UV variable
        int uvHandle = GLES20.glGetAttribLocation(m_Program, "vUV");

        // Enable a handle to the triangle colors
        GLES20.glEnableVertexAttribArray(uvHandle);

        // Prepare the triangle color
        GLES20.glVertexAttribPointer(uvHandle, 2,
                GLES20.GL_FLOAT, false,
                VERTEX_FLOATS * BYTES_PER_FLOAT, 6 * BYTES_PER_FLOAT);

        //enable alpha blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);

        int timeHandle = GLES20.glGetUniformLocation(m_Program, "time");
        GLES20.glUniform1f(timeHandle, m_fTime);

        // Draw the triangles.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, m_NumVertices);

        //disable alpha blending again
        GLES20.glDisable(GLES20.GL_BLEND);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(posHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
        GLES20.glDisableVertexAttribArray(uvHandle);
    }
}
