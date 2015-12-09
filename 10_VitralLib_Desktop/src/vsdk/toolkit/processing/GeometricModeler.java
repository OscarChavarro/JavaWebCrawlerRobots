//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - September 23 2007 - Oscar Chavarro: Original base version             =
//===========================================================================

package vsdk.toolkit.processing;

// Java classes
import java.util.ArrayList;

// VitralSDK classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.environment.geometry.InfinitePlane;
import vsdk.toolkit.environment.geometry.ParametricCurve;
import vsdk.toolkit.environment.geometry.PolyhedralBoundedSolid;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidFace;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidLoop;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidHalfEdge;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidVertex;

/**
This is a utility class containing a lot of geometry operations (mostly
geometry generating and modifying operations). This is a companion class
for the `ComputationalGeometry` class which holds geometrical querys and
`SimpleTestGeometryLibrary` which contains another geometry generation
procedures.

This class is comprised of static methods, depending only of VSDK's Entity's.
From a design point of view, it can be view as an "strategy" design pattern
in the sense it encapsulates algorithms. It also could be viewd as a "factory"
or "abstract factory", as it is a class for creating objects, following
the data hierarchy of interface "geometry".

From a practical point of use, note this is similar to Java's `Graphics`
class in Java2D, as this is the class where user can create graphics primitives
using mid-level constructs, but note that this class is not responsible of
any rendering, rasterizing or visualization; it only uses data structures.
*/
public class GeometricModeler extends ProcessingElement
{

    public static final int UNION = 1;
    public static final int INTERSECTION = 2;
    public static final int DIFFERENCE = 3;

    /**
    Creates a 3D line from point (x1, y1, z1) to point (x2, y2, z2).
    */
    public static ParametricCurve
    createLine(double x1, double y1, double z1,
               double x2, double y2, double z2)
    {
        ParametricCurve lineModel;
        Vector3D pointParameters[];

        lineModel = new ParametricCurve();
        pointParameters = new Vector3D[1];
        pointParameters[0] = new Vector3D(x1, y1, z1);
        lineModel.addPoint(pointParameters, ParametricCurve.CORNER);

        pointParameters = new Vector3D[1];
        pointParameters[0] = new Vector3D(x2, y2, z2);
        lineModel.addPoint(pointParameters, ParametricCurve.CORNER);

        return lineModel;
    }

    /**
    This method implements the example presented in section [MANT1988].12.2,
    and program [MANT1988].12.1.
    Generate an arc based on the radius and the coordinates of the center on
    the plane z=h. This method assumes that the first vertex of the arc
    already exists, so its identifier (vertexId) must be supplied.
    This method generates an approximation of a circular arc segment with
    `n` edges, centered at <cx, cy, h>, on the plane z=h, and with radius
    `rad`. The arc ranges from angle `phi1` to `phi2`, measured in degrees,
    where an angle of 0.0 degrees equals the x-axis and angles grow
    counterclockwise. The arc starts from existing vertex `vertexId` of face
    `faceId`.
    */
    public static void addArc(PolyhedralBoundedSolid solid,
        int faceId, int vertexId,
        double cx, double cy, double rad, double h, double phi1, double phi2,
        int n)
    {
        double x, y, angle, inc;
        int prev, i, nextVertexId;

        angle = Math.toRadians(phi1);
        inc = Math.toRadians(((phi2 - phi1) / ((double)n)));
        prev = vertexId;
        for ( i = 0; i < n; i++ ) {
            angle += inc;
            x = cx + rad * Math.cos(angle);
            y = cy + rad * Math.sin(angle);
            nextVertexId = solid.getMaxVertexId() + 1;
            solid.smev(faceId, prev, nextVertexId, new Vector3D(x, y, h));
            prev = nextVertexId;
        }
        solid.validateModel();
    }

    /**
    This method implements the example presented in section [MANT1988].12.2,
    and program [MANT1988].12.2.
    */
    public static PolyhedralBoundedSolid createCircularLamina(
        double cx, double cy, double rad, double h, int n)
    {
        PolyhedralBoundedSolid solid;

        solid = new PolyhedralBoundedSolid();
        solid.mvfs(new Vector3D(cx + rad, cy, h), 1, 1);
        addArc(solid, 1, 1, cx, cy, rad, h, 0, 
            ((double)(n-1))*360.0/((double)n), n-1);
        solid.smef(1, n, 1, 2);
        solid.validateModel();
        return solid;
    }

