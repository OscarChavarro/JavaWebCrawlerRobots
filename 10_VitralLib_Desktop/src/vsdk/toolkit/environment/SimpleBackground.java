//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - November 27 2005 - Oscar Chavarro: Original base version              =
//===========================================================================

package vsdk.toolkit.environment;

import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.common.linealAlgebra.Vector3D;

public class SimpleBackground extends Background {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20060502L;

    private ColorRgb _color;

    public SimpleBackground() {
        super();

        _color = new ColorRgb();
        _color.r = 0;
        _color.g = 0;
        _color.b = 0;
    }

    @Override
    public ColorRgb colorInDireccion(Vector3D d)
    {
        return _color;
    }

    public void setColor(double r, double g, double b)
    {
        _color.r = r;
        _color.g = g;
        _color.b = b;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
