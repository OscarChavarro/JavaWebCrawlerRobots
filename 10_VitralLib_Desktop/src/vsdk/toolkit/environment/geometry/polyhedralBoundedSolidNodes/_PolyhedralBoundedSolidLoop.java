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

import vsdk.toolkit.common.CircularDoubleLinkedList;
import vsdk.toolkit.common.FundamentalEntity;

/**
As noted in [MANT1988].10.2.1, a `_PolyhedralBoundedSolidLoop` describes
one connected boundary inside a `_PolyhedralBoundedSolidFace`.

Note that in the sake of simplify and eficiency current programming 
implementation of this class exhibit public access attributes. It is important
to note that those attributes will only be accessed directly from related 
classes in the same package
(vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes) and
from methods in the `PolyhedralBoundedSolid` class, and that they should
not be used from outer classes.
*/
public class _PolyhedralBoundedSolidLoop extends FundamentalEntity {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20061118L;

    /// Defined as presented in [MANT1988].10.2.1
    public _PolyhedralBoundedSolidFace parentFace;

    /// As noted in [MANT1988].10.2.3, consider that there is a special
    /// case for empty loops. Note that this case doesn't affect this
    /// reference.
    public _PolyhedralBoundedSolidHalfEdge boundaryStartHalfEdge;

    public CircularDoubleLinkedList<_PolyhedralBoundedSolidHalfEdge> halfEdgesList;

    //=================================================================
    public _PolyhedralBoundedSolidLoop(_PolyhedralBoundedSolidFace parent)
    {
        init(parent);
    }

    private void init(_PolyhedralBoundedSolidFace parent)
    {
        parentFace = parent;
        parentFace.boundariesList.add(this);
        halfEdgesList =
            new CircularDoubleLinkedList<_PolyhedralBoundedSolidHalfEdge>();
    }

    public void unlistHalfEdge(_PolyhedralBoundedSolidHalfEdge he)
    {
        if ( halfEdgesList.locateWindowAtElem(he) ) {
            halfEdgesList.removeElemAtWindow();
        }
        if ( halfEdgesList.size() > 0  ) {
            boundaryStartHalfEdge = halfEdgesList.get(0);
        }
        else {
            boundaryStartHalfEdge = null;
        }
    }

    /** 
    Locates a half edge that goes from vertex with id `a` to vertex with
    id `b`.  Returns null if no such half edge exists in this loop.
    @param a
    @param b
    @return requested halfedge
    */
    public _PolyhedralBoundedSolidHalfEdge halfEdgeVertices(int a, int b)
    {
        _PolyhedralBoundedSolidHalfEdge he, oldhe;
        he = boundaryStartHalfEdge;
        do {
            oldhe = he;
            he = he.next();
            if ( he == null ) {
                // Loop is not closed!
                break;
            }

            if ( oldhe.startingVertex.id == a && he.startingVertex.id == b) {
                return oldhe;
            }

        } while( he != boundaryStartHalfEdge );
        return null;
    }

    /** Locates a half edge that goes from vertex with id `a` to vertex with
    id `b`.  Returns null if no such half edge exists in this loop. */
    public _PolyhedralBoundedSolidHalfEdge firstHalfEdgeAtVertex(int a)
    {
        _PolyhedralBoundedSolidHalfEdge he, oldhe;
        he = boundaryStartHalfEdge;
        do {
            oldhe = he;
            he = he.next();
            if ( he == null ) {
                // Loop is not closed!
                break;
            }

            if ( oldhe.startingVertex.id == a ) {
                return oldhe;
            }

        } while( he != boundaryStartHalfEdge );
        return null;
    }

    /**
    Vitral SDK's current implementation of original `delhe` utility function
    presented at program [MANT1988].11.4. and section [MANT1988].11.2.2.
    Note that current implementation is quite diferent from the original from
    [MANT1988]. This could lead to subtle problems! This method's functionality
    should be better understood!
    */
    public void delhe(_PolyhedralBoundedSolidHalfEdge he)
    {
        halfEdgesList.locateWindowAtElem(he);
        halfEdgesList.removeElemAtWindow();

        if ( halfEdgesList.size() > 0 ) {
            boundaryStartHalfEdge = halfEdgesList.get(0);
        }
    }

    public void revert()
    {
        if ( halfEdgesList.size() <= 0 ) {
            return;
        }

        //-----------------------------------------------------------------
        _PolyhedralBoundedSolidHalfEdge he;
        _PolyhedralBoundedSolidEdge edges[], elast;
        boolean sides[], slast;
        int i;

        edges = new _PolyhedralBoundedSolidEdge[halfEdgesList.size()];
        sides = new boolean[halfEdgesList.size()];

        for ( i = 0; i < halfEdgesList.size(); i++ ) {
            he = halfEdgesList.get(i);
            edges[i] = he.parentEdge;
            sides[i] = false;
            if ( he.parentEdge.rightHalf == he ) {
                sides[i] = true;
            }
        }

        for ( i = 1; i < halfEdgesList.size(); i++ ) {
            halfEdgesList.get(i).parentEdge = edges[i-1];
            if ( sides[i-1] ) {
                edges[i-1].rightHalf = halfEdgesList.get(i);
            }
            else {
                edges[i-1].leftHalf = halfEdgesList.get(i);
            }
        }
        halfEdgesList.get(0).parentEdge = edges[i-1];
        if ( sides[i-1] ) {
            edges[i-1].rightHalf = halfEdgesList.get(0);
        }
        else {
            edges[i-1].leftHalf = halfEdgesList.get(0);
        }

        //-----------------------------------------------------------------
        halfEdgesList.reverse();
    }

    @Override
    public String toString()
    {
        String msg;

        msg = "Loop, parent face " + parentFace.id;

        return msg;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
