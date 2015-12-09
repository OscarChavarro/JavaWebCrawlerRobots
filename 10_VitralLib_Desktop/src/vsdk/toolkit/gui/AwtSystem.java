//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - December 31 2007 - Oscar Chavarro: Original base version              =
//===========================================================================

package vsdk.toolkit.gui;

// Java GUI classes
//import java.awt.event.KeyEvent; // Do not import!
//import java.awt.event.MouseEvent; // Do not import!
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

// VitralSDK classes
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.media.RGBAImage;
import vsdk.toolkit.render.awt.AwtRGBAImageRenderer;

/**
This class gives VitralSDK access to GUI operations in the AwtSystem, as
translation of Awt specific events to VitralSDK generalized / portable
events.
*/
public class AwtSystem extends PresentationElement
{
    private static Font font = null;

    public static MouseEvent awt2vsdkEvent(java.awt.event.MouseEvent eawt)
    {
        MouseEvent evsdk;

        evsdk = new MouseEvent();
        evsdk.setX(eawt.getX());
        evsdk.setY(eawt.getY());
        evsdk.setButton(eawt.getButton());
        evsdk.setModifiers(eawt.getModifiersEx());

        return evsdk;
    }

    public static MouseEvent awt2vsdkEvent(java.awt.event.MouseWheelEvent eawt)
    {
        MouseEvent evsdk;

        evsdk = new MouseEvent();
        evsdk.setX(eawt.getX());
        evsdk.setY(eawt.getY());
        evsdk.setButton(eawt.getButton());
        evsdk.setModifiers(eawt.getModifiersEx());
        evsdk.setClicks(eawt.getWheelRotation());

        return evsdk;
    }

    public static KeyEvent awt2vsdkEvent(java.awt.event.KeyEvent eawt)
    {
        KeyEvent evsdk;

        evsdk = new KeyEvent();
        awt2vsdkEvent(evsdk, eawt);
        return evsdk;
    }

