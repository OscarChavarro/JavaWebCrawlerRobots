//===========================================================================
package vsdk.toolkit.render;

import vsdk.toolkit.media.RGBImage;
import vsdk.toolkit.media.RGBPixel;
import vsdk.toolkit.media.ZBuffer;
import vsdk.toolkit.processing.ImageProcessing;

/**
The AutoStereogramGenerator class implements an strategy for calculating
autostereographic images from random color tiles or SIRDS.

The algorithm in this class is an adapted version of "OpenStereogram" system
available at http://code.google.com/p/openstereogram.
*/
public class AutoStereogramGenerator extends RenderingElement
{
    private static int getMinDepth(double separationFactor, int maxdepth, int observationDistance, int suppliedMinDepth) {
        int computedMinDepth = (int)( (separationFactor * maxdepth * observationDistance) /
            (((1 - separationFactor) * maxdepth) + observationDistance) );
        
        return Math.min( Math.max( computedMinDepth, suppliedMinDepth), maxdepth);
    }

    private static int getMaxDepth(int suppliedMaxDepth, int observationDistance) {
        return Math.max( Math.min( suppliedMaxDepth, observationDistance), 0);
    }
    
    private static int convertoToPixels(double valueInches, int ppi) {
        return (int)(valueInches * ppi);
    }

    /** Given a double depth value (usually from 0 to 1), this function
    scales it linearly and clamps it to [minDepth, maxDepth],
    usually [0, 255] */
    private static int getZ(double depth, int maxDepth, int minDepth) {
        return (((int)(depth * 255)) * (maxDepth - minDepth) / 255);
    }
    
    private static int getSeparation(int observationDistance, int eyeSeparation, int depth) {
        return (eyeSeparation * depth) / (depth + observationDistance);
    }

    /**
    Easy to use version of `generate` method with some common defaults.
    */
    public static void generate(
        RGBImage result, RGBImage tilePattern, ZBuffer depthMap)
    {
        generate(result, tilePattern, depthMap, 14.0, 2.5, 12, 0, 81, 81, 0, 0);
    }

    /**
    Given a z-buffer depth map, this method fills in the `resulting` image,
    which must be previously initialized to be the same size of the depth map.

    The method builds a SIRD (autostereogram) over repetitions of the
    `tilePattern` image, wich must be repeatable.

    Requires result and depthMap to be of equal size
    */
    public static void generate(
        RGBImage result, RGBImage tilePattern, ZBuffer depthMap, 
        double observationDistanceInches, 
        double eyeSeparationInches,
        double maxDepthInches, double minDepthInches,
        int horizontalPPI, int verticalPPI,
        int animationXOffset, int animationYOffset
    )
    {
        int x, y;
        int rdx, rdy, tdx, tdy;

        if ( animationXOffset < 0 ) {
            animationXOffset *= -1;
        }
        if ( animationYOffset < 0 ) {
            animationYOffset *= -1;
        }
        animationXOffset %= 256;
        animationYOffset %= 256;

        rdx = result.getXSize();
        rdy = result.getYSize();

        RGBPixel p;

        p = new RGBPixel();

        int linksL[] = new int[rdx];
        int linksR[] = new int[rdx];

        int observationDistance = convertoToPixels(observationDistanceInches, horizontalPPI);
        int eyeSeparation = convertoToPixels(eyeSeparationInches, horizontalPPI);
        int maxDepth = getMaxDepth( convertoToPixels(maxDepthInches, horizontalPPI), observationDistance );
        int minDepth = getMinDepth( 0.55, maxDepth, observationDistance, convertoToPixels(minDepthInches, horizontalPPI) );
        int verticalShift = verticalPPI / 16;
        int maxSeparation = getSeparation(observationDistance, eyeSeparation, maxDepth);

        RGBImage scaledTilePattern = new RGBImage();
        scaledTilePattern.initNoFill(maxSeparation, (int)(
            ((double)maxSeparation) * ((double)tilePattern.getYSize()) / 
            ((double)tilePattern.getXSize())
        ));

        tdx = scaledTilePattern.getXSize();
        tdy = scaledTilePattern.getYSize();

        ImageProcessing.resize(tilePattern, scaledTilePattern);

        for ( y = 0; y < rdy; y++ ) {
            for ( x = 0; x < rdx; x++ ) {
                linksL[x] = x;
                linksR[x] = x;
            }
            for ( x = 0; x < rdx; x++ ) {
                int depth = getZ(depthMap.getZ(x, y), maxDepth, minDepth);
                int separation = getSeparation(observationDistance, eyeSeparation, depth);
                int left = x - (separation / 2);
                int right = left + separation;
                
                if ( left >= 0 && right < rdx ) {
                    boolean visible = true;
                    
                    if ( linksL[right] != right) {
                        if ( linksL[right] < left) {
                            linksR[linksL[right]] = linksL[right];
                            linksL[right] = right;
                        }
                        else {
                            visible = false;
                        }
                    }
                    if ( linksR[left] != left) {
                        if ( linksR[left] > right) {
                            linksL[linksR[left]] = linksR[left];
                            linksR[left] = left;
                        }
                        else {
                            visible = false;
                        }
                    }
                    
                    if ( visible ) {
                        linksL[right] = left;
                        linksR[left] = right;
                    }
                }
            }
            
            int lastLinked = -10;
            for ( x = 0; x < rdx; x++ ) {

                if ( linksL[x] == x ) {
                    if ( lastLinked == x - 1 ) {
                        result.getPixelRgb(x - 1, y, p);
                        result.putPixelRgb(x, y, p);
                    }
                    else {
                        scaledTilePattern.getPixelRgb(
                            (x + animationXOffset) % maxSeparation,
                            ((y + animationYOffset) + ((x / maxSeparation) * verticalShift)) % tdy, 
                            p);
                        result.putPixelRgb(x, y, p);
                    }
                }
                else {
                    result.getPixelRgb(linksL[x], y, p);
                    result.putPixelRgb(x, y, p);
                    lastLinked = x;
                }
            }
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