    /**
    This method implements a generalized/extended version of the example
    presented in section [MANT1988].12.3.1, and figure [MANT1988].12.3. In the
    original example, a displacement vector is added (translated) to
    the extruded point. Current implementation allows translations, scales
    and rotations in terms of original face plane.
    PRE: Given face should be closed and planar ("well formed")
    Note that current algorithm could work on a lamina, on or an "ending face",
    permiting a solid modeler to build complex loft-type sweeps based on
    current method.
    */
    public static void translationalSweepExtrudeFace(
        PolyhedralBoundedSolid solid,
        _PolyhedralBoundedSolidFace face,
        Matrix4x4 T)
    {
        _PolyhedralBoundedSolidLoop l;
        _PolyhedralBoundedSolidHalfEdge first, scan;
        _PolyhedralBoundedSolidVertex v;
        Vector3D newPos;
        int i;

        for ( i = 0; i < face.boundariesList.size(); i++ ) {
            l = face.boundariesList.get(i);
            first = l.boundaryStartHalfEdge;
            scan = first.next();
            v = scan.startingVertex;
            newPos = T.multiply(v.position);
            solid.lmev(scan, scan, solid.getMaxVertexId()+1, newPos);
            while ( scan != first ) {
                v = scan.next().startingVertex;
                newPos = T.multiply(v.position);
                solid.lmev(scan.next(), scan.next(), 
                    solid.getMaxVertexId()+1, newPos);
                solid.lmef(scan.previous(), scan.next().next(),
                    solid.getMaxFaceId()+1);
                scan = (scan.next().mirrorHalfEdge()).next();
            }
            solid.lmef(scan.previous(), scan.next().next(),
                solid.getMaxFaceId()+1);
        }
        solid.validateModel();
    }

    /**
    This method implements a generalized/extended version of the 
    translationalSweepExtrudeFace, which makes the same, plus a second
    pass verifying if each of the newly created faces are planar or not.

    If a new face is not planar, the face is triangulated.
    */
    public static void translationalSweepExtrudeFacePlanar(
        PolyhedralBoundedSolid solid,
        _PolyhedralBoundedSolidFace face,
        Matrix4x4 T)
    {
        _PolyhedralBoundedSolidLoop l;
        _PolyhedralBoundedSolidHalfEdge first, scan;
        _PolyhedralBoundedSolidVertex v;
        Vector3D newPos;
        ArrayList<Integer> newfaces = new ArrayList<Integer>();
        int i;
        int newfaceid;

        for ( i = 0; i < face.boundariesList.size(); i++ ) {
            l = face.boundariesList.get(i);
            first = l.boundaryStartHalfEdge;
            scan = first.next();
            v = scan.startingVertex;
            newPos = T.multiply(v.position);
            solid.lmev(scan, scan, solid.getMaxVertexId()+1, newPos);
            while ( scan != first ) {
                v = scan.next().startingVertex;
                newPos = T.multiply(v.position);
                solid.lmev(scan.next(), scan.next(), 
                    solid.getMaxVertexId()+1, newPos);
                newfaceid = solid.getMaxFaceId()+1;
                solid.lmef(scan.previous(), scan.next().next(), newfaceid);
                newfaces.add(new Integer(newfaceid));
                scan = (scan.next().mirrorHalfEdge()).next();
            }
            newfaceid = solid.getMaxFaceId()+1;
            solid.lmef(scan.previous(), scan.next().next(), newfaceid);
            newfaces.add(new Integer(newfaceid));
        }

        _PolyhedralBoundedSolidFace newface;
        for ( i = 0; i < newfaces.size(); i++ ) {
            newfaceid = newfaces.get(i).intValue();
            newface = solid.findFace(newfaceid);
            if ( !solid.validateFaceIsPlanar(newface) ) {
                scan = newface.boundariesList.get(0).boundaryStartHalfEdge;
                newfaceid = solid.getMaxFaceId()+1;
                solid.lmef(scan.next(), scan.previous(), newfaceid);
            }
        }

        for ( i = 0; newfaces.size() > 0; i++ ) {
            newfaces.remove(0);
        }

        solid.validateModel();
    }

