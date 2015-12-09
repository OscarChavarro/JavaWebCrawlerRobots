//===========================================================================
package vsdk.toolkit.gui.variable;

import vsdk.toolkit.media.Image;

/**
*/
public class GuiBooleanVariable extends GuiVariable {

    private Image imageForTrueState;
    private Image imageForFalseState;
    private int style;
    
    public GuiBooleanVariable() {
        this.validRange = "false, true";
        imageForTrueState = null;
        imageForFalseState = null;
    }

    
    
    @Override
    public String getType() {
        return "Boolean";
    }

    @Override
    public String getValidRange() {
        return validRange;
    }
    //New Oscar August 13/2014
     public Image getImageForTrueState() {
        return imageForTrueState;
    }

    public Image getImageForFalseState() {
        return imageForFalseState;
    }

    public String setValidRange() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setValidRange(String vr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    //New Oscar August 13/2014
    public void setImageForTrueState(Image imageForTrueState) {
        this.imageForTrueState = imageForTrueState;
    }

    public void setImageForFalseState(Image imageForFalseState) {
        this.imageForFalseState = imageForFalseState;
    }

  
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
