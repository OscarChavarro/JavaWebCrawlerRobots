//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - March 26 2008 - Oscar Chavarro: Original base version                 =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [MANT1988] Mantyla Martti. "An Introduction To Solid Modeling",         =
//=     Computer Science Press, 1988.                                       =
//===========================================================================

package vsdk.toolkit.processing;

// Java classes
import java.util.ArrayList;
import java.util.Collections;

// VitralSDK classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.CircularDoubleLinkedList;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.InfinitePlane;
import vsdk.toolkit.environment.geometry.PolyhedralBoundedSolid;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidFace;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidLoop;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidEdge;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidHalfEdge;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidVertex;

/**
This class is used to store vertex / halfedge neigborhood information, as presented
in section [MANT1988].14.5, and program [MANT1988].14.3.
*/
class _PolyhedralBoundedSolidSplitterSectorClassification extends PolyhedralBoundedSolidOperator
{
    public static final int ABOVE = 1;
    public static final int BELOW = -1;
    public static final int ON = 0;

    public static final int COPLANAR_FACE = 10;
    public static final int INPLANE_EDGE = 20;
    public static final int CROSSING_EDGE = 30;
    public static final int UNDEFINED = 40;

    public _PolyhedralBoundedSolidHalfEdge sector;
    public int cl;

    // Following attributes are not taken from [MANT1988], and all operations
    // on them are fine tunning options aditional to original algorithm.
    public boolean isWide = false;
    public Vector3D position;
    public int situation = UNDEFINED;

    @Override
    public String toString()
    {
        String msg = "{";
        msg = msg + sector;
        switch ( cl ) {
          case ABOVE: msg = msg + " ABOVE"; break;
          case BELOW: msg = msg + " BELOW"; break;
          case ON: msg = msg + " ON"; break;
          default: msg = msg + "<INVALID!>"; break;
        }
        if ( isWide ) {
            msg = msg + " (W) ";
        }
        //msg = msg + ", pos: " + position;

        switch ( situation ) {
          case COPLANAR_FACE: msg = msg + "<COPLANAR_FACE>"; break;
          case INPLANE_EDGE: msg = msg + "<INPLANE_EDGE>"; break;
          case CROSSING_EDGE: msg = msg + "<CROSSING_EDGE>"; break;
          default: msg = msg + "<UNDEFINED>"; break;
        }

        msg = msg + "}";
        return msg;
    }
}

/**
Class `_PolyhedralBoundedSolidSplitterNullEdge` plays a role of a decorator
design patern for class `_PolyhedralBoundedSolidEdge`, and adds sort-ability.
*/
class _PolyhedralBoundedSolidSplitterNullEdge extends PolyhedralBoundedSolidOperator implements Comparable <_PolyhedralBoundedSolidSplitterNullEdge>
{
    public _PolyhedralBoundedSolidEdge e;

    public _PolyhedralBoundedSolidSplitterNullEdge(_PolyhedralBoundedSolidEdge e)
    {
        this.e = e;
    }

    @Override
    public int compareTo(_PolyhedralBoundedSolidSplitterNullEdge other)
    {
        Vector3D a;
        Vector3D b;

        a = this.e.rightHalf.startingVertex.position;
        b = other.e.rightHalf.startingVertex.position;

        if ( PolyhedralBoundedSolid.compareValue(a.x, b.x, 10*VSDK.EPSILON) != 0 ) {
            if ( a.x < b.x ) {
                return -1;
            }
            return 1;
        }
        else {
            if ( PolyhedralBoundedSolid.compareValue(a.y, b.y, 10*VSDK.EPSILON) != 0 ) {
                if ( a.y < b.y ) {
                    return -1;
                }
                return 1;
            }
            else {
                if ( a.z < b.z ) {
                    return -1;
                }
                return 1;
            }
        }
    }

}

/**
This is a utility class containing operations for implementing the boundary
representation split methods over winged-edge data structures, as presented
at chapter [MANT1988].14.

This class offers just one public method, which is supposed to be called
from GeometricModeler class.
*/
public class PolyhedralBoundedSolidSplitter extends PolyhedralBoundedSolidOperator
{
    /**
    Following variable `soov` ("set of ON-vertices") from program [MANT1988].14.1.
    */
    private static ArrayList<_PolyhedralBoundedSolidVertex> soov;