    public static PolyhedralBoundedSolid createBrepFromParametricCurve(ParametricCurve curve)
    {
        //-----------------------------------------------------------------
        int i, j;
        int totalNumberOfPoints;
        double list[][];
        Vector3D first;
        boolean beginning;
        int count;

        //-----------------------------------------------------------------
        totalNumberOfPoints = 0;

        for ( i = 1; i < curve.types.size(); i++ ) {
            if ( curve.types.get(i).intValue() == ParametricCurve.BREAK ) {
                i++;
                continue;
            }
            ArrayList<Vector3D> polyline = curve.calculatePoints(i, false);
            totalNumberOfPoints += polyline.size();
        }

        list = new double[totalNumberOfPoints][3];

        //-----------------------------------------------------------------
        PolyhedralBoundedSolid solid;
        boolean firstLoop = true;
        int nextVertexId = 1;
        int lastLoopStartVertexId = 1;
        count = 0;
        boolean needAnEnding = false;
        int nextFaceId = 1;

        solid = new PolyhedralBoundedSolid();

        first = new Vector3D();
        beginning = true;
        for ( i = 1; i < curve.types.size(); i++ ) {
            if ( curve.types.get(i).intValue() == ParametricCurve.BREAK ) {
                i++;

                //----------
                solid.mef(1, 1,
                          lastLoopStartVertexId, lastLoopStartVertexId+1, 
                          nextVertexId-1, nextVertexId-2, nextFaceId);
                nextFaceId++;

                if ( !firstLoop ) {
                    solid.kfmrh(2, nextFaceId-1);
                }
                //----------
                firstLoop = false;
                beginning = true;
                continue;
            }

            // Build a polyline for approximating the [i] curve segment
            ArrayList<Vector3D> polyline = curve.calculatePoints(i, false);

            // Insert into current contour the polyline
            for ( j = 0; j < polyline.size(); j++ ) {
                Vector3D vec = polyline.get(j);
                if ( !beginning ) {
                    Vector3D prev = new Vector3D(list[count-1][0], 
                                                 list[count-1][1],
                                                 list[count-1][2]);
                    if ( VSDK.vectorDistance(vec,  prev) > VSDK.EPSILON &&
                         VSDK.vectorDistance(vec, first) > VSDK.EPSILON ) {
                        list[count][0] = vec.x;
                        list[count][1] = vec.y;
                        list[count][2] = vec.z;
                        solid.smev(1, nextVertexId-1, nextVertexId, new Vector3D(vec));
                        nextVertexId++;
                        count++;
                    }
                  }
                  else {
                    beginning = false;
                    list[count][0] = vec.x;
                    list[count][1] = vec.y;
                    list[count][2] = vec.z;
                    if ( firstLoop ) {
                        solid.mvfs(new Vector3D(vec), nextVertexId, nextFaceId);
                        nextVertexId++;
                        nextFaceId++;
                    }
                    else {
                        solid.smev(1, nextVertexId-1, nextVertexId, new Vector3D(vec));
                        nextVertexId++;
                        solid.kemr(1, 1, nextVertexId-2, nextVertexId-1,
                            nextVertexId-1, nextVertexId-2);
                        lastLoopStartVertexId = nextVertexId-1;
                        needAnEnding = true;
                    }

                    first = new Vector3D(vec.x, vec.y, vec.z);
                    count++;
                }
            }
        }

        solid.mef(1, 1,
                  lastLoopStartVertexId, lastLoopStartVertexId+1, 
                  nextVertexId-1, nextVertexId-2, nextFaceId);
        nextFaceId++;
        if ( needAnEnding ) {
            solid.kfmrh(2, nextFaceId-1);
        }

        solid.validateModel();
        return solid;
    }

    /**
    Given the input `inSolid` and the cutting plane `inSplittingPlane`,
    this method appends to the `outSolidsAbove` list the solids resulting
    from cutting the solid with the plane and resulting above the plane,
    similarly, `outSolidsBelow` will be appended with solid pieces
    resulting below the plane.

    This is just a placeholder for method PolyhedralBoundedSolidSplitter.split.
    Check its documentation for extra information.
    */
    public static void split(
                      PolyhedralBoundedSolid inSolid,
                      InfinitePlane inSplittingPlane,
                      ArrayList<PolyhedralBoundedSolid> outSolidsAbove,
                      ArrayList<PolyhedralBoundedSolid> outSolidsBelow)
    {
        PolyhedralBoundedSolidSplitter.split(inSolid, inSplittingPlane,
                                             outSolidsAbove, outSolidsBelow);
    }

    /**
    This is just a placeholder for method
        PolyhedralBoundedSolidSetOperator.setOp.
    Check its documentation for extra information.
    */
    public static PolyhedralBoundedSolid setOp(
        PolyhedralBoundedSolid inSolidA,
        PolyhedralBoundedSolid inSolidB,
        int op)
    {
        return PolyhedralBoundedSolidSetOperator.setOp(inSolidA, inSolidB, op);
    }

    /**
    This is just a placeholder for method
        PolyhedralBoundedSolidSetOperator.setOp.
    Check its documentation for extra information.
    */
    public static PolyhedralBoundedSolid setOp(
        PolyhedralBoundedSolid inSolidA,
        PolyhedralBoundedSolid inSolidB,
        int op, boolean withDebug)
    {
        return PolyhedralBoundedSolidSetOperator.setOp(inSolidA, inSolidB, op, withDebug);
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
