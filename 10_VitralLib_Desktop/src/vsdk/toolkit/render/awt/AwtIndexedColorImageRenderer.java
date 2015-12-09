//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - December 18 2006 - Oscar Chavarro: Original base version              =
//===========================================================================

package vsdk.toolkit.render.awt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;

import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.media.IndexedColorImage;
import vsdk.toolkit.media.RGBPixel;

public class AwtIndexedColorImageRenderer extends AwtRenderer
{
    public static void draw(Graphics dc, IndexedColorImage img, int x0, int y0)
    {
        int x, y;
        RGBPixel pixel;

        for ( y = 0; y < img.getYSize(); y++ ) {
            for ( x = 0; x < img.getXSize(); x++ ) {
                pixel = img.getPixelRgb(x, y);
                dc.setColor(
                    new Color( VSDK.signedByte2unsignedInteger(pixel.r), 
                               VSDK.signedByte2unsignedInteger(pixel.g), 
                               VSDK.signedByte2unsignedInteger(pixel.b) )
                );
                dc.drawLine(x+x0, y+y0, x+x0, y+y0);
            }
        }
    }

    public static void draw(Graphics dc, IndexedColorImage img)
    {
        draw(dc, img, 0, 0);
    }

    public static boolean importFromAwtBufferedImage(
        BufferedImage input, IndexedColorImage output
    )
    {
        int w = input.getWidth();
        int h = input.getHeight();
        int w2 = output.getXSize();
        int h2 = output.getYSize();

        if ( w != w2 || h != h2 ) {
            if ( !output.initNoFill(w, h) ) {
                return false;
            }
        }

        ColorModel cm = input.getColorModel();
        int x, y;
        int pixel;
        int val;
        RGBPixel p = new RGBPixel();

        if ( cm.getNumColorComponents() == 3 ) {
            for ( y = 0; y < h; y++ ) {
                for ( x = 0; x < w; x++ ) {
                    // Warning: This method call is so slow...
                    pixel = input.getRGB(x, y);
                    p.r = (byte)((pixel & 0x00FF0000) >> 16);
                    p.g = (byte)((pixel & 0x0000FF00) >> 8);
                    p.b = (byte)((pixel & 0x000000FF));
                    val = VSDK.signedByte2unsignedInteger(p.r);
                    val += VSDK.signedByte2unsignedInteger(p.g);
                    val += VSDK.signedByte2unsignedInteger(p.b);
                    val /= 3;
                    output.putPixel(x, y, val);
                }
            }
        }
        else if ( cm.getNumColorComponents() == 1 ) {
            DataBuffer db = input.getData().getDataBuffer();
            int i;
            for ( i = 0, y = 0; y < h; y++ ) {
                for ( x = 0; x < w; x++, i++ ) {
                    val = db.getElem(i) & 0x000000FF;
                    // Warning: This method call is so slow...
                    output.putPixel(x, y, val);
                }
            }
        }
        else {
            VSDK.reportMessage(null, VSDK.FATAL_ERROR, "importFromAwtBufferedImage", "ColorSpace encoding not supported!");
        }
        return true;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
