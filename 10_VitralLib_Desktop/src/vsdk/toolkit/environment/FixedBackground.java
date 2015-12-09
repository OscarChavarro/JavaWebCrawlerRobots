//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 9 2007 - Oscar Chavarro: Original base version                    =
//===========================================================================

package vsdk.toolkit.environment;

import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.media.RGBAImage;

public class FixedBackground extends Background {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20060502L;

    private RGBAImage backgroundImage;
    private Camera camera;

    public FixedBackground(Camera camera, RGBAImage image) {
        super();

        this.camera = camera;
        backgroundImage = image;
    }

    public void setImage(RGBAImage image)
    {
        backgroundImage = image;
    }

    public RGBAImage getImage()
    {
        return backgroundImage;
    }

    /**
    BUG: not working math!
    @param d
    @return color as viewed in given direction
    */
    @Override
    public ColorRgb colorInDireccion(Vector3D d)
    {
        return null;
/*
        InfinitePlane plane = camera.calculateNearPlane();
        Ray r = new Ray(camera.getPosition(), d);
        ColorRgb color = new ColorRgb();
        double u, v;
        Vector3D p;
        Vector3D left = camera.getLeft();
        Vector3D up = camera.getUp();
        Vector3D rel;
        double near = camera.getNearPlaneDistance();
        Vector3D front = camera.getFront();

        if ( plane.doIntersectionWithNegative(r) ) {
            p = r.origin.add(d.multiply(r.t));
            rel = p.substract(front.multiply(near));
            u = rel.dotProduct(left);
            v = rel.dotProduct(up);
            color.r = 1;
            color.g = 0;
            color.b = 0;
            if ( u >= -1 && u <= 1 && v >= -1 && v <= 1 ) {
                return backgroundImage.getColorRgbBiLinear(u, v);
            }
        }

        return color;
*/
    }


    public Camera getCamera()
    {
        return camera;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
