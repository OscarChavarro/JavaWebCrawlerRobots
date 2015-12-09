//===========================================================================
package vsdk.toolkit.gui;

import java.util.HashMap;
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.media.RGBAImage;

/**
This class is used to set visual characteristics for text rendering.
*/
public class TextVisualConfiguration extends PresentationElement {
    private String fontName;
    private int fontSize;
    private ColorRgb foregroundColor;
    private ColorRgb backgroundColor;
    private int borderSize;
    private final HashMap<String, RGBAImage> characterSprites;

    public TextVisualConfiguration()
    {
        characterSprites = new HashMap<String, RGBAImage>();
        fontSize = 16;
        borderSize = 1;
        foregroundColor = new ColorRgb(1, 1, 1);
        backgroundColor = new ColorRgb(0, 0, 0);
    }

    /**
    @return the fontName
    */
    public String getFontName() {
        return fontName;
    }

    /**
    @param fontName the fontName to set
    */
    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    /**
    @return the fontSize
    */
    public int getFontSize() {
        return fontSize;
    }

    /**
    @param fontSize the fontSize to set
    */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /**
    @return the foregroundColor
    */
    public ColorRgb getForegroundColor() {
        return foregroundColor;
    }

    /**
    @param foregroundColor the foregroundColor to set
    */
    public void setForegroundColor(ColorRgb foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    /**
    @return the backgroundColor
    */
    public ColorRgb getBackgroundColor() {
        return backgroundColor;
    }

    /**
    @param backgroundColor the backgroundColor to set
    */
    public void setBackgroundColor(ColorRgb backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
    @return the characterSprites
    */
    public HashMap<String, RGBAImage> getCharacterSprites() {
        return characterSprites;
    }

    /**
    @return the borderSize
    */
    public int getBorderSize() {
        return borderSize;
    }

    /**
    @param borderSize the borderSize to set
    */
    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
