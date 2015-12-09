//===========================================================================
package vsdk.toolkit.gui.variable;

/**
*/
public class GuiStringVariable extends GuiVariable {
    @Override
    public String getType() {
        return "String";
    }

    @Override
    public String getValidRange() {
        return  validRange;
    }

    public String setValidRange() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setValidRange(String vr) {
        validRange = vr;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
