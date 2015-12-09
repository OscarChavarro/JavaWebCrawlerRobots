//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 10 2007 - Oscar Chavarro: Original base version                   =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [BAER2002] Baeentzen, Jakob Andreas. Aanaes, Henrik. "Generating Signed =
//=     Distance Fields From Triangle Meshes",  Technical report            =
//=     IMM-TR-2002-21, Thecnical University of Denmark, 2002.              =
//= [MANT1988] Mantyla Martti. "An Introduction To Solid Modeling",         =
//=     Computer Science Press, 1988.                                       =
//===========================================================================

package vsdk.toolkit.processing;

import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.Ray;
import vsdk.toolkit.common.linealAlgebra.Vector2D;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.InfinitePlane;

/**
This class contains common computational geometry operations (mostly
geometrical querys over existing geometries). This is a companion class
for the `GeometricModeler`, which holds creation and modification operations
over gemetries.
*/
public class ComputationalGeometry extends ProcessingElement
{
    private static final InfinitePlane workPlane;
        
        // For 2D line clipping an 4 bit outcode is used
    private static final int COHEN_SUTHERLAND_2D_INSIDE = 0; // 0000
    private static final int COHEN_SUTHERLAND_2D_LEFT = 1;  // 0001
    private static final int COHEN_SUTHERLAND_2D_RIGHT = 2; // 0010
    private static final int COHEN_SUTHERLAND_2D_BOTTOM = 4;// 0100
    private static final int COHEN_SUTHERLAND_2D_TOP = 8;   // 1000

    static {
        workPlane = new InfinitePlane(1, 1, 1, 0);
    }

    /**
    Check the related interface contract for method
    Geometry.doIntersection.

    SPECIFIC IMPLEMENTATION: this method solve the intersection problem
    for one triangle in two stages:
    <UL>
    <LI>Calculates the plane containing the Triangle and checks if the Ray
    intersects with that plane.
    <LI>If the Ray intersects the plane, a check is done to determine
    if the intersection is inside the Triangle.
    </UL>
    That logic y repeated for all the Triangles in the TriangleMesh, and
    the shortest length intersected Triangle is reported.<P>

    Precondition:
\f[
    \mathbf{Q} := inOut\_Ray.direction.length() = 1 \;
\f]

    NOTES:
    <UL>
    <LI>The plane normal is determined for each triangle as the cross product
    of two Triangle edge Vector3D's, algorithm step (1).
    <LI>The canonic equation for a plane with normal n is
\f[
        nx*x + ny*y +nz*z + d = 0
\f]
    <LI>The parametric equation for the ray inOut_Ray (call it r) is
\f[
        \vec p = \vec{r.o} + t * \vec{r.d}
\f]
    <LI>Combining those two equations and solving for parameter t, algorithm
    step (2), gives
\f[
        t = \frac{ -(nx*ox+ny*oy+nz*oz+d) }{ nx*dx+ny*dy+nz*dz }
\f]
    and observing that the appearing vector components can be expressed as
    dot product, this equation can be rewritten in the condensed vectorial
    form
\f[
        t = \frac{ -(\vec n \cdot \vec{r.o} +d) }
                               { \vec n \cdot \vec{r.d} }
\f]
    <LI>Scalar value d in that equation can be solve replacing the coordinates
    of any of the Triangle points into the plane equation.
    <LI>To check if an intersected point lies inside the triangle, a left/right
    test is done with each one of the three directed edge vectors. If all three
    tests pass, then the point is inside the triangle.
    <LI>If the normal and the direction of the ray are in the same direction
    (more than 90 degrees of vector angle) then the normal is inverted to manage
     meshes with reversed triangles.
    </UL>
    @param inOut_Ray
    @param v0
    @param v1
    @param v2
    @param outPoint
    @param outNormal
    @return 
    */
    public static boolean
    doIntersectionWithTriangle(
        Ray inOut_Ray,
        Vector3D v0, 
        Vector3D v1, 
        Vector3D v2,
        Vector3D outPoint, 
        Vector3D outNormal) 
    {
        Vector3D p;           // Point of intersection between ray and plane
        Vector3D u, v, n;     // Edge vectors and normal
        double t, a, b, d;    // Coefficients for solving equation (2)
        double s1, s2, s3;    // Side test for each of triangle border

        // The vectors u & v are two triangle edges, and define the 
        // normal (1)
        u = v1.substract(v0);
        v = v2.substract(v1);
        n = v.crossProduct(u);
        n.normalize();

        // This is the result of replacing point v0 on plane equation, 
        // solving for d
        d = -n.dotProduct(v0);

        // Calculate numerator and denominator for equation (2)
        a = n.dotProduct(inOut_Ray.origin) + d;
        b = n.dotProduct(inOut_Ray.direction);

        // The denominator is big when the ray is not parallel to the plane
        if ( Math.abs(b) > VSDK.EPSILON ) {
            // Solution for equation (2), only if non-zero denominator
            t = (-a) / b;

            if ( t < 0.0 ) return false;

            // Calculate the intersection point between ray and plane
            p = inOut_Ray.origin.add(inOut_Ray.direction.multiply(t));

            // Check if the point is inside the triangle
            s1 = u.crossProduct(p.substract(v0)).dotProduct(n);
            s2 = (v2.substract(v1)).crossProduct(p.substract(v1)).dotProduct(n);
            s3 = (v0.substract(v2)).crossProduct(p.substract(v2)).dotProduct(n);

            if ( (s1 >= 0 && s2 >= 0 && s3 >= 0) || 
                 (s1 <= 0 && s2 <= 0 && s3 <= 0) ) {
                inOut_Ray.t = t;
                outNormal.clone(n);
                outPoint.clone(p);
                return true;
            }
        }
        return false;
    }

