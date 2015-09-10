package nodomain.yalg;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

/**
 * Created by andreas on 10.09.2015.
 */
public class LaserRenderer {
    private int m_VBO;
    private int m_IBO;

    static int BYTES_PER_FLOAT = 4;

    LaserRenderer()
    {
        int[] tmp = new int[2];
        GLES20.glGenBuffers(2, tmp, 0);
        m_VBO = tmp[0];
        m_IBO = tmp[1];
    }

    static void addQuad(float[] positions, int[] indices, int offset, float[] a, float[] b, float[] c, float[] d)
    {
        positions[offset] = 1;
    }

    public void setLasers(Vector<float[]> linePositions, Vector<Integer> lineColors)
    {
        float[] quadPositions = new float[linePositions.size() * 4 * 3];
        int[] quadIndices = new int[quadPositions.length];

        for (int i = 0; i < linePositions.size(); i++)
        {

        }

        FloatBuffer positionsBuffer = ByteBuffer.allocateDirect(quadPositions.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        positionsBuffer.put(quadPositions).position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_VBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, positionsBuffer.capacity() * BYTES_PER_FLOAT, positionsBuffer, GLES20.GL_STATIC_DRAW);

        IntBuffer indicesBuffer = ByteBuffer.allocateDirect(quadIndices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
        indicesBuffer.put(quadIndices).position(0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, m_IBO);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.capacity() * BYTES_PER_FLOAT, indicesBuffer, GLES20.GL_STATIC_DRAW);
    }

    public void render()
    {

    }
}