    /**
    Following variable `sone` ("set of null edges") from program [MANT1988].14.1.
    */
    private static ArrayList<_PolyhedralBoundedSolidSplitterNullEdge> sone;

    /**
    Following variable `sonf` ("set of null faces") from program [MANT1988].14.1.
    */
    private static ArrayList<_PolyhedralBoundedSolidFace> sonf;

    private static ArrayList<_PolyhedralBoundedSolidFace> facesToFixAbove;
    private static ArrayList<_PolyhedralBoundedSolidFace> facesToFixBelow;

    /**
    Following variable `ends` from program [MANT1988].14.9.
    */
    private static ArrayList<_PolyhedralBoundedSolidHalfEdge> ends;
    private static ArrayList<_PolyhedralBoundedSolidHalfEdge> tieds;

    /**
    Implements function `addsoov` from section [MANT1988].14.4. and program
    [MANT1988].14.2.
    */
    private static void addsoov(_PolyhedralBoundedSolidVertex v)
    {
        int i;

        for ( i = 0; i < soov.size(); i++ ) {
            if ( soov.get(i) == v ) {
                return;
            }
        }
        soov.add(v);
    }

    /**
    Implements solid splitting reduction step as indicated on sections
    [MANT1988].14.2.1 and [MANT1988].14.4 and program [MANT1988].14.2.

    This method is responsible for generating the set of coplanar
    vertices of `inSolid` (with respect to `inSplittingPlane`) and store
    them on `soov` for later usage.

    This method subdivides all edges of `inSolid` that intersects
    `inSplittingPlane` at their intersection points.
    */
    private static void splitGenerate(PolyhedralBoundedSolid inSolid,
                                      InfinitePlane inSplittingPlane)
    {
        _PolyhedralBoundedSolidEdge e;
        _PolyhedralBoundedSolidHalfEdge he;
        _PolyhedralBoundedSolidVertex v1, v2;
        Vector3D p;
        double d1, d2, t;
        int s1, s2;
        int i;

        soov = new ArrayList<_PolyhedralBoundedSolidVertex>();
        for ( i = 0; i < inSolid.edgesList.size(); i++ ) {
            e = inSolid.edgesList.get(i);
            v1 = e.rightHalf.startingVertex;
            v2 = e.leftHalf.startingVertex;
            d1 = inSplittingPlane.pointDistance(v1.position);
            d2 = inSplittingPlane.pointDistance(v2.position);
            s1 = PolyhedralBoundedSolid.compareValue(d1, 0.0, VSDK.EPSILON);
            s2 = PolyhedralBoundedSolid.compareValue(d2, 0.0, VSDK.EPSILON);
            if ( (s1 == -1 && s2 == 1) || (s1 == 1 && s2 == -1) ) {
                t = d1 / (d1 - d2);
                p = v1.position.add((v2.position.substract(v1.position)).multiply(t));
                he = e.leftHalf.next();
                inSolid.lmev(e.rightHalf, he, inSolid.getMaxVertexId()+1, p);
                addsoov(he.previous().startingVertex);
            }
            else {
                if ( s1 == 0 ) {
                    addsoov(v1);
                }
                if ( s2 == 0 ) {
                    addsoov(v2);
                }
            }
        }

        /*
        System.out.println("-----");
        for ( i = 0; i < soov.size(); i++ ) {
            System.out.println("  - Vertex [" + i + "]: " + soov.get(i));
        }
        System.out.println("-----");
        */
    }

