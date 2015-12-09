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

import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.FundamentalEntity;
import vsdk.toolkit.environment.geometry.PolyhedralBoundedSolid;

/**
As noted in [MANT1988].10.2.1, a `_PolyhedralBoundedSolidHalfEdge` describes
one line segment inside a `_PolyhedralBoundedSolidLoop`. It has only a
reference to a vertex in a `PolyhedralBoundedSolid`.

Note that in the sake of simplify and eficiency current programming 
implementation this class exhibit public access attributes. It is important
to note that those attributes will only be accessed directly from related 
classes in the same package
(vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes) and
from methods in the `PolyhedralBoundedSolid` class, and that they should
not be used from outer classes.
*/
public class _PolyhedralBoundedSolidHalfEdge extends FundamentalEntity {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20061118L;

    public static final int LEFT_SIDE = 1;
    public static final int RIGHT_SIDE = 2;
    public static final int NO_SIDE = 3;

    /// Defined as presented in [MANT1988].10.2.1
    public _PolyhedralBoundedSolidLoop parentLoop;

    /// Defined as presented in [MANT1988].10.2.2. Note that as commented in
    /// [MANT1988].10.2.2, this reference can be `null` in the special case
    /// of empty loops.
    public _PolyhedralBoundedSolidEdge parentEdge;

    /// Defined as presented in [MANT1988].10.2.1
    public _PolyhedralBoundedSolidVertex startingVertex;

    //
    public int id;
    private static int currentId = 1;

    //=================================================================
    public _PolyhedralBoundedSolidHalfEdge(_PolyhedralBoundedSolidVertex v,
        _PolyhedralBoundedSolidLoop parentLoop,
        PolyhedralBoundedSolid parentSolid)
    {
        startingVertex = v;
        this.parentLoop = parentLoop;
        parentEdge = null;

        id = currentId;
        currentId++;
    }

    /**
    Locates the previous half edge in current list
    @return requested half edge
    */
    public _PolyhedralBoundedSolidHalfEdge previous()
    {
        parentLoop.halfEdgesList.locateWindowAtElem(this);
        parentLoop.halfEdgesList.previous();
        return parentLoop.halfEdgesList.getWindow();
    }

    /**
    Locates the next half edge in current list
    @return requested halfedge
    */
    public _PolyhedralBoundedSolidHalfEdge next()
    {
        parentLoop.halfEdgesList.locateWindowAtElem(this);
        parentLoop.halfEdgesList.next();
        return parentLoop.halfEdgesList.getWindow();
    }

    private int
    determineSideness()
    {
        if ( parentEdge == null ) {
            return NO_SIDE;
        }
        if ( this == parentEdge.leftHalf ) {
            return LEFT_SIDE;
        }
        else if ( this == parentEdge.rightHalf ) {
            return RIGHT_SIDE;
        }
        return NO_SIDE;
    }

    /**
    Given current half edge, this method returns complementary half edge
    with respect to parent edge. Note that this code corresponds to macro
    `mate(he)`, defined in program [MANT1988] 10.2, and annotated in
    sections [MANT1988].10.3. and [MANT1988].10.4.2.
    @return complementary halfedge
    */
    public _PolyhedralBoundedSolidHalfEdge
    mirrorHalfEdge()
    {
        if ( parentEdge == null ) return null;

        if ( this == parentEdge.rightHalf ) {
            return parentEdge.leftHalf;
        }
        return parentEdge.rightHalf;
    }

    /**
    Given `this` and `other` halfedges, returns true if their starting vertexes
    position are nearly equal (with respect to a the given `tolerance`).
    This method follows the suggested funcionality of procedure "match" from
    program [MANT1988].12.9. and presented in section [MANT1988].12.4.2.
    @param other
    @param tolerance
    @return true if given halfedges are very close (under tolerance), false
    if they are far away
    */
    public boolean
    vertexPositionMatch(_PolyhedralBoundedSolidHalfEdge other, double tolerance)
    {
        return VSDK.vectorDistance(
            this.startingVertex.position,
            other.startingVertex.position
        ) <= tolerance;
    }

    @Override
    public String toString()
    {
        String msg;
        msg = "HalfEdge id " + id + ". ";

        msg = msg + "From vertex [" + startingVertex.id + "] ";
        msg = msg + "to vertex [" + next().startingVertex.id + "]. ";

        msg += "Parent face [" + parentLoop.parentFace.id + "]. ";
        if ( parentEdge == null ) {
            msg = msg + "<without parent edge>. ";
          }
          else {
            msg = msg + "Parent edge " + parentEdge.id + " ";
            if ( this == parentEdge.leftHalf ) {
                msg = msg + "(left)";
              }
              else if ( this == parentEdge.rightHalf ) {
                msg = msg + "(right)";
              }
              else {
                msg = msg + "(INCONSISTENT!)";
            }
            msg = msg + ". ";
        }
        msg = msg + "Next halfedge: " + next().id + ".";
        return msg;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
