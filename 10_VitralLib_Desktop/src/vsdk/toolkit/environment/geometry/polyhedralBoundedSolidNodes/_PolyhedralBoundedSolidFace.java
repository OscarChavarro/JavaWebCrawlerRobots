//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - November 18 2006 - Oscar Chavarro: Original base version              =
//= - January 3 2007 - Oscar Chavarro: First phase implementation           =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [GLAS1989] Glassner, Andrew. "An introduction to ray tracing",          =
//=     Academic Press, 1989.                                               =
//= [MANT1988] Mantyla Martti. "An Introduction To Solid Modeling",         =
//=     Computer Science Press, 1988.                                       =
//===========================================================================
package vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes;

import java.util.ArrayList;

import vsdk.toolkit.common.CircularDoubleLinkedList;
import vsdk.toolkit.common.FundamentalEntity;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.ArrayListOfDoubles;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.PolyhedralBoundedSolid;
import vsdk.toolkit.environment.geometry.InfinitePlane;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.processing.ComputationalGeometry;

/**
As noted in [MANT1988].10.2.1, class `_PolyhedralBoundedSolidFace` represents
one planar face of the polyhedron represented by the half-edge data
structure in a `PolyhedralBoundedSolid`. A face is defined as a planar
polygon whose interior is connected, considering that could be convex or
concave, with or without holes (but without "islands", in which case there
are more than one polygon), and based in this, a polygon can have more than
one polygonal boundary.

Note that in current implementation, the first loop in the list of boundaries
is the outer boundary, and the others are "rings" or hole loops.

Note that in the sake of simplify and eficiency current programming 
implementation of this class exhibit public access attributes. It is important
to note that those attributes will only be accessed directly from related 
classes in the same package
(vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes) and
from methods in the `PolyhedralBoundedSolid` class, and that they should
not be used from outer classes.
*/
public class _PolyhedralBoundedSolidFace extends FundamentalEntity {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20061118L;

    /// Defined as presented in [MANT1988].10.2.1
    public int id;

    /// Defined as presented in [MANT1988].10.2.1
    public PolyhedralBoundedSolid parentSolid;

    /// Each face should have at least one loop, corresponding to the
    /// external boundary. Each subsequent loop will be interpreted as a ring.
    /// Defined as presented in [MANT1988].10.2.1
    public CircularDoubleLinkedList<_PolyhedralBoundedSolidLoop> boundariesList;
    /// Defined as presented in [MANT1988].10.2.1
    public InfinitePlane containingPlane;

    /// Used by method testPointInside. Normally null, not null value when
    /// a point intersected an edge. Following problem [MANT1988].13.3. and
    /// variable `hitthe`  from program [MANT1988].15.3.
    public _PolyhedralBoundedSolidHalfEdge lastIntersectedHalfedge;

    /// Used by method testPointInside. Normally null, not null value when
    /// a point intersected a vertex.  Following problem [MANT1988].13.3. and
    /// variable `hitvertex`  from program [MANT1988].15.3.
    public _PolyhedralBoundedSolidVertex lastIntersectedVertex;

    //=================================================================

    public _PolyhedralBoundedSolidFace(PolyhedralBoundedSolid parent, int id)
    {
        init(parent, id);
    }

    private void init(PolyhedralBoundedSolid parent, int id)
    {
        this.id = id;
        parentSolid = parent;
        parentSolid.polygonsList.add(this);
        boundariesList =
            new CircularDoubleLinkedList<_PolyhedralBoundedSolidLoop>();
        lastIntersectedHalfedge = null;
    }

    /**
    Find the halfedge from vertex `vn1` to vertex `vn2`. 
    Returns null if halfedge not found, or current founded halfedge otherwise.
    Build based over function `fhe` in program [MANT1988].11.9.
    @param vn1
    @param vn2
    @return requested half edge
    */
    public _PolyhedralBoundedSolidHalfEdge findHalfEdge(int vn1, int vn2)
    {
        _PolyhedralBoundedSolidLoop loop;
        _PolyhedralBoundedSolidHalfEdge he;
        int i;

        for ( i = 0; i < boundariesList.size(); i++ ) {
            loop = boundariesList.get(i);
            he = loop.halfEdgeVertices(vn1, vn2);
            if ( he != null ) {
                return he;
            }
        }
        return null;
    }

