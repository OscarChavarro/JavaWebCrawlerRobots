//===========================================================================
package vsdk.toolkit.gui;

import vsdk.toolkit.common.VSDK;

public class ProgressMonitorInRam extends ProgressMonitor {
    private double currentPercent;

    public ProgressMonitorInRam()
    {
    }

    @Override
    public void begin()
    {
        currentPercent = 0;
    }

    @Override
    public void end()
    {
        currentPercent = 100.0;
    }

    @Override
    public void
    update(double minValue, double maxValue, double currentValue)
    {
        if ( (maxValue - minValue) < VSDK.EPSILON ) {
            return;
        }
        currentPercent = 100 * (currentValue - minValue) / (maxValue - minValue);
    }

    /**
    @return the currentPercent
    */
    @Override
    public double getCurrentPercent() {
        return currentPercent;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
