//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - April 1 2008 - Oscar Chavarro: Original base version                  =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [MANT1986] Mantyla Martti. "Boolean Operations of 2-Manifolds through   =
//=     Vertex Neighborhood Classification". ACM Transactions on Graphics,  =
//=     Vol. 5, No. 1, January 1986, pp. 1-29.                              =
//= [MANT1988] Mantyla Martti. "An Introduction To Solid Modeling",         =
//=     Computer Science Press, 1988.                                       =
//= [.wMANT2008] Mantyla Martti. "Personal Home Page", <<shar>> archive     =
//=     containing the C programs from [MANT1988]. Available at             =
//=     http://www.cs.hut.fi/~mam . Last visited April 12 / 2008.           =
//===========================================================================

package vsdk.toolkit.processing;

// Java classes
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

// VitralSDK classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.InfinitePlane;
import vsdk.toolkit.environment.geometry.PolyhedralBoundedSolid;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidFace;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidEdge;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidHalfEdge;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidVertex;
import vsdk.toolkit.render.PolyhedralBoundedSolidDebugger;
import vsdk.toolkit.io.PersistenceElement;

/**
Class `_PolyhedralBoundedSolidSplitterNullEdge` plays a role of a decorator
design patern for class `_PolyhedralBoundedSolidEdge`, and adds sort-ability.
*/
class _PolyhedralBoundedSolidSetOperatorNullEdge 
    extends PolyhedralBoundedSolidOperator 
    implements Comparable <_PolyhedralBoundedSolidSetOperatorNullEdge>
{
    public _PolyhedralBoundedSolidEdge e;

    public _PolyhedralBoundedSolidSetOperatorNullEdge(_PolyhedralBoundedSolidEdge e)
    {
        this.e = e;
    }

    public int compareTo(_PolyhedralBoundedSolidSetOperatorNullEdge other)
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

    @Override
    public String toString()
    {
        return e.toString() + " (sorted with respect to position " + this.e.rightHalf.startingVertex.position + ")";
    }

}

/**
This class is used to store vertex / halfedge neigborhood information for the
vertex/vertex classifier as proposed on section [MANT1988].15.5. and program
[MANT1988].15.6.
*/
class _PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex extends PolyhedralBoundedSolidOperator implements Comparable <_PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex>
{
    public _PolyhedralBoundedSolidHalfEdge he;
    public Vector3D ref1;
    public Vector3D ref2;
    public Vector3D ref12;
    public Vector3D referenceLine;
    public Vector3D referenceU;
    public Vector3D referenceV;
    public boolean wide;

    public _PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex()
    {
        referenceLine = null;
        referenceU = null;
        referenceV = null;
    }

    public double getAngle()
    {
        if ( referenceLine == null || referenceU == null || referenceV == null ) {
            return -1000;
        }

        double x, y;
        double an;
        Vector3D a = ref1;

        if ( PolyhedralBoundedSolidSetOperator.colinearVectorsWithDirection(ref1, referenceLine) ) {
            a = ref2;
        }

        Vector3D u, v;

        u = new Vector3D(referenceU);
        u.normalize();
        v = new Vector3D(referenceV);
        v.normalize();
        a.normalize();

        x = a.dotProduct(u);
        y = a.dotProduct(v);

        an = Math.acos(x);
        if ( y < 0 ) an *= -1;

        return an;
    }

    public String toString()
    {
        String msg;

        msg = "R1: " + ref1 + " R2: " + ref2 + " HE " + he.startingVertex.id + "/" + he.next().startingVertex.id + (wide?"(W)":"(nw)");

        return msg;
    }

    public int compareTo(_PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex other)
    {
        double a, b;

        a = this.getAngle();
        b = other.getAngle();

        if ( a > b) return 1;
        if ( a < b) return -1;
        return 0;
    }
}

/**
This class is used to store sector / sector neigborhood information for the
vertex/vertex classifier as proposed on section [MANT1988].15.5. and program
[MANT1988].15.6.
*/
class _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector extends PolyhedralBoundedSolidOperator
{
    public int secta;
    public int sectb;
    public int s1a;
    public int s2a;
    public int s1b;
    public int s2b;
    public boolean intersect;
    public _PolyhedralBoundedSolidHalfEdge hea;
    public _PolyhedralBoundedSolidHalfEdge heb;
    public boolean wa;
    public boolean wb;
    public static final int ON = 0;
    public static final int OUT = 1;
    public static final int IN = -1;

    private String label(int i)
    {
        String msg = "<Unknown>";
        switch ( i ) {
          case ON: msg = "on"; break;
          case OUT: msg = "OUT"; break;
          case IN: msg = "IN"; break;
        }
        return msg;
    }

    public void fillCases()
    {
        if ( s1a == ON ) {
            switch ( s2a ) {
            case IN: s1a = OUT; break;
            case OUT: s1a = IN; break;
            }
        }
        if ( s2a == ON ) {
            switch ( s1a ) {
            case IN: s2a = OUT; break;
            case OUT: s2a = IN; break;
            }
        }
        if ( s1b == ON ) {
            switch ( s2b ) {
            case IN: s1b = OUT; break;
            case OUT: s1b = IN; break;
            }
        }
        if ( s2b == ON ) {
            switch ( s1b ) {
            case IN: s2b = OUT; break;
            case OUT: s2b = IN; break;
            }
        }
    }

    public String toString()
    {
        String msg = "Sector pair ";

        msg = msg + "A[" + (secta+1) + "] / B[" + (sectb+1) + "]: ";

        msg = msg + "VERTICES ( " + 
            hea.startingVertex.id + "-" + 
            (hea.next()).startingVertex.id + (wa?"(W)":"(nw)") + " / " + 
            heb.startingVertex.id + "-" +
            (heb.next()).startingVertex.id + (wb?"(W)":"(nw)") + " ) - ";
        msg = msg + "[" + label(s1a) + "/" + label(s2a) + ", " + label(s1b) + "/" + label(s2b) + "] ";
        if ( intersect ) {
            msg = msg + "intersecting";
        }
        else {
            msg = msg + "(droped)";
        }

        if ( s1a != 0 && s1b != 0 && s2a != 0 && s2b != 0 && intersect ) {
            msg += " (**) ";
        }

        return msg;
    }
}

/**
This class is used to store vertex / halfedge neigborhood information for the
vertex/face classifier, in a similar fashion to as presented in section
[MANT1988].14.5, and program [MANT1988].14.3., but biased for the set
operation algorithm as proposed on section [MANT1988].15..1. and problem
[MANT1988].15.4.
*/
class _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace extends PolyhedralBoundedSolidOperator
{
    public static final int ABOVE = 1;
    public static final int BELOW = -1;
    public static final int ON = 0;

    public static final int AinB = 11;
    public static final int AoutB = 12;
    public static final int BinA = 13;
    public static final int BoutA = 14;
    public static final int AonBplus = 15;
    public static final int AonBminus = 16;
    public static final int BonAplus = 17;
    public static final int BonAminus = 18;

    public static final int COPLANAR_FACE = 10;
    public static final int INPLANE_EDGE = 20;
    public static final int CROSSING_EDGE = 30;
    public static final int UNDEFINED = 40;

    public _PolyhedralBoundedSolidHalfEdge sector;
    public InfinitePlane referencePlane;
    public int cl;

    // Following attributes are not taken from [MANT1988], and all operations
    // on them are fine tunning options aditional to original algorithm.
    public boolean isWide = false;
    public Vector3D position;
    public int situation = UNDEFINED;
    public boolean reverse = false;

    public _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace()
    {
        ;
    }

    public _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace(_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace other)
    {
        this.sector = other.sector;
        this.referencePlane = other.referencePlane;
        this.cl = other.cl;
        this.isWide = other.isWide;
        this.position = other.position;
        this.situation = other.situation;    
        this.reverse = other.reverse;
    }

    /**
    Current method implements the set of changes from table [MANT1988].15.3.
    for the edge reclassification rules for the third stage of a vertex/face
    classifier.
    */
    public void applyRules(int op)
    {
        if ( op == UNION ) {
            switch ( cl ) {
              case AonBplus:     cl = AoutB;    break;
              case AonBminus:    cl = AinB;    break;
              case BonAplus:     cl = BinA;    break;
              case BonAminus:    cl = BinA;    break;
            }
        }
        else if ( op == INTERSECTION ) {
            switch ( cl ) {
              case AonBplus:     cl = AinB;    break;
              case AonBminus:    cl = AoutB;    break;
              case BonAplus:     cl = BoutA;    break;
              case BonAminus:    cl = BoutA;    break;
            }
        }
        else if ( op == DIFFERENCE ) {
            switch ( cl ) {
              case AonBplus:     cl = AinB;    break;
              case AonBminus:    cl = AoutB;    break;
              case BonAplus:     cl = BoutA;    break;
              case BonAminus:    cl = BoutA;    break;
            }
        }
    }

    public void updateLabel(int BvsA)
    {
        InfinitePlane a = sector.parentLoop.parentFace.containingPlane;
        InfinitePlane b = referencePlane;

        if ( BvsA == 0 ) {
            switch ( cl ) {
              case ABOVE: cl = AoutB; break;
              case BELOW: cl = AinB; break;
              case ON:
                if ( a.overlapsWith(b, VSDK.EPSILON) ) {
                    cl = AonBplus;
                }
                else {
                    cl = AonBminus;
                }
                break;
            }
        }
        else {
            switch ( cl ) {
              case ABOVE: cl = BoutA; break;
              case BELOW: cl = BinA; break;
              case ON:
                if ( a.overlapsWith(b, VSDK.EPSILON) ) {
                    cl = BonAplus;
                }
                else {
                    cl = BonAminus;
                }
                break;
            }
        }
    }

    public String toString()
    {
        String msg = "Sector(";
        msg = msg + sector + " | ";
        switch ( cl ) {
          case ABOVE: msg = msg + " ABOVE"; break;
          case BELOW: msg = msg + " BELOW"; break;
          case ON: msg = msg + " ON"; break;
          case AinB: msg = msg + "AinB"; break;
          case AoutB: msg = msg + "AoutB"; break;
          case BinA: msg = msg + "BinA"; break;
          case BoutA: msg = msg + "BoutA"; break;
          case AonBplus: msg = msg + "AonBplus"; break;
          case AonBminus: msg = msg + "AonBminus"; break;
          case BonAplus: msg = msg + "BonAplus"; break;
          case BonAminus: msg = msg + "BonAminus"; break;
          default: msg = msg + "<INVALID!>"; break;
        }
        msg = msg + " ";
        if ( isWide ) {
            msg = msg + "(W) ";
        }
        //msg = msg + ", pos: " + position;

        switch ( situation ) {
          case COPLANAR_FACE: msg = msg + "<COPLANAR_FACE>"; break;
          case INPLANE_EDGE: msg = msg + "<INPLANE_EDGE>"; break;
          case CROSSING_EDGE: msg = msg + "<CROSSING_EDGE>"; break;
          default: msg = msg + "<UNDEFINED>"; break;
        }

        msg = msg + ")";
        return msg;
    }
}

class _PolyhedralBoundedSolidSetOperatorVertexVertex extends PolyhedralBoundedSolidOperator
{
    public _PolyhedralBoundedSolidVertex va;
    public _PolyhedralBoundedSolidVertex vb;

    public String toString() {
        String msg = "(" + va + ") / (" + vb + "}";
        return msg;
    }
}

class _PolyhedralBoundedSolidSetOperatorVertexFace extends PolyhedralBoundedSolidOperator
{
    public _PolyhedralBoundedSolidVertex v;
    public _PolyhedralBoundedSolidFace f;

    public String toString() {
        String msg = "{" + v + " / " + f + "}";
        return msg;
    }
}

/**
This class encapsulates the set operations algorithms for boundary
representation solids in VitralSDK. Basically, this class implements the
original algorithm published in the paper [MANT1986] and in the second
part of the book [MANT1988].
The algorithm is structured in 5 big phases:
  0. Calculate vertex/face and vertex/vertex crossings.
  1. Classify and split for vertex/face cases.
  2. Classify and split for vertex/vertex cases.
  3. Connect.
  4. Finish.
Note that each big phase is controlled in a method (mark as "big phase" in
its documentation).
*/
public class PolyhedralBoundedSolidSetOperator extends PolyhedralBoundedSolidOperator
{
    /**
    Debug flags.
    */
    private static final int DEBUG_01_STRUCTURE = 0x01;
    private static final int DEBUG_02_GENERATOR = 0x02;
    private static final int DEBUG_03_VERTEXFACECLASIFFIER = 0x04;
    private static final int DEBUG_04_VERTEXVERTEXCLASIFFIER = 0x08;
    private static final int DEBUG_05_CONNECT = 0x10;
    private static final int DEBUG_06_FINISH = 0x20;
    private static final int DEBUG_99_SHOWOPERATIONS = 0x20;