    /**
    Current method is the first step for the initial classification of vertex
    neighborhood for `vtx`, as indicated on section [MANT1988].14.5.2. and
    program [MANT1988].14.4.

    Vitral SDK's implementation of this procedure extends the original from
    [MANT1988] by adding extra information flags to sector classifications
    `.isWide`, `.position` and `.situation`. Those flags are an additional
    aid for debugging purposes and specifically the `situation` flag will be
    later used on `splitClassify` to correct the ordering of sectors in order
    to keep consistency with Vitral SDK's interpretation of coordinate system.
    */
    private static ArrayList<_PolyhedralBoundedSolidSplitterSectorClassification> getNeighborhood(_PolyhedralBoundedSolidVertex vtx, InfinitePlane inSplittingPlane)
    {
        _PolyhedralBoundedSolidHalfEdge he;
        Vector3D bisect;
        double d;
        _PolyhedralBoundedSolidSplitterSectorClassification c;

        ArrayList<_PolyhedralBoundedSolidSplitterSectorClassification> neighborSectorsInfo;
        neighborSectorsInfo = new ArrayList<_PolyhedralBoundedSolidSplitterSectorClassification>();
        he = vtx.emanatingHalfEdge;

        do {
            c = new _PolyhedralBoundedSolidSplitterSectorClassification();
            c.sector = he;
            d = inSplittingPlane.pointDistance((he.next()).startingVertex.position);
            c.cl = PolyhedralBoundedSolid.compareValue(d, 0.0, VSDK.EPSILON);
            c.isWide = false;
            c.position = new Vector3D((he.next()).startingVertex.position);
            c.situation = _PolyhedralBoundedSolidSplitterSectorClassification.UNDEFINED;
            neighborSectorsInfo.add(c);
            if ( checkWideness(he) ) {
                bisect = bisector(he);
                c.situation = _PolyhedralBoundedSolidSplitterSectorClassification.CROSSING_EDGE;

                c = new _PolyhedralBoundedSolidSplitterSectorClassification();
                c.sector = he;
                d = inSplittingPlane.pointDistance(bisect);
                c.cl = PolyhedralBoundedSolid.compareValue(d, 0.0, VSDK.EPSILON);
                c.isWide = true;
                c.position = new Vector3D(bisect);
                c.situation = _PolyhedralBoundedSolidSplitterSectorClassification.CROSSING_EDGE;
                neighborSectorsInfo.add(c);
            }
            he = (he.mirrorHalfEdge()).next();
        } while ( he != vtx.emanatingHalfEdge );

        //-----------------------------------------------------------------
        // Extra pass, not from original [MANT1988] code
        int i;

        for ( i = 0; i < neighborSectorsInfo.size(); i++ ) {
            c = neighborSectorsInfo.get(i);
            if ( c.cl == _PolyhedralBoundedSolidSplitterSectorClassification.ON && c.situation == _PolyhedralBoundedSolidSplitterSectorClassification.UNDEFINED ) {
                c.situation = _PolyhedralBoundedSolidSplitterSectorClassification.INPLANE_EDGE;
            }
        }

        return neighborSectorsInfo;
    }

    private static boolean inplaneEdgesOn(
        ArrayList<_PolyhedralBoundedSolidSplitterSectorClassification> nbr)
    {
        int i;

        for ( i = 0; i < nbr.size(); i++ ) {
            if ( nbr.get(i).situation == _PolyhedralBoundedSolidSplitterSectorClassification.INPLANE_EDGE ) return true;
        }
        return false;
    }

    /**
    Current method applies the first reclassification rule presented at
    sections [MANT1988].14.5.1 and [MANT1988].14.5.2:
    For the given vertex neigborhood, classify each edge according to whether
    its final vertex lies above, on or below the `inSplittingPlane`. Tag
    the edge with the corresponding label ABOVE, ON or BELOW.
    Following program [MANT1988].14.5.
    */
    private static void reclassifyOnSectors(
        ArrayList<_PolyhedralBoundedSolidSplitterSectorClassification> nbr,
        InfinitePlane inSplittingPlane)
    {
        _PolyhedralBoundedSolidFace f;
        Vector3D c;
        double d;
        int i;
        _PolyhedralBoundedSolidSplitterSectorClassification l;

        for ( i = 0; i < nbr.size(); i++ ) {
            l = nbr.get(i);
            f = l.sector.parentLoop.parentFace;
            c = f.containingPlane.getNormal().crossProduct(inSplittingPlane.getNormal());
            d = c.dotProduct(c);
            if ( PolyhedralBoundedSolid.compareValue(d, 0.0, VSDK.EPSILON) == 0 ) {
                // Entering this means "faces are coplanar"
                d = f.containingPlane.getNormal().dotProduct(inSplittingPlane.getNormal());
                if ( PolyhedralBoundedSolid.compareValue(d, 0.0, VSDK.EPSILON) == 1 ) {
                    l.cl = _PolyhedralBoundedSolidSplitterSectorClassification.BELOW;
                    l.situation = _PolyhedralBoundedSolidSplitterSectorClassification.COPLANAR_FACE;
                    nbr.get((i+1)%nbr.size()).cl = _PolyhedralBoundedSolidSplitterSectorClassification.BELOW;
                }
                else {
                    l.cl = _PolyhedralBoundedSolidSplitterSectorClassification.ABOVE;
                    l.situation = _PolyhedralBoundedSolidSplitterSectorClassification.COPLANAR_FACE;
                    nbr.get((i+1)%nbr.size()).cl = _PolyhedralBoundedSolidSplitterSectorClassification.ABOVE;
                }
            }
        }
    }

