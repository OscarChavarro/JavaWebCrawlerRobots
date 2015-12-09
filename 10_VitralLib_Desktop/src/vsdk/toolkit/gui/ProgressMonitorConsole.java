//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - March 19 2006 - Oscar Chavarro: Original base version                 =
//===========================================================================

package vsdk.toolkit.gui;

import vsdk.toolkit.common.VSDK;

public class ProgressMonitorConsole extends ProgressMonitor {

    private double currentPercent;
    private double jumpPercent;
    private int lastPrintedLabel;

    public ProgressMonitorConsole()
    {
    }

    @Override
    public void begin()
    {
        currentPercent = 0;
        lastPrintedLabel = 0;
        jumpPercent = 2;
        System.out.print("[ 0% ");
    }

    @Override
    public void end()
    {
        System.out.println(" 100% ]");
    }

    private boolean
    testLabelLimit(int limit)
    {
        if ( limit == lastPrintedLabel ) return false;

        if ( currentPercent - 6*jumpPercent/10 < limit &&
             currentPercent + 6*jumpPercent/10 > limit )
        {
            System.out.print(" " + limit + "% ");
            lastPrintedLabel = limit;
            return true;
        }
        return false;
    }

    @Override
    public void
    update(double minValue, double maxValue, double currentValue)
    {
        if ( (maxValue - minValue) < VSDK.EPSILON ) {
            return;
        }
        double v = 100 * (currentValue - minValue) / (maxValue - minValue);

        while ( currentPercent + jumpPercent < v ) {
            currentPercent += jumpPercent;

            if ( !testLabelLimit(25) &&
                 !testLabelLimit(50) && 
                 !testLabelLimit(75) ) {
                System.out.print("-");
            }
        }
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
