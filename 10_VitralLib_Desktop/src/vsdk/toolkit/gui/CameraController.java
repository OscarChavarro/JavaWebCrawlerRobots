//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - August 8 2005 - Oscar Chavarro: Original base version                 =
//===========================================================================

package vsdk.toolkit.gui;

import vsdk.toolkit.environment.Camera;

public abstract class CameraController extends Controller {
    public abstract boolean processKeyPressedEvent(KeyEvent keyEvent);

    public abstract boolean processKeyReleasedEvent(KeyEvent keyEvent);
    
    public abstract boolean processMousePressedEvent(MouseEvent e);
    
    public abstract boolean processMouseReleasedEvent(MouseEvent e);
    
    public abstract boolean processMouseClickedEvent(MouseEvent e);
    
    public abstract boolean processMouseMovedEvent(MouseEvent e);
    
    public abstract boolean processMouseDraggedEvent(MouseEvent e);
    
    public abstract boolean processMouseWheelEvent(MouseEvent e);
    
    public abstract Camera getCamera();
    public abstract void setCamera(Camera camera);
    public abstract void setDeltaMovement(double factor);
    
    public void tick(double inCurrentTime)
    {        
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
