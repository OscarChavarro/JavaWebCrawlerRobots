//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - July 22 2007 - Oscar Chavarro: Original base version                  =
//===========================================================================

package vsdk.toolkit.environment.scene;

// Java basic classes
import java.util.ArrayList;

// VSDK Classes
import vsdk.toolkit.common.Entity;
import vsdk.toolkit.environment.Background;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.environment.Light;

public class SimpleScene extends Entity
{
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20100901L;

    private ArrayList<SimpleBody> simpleBodiesArray;
    private ArrayList<Light> lightsArray;
    private ArrayList<Background> backgroundsArray;
    private ArrayList<Camera> camerasArray;
    private int activeCameraIndex;
    private int activeBackgroundIndex;

    public SimpleScene()
    {
        simpleBodiesArray = new ArrayList<SimpleBody>();
        lightsArray = new ArrayList<Light>();
        backgroundsArray = new ArrayList<Background>();
        camerasArray = new ArrayList<Camera>();
    }

    public int getActiveCameraIndex()
    {
        return activeCameraIndex;
    }

    public int getActiveBackgroundIndex()
    {
        return activeBackgroundIndex;
    }

    public void setActiveCameraIndex(int i)
    {
        activeCameraIndex = i;
    }

    public void setActiveBackgroundIndex(int i)
    {
        activeBackgroundIndex = i;
    }

    public void addBody(SimpleBody b)
    {
        simpleBodiesArray.add(b);
    }

    public void addCamera(Camera c)
    {
        camerasArray.add(c);
    }

    public void addBackground(Background b)
    {
        backgroundsArray.add(b);
    }

    public void addLight(Light l)
    {
        lightsArray.add(l);
    }

    public ArrayList<SimpleBody> getSimpleBodies()
    {
        return simpleBodiesArray;
    }

    public ArrayList<Light> getLights()
    {
        return lightsArray;
    }

    public ArrayList<Background> getBackgrounds()
    {
        return backgroundsArray;
    }

    public ArrayList<Camera> getCameras()
    {
        return camerasArray;
    }

    public void setSimpleBodies(ArrayList<SimpleBody> simpleBodies)
    {
        simpleBodiesArray = simpleBodies;
    }

    public void setLights(ArrayList<Light> lights)
    {
        lightsArray = lights;
    }

    public void setBackgrounds(ArrayList<Background> backgrounds)
    {
        backgroundsArray = backgrounds;
    }

    public Background getActiveBackground()
    {
        return backgroundsArray.get(activeBackgroundIndex);
    }

    public Camera getActiveCamera()
    {
        return camerasArray.get(activeCameraIndex);
    }

    public void setCameras(ArrayList<Camera> cameras)
    {
        camerasArray = cameras;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
