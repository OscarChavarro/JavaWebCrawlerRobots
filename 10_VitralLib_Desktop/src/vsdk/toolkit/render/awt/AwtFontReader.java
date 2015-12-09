//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - August 15 2006 - Oscar Chavarro: Original base version                =
//===========================================================================

package vsdk.toolkit.render.awt;

import java.io.File;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.PathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.AffineTransform;

import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.io.geometry.FontReader;
import vsdk.toolkit.environment.geometry.ParametricCurve;

/**
This is an implementation of the persistence interface FontReader. It is
managed as a rendering operation because it actually uses font rendering
capabilities from Awt. Current mechanism used for font persistence (import)
is to "render" the glyph into an Awt rendering data structure, but not
showing it, instead using it to construct another data structure, which is
returned.

This class is a concrete factory in an abstract factory design pattern role.
*/
public class AwtFontReader extends FontReader
{
    private Font currentFont;
    private String fileName;

    public AwtFontReader()
    {
        currentFont = null;
        fileName = null;
    }

    /**
    Given a font file and a character, this method return a parametric
    curve representing a glyph. If something goes wrong, this method
    returns null. The character string is usually a lone character,
    but under some circumstantes this chatacter is acommpanied with
    a context (i.e. in arabic languages where glyph selection depends
    on a bounding form).

    Note that glyph vectorization in Java's AWT relys to much (perhaps
    incorrectly) on current font size. When using a size of 1 point,
    some glyphs composed of multiple curves get incorrectly placed components.
    So this method works with fonts `factor` points in size, to later scale the
    glyphs down by a factor of 1/`factor`.
    */
    @Override
    public ParametricCurve extractGlyph(
        String fontFile, String characterAndItsContext)
    {
        //-----------------------------------------------------------------
        float factor = 10.0f;
        if ( currentFont == null || fileName == null || 
             !fontFile.equals(fileName) ) {
            fileName = fontFile;
            try {
                currentFont = Font.createFont(Font.TRUETYPE_FONT, 
                                         new File(fontFile));
                currentFont = currentFont.deriveFont(factor);
            }
            catch ( Exception e ) {
                System.err.println("Error loading font file " + fontFile);
                return null;
            }
/*
            System.out.println("---- Font with " + 
                currentFont.getNumGlyphs() + " char to glyph mappings ----");
*/
        }

        ParametricCurve curve;

        //-----------------------------------------------------------------
        Vector3D pointParameters[];

        curve = new ParametricCurve();

        //- Glyph analisys -------------------------------------------
        AffineTransform a = new AffineTransform();

        FontRenderContext frc = new FontRenderContext(a, true, true);

        GlyphVector gv = currentFont.createGlyphVector(frc, characterAndItsContext);
        GeneralPath p = (GeneralPath)gv.getGlyphOutline(0);

        boolean endIt = false;
        int code;


//*****************************************************************
// This code can be used to debug rasterization of corresponding font in
// raster technique
/*
        vsdk.toolkit.media.RGBImage img = new vsdk.toolkit.media.RGBImage();
        img.init(640, 480);

        java.awt.image.BufferedImage bi;
        bi = vsdk.toolkit.render.awt.AwtRGBImageRenderer.exportToAwtBufferedImage(img);

        java.awt.Graphics2D offlineContext = (java.awt.Graphics2D)bi.getGraphics();
        offlineContext.setFont(currentFont);
        offlineContext.setColor(java.awt.Color.RED);
        offlineContext.drawString(characterAndItsContext, 100, 100);

        vsdk.toolkit.render.awt.AwtRGBImageRenderer.importFromAwtBufferedImage(bi, img);
        vsdk.toolkit.io.image.ImagePersistence.exportJPG(new File("output.jpg"), img);
*/
//*****************************************************************

        for ( PathIterator pi = p.getPathIterator(a);
              !pi.isDone(); pi.next() ) {
            double coords[] = new double[6];
            int type = pi.currentSegment(coords);

            code = 0;
            switch ( type ) {
              case PathIterator.SEG_CUBICTO:
                break;
              case PathIterator.SEG_LINETO:
                code = 1;
                break;
              case PathIterator.SEG_MOVETO:
                code = 0;
                break;
              case PathIterator.SEG_QUADTO:
                code = 2;
                break;
              case PathIterator.SEG_CLOSE:
                code = 3;
                break;
              default:
                System.out.print("AwtFontReader.extractGlyph: UNKNOWN");
                break;
            }

            if ( !endIt ) {
                switch ( code ) {
                  case 0:
                    curve.addPoint(null, ParametricCurve.BREAK);

                    pointParameters = new Vector3D[1];
                    pointParameters[0] = 
                        new Vector3D(coords[0]/factor, -coords[1]/factor, 0);
                    curve.addPoint(pointParameters, ParametricCurve.CORNER);
                    break;
                  case 1:
                    pointParameters = new Vector3D[1];
                    pointParameters[0] = new Vector3D(coords[0]/factor, -coords[1]/factor, 0);
                    curve.addPoint(pointParameters, ParametricCurve.CORNER);
                    break;
                  case 2:
                    pointParameters = new Vector3D[2];
                    // Note the inverse order of awt with respect to VSDK!
                    pointParameters[0] = new Vector3D(coords[2]/factor, -coords[3]/factor, 0);
                    pointParameters[1] = new Vector3D(coords[0]/factor, -coords[1]/factor, 0);
                    curve.addPoint(pointParameters, ParametricCurve.QUAD);
                    break;
                  case 3:
                    //endIt = true;
                    break;
                  default:
                    break;
                }
            }
        }

        return curve;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
