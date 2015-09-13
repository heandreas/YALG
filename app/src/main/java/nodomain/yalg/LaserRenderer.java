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
    private final int m_VBO;

    private final int m_Program;

    private int m_NumVertices;

    static final int BYTES_PER_FLOAT = 4;
    static final float LASER_WIDTH = 0.01f;

    private final String vertexShaderCode =
                    "attribute vec3 vPosition;" +
                    "attribute vec3 vColor;" +
                    "varying vec3 col;" +
                    "void main() {" +
                    "  gl_Position = vec4(vPosition, 1);" +
                    "  col = vColor;" +
                    "}";

    private final String fragmentShaderCode =
                    "precision mediump float;" +
                    "varying vec3 col;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(col, 1);" +
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
    }

    static void writePosToBuffer(PointF pos, ColorF color, int offset, float[] buffer)
    {
        buffer[offset * 6] = pos.x;
        buffer[offset * 6 + 1] = pos.y;
        buffer[offset * 6 + 2] = 0.0f;

        buffer[offset * 6 + 3] = color.getRed();
        buffer[offset * 6 + 4] = color.getGreen();
        buffer[offset * 6 + 5] = color.getBlue();
    }

    public void setLasers(List<PointF> linePositions, List<ColorF> lineColors)
    {
        m_NumVertices = (linePositions.size() / 2) * 6;
        float[] quadVertices = new float[m_NumVertices * 6];

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

            ColorF col = lineColors.get(i / 2);

            writePosToBuffer(corners[0], col, currVertexOffset, quadVertices);
            writePosToBuffer(corners[2], col, currVertexOffset + 1, quadVertices);
            writePosToBuffer(corners[1], col, currVertexOffset + 2, quadVertices);

            writePosToBuffer(corners[0], col, currVertexOffset + 3, quadVertices);
            writePosToBuffer(corners[3], col, currVertexOffset + 4, quadVertices);
            writePosToBuffer(corners[2], col, currVertexOffset + 5, quadVertices);

            currVertexOffset += 6;
        }

        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(quadVertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(quadVertices).position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_VBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * BYTES_PER_FLOAT, vertexBuffer, GLES20.GL_STATIC_DRAW);
    }

    public void render()
    {
        GLES20.glUseProgram(m_Program);

        // get handle to vertex shader's vPosition member
        int posHandle = GLES20.glGetAttribLocation(m_Program, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(posHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(posHandle, 3,
                GLES20.GL_FLOAT, false,
                24, 0);

        int colorHandle = GLES20.glGetAttribLocation(m_Program, "vColor");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(colorHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(colorHandle, 3,
                GLES20.GL_FLOAT, false,
                24, 12);

        // Draw the triangles.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, m_NumVertices);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(posHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
    }
}
