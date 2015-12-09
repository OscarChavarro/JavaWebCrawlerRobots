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

package vsdk.toolkit.processing;

import vsdk.toolkit.common.Complex;

public class SignalProcessing extends ProcessingElement {

    /**
    Compute the FFT of x[], assuming its length is a power of 2.
    Bare bones implementation that runs in O(N log N) time. Design goal
    is to optimize the clarity of the code, rather than performance.
    Not the most memory efficient algorithm (because it uses
    an object type for representing complex numbers and because
    it re-allocates memory for the subarray, instead of doing
    in-place or reusing a single temporary array)
    Current implementation of complex number based on (borrowed from, jeje)
    sample in [.wPRIN2007].9 course notes.
    */
    public static Complex[] fft(Complex[] x) {
        int N = x.length;

        // Base case
        if (N == 1) return new Complex[] { x[0] };

        // Radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) {
            throw new RuntimeException("N is not a power of 2");
        }

        // Fft of even terms
        Complex[] even = new Complex[N/2];
        for (int k = 0; k < N/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = fft(even);

        // Fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < N/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = fft(odd);

        // Combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N/2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].plus(wk.times(r[k]));
            y[k + N/2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }


    /**
    Compute the inverse FFT of x[], assuming its length is a power of 2.
    Bare bones implementation that runs in O(N log N) time. Design goal
    is to optimize the clarity of the code, rather than performance.
    Not the most memory efficient algorithm (because it uses
    an object type for representing complex numbers and because
    it re-allocates memory for the subarray, instead of doing
    in-place or reusing a single temporary array)
    Current implementation of complex number based on (borrowed from, jeje)
    sample in [.wPRIN2007].9 course notes.
    */
    public static Complex[] ifft(Complex[] x) {
        int N = x.length;
        Complex[] y = new Complex[N];

        // Take conjugate
        for (int i = 0; i < N; i++) {
            y[i] = x[i].conjugate();
        }

        // Compute forward FFT
        y = fft(y);

        // Take conjugate again
        for (int i = 0; i < N; i++) {
            y[i] = y[i].conjugate();
        }

        // Divide by N
        for (int i = 0; i < N; i++) {
            y[i] = y[i].times(1.0 / N);
        }

        return y;
    }

    /**
    Compute the circular convolution of x and y.
    Current implementation of complex number based on (borrowed from, jeje)
    sample in [.wPRIN2007].9 course notes.
    */
    public static Complex[] circularConvolve(Complex[] x, Complex[] y) {

        // Should probably pad x and y with 0s so that they have same length
        // and are powers of 2
        if (x.length != y.length) {
            throw new RuntimeException("Dimensions don't agree");
        }

        int N = x.length;

        // Compute FFT of each sequence
        Complex[] a = fft(x);
        Complex[] b = fft(y);

        // Point-wise multiply
        Complex[] c = new Complex[N];
        for (int i = 0; i < N; i++) {
            c[i] = a[i].times(b[i]);
        }

        // Compute inverse FFT
        return ifft(c);
    }


    /**
    Compute the linear convolution of x and y.
    Current implementation of complex number based on (borrowed from, he he)
    sample in [.wPRIN2007].9 course notes.
    */
    public static Complex[] linearConvolve(Complex[] x, Complex[] y) {
        Complex ZERO = new Complex(0, 0);

        Complex[] a = new Complex[2*x.length];

        //System.arraycopy(x, 0, a, 0, x.length);
        for (int i = 0;        i <   x.length; i++) a[i] = x[i];
        for (int i = x.length; i < 2*x.length; i++) a[i] = ZERO;

        Complex[] b = new Complex[2*y.length];

        //System.arraycopy(y, 0, b, 0, y.length);
        for (int i = 0;        i <   y.length; i++) b[i] = y[i];
        for (int i = y.length; i < 2*y.length; i++) b[i] = ZERO;

        return circularConvolve(a, b);
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
