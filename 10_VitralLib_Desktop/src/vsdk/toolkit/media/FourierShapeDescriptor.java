//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 18 2007 - Oscar Chavarro: Original base version                   =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [FUNK2003], Funkhouser, Thomas.  Min, Patrick. Kazhdan, Michael. Chen,  =
//=     Joyce. Halderman, Alex. Dobkin, David. Jacobs, David. "A Search     =
//=     Engine for 3D Models", ACM Transactions on Graphics, Vol 22. No1.   =
//=     January 2003. Pp. 83-105                                            =
//===========================================================================

package vsdk.toolkit.media;

import vsdk.toolkit.common.VSDK;

/**
Stores the feature vector for a set of 32 elements
around a volume or image as described in [FUNK2003]
\todo  Generalize for variable number of elements and harmonics
*/
public class FourierShapeDescriptor extends ShapeDescriptor
{
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20070523L;

    private double featureVector[];
    private static final int numberOfElements = 32;
    private static final int numberOfHarmonics = 16;

    public FourierShapeDescriptor(String label)
    {
        super(label);
        featureVector = new double[numberOfElements*numberOfHarmonics];
    }

    /**
    Set the Fourier transform (spherical harmonic) for sphere `sphere`, harmonic `harmonic` to complex value <r, i>
    */
    public void setFeature(int sphere, int harmonic, double r, double i)
    {
        if ( sphere < 0 || sphere >= numberOfElements || harmonic < 0 || harmonic >= numberOfHarmonics ) {
            return;
        }
        double harmonicAmplitude = Math.sqrt(r*r + i*i);
        featureVector[sphere*numberOfHarmonics+harmonic] = harmonicAmplitude;
    }

    @Override
    public String toString()
    {
        String msg;
        msg = "SphericalHarmonics amplitudes for " + numberOfElements + " spheres and " + numberOfHarmonics + " harmonics:\n";
        int i;
        for ( i = 0; i < numberOfElements*numberOfHarmonics; i++ ) {
            msg += "  - " + VSDK.formatDouble(featureVector[i]) + "\n";
        }
        return msg;
    }

    @Override
    public double [] getFeatureVector() {
        return featureVector;
    }

    @Override
    public void setFeatureVector(double vector[]) {
        if ( vector.length != numberOfElements*numberOfHarmonics ) {
            VSDK.reportMessage(this, VSDK.ERROR, "setFeatureVector",
                "Trying to set featurevector from incorrectly sized data!");
            return;
        }
        featureVector = new double[numberOfElements*numberOfHarmonics];
        int i;
        for ( i = 0; i < featureVector.length; i++ ) {
            featureVector[i] = vector[i];
        }
    }

    @Override
    public void finalize()
    {
        label = null;
        featureVector = null;
        try {
            super.finalize();
        } catch (Throwable ex) {
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
