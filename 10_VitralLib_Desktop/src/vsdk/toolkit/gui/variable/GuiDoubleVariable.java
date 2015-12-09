//===========================================================================
package vsdk.toolkit.gui.variable;

/**
*/
public class GuiDoubleVariable extends GuiVariable {
    @Override
    public String getType() {
        return "Double";
    }
    
    public GuiDoubleVariable() {
        super();
        this.validRange = "(-INF, INF)";
    }

    @Override
    public String getValidRange() {
        return validRange;
    }

    @Override
    public void setValidRange(String vr) {
        validRange = vr;
    }


    public String setValidRange() {
        return "Raro";
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