    public static void awt2vsdkEvent(KeyEvent evsdk, java.awt.event.KeyEvent eawt)
    {
        //-----------------------------------------------------------------
        char unicode_id;
        int keycode;

        unicode_id = eawt.getKeyChar();
        keycode = eawt.getKeyCode();

        //-----------------------------------------------------------------
        if ( ((eawt.getModifiersEx()) & java.awt.event.KeyEvent.ALT_DOWN_MASK) != 0x0 ) {
            evsdk.modifierMask |= KeyEvent.MASK_ALT;
        }
        if ( ((eawt.getModifiersEx()) & java.awt.event.KeyEvent.CTRL_DOWN_MASK) != 0x0 ) {
            evsdk.modifierMask |= KeyEvent.MASK_CTRL;
        }
        if ( ((eawt.getModifiersEx()) & java.awt.event.KeyEvent.SHIFT_DOWN_MASK) != 0x0 ) {
            evsdk.modifierMask |= KeyEvent.MASK_SHIFT;
        }

        //-----------------------------------------------------------------
        if ( eawt.getKeyLocation() == java.awt.event.KeyEvent.KEY_LOCATION_NUMPAD ) {
            switch ( keycode ) {
            /*case 110:
                evsdk.keycode = java.awt.event.KeyEvent.KEY_NUMPERIOD;
                break;*/
            }
        }

        switch ( keycode ) {
          case java.awt.event.KeyEvent.VK_ESCAPE:
            evsdk.keycode = KeyEvent.KEY_ESC;
            return;
          case java.awt.event.KeyEvent.VK_NUMPAD0:
            evsdk.keycode = KeyEvent.KEY_NUM0;
            return;
          case java.awt.event.KeyEvent.VK_NUMPAD1:
            evsdk.keycode = KeyEvent.KEY_NUM1;
            return;
          case java.awt.event.KeyEvent.VK_NUMPAD2:
            evsdk.keycode = KeyEvent.KEY_NUM2;
            return;
          case java.awt.event.KeyEvent.VK_NUMPAD3:
            evsdk.keycode = KeyEvent.KEY_NUM3;
            return;
          case java.awt.event.KeyEvent.VK_NUMPAD4:
            evsdk.keycode = KeyEvent.KEY_NUM4;
            return;
          case java.awt.event.KeyEvent.VK_NUMPAD5:
            evsdk.keycode = KeyEvent.KEY_NUM5;
            return;
          case java.awt.event.KeyEvent.VK_NUMPAD6:
            evsdk.keycode = KeyEvent.KEY_NUM6;
            return;
          case java.awt.event.KeyEvent.VK_NUMPAD7:
            evsdk.keycode = KeyEvent.KEY_NUM7;
            return;
          case java.awt.event.KeyEvent.VK_NUMPAD8:
            evsdk.keycode = KeyEvent.KEY_NUM8;
            return;
          case java.awt.event.KeyEvent.VK_NUMPAD9:
            evsdk.keycode = KeyEvent.KEY_NUM9;
            return;
          case java.awt.event.KeyEvent.VK_ENTER:
            evsdk.keycode = KeyEvent.KEY_ENTER;
            break;
          case java.awt.event.KeyEvent.VK_BACK_SPACE:
            evsdk.keycode = KeyEvent.KEY_BACKSPACE;
            break;
          case java.awt.event.KeyEvent.VK_DELETE:
            evsdk.keycode = KeyEvent.KEY_DELETE;
            break;
        }

        evsdk.unicode_id = unicode_id;
        if ( unicode_id == java.awt.event.KeyEvent.CHAR_UNDEFINED ) {
            evsdk.unicode_id = KeyEvent.KEY_NONE;
            switch ( keycode ) {
              case java.awt.event.KeyEvent.VK_F1:
                evsdk.keycode = KeyEvent.KEY_F1;
                break;
              case java.awt.event.KeyEvent.VK_F2:
                evsdk.keycode = KeyEvent.KEY_F2;
                break;
              case java.awt.event.KeyEvent.VK_F3:
                evsdk.keycode = KeyEvent.KEY_F3;
                break;
              case java.awt.event.KeyEvent.VK_F4:
                evsdk.keycode = KeyEvent.KEY_F4;
                break;
              case java.awt.event.KeyEvent.VK_F5:
                evsdk.keycode = KeyEvent.KEY_F5;
                break;
              case java.awt.event.KeyEvent.VK_F6:
                evsdk.keycode = KeyEvent.KEY_F6;
                break;
              case java.awt.event.KeyEvent.VK_F7:
                evsdk.keycode = KeyEvent.KEY_F7;
                break;
              case java.awt.event.KeyEvent.VK_F8:
                evsdk.keycode = KeyEvent.KEY_F8;
                break;
              case java.awt.event.KeyEvent.VK_F9:
                evsdk.keycode = KeyEvent.KEY_F9;
                break;
              case java.awt.event.KeyEvent.VK_F10:
                evsdk.keycode = KeyEvent.KEY_F10;
                break;
              case java.awt.event.KeyEvent.VK_F11:
                evsdk.keycode = KeyEvent.KEY_F11;
                break;
              case java.awt.event.KeyEvent.VK_F12:
                evsdk.keycode = KeyEvent.KEY_F12;
                break;
              case java.awt.event.KeyEvent.VK_UP:
                evsdk.keycode = KeyEvent.KEY_UP;
                break;
              case java.awt.event.KeyEvent.VK_DOWN:
                evsdk.keycode = KeyEvent.KEY_DOWN;
                break;
              case java.awt.event.KeyEvent.VK_LEFT:
                evsdk.keycode = KeyEvent.KEY_LEFT;
                break;
              case java.awt.event.KeyEvent.VK_RIGHT:
                evsdk.keycode = KeyEvent.KEY_RIGHT;
                break;
              case java.awt.event.KeyEvent.VK_ALT:
                evsdk.keycode = KeyEvent.KEY_LALT;
                break;
              case java.awt.event.KeyEvent.VK_CONTROL:
                evsdk.keycode = KeyEvent.KEY_LCTRL;
                break;
            }
        }
        else {
            switch ( unicode_id ) {
              case 'A':
                evsdk.keycode = KeyEvent.KEY_A;
                break;
              case 'B':
                evsdk.keycode = KeyEvent.KEY_B;
                break;
              case 'C':
                evsdk.keycode = KeyEvent.KEY_C;
                break;
              case 'D':
                evsdk.keycode = KeyEvent.KEY_D;
                break;
              case 'E':
                evsdk.keycode = KeyEvent.KEY_E;
                break;
              case 'F':
                evsdk.keycode = KeyEvent.KEY_F;
                break;
              case 'G':
                evsdk.keycode = KeyEvent.KEY_G;
                break;
              case 'H':
                evsdk.keycode = KeyEvent.KEY_H;
                break;
              case 'I':
                evsdk.keycode = KeyEvent.KEY_I;
                break;
              case 'J':
                evsdk.keycode = KeyEvent.KEY_J;
                break;
              case 'K':
                evsdk.keycode = KeyEvent.KEY_K;
                break;
              case 'L':
                evsdk.keycode = KeyEvent.KEY_L;
                break;
              case 'M':
                evsdk.keycode = KeyEvent.KEY_M;
                break;
              case 'N':
                evsdk.keycode = KeyEvent.KEY_N;
                break;
              case 'O':
                evsdk.keycode = KeyEvent.KEY_O;
                break;
              case 'P':
                evsdk.keycode = KeyEvent.KEY_P;
                break;
              case 'Q':
                evsdk.keycode = KeyEvent.KEY_Q;
                break;
              case 'R':
                evsdk.keycode = KeyEvent.KEY_R;
                break;
              case 'S':
                evsdk.keycode = KeyEvent.KEY_S;
                break;
              case 'T':
                evsdk.keycode = KeyEvent.KEY_T;
                break;
              case 'U':
                evsdk.keycode = KeyEvent.KEY_U;
                break;
              case 'V':
                evsdk.keycode = KeyEvent.KEY_V;
                break;
              case 'W':
                evsdk.keycode = KeyEvent.KEY_W;
                break;
              case 'X':
                evsdk.keycode = KeyEvent.KEY_X;
                break;
              case 'Y':
                evsdk.keycode = KeyEvent.KEY_Y;
                break;
              case 'Z':
                evsdk.keycode = KeyEvent.KEY_Z;
                break;
              case 'a':
                evsdk.keycode = KeyEvent.KEY_a;
                break;
              case 'b':
                evsdk.keycode = KeyEvent.KEY_b;
                break;
              case 'c':
                evsdk.keycode = KeyEvent.KEY_c;
                break;
              case 'd':
                evsdk.keycode = KeyEvent.KEY_d;
                break;
              case 'e':
                evsdk.keycode = KeyEvent.KEY_e;
                break;
              case 'f':
                evsdk.keycode = KeyEvent.KEY_f;
                break;
              case 'g':
                evsdk.keycode = KeyEvent.KEY_g;
                break;
              case 'h':
                evsdk.keycode = KeyEvent.KEY_h;
                break;
              case 'i':
                evsdk.keycode = KeyEvent.KEY_i;
                break;
              case 'j':
                evsdk.keycode = KeyEvent.KEY_j;
                break;
              case 'k':
                evsdk.keycode = KeyEvent.KEY_k;
                break;
              case 'l':
                evsdk.keycode = KeyEvent.KEY_l;
                break;
              case 'm':
                evsdk.keycode = KeyEvent.KEY_m;
                break;
              case 'n':
                evsdk.keycode = KeyEvent.KEY_n;
                break;
              case 'o':
                evsdk.keycode = KeyEvent.KEY_o;
                break;
              case 'p':
                evsdk.keycode = KeyEvent.KEY_p;
                break;
              case 'q':
                evsdk.keycode = KeyEvent.KEY_q;
                break;
              case 'r':
                evsdk.keycode = KeyEvent.KEY_r;
                break;
              case 's':
                evsdk.keycode = KeyEvent.KEY_s;
                break;
              case 't':
                evsdk.keycode = KeyEvent.KEY_t;
                break;
              case 'u':
                evsdk.keycode = KeyEvent.KEY_u;
                break;
              case 'v':
                evsdk.keycode = KeyEvent.KEY_v;
                break;
              case 'w':
                evsdk.keycode = KeyEvent.KEY_w;
                break;
              case 'x':
                evsdk.keycode = KeyEvent.KEY_x;
                break;
              case 'y':
                evsdk.keycode = KeyEvent.KEY_y;
                break;
              case 'z':
                evsdk.keycode = KeyEvent.KEY_z;
                break;
              case '0':
                evsdk.keycode = KeyEvent.KEY_0;
                break;
              case '1':
                evsdk.keycode = KeyEvent.KEY_1;
                break;
              case '2':
                evsdk.keycode = KeyEvent.KEY_2;
                break;
              case '3':
                evsdk.keycode = KeyEvent.KEY_3;
                break;
              case '4':
                evsdk.keycode = KeyEvent.KEY_4;
                break;
              case '5':
                evsdk.keycode = KeyEvent.KEY_5;
                break;
              case '6':
                evsdk.keycode = KeyEvent.KEY_6;
                break;
              case '7':
                evsdk.keycode = KeyEvent.KEY_7;
                break;
              case '8':
                evsdk.keycode = KeyEvent.KEY_8;
                break;
              case '9':
                evsdk.keycode = KeyEvent.KEY_9;
                break;
              case ',':
                evsdk.keycode = KeyEvent.KEY_COMMA;
                break;
              case '.':
                evsdk.keycode = KeyEvent.KEY_PERIOD;
                break;
              case ' ':
                evsdk.keycode = KeyEvent.KEY_SPACE;
                break;
              case '\n':
                evsdk.keycode = KeyEvent.KEY_ENTER;
                break;
              case '\b':
                evsdk.keycode = KeyEvent.KEY_BACKSPACE;
                break;
            }
        }
    }

