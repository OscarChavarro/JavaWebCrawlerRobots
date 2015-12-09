//===========================================================================
package vsdk.toolkit.gui.variable;

/**
*/
public class GuiVector3DVariable extends GuiVariable {
    @Override
    public String getType() {
        return "Vector3D";
    }
    
    public GuiVector3DVariable() {
        super();
        this.validRange = "<(-INF, INF), (-INF, INF), (-INF, INF)>";
    }

    @Override
    public String getValidRange() {
        return this.validRange;
    }

    public void setValidRange() {
        
    }

    @Override
    public void setValidRange(String vr) {
        this.validRange = vr;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
