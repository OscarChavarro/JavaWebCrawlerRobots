//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - November 3 2006 - Oscar Chavarro: Original base version               =
//===========================================================================

package vsdk.toolkit.common.linealAlgebra;

import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.FundamentalEntity;

public class Vector4D extends FundamentalEntity 
{
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20061103L;

    /// Yes, they are public due to efficiency issues
    public double x, y, z, w;

    /**
     The default Vector4D value is the zero vector in 3D (Unit in homogenous
     space
     */
    public Vector4D() {
        x = 0;
        y = 0;
        z = 0;
        w = 1;
    }

    /**
     *
     * @param x double
     * @param y double
     * @param z double
     */
    public Vector4D(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
        w = 1;
    }

    public Vector4D(double x, double y, double z, double w) {
        this.x = x; this.y = y; this.z = z;
        this.w = w;
    }

    public Vector4D(Vector3D B) {
        this.x = B.x; this.y = B.y; this.z = B.z;
        w = 1;
    }

    public Vector4D(Vector4D B) {
        this.x = B.x; this.y = B.y; this.z = B.z;
        this.w= B.w;
    }

    public final Vector4D multiply(double a) {
        return new Vector4D(a * x, a * y, a * z, a * w);
    }

    /**
     *
     * @param B Vector4D
     * @return Vector4D
     */
/*
NOT DEFINED YET!
    public final Vector4D crossProduct(Vector4D B) {
        return new Vector4D(?);
    }
*/
    /**
     *
     * @param B Vector4D
     * @return double
     */
/*
NOT DEFINED YET!
    public final double dotProduct(Vector4D B) {
        return ?
    }
*/
    /**
     *
     */
    public final void normalize() {
        double t = x*x + y*y + z*z + w*w;
        if ( Math.abs(t) < VSDK.EPSILON ) return;
        if (t != 0 && t != 1) t = (1.0 / Math.sqrt(t));
        x *= t;
        y *= t;
        z *= t;
        w *= t;
    }

    public final void divideByW() {
        if ( Math.abs(w) < VSDK.EPSILON ) return;
        x /= w;
        y /= w;
        z /= w;
        w /= w;
    }

    /**
     *
     * @return double
     */
    public final double length() {
        return Math.sqrt(x*x + y*y + z*z + w*w);
    }

    public final Vector4D add(Vector4D b)
    {
        return new Vector4D(x + b.x, y + b.y, z + b.z, w + b.w);
    }

    public final Vector4D substract(Vector4D b)
    {
        return new Vector4D(x - b.x, y - b.y, z - b.z, w - b.w);
    }

    public float[] exportFloatToArray()
    {
        float[] ret={(float)x, (float)y, (float)z, (float)w};
        return ret;
    }

    public double[] exportToDoubleArray()
    {
        double[] ret={x, y, z, w};
        return ret;
    }

    /**
    Provides an object to text report convertion, optimized for human
    readability and debugging. Do not use for serialization or persistence
    purposes.
    @return human readable representation of current vector
    */
    @Override
    public String toString()
    {
        String msg;

        msg = "<" + VSDK.formatDouble(x) + ", " + VSDK.formatDouble(y) +
              ", " + VSDK.formatDouble(z) + ", " + VSDK.formatDouble(w) + ">";

        return msg;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
