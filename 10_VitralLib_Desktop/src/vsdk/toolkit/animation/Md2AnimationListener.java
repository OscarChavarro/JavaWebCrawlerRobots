//===========================================================================
package vsdk.toolkit.animation;

// VitralSDK classes
import vsdk.toolkit.environment.geometry.Md2Mesh;

/**
*/
public class Md2AnimationListener  extends AnimationListener {
    private final Md2Mesh md2Mesh;

    public Md2AnimationListener(Md2Mesh md2Mesh)
    {
        this.md2Mesh = md2Mesh;
    }

    @Override
    public void tick(AnimationEvent e) {
        // Vector3D p = new Vector3D(e.getT()*10.0, 0.0, 1.0);
        
        if ( md2Mesh != null ) {
            //model.setAnimationTestCursorPosition(p);
            md2Mesh.setElapsedTimeSeg((float)e.getT());
        }
    }    
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
