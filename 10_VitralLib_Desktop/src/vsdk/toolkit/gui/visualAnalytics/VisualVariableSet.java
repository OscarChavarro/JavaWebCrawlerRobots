//===========================================================================
package vsdk.toolkit.gui.visualAnalytics;

import java.util.ArrayList;
import vsdk.toolkit.gui.PresentationElement;

/**
A visual variable set is a container for visual variables used on visual
analytics widgets.
*/
public class VisualVariableSet extends PresentationElement {
    private final ArrayList<VisualDoubleVariable> doubles;

    public VisualVariableSet()
    {
        doubles = new ArrayList<VisualDoubleVariable>();
    }
        
    /**
    @return the doubles
    */
    public ArrayList<VisualDoubleVariable> getDoubles() {
        return doubles;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
