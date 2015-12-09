//===========================================================================
package vsdk.toolkit.animation;


// Java basic classes
import java.util.ArrayList;

// VSDK classes
import vsdk.toolkit.common.VSDK;

public class AnimationEventGenerator implements Runnable {
    private final int fps;
    private final ArrayList<AnimationListener> listeners;
    
    public AnimationEventGenerator()
    {
        fps = 24;
        listeners = new ArrayList<AnimationListener>();
    }

    public void addAnimationListener(AnimationListener l)
    {
        listeners.add(l);
    }
    
    private void dispatch(AnimationEvent e)
    {
        int i;

        for ( i = 0; i < listeners.size(); i++ ) {
            listeners.get(i).tick(e);
        } 
    }

    private double getRealTimeSeconds()
    {
        return (double)System.currentTimeMillis()/1000.0;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("ANIM: AnimationEventGenerator.run");
        
        double t;
        double t0 = getRealTimeSeconds();
        AnimationEvent e = new AnimationEvent();
        while ( true ) {
            t = getRealTimeSeconds() - t0;
            e.setT(t);
            try { Thread.sleep(1000/fps); }
            catch ( InterruptedException ex ) {
                VSDK.reportMessageWithException(
                    this, VSDK.FATAL_ERROR, "run", "Error in sleep", ex);
            }
            dispatch(e);
        }
    }
        
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
