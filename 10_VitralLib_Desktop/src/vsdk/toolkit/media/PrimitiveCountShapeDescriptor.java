//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - June 17 2007 - Oscar Chavarro: Original base version                  =
//===========================================================================

package vsdk.toolkit.media;

import vsdk.toolkit.common.VSDK;

/**
Stores primitive counts as a feature vector. Based on the primitive types
defined in the VSDK utility class.
*/
public class PrimitiveCountShapeDescriptor extends ShapeDescriptor
{
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20070523L;

    private double featureVector[];
    private static final int numberOfElements = VSDK.PRIMITIVE_TYPE_COUNT;

    public PrimitiveCountShapeDescriptor(String label)
    {
        super(label);
        featureVector = new double[numberOfElements];
        int i;
        for ( i = 0; i < featureVector.length; i++ ) {
            featureVector[i] = 0.0;
        }
    }

    /**
    */
    public void setFeature(int primitiveType, long count)
    {
        if ( primitiveType < 0 || primitiveType >= numberOfElements ) {
            return;
        }
        featureVector[primitiveType] = count;
    }

    @Override
    public String toString()
    {
        String msg;
        msg = "Primitive counts for " + numberOfElements + " types:\n";
        int i;
        for ( i = 0; i < numberOfElements; i++ ) {
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
        if ( vector.length != numberOfElements ) {
            VSDK.reportMessage(this, VSDK.ERROR, "setFeatureVector",
                "Trying to set featurevector from incorrectly sized data!");
            return;
        }
        featureVector = new double[numberOfElements];
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