    /**
    Given a line from p0 to p1 and a point p, this method gives the minimum
    distance between line and point.
    
    Following implementation from http://geomalgorithms.com/a02-_lines.html
    @param p0
    @param p1
    @param p
    @return 
    */
    public static double lineToPointDistance(
        Vector3D p0, 
        Vector3D p1,
        Vector3D p)
    {
        //----------------------------------------------------------------------
        Vector3D lineVector = p1.substract(p0);

        double denominator = lineVector.length();
        if ( denominator < VSDK.EPSILON ) {
            return Double.NaN;
        }

        //----------------------------------------------------------------------
        Vector3D v, w;

        v = p1.substract(p0);
        w = p.substract(p0);
        
        double c1;
        double c2;

        c1 = w.dotProduct(v);
        if ( c1 <= 0.0 ) {
            return VSDK.vectorDistance(p, p0);
        }
        
        c2 = v.dotProduct(v);
        if ( c2 <= c1 ) {
            return VSDK.vectorDistance(p, p1);
        }
        
        double b;
        b = c1 / c2;
        Vector3D pb;
        pb = p0.add(v.multiply(b));
           
        return VSDK.vectorDistance(p, pb);    
    }
    
    /**
    Given a line that passes between points `p0` and `p1`, this method 
    determines if point `p` falls under `distanceTolerance` in such line.
    @param p0
    @param p1
    @param p
    @param distanceTolerance
    @return 
    */
    public static int lineContainmentTest(
        Vector3D p0, 
        Vector3D p1,
        Vector3D p, 
        double distanceTolerance) 
    {
        double d;

        d = lineToPointDistance(p0, p1, p);
        
        if ( d <= distanceTolerance ) {
            return Geometry.LIMIT;
        }
        return Geometry.OUTSIDE;
    }

    /**
    Given a line that passes between points `p0` and `p1`, this method 
    determines if point `p` falls under `distanceTolerance` in such line.

    This method is functionaly equivalent to procedures `contev` and
    `intrev` from program [MANT1988].13.3. and section [MANT1988].13.2.2.
    @param p0
    @param p1
    @param p
    @param distanceTolerance
    @return 
    */
    public static int lineSegmentContainmentTest(
        Vector3D p0, 
        Vector3D p1,
        Vector3D p, 
        double distanceTolerance) 
    {
        double d;
        Vector3D a, b;

        a = p1.substract(p0);
        b = p.substract(p0);

        double denominator = a.length();
        if ( denominator < VSDK.EPSILON ) return Geometry.OUTSIDE;

        double numerator = a.crossProduct(b).length();
        d = numerator / denominator;

        if ( d <= distanceTolerance ) {
            double t = a.dotProduct(b) / a.dotProduct(a);
            if ( t < -VSDK.EPSILON || t > 1+VSDK.EPSILON ) return Geometry.OUTSIDE;

            return Geometry.LIMIT;
        }
        return Geometry.OUTSIDE;
    }

