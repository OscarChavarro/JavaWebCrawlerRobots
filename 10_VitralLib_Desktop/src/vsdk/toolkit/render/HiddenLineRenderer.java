//===========================================================================
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [FOLE1992] Foley, vanDam, Feiner, Hughes. "Computer Graphics,           =
//=          principles and practice" - second edition, Addison Wesley,     =
//=          1992.                                                          =
//= [APPE1967] Appel, Arthur. "The notion of quantitative invisivility and  =
//=          the machine rendering of solids". Proceedings, ACM National    =
//=          meeting 1967.                                                  =
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - September 5 2007 - Oscar Chavarro: Original base version              =
//===========================================================================

package vsdk.toolkit.render;

// Java classes
import java.util.ArrayList;
import java.util.Collections;

// VitralSDK classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.Ray;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.InfinitePlane;
import vsdk.toolkit.environment.geometry.PolyhedralBoundedSolid;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidFace;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidEdge;
import vsdk.toolkit.processing.ComputationalGeometry;

class _AppelEdgeSegment extends RenderingElement implements Comparable <_AppelEdgeSegment>
{
    /// Distance from start to end with respect to line parameter
    public double t;
    public int deltaQI; // Relative change in quantitative invisibility

    @Override
    public int compareTo(_AppelEdgeSegment other)
    {
        if ( this.t < other.t - VSDK.EPSILON ) return -1;
        else if ( this.t > other.t + VSDK.EPSILON ) return 1;
        return 0;
    }
}

class _AppelEdgeCache extends RenderingElement
{
    public static final int HIDDEN_LINE = 0;
    public static final int VISIBLE_LINE = 1;
    public static final int CONTOUR_LINE = 2;

    public int edgeType;
    /// True if current line starts on the end of a previous one in the same
    /// solid and with the same quantitative invisibility. When this happens,
    /// quantitative invisibility can be acumulated in the edge sequence,
    /// otherwise must be calculated.
    boolean onSequence;
    public Vector3D start;
    public Vector3D end;
    /// d = start - end
    public Vector3D d;
    /// `visibleEdgeForContourLine` contains an explicit reference to the
    /// planar surface marked as "S" on figure [APPE1967].5.
    public _PolyhedralBoundedSolidFace visibleEdgeForContourLine;

    public void setStart(Vector3D s)
    {
        start = new Vector3D(s);
    }

    public void setEnd(Vector3D e)
    {
        end = new Vector3D(e);
    }
}

/**
This class implements the Appel's algorithm for hidden line rendering. :)
*/
public class HiddenLineRenderer extends RenderingElement
{
    private static int
    computeQuantitativeInvisibility(ArrayList <SimpleBody> solids,
        Camera camera, _AppelEdgeCache edge)
    {
        int qi = 0;
        int i;

        for ( i = 0; i < solids.size(); i++ ) {
            qi += solids.get(i).computeQuantitativeInvisibility(
                camera.getPosition(), edge.start.add(edge.d.multiply(10*VSDK.EPSILON)));
        }
        return qi;
    }

    /**
    Given a set of solids, this method computes the "edge cache": a list
    of edges, where every edge gets one of three classifications:
    HIDDEN_LINE, VISIBLE_LINE or CONTOUR_LINE.
    Edges are classified acording to the visibility of its participating
    faces. Note that current implementation supposes that every given
    edge is shared by exactly two planar surfaces, and that no pair of
    edges intersects. This assumption is implied here by first converting
    the solid to a polyhedral bounded representation (BREP). As VitralSDK
    BREP ensures that assumptions, current implementation is solid with
    that datastructure.

    Original [APPE1967] paper makes no assumption on data representation as
    long that the representation is able to answer the question of what pair
    or surfaces share an edge, so this Vitral SDK implementation is more
    restrictive that the one of the original paper, but it is expected to me
    also more robust.
    */
    private static void buildCache(ArrayList <SimpleBody> solids,
                                   SimpleBody body, 
                                   ArrayList <_AppelEdgeCache> cache,
                                   ArrayList <_AppelEdgeCache> contourCache,
                                   Camera camera)
    {
        Geometry g = body.getGeometry();

        if ( g == null ) {
            return;
        }

        g = g.exportToPolyhedralBoundedSolid();

        solids.add(body);
        PolyhedralBoundedSolid solid = (PolyhedralBoundedSolid)g;

        int i;
        long l = 0;

        _PolyhedralBoundedSolidFace face1;
        _PolyhedralBoundedSolidFace face2;
        boolean f1, f2;
        _AppelEdgeCache materialLine;
        Vector3D prevEnd = new Vector3D();

        for ( i = 0; i < solid.edgesList.size(); i++ ) {
            _PolyhedralBoundedSolidEdge e = solid.edgesList.get(i);

            int start, end;
            start = e.getStartingVertexId();
            end = e.getEndingVertexId();
            if ( start >= 0 && end >= 0 ) {
                Vector3D startPosition;
                Vector3D endPosition;

                startPosition = e.leftHalf.startingVertex.position;
                endPosition = e.rightHalf.startingVertex.position;
                if ( startPosition != null && endPosition != null ) {
                    //--------------------------------------------------------
                    face1 = e.leftHalf.parentLoop.parentFace;
                    face2 = e.rightHalf.parentLoop.parentFace;
                    f1 = face1.isVisibleFrom(camera) >= 0;
                    f2 = face2.isVisibleFrom(camera) >= 0;

                    //--------------------------------------------------------
                    materialLine = new _AppelEdgeCache();
                    materialLine.setStart(startPosition);
                    materialLine.setEnd(endPosition);
                    materialLine.d = endPosition.substract(startPosition);
                    if ( l > 0 &&
                         VSDK.vectorDistance(prevEnd, startPosition) < 
                             VSDK.EPSILON ) {
                        materialLine.onSequence = true;
                    }
                    else {
                        materialLine.onSequence = false;
                    }
                    if ( !f1 && !f2 ) {
                        // Totally hidden lines
                        materialLine.edgeType = _AppelEdgeCache.HIDDEN_LINE;
                    }
                    else if ( f1 && !f2 || !f1 && f2 ) {
                        // Contour lines
                        materialLine.edgeType = _AppelEdgeCache.CONTOUR_LINE;
                        if ( f1 ) {
                            materialLine.visibleEdgeForContourLine = face1;
                        }
                        else {
                            materialLine.visibleEdgeForContourLine = face2;
                        }
                        contourCache.add(materialLine);
                    }
                    else {
                        // Visible non contour lines
                        materialLine.edgeType = _AppelEdgeCache.VISIBLE_LINE;
                    }
                    cache.add(materialLine);
                    //--------------------------------------------------------
                    prevEnd.clone(endPosition);
                    l++;
                }
            }
        }
    }

