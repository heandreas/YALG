package nodomain.yalg;

import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * Created by andreas on 10.09.2015.
 */
public class LaserRenderer {
    private final int m_VBO;

    private final int m_Program;

    private int m_NumVertices;

    static final int BYTES_PER_FLOAT = 4;
    static final float LASER_WIDTH = 0.02f;

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(0, 1, 0, 1);" +
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

    static void writePosToBuffer(PointF pos, int offset, float[] buffer)
    {
        buffer[offset * 3] = pos.x;
        buffer[offset * 3 + 1] = pos.y;
        buffer[offset * 3 + 2] = 0.0f;
    }

    public void setLasers(List<PointF> linePositions) //, Vector<Integer> lineColors)
    {
        m_NumVertices = (linePositions.size() / 2) * 6;
        float[] quadPositions = new float[m_NumVertices * 3];

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

            writePosToBuffer(corners[0], currVertexOffset, quadPositions);
            writePosToBuffer(corners[2], currVertexOffset + 1, quadPositions);
            writePosToBuffer(corners[1], currVertexOffset + 2, quadPositions);

            writePosToBuffer(corners[0], currVertexOffset + 3, quadPositions);
            writePosToBuffer(corners[3], currVertexOffset + 4, quadPositions);
            writePosToBuffer(corners[2], currVertexOffset + 5, quadPositions);

            currVertexOffset += 6;
        }

        FloatBuffer positionsBuffer = ByteBuffer.allocateDirect(quadPositions.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        positionsBuffer.put(quadPositions).position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_VBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, positionsBuffer.capacity() * BYTES_PER_FLOAT, positionsBuffer, GLES20.GL_STATIC_DRAW);
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
                0, 0);

        // get handle to fragment shader's vColor member
        // int colorHandle = GLES20.glGetUniformLocation(m_Program, "vColor");

        // Set color for drawing the triangle
        // GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // Draw the triangles.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, m_NumVertices);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(posHandle);
    }
}
