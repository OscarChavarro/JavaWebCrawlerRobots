//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - February 24 2006 - Gina Chiquillo: Original base version              =
//= - April 6 2006 - Oscar Chavarro: Quality check - integration            =
//= - May 22 2006 - David Diaz/Oscar Chavarro: documentation added          =
//===========================================================================

package vsdk.toolkit.io.image;

import java.io.Reader;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.StreamTokenizer;

import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.media.RGBColorPalette;
import vsdk.toolkit.io.PersistenceElement;

/**
This class contains the persistence operations to load and save
RGBColorPalettes
*/
public class RGBColorPalettePersistence extends PersistenceElement {

    /** 
    This method reads a text formated stream in the style of GIMP
    palettes and from its data generates a RGBColorPalette
    @return the RGBColorPalette builded from the source
    */
    public static RGBColorPalette importGimpPalette(Reader source)
    {
        RGBColorPalette p = new RGBColorPalette();
        p.init(0);

        StreamTokenizer parser = new StreamTokenizer(source);

        parser.resetSyntax();
        parser.eolIsSignificant(true);
        parser.slashSlashComments(false);
        parser.slashStarComments(false);
        parser.commentChar('#');
        parser.whitespaceChars(' ', ' ');
        parser.whitespaceChars(',', ',');
        parser.whitespaceChars('\t', '\t');
        parser.wordChars('A', 'Z');
        parser.wordChars('a', 'z');
        parser.wordChars('0', '9');
        parser.wordChars('_', '_');
        parser.parseNumbers();

        int tokenType;
        int startline = 0;
        ColorRgb col = null;

        do {
            try {
                tokenType = parser.nextToken();
              }
              catch (Exception e) {
                break;
            }
            switch ( tokenType ) {
              case StreamTokenizer.TT_EOL: 
                startline = 0;
                break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_NUMBER:
                switch ( startline ) {
                  case 0:
                    col = new ColorRgb();
                    col.r = (parser.nval)/255.0;
                    break;
                  case 1:
                    col.g = (parser.nval)/255.0;
                    break;
                  case 2:
                    col.b = (parser.nval)/255.0;
                    p.addColor(col);
                    break;
                }
                startline++;
                break;
              case StreamTokenizer.TT_WORD:
                break;
              default:
                break;
            }
        } while ( tokenType != StreamTokenizer.TT_EOF );

        return p;
    }

    /** This method reads a binary raw stream of consecutive RGB color values
     */
    public static RGBColorPalette importRawPalette(DataInputStream dis) throws IOException {
        RGBColorPalette p = new RGBColorPalette();
        p.init(0);

        while(dis.available()>0){
          int nr = dis.readByte();
          if (nr < 0) {
            nr = 256 + nr;
          }
          int ng = dis.readByte();
          if (ng < 0) {
            ng = 256 + ng;
          }
    
          int nb = dis.readByte();
          if (nb < 0) {
            nb = 256 + nb;
          }
    
          ColorRgb color = new ColorRgb(nr, ng, nb);
          p.addColor(color);
        }

        dis.close();
        return p;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
