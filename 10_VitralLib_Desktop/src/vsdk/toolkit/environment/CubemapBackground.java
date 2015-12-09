//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - November 27 2005 - Oscar Chavarro: Original base version              =
//= - February 15 2006 - Oscar Chavarro: Implemented true colorInDirection  =
//= - May 3 2007 - Oscar Chavarro: colorInDirection corrected for cubemap   =
//===========================================================================

package vsdk.toolkit.environment;

import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.Ray;
import vsdk.toolkit.environment.geometry.GeometryIntersectionInformation;
import vsdk.toolkit.environment.geometry.Box;
import vsdk.toolkit.media.RGBAImage;

public class CubemapBackground extends Background {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20060502L;

    private RGBAImage [] backgroundImages;
    private Camera camera;
    private Box boundingCube = null;

    public CubemapBackground(Camera camera,
                             RGBAImage front,
                             RGBAImage right,
                             RGBAImage back,
                             RGBAImage left,
                             RGBAImage down,
                             RGBAImage up) {
        super();

        this.camera = camera;
        backgroundImages = new RGBAImage[6];
        backgroundImages[0] = front;
        backgroundImages[1] = right;
        backgroundImages[2] = back;
        backgroundImages[3] = left;
        backgroundImages[4] = down;
        backgroundImages[5] = up;
        boundingCube = new Box(1, 1, 1);
    }

    /**
    @param d
    @return color as viewed in given direction
    */
    @Override
    public ColorRgb colorInDireccion(Vector3D d)
    {
        double u;
        double v;
        RGBAImage img;

        d.normalize();
        Ray r = new Ray(new Vector3D(0, 0, 0), d);
        if ( !boundingCube.doIntersection(r) ) {
            return new ColorRgb();
        }
        GeometryIntersectionInformation data;
        data = new GeometryIntersectionInformation();
        boundingCube.doExtraInformation(r, r.t, data);

        int plane = boundingCube.getLastIntersectedPlane();

        u = 1 - data.u;
        v = 1 - data.v;
        switch ( plane ) {
          case 1: // Top
            img = backgroundImages[5];
            u = 1 - data.v;
            v = data.u;
            break;
          case 2: // Down
            img = backgroundImages[4];
            u = data.v;
            v = 1 - data.u;
            break;
          case 3: // Front
            img = backgroundImages[0];
            break;
          case 4: // Back
            img = backgroundImages[2];
            break;
          case 5: // Right
            img = backgroundImages[1];
            break;
          default: // Left
            img = backgroundImages[3];
            break;
        }

        return img.getColorRgbBiLinear(u, v);
    }

    public RGBAImage [] getImages()
    {
        return backgroundImages;
    }

    public Camera getCamera()
    {
        return camera;
    }

    public void setCamera(Camera camera)
    {
        this.camera = camera;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
