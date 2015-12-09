//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 30 2007 - Oscar Chavarro: Original base version                   =
//===========================================================================

package vsdk.toolkit.processing;

import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.media.Image;
import vsdk.toolkit.media.IndexedColorImage;
import vsdk.toolkit.media.RGBPixel;

/**
\todo  Current implementation is not well designed. This class' design should
be checked to inforce:
  - Interoperability with existing image processing toolkits/frameworks like
    JAI, ITK, Khoros, OpenCV, Matlab+ImageToolbox, ImageMagick+JMagick, GIMP,
    etc.
  - Programmability of image processing operations using GPUs
  - Filter graph approach
*/

public abstract class ImageProcessing extends ProcessingElement {

    private static int
    gammaCorrection8bits(int in, double gamma)
    {
        double a, b;
        int out;

        a = ((double)in) / 255.0;
        b = Math.pow(a, 1.0/gamma);
        out = (int)(b*255.0);

        return out;
    }

    public static void
    gammaCorrection(IndexedColorImage img, double gamma)
    {
        int x, y;
        int val;

        for ( x = 0; x < img.getXSize(); x++ ) {
            for ( y = 0; y < img.getYSize(); y++ ) {
                val = img.getPixel(x, y);
                val = gammaCorrection8bits(val, gamma);
                img.putPixel(x, y, VSDK.unsigned8BitInteger2signedByte(val));
            }
        }
    }

    /**
    Given the `input` and `output` previously created images, fills in
    `output`'s space the `this` image using bilinear interpolation.
    \todo  worked well only in the growing case. Must add the shrinking
    case for area averaging.
    */
    public static void resize(Image input, Image output)
    {
        int xSizeIn = input.getXSize();
        int ySizeIn = input.getYSize(); 
        int xSizeOut = output.getXSize();
        int ySizeOut = output.getYSize();
        double u, v;
        int x, y;
        ColorRgb source;
        RGBPixel target = new RGBPixel();
        RGBPixel acum;

        if ( xSizeOut == xSizeIn && ySizeOut == ySizeIn ) {
            copy(input, output);
        }
        else if ( xSizeOut > xSizeIn && ySizeOut > ySizeIn ) {
            for ( x = 0; x < xSizeOut; x++ ) {
                for ( y = 0; y < ySizeOut; y++ ) {
                    u = ((double)x)/((double)(xSizeOut));
                    v = ((double)y)/((double)(ySizeOut));
                    source = input.getColorRgbBiLinear(u, v);
                    target.r = VSDK.unsigned8BitInteger2signedByte((int)(source.r*255));
                    target.g = VSDK.unsigned8BitInteger2signedByte((int)(source.g*255));
                    target.b = VSDK.unsigned8BitInteger2signedByte((int)(source.b*255));
                    output.putPixelRgb(x, y, target);
                }
            }
        }
        else {
            output.init(xSizeOut, ySizeOut);
            double xf = (((double)xSizeIn) / ((double)xSizeOut));
            double yf = (((double)ySizeIn) / ((double)ySizeOut));
            int xfi = (int)xf;
            int yfi = (int)yf;
            double w = (xfi * yfi);
            double acumr;
            double acumg;
            double acumb;

            int xx, yy, x0, y0, x1, y1;
            acum = new RGBPixel();
            for ( xx = 0; xx < xSizeOut; xx++ ) {
                for ( yy = 0; yy < ySizeOut; yy++ ) {
                    acumr = acumg = acumb = 0.0;

                    x0 = (int)(((double)xx)*xf);
                    x1 = x0 + xfi;
                    for ( x = x0; x < x1 && x < xSizeIn; x++ ) {
                        y0 = (int)(((double)yy)*yf);
                        y1 = y0 + yfi;
                        for ( y = y0; y < y1 && y < ySizeIn; y++ ) {
                            target = input.getPixelRgb(x, y);
                            acumr += ((double)VSDK.signedByte2unsignedInteger(target.r)) / w;
                            acumg += ((double)VSDK.signedByte2unsignedInteger(target.g)) / w;
                            acumb += ((double)VSDK.signedByte2unsignedInteger(target.b)) / w;
                        }
                    }

                    if ( acumr >= 255.0 ) acumr = 255.0;
                    if ( acumg >= 255.0 ) acumg = 255.0;
                    if ( acumb >= 255.0 ) acumb = 255.0;

                    acum.r = VSDK.unsigned8BitInteger2signedByte((int)(acumr));
                    acum.g = VSDK.unsigned8BitInteger2signedByte((int)(acumg));
                    acum.b = VSDK.unsigned8BitInteger2signedByte((int)(acumb));

                    output.putPixelRgb(xx, yy, acum);
                }
            }
        }
    }

