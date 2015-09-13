package nodomain.yalg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * Created by andreas on 13.09.2015.
 */
public class TextureFactory {
    private static TextureFactory instance;
    private TextureFactory () {}

    public static TextureFactory getInstance () {
        if (TextureFactory.instance == null) {
            TextureFactory.instance = new TextureFactory();
        }
        return TextureFactory.instance;
    }

    GameActivity context = null;


    public void setGameActivity(GameActivity activity) {
        context = activity;
    }

    private int laserID;
    private int primsaID;
    private int sensorID;
    private int spiegelID;
    private int blockID;

    private int backgroundID;
    private int onePixelID;



    public void loadTextures() {
        laserID = loadTexture(R.raw.laser);
        primsaID = loadTexture(R.raw.prisma);
        sensorID = loadTexture(R.raw.sensor);
        spiegelID = loadTexture(R.raw.spiegel);
        blockID = loadTexture(R.raw.block);
        backgroundID = loadTexture(R.raw.background);
        onePixelID = loadTexture(R.raw.onepx);
    }

    public int getTextureByName(String name) throws Exception {
        if (name.equals("laser"))
            return laserID;
        if (name.equals("prism"))
            return primsaID;
        if (name.equals("sensor"))
            return sensorID;
        if (name.equals("mirror"))
            return spiegelID;
        if (name.equals("block"))
            return blockID;

        if (name.equals("background"))
            return backgroundID;
        if (name.equals("onepx"))
            return onePixelID;

        throw new Exception("Could not find texture " + name + "!");
    }

    int loadTexture(final int resourceId)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }
}