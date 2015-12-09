//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - November 18 2006 - Oscar Chavarro: Original base version              =
//= - January 3 2007 - Oscar Chavarro: First phase implementation           =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [MANT1988] Mantyla Martti. "An Introduction To Solid Modeling",         =
//=     Computer Science Press, 1988.                                       =
//===========================================================================

package vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes;

import vsdk.toolkit.common.FundamentalEntity;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.environment.geometry.PolyhedralBoundedSolid;

/**
As noted in [MANT1988].10.2.2, a `_PolyhedralBoundedSolidVertex` contains
a vertex position for the geometric information of the boundary model,
and a reference to one of the halfedges emanating from it.
*/
public class _PolyhedralBoundedSolidVertex extends FundamentalEntity {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20061118L;

    /// Defined as presented in [MANT1988].10.2.1
    public int id;

    /// Defined as presented in [MANT1988].10.2.1
    public Vector3D position;

    /// Defined as presented in [MANT1988].10.2.2
    public _PolyhedralBoundedSolidHalfEdge emanatingHalfEdge;

    public ColorRgb debugColor;

    //=================================================================
    public _PolyhedralBoundedSolidVertex(PolyhedralBoundedSolid parentSolid,
                                         Vector3D position, int id)
    {
        init(parentSolid, position, id);
    }

    private void
    init(PolyhedralBoundedSolid parentSolid, Vector3D position, int id)
    {
        this.id = id;
        emanatingHalfEdge = null;
        this.position = new Vector3D(position);
        parentSolid.verticesList.add(this);
        debugColor = new ColorRgb(1, 0, 0);
    }

    @Override
    public String toString()
    {
        String msg;
        msg = "vertex id " + id + ". Position " + position + ". ";
        if ( emanatingHalfEdge == null ) {
            msg = msg + "No associated halfedge.";
        }
        else {
            msg = msg + "H.E. " + emanatingHalfEdge.id;
        }
        return msg;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
