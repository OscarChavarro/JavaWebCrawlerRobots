//===========================================================================
package vsdk.toolkit.gui.visualAnalytics;

import vsdk.toolkit.gui.PresentationElement;

/**
A visual variable contains information needed to display it on visual
analytics widgets.
*/
public class VisualDoubleVariable extends PresentationElement {
    private String name;
    private String description;
    private double currentValue;

    public VisualDoubleVariable()
    {
        name = null;
        description = null;
        currentValue = 0;
    }

    /**
    @return the name
    */
    public String getName() {
        return name;
    }

    /**
    @param name the name to set
    */
    public void setName(String name) {
        this.name = name;
    }

    /**
    @return the description
    */
    public String getDescription() {
        return description;
    }

    /**
    @param description the description to set
    */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
    @return the currentValue
    */
    public double getCurrentValue() {
        return currentValue;
    }

    /**
    @param currentValue the currentValue to set
    */
    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
