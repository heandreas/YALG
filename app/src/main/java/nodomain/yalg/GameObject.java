package nodomain.yalg;

import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by andreas on 10.09.2015.
 */
public class GameObject {
    ColorF m_Color = new ColorF(1.0f, 1.0f, 1.0f);

    protected PointF m_Position = new PointF(0, 0);
    protected PointF m_Rotation = new PointF(1, 0);

    protected float m_uvScale = 1.0f;

    protected PointF m_Extents = new PointF(0.2f, 0.2f);

    protected boolean m_IsActive = true;
    protected boolean m_MustBeActiveForWinning = false;

    protected boolean m_IsMovable = false;

    protected String m_TextureName = "sensor";

    public String getTextureName() { return m_TextureName; }

    public void setTextureName(String textureName) { m_TextureName = textureName; }

    public boolean getIsMovable() { return m_IsMovable; }
    public void setIsMovable(boolean isMovable) { m_IsMovable = isMovable; }

    public boolean getIsActive() { return m_IsActive; }
    public void setIsActive(boolean isActive) {
        m_IsActive = isActive;
    }

    public boolean getMustBeActiveForWinning() { return m_MustBeActiveForWinning; }
    public void setMustBeActiveForWinning(boolean mustBeActiveForWinning) { m_MustBeActiveForWinning = mustBeActiveForWinning; }

    public void setPosition(PointF position) {
        m_Position = position;
    }
    PointF getPosition() {
        return m_Position;
    }

    public void setDirection(PointF direction) {
        m_Rotation = direction;
        Vec2D.normalize(m_Rotation);
    }
    PointF getDirection() {
        return m_Rotation;
    }

    public void setUVScale(float fScale) {
        m_uvScale = fScale;
    }

    public void setExtents(PointF extents) {
        m_Extents = extents;
    }
    PointF getExtents() {
        return m_Extents;
    }

    public void getRefractors(ArrayList<PointF> lineSegments, ArrayList<Float> coefficients) {}
    public void getRays(ArrayList<PointF> origins, ArrayList<PointF> dirs) {}

    private int m_VBO = -1;

    static void writePosToBuffer(PointF pos, PointF uv, int offset, float[] buffer) {
        buffer[offset * 5] = pos.x;
        buffer[offset * 5 + 1] = pos.y;
        buffer[offset * 5 + 2] = 0.0f;
        buffer[offset * 5 + 3] = uv.x;
        buffer[offset * 5 + 4] = uv.y;
    }

    public void render(int posHandle, int uvHandle, int laserColHandle) {
        if (m_VBO < 0) {
            int[] tmp = new int[1];
            GLES20.glGenBuffers(1, tmp, 0);
            m_VBO = tmp[0];
        }

        float[] vertexBuffer = new float[30];
        PointF[] corners = {new PointF(-m_Extents.x, -m_Extents.y),
                new PointF(-m_Extents.x, m_Extents.y),
                new PointF(m_Extents.x, m_Extents.y),
                new PointF(m_Extents.x, -m_Extents.y)};
        for (int i = 0; i < 4; i++) {
            corners[i] = Vec2D.rotatePoint(corners[i], m_Rotation);
            corners[i].x += m_Position.x;
            corners[i].y += m_Position.y;
        }
        PointF[] uvs = {new PointF(0, 0),
                new PointF(0, m_uvScale),
                new PointF(m_uvScale, m_uvScale),
                new PointF(m_uvScale, 0)};

        writePosToBuffer(corners[0], uvs[0], 0, vertexBuffer);
        writePosToBuffer(corners[2], uvs[2], 1, vertexBuffer);
        writePosToBuffer(corners[1], uvs[1], 2, vertexBuffer);

        writePosToBuffer(corners[0], uvs[0], 3, vertexBuffer);
        writePosToBuffer(corners[3], uvs[3], 4, vertexBuffer);
        writePosToBuffer(corners[2], uvs[2], 5, vertexBuffer);

        FloatBuffer vertexBufferRaw = ByteBuffer.allocateDirect(vertexBuffer.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBufferRaw.put(vertexBuffer).position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_VBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBufferRaw.capacity() * 4, vertexBufferRaw, GLES20.GL_STATIC_DRAW);

        GLES20.glEnableVertexAttribArray(posHandle);
        GLES20.glVertexAttribPointer(posHandle, 3,
                GLES20.GL_FLOAT, false,
                20, 0);

        GLES20.glEnableVertexAttribArray(uvHandle);
        GLES20.glVertexAttribPointer(uvHandle, 2,
                GLES20.GL_FLOAT, false,
                20, 12);

        try {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureFactory.getInstance().getTextureByName(m_TextureName));
        } catch(Exception e) {
            e.printStackTrace();
        }

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glUniform3f(laserColHandle, m_Color.getRed(), m_Color.getGreen(), m_Color.getBlue());

        // Draw the triangles.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

}