    /**
    The integer `debugFlags` is a bitwise combination of debugging flags
    used to control debug messages printed on the standard output.
    */
    private static int debugFlags = 0;

    /**
    Used for exporting internal state in graphical form.
    */
    private static PolyhedralBoundedSolidDebugger offlineRenderer = null;

    /**
    Following variable `sonvv` from program [MANT1988].15.1.
    */
    private static ArrayList<_PolyhedralBoundedSolidSetOperatorVertexVertex> sonvv;

    /**
    Following variable `sonva` from program [MANT1988].15.1.
    */
    private static ArrayList<_PolyhedralBoundedSolidSetOperatorVertexFace> sonva;

    /**
    Following variable `sonvb` from program [MANT1988].15.1.
    */
    private static ArrayList<_PolyhedralBoundedSolidSetOperatorVertexFace> sonvb;

    /**
    Following variable `sonea` from program [MANT1988].15.1.
    */
    private static ArrayList<_PolyhedralBoundedSolidSetOperatorNullEdge> sonea;

    /**
    Following variable `soneb` from program [MANT1988].15.1.
    */
    private static ArrayList<_PolyhedralBoundedSolidSetOperatorNullEdge> soneb;

    /**
    Following variable `sonfa` from program [MANT1988].15.1.
    */
    private static ArrayList<_PolyhedralBoundedSolidFace> sonfa;

    /**
    Following variable `sonfb` from program [MANT1988].15.1.
    */
    private static ArrayList<_PolyhedralBoundedSolidFace> sonfb;

    /**
    Following variable `nba` from program [MANT1988].15.6.
    */
    private static ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex> nba;

    /**
    Following variable `nba` from program [MANT1988].15.6.
    */
    private static ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex> nbb;

    /**
    Following variable `sectors` from program [MANT1988].15.6.
    */
    private static ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector> sectors;

    /**
    Following variable `endsa` from program [MANT1988].15.13.
    */
    private static ArrayList<_PolyhedralBoundedSolidHalfEdge> endsa;

    /**
    Following variable `endsb` from program [MANT1988].15.13.
    */
    private static ArrayList<_PolyhedralBoundedSolidHalfEdge> endsb;

    /**
    Procedure `updmaxnames` functionality is described on section
    [MANT1988].15.4. This method increments the face and vertex
    identifiers of `solidToUpdate` so that they do not overlap with
    `referenceSolid` identifiers.
    */
    public static void updmaxnames(PolyhedralBoundedSolid solidToUpdate,
                                   PolyhedralBoundedSolid referenceSolid)
    {
        _PolyhedralBoundedSolidVertex v;
        _PolyhedralBoundedSolidFace f;
        int i;

        for ( i = 0; i < solidToUpdate.verticesList.size(); i++ ) {
            v = solidToUpdate.verticesList.get(i);
            v.id += referenceSolid.getMaxVertexId();
            if ( v.id > solidToUpdate.maxVertexId ) {
                solidToUpdate.maxVertexId = v.id;
            }
        }

        for ( i = 0; i < solidToUpdate.polygonsList.size(); i++ ) {
            f = solidToUpdate.polygonsList.get(i);
            f.id += referenceSolid.getMaxFaceId();
            if ( f.id > solidToUpdate.maxFaceId ) {
                solidToUpdate.maxFaceId = f.id;
            }
        }
    }

    /**
    */
    private static int nextVertexId(PolyhedralBoundedSolid current,
                             PolyhedralBoundedSolid other)
    {
        int a, b, m;

        a = current.getMaxVertexId();
        b = other.getMaxVertexId();
        m = a;
        if ( b > a ) {
            m = b;
        }

        return m+1;
    }

    /**
    */
    private static void addsovf(_PolyhedralBoundedSolidHalfEdge he,
                                _PolyhedralBoundedSolidFace f, int BvsA)
    {
        //-----------------------------------------------------------------
        _PolyhedralBoundedSolidSetOperatorVertexFace elem;
        ArrayList<_PolyhedralBoundedSolidSetOperatorVertexFace> sonv;

        if ( BvsA == 0 ) {
            sonv = sonva;
        }
        else {
            sonv = sonvb;
        }

        int i;

        for ( i = 0; i < sonv.size(); i++ ) {
            elem = sonv.get(i);
            if ( elem.v == he.startingVertex && elem.f == f ) {
                return;
            }
        }

        //-----------------------------------------------------------------
        elem = new _PolyhedralBoundedSolidSetOperatorVertexFace();
        elem.v = he.startingVertex;
        elem.f = f;
        sonv.add(elem);
    }

    /**
    */
    private static void addsovv(_PolyhedralBoundedSolidVertex a,
                                _PolyhedralBoundedSolidVertex b, int BvsA)
    {
        //-----------------------------------------------------------------
        _PolyhedralBoundedSolidSetOperatorVertexVertex elem;
        int i;

        for ( i = 0; i < sonvv.size(); i++ ) {
            elem = sonvv.get(i);
            if ( BvsA == 0 && elem.va == a && elem.vb == b ||
                 BvsA != 0 && elem.va == b && elem.vb == a ) {
                return;
            }
        }

        //-----------------------------------------------------------------
        elem = new _PolyhedralBoundedSolidSetOperatorVertexVertex();
        if ( BvsA == 0 ) {
            elem.va = a;
            elem.vb = b;
        }
        else {
            elem.va = b;
            elem.vb = a;
        }
        sonvv.add(elem);
    }

    /**
    Following [MANT1988].15.4.
    */
    private static void doVertexOnFace(
        _PolyhedralBoundedSolidVertex v,
        _PolyhedralBoundedSolidFace f,
        int BvsA,
        PolyhedralBoundedSolid current,
        PolyhedralBoundedSolid other)
    {
        int cont;
        double d;

        d = f.containingPlane.pointDistance(v.position);
        if ( PolyhedralBoundedSolid.compareValue(d, 0.0, VSDK.EPSILON) == 0 ) {
            cont = cont = f.testPointInside(v.position, VSDK.EPSILON);
            if ( cont == Geometry.INSIDE ) {
                addsovf(v.emanatingHalfEdge, f, BvsA);
            }
            else if ( cont == Geometry.LIMIT &&
                      f.lastIntersectedHalfedge != null ) {
                current.lmev(
                    f.lastIntersectedHalfedge,
                    f.lastIntersectedHalfedge.mirrorHalfEdge().next(),
                    nextVertexId(current, other), v.position);
                addsovv(v, f.lastIntersectedHalfedge.startingVertex, BvsA);
            }
            else if ( cont == Geometry.LIMIT &&
                      f.lastIntersectedVertex != null ) {
                addsovv(v, f.lastIntersectedVertex, BvsA);
            }
        }
    }

    /**
    Following program [MANT1988].15.3.
    */
    private static void doSetOpGenerate(
        _PolyhedralBoundedSolidEdge e,
        _PolyhedralBoundedSolidFace f,
        int BvsA,
        PolyhedralBoundedSolid current,
        PolyhedralBoundedSolid other)
    {
        _PolyhedralBoundedSolidVertex v1, v2;
        double d1, d2, d3, t;
        Vector3D p;
        int s1, s2, cont;

        v1 = e.rightHalf.startingVertex;
        v2 = e.leftHalf.startingVertex;
        d1 = f.containingPlane.pointDistance(v1.position);
        d2 = f.containingPlane.pointDistance(v2.position);
        s1 = PolyhedralBoundedSolid.compareValue(d1, 0.0, VSDK.EPSILON);
        s2 = PolyhedralBoundedSolid.compareValue(d2, 0.0, VSDK.EPSILON);

        if ( (s1 == -1 && s2 == 1) || (s1 == 1 && s2 == -1) ) {
            t = d1 / (d1 - d2);
            p = v1.position.add(
                    (v2.position.substract(
                         v1.position)).multiply(t));

            d3 = f.containingPlane.pointDistance(p);
            if ( PolyhedralBoundedSolid.compareValue(d3, 0.0, VSDK.EPSILON) == 0 ) {
                cont = f.testPointInside(p, VSDK.EPSILON);

                if ( cont != Geometry.OUTSIDE ) {
                    current.lmev(e.rightHalf, e.leftHalf.next(),
                                       nextVertexId(current, other), p);

                    if ( cont == Geometry.INSIDE ) {
                        // Reduction step phase 5/6: Edge crosses inside a face
                        // No subdivide?
                        // Reduction step phase 7/8: stop vertex/face
                        addsovf(e.rightHalf, f, BvsA);
                    }
                    else if ( cont == Geometry.LIMIT &&
                              f.lastIntersectedHalfedge != null ) {
                        // Reduction step phase 1: Edge crosses other edge
                        // Subdivide both edges (here one of them, the other
                        // is this same code but when called from other solid),
                        // at their intersection point (`p`), i.e. replace
                        // each edge by two edges and a new vertex lying at `p`.
                        current.lmev(f.lastIntersectedHalfedge,
                            f.lastIntersectedHalfedge.mirrorHalfEdge().next(),
                                     nextVertexId(current, other), p);
                        // Reduction step phase 4: store vertex/vertex
                        addsovv(e.rightHalf.startingVertex, f.lastIntersectedHalfedge.startingVertex, BvsA);
                    }
                    else if ( cont == Geometry.LIMIT &&
                              f.lastIntersectedVertex != null ) {
                        // Reduction step phase 2/3: Edge touches vertex
                        // No subdivide?
                        // Reduction step phase 4: store vertex/vertex
                        addsovv(e.rightHalf.startingVertex, f.lastIntersectedVertex, BvsA);

                    }
                    processEdge(e.rightHalf.previous().parentEdge, current, BvsA, other);
                }

            }
        }
        else {
            if ( s1 == 0 ) {
                doVertexOnFace(v1, f, BvsA, current, other);
            }
            if ( s2 == 0 ) {
                doVertexOnFace(v2, f, BvsA, current, other);
            }
        }

    }

    /**
    Following program [MANT1988].15.2.
    */
    private static void processEdge(_PolyhedralBoundedSolidEdge e,
                                    PolyhedralBoundedSolid s,
                                    int BvsA,
                                    PolyhedralBoundedSolid other)
    {
        _PolyhedralBoundedSolidFace f;
        int i;

        for ( i = 0; i < s.polygonsList.size(); i++ ) {
            f = s.polygonsList.get(i);
            doSetOpGenerate(e, f, BvsA, s, other);
        }
    }

    /**
    Initial vertex intersection detector for the set operations algorithm
    (big phase 0).
    Following program [MANT1988].15.2.
    */
    private static void setOpGenerate(PolyhedralBoundedSolid inSolidA,
                                      PolyhedralBoundedSolid inSolidB)
    {
        _PolyhedralBoundedSolidEdge e;

        sonvv = new ArrayList<_PolyhedralBoundedSolidSetOperatorVertexVertex>();
        sonva = new ArrayList<_PolyhedralBoundedSolidSetOperatorVertexFace>();
        sonvb = new ArrayList<_PolyhedralBoundedSolidSetOperatorVertexFace>();

        int i;

        for ( i = 0; i < inSolidA.edgesList.size(); i++ ) {
            e = inSolidA.edgesList.get(i);
            processEdge(e, inSolidB, 0, inSolidA);
        }
        for ( i = 0; i < inSolidB.edgesList.size(); i++ ) {
            e = inSolidB.edgesList.get(i);
            processEdge(e, inSolidA, 1, inSolidB);
        }
    }

    /**
    Current method is the first step for the initial vertex/face classification
    of sectors (vertex neighborhood) for `vtx`, as indicated on section
    [MANT1988].14.5.2. and program [MANT1988].14.4., but biased towards the
    set operator classifier, as proposed on section [MANT1988].15.6.1. and
    problem [MANT1988].15.4.

    Vitral SDK's implementation of this procedure extends the original from
    [MANT1988] by adding extra information flags to sector classifications
    `.isWide`, `.position` and `.situation`. Those flags are an additional
    aid for debugging purposes and specifically the `situation` flag will be
    later used on `splitClassify` to correct the ordering of sectors in order
    to keep consistency with Vitral SDK's interpretation of coordinate system.
    */
    private static
    ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace>
    vertexFaceGetNeighborhood(
        _PolyhedralBoundedSolidVertex vtx,
        InfinitePlane referencePlane,
        int BvsA)
    {
        _PolyhedralBoundedSolidHalfEdge he;
        Vector3D bisect;
        double d;
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace c;

        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> neighborSectorsInfo;
        neighborSectorsInfo = new ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace>();

        he = vtx.emanatingHalfEdge;
        do {
            c = new _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace();
            c.sector = he;
            d = referencePlane.pointDistance((he.next()).startingVertex.position);
            c.cl = PolyhedralBoundedSolid.compareValue(d, 0.0, VSDK.EPSILON);
            c.isWide = false;
            c.position = new Vector3D((he.next()).startingVertex.position);
            c.situation = _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.UNDEFINED;
            c.referencePlane = referencePlane;
            neighborSectorsInfo.add(c);
            if ( checkWideness(he) ) {
                bisect = inside(he).add(vtx.position);
                c.situation = _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.CROSSING_EDGE;

                c = new _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace();
                c.sector = he;
                d = referencePlane.pointDistance(bisect);
                c.cl = PolyhedralBoundedSolid.compareValue(d, 0.0, VSDK.EPSILON);
                c.isWide = true;
                c.position = new Vector3D(bisect);
                c.situation = _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.CROSSING_EDGE;
                c.referencePlane = referencePlane;
                neighborSectorsInfo.add(c);
            }
            he = (he.mirrorHalfEdge()).next();
        } while ( he != vtx.emanatingHalfEdge );

        //-----------------------------------------------------------------
        // Extra pass, not from original [MANT1988] code
        int i;

        for ( i = 0; i < neighborSectorsInfo.size(); i++ ) {
            c = neighborSectorsInfo.get(i);
            if ( c.cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.ON && c.situation == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.UNDEFINED ) {
                c.situation = _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.INPLANE_EDGE;
            }
        }

        return neighborSectorsInfo;
    }

