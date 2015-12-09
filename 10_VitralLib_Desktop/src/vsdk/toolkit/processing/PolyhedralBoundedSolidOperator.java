//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - April 8 2008 - Oscar Chavarro: Original base version                  =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [MANT1988] Mantyla Martti. "An Introduction To Solid Modeling",         =
//=     Computer Science Press, 1988.                                       =
//===========================================================================

package vsdk.toolkit.processing;

// VitralSDK classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.CircularDoubleLinkedList;
import vsdk.toolkit.environment.geometry.PolyhedralBoundedSolid;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidFace;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidLoop;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidEdge;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidHalfEdge;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidVertex;

/**
This class is not intended to be used directly, but rather as a common service
provider for classes PolyhedralBoundedSolidSplitter and
PolyhedralBoundedSolidSetOperator.
*/
public class PolyhedralBoundedSolidOperator extends GeometricModeler
{
    /**
    */
    private static boolean searchForEdge(
        CircularDoubleLinkedList<_PolyhedralBoundedSolidEdge> l,
        _PolyhedralBoundedSolidEdge e)
    {
        int i;

        for ( i = 0; i < l.size(); i++ ) {
            if ( l.get(i) == e ) return true;
        }
        return false;
    }

    /**
    */
    private static boolean searchForVertex(
        CircularDoubleLinkedList<_PolyhedralBoundedSolidVertex> l,
        _PolyhedralBoundedSolidVertex v)
    {
        int i;

        for ( i = 0; i < l.size(); i++ ) {
            if ( l.get(i) == v ) return true;
        }
        return false;
    }

    /**
    Following section [MANT1988].14.7.1 and program [MANT1988].14.8.
    */
    protected static boolean neighbor(_PolyhedralBoundedSolidHalfEdge h1, _PolyhedralBoundedSolidHalfEdge h2)
    {
        return (h1.parentLoop.parentFace == h2.parentLoop.parentFace) &&
            ( (
              h1 == h1.parentEdge.rightHalf && h2 == h2.parentEdge.leftHalf
              ) || 
              (
              h1 == h1.parentEdge.leftHalf && h2 == h2.parentEdge.rightHalf
              ) );
    }

    /**
    This is the answer to problem [MANT1988].14.2.

    \todo  Check for consistency of `emanatingHalfEdge` pointers for vertices.
    */
    protected static void cleanup(PolyhedralBoundedSolid s)
    {
        int i, j;
        _PolyhedralBoundedSolidFace f;
        _PolyhedralBoundedSolidLoop l;
        _PolyhedralBoundedSolidHalfEdge he;

        for ( i = 0; i < s.polygonsList.size(); i++ ) {
            f = s.polygonsList.get(i);
            for ( j = 0; j < f.boundariesList.size(); j++ ) {
                l = f.boundariesList.get(j);
                he = l.boundaryStartHalfEdge;
                do {
                    //
                    if ( !searchForEdge(s.edgesList, he.parentEdge) ) {
                        s.edgesList.add(he.parentEdge);
                    }
                    if ( !searchForVertex(s.verticesList, he.startingVertex) ) {
                        s.verticesList.add(he.startingVertex);
                        he.startingVertex.emanatingHalfEdge = he;
                    }
                    //
                    he = he.next();
                } while( he != l.boundaryStartHalfEdge );
            }
        }
    }

    /**
    Following section [MANT1988].14.8. and program [MANT1988].14.12.
    */
    protected static void movefac(_PolyhedralBoundedSolidFace f,
                                  PolyhedralBoundedSolid s)
    {
        _PolyhedralBoundedSolidLoop l;
        _PolyhedralBoundedSolidHalfEdge he;
        _PolyhedralBoundedSolidFace f2;
        int i;

        f.parentSolid.polygonsList.locateWindowAtElem(f);
        f.parentSolid.polygonsList.removeElemAtWindow();
        s.polygonsList.add(f);
        f.parentSolid = s;

        for ( i = 0; i < f.boundariesList.size(); i++ ) {
            l = f.boundariesList.get(i);
            he = l.boundaryStartHalfEdge;
            do {
                f2 = he.mirrorHalfEdge().parentLoop.parentFace;
                if ( f2.parentSolid != s ) {
                    movefac(f2, s);
                }
                he = he.next();
            } while( he != l.boundaryStartHalfEdge );
        }                
    }