    /**
    Find the first halfedge originating from vertex `vn1`.
    Returns null if halfedge not found, or current founded halfedge otherwise.
    @param vn1
    @return requested halfedge
    */
    public _PolyhedralBoundedSolidHalfEdge findHalfEdge(int vn1)
    {
        _PolyhedralBoundedSolidLoop loop;
        _PolyhedralBoundedSolidHalfEdge he;
        int i;

        for ( i = 0; i < boundariesList.size(); i++ ) {
            loop = boundariesList.get(i);
            he = loop.firstHalfEdgeAtVertex(vn1);
            if ( he != null ) {
                return he;
            }
        }
        return null;
    }

    /**
    PRE: current face points must be all co-planar. Previous solid validation
    should be made!
    POST: current face contains a plane containing the face.
    @return true if current face is in a situation where containing plane
    could be calculated.
    */
    public boolean
    calculatePlane()
    {
        //return calculatePlaneByVertexSequenceNormalCrossProduct();
        return calculatePlaneByCorner(0.1);//If we assume meters, 0.01 is 1cm.
    }

    /**
    Current implementation takes in to account only the first loop.
    @return true if current face is in a situation where containing plane
    could be calculated.
    */
    private boolean calculatePlaneByCorner (double tolerance) {
        _PolyhedralBoundedSolidLoop loop;
        _PolyhedralBoundedSolidHalfEdge he, heStart, heInferior;
        Vector3D p0 = new Vector3D ();
        Vector3D p1;
        Vector3D a = new Vector3D ();
        Vector3D b = new Vector3D ();
        Vector3D n1;
        Vector3D temp = new Vector3D ();
        boolean readyVecA, readyVecB;
        double dotP;
        //domPlane: 1=xy, 2=xz, 3=yz
        byte domPlane;
        Vector3D vPrev = new Vector3D ();
        Vector3D vNext = new Vector3D ();

        if ( boundariesList.size () < 1 ) {
            return true;
        }
        loop = boundariesList.get (0);
        he = loop.boundaryStartHalfEdge;
        if ( he == null ) {
            // Loop without starting halfedge
            return true;
        }
        heStart = he;

        // Calculate temporal normal (the sense may not be the correct), to find
        // the dominant plane.
        // The superior point is calculated too.
        readyVecA = false;
        readyVecB = false;

        do {
            //Obtain any two non collinear vectors
            p0 = he.startingVertex.position;
            p1 = he.next ().startingVertex.position;
            temp.substract (p1, p0);
            if ( !readyVecA ) {
                if ( temp.length () > tolerance ) {
                    a.clone (temp);
                    a.normalize ();
                    readyVecA = true;
                }
            } else if ( !readyVecB ) {
                if ( temp.length () > tolerance ) {
                    temp.normalize ();
                    dotP = Math.abs (temp.dotProduct (a));
                    if ( dotP < 1 - VSDK.EPSILON * 1000 ) {
                        b.clone (temp);
                        readyVecB = true;
                    }
                }
            }
            he = he.next ();
        } while ( he != heStart && !readyVecB );
        if ( (a.length () == 0) || (b.length () == 0) ) {
            // Any vector is zero.
            return true;
        }
        n1 = a.crossProduct (b); //Temporal normal.
        // Special case: triangle
        if ( loop.halfEdgesList.size () == 3 ) {
            n1.normalize ();
            containingPlane = new InfinitePlane (n1, p0);
            return false;
        }
        //Test for dominant plane.
        //domPlane: 1=xy, 2=xz, 3=yz
        if ( Math.abs(n1.z) > Math.abs(n1.x) ) {
            if ( Math.abs(n1.z) > Math.abs(n1.y) ) {
                domPlane = 1;
            } else {
                domPlane = 2;
            }
        } else if ( Math.abs(n1.x) > Math.abs(n1.y) ) {
            domPlane = 3;
        } else {
            domPlane = 2;
        }
        //Find inferior point of face, given the dominant plane.
        he = loop.boundaryStartHalfEdge;
        heInferior = he;
        he = he.next ();
        while ( he != heStart ) {
            p0 = he.startingVertex.position;
            switch ( domPlane ) {
                case 1: //xy plane
                    if ( p0.y < heInferior.startingVertex.position.y ) {
                        heInferior = he;
                    }
                    break;
                case 2: //xz plane
                    if ( p0.z < heInferior.startingVertex.position.z ) {
                        heInferior = he;
                    }
                    break;
                case 3: //yz plane
                    if ( p0.z < heInferior.startingVertex.position.z ) {
                        heInferior = he;
                    }
                    break;
            }
            he = he.next ();
        }
        // Find next and previous vectors from inferior point(previously found) 
        // to calculate the plane.
        he = heInferior;
        p0 = heInferior.startingVertex.position;
        do {
            he = he.next ();
            p1 = he.startingVertex.position;
            vNext.substract (p1, p0);
            if ( vNext.length () > tolerance ) {
                vNext.normalize ();
                break;
            }
        } while ( he != heInferior );
        he = heInferior;
        do {
            // The previous vector should not be collinear with the first one found.
            he = he.previous ();
            p1 = he.startingVertex.position;
            vPrev.substract (p1, p0);
            if ( vPrev.length () > tolerance ) {
                vPrev.normalize ();
                dotP = Math.abs (vPrev.dotProduct (vNext));
                if ( dotP < 1 - VSDK.EPSILON * 1000 ) {
                    break;
                }
            }
        } while ( he != heInferior );
        n1 = vNext.crossProduct (vPrev);
        containingPlane = new InfinitePlane (n1, p0);
        return false;
    }

