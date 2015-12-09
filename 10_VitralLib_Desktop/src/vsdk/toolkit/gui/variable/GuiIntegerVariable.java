//===========================================================================
package vsdk.toolkit.gui.variable;

/**
*/
public class GuiIntegerVariable extends GuiVariable {
    @Override
    public String getType() {
        return "Integer";
    }

    public GuiIntegerVariable() {
        super();
                
        this.validRange = "(-INF, INF)";
    }
    
    @Override
    public String getValidRange() {
        return validRange;
    }

    public String setValidRange() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setValidRange(String vr) {
        this.validRange = vr;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
