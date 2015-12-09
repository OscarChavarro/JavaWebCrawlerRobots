//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - April 30 2008 - Oscar Chavarro: Original base version                 =
//===========================================================================

package vsdk.toolkit.processing;

public class StopWatch extends ProcessingElement
{
    private long t0;
    private long t1;
    private boolean running;

    public StopWatch()
    {
        t0 = 0;
        t1 = 1;
        running = false;
    }

    public void start()
    {
        //t0 = System.currentTimeMillis();
        running = true;
        t0 = System.nanoTime();
    }

    public void stop()
    {
        //t1 = System.currentTimeMillis();
        if ( running ) {
            t1 = System.nanoTime();
        }
        running = false;
    }

    public double getElapsedRealTime()
    {
        if ( running ) {
            t1 = System.nanoTime();
        }
        double a, b;
        a = (double)t0;
        b = (double)t1;
        //return (b - a)/1000.0;
        return (b - a)/1000000000.0;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
