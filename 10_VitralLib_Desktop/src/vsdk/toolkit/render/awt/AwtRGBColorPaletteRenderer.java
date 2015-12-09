//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - March 31 2006 - Oscar Chavarro: Original base version                 =
//===========================================================================

package vsdk.toolkit.render.awt;

import java.awt.Color;
import java.awt.Graphics;

import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.media.RGBColorPalette;

public class AwtRGBColorPaletteRenderer extends AwtRenderer
{
    private static float convert(double in)
    {
        if ( in > 1.0 ) in = 1.0;
        if ( in < 0.0 ) in = 0.0;
        return (float)in;
    }

    public static void drawFlatVertical(
        Graphics dc,
        RGBColorPalette palette,
        int x0,
        int y0,
        int dx,
        int dy)
    {
        int x, y;
        ColorRgb c;
        Color cawt = null;
        double delta = 1.0 / ((double)(dy-1));

        for ( y = 0; y < dy; y++ ) {
            c = palette.evalNearest(delta*((double)y));
            try {
                cawt = new Color(convert(c.r), convert(c.g), convert(c.b));
            }
            catch( Exception e ){
                System.out.println("Warning: initializing color " + c);
            }
            for ( x = 0; x < dx; x++ ) {
                dc.setColor(cawt);
                dc.drawLine(x+x0, y+y0, x+x0, y+y0);
            }
        }
    }

    public static void drawShadedVertical(
        Graphics dc,
        RGBColorPalette palette,
        int x0,
        int y0,
        int dx,
        int dy)
    {
        int x, y;
        ColorRgb c;
        Color cawt = null;
        double delta = 1.0 / ((double)(dy-1));

        for ( y = 0; y < dy; y++ ) {
            c = palette.evalLinear(delta*((double)y));
            try {
                cawt = new Color(convert(c.r), convert(c.g), convert(c.b));
            }
            catch( Exception e ){
                System.out.println("Warning: initializing color " + c);
            }
            for ( x = 0; x < dx; x++ ) {
                dc.setColor(cawt);
                dc.drawLine(x+x0, y+y0, x+x0, y+y0);
            }
        }
    }

    public static void drawShadedHorizontal(
        Graphics dc,
        RGBColorPalette palette,
        int x0,
        int y0,
        int dx,
        int dy)
    {
        int x, y;
        ColorRgb c;
        Color cawt = null;
        double delta = 1.0 / ((double)(dx-1));

        for ( x = 0; x < dx; x++ ) {
            c = palette.evalLinear(delta*((double)x));
            try {
                cawt = new Color(convert(c.r), convert(c.g), convert(c.b));
            }
            catch( Exception e ){
                System.out.println("Warning: initializing color " + c);
            }
            for ( y = 0; y < dy; y++ ) {
                dc.setColor(cawt);
                dc.drawLine(x+x0, y+y0, x+x0, y+y0);
            }
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