    public static RGBAImage calculateLabelImage(String label, ColorRgb color, 
						Font newfont)
    {
        font = newfont;
        return calculateLabelImage(label, color);
    }

    public static RGBAImage calculateLabelImage(String label, ColorRgb color)
    {
        return calculateLabelImage(label, color, 14);
    }
    
    public static RGBAImage calculateLabelImageBordered(
        String label, ColorRgb colorForeground, ColorRgb colorBackground, 
        int fontSize)
    {        
        font = new Font("Arial", Font.PLAIN, fontSize);

        return calculateLabelImageBordered(
            label, colorForeground, colorBackground, fontSize, font);        
    }

    public static RGBAImage calculateLabelImage(
        String label, ColorRgb color, int fontSize)
    {
        font = new Font("Arial", Font.PLAIN, fontSize);

        return calculateLabelImage(label, color, fontSize, font);
    }
    
    public static RGBAImage calculateLabelImage(
        String label, ColorRgb color, int fontSize, Font newFont)
    {
        
        font = newFont;
        Rectangle ri;

        //-----------------------------------------------------------------

        //- Obtain an offline Java2D graphics context ---------------------
        RGBAImage tmpImage;
        tmpImage = new RGBAImage();
        tmpImage.init(1, 1);

        BufferedImage bi;
        Graphics offlineContext;

        bi = AwtRGBAImageRenderer.exportToAwtBufferedImage(tmpImage);
        offlineContext = bi.getGraphics();
        offlineContext.setFont(font);
        FontMetrics fm = offlineContext.getFontMetrics();

        //- Calculate the required image area for current label & font ----
        Rectangle2D r = fm.getStringBounds(label, offlineContext);
        ri = r.getBounds();

        LineMetrics metrics;
        int up;

        metrics = fm.getLineMetrics(label, offlineContext);

        up = (int)(Math.ceil(metrics.getAscent()));

        //- Calculate label image -----------------------------------------
        RGBAImage labelImage;
        labelImage = new RGBAImage();
        labelImage.init(ri.width, ri.height);
        labelImage = fillImageWithText(labelImage, color, label, 0, up);

        return labelImage;
    }