    /**
    Current method applies the second reclassification rule presented at
    sections [MANT1988].14.5.1 and [MANT1988].14.5.2:
    After applying the first rule on method `reclassifyOnSectors`, ON edges
    may appear in only four kinds of consecutive arrangements. For each
    of the following arrangements, ON edge is reclassified as ABOVE or BELOW:
      - Sequence ABOVE/ON/ABOVE -> reclassified as BELOW
      - Sequence ABOVE/ON/BELOW -> reclassified as BELOW
      - Sequence BELOW/ON/BELOW -> reclassified as ABOVE
      - Sequence BELOW/ON/ABOVE -> reclassified as BELOW
    Those 4 rules are designed so that nonmanifold results will be represented
    as disconnected models.
    Following program [MANT1988].14.6.
    */
    private static void reclassifyOnEdges(ArrayList<_PolyhedralBoundedSolidSplitterSectorClassification> nbr)
    {
        _PolyhedralBoundedSolidSplitterSectorClassification l;
        int i;

        for ( i = 0; i < nbr.size(); i++ ) {
            l = nbr.get(i);
            if ( l.cl == _PolyhedralBoundedSolidSplitterSectorClassification.ON ) {
                if ( nbr.get((nbr.size()+i-1) % nbr.size()).cl == _PolyhedralBoundedSolidSplitterSectorClassification.BELOW ) {
                    if ( nbr.get((i+1) % nbr.size()).cl == _PolyhedralBoundedSolidSplitterSectorClassification.BELOW ) {
                        nbr.get(i).cl = _PolyhedralBoundedSolidSplitterSectorClassification.ABOVE;
                    }
                    else {
                        nbr.get(i).cl = _PolyhedralBoundedSolidSplitterSectorClassification.BELOW;
                    }
                }
                else {
                    nbr.get(i).cl = _PolyhedralBoundedSolidSplitterSectorClassification.BELOW;
                }
            }
        }
    }

    /**
    Following section [MANT1988].14,6,2 and program [MANT1988].14.7.
    Note, this code is horrible! YUCK! :P

    With respect to the original algorithm from [MANT1988], current
    implementation adds an extra check to ensure the orientation of the
    edges from below to above.
    */
    private static void insertNullEdges(
        ArrayList<_PolyhedralBoundedSolidSplitterSectorClassification> nbr,
        PolyhedralBoundedSolid inSolid, InfinitePlane inSplittingPlane)
    {
        int start, i;
        _PolyhedralBoundedSolidHalfEdge head, tail;
        _PolyhedralBoundedSolidSplitterSectorClassification n;
        int nnbr = nbr.size();

        if ( nnbr <= 0 ) return;
        n = nbr.get(0);

        //- Locate the head of an ABOVE-sequence --------------------------
        i = 0;
        while ( !( nbr.get(i).cl == _PolyhedralBoundedSolidSplitterSectorClassification.BELOW &&
                   nbr.get( (i+1)%nnbr ).cl == _PolyhedralBoundedSolidSplitterSectorClassification.ABOVE )  ) {
            i++;
            if ( i >= nnbr ) {
                return;
            }
        }
        start = i;
        head = nbr.get(i).sector;

        //-----------------------------------------------------------------
        while ( true ) {
            //- Locate the final sector of the sequence ------------------
            while ( !( nbr.get(i).cl == _PolyhedralBoundedSolidSplitterSectorClassification.ABOVE &&
                       nbr.get( (i+1)%nnbr ).cl == _PolyhedralBoundedSolidSplitterSectorClassification.BELOW ) ) {
                i = (i+1) % nnbr;
            }
            tail = nbr.get(i).sector;

            //- Insert null edge -----------------------------------------
            int d1;
            d1 = inSplittingPlane.doContainmentTestHalfSpace(head.next().startingVertex.position, VSDK.EPSILON);

            //System.out.println("LMEV:");
            if ( d1 != Geometry.OUTSIDE ) {
                //System.out.println("  - H1: " + tail);
                //System.out.println("  - H2: " + head);
                inSolid.lmev(tail, head, inSolid.getMaxVertexId()+1, head.startingVertex.position);
                sone.add(new _PolyhedralBoundedSolidSplitterNullEdge(tail.previous().parentEdge));
            }
            else {
                //System.out.println("  - H1: " + head);
                //System.out.println("  - H2: " + tail);
                inSolid.lmev(head, tail, inSolid.getMaxVertexId()+1, head.startingVertex.position);
                sone.add(new _PolyhedralBoundedSolidSplitterNullEdge(head.previous().parentEdge));
            }

            //- Locate the start of the next sequence --------------------
            while ( !( nbr.get(i).cl == _PolyhedralBoundedSolidSplitterSectorClassification.BELOW &&
                       nbr.get( (i+1) % nnbr ).cl == _PolyhedralBoundedSolidSplitterSectorClassification.ABOVE ) ) {
                i = (i+1) % nnbr;
                if ( i == start ) {
                    return;
                }
            }
        }
    }