    /**
    Constructs a vector along the bisector of the sector defined by `he`.
    Answer to problem [MANT1988].14.1.

    Current implementation assumes the following interpretation:
    Given a vertex of interest `he.startingVertex`, one can measure the angle
    of incidence of loop `he.parentLoop` on vertex of interest by measuring the
    angle between the halfedges `he` (direction `a`) and `he.previous` 
    (direction `b`).  The bisector vector is the one having its tail on the
    vertex of interest position `he.startingVertex.position` and its end
    pointing in the middle of `a` and `b` directions.

    This is the answer to problem [MANT1988].14.1.

    \todo : check current assumptions!

    This protected method is here for exclusive use of subclasses
    `PolyhedralBoundedSolidSplitter` and `PolyhedralBoundedSolidSetOperator`.
    */
    protected static Vector3D bisector(_PolyhedralBoundedSolidHalfEdge he)
    {
        Vector3D middle;
        Vector3D a, b;

        a = (he.next()).startingVertex.position.substract(he.startingVertex.position);
        b = (he.previous()).startingVertex.position.substract(he.startingVertex.position);
        a.normalize();
        b.normalize();

        middle = he.startingVertex.position.add((a.add(b)).multiply(0.5));

        return middle;
    }

    /**
    Moves those rings of `f1` that do not lie within its outer loop to
    `f2`.
    This procedure is used on the splitter and set operator algorithms to
    ensure that after a face has been divided by a MEF, all loops will end up
    in the correct halves.
    This is an answer to problem [MANT1988].13.5. Its use in the context of
    the splitter algorithm is briefly described on section [MANT1988].14.7.2.
    */
    private static void laringmv(_PolyhedralBoundedSolidFace f1,
                                 _PolyhedralBoundedSolidFace f2)
    {
        _PolyhedralBoundedSolidLoop l;
        int i;

        // It is supposed to move all (internal) rings from `f1` to `f2`
        // using PolyhedralBoundedSolid.lringmv
        for ( i = 1; i < f1.boundariesList.size(); i++ ) {
        l = f1.boundariesList.get(i);
            if ( f1.parentSolid.lringmv(l, f2, false) ) {
                i--;
            }
        }
    }

    /**
    Following section [MANT1988].14.7.2. and program [MANT1988].14.10.
    */
    protected static void
    join(_PolyhedralBoundedSolidHalfEdge h1, _PolyhedralBoundedSolidHalfEdge h2, boolean withDebug)
    {
        _PolyhedralBoundedSolidFace oldf, newf;
        PolyhedralBoundedSolid s;

        if ( withDebug ) {
            System.out.println("       -> JOIN:");
            System.out.println("          . H1: " + h1);
            System.out.println("          . H2: " + h2);
        }

        oldf = h1.parentLoop.parentFace;
        newf = null;
        s = oldf.parentSolid;
        if ( h1.parentLoop == h2.parentLoop ) {
            if ( h1.previous().previous() != h2 ) {
                newf = s.lmef(h1, h2.next(), s.getMaxFaceId()+1);
                if ( withDebug ) {
                    //h1.next().parentEdge.debugColor = new ColorRgb(1, 0, 0);
                }
            }
        }
        else {
            s.lmekr(h1, h2.next());
            if ( withDebug ) {
                //h1.next().parentEdge.debugColor = new ColorRgb(0, 1, 0);
            }
        }

        if ( h1.next().next() != h2 ) {
            s.lmef(h2, h1.next(), s.getMaxFaceId()+1);
            if ( withDebug ) {
                //h2.next().parentEdge.debugColor = new ColorRgb(0, 0, 1);
            }
            if ( newf != null && oldf.boundariesList.size() >= 2 ) {
                laringmv(oldf, newf);
            }
        }
    }

    /**
    This method checks whether the edges `he.previous().parentEdge` and
    `he.parentEdge` make a convex (less than 180 degrees) or concave
    (larger than 180 degrees) angle. In the first case the method returns
    `false` and `true` for the second case.
    This is an answer to problem [MANT1988].13.6.
    \todo  Pending to verify functionality against method proposed by [MANT1988].
    This should be a method of `PolyhedralBoundedSolid`, as it is of
    common utility to some other operations.

    PRE: Parent solid should be previously validated to contain correct
    face equations.

    This protected method is here for exclusive use of subclasses
    `PolyhedralBoundedSolidSplitter` and `PolyhedralBoundedSolidSetOperator`.
    */
    protected static boolean checkWideness (_PolyhedralBoundedSolidHalfEdge he)
    {
        Vector3D a, b, c;

        a = (he.next()).startingVertex.position.substract(he.startingVertex.position);
        b = (he.previous()).startingVertex.position.substract(he.startingVertex.position);
        a.normalize();
        b.normalize();

        c = b.crossProduct(a);

// HERE SHOULD COMPARE `c` WITH FACE NORMAL!

        if ( c.length() < 10*VSDK.EPSILON ) {
            // Angle greater than 180 degrees
            return true;
        }

        return false;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