    public static RGBAImage calculateLabelImageBordered(
        String label, ColorRgb colorFg, ColorRgb colorBg, 
        int fontSize, Font newFont)
    {        
        font = newFont;
        Rectangle ri;

        //-----------------------------------------------------------------

        //- Obtain an offline Java2D graphics context ---------------------
        RGBAImage tmpImage;
        tmpImage = new RGBAImage();
        tmpImage.init(1, 1);

        BufferedImage bi;
        Graphics offlineContext;

        bi = AwtRGBAImageRenderer.exportToAwtBufferedImage(tmpImage);
        offlineContext = bi.getGraphics();
        offlineContext.setFont(font);
        FontMetrics fm = offlineContext.getFontMetrics();

        //- Calculate the required image area for current label & font ----
        Rectangle2D r = fm.getStringBounds(label, offlineContext);
        ri = r.getBounds();

        LineMetrics metrics;
        int up;

        metrics = fm.getLineMetrics(label, offlineContext);

        up = (int)(Math.ceil(metrics.getAscent()));

        //- Calculate label image -----------------------------------------
        RGBAImage labelImage;
        labelImage = new RGBAImage();
        labelImage.init(ri.width+2, ri.height+2);
        labelImage = fillImageWithText(labelImage, colorBg, label, 0, up+1);
        labelImage = fillImageWithText(labelImage, colorBg, label, 2, up+1);
        labelImage = fillImageWithText(labelImage, colorBg, label, 1, up);
        labelImage = fillImageWithText(labelImage, colorBg, label, 1, up+2);
        labelImage = fillImageWithText(labelImage, colorFg, label, 1, up+1);

        return labelImage;
    }

    private static RGBAImage fillImageWithText(
        RGBAImage labelImage, ColorRgb color, String label, int dx, int dy) {        
        BufferedImage bi;
        Graphics offlineContext;
        bi = AwtRGBAImageRenderer.exportToAwtBufferedImage(labelImage);
        offlineContext = bi.getGraphics();
        offlineContext.setFont(font);
        offlineContext.setColor(
            new Color((float)color.r, (float)color.g, (float)color.b));
        offlineContext.drawString(label, dx, dy);
        AwtRGBAImageRenderer.importFromAwtBufferedImage(bi, labelImage);
        return labelImage;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