    /**
    Vertex neighborhood classifier, as presented in section [MANT1988].14.5,
    and program 14.3.

    It appears that original algorithm from [MANT1988] assumes a left handed
    geometry or orientation or other difference to current Vitral SDK
    implementation of the boundary representation. That difference implies
    a reverse order in some cases, so `inplaneEdgesOn` check is added here
    to keep current implementation's consistency.
    */
    private static void splitClassify(PolyhedralBoundedSolid inSolid, InfinitePlane inSplittingPlane)
    {
        int i;

        sone = new ArrayList<_PolyhedralBoundedSolidSplitterNullEdge>();

        /// Following variable `nbr` from program [MANT1988].14.3.
        ArrayList<_PolyhedralBoundedSolidSplitterSectorClassification> nbr;

        for ( i = 0; i < soov.size(); i++ ) {
            nbr = getNeighborhood(soov.get(i), inSplittingPlane);
            if ( inplaneEdgesOn(nbr) ) {
                Collections.reverse(nbr);
            }
            reclassifyOnSectors(nbr, inSplittingPlane);
            reclassifyOnEdges(nbr);
            insertNullEdges(nbr, inSolid, inSplittingPlane);
        }
    }

    /**
    Following section [MANT1988].14.7.2. and program [MANT1988].14.9.
    */
    private static _PolyhedralBoundedSolidHalfEdge
    canJoin(_PolyhedralBoundedSolidHalfEdge he)
    {
        _PolyhedralBoundedSolidHalfEdge ret;
        int i;

        for ( i = 0; i < ends.size(); i++ ) {
            if ( neighbor(he, ends.get(i)) ) {
                ret = ends.get(i);
                ends.remove(i);
                tieds.add(ret);
                return ret;
            }
        }
        ends.add(he);
        return null;
    }

    private static void printNbr(ArrayList<_PolyhedralBoundedSolidSplitterSectorClassification> neighborSectorsInfo)
    {
        int i;

        for ( i = 0; i < neighborSectorsInfo.size(); i++ ) {
            System.out.println("  - " + neighborSectorsInfo.get(i));
        }
    }

    private static void printEnds()
    {
        int i;

        for ( i = 0; i < ends.size(); i++ ) {
            System.out.println("  - ends[" + i + "]: " + ends.get(i));
        }
    }

    /**
    Following section [MANT1988].14.7.2. and program [MANT1988].14.10.
    */
    private static void cut(_PolyhedralBoundedSolidHalfEdge he)
    {
        PolyhedralBoundedSolid s;

        s = he.parentLoop.parentFace.parentSolid;

        if ( he.parentEdge.rightHalf.parentLoop ==
             he.parentEdge.leftHalf.parentLoop ) {
            sonf.add(he.parentLoop.parentFace);
            s.lkemr(he.parentEdge.rightHalf, he.parentEdge.leftHalf);
        }
        else {
            s.lkef(he.parentEdge.rightHalf, he.parentEdge.leftHalf);
        }
    }

