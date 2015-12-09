//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - April 8 2006 - Oscar Chavarro: Original base version                  =
//= - November 1 2006 - Alfonso Barbosa, Diana Reyes: added classifyPoint   =
//===========================================================================

package vsdk.toolkit.environment.geometry;

import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.Ray;
import vsdk.toolkit.common.VSDK;

public class InfinitePlane extends HalfSpace {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20060502L;

    // This is the infinite plane with canonical equation ax + bx + cx + d = 0
    private double a;
    private double b;
    private double c;
    private double d;

    public InfinitePlane(InfinitePlane other)
    {
        this.clone(other);
    }

    public InfinitePlane(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public InfinitePlane(Vector3D normal, Vector3D pointInPlane) {
        normal.normalize();
        a = normal.x;
        b = normal.y;
        c = normal.z;
        d = -normal.dotProduct(pointInPlane);
    }

    public InfinitePlane(Vector3D p0, Vector3D p1, Vector3D p2) {
        Vector3D aa;
        Vector3D bb;
        Vector3D normal;
        aa = p1.substract(p0);
        aa.normalize();
        bb = p2.substract(p0);
        bb.normalize();
        normal = aa.crossProduct(bb);
        normal.normalize();
        this.a = normal.x;
        this.b = normal.y;
        this.c = normal.z;
        this.d = -normal.dotProduct(p0);
    }

    public final void clone(InfinitePlane other)
    {
        this.a = other.a;
        this.b = other.b;
        this.c = other.c;
        this.d = other.d;
    }

    @Override
    public boolean
    doIntersection(Ray inout_rayo) {
        double denominator = a*inout_rayo.direction.x + b*inout_rayo.direction.y + c*inout_rayo.direction.z;
        if ( Math.abs(denominator) < VSDK.EPSILON ) return false;
        double t = -(a*inout_rayo.origin.x + b*inout_rayo.origin.y + c*inout_rayo.origin.z + d)/denominator;

        if ( t < 0 ) return false;

        inout_rayo.t = t;

        return true;
    }

    public boolean
    doIntersectionWithNegative(Ray inout_rayo) {
        double denominator = a*inout_rayo.direction.x + b*inout_rayo.direction.y + c*inout_rayo.direction.z;
        if ( Math.abs(denominator) < VSDK.EPSILON ) {
            Ray r = new Ray(inout_rayo.origin, inout_rayo.direction.multiply(-1));
            if ( doIntersection(r) ) {
                inout_rayo.t = -r.t;
                return true;
            }
            else {
                return false;
            }
        }
        double t = -(a*inout_rayo.origin.x + b*inout_rayo.origin.y + c*inout_rayo.origin.z + d)/denominator;

        inout_rayo.t = t;

        return true;
    }

    /**
    Por a given point `p`, calculates if it lies inside, outside or 
    on surface with respect to current plane, taking the plane as an
    infinite halfspace, not as a surface.
    @param p
    @param distanceTolerance
    @return 0 if point is on the plane surface, 1 if point is outside or
    -1 if point is inside the plane - INSIDE, OUTSIDE or LIMIT constant value.
    Note that current interpretation of the plane is done as a semispace,
    where "outside" means the direction pointed by plane's normal.
    */
    public int doContainmentTestHalfSpace(Vector3D p,
                                          double distanceTolerance) {
        double num = a*p.x + b*p.y + c*p.z + d;
        int op = LIMIT;

        if( num > distanceTolerance ) {
            op = OUTSIDE;
        }
        else if( num < -distanceTolerance ) {
            op = INSIDE;
        }
        return op;
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doContainmentTest.
    @return INSIDE, OUTSIDE or LIMIT constant value
    */
    @Override
    public int doContainmentTest(Vector3D p, double distanceTolerance)
    {
        double num = a*p.x + b*p.y + c*p.z + d;
        int op = LIMIT;

        if( num > distanceTolerance ) {
            op = OUTSIDE;
        }
        else if( num < -distanceTolerance ) {
            op = -INSIDE;
        }
        return op;    
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doExtraInformation.
    @param inT
    */
    @Override
    public void
    doExtraInformation(Ray inRay, double inT, 
                                  GeometryIntersectionInformation outData) {
        outData.p.x = inRay.origin.x + inT*inRay.direction.x;
        outData.p.y = inRay.origin.y + inT*inRay.direction.y;
        outData.p.z = inRay.origin.z + inT*inRay.direction.z;

        outData.n.clone(getNormal());
    }

    /**
    TODO: Current returned values are not always valid!
    @return a new 6 valued double array containing the coordinates of a min-max
    bounding box for current geometry.
    */
    @Override
    public double[] getMinMax()
    {
        double minmax[] = new double[6];
        for ( int i = 0; i < 3; i++ ) {
            minmax[i] = -Double.MAX_VALUE;
        }
        for ( int i = 3; i < 6; i++ ) {
            minmax[i] = Double.MAX_VALUE;
        }
        return minmax;
    }

    public Vector3D getNormal()
    {
        Vector3D n = new Vector3D(a, b, c);
        n.normalize();
        return n;
    }

    public double getD()
    {
        return d;
    }

    public void setNormal(Vector3D n)
    {
        n.normalize();
        a = n.x;
        b = n.y;
        c = n.z;
    }

    public void setD(double d)
    {
        this.d = d;
    }

    /**
    Given a plane normal and a point in the plane, this method updates
    current plane to fit that spec.
    @param p
    @param n
    */
    public void setFromPointNormal(Vector3D p, Vector3D n)
    {
        setNormal(n);
        setD(-(n.x*p.x + n.y*p.y + n.z*p.z));
    }

    /**
    Given point `p`, current method returns the minimum (signed) distance
    between such a point and this plane.
    @param p
    @return a number indicating minimum signed distances to current plane
    */
    public double pointDistance(Vector3D p)
    {
        return a*p.x + b*p.y + c*p.z + d;
    }

    /**
    Given point `p`, current method returns the point in this plane such as
    its distance is the minimum to `p`. Note this correspond to "the point's
    projection to the plane" such as the projector is at 90 deg. angle with
    respect to the plane.
    @param p
    @return a new Vector3D containing point projection over current plane
    */
    public Vector3D projectPoint(Vector3D p)
    {
        double distance = pointDistance(p);
        Vector3D n = new Vector3D(a, b, c);
        n.normalize();
        n = n.multiply(distance);
        return p.substract(n);
    }

    /**
    Given point `p`, current method returns the mirrored point of `p` with
    respect to this plane. Note that the intersection between this plane and
    the line from `p` to its mirror is the projection of p over the plane.
    @param p
    @return a new Vector3D with a point mirrored with respect to input point
    around current plane
    */
    public Vector3D mirrorPoint(Vector3D p)
    {
        double distance = pointDistance(p);
        Vector3D n = new Vector3D(a, b, c);
        n.normalize();
        n = n.multiply(2*distance);
        return p.substract(n);
    }

    /**
    Returns `true` if `this` plane is overlaping with `other` plane.
    Note that two planes are overlaping if they are both coplanar and
    at the same distance from the origin.
    @param other
    @param tolerance
    @return true if plane overlaps other plane, false otherwise
    */
    public boolean overlapsWith(InfinitePlane other, double tolerance)
    {
        double a1, b1, c1, d1;
        double a2, b2, c2, d2;
        double l1, l2;
        Vector3D n1;
        Vector3D n2;

        a1 = this.a;
        b1 = this.b;
        c1 = this.c;
        d1 = this.d;
        a2 = other.a;
        b2 = other.b;
        c2 = other.c;
        d2 = other.d;
        n1 = new Vector3D(a1, b1, c1);
        n2 = new Vector3D(a2, b2, c2);
        l1 = n1.length();
        l2 = n2.length();
        a1 /= l1;
        b1 /= l1;
        c1 /= l1;
        d1 /= l1;
        a2 /= l2;
        b2 /= l2;
        c2 /= l2;
        d2 /= l2;
        return Math.abs(a2 - a1) <= tolerance &&
            Math.abs(b2 - b1) <= tolerance &&
            Math.abs(c2 - c1) <= tolerance &&
            Math.abs(d2 - d1) <= tolerance;
    }

    @Override
    public String toString()
    {
        String msg = "InfinitePlane: N=<" + 
            VSDK.formatDouble(a) + ", " +
            VSDK.formatDouble(b) + ", " +
            VSDK.formatDouble(c) + ">, D=" +
            VSDK.formatDouble(d);
        return msg;
    }

    /**
     * @return the a
     */
    public double getA() {
        return a;
    }

    /**
     * @param a the a to set
     */
    public void setA(double a) {
        this.a = a;
    }

    /**
     * @return the b
     */
    public double getB() {
        return b;
    }

    /**
     * @param b the b to set
     */
    public void setB(double b) {
        this.b = b;
    }

    /**
     * @return the c
     */
    public double getC() {
        return c;
    }

    /**
     * @param c the c to set
     */
    public void setC(double c) {
        this.c = c;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
