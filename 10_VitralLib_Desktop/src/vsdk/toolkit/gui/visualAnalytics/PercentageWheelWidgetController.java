//===========================================================================
package vsdk.toolkit.gui.visualAnalytics;

// VSDK classes
import vsdk.toolkit.common.Ray;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.environment.geometry.InfinitePlane;
import vsdk.toolkit.gui.Controller;
import vsdk.toolkit.gui.MouseEvent;

/**
*/
public class PercentageWheelWidgetController extends Controller {
    
    private final PercentageWheelWidget controlledWidget;
    
    public PercentageWheelWidgetController(PercentageWheelWidget controlledWidget)
    {
        this.controlledWidget = controlledWidget;
    }
    
    public boolean processMousePressedEvent(MouseEvent e, Camera c)
    {
        int i;

        i = selectSector(e.getX(), e.getY(), c);
        
        if ( i == -1 ) {
            return false;
        }
        
        controlledWidget.setSelectedSector(i);

        return true;   
    }
    
    public boolean processMouseReleasedEvent(MouseEvent e)
    {
        return false;        
    }
    
    public boolean processMouseClickedEvent(MouseEvent e)
    {
        return false;        
    }
    
    public boolean processMouseMovedEvent(MouseEvent e, Camera c)
    {
        int i;

        i = selectSector(e.getX(), e.getY(), c);
        
        if ( i == -1 ) {
            return false;
        }
        
        controlledWidget.setHighligtedSector(i);

        return true;   
    }

    private int selectSector(int x, int y, Camera c) {
        int i;
        Vector3D p = controlledWidget.getPosition();
        Ray ray;
        c.updateVectors();
        ray = c.generateRay(x, y);
        InfinitePlane plane;
        plane = new InfinitePlane(new Vector3D(0, 0, 1), p);
        if ( !plane.doIntersection(ray) ) {
            return -1;
        }

        Vector3D inPlane;
        double r;
        double angle;
        
        inPlane = ray.origin.add(ray.direction.multiply(ray.t)).substract(p);
        r = inPlane.length();
        angle = Math.toDegrees(inPlane.obtainSphericalThetaAngle());

        r = r/controlledWidget.getScale();
        
        if ( r < controlledWidget.getInnerRadius() ) {
            // User has click inside label center, nothing happens
            return -1;
        }
        else if ( r > controlledWidget.getOuterRadius() +
                      controlledWidget.getBorderWidth() ) {
            // User has click outside wheel, nothing happens
            return -1;
        }

        int N;
        N = controlledWidget.getDataset().getDoubles().size();

        i = (int)Math.floor(angle * (((double)N) / 360.0));
        return i;
    }

    /**
     * @return the controlledWidget
     */
    public PercentageWheelWidget getControlledWidget() {
        return controlledWidget;
    }
   
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