    /**
    */
    private static boolean isLoose(_PolyhedralBoundedSolidHalfEdge he)
    {
        int i;

        for ( i = 0; i < tieds.size(); i++ ) {
            if ( he == tieds.get(i) ) return false;
        }

        return true;
    }

    /**
    Following section [MANT1988].14.7.2. and program [MANT1988].14.9.
    */
    private static void splitConnect()
    {        
        int i;

        ends = new ArrayList<_PolyhedralBoundedSolidHalfEdge>();
        tieds = new ArrayList<_PolyhedralBoundedSolidHalfEdge>();

        //-----------------------------------------------------------------
        _PolyhedralBoundedSolidEdge nextedge;
        _PolyhedralBoundedSolidHalfEdge h1, h2;

        sonf = new ArrayList<_PolyhedralBoundedSolidFace>();

        Collections.sort(sone);

        //System.out.println(sone.get(0).e.rightHalf.parentLoop.parentFace.parentSolid);

        for ( i = 0; i < sone.size(); i++ ) {
            //System.out.println("- " + i + " ---------------------------------------------------------------------");
            //System.out.println(" - " + sone.get(i).e + " / " + sone.get(i).e.rightHalf.startingVertex.position);

            nextedge = sone.get(i).e;
            //System.out.println("    . edge.rightHalf: " + nextedge.rightHalf);
            h1 = canJoin(nextedge.rightHalf);

            //System.out.println("    . h1: " + h1);

            if ( h1 != null ) {
                //System.out.println("    . -> JOIN H1");
                join(h1, nextedge.rightHalf, false);
                tieds.add(nextedge.rightHalf);
                if ( !isLoose(h1.mirrorHalfEdge()) ) {
                    //System.out.println("    . -> CUT H1");
                    cut(h1);
                }
            }
            //System.out.println("    . edge.leftHalf: " + nextedge.leftHalf);
            h2 = canJoin(nextedge.leftHalf);

            //System.out.println("    . h2: " + h2);

            if ( h2 != null ) {
                //System.out.println("    . -> JOIN H2");
                join(h2, nextedge.leftHalf, false);
                tieds.add(nextedge.leftHalf);
                if ( !isLoose(h2.mirrorHalfEdge()) ) {
                    //System.out.println("    . -> CUT H2");
                    cut(h2);
                }
            }
            if ( h1 != null && h2 != null ) {
                //System.out.println("    . -> CUT DUAL");
                cut(nextedge.rightHalf);
            }

            //printEnds();
        }
    }

    /**
    Following section [MANT1988].14.8. and program [MANT1988].14.12.
    */
    private static void classify(PolyhedralBoundedSolid S,
                                 PolyhedralBoundedSolid Above,
                                 PolyhedralBoundedSolid Below)
    {
        int i;
        facesToFixAbove = new ArrayList<_PolyhedralBoundedSolidFace>();
        facesToFixBelow = new ArrayList<_PolyhedralBoundedSolidFace>();

        for ( i = 0; i < sonf.size()/2; i++ ) {
            movefac(sonf.get(i), Above);
            facesToFixAbove.add(sonf.get(i));
            movefac(sonf.get(i+sonf.size()/2), Below);
            facesToFixBelow.add(sonf.get(i+sonf.size()/2));
        }
    }

    private static boolean isNullFace(_PolyhedralBoundedSolidFace f)
    {
        int i;

        for ( i = 0; i < sonf.size(); i++ ) {
            if ( sonf.get(i) == f ) return true;
        }
        return false;
    }

    /**
    */
    private static void destroy(PolyhedralBoundedSolid inSolid)
    {
        inSolid.polygonsList =
            new CircularDoubleLinkedList<_PolyhedralBoundedSolidFace>();
        inSolid.edgesList =
            new CircularDoubleLinkedList<_PolyhedralBoundedSolidEdge>();
        inSolid.verticesList =
            new CircularDoubleLinkedList<_PolyhedralBoundedSolidVertex>();
    }