    /**
    This method calculates containment test for triangle defined by its
    3 vertex positions.  It implements a region classification based
    strategy proposed in [BAER2002].  For a given triangle and with respect
    to triangle's containing plane, a poing lies in one of 7 regions:
       - R1: inside triangle
       - R2: outside triangle, near edge 1
       - R3: outside triangle, near edge 2
       - R4: outside triangle, near edge 3
       - R5: outside triangle, near vertex 1
       - R6: outside triangle, near vertex 2
       - R7: outside triangle, near vertex 3
    @param p0
    @param p1
    @param p2
    @param p
    @param distanceTolerance
    @return 
    */
    public static int triangleContainmentTest(
        Vector3D p0, 
        Vector3D p1, 
        Vector3D p2, 
        Vector3D p,
        double distanceTolerance)
    {
        Vector3D n, a, b;

        a = p1.substract(p0);
        b = p2.substract(p0);
        n = a.crossProduct(b);
        n.normalize();

        workPlane.setFromPointNormal(p0, n);

        if ( workPlane.doContainmentTest(p, distanceTolerance) == 
             Geometry.LIMIT ) {
            // Barycentric coordinates test containment technique (Region 1)
            Vector3D c = p.substract(p0);
            double dot00, dot01, dot02, dot11, dot12, invDenom, u, v;
            dot00 = a.dotProduct(a);
            dot01 = a.dotProduct(b);
            dot02 = a.dotProduct(c);
            dot11 = b.dotProduct(b);
            dot12 = b.dotProduct(c);
            invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
            u = (dot11 * dot02 - dot01 * dot12) * invDenom;
            v = (dot00 * dot12 - dot01 * dot02) * invDenom;
            if ( (u >= 0) && (v >= 0) && (u + v <= 1) ) {
                // R1
                return Geometry.LIMIT;
            }
            else if ( (u <= 0) && (v >= 0) && (u + v <= 1) ) {
                // R2
                return
                    lineSegmentContainmentTest(p0, p2, p, distanceTolerance);
            }
            else if ( (u >= 0) && (v <= 0) && (u + v <= 1) ) {
                // R3
                return
                    lineSegmentContainmentTest(p0, p1, p, distanceTolerance);
            }
            else if ( (u >= 0) && (v >= 0) && (u + v >= 1) ) {
                // R4
                return
                    lineSegmentContainmentTest(p1, p2, p, distanceTolerance);
            }
            else if ( (u <= 0) && (v <= 0) ) {
                // R5
                if ( VSDK.vectorDistance(p, p0) < distanceTolerance ) {
                    return Geometry.LIMIT;
                }
            }
            else if ( (u <= 0) && (v >= 1) ) {
                // R6
                if ( VSDK.vectorDistance(p, p2) < distanceTolerance ) {
                    return Geometry.LIMIT;
                }
            }
            else {
                // R7
                if ( VSDK.vectorDistance(p, p1) < distanceTolerance ) {
                    return Geometry.LIMIT;
                }
            }
        }
        return Geometry.OUTSIDE;
    }

    public static void triangleMinMax(
        Vector3D p0, Vector3D p1, Vector3D p2, double minMax[])
    {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;

        if ( p0.x < minX ) minX = p0.x;
        if ( p0.y < minY ) minY = p0.y;
        if ( p0.z < minZ ) minZ = p0.z;
        if ( p0.x > maxX ) maxX = p0.x;
        if ( p0.y > maxY ) maxY = p0.y;
        if ( p0.z > maxZ ) maxZ = p0.z;

        if ( p1.x < minX ) minX = p1.x;
        if ( p1.y < minY ) minY = p1.y;
        if ( p1.z < minZ ) minZ = p1.z;
        if ( p1.x > maxX ) maxX = p1.x;
        if ( p1.y > maxY ) maxY = p1.y;
        if ( p1.z > maxZ ) maxZ = p1.z;

        if ( p2.x < minX ) minX = p2.x;
        if ( p2.y < minY ) minY = p2.y;
        if ( p2.z < minZ ) minZ = p2.z;
        if ( p2.x > maxX ) maxX = p2.x;
        if ( p2.y > maxY ) maxY = p2.y;
        if ( p2.z > maxZ ) maxZ = p2.z;

        minMax[0] = minX;
        minMax[1] = minY;
        minMax[2] = minZ;
        minMax[3] = maxX;
        minMax[4] = maxY;
        minMax[5] = maxZ;
    }