    /**
    Current method applies the first reclassification rule presented at
    sections [MANT1988].14.5.1 and [MANT1988].14.5.2., but biased towards the
    set operator classifier, as proposed on section [MANT1988].15.6.1. and
    problem [MANT1988].15.4.:
    For the given vertex neigborhood, classify each edge according to whether
    its final vertex lies above (out), on or below (in) the `referencePlane`.
    Tag the edge with the corresponding label ABOVE, ON or BELOW.
    Following program [MANT1988].14.5.
    */
    private static void vertexFaceReclassifyOnSectorsNoPeekVersion(
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> nbr,
        InfinitePlane referencePlane, int BvsA, int op)
    {
        _PolyhedralBoundedSolidFace f;
        Vector3D c;
        double d;
        int i;
        int nnbr = nbr.size();
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector ni;

        ni = new _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector();

        //-----------------------------------------------------------------
        // Backup neighborhood to prevent empty case
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> backup;

        backup = new ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace>();
        for ( i = 0; i < nnbr; i++ ) {
            backup.add(new _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace(nbr.get(i)));
        }

        //-----------------------------------------------------------------
        // Only will be activated if non empty case result (force to intersect)
        for ( i = 0; i < nnbr; i++ ) {
            // Test coplanarity
            f = nbr.get(i).sector.parentLoop.parentFace;
            c = f.containingPlane.getNormal().crossProduct(referencePlane.getNormal());
            d = c.dotProduct(c);
            if ( PolyhedralBoundedSolid.compareValue(d, 0.0, VSDK.EPSILON) == 0 ) {
                // Entering this means "faces are coplanar"
                //System.out.println("**** UNTESTED CASE!");
                d = f.containingPlane.getNormal().dotProduct(referencePlane.getNormal());
                if ( PolyhedralBoundedSolid.compareValue(d, 0.0, VSDK.EPSILON) == 1 ) {
                    // Identical
                    if ( BvsA != 0 ) {
                        nbr.get(i).cl = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                        nbr.get((i+1)%nnbr).cl =
                            (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                    }
                    else {
                        nbr.get(i).cl = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN;
                        nbr.get((i+1)%nnbr).cl =
                            (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN;
                    }
                }
                else {
                    // Opposite
                    if ( BvsA != 0 ) {
                        nbr.get(i).cl = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                        nbr.get((i+1)%nnbr).cl =
                            (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                    }
                    else {
                        nbr.get(i).cl = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                        nbr.get((i+1)%nnbr).cl =
                            (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                    }
                }
            }
        }

        //-----------------------------------------------------------------
        // Restore original neighborhood if result is empty case
        int ins = 0;
        int outs = 0;

        for ( i = 0; i < nnbr; i++ ) {
            if ( nbr.get(i).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT ) {
                outs++;
            }
            else {
                ins++;
            }
        }
        if ( outs == nnbr || ins == nnbr) {
            //System.out.println("**** WRONG ON SECTOR RECLASSIFICATION! REVERSED!");
            for ( i = 0; i < nnbr; i++ ) {
                nbr.get(i).cl = backup.get(i).cl;
                //nbr.get(i).reverse = true;
            }
        }
    }

    /**
    Current method applies the first reclassification rule presented at
    sections [MANT1988].14.5.1 and [MANT1988].14.5.2., but biased towards the
    set operator classifier, as proposed on section [MANT1988].15.6.1. and
    problem [MANT1988].15.4.:
    For the given vertex neigborhood, classify each edge according to whether
    its final vertex lies above (out), on or below (in) the `referencePlane`.
    Tag the edge with the corresponding label ABOVE, ON or BELOW.
    Following program [MANT1988].14.5.
    -----------------------------------------------------------------
    Reclassification procedure for "on"-sectors on the vertex/face clasiffier,
    Original answer from [.WMANT2008].
    */
    private static void vertexFaceReclassifyOnSectors(
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> nbr,
        InfinitePlane referencePlane, int BvsA, int op)
    {
        _PolyhedralBoundedSolidFace f;
        Vector3D c;
        double d;
        int i;
        int nnbr = nbr.size();
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector ni;

        ni = new _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector();

        //-----------------------------------------------------------------
        // Backup neighborhood to prevent empty case
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> backup;

        backup = new ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace>();
        for ( i = 0; i < nnbr; i++ ) {
            backup.add(new _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace(nbr.get(i)));
        }

        //-----------------------------------------------------------------
        // Only will be activated if non empty case result (force to intersect)
        for ( i = 0; i < nnbr; i++ ) {
            // Test coplanarity
            f = nbr.get(i).sector.mirrorHalfEdge().parentLoop.parentFace;
            c = f.containingPlane.getNormal().crossProduct(referencePlane.getNormal());
            d = c.dotProduct(c);
            if ( PolyhedralBoundedSolid.compareValue(d, 0.0, VSDK.EPSILON) == 0 ) {
                // Entering this means "faces are coplanar"
                d = f.containingPlane.getNormal().dotProduct(referencePlane.getNormal());
                // Test orientation
                if ( PolyhedralBoundedSolid.compareValue(d, 0.0, VSDK.EPSILON) == 1 ) {
                    // Identical
                    if ( BvsA != 0 ) {
                        nbr.get(i).cl = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                        nbr.get((i+1)%nnbr).cl =
                            (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                    }
                    else {
                        nbr.get(i).cl = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN;
                        nbr.get((i+1)%nnbr).cl =
                            (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN;
                    }
                }
                else {
                    // Opposite
                    if ( BvsA != 0 ) {
                        nbr.get(i).cl = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                        nbr.get((i+1)%nnbr).cl =
                            (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                    }
                    else {
                        nbr.get(i).cl = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                        nbr.get((i+1)%nnbr).cl =
                            (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                    }
                }
            }
        }

        //-----------------------------------------------------------------
        // Restore original neighborhood if result is empty case
        int ins = 0;
        int outs = 0;

        for ( i = 0; i < nnbr; i++ ) {
            if ( nbr.get(i).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT ) {
                outs++;
            }
            else {
                ins++;
            }
        }
        if ( outs == nnbr || ins == nnbr) {
            System.out.println("**** WRONG ON SECTOR RECLASSIFICATION! REVERSED!");
            for ( i = 0; i < nnbr; i++ ) {
                nbr.get(i).cl = backup.get(i).cl;
                //nbr.get(i).reverse = true;
            }
        }
    }

    private static void printNbr(ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> neighborSectorsInfo)
    {
        int i;

        for ( i = 0; i < neighborSectorsInfo.size(); i++ ) {
            System.out.println("    . " + neighborSectorsInfo.get(i));
        }
    }

    private static boolean inplaneEdgesOn(
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> nbr)
    {
        int i;

        for ( i = 0; i < nbr.size(); i++ ) {
            if ( nbr.get(i).situation == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.INPLANE_EDGE ) return true;
        }
        return false;
    }

    private static void vertexFaceReclassifyOnEdges(
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> nbr,
        int op, boolean useBorrowed)
    {
        if ( useBorrowed ) {
            vertexFaceReclassifyOnEdgesBorrowed(nbr, op);
          }
          else {
            vertexFaceReclassifyOnEdgesNoPeekVersion(nbr, op);
        }
    }

    /**
    Current method implements the set of changes from table [MANT1988].15.3.
    for the reclassification rules.
    */
    private static void vertexFaceReclassifyOnEdgesNoPeekVersion(
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> nbr,
        int op)
    {
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace l;
        int i;

        for ( i = 0; i < nbr.size(); i++ ) {
            l = nbr.get(i);
            l.applyRules(op);
        }
    }

    /**
    Current method implements the set of changes from table [MANT1988].15.3.
    for the reclassification rules.
    -----------------------------------------------------------------
    Original answer from [.WMANT2008].
    */
    private static void vertexFaceReclassifyOnEdgesBorrowed(
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> nbr,
        int op)
    {
        int i;
        int nnbr = nbr.size();
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector ni;

        ni = new _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector();

        for ( i = 0; i < nnbr; i++ ) {
            if ( nbr.get(i).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.ON ) {
                if ( nbr.get((nnbr+i-1)%nnbr).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN ) {
                    if ( nbr.get((i+1)%nnbr).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN ) {
                        nbr.get(i).cl = _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN;
                    }
                    else {
                        nbr.get(i).cl = _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN;
                    }
                }
                else {
                    // OUT 
                    if ( nbr.get((i+1)%nnbr).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN ) {
                        nbr.get(i).cl = _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN;
                    }
                    else {
                        nbr.get(i).cl = _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                    }
                }
            }
        }
    }

    private static void vertexFaceInsertNullEdges(
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> nbr,
        _PolyhedralBoundedSolidFace f,
        _PolyhedralBoundedSolidVertex v,
        int BvsA, boolean useBorrowed, PolyhedralBoundedSolid inSolidA,
        PolyhedralBoundedSolid inSolidB)
    {
        if ( useBorrowed ) {
            vertexFaceInsertNullEdgesBorrowed(nbr, f, v, BvsA, inSolidA, inSolidB);
        }
        else {
            vertexFaceInsertNullEdgesNoPeekVersion(nbr, f, v, BvsA, inSolidA, inSolidB);
        }
    }

    /**
    This method implements the third stage of the vertex/face classifier:
    given the previously reclassified list of vertex neigbors, insert
    a new vertex (using operator lmev) in the direction of the last
    "in" before an "out" sector of the sequence.

    This implementation follows section [MANT1988].14,6,2 and program
    [MANT1988].14.7., but it is biased for set operations, as indicated on
    section [MANT1988].15.6.1.

    Taking in to account the updated version modifications from
    [.wMANT2008].
    */
    private static void vertexFaceInsertNullEdgesNoPeekVersion(
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> nbr,
        _PolyhedralBoundedSolidFace f,
        _PolyhedralBoundedSolidVertex v,
        int BvsA, PolyhedralBoundedSolid inSolidA,
        PolyhedralBoundedSolid inSolidB)
    {
        int start, i;
        _PolyhedralBoundedSolidHalfEdge head, tail;
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace n;
        PolyhedralBoundedSolid solida;
        int nnbr = nbr.size();
        ArrayList<_PolyhedralBoundedSolidSetOperatorNullEdge> sone = null;

        solida = v.emanatingHalfEdge.parentLoop.parentFace.parentSolid;

        if ( nnbr <= 0 ) return;
        n = nbr.get(0);

        //- Locate the head of an ABOVE-sequence --------------------------
        i = 0;
        while ( !( (nbr.get(i).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.AinB || nbr.get(i).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.BinA) &&
                   ((nbr.get( (i+1)%nnbr ).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.AoutB) ||
                     nbr.get( (i+1)%nnbr ).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.BoutA))  ) {
            i++;
            if ( i >= nnbr ) {
                //System.out.println("**** EMPTY CASE!");
                return;
            }
        }
        start = i;
        head = nbr.get(i).sector;

        //-----------------------------------------------------------------
        while ( true ) {
            //- Locate the final sector of the sequence ------------------
            while ( !( (nbr.get(i).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.AoutB || nbr.get(i).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.BoutA) &&
                       (nbr.get( (i+1)%nnbr ).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.AinB ||
                        nbr.get( (i+1)%nnbr ).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.BinA) ) ) {
                i = (i+1) % nnbr;
            }
            tail = nbr.get(i).sector;

            //- Insert null edge -----------------------------------------
            if ( (debugFlags & DEBUG_03_VERTEXFACECLASIFFIER) != 0x00 &&
                 (debugFlags & DEBUG_99_SHOWOPERATIONS) != 0x00 ) {
                System.out.println("       -> LMEV (Vertex/face split):");
                System.out.println("          . (" + start + ") H1: " + head);
                System.out.println("          . (" + i + ") H2: " + tail);
            }

            solida.lmev(head, tail, nextVertexId(inSolidA, inSolidB), head.startingVertex.position);

            if ( (debugFlags & DEBUG_03_VERTEXFACECLASIFFIER) != 0x00 &&
                 (debugFlags & DEBUG_99_SHOWOPERATIONS) != 0x00 ) {
                //head.startingVertex.debugColor = new ColorRgb(0, 1, 0);
                System.out.println("          . New vertex: " + head.startingVertex.id);
            }

            if ( BvsA != 0 ) {
                sone = soneb;
              }
              else {
                sone = sonea;
            }
            sone.add(new _PolyhedralBoundedSolidSetOperatorNullEdge(head.previous().parentEdge));

            //- Pierce face ---------------------------------------------------
            makering(f, v, BvsA, inSolidA, inSolidB);

            //- Locate the start of the next sequence --------------------
            while ( !( (nbr.get(i).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.AinB || nbr.get(i).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.BinA) &&
                       ((nbr.get( (i+1) % nnbr ).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.AoutB ||
                         nbr.get( (i+1) % nnbr ).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace.BoutA)) ) ) {
                i = (i+1) % nnbr;
                if ( i == start ) {
                    return;
                }
            }
        }

        //-----------------------------------------------------------------
    }

    private static void vertexFaceInsertNullEdgesBorrowed(
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> nbr,
        _PolyhedralBoundedSolidFace f,
        _PolyhedralBoundedSolidVertex v,
        int BvsA, PolyhedralBoundedSolid inSolidA,
        PolyhedralBoundedSolid inSolidB)
    {
        int start, i;
        _PolyhedralBoundedSolidHalfEdge head, tail;
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace n;
        PolyhedralBoundedSolid solida;
        int nnbr = nbr.size();
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector ni;
        ArrayList<_PolyhedralBoundedSolidSetOperatorNullEdge> sone = null;

        ni = new _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector();

        solida = v.emanatingHalfEdge.parentLoop.parentFace.parentSolid;

        if ( nnbr <= 0 ) return;
        n = nbr.get(0);

        //- Locate the head of an ABOVE-sequence --------------------------
        i = 0;
        while ( !( nbr.get(i).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN &&
                   nbr.get((i+1)%nnbr).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT ) ) {
            i++;
            if ( i >= nnbr ) {
                //System.out.println("**** EMPTY CASE!");
                return;
            }
        }
        start = i;
        head = nbr.get(i).sector;

        //-----------------------------------------------------------------
        while ( true ) {
            //- Locate the final sector of the sequence ------------------
            while ( !( nbr.get(i).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT &&
                       nbr.get((i+1)%nnbr).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN ) ) {
                i = (i+1) % nnbr;
            }
            tail = nbr.get(i).sector;

            //- Insert null edge -----------------------------------------
            if ( (debugFlags & DEBUG_03_VERTEXFACECLASIFFIER) != 0x00 &&
                 (debugFlags & DEBUG_99_SHOWOPERATIONS) != 0x00 ) {
                System.out.println("       -> LMEV (Vertex/face split):");
                System.out.println("          . (" + start + ") H1: " + head);
                System.out.println("          . (" + i + ") H2: " + tail);
            }
            solida.lmev(head, tail, nextVertexId(inSolidA, inSolidB), head.startingVertex.position);

            if ( BvsA != 0 ) {
                sone = soneb;
              }
              else {
                sone = sonea;
            }
            sone.add(new _PolyhedralBoundedSolidSetOperatorNullEdge(head.previous().parentEdge));

            //- Pierce face ---------------------------------------------------
            makering(f, v, BvsA, inSolidA, inSolidB);

            //- Locate the start of the next sequence --------------------
            while ( !( nbr.get(i).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN &&
                       nbr.get((i+1)%nnbr).cl == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT ) ) {
                i = (i+1) % nnbr;
                if ( i == start ) {
                    return;
                }
            }
        }

        //-----------------------------------------------------------------
    }

    /**
    Vertex/Face classifier for the set operations algorithm (big phase 1).
    Answer to problem [MANT1988].15.4.
    */
    private static void vertexFaceClassify(
        _PolyhedralBoundedSolidVertex v,
        _PolyhedralBoundedSolidFace f,
        int op,
        int BvsA,
        PolyhedralBoundedSolid inSolidA,
        PolyhedralBoundedSolid inSolidB)
    {
        //- Following classification strategy from the splitter algorithm -
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnFace> nbr;

        if ( (debugFlags & DEBUG_01_STRUCTURE) != 0x00 ) {
            if ( (debugFlags & DEBUG_03_VERTEXFACECLASIFFIER) != 0x00 ) {
                System.out.print("  * ");
            }
            else {
                System.out.print("  - ");
            }
            System.out.println("Vertex/face pair V[" + v.id + "] / f[" + f.id + "]");
        }

        nbr = vertexFaceGetNeighborhood(v, f.containingPlane, BvsA);
        if ( inplaneEdgesOn(nbr) ) {
            // In "strict analogy" to the splitter problem
            Collections.reverse(nbr);
        }

        if ( (debugFlags & DEBUG_03_VERTEXFACECLASIFFIER) != 0x00 ) {
            System.out.println("   - Initial sector neigborhood by near end vertices:");
            printNbr(nbr);
        }

        vertexFaceReclassifyOnSectorsNoPeekVersion(nbr, f.containingPlane, op, BvsA);

        //- Adjusting results for set operation interpretation ------------
        boolean borrowed = false;

        int i;
        for ( i = 0; !borrowed && i < nbr.size(); i++ ) {
            nbr.get(i).updateLabel(BvsA);
        }

        if ( (debugFlags & DEBUG_03_VERTEXFACECLASIFFIER) != 0x00 ) {
            System.out.println("   - Sector neigborhood reclassified on sectors (8-way boundary classification):");
            printNbr(nbr);
        }

        vertexFaceReclassifyOnEdges(nbr, op, borrowed);

        if ( (debugFlags & DEBUG_03_VERTEXFACECLASIFFIER) != 0x00 ) {
            System.out.println("   - Sector neigborhood reclassified on edges:");
            printNbr(nbr);
        }

        vertexFaceInsertNullEdges(nbr, f, v, BvsA, borrowed, inSolidA, inSolidB);
    }

    private static void makering(
        _PolyhedralBoundedSolidFace f,
        _PolyhedralBoundedSolidVertex v,
        int type,
        PolyhedralBoundedSolid inSolidA,
        PolyhedralBoundedSolid inSolidB)
    {
        PolyhedralBoundedSolid solida, solidb;
        _PolyhedralBoundedSolidHalfEdge he;

        solida = inSolidA;
        solidb = inSolidB;
        if ( type == 1 ) {
            solida = inSolidB;
            solidb = inSolidA;
        }
        //solida = v.emanatingHalfEdge.parentLoop.parentFace.parentSolid;
        //solidb = f.parentSolid;

        he = f.boundariesList.get(0).boundaryStartHalfEdge;

        int vn1, vn2;
        vn1 = nextVertexId(solida, solidb);
        solidb.lmev(he, he, vn1, v.position);
        he = solidb.findVertex(vn1).emanatingHalfEdge;
        solidb.lkemr(he.mirrorHalfEdge(), he);

        vn2 = nextVertexId(solida, solidb);
        solidb.lmev(he, he, vn2, v.position);

        if ( (debugFlags & DEBUG_03_VERTEXFACECLASIFFIER) != 0x00 &&
             (debugFlags & DEBUG_99_SHOWOPERATIONS) != 0x00 ) {
            System.out.println("       -> MAKERING (Vertex/face pierce):");
            System.out.println("          . New vertexes: " + vn1 + "/" + vn2 + ".");
            //he.startingVertex.debugColor = new ColorRgb(0, 0, 1);
        }

        ArrayList<_PolyhedralBoundedSolidSetOperatorNullEdge> sone = null;
        if ( type == 1 ) {
            sone = sonea;
        }
        else {
            sone = soneb;
        }
        sone.add(new _PolyhedralBoundedSolidSetOperatorNullEdge(he.parentEdge));
    }

    /**
    Constructs a vector along the bisector of the sector defined by `he`.
    that points inward the he's containing face.
    */
    protected static Vector3D inside(_PolyhedralBoundedSolidHalfEdge he)
    {
        Vector3D middle = null;
        Vector3D a, b, n;

        a = (he.next()).startingVertex.position.substract(he.startingVertex.position);
        b = (he.previous()).startingVertex.position.substract(he.startingVertex.position);
        a.normalize();
        b.normalize();

        n = he.parentLoop.parentFace.containingPlane.getNormal();

        middle = n.crossProduct(a);
        middle.normalize();

        return middle;
    }


    /**
    Following program [MANT1988].15.8.
    */
    private static
    ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex>
    nbrpreproc(_PolyhedralBoundedSolidVertex v)
    {
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex n, nold;
        Vector3D bisec;
        _PolyhedralBoundedSolidHalfEdge he;
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex> nb;

        nb = new ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex>();

        he = v.emanatingHalfEdge;
        Vector3D oldref2;

        do {
            n = new _PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex();
            n.he = he;
            n.wide = false;

            n.ref1 = he.previous().startingVertex.position.substract(
                he.startingVertex.position);    
            n.ref2 = he.next().startingVertex.position.substract(
                he.startingVertex.position);
            n.ref12 = n.ref1.crossProduct(n.ref2);

            if ( (n.ref12.length() < VSDK.EPSILON) ||
                 (n.ref12.dotProduct(he.parentLoop.parentFace.containingPlane.getNormal()) > 0.0 ) ) {
                // Inside this conditional means: current vertex is a wide one
                if ( (n.ref12.length() < VSDK.EPSILON) ) {
                    bisec = inside(he);
                }
                else {
                    bisec = n.ref1.add(n.ref2);
                    bisec = bisec.multiply(-1);
                }
                oldref2 = n.ref2;
                n.ref2 = bisec;
                n.ref12 = n.ref1.crossProduct(n.ref2);
                nold = n;
                nb.add(n);

                n = new _PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex();
                n.he = he;
                n.ref2 = oldref2;
                n.ref1 = bisec;
                n.ref12 = n.ref1.crossProduct(n.ref2);
                n.wide = true;
            }

            nb.add(n);

            he = (he.mirrorHalfEdge()).next();
        } while( he != v.emanatingHalfEdge );

        return nb;
    }

    private static double angleFromVectors(Vector3D u, Vector3D v, Vector3D a)
    {
        double x, y;
        double an;

        x = a.dotProduct(u);
        y = a.dotProduct(v);

        an = Math.acos(x);
        if ( y < 0 ) an *= -1;
        return an;
    }

    /**
    Given two coplanar sectors that share a common edge, this method determine
    if the sectors are edge neighbors (this case returs false) or overlaping
    sectors (this case returns true).
    */
    private static boolean sectorOverSector(
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex na,
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex nb,
        Vector3D commonEdge
    )
    {
        Vector3D boundingEdgeA = null;
        Vector3D boundingEdgeB = null;

        if ( colinearVectorsWithDirection(na.ref1, commonEdge) ) {
            boundingEdgeA = na.ref2;
        }
        else {
            boundingEdgeA = na.ref1;
        }

        if ( colinearVectorsWithDirection(nb.ref1, commonEdge) ) {
            boundingEdgeB = nb.ref2;
        }
        else {
            boundingEdgeB = nb.ref1;
        }

        if ( colinearVectorsWithDirection(boundingEdgeA, boundingEdgeB) ) {
            return true;
        }
        return false;
    }

    /**
    Checks if two coplanar sectors overlaps, by doing a "sector within" test
    for coplanar sectors: If the two given sectors are coplanar and with
    overlaping faces:
      - If sectors only intersects in one point returns false.
      - If sectors intersects on a line or area returns true.

    Following section [MANT1988].15.6.2. Note that this operation is not
    elaborated on [MANT1988], but left as an excercise.

    PRE: Given sectors are "coplanar".
    */
    private static boolean sectoroverlap(
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex na,
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex nb)
    {
        //- Convert side vectors of sectors into angles -------------------
        double a1, a2;
        double b1, b2;
        Vector3D u, v, a, b, c, n;

        n = na.he.parentLoop.parentFace.containingPlane.getNormal();
        u = new Vector3D(na.ref1);
        u.normalize();
        v = n.crossProduct(u);
        v.normalize();

        a = new Vector3D(na.ref2);
        a.normalize();
        b = new Vector3D(nb.ref1);
        b.normalize();
        c = new Vector3D(nb.ref2);
        c.normalize();

        a1 = angleFromVectors(u, v, u);
        a2 = angleFromVectors(u, v, a);
        b1 = angleFromVectors(u, v, b);
        b2 = angleFromVectors(u, v, c);

        //- Order the angles in ascending order angle intervals -----------
        // Given angles are between -180 and 180 degrees
        double t;

        if ( a1 > a2 ) {
            t = a1;
            a1 = a2;
            a2 = t;
        }
        if ( b1 > b2 ) {
            t = b1;
            b1 = b2;
            b2 = t;
        }

        //- Calculate interval intersection -------------------------------
        if ( a2 + VSDK.EPSILON > b1 - VSDK.EPSILON ) {

            if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0 ) {
                System.out.print(" <TRUE>");
            }

            return true;
        }

        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0 ) {
            System.out.print(" <FALSE>");
        }

        return false;
    }

    /**
    Following program [MANT1988].15.9. According to the sector intersection
    test from section [MANT1988].15.6.2, the variables (with respect to
    the central vertex on a given sector) are:
      - dir is the vector from the starting vertex of the sector, pointing
        on the direction of the intersection line with another sector, or
        `int` in figure [MANT1988].15.8. and equation [MANT1988].15.5.
      - ref1 and ref2 are the same as in figure [MANT1988].15.8. and
        equation [MANT1988].15.5.
      - ref12 is the cross product of ref1 and ref2, or `ref` in figure
        [MANT1988].15.8. and equation [MANT1988].15.5.
      - c1 is the cross product of ref1 and dir, or `test1` in figure
        [MANT1988].15.8. and equation [MANT1988].15.5.
      - c2 is the cross product of dir and ref2, or `test2` in figure
        [MANT1988].15.8. and equation [MANT1988].15.5.
    */
    private static boolean sctrwitthin(Vector3D dir, Vector3D ref1,
                            Vector3D ref2, Vector3D ref12)
    {
        Vector3D c1, c2;
        int t1, t2;

        c1 = dir.crossProduct(ref1);
        if ( c1.length() < VSDK.EPSILON ) {
            return (ref1.dotProduct(dir) > 0.0);
        }
        c2 = ref2.crossProduct(dir);
        if ( c2.length() < VSDK.EPSILON ) {
            return (ref2.dotProduct(dir) > 0.0);
        }
        t1 = PolyhedralBoundedSolid.compareValue(c1.dotProduct(ref12), 0.0, VSDK.EPSILON);
        t2 = PolyhedralBoundedSolid.compareValue(c2.dotProduct(ref12), 0.0, VSDK.EPSILON);
        return ( t1 < 0.0 && t2 < 0.0 );
    }

    private static boolean sctrwitthinProper(Vector3D dir, Vector3D ref1,
                                             Vector3D ref2, Vector3D ref12)
    {
        if ( colinearVectors(dir, ref1) || colinearVectors(dir, ref2) ) {
            return false;
        }

        return sctrwitthin(dir, ref1, ref2, ref12);
    }

    /**
    Sector intersection test.

    Following program [MANT1988].15.9. and section [MANT1988].15.6.2.
    */
    private static boolean vertexVertexSectorIntersectionTest(int i, int j)
    {
        //-----------------------------------------------------------------
        _PolyhedralBoundedSolidHalfEdge h1, h2;
        boolean c1, c2;
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex na, nb;

        na = nba.get(i);
        nb = nbb.get(j);
        h1 = na.he;
        h2 = nb.he;

        //-----------------------------------------------------------------
        // Here, n1 and n2 are the plane normals for containing faces of
        // sectors i and j, as in figure [MANT1988].15.7.
        Vector3D n1, n2;
        Vector3D intrs;

        n1 = h1.parentLoop.parentFace.containingPlane.getNormal();
        n2 = h2.parentLoop.parentFace.containingPlane.getNormal();
        intrs = n1.crossProduct(n2);

        //-----------------------------------------------------------------
        if ( intrs.length() < VSDK.EPSILON ) {
            if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0 ) {
                System.out.print(" <coplanar>");
            }
            return sectoroverlap(na, nb);
        }

        //-----------------------------------------------------------------
        c1 = sctrwitthin(intrs, na.ref1, na.ref2, na.ref12);
        c2 = sctrwitthin(intrs, nb.ref1, nb.ref2, nb.ref12);
        if ( c1 && c2 ) {
            if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0 ) {
                System.out.print(" <TRUE>");
            }
            return true;
        }
        else {
            intrs = intrs.multiply(-1);
            c1 = sctrwitthin(intrs, na.ref1, na.ref2, na.ref12);
            c2 = sctrwitthin(intrs, nb.ref1, nb.ref2, nb.ref12);
            if ( c1 && c2 ) {
                if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0 ) {
                    System.out.print(" <TRUE>");
                }
                return true;
            }
        }

        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0 ) {
            System.out.print(" <FALSE>");
        }

        return false;
    }

    /**
    Given a pair of coincident vertices `va` (on solid A) and `vb` (on solid
    B), this method creates the lists `nba`, `nbb` and `sectors`, as explained
    in section [MANT1988].15.6.2. and program [MANT1988].15.7.

    Note that from all possible sector pairs, this method does not include
    in the `sectors` set any sector pair that touches just in one point.
    */
    private static void vertexVertexGetNeighborhood(
        _PolyhedralBoundedSolidVertex va,
        _PolyhedralBoundedSolidVertex vb)
    {
        //-----------------------------------------------------------------
        int i;

        nba = nbrpreproc(va);
        nbb = nbrpreproc(vb);
        sectors = new ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector>();


        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0 ) {
            System.out.println("   - NBA list of neighbor sectors for vertex on {A}:");
            for ( i = 0; i < nba.size(); i++ ) {
                System.out.println("    . A[" + (i+1) + "]: " + nba.get(i));
            }
            System.out.println("   - NBB list of neighbor sectors for vertex on {B}:");
            for ( i = 0; i < nbb.size(); i++ ) {
                System.out.println("    . B[" + (i+1) + "]: " + nbb.get(i));
            }
        }

        //-----------------------------------------------------------------
        _PolyhedralBoundedSolidHalfEdge ha, hb;
        double d1, d2, d3, d4;
        int j;
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector s;
        Vector3D na, nb;
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex xa, xb;

        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0 ) {
            System.out.println("   - Initial intersection tests between sectors (false intersections are sectors touching on a single point):");
        }

        for ( i = 0; i < nba.size(); i++ ) {
            for ( j = 0; j < nbb.size(); j++ ) {

                if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0 ) {
                    System.out.print("    . A[" + (i+1) + "] / B[" + (j+1) + "]:");
                }

                if ( vertexVertexSectorIntersectionTest(i, j) ) {
                    s = new _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector();
                    s.secta = i;
                    s.sectb = j;
                    xa = nba.get(i);
                    xb = nbb.get(j);
                    s.hea = xa.he;
                    s.heb = xb.he;
                    s.wa = xa.wide;
                    s.wb = xb.wide;

                    na = xa.he.parentLoop.parentFace.containingPlane.getNormal();
                    nb = xb.he.parentLoop.parentFace.containingPlane.getNormal();
                    d1 = nb.dotProduct(xa.ref1);
                    d2 = nb.dotProduct(xa.ref2);
                    d3 = na.dotProduct(xb.ref1);
                    d4 = na.dotProduct(xb.ref2);
                    s.s1a = PolyhedralBoundedSolid.compareValue(d1, 0.0, VSDK.EPSILON);
                    s.s2a = PolyhedralBoundedSolid.compareValue(d2, 0.0, VSDK.EPSILON);
                    s.s1b = PolyhedralBoundedSolid.compareValue(d3, 0.0, VSDK.EPSILON);
                    s.s2b = PolyhedralBoundedSolid.compareValue(d4, 0.0, VSDK.EPSILON);
                    s.intersect = true;
                    sectors.add(s);
                }

                if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0 ) {
                    System.out.print("\n");
                }

            }
        }

    }

    /**
    Following section [MANT1988].15.6.2. and program [MANT1988].15.10.

    Note that this sector deactivates the intersection flag for non-
    interpenetrating coplanar sectors (those who touches just in an edge or
    common line) and its neighbors.
    */
    private static void vertexVertexReclassifyOnSectors(int op)
    {
        _PolyhedralBoundedSolidHalfEdge ha, hb;
        int i, j, newsa, newsb;
        boolean nonopposite;
        int secta, prevsecta, nextsecta;
        int sectb, prevsectb, nextsectb;
        double d;
        Vector3D n1, n2;
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector si, sj;

        for ( i = 0; i < sectors.size(); i++ ) {
            if ( sectors.get(i).s1a == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.ON &&
                 sectors.get(i).s2a == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.ON &&
                 sectors.get(i).s1b == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.ON &&
                 sectors.get(i).s2b == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.ON ) {
                // This condition means: "current sectors are coplanar"

                // Determine orientation for current sector pair
                secta = sectors.get(i).secta;
                sectb = sectors.get(i).sectb;
                prevsecta = (secta == 0)?nba.size()-1:secta-1;
                prevsectb = (sectb == 0)?nbb.size()-1:sectb-1;
                nextsecta = (secta == nba.size()-1)?0:secta+1;
                nextsectb = (sectb == nbb.size()-1)?0:sectb+1;
                ha = nba.get(secta).he;
                hb = nbb.get(sectb).he;
                n1 = ha.parentLoop.parentFace.containingPlane.getNormal();
                n2 = hb.parentLoop.parentFace.containingPlane.getNormal();
                d = VSDK.vectorDistance(n1, n2);
                nonopposite = ( d < VSDK.EPSILON );
                if ( nonopposite ) {
                    newsa = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN;
                    newsb = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                }
                else {
                    newsa = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                    newsb = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;
                }
                si = sectors.get(i);

                // Propagate to neigbor sectors
                for ( j = 0; j < sectors.size(); j++ ) {
                    sj = sectors.get(j);
                    if ( (sj.secta == prevsecta) && (sj.sectb == sectb) ) {
                        if ( sj.s1a != _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.ON ) {
                            sj.s2a = newsa;
                        }
                    }
                    if ( (sj.secta == nextsecta) && (sj.sectb == sectb) ) {
                        if ( sj.s2a != _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.ON ) {
                            sj.s1a = newsa;
                        }
                    }
                    if ( (sj.secta == secta) && (sj.sectb == prevsectb) ) {
                        if ( sj.s1b != _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.ON ) {
                            sj.s2b = newsb;
                        }
                    }
                    if ( (sj.secta == secta) && (sj.sectb == nextsectb) ) {
                        if ( sj.s2b != _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.ON ) {
                            sj.s1b = newsb;
                        }
                    }
                    if ( (sj.s1a == sj.s2a) && 
                         (sj.s1a == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN || sj.s1a == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT) ) {
                        sj.intersect = false;
                    }
                    if ( (sj.s1b == sj.s2b) && 
                         (sj.s1b == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN || sj.s1b == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT) ) {
                        sj.intersect = false;
                    }
                }

                // End
                si.s1a = si.s2a = newsa;
                si.s1b = si.s2b = newsb;
                si.intersect = false;
            }
        }
    }

    private static boolean colinearVectors(Vector3D a, Vector3D b)
    {
        if ( a.crossProduct(b).length() < VSDK.EPSILON ) {
            return true;
        }
        return false;
    }

    public static boolean colinearVectorsWithDirection(Vector3D a, Vector3D b)
    {
        if ( a.crossProduct(b).length() < VSDK.EPSILON ) {
            if ( a.dotProduct(b) >= 0 ) return true;
        }
        return false;
    }

    private static void addNoRepeat(
        ArrayList<_PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex> list,
        _PolyhedralBoundedSolidSetOperatorSectorClassificationOnVertex element)
    {
        int i;

        for ( i = 0; i < list.size(); i++ ) {
            if ( list.get(i) == element ) return;
        }
        list.add(element);
    }

    /**
    Reclassification procedure for "on"-edges on the vertex/vertex clasiffier,
    as expected to work from functional high level description on section
    [MANT1986].15.6.2.  Astonishingly, the descriptions given on [MANT1988].15.
    and figures [MANT1988].15.10., [MANT1988].15.11., and [MANT1988].15.12.
    does not provides enough information to lead to a complete implementation
    of the complex case analysis required for sectors on sector and sectors
    on edge intersections.
    Fortunately, the missing details can be found on [MANT1986].6.2.2.

    PRE: All detected sector pairs are complient with the following:
    - They are not coplanar
    - They intersect in a common line

    Given two intersecting sectors (the "test sectors"), this method seek if
    their common intersection line intersects with a third sector inside
    (the "reference sector on edge-sector coincidence") or if that line
    intersects with a common edge of a third and a fourth sectors (the
    "reference pair of sectors on edge-edge coincidence"), and calls the
    corresponding case management methods for each situation.
    -----------------------------------------------------------------
    Reclassification procedure for "on"-edges on the vertex/vertex clasiffier,
    Original answer from [.WMANT2008].
    */
    private static void vertexVertexReclassifyOnEdges(int op)
    {
        int i, j, newsa, newsb;
        int secta, prevsecta;
        int sectb, prevsectb;

        // Search for doubly coplanar edges
        for ( i = 0; i < sectors.size(); i++ ) {
            // Double "on"-edge ?
            if ( sectors.get(i).intersect &&
                 sectors.get(i).s1a == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.ON &&
                 sectors.get(i).s1b == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.ON ) {
                // Figure out the new classifications for the "on"-edges
                newsa = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN;
                newsb = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT;

                secta = sectors.get(i).secta;
                sectb = sectors.get(i).sectb;
                prevsecta = (secta == 0)?nba.size()-1:secta-1;
                prevsectb = (sectb == 0)?nbb.size()-1:sectb-1;

                // Reclassify all instances of the situation
                for ( j = 0; j < sectors.size(); j++ ) {
                    if ( sectors.get(j).intersect ) {
                        if ( (sectors.get(j).secta == secta) &&
                             (sectors.get(j).sectb == sectb) ) {
                            sectors.get(j).s1a = newsa;
                            sectors.get(j).s1b = newsb;
                        }

                        if ( (sectors.get(j).secta == prevsecta) &&
                             (sectors.get(j).sectb == sectb) ) {
                            sectors.get(j).s2a = newsa;
                            sectors.get(j).s1b = newsb;
                        }

                        if ( (sectors.get(j).secta == secta) &&
                             (sectors.get(j).sectb == prevsectb) ) {
                            sectors.get(j).s1a = newsa;
                            sectors.get(j).s2b = newsb;
                        }

                        if ( (sectors.get(j).secta == prevsecta) &&
                             (sectors.get(j).sectb == prevsectb) ) {
                            sectors.get(j).s2a = newsa;
                            sectors.get(j).s2b = newsb;
                        }

                        if ( sectors.get(j).s1a == sectors.get(j).s2a &&
                            (sectors.get(j).s1a == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN ||
                             sectors.get(j).s1a == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT) ) {
                            sectors.get(j).intersect = false;
                        }
                        if ( sectors.get(j).s1b == sectors.get(j).s2b &&
                             (sectors.get(j).s1b == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN ||
                            sectors.get(j).s1b == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT) ) {
                            sectors.get(j).intersect = false;
                        }
                    }
                }
            }
        }

        // Search for singly coplanar edges
        for ( i = 0; i < sectors.size(); i++ ) {
            if ( sectors.get(i).intersect && sectors.get(i).s1a == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.ON ) {
                secta = sectors.get(i).secta;
                sectb = sectors.get(i).sectb;
                prevsecta = (secta == 0)?nba.size()-1:secta-1;
                prevsectb = (sectb == 0)?nbb.size()-1:sectb-1;
                newsa = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN;

                for ( j = 0; j < sectors.size(); j++ ) {
                    if ( sectors.get(j).intersect ) {
                        if ( (sectors.get(j).secta == secta) &&
                             (sectors.get(j).sectb == sectb) ) {
                            sectors.get(j).s1a = newsa;
                        }

                        if ( (sectors.get(j).secta == prevsecta) &&
                             (sectors.get(j).sectb == sectb) ) {
                            sectors.get(j).s2a = newsa;
                        }

                        if ( sectors.get(j).s1a == sectors.get(j).s2a &&
                             (sectors.get(j).s1a == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN ||
                              sectors.get(j).s1a == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT) ) {
                            sectors.get(j).intersect = false;
                        }
                    }
                }
            }
            else if ( sectors.get(i).intersect && sectors.get(i).s1b == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.ON ) {
                secta = sectors.get(i).secta;
                sectb = sectors.get(i).sectb;
                prevsecta = (secta == 0)?nba.size()-1:secta-1;
                prevsectb = (sectb == 0)?nbb.size()-1:sectb-1;
                newsb = (op == UNION)?_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT:_PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN;

                for ( j=0; j < sectors.size(); j++ ) {
                    if ( sectors.get(j).intersect ) {
                        if ( (sectors.get(j).secta == secta) &&
                             (sectors.get(j).sectb == sectb) ) {
                            sectors.get(j).s1b = newsb;
                        }

                        if ( (sectors.get(j).secta == secta) &&
                             (sectors.get(j).sectb == prevsectb) ) {
                            sectors.get(j).s2b = newsb;
                        }

                        if ( sectors.get(j).s1b == sectors.get(j).s2b &&
                             (sectors.get(j).s1b == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN ||
                              sectors.get(j).s1b == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT) ) {
                            sectors.get(j).intersect = false;
                        }
                    }
                }
            }
        }
    }

    /**
    Following program [MANT1988].15.12.
    Taking in to account the updated version modifications from
    [.wMANT2008].
    */
    private static void separateEdgeSequence(_PolyhedralBoundedSolidHalfEdge from,
                               _PolyhedralBoundedSolidHalfEdge to,
                               int type,
                               PolyhedralBoundedSolid inSolidA,
                               PolyhedralBoundedSolid inSolidB)
    {
        //-----------------------------------------------------------------
        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 ) {
            System.out.println("      SEPARATEEDGESEQUENCE " + type);
            System.out.println("        From: " + from);
            System.out.println("        To: " + to);
        }

        if ( from == null || to == null ) {
            VSDK.reportMessage(null, VSDK.FATAL_ERROR, "separateEdgeSequence", 
                "Unexpected case: null halfedges!");
        }

        PolyhedralBoundedSolid s;
        s = from.parentLoop.parentFace.parentSolid;

        if ( s != to.parentLoop.parentFace.parentSolid ) {
            VSDK.reportMessage(null, VSDK.FATAL_ERROR, "separateEdgeSequence", 
                "Unexpected case: halfedges on different solids!");
        }

        //-----------------------------------------------------------------
        // Recover from null edges already inserted
        if ( nulledge(from.previous()) && strutnulledge(from.previous()) ) {
            // Look at orientation
            if( from.previous() == from.previous().parentEdge.leftHalf ) {
                from = from.previous().previous();
                System.out.println("* NOT SUPPORTED CASE A ***************");
                //from.startingVertex.debugColor = new ColorRgb(0, 1, 0);
            }
        }
        if ( nulledge(to.previous()) && strutnulledge(to.previous()) ) {
            if ( to.previous() == to.previous().parentEdge.rightHalf ) {
                to = to.previous().previous();
                System.out.println("* NOT SUPPORTED CASE B ***************");
                //to.startingVertex.debugColor = new ColorRgb(0, 0, 1);
            }
        }
        if ( from.startingVertex != to.startingVertex ) {
            if ( from.previous() == to.previous().mirrorHalfEdge() ) {
                from = from.previous();
                System.out.println("* NOT SUPPORTED CASE C ***************");
                //from.startingVertex.debugColor = new ColorRgb(0, 1, 0);
            }
            else if ( from.previous().startingVertex == to.startingVertex ) {
                from = from.previous();
                System.out.println("* NOT SUPPORTED CASE D ***************");
                //from.startingVertex.debugColor = new ColorRgb(0, 1, 0);
            }
            else if ( to.previous().startingVertex == from.startingVertex ) {
                to = to.previous();
                System.out.println("* NOT SUPPORTED CASE E ***************");
                //to.startingVertex.debugColor = new ColorRgb(0, 0, 1);
            }
        }

        //-----------------------------------------------------------------
        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 &&
             (debugFlags & DEBUG_99_SHOWOPERATIONS ) != 0x00 ) {
            System.out.println("       -> LMEV (Separate edge sequence):");
            System.out.println("          . H1: " + to);
            System.out.println("          . H2: " + from);
            //from.startingVertex.debugColor = new ColorRgb(1, 0, 1);
        }

        int id = nextVertexId(inSolidA, inSolidB);

        s.lmev(to, from, id, to.startingVertex.position);

        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 &&
             (debugFlags & DEBUG_99_SHOWOPERATIONS) != 0x00 ) {
            System.out.println("          . New vertex: " + id);
        }

        if ( type == 0 ) {
            sonea.add(new _PolyhedralBoundedSolidSetOperatorNullEdge(from.previous().parentEdge));
        }
        else {
            soneb.add(new _PolyhedralBoundedSolidSetOperatorNullEdge(from.previous().parentEdge));
        }

    }

    /**
    Following program [MANT1988].15.12.
    Taking in to account the updated version modifications from
    [.wMANT2008].
    */
    private static void separateInterior(_PolyhedralBoundedSolidHalfEdge he,
                               int type,
                               boolean orient,
                               PolyhedralBoundedSolid inSolidA,
                               PolyhedralBoundedSolid inSolidB)
    {
        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 ) {
            System.out.println("      SEPARATEINTERIOR " + type);
            System.out.println("        From/To: " + he);
        }

        _PolyhedralBoundedSolidHalfEdge tmp;