    /**
    Current implementation takes in to account only the first loop. This method
    has proved to be inefficient.
    @return true if current face is in a situation where containing plane
    could be calculated.
    */
    private boolean calculatePlaneByVertexSequenceNormalCrossProduct() {
        if ( boundariesList.size() < 1 ) {
            return true;
        }
        _PolyhedralBoundedSolidLoop loop;
        _PolyhedralBoundedSolidHalfEdge he, heStart;
        loop = boundariesList.get(0);
        he = loop.boundaryStartHalfEdge;
        if ( he == null ) {
            // Loop without starting halfedge
            return true;
        }
        heStart = he;
        boolean colinearPoints = true;
        do {
            // This is only considering the first three vertices, and not taking
            // in to account the possible case of too close vertices. Should be
            // replaced to consider the full vertices set.
            //- Do normal estimation on a three set of points -----------------
            Vector3D p0;
            Vector3D p1;
            Vector3D p2;
            Vector3D a, b;
            Vector3D n;
            p0 = he.startingVertex.position;
            p1 = he.next().startingVertex.position;
            p2 = he.next().next().startingVertex.position;
            a = p1.substract(p0);
            a.normalize();
            b = p2.substract(p0);
            b.normalize();
            n = a.crossProduct(b);
            // Iterate if the given three vertices are colinear
            double angleInDegrees;
            angleInDegrees = Math.toDegrees(Math.acos(a.dotProduct(b)));
            
            /*
            System.out.println("  - Face plane determination angle: " +
            VSDK.formatDouble(angleInDegrees));
            if ( angleInDegrees < 1.0 ) {
            System.out.println("  * Need to fix face");
            }
             */
            boolean firstTimer = true;
            // In a given polygon, pass 1 for angle correction seeks big
            // angles, leading to less error on normal plane calculation
            /*
            while ( angleInDegrees < 30.0 && he.next().next() != heStart &&
                he.next() != heStart ) {
                he = he.next();
                p0 = he.startingVertex.position;
                p1 = he.next().startingVertex.position;
                p2 = he.next().next().startingVertex.position;

                a = p1.substract(p0);
                a.normalize();
                b = p2.substract(p0);
                b.normalize();
                n = a.crossProduct(b);
                angleInDegrees = Math.acos(a.dotProduct(b));
                //System.out.println("  . Big iteration angle: " + angleInDegrees);
                firstTimer = false;
            }
            */
            
            // This code will fail for polygons with all angles under 1 degre,
            // for example a polygon representing a circle with more than 
            // 360 segments
            while ( angleInDegrees < 1.0 && he.next().next() != heStart &&
                he.next() != heStart ) {
                he = he.next();
                p0 = he.startingVertex.position;
                p1 = he.next().startingVertex.position;
                p2 = he.next().next().startingVertex.position;

                a = p1.substract(p0);
                a.normalize();
                b = p2.substract(p0);
                b.normalize();
                n = a.crossProduct(b);
                angleInDegrees = Math.acos(a.dotProduct(b));
                //System.out.println("  . Iteration angle: " + angleInDegrees);
                firstTimer = false;
            }
            
            //if ( !firstTimer ) {
            //    System.out.println("  * fixed");
            //}
            
            if ( angleInDegrees < 1.0 ) {
                //VSDK.reportMessage(this, VSDK.WARNING, "calculatePlane", 
                //    "Face is colinear degenerate case!");
                containingPlane = null;
                return false;
            }
            // Do plane
            if ( n.length() < VSDK.EPSILON ||
                a.length() < VSDK.EPSILON ||
                b.length() < VSDK.EPSILON ) {
                he = he.next();
                continue;
            }
            else {
                colinearPoints = false;
            }
            n.normalize();
            containingPlane = new InfinitePlane(n, p0);
            //- Determine if p1 region is convex or concave -------------------
            Vector3D middle = a.add(b);
            Vector3D testPoint;
            middle.normalize();
            middle = middle.multiply(10.0*VSDK.EPSILON);
            testPoint = p0.add(middle);
            //- If concave, swap normal direction -----------------------------
            if ( testPointInside(testPoint, VSDK.EPSILON) == Geometry.OUTSIDE ) {
                n = n.multiply(-1.0);
            }
            containingPlane = new InfinitePlane(n, p0);
            he = he.next();
        }
        while ( he != heStart && colinearPoints );
        /*
        do {
        he = he.next();
        if ( he == null ) {
        // Loop is not closed!
        break;
        }
        // ?
        } while( he != heStart );
         */
        return false;
    }