    /**
    Implementation of the Cohen-Sutherland line clipping algorithm on two
    Dimensions
    @param p0 first point of the line to be clipped
    @param p1 second point of the line to be clipped
    @param min coordinate with the minimum (x,y) values of the clipping
    rectangle
    @param max coordinate with the maximum (x,y) values of the clipping
    rectangle
    @param tolerance could be 0, or an small value. Positive makes area smaller,
    negative makes area bigger.
    @return <code> true </code> when there is at least a portion of the line
    inside the clipping rectangle and <code> false </code> when the line is
    outside the clipping rectangle.
    */
    public static boolean cohenSutherlandLineClipping2D(
        Vector2D p0, 
        Vector2D p1,
        Vector2D min, 
        Vector2D max,
        double tolerance)
    {
        double aux;
        if ( min.x > max.x ) {
            aux = min.x;
            min.x = max.x;
            max.x = aux;
        }
        if ( min.y > max.y ) {
            aux = min.y;
            min.y = max.y;
            max.y = aux;
        }
        
        if ( max.x - min.x < VSDK.EPSILON ||
             max.y - min.y < VSDK.EPSILON ) {
            //System.out.println("Error: to small area");
            return false;
        }

        int outCode0 = computeCohenSutherland2DCode(p0, min, max, tolerance);
        int outCode1 = computeCohenSutherland2DCode(p1, min, max, tolerance);
        int outCodeOut;
        boolean accept = false;
        double x = 0.0;
        double y = 0.0;
        
        while ( !accept ) {
            if ( (outCode0 | outCode1) == 0 ) {
                // Both endpoints are inside clip rectangle
                accept = true;
                break;
            }
            else if ( (outCode0 & outCode1) != 0 ) {
                // Both endpoints are outside one of the 4 limit lines
                accept = false;
                break;
            } 
            else {
                // Line should be trimmed
                double e = 2*tolerance; //2*VSDK.EPSILON;
                outCodeOut = (outCode0 != 0) ? outCode0 : outCode1;
                if ( (outCodeOut & COHEN_SUTHERLAND_2D_TOP) == 
                     COHEN_SUTHERLAND_2D_TOP) {
                    x = p0.x + 
                        (p1.x - p0.x) * ((max.y-e) - p0.y) / (p1.y - p0.y);
                    y = max.y - e;
                }
                else if ( (outCodeOut & COHEN_SUTHERLAND_2D_BOTTOM) == 
                    COHEN_SUTHERLAND_2D_BOTTOM) {
                    x = p0.x + (p1.x - p0.x) * (min.y+e - p0.y) / (p1.y - p0.y);
                    y = min.y+e;
                }
                else if ( (outCodeOut & COHEN_SUTHERLAND_2D_RIGHT) == 
                    COHEN_SUTHERLAND_2D_RIGHT) {
                    y = p0.y + (p1.y - p0.y) * (max.x-e - p0.x) / (p1.x - p0.x);
                    x = max.x-e;
                }
                else if ( (outCodeOut & COHEN_SUTHERLAND_2D_LEFT) == 
                    COHEN_SUTHERLAND_2D_LEFT ) {
                    y = p0.y + (p1.y - p0.y) * (min.x+e - p0.x) / (p1.x - p0.x);
                    x = min.x+e;
                }
            }
            if ( outCodeOut == outCode0 ) {
                p0.x = x;
                p0.y = y;
                outCode0 = computeCohenSutherland2DCode(p0, min, max, tolerance);
            }
            else {
                p1.x = x;
                p1.y = y;
                outCode1 = computeCohenSutherland2DCode(p1, min, max, tolerance);
            }            
        }
        return accept;
    }

    /**
    Compute a code of the position of a point relative to the clipping
    rectangle
    @param p point to be located
    @param min
    @param max
    @return the code in four bits representing the location of the point
    */
    private static int computeCohenSutherland2DCode(
        Vector2D p, Vector2D min, Vector2D max, double tolerance){
        int outCode;
        outCode = COHEN_SUTHERLAND_2D_INSIDE;
        double e = tolerance;

        if ( p.x < min.x + e ) {
            outCode |= COHEN_SUTHERLAND_2D_LEFT;
        }
        if ( p.x > max.x - e ) {
            outCode |= COHEN_SUTHERLAND_2D_RIGHT;
        }
        if ( p.y < min.y + e ) {
            outCode |= COHEN_SUTHERLAND_2D_BOTTOM;
        }
        if ( p.y > max.y - e ) {
            outCode |= COHEN_SUTHERLAND_2D_TOP;
        }
        return outCode;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