/*
        // Recover from null edges inserted
        if ( nulledge(he.previous()) ) {
            if( ((he.previous() == he.previous().parentEdge.rightHalf) && orient) ||
                ((he.previous() == he.previous().parentEdge.leftHalf) && !orient) ) {
                he = he.previous();
            }
        }

        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 &&
             (debugFlags & DEBUG_99_SHOWOPERATIONS ) != 0x00 ) {
                System.out.println("       -> LMEVSTRUT (Separate interior):");
                System.out.println("          . H1: " + he);
        }


*/

//      he = he.mirrorHalfEdge().next();


        int id = nextVertexId(inSolidA, inSolidB);
        he.parentLoop.parentFace.parentSolid.lmev(he, he, id,
            he.startingVertex.position);

        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 &&
             (debugFlags & DEBUG_99_SHOWOPERATIONS) != 0x00 ) {
            System.out.println("          . New vertex: " + id);
            //he.startingVertex.debugColor = new ColorRgb(0, 1, 1);
        }

        // A piece of Black Art: reverse orientation of the null edge
        if ( !orient ) {
            tmp = he.previous().parentEdge.rightHalf;
            he.previous().parentEdge.rightHalf = he.previous().parentEdge.leftHalf;
            he.previous().parentEdge.leftHalf = tmp;
        }

        if ( type == 0 ) {
            sonea.add(new _PolyhedralBoundedSolidSetOperatorNullEdge(he.previous().parentEdge));
        }
        else {
            soneb.add(new _PolyhedralBoundedSolidSetOperatorNullEdge(he.previous().parentEdge));
        }

    }

    /**
    Borrowed from [.wMANT2008].
    */
    private static boolean nulledge(_PolyhedralBoundedSolidHalfEdge he)
    {
        return ( VSDK.vectorDistance(he.startingVertex.position, he.next().startingVertex.position) < VSDK.EPSILON);
    }

    /**
    Borrowed from [.wMANT2008].
    */
    private static boolean strutnulledge(_PolyhedralBoundedSolidHalfEdge he)
    {
        if( he == he.mirrorHalfEdge().next() ||
            he == he.mirrorHalfEdge().previous() ) {
            return true;
        }
        return false;
    }

    /**
    Borrowed from [.wMANT2008].
    */
    private static boolean convexedg(_PolyhedralBoundedSolidHalfEdge he)
    {
        _PolyhedralBoundedSolidHalfEdge h2;
        Vector3D dir, cr;

        h2 = he.next();
        if ( nulledge(he) ) {
            h2 = h2.next();
        }
        dir = h2.startingVertex.position.substract(he.startingVertex.position);
        cr = he.parentLoop.parentFace.containingPlane.getNormal().crossProduct(he.mirrorHalfEdge().parentLoop.parentFace.containingPlane.getNormal());
        if ( cr.length() < VSDK.EPSILON ) {
            return true;
        }
        return (dir.dotProduct(cr) < 0.0);
    }

    /**
    Borrowed from [.wMANT2008].
    */
    private static boolean sectorwide(_PolyhedralBoundedSolidHalfEdge he, int ind)
    {
        Vector3D ref1, ref2, ref12;

        ref1 = he.previous().startingVertex.position.substract(he.startingVertex.position);
        ref2 = he.next().startingVertex.position.substract(he.startingVertex.position);
        ref12 = ref1.crossProduct(ref2);
        if ( ref12.length() < VSDK.EPSILON ) {
            return true;
        }
        return ((ref12.dotProduct(he.parentLoop.parentFace.containingPlane.getNormal()) > 0.0) ? false : true );
    }

    /**
    Borrowed from [.wMANT2008].
    */
    private static boolean getOrientation(
        _PolyhedralBoundedSolidHalfEdge ref,
        _PolyhedralBoundedSolidHalfEdge he1,
        _PolyhedralBoundedSolidHalfEdge he2)
    {
        _PolyhedralBoundedSolidHalfEdge mhe1, mhe2;
        boolean retcode = false;

        mhe1 = he1.mirrorHalfEdge().next();
        mhe2 = he2.mirrorHalfEdge().next();
        if ( mhe1 != he2 && mhe2 == he1 ) {
            retcode = convexedg(he2);
        }
        else {
            retcode = convexedg(he1);
        }
        if( sectorwide(mhe1, 0) && sectorwide(ref, 0) ) {
            retcode = !retcode;
        }

/*
        if ( retcode ) {
            ref.startingVertex.debugColor = new ColorRgb(0, 1, 0);
        }
        else {
            ref.startingVertex.debugColor = new ColorRgb(0, 0, 1);
        }
*/

        return !retcode;
    }

    /**
    Following section [MANT1988].15.6.2. and program [MANT1988].15.11.
    */
    private static void vertexVertexInsertNullEdges(PolyhedralBoundedSolid inSolidA,
                                                    PolyhedralBoundedSolid inSolidB)
    {
        _PolyhedralBoundedSolidHalfEdge ha1 = null;
        _PolyhedralBoundedSolidHalfEdge ha2 = null;
        _PolyhedralBoundedSolidHalfEdge hb1 = null;
        _PolyhedralBoundedSolidHalfEdge hb2 = null;
        int i;

        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 ) {
            System.out.println("   - Null edges insertion:");
        }

        int count = 0;

        for ( i = 0; i < sectors.size(); i++ ) {
            if ( sectors.get(i).intersect ) count++;
        }

        if ( count == 0 && sectors.size() > 0 ) {
            ha1 = nba.get(sectors.get(0).secta).he;
            hb1 = nbb.get(sectors.get(0).sectb).he;
            //System.out.println("**** EMPTY CASE");
        }

        i = 0;
        while ( true ) {
            //-------------------------------------------------------------
            while ( !sectors.get(i).intersect ) {
                i++;
                if ( i == sectors.size() ) {
                    return;
                }
            }
            if ( sectors.get(i).s1a == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT ) {
                ha1 = nba.get(sectors.get(i).secta).he;
            }
            else {
                ha2 = nba.get(sectors.get(i).secta).he;
            }
            if ( sectors.get(i).s1b == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN ) {
                hb1 = nbb.get(sectors.get(i).sectb).he;
                i++;
            }
            else {
                hb2 = nbb.get(sectors.get(i).sectb).he;
                i++;
            }

            //-------------------------------------------------------------
            while ( !sectors.get(i).intersect ) {
                i++;
                if ( i == sectors.size() ) {
                    return;
                }
            }
            if ( sectors.get(i).s1a == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.OUT ) {
                ha1 = nba.get(sectors.get(i).secta).he;
            }
            else {
                ha2 = nba.get(sectors.get(i).secta).he;
            }
            if ( sectors.get(i).s1b == _PolyhedralBoundedSolidSetOperatorSectorClassificationOnSector.IN ) {
                hb1 = nbb.get(sectors.get(i).sectb).he;
                i++;
            }
            else {
                hb2 = nbb.get(sectors.get(i).sectb).he;
                i++;
            }

            //-------------------------------------------------------------
            if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 ) {
                System.out.println("    . Deciding case:");
                System.out.println("      -> Ha1: " + ha1);
                System.out.println("      -> Ha2: " + ha2);
                System.out.println("      -> Hb1: " + hb1);
                System.out.println("      -> Hb2: " + hb2);
            }

            //-------------------------------------------------------------
            if ( ha1 == ha2 ) {
                if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 ) {
                    System.out.println("    . STRUT A CASE");
                }
                separateInterior(ha1, 0, getOrientation(ha1, hb1, hb2), inSolidA, inSolidB);
                separateEdgeSequence(hb1, hb2, 1, inSolidA, inSolidB);
            }
            else if ( hb1 == hb2 ) {
                if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 ) {
                    System.out.println("    . STRUT B CASE");
                }
                separateInterior(hb1, 1, getOrientation(hb1, ha2, ha1), inSolidA, inSolidB);
                separateEdgeSequence(ha2, ha1, 0, inSolidA, inSolidB);
            }
            else {
                if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 ) {
                    System.out.println("    . PARALLEL CASE");
                }
                separateEdgeSequence(ha2, ha1, 0, inSolidA, inSolidB);
                separateEdgeSequence(hb1, hb2, 1, inSolidA, inSolidB);
            }
            if ( i == sectors.size() ) {
                return;
            }
        }
    }

    /**
    Vertex/Vertex classifier for the set operations algorithm (big phase 2).
    Following program [MANT1988].15.6. Similar in structure to program
    [MANT1988].14.3.
    */
    private static void vertexVertexClassify(
        _PolyhedralBoundedSolidVertex va,
        _PolyhedralBoundedSolidVertex vb,
        int op,
        PolyhedralBoundedSolid inSolidA,
        PolyhedralBoundedSolid inSolidB)
    {
        if ( (debugFlags & DEBUG_01_STRUCTURE) != 0x00 ) {
            if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) == 0x00 ) {
                System.out.print("  - ");
            }
            else {
                System.out.print("  * ");
            }
            System.out.print("Vertex of {A} / Vertex of {B} pair: A[" + va.id + "] / B[" + vb.id + "]");
            if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) == 0x00 ) {
                System.out.println(".");
            }
            else {
                System.out.println(" ->");
            }
        }

        vertexVertexGetNeighborhood(va, vb);

        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 ) {
            System.out.println("   - Initial sector/sector intersection candidates:");
            for ( int i = 0; i < sectors.size(); i++ ) {
                System.out.println("    . " + sectors.get(i));
            }
        }

        vertexVertexReclassifyOnSectors(op);

        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 ) {
            System.out.println("   - On sector reclassified:");
            for ( int i = 0; i < sectors.size(); i++ ) {
                System.out.println("    . " + sectors.get(i));
            }
        }

        vertexVertexReclassifyOnEdges(op);

        if ( (debugFlags & DEBUG_04_VERTEXVERTEXCLASIFFIER) != 0x00 ) {
            System.out.println("   - On edges reclassified:");
            for ( int i = 0; i < sectors.size(); i++ ) {
                System.out.println("    . " + sectors.get(i));
            }
        }

        vertexVertexInsertNullEdges(inSolidA, inSolidB);
    }

    /**
    Main control algorithm for the big phases 1 and 2. This calls the
    classifiers for vertex/face and vertex/vertex coincidences found on
    `setOpGenerate`.
    Following section [MANT1988].16.6.1. and program [MANT1988].15.5.
    */
    private static void setOpClassify(int op,
                                      PolyhedralBoundedSolid inSolidA,
                                      PolyhedralBoundedSolid inSolidB)
    {
        int i;

        if ( (debugFlags & DEBUG_01_STRUCTURE) != 0x00 ) {
            System.out.println("- 1.A. ----------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("VERTICES OF {A} TOUCHING FACES ON {B} (sonva array of " + sonva.size() + " matches)");
        }

        for ( i = 0; i < sonva.size(); i++ ) {
            vertexFaceClassify(sonva.get(i).v, sonva.get(i).f, op, 0, inSolidA, inSolidB);
        }

        if ( (debugFlags & DEBUG_01_STRUCTURE) != 0x00 ) {
            System.out.println("- 1.B. ----------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("VERTICES OF {B} TOUCHING FACES ON {A} (sonvb array of " + sonvb.size() + " matches):");
        }

        for ( i = 0; i < sonvb.size(); i++ ) {
            vertexFaceClassify(sonvb.get(i).v, sonvb.get(i).f, op, 1, inSolidA, inSolidB);
        }

        if ( (debugFlags & DEBUG_01_STRUCTURE) != 0x00 ) {
            System.out.println("- 2. ------------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("VERTEX-VERTEX PAIRS (sonvv array of " + sonvv.size() + " pairs):");
        }

        for ( i = 0; i < sonvv.size(); i++ ) {
            vertexVertexClassify(sonvv.get(i).va, sonvv.get(i).vb, op, inSolidA, inSolidB);
        }
    }

    private static void sortNullEdges()
    {
        Collections.sort(sonea);
        Collections.sort(soneb);
    }

    /**
    Following section [MANT1988].15.7. and program [MANT1988].15.13.
    */
    private static _PolyhedralBoundedSolidHalfEdge[]
    canJoin(_PolyhedralBoundedSolidHalfEdge hea,
             _PolyhedralBoundedSolidHalfEdge heb)
    {
        int i, j;
        _PolyhedralBoundedSolidHalfEdge ret[];
        boolean condition1;
        boolean condition2;

        ret = new _PolyhedralBoundedSolidHalfEdge[2];

        for ( i = 0; i < endsa.size(); i++ ) {

            condition1 = neighbor(hea, endsa.get(i));
            condition2 = neighbor(heb, endsb.get(i));

            if ( (debugFlags & DEBUG_05_CONNECT) != 0x00 ) {
                System.out.println("    . Testing for neighborhood A[" +
                   hea.startingVertex.id + 
                   "/" + 
                   hea.next().startingVertex.id + 
                   "] vs. A[" +
                   endsa.get(i).startingVertex.id +
                   "/" + 
                   endsa.get(i).next().startingVertex.id + 
                   "]: " +
                   (condition1?"true":"false") + 
                   " ParentFaces: " + 
                   hea.parentLoop.parentFace.id + 
                   " / " + 
                   endsa.get(i).parentLoop.parentFace.id);

                System.out.println("    . Testing for neighborhood B[" +
                   heb.startingVertex.id + 
                   "/" + 
                   heb.next().startingVertex.id + 
                   "] vs. B[" +
                   endsb.get(i).startingVertex.id +
                   "/" + 
                   endsb.get(i).next().startingVertex.id + 
                   "]: " +
                   (condition2?"true":"false") + 
                   " ParentFaces: " + 
                   heb.parentLoop.parentFace.id + 
                   " / " + 
                   endsb.get(i).parentLoop.parentFace.id);
            }

            if ( condition1 && condition2 ) {
                ret[0] = endsa.get(i);
                ret[1] = endsb.get(i);
                endsa.remove(i);
                endsb.remove(i);
                return ret;
            }
        }
        endsa.add(hea);
        endsb.add(heb);
        return null;
    }

    private static boolean isLooseA(_PolyhedralBoundedSolidHalfEdge he)
    {
        int i;

        for ( i = 0; i < endsa.size(); i++ ) {
            if ( he == endsa.get(i) ) return true;
        }

        return false;
    }

    private static boolean isLooseB(_PolyhedralBoundedSolidHalfEdge he)
    {
        int i;

        for ( i = 0; i < endsb.size(); i++ ) {
            if ( he == endsb.get(i) ) return true;
        }

        return false;
    }

    private static void cutA(_PolyhedralBoundedSolidHalfEdge he)
    {
        PolyhedralBoundedSolid s;
        boolean withDebug = ((debugFlags & DEBUG_99_SHOWOPERATIONS) != 0x0) &&
                            ((debugFlags & DEBUG_05_CONNECT) != 0x00);

        if ( withDebug ) {
            System.out.println("       -> CUTA:");
            System.out.println("          . He: " + he);
        }

        s = he.parentLoop.parentFace.parentSolid;

        if ( he.parentEdge.rightHalf.parentLoop ==
             he.parentEdge.leftHalf.parentLoop ) {
            sonfa.add(he.parentLoop.parentFace);
            s.lkemr(he.parentEdge.rightHalf, he.parentEdge.leftHalf);
        }
        else {
            s.lkef(he.parentEdge.rightHalf, he.parentEdge.leftHalf);
        }
    }

    private static void cutB(_PolyhedralBoundedSolidHalfEdge he)
    {
        PolyhedralBoundedSolid s;
        boolean withDebug = ((debugFlags & DEBUG_99_SHOWOPERATIONS) != 0x0) &&
                            ((debugFlags & DEBUG_05_CONNECT) != 0x00);

        if ( withDebug ) {
            System.out.println("       -> CUTB:");
            System.out.println("          . He: " + he);
        }

        s = he.parentLoop.parentFace.parentSolid;

        if ( he.parentEdge.rightHalf.parentLoop ==
             he.parentEdge.leftHalf.parentLoop ) {
            sonfb.add(he.parentLoop.parentFace);
            s.lkemr(he.parentEdge.rightHalf, he.parentEdge.leftHalf);
        }
        else {
            s.lkef(he.parentEdge.rightHalf, he.parentEdge.leftHalf);
        }
    }

    private static void removeLooseEndsA(_PolyhedralBoundedSolidHalfEdge he)
    {
        _PolyhedralBoundedSolidHalfEdge heStart;
        int i;

        heStart = he;
        do {
            for ( i = 0; i < endsa.size(); i++ ) {
                if ( endsa.get(i) == he ) {
                    endsa.remove(i);
                    break;
                }
            }
            he = he.next();
        } while ( he != heStart );
    }

    private static void removeLooseEndsB(_PolyhedralBoundedSolidHalfEdge he)
    {
        _PolyhedralBoundedSolidHalfEdge heStart;
        int i;

        heStart = he;
        do {
            for ( i = 0; i < endsb.size(); i++ ) {
                if ( endsb.get(i) == he ) {
                    endsb.remove(i);
                    break;
                }
            }
            he = he.next();
        } while ( he != heStart );
    }

    /**
    Neighbor null edges connector for the set operations algorithm
    (big phase 3).
    Following section [MANT1988].15.7. and program [MANT1988].15.14.
    */
    private static void setOpConnect()
    {
        //-----------------------------------------------------------------
        if ( (debugFlags & DEBUG_01_STRUCTURE) != 0x00 ) {
            System.out.println("- 3. ------------------------------------------------------------------------------------------------------------------------------------------------------");
        }

        sortNullEdges();

        int i;

        if ( (debugFlags & DEBUG_05_CONNECT) != 0x00 ) {
            System.out.println("SORTED SET OF " + sonea.size() + " NULL EDGES PAIRS TO BE CONNECTED");
        }

        //-----------------------------------------------------------------
        _PolyhedralBoundedSolidEdge nextedgea, nextedgeb;
        _PolyhedralBoundedSolidHalfEdge h1a = null, h2a = null, h1b = null, h2b = null;
        _PolyhedralBoundedSolidHalfEdge r[];
        boolean withDebug = ((debugFlags & DEBUG_99_SHOWOPERATIONS) != 0x0) &&
                            ((debugFlags & DEBUG_05_CONNECT) != 0x00);

        endsa = new ArrayList<_PolyhedralBoundedSolidHalfEdge>();
        endsb = new ArrayList<_PolyhedralBoundedSolidHalfEdge>();

        sonfa = new ArrayList<_PolyhedralBoundedSolidFace>();
        sonfb = new ArrayList<_PolyhedralBoundedSolidFace>();
        int j;

        if ( sonea.size() != soneb.size() ) {
            System.out.println("**** Not paired null edges!");
        }

        for ( i = 0; i < sonea.size() && i < soneb.size(); i++ ) {
            _PolyhedralBoundedSolidHalfEdge ha, ham;
            _PolyhedralBoundedSolidHalfEdge hb, hbm;
            _PolyhedralBoundedSolidHalfEdge tmp;

            ha = sonea.get(i).e.rightHalf;
            ham = sonea.get(i).e.leftHalf;
            hb = soneb.get(i).e.rightHalf;
            hbm = soneb.get(i).e.leftHalf;

            //-----------------------------------------------------------------
            if ( (debugFlags & DEBUG_05_CONNECT) != 0x00 ) {
                System.out.println("  - " + (endsa.size()+endsb.size()) + 
                    " = " + endsa.size() + "+" + endsb.size() + 
                    " loose ends before processing pair [" + i + "]:");

                for ( j = 0; j < endsa.size(); j++ ) {
                    _PolyhedralBoundedSolidHalfEdge hat, hbt;
                    hat = endsa.get(j);
                    hbt = endsb.get(j);
                    System.out.println("    . [" + j + "]: He(A): " +
                                       hat.startingVertex.id +
                                       "/" + hat.next().startingVertex.id + 
                                       " | He(B): " + hbt.startingVertex.id +
                                       "/" + hbt.next().startingVertex.id);
                }

                if ( ha.startingVertex.id > ham.startingVertex.id ) {
                    // Force halfedges to go from old vertex to new vertex,
                    // that is, from IN to OUT direction.
                    System.out.println("********* FORCING ORDER!");
                }

                System.out.println("  - Processing pair [" + i + "]: "+
                    "He(A1): " + ha.startingVertex.id + "/" + ha.next().startingVertex.id + 
                    " He(A2): " + ham.startingVertex.id + "/" + ham.next().startingVertex.id + 
                    " He(B1): " + hb.startingVertex.id + "/" + hb.next().startingVertex.id +
                    " He(B2): " + hbm.startingVertex.id + "/" + hbm.next().startingVertex.id);
            }

            //-----------------------------------------------------------------
            // This assumes that for each null edge on solid a there is
            // another on solid b.
            boolean swap = false;
            nextedgea = sonea.get(i).e;
            nextedgeb = soneb.get(i).e;
            h1a = null;
            h2a = null;
            h1b = null;
            h2b = null;

            //----
            // Force correct order of neighborhoods: they always goes from
            // IN to OUT.
            if ( ha.startingVertex.id > ham.startingVertex.id ) {
                tmp = nextedgea.rightHalf;
                nextedgea.rightHalf = nextedgea.leftHalf;
                nextedgea.leftHalf = tmp;
                if ( hb.startingVertex.id > hbm.startingVertex.id ) {
                    tmp = nextedgeb.rightHalf;
                    nextedgeb.rightHalf = nextedgeb.leftHalf;
                    nextedgeb.leftHalf = tmp;
                }
            }
            //----

            r = canJoin(nextedgea.rightHalf, nextedgeb.leftHalf);
            if ( r != null ) {
                h1a = r[0];
                h2b = r[1];
                join(h1a, nextedgea.rightHalf, withDebug);
                removeLooseEndsA(h1a);
                if ( !isLooseA(h1a.mirrorHalfEdge()) ) {
                    cutA(h1a);
                }
                join(h2b, nextedgeb.leftHalf, withDebug);
                removeLooseEndsB(h2b);
                if ( !isLooseB(h2b.mirrorHalfEdge()) ) {
                    cutB(h2b);
                }
            }

            r = canJoin(nextedgea.leftHalf, nextedgeb.rightHalf);
            if ( r != null ) {
                h2a = r[0];
                h1b = r[1];
                join(h2a, nextedgea.leftHalf, withDebug);
                removeLooseEndsA(h2a);
                if ( !isLooseA(h2a.mirrorHalfEdge()) ) {
                    cutA(h2a);
                }
                join(h1b, nextedgeb.rightHalf, withDebug);
                removeLooseEndsB(h1b);
                if ( !isLooseB(h1b.mirrorHalfEdge()) ) {
                    cutB(h1b);
                }
            }

            if ( h1a != null && h1b != null && h2a != null && h2b != null ) {
                cutA(nextedgea.rightHalf);
                cutB(nextedgeb.rightHalf);
            }
        }

        if ( (debugFlags & DEBUG_05_CONNECT) != 0x00 ) {
            System.out.println("  . Pending null edges to connect:");
            for ( i = 0; i < endsa.size(); i++ ) {
                System.out.println("    . A[" + (i+1) + "]: " + endsa.get(i));
            }
            for ( i = 0; i < endsb.size(); i++ ) {
                System.out.println("    . B[" + (i+1) + "]: " + endsb.get(i));
            }
        }

    }

    /**
    Answer integrator for the set operations algorithm (big phase 4).
    Following program [MANT1988].15.15.
    */
    private static void setOpFinish(
        PolyhedralBoundedSolid inSolidA,
        PolyhedralBoundedSolid inSolidB,
        PolyhedralBoundedSolid outRes,
        int op
    )
    {
        int i, j, inda, indb;
        _PolyhedralBoundedSolidFace f;

        if ( (debugFlags & DEBUG_01_STRUCTURE) != 0x00 ) {
            System.out.println("- 4. ------------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("setOpFinish");
        }

        if ( (debugFlags & DEBUG_06_FINISH) != 0x00 ) {
            System.out.println("TESTING FINISH: " + sonfa.size());
        }

        inda = (op == INTERSECTION) ? sonfa.size() : 0;
        indb = (op == UNION) ? 0 : sonfb.size();

        int oldsize = sonfa.size();

        for ( i = 0; i < oldsize; i++ ) {
            f = inSolidA.lmfkrh(sonfa.get(i).boundariesList.get(1),
                                inSolidA.getMaxFaceId()+1);
            sonfa.add(f);

            f = inSolidB.lmfkrh(sonfb.get(i).boundariesList.get(1),
                                inSolidB.getMaxFaceId()+1);
            sonfb.add(f);
        }

        if ( op == DIFFERENCE ) {
            inSolidB.revert();
        }

        for ( i = 0; i < oldsize; i++ ) {
            movefac(sonfa.get(i+inda), outRes);
            movefac(sonfb.get(i+indb), outRes);
        }

        cleanup(outRes);

        for ( i = 0; i < oldsize; i++ ) {
            outRes.lkfmrh(sonfa.get(i+inda), sonfb.get(i+indb));
            outRes.loopGlue(sonfa.get(i+inda).id);
        }
        outRes.compactIds();
    }

    public static PolyhedralBoundedSolid setOp(
        PolyhedralBoundedSolid inSolidA,
        PolyhedralBoundedSolid inSolidB,
        int op)
    {
        return setOp(inSolidA, inSolidB, op, false);
    }

    /**
    */
    private static void debugSolid(PolyhedralBoundedSolid solid, String pattern)
    {
        System.out.println("**** DEBUGGING SOLID INFORMATION WRITEN TO FILES " +
            pattern + " ****");
        try {
            File fd = new File(pattern + ".txt");
            FileOutputStream fos = new FileOutputStream(fd);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            if ( offlineRenderer != null ) {
                offlineRenderer.execute(solid, pattern + ".png");
            }

            PersistenceElement.writeAsciiLine(bos, solid.toString());
            bos.close();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
    Following program [MANT1988].15.1.
    */
    public static PolyhedralBoundedSolid setOp(
        PolyhedralBoundedSolid inSolidA,
        PolyhedralBoundedSolid inSolidB,
        int op, boolean withDebug)
    {
        if ( withDebug ) {
            debugFlags = 0
              | DEBUG_01_STRUCTURE
              | DEBUG_02_GENERATOR
              | DEBUG_03_VERTEXFACECLASIFFIER
              | DEBUG_04_VERTEXVERTEXCLASIFFIER
              | DEBUG_05_CONNECT
              | DEBUG_06_FINISH
              | DEBUG_99_SHOWOPERATIONS
              ;
            offlineRenderer = PolyhedralBoundedSolidDebugger.createOfflineRenderer();
        }
        else {
            debugFlags = 0;
        }

        if ( (debugFlags & DEBUG_01_STRUCTURE) != 0x00 ) {
            System.out.println("= [START OF SETOP REPORT] =================================================================================================================================");
            System.out.println("Dumping debug log for PolyhedralBoundedSolidSetOperator.setOp.");
            System.out.println("The algorithm structure is:");
            System.out.println("  0. Calculate vertex/face and vertex/vertex crossings.");
            System.out.println("  1. Classify and split for vertex/face cases.");
            System.out.println("  2. Classify and split for vertex/vertex cases.");
            System.out.println("  3. Connect.");
            System.out.println("  4. Finish.");
        }

        //-----------------------------------------------------------------
        PolyhedralBoundedSolid res = new PolyhedralBoundedSolid();

        sonea = new ArrayList<_PolyhedralBoundedSolidSetOperatorNullEdge>();
        soneb = new ArrayList<_PolyhedralBoundedSolidSetOperatorNullEdge>();

        //-----------------------------------------------------------------
        if ( withDebug ) {
            debugSolid(inSolidA, "outputA_stage00");
            debugSolid(inSolidB, "outputB_stage00");
        }

        inSolidA.compactIds();
        inSolidB.compactIds();
        inSolidA.validateModel();
        inSolidB.validateModel();
        inSolidA.maximizeFaces();
        inSolidB.maximizeFaces();
        inSolidA.validateModel();
        inSolidB.validateModel();
        inSolidA.compactIds();
        inSolidB.compactIds();
        updmaxnames(inSolidB, inSolidA);

        if ( withDebug ) {
            debugSolid(inSolidA, "outputA_stage01");
            debugSolid(inSolidB, "outputB_stage01");
        }

        setOpGenerate(inSolidA, inSolidB);

        if ( withDebug ) {
            debugSolid(inSolidA, "outputA_stage02");
            debugSolid(inSolidB, "outputB_stage02");
        }

        setOpClassify(op, inSolidA, inSolidB);

        if ( withDebug ) {
            debugSolid(inSolidA, "outputA_stage03");
            debugSolid(inSolidB, "outputB_stage03");
        }

        if ( sonea.isEmpty() && sonvv.isEmpty() ) {
            // No intersections found
            if ( op == INTERSECTION ) {
                return res;
            }
            else if ( op == DIFFERENCE ) {
                res.merge(inSolidA);
                return res;
            }
            else if ( op == UNION ) {
                res.merge(inSolidA);
                res.merge(inSolidB);
                return res;
            }
        }

        if ( withDebug ) {
            debugSolid(inSolidA, "outputA_stage04");
            debugSolid(inSolidB, "outputB_stage04");
        }

        setOpConnect();

        if ( withDebug ) {
            debugSolid(inSolidA, "outputA_stage05");
            debugSolid(inSolidB, "outputB_stage05");
        }

        setOpFinish(inSolidA, inSolidB, res, op);

        if ( withDebug ) {
            debugSolid(inSolidA, "outputA_stage06");
            debugSolid(inSolidB, "outputB_stage06");
            debugSolid(res, "outputR_stage06");
        }

        res.validateModel();
        res.compactIds();
        res.maximizeFaces();
        res.compactIds();
        res.validateModel();

        if ( withDebug ) {
            debugSolid(res, "outputR_stage07");
        }

        if ( (debugFlags & DEBUG_01_STRUCTURE) != 0x00 ) {
            System.out.println("= [END OF SETOP REPORT] ===================================================================================================================================");
        }

        return res;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