    /**
    A face `a` is "inside" other `b` (and should be an internal loop) if
    all vertices from `b` are inside the polygon of `a`.

    PRE: given faces are coplanar.
    */
    private static boolean faceInsideFace(
        _PolyhedralBoundedSolidFace a, _PolyhedralBoundedSolidFace b)
    {
        int i;
        _PolyhedralBoundedSolidLoop l;
        _PolyhedralBoundedSolidHalfEdge he, heStart;

        a.calculatePlane();
        for ( i = 0; i < b.boundariesList.size(); i++ ) {
            heStart = b.boundariesList.get(i).boundaryStartHalfEdge;
            he = heStart;
            do {
                if ( a.testPointInside(he.startingVertex.position, VSDK.EPSILON) == Geometry.OUTSIDE ) {
                    return false;
                }
                he = he.next();
            } while ( he != heStart );
        }
        return true;
    }

    /**
    */
    private static void fixNullFaces(ArrayList<_PolyhedralBoundedSolidFace> l)
    {
        if ( l.size() == 1 ) {
            l.remove(0);
            return;
        }

        int i, j;

        for ( i = 0; i < l.size(); i++ ) {
            for ( j = 0; j < l.size(); j++ ) {
                if ( i == j ) continue;
                if ( faceInsideFace(l.get(j), l.get(i) ) ) {
                    l.get(i).parentSolid.lkfmrh(l.get(i), l.get(j));
                    l.remove(j);
                    // Repeat he process with remaining list
                    fixNullFaces(l);
                    return;
                }
            }
        }
    }

    /**
    Following section [MANT1988].14.8. and program [MANT1988].14.11.
    */
    private static void splitFinish(PolyhedralBoundedSolid inSolid,
                             ArrayList<PolyhedralBoundedSolid> outSolidsAbove,
                             ArrayList<PolyhedralBoundedSolid> outSolidsBelow)
    {
        int i;
        int firstHalfSize;
        _PolyhedralBoundedSolidFace newface;
        PolyhedralBoundedSolid newAbove, newBelow;

        firstHalfSize = sonf.size();
        for ( i = 0; i < firstHalfSize; i++ ) {
            newface = inSolid.lmfkrh(sonf.get(i).boundariesList.get(1), inSolid.getMaxFaceId()+1);
            sonf.add(newface);
        }
        newAbove = new PolyhedralBoundedSolid();
        newBelow = new PolyhedralBoundedSolid();
        classify(inSolid, newAbove, newBelow);
        fixNullFaces(facesToFixAbove);
        fixNullFaces(facesToFixBelow);
        cleanup(newAbove);
        newAbove.validateModel();
        cleanup(newBelow);
        newBelow.validateModel();
        outSolidsAbove.add(newAbove);
        outSolidsBelow.add(newBelow);
        destroy(inSolid);
    }

    /**
    Given the input `inSolid` and the cutting plane `inSplittingPlane`,
    this method appends to the `outSolidsAbove` list the solids resulting
    from cutting the solid with the plane and resulting above the plane,
    similarly, `outSolidsBelow` will be appended with solid pieces
    resulting below the plane.

    Current macro-algorithm follows the strategy outlined on sections
    [MANT1988].14.1, [MANT1988].14.2 and [MANT1988].14.3 and program
    [MANT1988].14.1.
    */
    public static void split(
                      PolyhedralBoundedSolid inSolid,
                      InfinitePlane inSplittingPlane,
                      ArrayList<PolyhedralBoundedSolid> outSolidsAbove,
                      ArrayList<PolyhedralBoundedSolid> outSolidsBelow)
    {
        //-----------------------------------------------------------------
        inSolid.validateModel();
        splitGenerate(inSolid, inSplittingPlane);
        splitClassify(inSolid, inSplittingPlane);

        if ( sone.size() <= 0 ) {
            // Plane should be tested here before asuming this order!
            outSolidsAbove.add(inSolid);
            outSolidsBelow.add(new PolyhedralBoundedSolid());
            return;
        }

        splitConnect();
        splitFinish(inSolid, outSolidsAbove, outSolidsBelow);

        //-----------------------------------------------------------------
        soov = null;
        sone = null;
        sonf = null;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