    /**
    Copies the contents from the `input` image to the `output` image. Note that
    this method can serve also as a format conversion between different Image
    formats (i.e. convert an RGBImage to an IndexedColorImage).
    */
    public static void copy(Image input, Image output)
    {
        int xSize = input.getXSize();
        int ySize = input.getYSize();
        int x, y;
        RGBPixel target;

        output.init(xSize, ySize);
        for ( x = 0; x < xSize; x++ ) {
            for ( y = 0; y < ySize; y++ ) {
                target = input.getPixelRgb(x, y);
                output.putPixelRgb(x, y, target);
            }
        }
    }

    /**
    Takes the greater dimension of the input image, and uses it to create an
    output square image of such dimension. Then copies the input image
    centered in the output image.
    */
    public static void squareFill(Image input, Image output)
    {
        int xSize = input.getXSize();
        int ySize = input.getYSize();
        int x, y;
        RGBPixel target;
        int maxSize = xSize;

        if ( ySize > maxSize ) {
            maxSize = ySize;
        }
        output.init(maxSize, maxSize);

        int dx, dy;
        dx = (maxSize-xSize)/2;
        dy = (maxSize-ySize)/2;
        for ( x = 0; x < xSize; x++ ) {
            for ( y = 0; y < ySize; y++ ) {
                target = input.getPixelRgb(x, y);
                output.putPixelRgb(x+dx, y+dy, target);
            }
        }
    }

    /**
    Grows the input image copying the input in the center of a border of
    `border` size.
    */
    public static void frame(Image input, Image output, int border)
    {
        int xSize = input.getXSize();
        int ySize = input.getYSize();
        int x, y;
        RGBPixel target;

        output.init(xSize+2*border, ySize+2*border);

        int dx, dy;
        for ( x = 0; x < xSize; x++ ) {
            for ( y = 0; y < ySize; y++ ) {
                target = input.getPixelRgb(x, y);
                output.putPixelRgb(x+border, y+border, target);
            }
        }
    }

    /**
    This method extracts a region of interest from source image rectangle
    including points from <x0Roi, y0Roi> to <x1Roi, y1Roi>.
    */
    public static void extractRoi(Image source, Image roi,
        int x0Roi, int y0Roi, int x1Roi, int y1Roi)
    {
        int tmp;
        int dx, dy;

        //-----------------------------------------------------------------
        if ( x0Roi > x1Roi ) {
            tmp = x0Roi;
            x0Roi = x1Roi;
            x1Roi = tmp;
        }
        if ( y0Roi > y1Roi ) {
            tmp = y0Roi;
            y0Roi = y1Roi;
            y1Roi = tmp;
        }
        if ( x0Roi < 0 ) x0Roi = 0;
        if ( y0Roi < 0 ) y0Roi = 0;
        if ( x1Roi >= source.getXSize() ) x1Roi = source.getXSize()-1;
        if ( y1Roi >= source.getYSize() ) y1Roi = source.getYSize()-1;
        if ( x0Roi >= source.getXSize() ||
             y0Roi >= source.getYSize()) {
            return;
        }
        dx = x1Roi - x0Roi + 1;
        dy = y1Roi - y0Roi + 1;

        //-----------------------------------------------------------------
        RGBPixel target;
        int x, y;

        roi.init(dx, dy);
        for ( x = 0; x < dx; x++ ) {
            for ( y = 0; y < dy; y++ ) {
                target = source.getPixelRgb(x0Roi+x, y0Roi+y);
                roi.putPixelRgb(x, y, target);
            }
        }
    }

    /**
    A distance field is a scalar map where each pixel value correspond to the
    nearest distance to an "inside" pixel.
    Every pixel in the input image with a value greater or equal to `threshold`
    will be noted as "inside", otherwise will be "outside".
    This implements the naive, real, full, simple (direct) and non-optimized
    version of the algorithm, which doesn't have extra memory requirements
    and has the following complexity:
       - Time: O(N^4)
       - Space: O(2*N^2)
    Where N is the size in pixels of a squared input image for the square
    image case.
    This version of the algorithm is provided for reference (comparison between
    this algorithm results and optimized versions' results). Its use is not
    recommended for applications' use. Use processDistanceFieldWithArray
    instead.
    */
    public static boolean
    processDistanceField(Image inInput, IndexedColorImage outOutput,
        int threshold)
    {
        if ( inInput == null || outOutput == null ) {
            return false;
        }

        int dx = inInput.getXSize();
        int dy = inInput.getYSize();

        if ( dx != outOutput.getXSize() || dy != outOutput.getYSize() ) {
            return false;
        }

        int x, y, xx, yy;
        RGBPixel p;
        double dist2;
        double maxdist2 = ((double)dx)*((double)dx) + ((double)dy)*((double)dy);
        double mindist2;
        double maxdist = Math.sqrt(maxdist2);
        int val;

        for ( x = 0; x < dx; x++ ) {
            for ( y = 0; y < dy; y++ ) {
                // Calculate the nearest distance to output (x, y)
                mindist2 = maxdist2;

                for ( xx = 0; xx < dx; xx++ ) {
                    for ( yy = 0; yy < dy; yy++ ) {
                        p = inInput.getPixelRgb(xx, yy);
                        val = (VSDK.signedByte2unsignedInteger(p.r) +
                               VSDK.signedByte2unsignedInteger(p.g) +
                               VSDK.signedByte2unsignedInteger(p.b)) / 3;
        
                        if ( val >= threshold ) {
                            dist2 = 
                         ((double)xx - (double)x) * ((double)xx - (double)x) +
                         ((double)yy - (double)y) * ((double)yy - (double)y);

                            if ( dist2 < mindist2 ) {
                                mindist2 = dist2;
                            }
                        }
                    }
                }

                // Set output value to current mindistance
                val = (int)((Math.sqrt(mindist2) / maxdist)*255.0);
                outOutput.putPixel(x, y,
                    VSDK.unsigned8BitInteger2signedByte(val));
            }
        }

        return true;
    }