    /**
    @coord: 1 means drop x, 2 means drop y and 3 means drop z
    */
    private void dropCoordinate(Vector3D in, Vector3D out, int coord)
    {
        out.z = 0;

        switch ( coord ) {
          case 1:
            // Drop X
            out.x = in.y;
            out.y = in.z;
            break;
          case 2:
            // Drop Y
            out.x = in.x;
            out.y = in.z;
            break;
          case 3: default:
            // Drop Z
            out.x = in.x;
            out.y = in.y;
            break;
        }
    }

    /**
    Given a point p in the containing plane of this face, the method returns:
    @param p
    @param tolerance
    @return Geometry.OUTSIDE if point is outside polygon, Geometry.LIMIT if
    its in the polygon border, Geometry.INSIDE if point is inside border.
    PRE:
    - Polygon is planar
    - Point p is in the containing plane
    The structure of this algorithm follows the one outlined in
    [GLAS1989].2.3.2. with a little variation in the handlig of `sh`
    which allows this code to manage internal loops.

    Functionally, this is equivalent to procedure `contfv` proposed at
    problem [MANT1988].13.3. For [MANT1988] based algorithms compatibility
    this method supports the generation oof extra information, and
    stores .

    When point is outside the border of the polygon, this method writes
    null values to attributes `lastIntersectedHalfedge` and
    `lastIntersectedVertex`. When the point is on the border of the polygon,
    it can be over a vertex or over an edge.  If it is on a vertex, the
    intersecting vertex is referenced on attribute `lastIntersectedVertex`
    and `lastIntersectedHalfedge` is leaved null. Otherwise, when point
    intersects an edge, the intersected edge is referenced at
    `lastIntersectedHalfedge`, and `lastIntersectedVertex` is leaved null.

    Altough should be seldom of a problem, note the decribed mechanism for
    quering extra border intersection is not re-entrant (thread safe) for
    current face.
    */
    public int
    testPointInside(Vector3D p, double tolerance)
    {
        int nc; // Number of crossings
        int sh; // Sign holder for vertex crossings
        int nsh; // Next sign holder for vertex crossings

        lastIntersectedHalfedge = null;
        lastIntersectedVertex = null;

        //-----------------------------------------------------------------
        //- 1. For all vertices in face, project them in to dominant
        //- coordinate's plane
        ArrayListOfDoubles polygon2Du = new ArrayListOfDoubles(100);
        ArrayListOfDoubles polygon2Dv = new ArrayListOfDoubles(100);
        ArrayList<_PolyhedralBoundedSolidHalfEdge> polygon2Dh;
        ArrayList<_PolyhedralBoundedSolidVertex> polygon2Dvv;
        double u, v;
        Vector3D projectedPoint = new Vector3D();
        int dominantCoordinate;
        int i;
        Vector3D n;

        polygon2Dh = new ArrayList<_PolyhedralBoundedSolidHalfEdge>();
        polygon2Dvv = new ArrayList<_PolyhedralBoundedSolidVertex>();
        n = containingPlane.getNormal();

        if ( Math.abs(n.x) >= Math.abs(n.y) &&
             Math.abs(n.x) >= Math.abs(n.z) ) {
            dominantCoordinate = 1;
        }
        else if ( Math.abs(n.y) >= Math.abs(n.x) &&
                  Math.abs(n.y) >= Math.abs(n.z) ) {
            dominantCoordinate = 2;
        }
        else {
            dominantCoordinate = 3;
        }

        _PolyhedralBoundedSolidHalfEdge he;

        for ( i = 0; i < boundariesList.size(); i++ ) {
            _PolyhedralBoundedSolidLoop loop;
            _PolyhedralBoundedSolidHalfEdge heStart, heOld;

            loop = boundariesList.get(i);
            he = loop.boundaryStartHalfEdge;            
            if ( he == null ) {
                // Loop without starting halfedge
                return Geometry.OUTSIDE;
            }
            heStart = he;
            do {
                if ( VSDK.vectorDistance(p, he.startingVertex.position) 
                     < 2*tolerance ) {
                    lastIntersectedVertex = he.startingVertex;
                    return Geometry.LIMIT;
                }

                dropCoordinate(he.startingVertex.position, projectedPoint,
                               dominantCoordinate);
                polygon2Du.add(projectedPoint.x);
                polygon2Dv.add(projectedPoint.y);
                polygon2Dh.add(he);
                heOld = he;
                polygon2Dvv.add(he.startingVertex);
                he = he.next();
                if ( he == null ) {
                    // Loop is not closed!
                    return Geometry.OUTSIDE;
                }
                dropCoordinate(he.startingVertex.position, projectedPoint,
                               dominantCoordinate);
                polygon2Du.add(projectedPoint.x);
                polygon2Dv.add(projectedPoint.y);
                polygon2Dvv.add(he.startingVertex);

                if ( VSDK.vectorDistance(p, he.startingVertex.position) 
                     < 2*tolerance ) {
                    lastIntersectedVertex = he.startingVertex;
                    return Geometry.LIMIT;
                }

                if ( ComputationalGeometry.lineSegmentContainmentTest(
                         heOld.startingVertex.position,
                         he.startingVertex.position, p, tolerance
                     ) == Geometry.LIMIT ) {
                    lastIntersectedHalfedge = heOld;
                    return Geometry.LIMIT;
                }
            } while( he != heStart );
        }

        dropCoordinate(p, projectedPoint, dominantCoordinate);
        u = projectedPoint.x;
        v = projectedPoint.y;

        //-----------------------------------------------------------------
        //- 2. Translate the 2D polygon such that the intersection point is
        //- in the origin
        for ( i = 0; i < polygon2Du.size(); i++ ) {
            double val;
            val = polygon2Du.get(i) - u;
            polygon2Du.set(i, val);
            val = polygon2Dv.get(i) - v;
            polygon2Dv.set(i, val);
        }
        nc = 0;

        //-----------------------------------------------------------------
        //- 3. Iterate edges
        double ua, va, ub, vb;
        _PolyhedralBoundedSolidVertex vva, vvb;

        for ( i = 0; i < polygon2Du.size() - 1; i += 2 ) {
            // This iteration tests the line segment (ua, va) - (ub, vb)
            ua = polygon2Du.get(i);
            va = polygon2Dv.get(i);
            ub = polygon2Du.get(i+1);
            vb = polygon2Dv.get(i+1);
            vva = polygon2Dvv.get(i);
            vvb = polygon2Dvv.get(i+1);

            // Note that testing line is (y = 0), so "segment crossed" can be
            // detected as a sign change in the v dimension.

            // First, calculate the va and vb signs in sh and nsh respectively
            if ( va < 0 ) {
                sh = -1;
            }
            else {
                sh = 1;
            }
            if ( vb < 0 ) {
                nsh = -1;
            }
            else {
                nsh = 1;
            }

            // If a sign change in the v dimension occurs, then report cross...
            if ( sh != nsh ) {
                // But taking into account the special case crossing occurring
                // over a vertex
                if ( ua >= 0 && ub >= 0 ) {
                    nc++;
                }
                else if ( ua >= 0 || ub >= 0 ) {
                    if ( ua - va*(ub-ua)/(vb - va) > 0 ) {
                        nc++;
                    }
                }
            }

        }

        if ( (nc % 2) == 1 ) {
            return Geometry.INSIDE;
        }

        return Geometry.OUTSIDE;
    }