    /**
    This method takes an edge that is candidate to be visible, breaks it into
    segments and for each segment determines visibility. Visible segments
    are reported in `outVisibleContourLineEndPoints` and `outVisibleNonContourLineEndPoints`,
    and hidden segments are reported on `outHiddenLineEndPoints`.
    PRE:
      - Current edge is known to correspond to a material line (normal or
        contour)
      - `contourCache` contains the list of contour lines
    */
    private static void
    processLineToBeDrawn(
        ArrayList <SimpleBody> solids,
        _AppelEdgeCache inEdge,
        Camera inCamera,
        ArrayList <Vector3D> outVisibleContourLineEndPoints,
        ArrayList <Vector3D> outVisibleNonContourLineEndPoints,
        ArrayList <Vector3D> outHiddenLineEndPoints,
        ArrayList <_AppelEdgeCache> contourCache)
    {
        //- 1. Compute the sweep plane triangle ---------------------------
        // Defines plane "SP1" on figure [APPE1967].5.
        Vector3D sp1a, sp1b, sp1c;

        sp1a = inEdge.start;
        sp1b = inEdge.end;
        sp1c = inCamera.getPosition();

        //- 2. Break current edge into segments ---------------------------
        // Defines plane "SP2" on figure [APPE1967].5.
        Vector3D sp2a, sp2b, sp2c;
        Vector3D K; // Preceding point "K" on figure [APPE1967].5.
        Vector3D J; // "K" projected on "SP2"
        Ray ray = new Ray(new Vector3D(), new Vector3D());
        double t0;
        int i;
        int pos;
        _AppelEdgeCache cl;          // Line "CL" on figure 5 of [APPE1967]
        Vector3D p = new Vector3D(); // Point "PP1" on figure 5 of [APPE1967]
        Vector3D n = new Vector3D();
        ArrayList<_AppelEdgeSegment> segments;
        _AppelEdgeSegment segment;
        InfinitePlane plane;

        segments = new ArrayList<_AppelEdgeSegment>();
        segment = new _AppelEdgeSegment();
        segment.t = 0;
        segment.deltaQI = 0;
        segments.add(segment);
        sp2c = inCamera.getPosition();

        for ( i = 0; i < contourCache.size(); i++ ) {
            cl = contourCache.get(i);
            if ( cl == inEdge ) {
                // Do not break an edge with itself.
                continue;
            }
            ray.origin.clone(cl.start.add(cl.d.multiply(3*VSDK.EPSILON)));
            ray.direction.clone(cl.d);
            t0 = ray.direction.length() - 6*VSDK.EPSILON;
            ray.direction.normalize();
            if (
             ComputationalGeometry.doIntersectionWithTriangle(ray, sp1a, sp1b, sp1c, p, n) &&
             ray.t < t0
            ) {
                // The breaking point in the current testing edge corresponding
                // to the passing contour is the piercing point where the
                // edge intersects with the contour's sweeping plane.
                sp2a = cl.start;
                sp2b = cl.end;
                plane = new InfinitePlane(sp2a, sp2b, sp2c);
                ray.origin.clone(inEdge.start);
                ray.direction.clone(inEdge.d);
                ray.direction.normalize();
                if ( plane.doIntersection(ray) ) {
                    segment = new _AppelEdgeSegment();
                    segment.t = ray.t / inEdge.d.length(); // Point "PP2"

                    // Determine the change in quantitative invisibility...
                    K = inEdge.start.add(inEdge.d.multiply(segment.t-2*VSDK.EPSILON));

                    // Project K on SP2
                    ray.origin.clone(K);
                    ray.direction = sp2c.substract(K);
                    ray.direction.normalize();
                    if ( cl.visibleEdgeForContourLine.containingPlane.
                         doIntersection(ray) ) {
                        J = ray.origin.add(ray.direction.multiply(ray.t));
                        pos = cl.visibleEdgeForContourLine.testPointInside(J, VSDK.EPSILON);
                        if ( pos == Geometry.INSIDE || pos == Geometry.LIMIT ) {
                            segment.deltaQI = 1;
                        }
                        else {
                            segment.deltaQI = -1;
                        }
                        segments.add(segment);
                    }
                }
            }
        }
        segment = new _AppelEdgeSegment();
        segment.t = 1;
        segments.add(segment);

        //- 3. Sort segment set -------------------------------------------
        Collections.sort(segments);

        // Erase null segments
        for ( i = 0; i < segments.size()-1; i++ ) {
            if ( segments.get(i).compareTo(segments.get(i+1)) == 0 ) {
                segments.remove(i);
                i--;
            }
        }

        //- 4. Determine visibility for each segment based on Q.I. --------
        Vector3D pos1, pos2;
        int qi;

        qi = computeQuantitativeInvisibility(solids, inCamera, inEdge);

        for ( i = 0; i < segments.size()-1; i++ ) {
            segment = segments.get(i);
            pos1 = inEdge.start.add(inEdge.d.multiply(segment.t));
            qi += segment.deltaQI;

            double val1 = segment.t;

            segment = segments.get(i+1);
            pos2 = inEdge.start.add(inEdge.d.multiply(segment.t));


            double val2 = segment.t;

            // This disables propagation of Q.I., making all slower!
            Vector3D posx = inEdge.start.add(inEdge.d.multiply((val2+val1)/2));
            qi = solids.get(0).computeQuantitativeInvisibility(inCamera.getPosition(), posx);
            //

            if ( qi == 0 ) {
                if ( inEdge.edgeType == _AppelEdgeCache.CONTOUR_LINE ) {
                    outVisibleContourLineEndPoints.add(new Vector3D(pos1));
                    outVisibleContourLineEndPoints.add(new Vector3D(pos2));
                }
                else {
                    outVisibleNonContourLineEndPoints.add(new Vector3D(pos1));
                    outVisibleNonContourLineEndPoints.add(new Vector3D(pos2));
                }
            }
            else {
                outHiddenLineEndPoints.add(new Vector3D(pos1));
                outHiddenLineEndPoints.add(new Vector3D(pos2));
            }
        }

        //segments = null;
    }

