package vsdk.toolkit.processing;

import java.util.ArrayList;
import vsdk.toolkit.common.Vertex2D;
import vsdk.toolkit.environment.geometry.Polygon2D;
import vsdk.toolkit.environment.geometry._Polygon2DContour;

/**
 * Similar to Polygon2D but is used in polygon clipping(use _Polygon2DContourWA not _Polygon2DContour).
 *
 * This class have a copy constructor that, if required, cleans the original
 * polygon.
 */
public class _Polygon2DWA {

    public ArrayList<_Polygon2DContourWA> loops;
    private _Polygon2DContourWA currentLoop;

    public _Polygon2DWA() {
        loops = new ArrayList<_Polygon2DContourWA>();
        nextLoop();
    }

    /**
     * Copy constructor with normal polygon. If copyClean is true this
     * constructor copies polyToCopy without repeated points and without points
     * that lie in a line between any to points.
     *
     * @param polyToCopy The normal polygon to copy.
     * @param copyClean
     */
    public _Polygon2DWA(Polygon2D polyToCopy, boolean copyClean) {
        boolean oneOrMoreLoops = false;
        Vertex2D prevV, prevPV;
        boolean removeLast, isFirst;
        int i,j;

        loops = new ArrayList<_Polygon2DContourWA>();
//        for ( _Polygon2DContour contourToCopy : polyToCopy.loops ) {
        for ( i = 0; i < polyToCopy.loops.size(); ++i ) {
            _Polygon2DContour contourToCopy = polyToCopy.loops.get(i);
            oneOrMoreLoops = true;
            nextLoop();
            if ( contourToCopy.vertices.isEmpty() ) {
                continue;
            }
            if ( contourToCopy.vertices.size() == 1 ) {
                prevV = contourToCopy.vertices.get(0);
                addVertex(prevV.x, prevV.y, prevV.color.r, prevV.color.g, prevV.color.b);
            } else {
                prevV = contourToCopy.vertices.get(contourToCopy.vertices.size() - 1);
                prevPV = contourToCopy.vertices.get(contourToCopy.vertices.size() - 2);
                removeLast = false;
                isFirst = true;
//                for ( Vertex2D v : contourToCopy.vertices ) {
                for ( j = 0; j < contourToCopy.vertices.size(); ++j ) {
                    Vertex2D v = contourToCopy.vertices.get(j);
                    if ( copyClean ) {
                        if ( Math.abs(prevV.x - v.x) > 0.0001 || Math.abs(prevV.y - v.y) > 0.0001 ) {
                            addVertex(v.x, v.y, v.color.r, v.color.g, v.color.b);
                            if ( areCollinearAndOposite2DVectors(prevPV, prevV, v) ) {
                                if ( isFirst ) {
                                    removeLast = true;
                                } else {
                                    //Remove prevV in the copy:
                                    currentLoop.removeVertex(currentLoop.vertices.size() - 2);
                                }
                            } else //If prevV is not removed from the copy:
                            {
                                prevPV = prevV;
                            }
                            prevV = v;
                        }
                        isFirst = false;
                    } else {
                        addVertex(v.x, v.y, v.color.r, v.color.g, v.color.b);
                    }
                }
                if ( removeLast ) {
                    currentLoop.removeVertex(currentLoop.vertices.size() - 1);
                }
                if ( currentLoop.vertices.size() == 0 ) { //In the case of two or more repeated points.
                    prevV = contourToCopy.vertices.get(0);
                    addVertex(prevV.x, prevV.y, prevV.color.r, prevV.color.g, prevV.color.b);
                }
            }
        }
        if ( !oneOrMoreLoops ) {
            nextLoop(); //At least one loop.
        }
    }
//    /** This function copies polyToCopy into destPolyWA[0] without repeated points and
//     * without points that lie in a line between any to points.
//     * @param destPolyWA
//     * @param polyToCopy 
//     */
//    public void copyPolyClean(_Polygon2DWA[] destPolyWA, Polygon2D polyToCopy) {
//        for(_Polygon2DContour p2DCont : polyToCopy.loops)
//            for(_Polygon2DContourWA p2DCont : p2DCont.vertices) {
//                
//            }
//    }

    private boolean areCollinearAndOposite2DVectors(Vertex2D vEndA, Vertex2D vStartAB, Vertex2D vEndB) {
        Vertex2D a;
        Vertex2D b;
        double temp;

        a = new Vertex2D(vEndA.x - vStartAB.x, vEndA.y - vStartAB.y);
        b = new Vertex2D(vEndB.x - vStartAB.x, vEndB.y - vStartAB.y);
        temp = Math.sqrt(a.x * a.x + a.y * a.y);
        a.x = a.x / temp;
        a.y = a.y / temp;
        temp = Math.sqrt(b.x * b.x + b.y * b.y);
        b.x = b.x / temp;
        b.y = b.y / temp;
        //Dot product.
        temp = a.x * b.x + a.y * b.y;
        return (temp > -1.0001 && temp < -0.9999);
    }

    public final void addVertex(double x, double y, double r, double g, double b) {
        currentLoop.addVertex(x, y, r, g, b);
    }

    public void addVertex(double x, double y) {
        currentLoop.addVertex(x, y);
    }

    public void pushVertex(double x, double y) {
        currentLoop.pushVertex(x, y);
    }

    public final void nextLoop() {
        currentLoop = new _Polygon2DContourWA();
        loops.add(currentLoop);
    }

}