    /**
    Current implementation only takes into account the containing plane.
    @param c
    @return 1 if this face is visible from camera c, -1 if is not visible and
    0 if is tangent to it.
    \todo : generalize to plane. This is returning "1" in cases where should
    return "-1".
    */
    public int isVisibleFrom(Camera c)
    {
        Vector3D iv = new Vector3D(1, 0, 0);
        Vector3D viewingVector;
        viewingVector = c.getRotation().multiply(iv);
        Vector3D n = containingPlane.getNormal();
        Vector3D cp, t;
        n.normalize();
        double dot;
        int i;
        Vector3D p;

        if ( c.getProjectionMode() == Camera.PROJECTION_MODE_ORTHOGONAL ) {
            viewingVector.normalize();
            dot = n.dotProduct(viewingVector);
            if ( dot > VSDK.EPSILON ) {
                return -1;
            }
            else if ( dot > VSDK.EPSILON ) {
                return 1;
            }
            else return 0;
        }
        else {
            cp = c.getPosition();
            _PolyhedralBoundedSolidLoop l;
            for ( i = 0; i < boundariesList.size(); i++ ) {
                //System.out.println("  - Testing boundary " + i + " of " + boundariesList.size());
                l = boundariesList.get(i);
                _PolyhedralBoundedSolidHalfEdge he, heStart;

                he = l.boundaryStartHalfEdge;
                heStart = he;
                do {
                    // Logic
                    he = he.next();
                    if ( he == null ) {
                        // Loop is not closed!
                        break;
                    }

                    // Calculate containing plane equation for current edge
                    p = he.startingVertex.position;
                    //System.out.println("    . Testing point " + p);
                    t = p.substract(cp);
                    t = t.multiply(-1);
                    t.normalize();
                    //System.out.println("     -> Viewing point " + t);
                    if ( t.dotProduct(n) > 0.0 ) {
                        return 1;
                        //System.out.println("  * Face in");
                    }
                } while( he != heStart );
            }
            //System.out.println("  * Face out");
            return -1;
        }
    }

    public void revert()
    {
        int i;

        for ( i = 0; i < boundariesList.size(); i++ ) {
            boundariesList.get(i).revert();
        }
    }

    @Override
    public String toString()
    {
        String msg;

        msg = "Face id [" + id + "], " + boundariesList.size() + " loops.";

        return msg;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
