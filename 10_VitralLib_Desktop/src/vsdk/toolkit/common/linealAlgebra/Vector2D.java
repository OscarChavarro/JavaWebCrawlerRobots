//===========================================================================
package vsdk.toolkit.common.linealAlgebra;

import vsdk.toolkit.common.FundamentalEntity;
import vsdk.toolkit.common.VSDK;

/**
Class Vector2D represents a one dimensional array of two values, usually
to be interpreted as:
  - A column vector of 1x2 positions, useful in linear algebra computations.
  - A point in the R2 Euclidean space
As current class is supposed to be used in the context of computer graphics,
array elements are not indexed, to say from 0 to 2, but are instead named
with the usual 2D axis labels `x` and `y`.
This is one of the most fundamental classes in VitralSDK toolkit, and its
attributes are usually accessed in the inner loops of computational intensive
calculations. As such, the attributes are promoted to be public, yes, 
breaking encapsulation and converting current class to a mere non-evolvable
structure.
Lack of get/set method enforces a direct attribute access programming style
which will lend to shorter code.
*/
public class Vector2D extends FundamentalEntity {
    /// Check the general attribute description in superclass Entity.
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final long serialVersionUID = 20060502L;

    /// Yes, they are public due to efficiency issues
    public double x, y;
    
    /**
    The default Vector3D value is the zero value
    */
    public Vector2D() {
        x = 0;
        y = 0;
    }

    /**
    @param x X coordinate
    @param y Y coordinate
    */
    public Vector2D(double x, double y) {
        this.x = x; 
        this.y = y;
    }

    public Vector2D(Vector2D B) {
        this.x = B.x;
        this.y = B.y;
    }
    
    public final Vector2D multiply(double a) {
        return new Vector2D(a * x, a * y);
    }

    /**
    Make this vector internal values equal to the other's.
    @param other
    */
    public final void clone(Vector2D other)
    {
        this.x = other.x;
        this.y = other.y;
    }

    /**
    */
    public final void normalize() {
        double t = x*x + y*y;
        if ( Math.abs(t) < VSDK.EPSILON ) return;
        if (t != 0 && t != 1) t = (1.0 / Math.sqrt(t));
        x *= t;
        y *= t;
    }

    /**
    @return current vector length
    */
    public final double length() {
        return Math.sqrt(x*x + y*y);
    }

    public final Vector2D add(Vector2D b)
    {
        return new Vector2D(x + b.x, y + b.y);
    }
    
    /**
    Stores in `this` Vector2D the result of adding the operands `a` and `b`
    @param a
    @param b
    */
    public final void add(Vector2D a, Vector2D b)
    {
        this.x = a.x + b.x;
        this.y = a.y + b.y;
    }

    public final Vector2D substract(Vector2D b)
    {
        return new Vector2D(x - b.x, y - b.y);
    }

    /**
    Stores in `this` Vector3D the result of subtracting the operands `a` and `b`
    @param a
    @param b
    */
    public final void substract(Vector2D a, Vector2D b)
    {
        this.x = a.x - b.x;
        this.y = a.y - b.y;
    }

    public float[] exportToFloatArrayVect()
    {
        float[] ret={(float)x, (float)y};
        return ret;
    }

    public double[] exportToDoubleArrayVect()
    {
        double[] ret={x, y};
        return ret;
    }

    /**
    Provides an object to text report a convert, optimized for human
    readability and debugging. Do not use for serialization or persistence
    purposes.
    @return human readable representation of current vector
    */
    @Override
    public String toString()
    {
        String msg;

        msg = "<" + VSDK.formatDouble(x) + ", " + VSDK.formatDouble(y) + ">";

        return msg;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
