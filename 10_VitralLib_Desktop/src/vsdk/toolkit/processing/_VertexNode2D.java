package vsdk.toolkit.processing;

import vsdk.toolkit.common.ColorRgb;

/**
 * Similar to Vertex2D but is used in polygon clipping.
 */
public class _VertexNode2D {

    public double x;
    public double y;
    public ColorRgb color;
    public byte flags; //The first two bits are used
    /**
     * pairNode is the node in the other polygon(in the intersection)
     */
    public _DoubleLinkedListNode<_VertexNode2D> pairNode;
    //public boolean isCut; ///
    //ListIterator<VertexNode2D> lstitVertNodeOtherPoly;

    public _VertexNode2D() {
        this.color = new ColorRgb();
        flags = 0;
        pairNode = null;
//        isCut = false;
    }

    public _VertexNode2D(double x, double y) {
        this.x = x;
        this.y = y;
        this.color = new ColorRgb();
        flags = 0;
        pairNode = null;
//        isCut = false;
    }

    public _VertexNode2D(double x, double y, double r, double g, double b) {
        this.x = x;
        this.y = y;
        this.color = new ColorRgb(r, g, b);
        flags = 0;
        pairNode = null;
//        isCut = false;
    }

    public _VertexNode2D(_VertexNode2D other) {
        this.x = other.x;
        this.y = other.y;
        this.color = new ColorRgb(other.color.r, other.color.g, other.color.b);
        this.flags = other.flags;
        this.pairNode = other.pairNode;
//        isCut = other.isCut;
    }
}