    /**
    A distance field is a scalar map where each pixel value correspond to the
    nearest distance to an "inside" pixel.
    Every pixel in the input image with a value greater or equal to `threshold`
    will be noted as "inside", otherwise will be "outside".
    This implements an optimized version of the algorithm in method
    `processDistanceField`. Current optimization was made using a dynamic
    programming technique which requires an extra preprocessing step and
    and array, which is of N^2 positions in the worst case.
    Algorithm with optimization is bounded by
       - Time: O(N^4)
       - Space: O(3*N^2)
    but falls to
       - Time: O((2+K)*N^2)
       - Space: O(2*N^2+K)
    where K is usually N*0.06 in contour type images.

    Where N is the size in pixels of a squared input image for the square
    image case.
    This version of the algorithm is provided for reference (comparison between
    this algorithm results and optimized versions' results). Its use is not
    recommended for applications' use. Use processDistanceFieldWithArray
    instead.
    */
    public static boolean
    processDistanceFieldWithArray(Image inInput, IndexedColorImage outOutput,
        int threshold)
    {
        if ( inInput == null || outOutput == null ) {
            return false;
        }

        int dx = inInput.getXSize();
        int dy = inInput.getYSize();

        if ( dx != outOutput.getXSize() || dy != outOutput.getYSize() ) {
            return false;
        }

        int x, y;
        int arrSize = 0;
        RGBPixel p;
        int val;

        //- Preprocessing phase 1: determine number of zero distance pixels
        for ( x = 0; x < dx; x++ ) {
            for ( y = 0; y < dy; y++ ) {
                p = inInput.getPixelRgb(x, y);
                val = (VSDK.signedByte2unsignedInteger(p.r) +
                       VSDK.signedByte2unsignedInteger(p.g) +
                       VSDK.signedByte2unsignedInteger(p.b)) / 3;
                if ( val >= threshold ) {
                    arrSize++;
                }
            }
        }

        //- Preprocessing phase 2: fill array with 0-distance pixel coords.
        int xcoords[];
        int ycoords[];
        int i = 0;

        xcoords = new int[arrSize];
        ycoords = new int[arrSize];
        for ( x = 0; x < dx; x++ ) {
            for ( y = 0; y < dy; y++ ) {
                p = inInput.getPixelRgb(x, y);
                val = (VSDK.signedByte2unsignedInteger(p.r) +
                       VSDK.signedByte2unsignedInteger(p.g) +
                       VSDK.signedByte2unsignedInteger(p.b)) / 3;
                if ( val >= threshold ) {
                    xcoords[i] = x;
                    ycoords[i] = y;
                    i++;
                }
            }
        }

        //- Optimized distance field algorithm ----------------------------
        int xx, yy;
        double dist2;
        double maxdist2 = ((double)dx)*((double)dx) + ((double)dy)*((double)dy);
        double mindist2;
        double maxdist = Math.sqrt(maxdist2);

        for ( x = 0; x < dx; x++ ) {
            for ( y = 0; y < dy; y++ ) {
                mindist2 = maxdist2;
                for ( i = 0; i < arrSize; i++ ) {
                    xx = xcoords[i];
                    yy = ycoords[i];
                    dist2 = 
                      ((double)xx - (double)x) * ((double)xx - (double)x) +
                      ((double)yy - (double)y) * ((double)yy - (double)y);
                    if ( dist2 < mindist2 ) {
                        mindist2 = dist2;
                    }
                }
                // Set output value to current mindistance
                val = (int)((Math.sqrt(mindist2) / maxdist)*255.0);
                outOutput.putPixel(x, y,
                    VSDK.unsigned8BitInteger2signedByte(val));
            }
        }
        return true;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
