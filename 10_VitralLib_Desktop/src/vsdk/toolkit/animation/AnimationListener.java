//===========================================================================
package vsdk.toolkit.animation;

public abstract class AnimationListener {
    public abstract void tick(AnimationEvent e);

    public boolean isPaused() {
        return false;
    }

    public void setPaused(boolean paused) {
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