    /**
    Given a viewing camera and a set of bodies, this method generates three
    sets of lines for visible/hidden line rendering, as described in
    paper [APPE1967] and section [FOLE1992].15.3.2. The calculated end line
    points are in 3D space and contains viewer's perception to respect to which
    line segments are visible (as part of the object contour or non contour
    material lines) and which line segments are visible.
    */
    public static void executeAppelAlgorithm(
        ArrayList <SimpleBody> inSimpleBodyArray,
        Camera inCamera,
        ArrayList <Vector3D> outVisibleContourLineEndPoints,
        ArrayList <Vector3D> outVisibleNonContourLineEndPoints,
        ArrayList <Vector3D> outHiddenLineEndPoints)
    {
        //-----------------------------------------------------------------
        ArrayList <_AppelEdgeCache> cache;
        ArrayList <_AppelEdgeCache> contourCache;

        cache = new ArrayList <_AppelEdgeCache>();
        contourCache = new ArrayList <_AppelEdgeCache>();

        //-----------------------------------------------------------------
        ArrayList <SimpleBody> solids;
        int i;

        solids = new ArrayList <SimpleBody>();

        for ( i = 0; i < inSimpleBodyArray.size(); i++ ) {
            buildCache(solids, inSimpleBodyArray.get(i), cache, contourCache, inCamera);
        }

        //-----------------------------------------------------------------
        _AppelEdgeCache edge;

        for ( i = 0; i < cache.size(); i++ ) {
            edge = cache.get(i);
            // Note that a "line to be drawn" is any line in the cache
            // not marked as a hidden line.
            switch ( edge.edgeType ) {
              case _AppelEdgeCache.HIDDEN_LINE:
                outHiddenLineEndPoints.add(new Vector3D(edge.start));
                outHiddenLineEndPoints.add(new Vector3D(edge.end));
                break;
              case _AppelEdgeCache.CONTOUR_LINE:
              case _AppelEdgeCache.VISIBLE_LINE:
                processLineToBeDrawn(
                    solids,
                    edge, inCamera, outVisibleContourLineEndPoints,
                    outVisibleNonContourLineEndPoints, outHiddenLineEndPoints,
                    contourCache);
                break;
              default: break;
            }
        }
        //-----------------------------------------------------------------
        //cache = null;
        //contourCache = null;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
