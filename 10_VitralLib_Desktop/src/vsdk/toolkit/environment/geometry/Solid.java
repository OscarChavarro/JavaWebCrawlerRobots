//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 2 2006 - Oscar Chavarro: Original base version                    =
//===========================================================================

package vsdk.toolkit.environment.geometry;

import vsdk.toolkit.common.linealAlgebra.Vector3D;

public abstract class Solid extends Geometry {
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final long serialVersionUID = 20150218L;
    
    /**
    Given current solid, the method `doCenterOfMass` returns a vector
    containing the center of mass for current solid, assuming that all the
    solid interior is filled with a material of constant density.

    This method should be overwritten and defined for every solid

    @return a new Vector3D containing the coordinate of current solid
    center of mass.
    */
    public Vector3D doCenterOfMass() {
        return new Vector3D(0, 0, 0);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
