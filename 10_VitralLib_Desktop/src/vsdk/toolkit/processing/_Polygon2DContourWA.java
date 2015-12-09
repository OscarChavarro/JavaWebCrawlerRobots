package vsdk.toolkit.processing;

/**
 * Similar to _Polygon2DContour but is used in polygon clipping.
 */
public class _Polygon2DContourWA {

    public _CircDoubleLinkedList<_VertexNode2D> vertices;
    public boolean isClipped = false;
    public boolean isHole = false;
//    /** Classified signify: is a hole or not. */
//    public boolean isClassified = false;

    public _Polygon2DContourWA() {
        vertices = new _CircDoubleLinkedList<_VertexNode2D>();
    }

    public void addVertex(double x, double y, double r, double g, double b) {
        vertices.add(new _VertexNode2D(x, y, r, g, b));
    }

    public void addVertex(double x, double y) {
        vertices.add(new _VertexNode2D(x, y));
    }

    public void removeVertex(int ind) {
        vertices.remove(ind);
    }

    public void pushVertex(double x, double y) {
        vertices.add(0, new _VertexNode2D(x, y));
    }
}
