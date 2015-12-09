//===========================================================================
//= References:                                                             =
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - June 8 2005 - Oscar Chavarro: Original base version                   =
//=-------------------------------------------------------------------------=
//= [.wPRIN2007] Princeton University, "Introduction to programming in      =
//=     Java", course notes available at                                    =
//=     http://www.cs.princeton.edu/introcs/97data/                         =
//=     last accessed, june 8 2007.                                         =
//===========================================================================

package vsdk.toolkit.common;

/**
A complex child is one with a real mother and an imaginary father.

Current implementation of complex number based on (borrowed from, jeje)
sample in [.wPRIN2007].9 course notes.
*/
public class Complex extends FundamentalEntity {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20070712L;

    /// Current complex number real part
    public double r;
    /// Current complex number imaginary part
    public double i;

    /**
    Create a new object with the given real and imaginary parts
    @param real
    @param imag
    */
    public Complex(double real, double imag) {
        r = real;
        i = imag;
    }

    /**
    Return abs/modulus/magnitude
    @return complex number magnitude
    */
    public double abs() {
        return Math.hypot(r, i); // Math.sqrt(r*r + i*i)
    }

    /**
    Return angle/phase/argument
    @return complex number phase
    */
    public double phase() {
        // between -pi and pi
        return Math.atan2(i, r);
    }

    /**
    Return a new Complex object whose value is (this + b)
    @param b
    @return complex number with the result of adding two other complex numbers
    */
    public Complex plus(Complex b) {
        double real = this.r + b.r;
        double imag = this.i + b.i;
        return new Complex(real, imag);
    }

    /**
    Return a new Complex object whose value is (this - b)
    @param b
    @return negated complex number
    */
    public Complex minus(Complex b) {
        Complex a = this;
        double real = a.r - b.r;
        double imag = a.i - b.i;
        return new Complex(real, imag);
    }

    /**
    Return a new Complex object whose value is (this * b)
    @param b
    @return a new complex number with the result of the multiplication of other
    two complex numbers
    */
    public Complex times(Complex b) {
        Complex a = this;
        double real = a.r * b.r - a.i * b.i;
        double imag = a.r * b.i + a.i * b.r;
        return new Complex(real, imag);
    }

    /**
    Scalar multiplication
    Return a new object whose value is (this * alpha)
    @param alpha
    @return scalar multiplied complex number
    */
    public Complex times(double alpha) {
        return new Complex(alpha * r, alpha * i);
    }

    /**
    Return a new Complex object whose value is the conjugate of this
    @return conjugate complex number
    */
    public Complex conjugate() {
      return new Complex(r, -i);
    }

    /**
    Return a new Complex object whose value is the reciprocal of this
    @return reciprocal complex number
    */
    public Complex reciprocal() {
        double scale = r*r + i*i;
        return new Complex(r / scale, -i / scale);
    }

    /**
    Return a / b
    @param b
    @return a new complex number with the division result of other two
    complex numbers
    */
    public Complex divides(Complex b) {
        Complex a = this;
        return a.times(b.reciprocal());
    }

    /**
    Return a new Complex object whose value is the complex exponential of this
    @return exponent of current complex number 
    */
    public Complex exp() {
        return new Complex(Math.exp(r) * Math.cos(i), Math.exp(r) * Math.sin(i));
    }

    /**
    Return a new Complex object whose value is the complex sine of this
    @return complex sine
    */
    public Complex sin() {
        return new Complex(Math.sin(r) * Math.cosh(i), Math.cos(r) * Math.sinh(i));
    }

    /**
    Return a new Complex object whose value is the complex cosine of this
    @return complex cosine
    */
    public Complex cos() {
        return new Complex(Math.cos(r) * Math.cosh(i), -Math.sin(r) * Math.sinh(i));
    }

    /**
    Return a new Complex object whose value is the complex tangent of this
    @return complex tangent
    */
    public Complex tan() {
        return sin().divides(cos());
    }

    /**
    A static version of plus
    @param a
    @param b
    @return a new complex number with the addition result of other two complex
    numbers
    */
    public static Complex plus(Complex a, Complex b) {
        double real = a.r + b.r;
        double imag = a.i + b.i;
        Complex sum = new Complex(real, imag);
        return sum;
    }

    /**
    Return a string representation of the invoking Complex object
    @return human readable representation of current complex number
    */
    @Override
    public String toString() {
        if ( i <  0 ) {
            return VSDK.formatDouble(r) + " - " + VSDK.formatDouble(-i) + "i";
        }
        return VSDK.formatDouble(r) + " + " + VSDK.formatDouble(i) + "i";
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
