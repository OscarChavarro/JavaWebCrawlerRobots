//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - March 14 2006 - Fabio Aroca & Eduardo Mendoza: Original base version  =
//= - March 14 2006 - Oscar Chavarro: quality check                         =
//= - May 25 2006 - David Diaz & Oscar Chavarro: documentation added        =
//===========================================================================

package vsdk.toolkit.media;

import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.ColorRgb;

/**
This class represents a depth map. A depth map can be used to:
<UL>
<LI> Represent how far a pixel in the color buffer is from the camera. This
is useful as part of visualization algorithms like ZBuffer.
<LI> Represent Light maps. Light maps are useful in two pass algorithms for
shadow approximation.
</UL>
*/
public class ZBuffer extends MediaEntity {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20060502L;

    private float[] depth;
    private int xSize;
    private int ySize;

    /**
    Consructs a Z buffer with the specified parameters; the depth data is 
    initialized to value 0.0
    @param width The width of the z buffer
    @param height The height of the z buffer
    */
    public ZBuffer(int width, int height) {
        xSize = width;
        ySize = height;
        depth=new float[xSize*ySize];
    }

    /**
    Creates a z buffer given the z buffer's raw data. The stored z buffer will have 
    an inverse row order than that of the parameter's data
    @param width The width of the z buffer and the parameter input data
    @param height The height of the z buffer and the parameter input data
    @param dep The z buffer data to store in this z buffer  
    */
    public ZBuffer(float[] dep, int width, int height) {
        xSize = width;
        ySize = height;
        depth = new float[width*height];

        int pos = 0;
        for (int y = ySize - 1; y >= 0; y--) {
            for (int x = 0; x < xSize; x++) {
                depth[xSize*y+x] = dep[pos];
                pos++;
            }
        }
    }

    /**
    Returns the width of this z buffer
    @return The width of this z buffer
    */
    public int getXSize()
    {
        return xSize;
    }

    /**
    Returns the height of this z buffer
    @return The height of this z buffer
    */
    public int getYSize()
    {
        return ySize;
    }

    /**
    This method returns the Z buffer data stored in this depth map
    @return The Z buffer data stored in this depth map
    */
    public float[] getZBuffer() {
        return depth;
    }

    /**
    Returns the depth value of the Z buffer at the specified position
    @param x the horizontal position of the value to return
    @param y the vertical position of the value to return
    @return The depth value of the z buffer at the specified position
    */
    public float getZ(int x, int y) {
        int pos = (xSize * y) + x;
        return depth[pos];
    }

    /**
    Sets this Z buffer data with that of the input parameter
    @param dep The data to store in this z buffer
    */
    public void setZBuffer(float[] dep) {
        depth = new float[dep.length];

        //System.arraycopy(dep, 0, depth, 0, dep.length);
        for (int i = 0; i < dep.length; i++) {
            depth[i] = dep[i];
        }
    }

    /**
    Sets the Z buffer value from the input parameter in a specified position 
    @param x the horizontal position of the value to change
    @param y the vertical position of the value to change
    @param v The value to replace in the z buffer
    */
    public void setZ(int x, int y, float v) {
        int pos = (xSize * y) + x;
        depth[pos] = v;
    }

    /**
    This method converts this Z buffer into an RGBImage using the specified
    ColorPalette
    */
    public IndexedColorImage exportIndexedColorImage() {
        IndexedColorImage image = new IndexedColorImage();
        image.init(xSize, ySize);
        int pos = 0;
        int val;

        for (int y = 0; y <image.getYSize(); y++) {
            for (int x = 0; x < image.getXSize(); x++) {
                float f = depth[pos];
                if ( f < 0.0 ) f = 0.0f;
                if ( f > 1.0 ) f = 1.0f;
                val = (int)(f * 255.0);
                image.putPixel(x, y, VSDK.unsigned8BitInteger2signedByte(val));
                pos++;
            }
        }
        return image;
    }

    /**
    This method converts this Z buffer into an RGBImage using the specified
    ColorPalette
    @param p The color palete used to convert this z buffer into an RGBImage
    @return An RGBAImage that represents this z buffer
    */
    public RGBImage exportRGBImage(RGBColorPalette p) {
        RGBImage image = new RGBImage();
        image.init(xSize, ySize);
        int pos = 0;

        ColorRgb c;

        for (int y = 0; y <image.getYSize(); y++) {
            for (int x = 0; x < image.getXSize(); x++) {
                float f = depth[pos];
                if ( f < 0.0 ) f = 0.0f;
                if ( f > 1.0 ) f = 1.0f;
                c = p.evalLinear(f);
                image.putPixel(x, y, 
                    (byte)(c.r*256), (byte)(c.g*256), (byte)(c.b*256));
                pos++;
            }
        }
        return image;
    }

    /**
    This method converts this Z buffer into an RGBAImage using the specified 
    ColorPalette
    @param p The color palete used to convert this Z buffer into an RGBAImage
    @return An RGBAImage that represents this Z buffer
    */
    public RGBAImage exportRGBAImage(RGBColorPalette p) {
        RGBAImage image = new RGBAImage();
        image.init(xSize, ySize);
        int pos = 0;

        ColorRgb c;

        for (int y = 0; y <image.getYSize(); y++) {
            for (int x = 0; x < image.getXSize(); x++) {
                float f = depth[pos];
                if ( f < 0.0 ) f = 0.0f;
                if ( f > 1.0 ) f = 1.0f;
                c = p.evalLinear(f);
                image.putPixel(x, y, 
                    (byte)(c.r*256), (byte)(c.g*256), (byte)(c.b*256));
                pos++;
            }
        }
        return image;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
