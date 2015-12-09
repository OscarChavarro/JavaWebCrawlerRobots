//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 21 2007 - Oscar Chavarro: Original base version                   =
//===========================================================================

package vsdk.toolkit.processing;

import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.io.PersistenceElement;

public class SpharmonicKitWrapper extends ProcessingElement {

    static boolean loaded = false;

    private static native boolean
    executeSphericalHarmonics(
        byte inImage[],
        double outSphericalHarmonicsR[],
        double outSphericalHarmonicsI[]);

    public static boolean
    calculateSphericalHarmonics(
        byte inImage[],
        double outSphericalHarmonicsR[],
        double outSphericalHarmonicsI[])
    {
        if ( !loaded ) {
            // Not working!
            //System.setProperty("java.library.path", "./bin");
            //System.out.println(System.getProperty("java.library.path"));

            if ( !PersistenceElement.verifyLibrary("spharmonickit") ) {
                VSDK.reportMessage(null, VSDK.ERROR,
                  "SpharmonicKitWrapper.calculateSphericalHarmonicLenghts",
"Native library spharmonickit not available. Check you have it installed\n" + 
"globally, or that you used the -Djava.library.path=foldercontaning.dllor.so\n" +
"in the command line for java interpreter JVM.\n" + 
"Further error reporting in this issue disabled.");
                return false;
            }
            System.loadLibrary("spharmonickit");
            loaded = true;
        }
        if ( loaded == true ) {
            return executeSphericalHarmonics(inImage,
                outSphericalHarmonicsR, outSphericalHarmonicsI);
        }
        else {
            return false;
        }
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
