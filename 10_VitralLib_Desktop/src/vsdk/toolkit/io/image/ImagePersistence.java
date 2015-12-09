//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - September 2 2005 - David Diaz: Original base version                  =
//= - November 24 2005 - Oscar Chavarro: check pending:                     =
//= - May 22 2006 - David Diaz/Oscar Chavarro: documentation added          =
//= - August 6 2006                                                         =
//=   - Oscar Chavarro: managed RGB and RGBA cases independently            =
//=   - Oscar Chavarro: Awt BufferedImage convertion moved to render.awt    =
//= - May 1 2007 - Oscar Chavarro: updated to ImageIO API                   =
//===========================================================================

package vsdk.toolkit.io.image;

// Basic JDK classes
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;
import java.util.ArrayList;

// VitralSDK classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.media.Image;
import vsdk.toolkit.media.RGBImage;
import vsdk.toolkit.media.RGBAImage;
import vsdk.toolkit.media.RGBPixel;
import vsdk.toolkit.media.IndexedColorImage;
import vsdk.toolkit.io.PersistenceElement;

/**
This class is a front end front which images of various formats can be
exported and/or imported to/from files.

This class follows a Singleton design pattern.

\todo  Does this implements a "Builder" design pattern??? A Factory design 
      pattern... possibly some of that combined with a Facade design 
      pattern?
 */
public class ImagePersistence extends PersistenceElement
{
    public static boolean nativeWarningGiven = false;
    private static ArrayList<ImagePersistenceHelper> helpers = null;

    static {
        initHelpers();
    }

    private static void loadPluginHelper(String className)
    {
        ClassLoader cl = ImagePersistence.class.getClassLoader();
        try {
            Class<? extends Object> handle;
            handle = cl.loadClass(className);
            Object o;
            o = handle.newInstance();
            if ( o instanceof ImagePersistenceHelper ) {
                helpers.add((ImagePersistenceHelper)o);
            }
        }
        catch ( ClassNotFoundException e ) {
            // Class is not available... (not a problem)
        }
        catch ( InstantiationException e ) {
        }
        catch ( IllegalAccessException e ) {
        }
    }

    public static void initHelpers()
    {
        if ( helpers == null  ) {
            helpers = new ArrayList<ImagePersistenceHelper>();
        }
        
        // Call order reflects prefered order
        loadPluginHelper("vsdk.toolkit.io.image.ImagePersistenceAwt");
        loadPluginHelper("vsdk.toolkit.io.image.ImagePersistenceTarga");
        loadPluginHelper("vsdk.toolkit.io.image.ImagePersistenceJogl");
        loadPluginHelper("vsdk.toolkit.io.image.ImagePersistenceAndroid");
    }

    public static void registerHelper(ImagePersistenceHelper h)
    {
        initHelpers();
        helpers.add(h);
    }

    private static RGBAImage importDDSRGBA(File inImageFd)
    {
        try {
            int i;
            for ( i = 0; i < helpers.size(); i++ ) {
                if ( helpers.get(i).rgbaFormatSupported("dds") ) {
                    return helpers.get(i).importRGBA(inImageFd);
                }
            }
        }
        catch ( Exception e ) {

        }
        return createNotAvailableImageRGBA();
    }

    private static RGBAImage createNotAvailableImageRGBA()
    {
        RGBAImage data;
        data = new RGBAImage();
        data.init(256, 256);
        data.createTestPattern();
        VSDK.reportMessage(null, VSDK.WARNING, 
            "ImagePersistence",
            "Helper class not available, returning test RGBA image"
        );
        return data;
    }

    private static RGBImage createNotAvailableImageRGB()
    {
        RGBImage data;
        data = new RGBImage();
        data.init(256, 256);
        data.createTestPattern();
        VSDK.reportMessage(null, VSDK.WARNING, 
            "ImagePersistence",
            "Helper class not available, returning test RGB image"
        );
        return data;
    }

    private static IndexedColorImage createNotAvailableImageIndexedColor()
    {
        IndexedColorImage data;
        data = new IndexedColorImage();
        data.init(256, 256);
        data.createTestPattern();
        VSDK.reportMessage(null, VSDK.WARNING, 
            "ImagePersistence",
            "Helper class not available, returning test IndexedColor image"
        );
        return data;
    }

    private static RGBImage importDDSRGB(File inImageFd)
    {
        try {
            int i;
            for ( i = 0; i < helpers.size(); i++ ) {
                if ( helpers.get(i).rgbFormatSupported("dds") ) {
                    return helpers.get(i).importRGB(inImageFd);
                }
            }
        }
        catch ( Exception e ) {

        }
        return createNotAvailableImageRGB();
    }

    /**
    Given the filename of an input data file which contains an image, this
    method tries to recognize the file format and load the contents of it
    to the image.

    \todo  Do not assume the file format only from the filename extension,
    but trying to detect file headers.

    @param inImageFd - The file containing the image
    @return An RGBAImage entity that contains the image loaded in memory.

    Will change:
      - Choose a better name for this method
      - Do not receive a File, but a Stream of bytes
     * @throws vsdk.toolkit.io.image.ImageNotRecognizedException
    */
    public static RGBAImage importRGBA(File inImageFd) 
        throws ImageNotRecognizedException, Exception
    {
        String type = extractExtensionFromFile(inImageFd);
        RGBAImage retImage;
        retImage = new RGBAImage();

        //- Try optimized reading, if native library is available ---------
        if ( NativeImageReaderWrapper.available && type.equals("png") ) {
            _NativeImageReaderWrapperHeaderInfo header;
            header = new _NativeImageReaderWrapperHeaderInfo();
            NativeImageReaderWrapper.readPngHeader(header,
                inImageFd.getAbsolutePath());
            retImage.initNoFill((int)header.xSize, (int)header.ySize);
            NativeImageReaderWrapper.readPngDataRGBA(header, retImage.getRawImageDirectBuffer());
            return retImage;
        }

        if ( !nativeWarningGiven && !NativeImageReaderWrapper.available &&
             type.equals("png") ) {
            nativeWarningGiven = true;
            VSDK.reportMessage(null, VSDK.WARNING, "ImagePersistence.importRGBA", "NativeImageReader library not found, falling to AWT-based PNG reading, which can be slow.");
        }

        //-----------------------------------------------------------------
        int i;
        for ( i = 0; i < helpers.size(); i++ ) {
            if ( helpers.get(i).rgbaFormatSupported(type) ) {
                return helpers.get(i).importRGBA(inImageFd);
            }
        }

        if( type.equals("dds") ) {
            //delete retImage;
            return importDDSRGBA(inImageFd);
        }
        
        throw new ImageNotRecognizedException("Image not recognized", inImageFd);
    }

    /**
    Given a text string, this method determines if is a Unix style single
    line comment, that is, if line begins with a '#' character.
    */
    private static boolean
    isTextComment(String line)
    {
        char arr[] = line.toCharArray();
        int i;

        for ( i = 0; 
              i < arr.length && (arr[i] != ' ' && arr[i] != '\t'); 
              i++ ) {
            // Skip comment
        }

        return i < arr.length && arr[i] == '#';
    }

    public static RGBImage importRGB(InputStream is) throws ImageNotRecognizedException, Exception
    {
        try {
            int i;
            for ( i = 0; i < helpers.size(); i++ ) {
                if ( helpers.get(i).rgbFormatFromInputStreamSupported("*") ) {
                    return helpers.get(i).importRGB(is);
                }
            }
        }
        catch ( Exception e ) {

        }
        return createNotAvailableImageRGB();
    }

    public static RGBAImage importRGBA(InputStream is) throws ImageNotRecognizedException, Exception
    {
        try {
            int i;
            for ( i = 0; i < helpers.size(); i++ ) {
                if ( helpers.get(i).rgbaFormatFromInputStreamSupported("*") ) {
                    return helpers.get(i).importRGBA(is);
                }
            }
        }
        catch ( Exception e ) {
            VSDK.reportMessageWithException(null, VSDK.FATAL_ERROR, "ImagePersistence.importRGBA", "INPUT ERROR: ", e);
        }
        return createNotAvailableImageRGBA();
    }

    /**
    Given the filename of an input data file which contains an image, this
    method tries to recognize the file format and load the contents of it
    to the image.

    \todo  Do not assume the file format only from the filename extension,
    but trying to detect file headers.

    @param inImageFd - The file respesenting the image
    @return An RGBImage entity that contains the image loaded in memory.

    Will change:
      - Choose a better name for this method
      - Do not recieve a File, but a Stream of bytes
    @throws vsdk.toolkit.io.image.ImageNotRecognizedException
    */
    public static RGBImage importRGB(File inImageFd) 
        throws ImageNotRecognizedException, Exception
    {
        String type = extractExtensionFromFile(inImageFd);
        RGBImage retImage = new RGBImage();

        //- Try optimized reading, if native library is available ---------
        if ( NativeImageReaderWrapper.available && type.equals("png") ) {
            _NativeImageReaderWrapperHeaderInfo header;
            header = new _NativeImageReaderWrapperHeaderInfo();
            NativeImageReaderWrapper.readPngHeader(header,
                inImageFd.getAbsolutePath());
            retImage.initNoFill((int)header.xSize, (int)header.ySize);
            NativeImageReaderWrapper.readPngDataRGB(header, retImage.getRawImageDirectBuffer());
            return retImage;
        }

        if ( !nativeWarningGiven && !NativeImageReaderWrapper.available &&
             type.equals("png") ) {
            nativeWarningGiven = true;
            VSDK.reportMessage(null, VSDK.WARNING, "ImagePersistence.importRGB", "NativeImageReader library not found, falling to AWT-based PNG reading, which can be slow.");
        }

        //-----------------------------------------------------------------
        int i;
        for ( i = 0; i < helpers.size(); i++ ) {
            if ( helpers.get(i).rgbFormatSupported(type) ) {
                return helpers.get(i).importRGB(inImageFd);
            }
        }

        if( type.equals("dds") ) {
            //delete retImage;
            return importDDSRGB(inImageFd);
        }
        else if( type.equals("ppm") )  {
            try {
                BufferedInputStream bis;
                FileInputStream fis;

                fis = new FileInputStream(inImageFd);
                bis = new BufferedInputStream(fis);

                boolean exit = false;
                String line;
                int stage = 1;
                int xSize = 0, ySize = 0;

                do {
                    line = readAsciiLine(bis);
                    if ( line.equals("255") ) {
                        exit = true;
                    }
                    if ( isTextComment(line) ) {
                        continue;
                    }
                    switch ( stage ) {
                      case 1: // PPM signature - data type
                        if ( !line.startsWith("P6") ) {
                            throw new ImageNotRecognizedException("Error reading internal PPM file subformat:\n" + line, inImageFd);
                        }
                        stage++;
                        break;
                      case 2:
                        if ( line.startsWith("#") ) {
                            // Skip comment line
                          }
                          else {
                            StringTokenizer parser;
                            parser = new StringTokenizer(line);
                            xSize = Integer.parseInt(parser.nextToken());
                            ySize = Integer.parseInt(parser.nextToken());
                            stage++;
                        }
                        break;
                    }
                } while ( !exit );

                retImage = new RGBImage();
                retImage.initNoFill(xSize, ySize);
                //byte barr[] = retImage.getRawImage();
                //readBytes(bis, barr);

                ByteBuffer bb = retImage.getRawImageDirectBuffer();
                byte barr[] = new byte[xSize*3];
                for ( i = 0; i < ySize; i++ ) {
                    readBytes(bis, barr);
                    bb.put(barr);
                }

                //-------------------------------------------------------------
                // Invert image
                int x, y;
                RGBPixel pa = new RGBPixel();
                RGBPixel pb = new RGBPixel();

                for ( y = 0; y < ySize/2; y++ ) {
                    for ( x = 0; x < xSize; x++ ) {
                        retImage.getPixelRgb(x, y, pa);
                        retImage.getPixelRgb(x, ySize-y-1, pb);
                        retImage.putPixelRgb(x, y, pb);
                        retImage.putPixelRgb(x, ySize-y-1, pa);
                    }
                }
                //-------------------------------------------------------------

                bis.close();
                fis.close();
                return retImage;
              }
              catch ( Exception e ) {
                  VSDK.reportMessage(null, VSDK.ERROR, "importRGB (B)",
                                     "Cannot import image file \"" + inImageFd.getAbsolutePath() + "\"" + e);
                 throw new ImageNotRecognizedException("Error reading internal file:\n" + e, inImageFd);
            }
        }
        return createNotAvailableImageRGB();
    }

    /**
    Given the filename of an input data file which contains an image, this
    method tries to recognize the file format and load the contents of it
    to the image.

    \todo  Do not assume the file format only from the filename extension,
    but trying to detect file headers.

    @param inImageFd - The file respesenting the image
    @return An IndexedColorImage entity that contains the image loaded in memory.

    Will change:
      - Choose a better name for this method
      - Do not recieve a File, but a Stream of bytes
    @throws vsdk.toolkit.io.image.ImageNotRecognizedException
    */
    public static IndexedColorImage importIndexedColor(File inImageFd) 
        throws ImageNotRecognizedException, Exception
    {
        String type = extractExtensionFromFile(inImageFd);
        IndexedColorImage retImage;
        Image img;

        int i;
        for ( i = 0; i < helpers.size(); i++ ) {
            if ( helpers.get(i).indexedColorFormatSupported(type) ) {
                return helpers.get(i).importIndexedColor(inImageFd);
            }
        }

        if( type.equals("bw") ) {
            img = ImagePersistenceSGI.readImageSGI(inImageFd.getAbsolutePath());
            if ( img instanceof IndexedColorImage ) {
                retImage = (IndexedColorImage)img;
            }
            else {
                throw new ImageNotRecognizedException("Convertion needed", 
                inImageFd);
            }
            return retImage;
        }
        throw new ImageNotRecognizedException("Image not recognized", inImageFd);
    }
   
    private static void transferPixels(int[] ori, byte[] dest, int w, int h, int pixelDepth)
    {
        int bPos=0;
        for(int i=0; i<w*h; i++)
        {
            dest[bPos]=(byte)(ori[i]>>16);
            bPos++;
            dest[bPos]=(byte)(ori[i]>>8);
            bPos++;
            dest[bPos]=(byte)(ori[i]);
            bPos++;
            if(pixelDepth==32)
            {
                dest[bPos]=(byte)(ori[i]>>24);
                bPos++;
            }
        }
    }
    
    /**
    This method writes the contents of the specified image to a file in 
    binary JPEG image format. Returns true if everything
    works fine, false if something fails, like a permission access denied
    or if storage device runs out of space.
    @param fd
    @param img
    @return 
    */
    public static boolean exportJPG(File fd, Image img)
    {
        try {
            FileOutputStream fos;
            fos = new FileOutputStream(fd);

            exportJPG(fos, img);

            fos.close();
        }
        catch ( Exception e ) {
            return false;
        }
        return true;
    }

    public static boolean exportGIF(File fd, Image img)
    {
        int i;

        for ( i = 0; i < helpers.size(); i++ ) {
            if ( helpers.get(i).gifExportSupported() ) {
                return helpers.get(i).exportGIF(fd, img);
            }
        }
            
        VSDK.reportMessage(null, VSDK.WARNING, 
            "ImagePersistence",
            "Helper class not available, not saving GIF image"
        );
        return false;
    }

    public static void exportJPG(OutputStream os, Image img) throws Exception
    {
        int i;

        for ( i = 0; i < helpers.size(); i++ ) {
            if ( helpers.get(i).jpgExportSupported() ) {
                helpers.get(i).exportJPG(os, img);
                return;
            }
        }
        VSDK.reportMessage(null, VSDK.WARNING, 
            "ImagePersistence",
            "Helper class not available, not saving JPG image");
    }

    public static void exportPNG(OutputStream os, Image img)
        throws Exception
    {
        int i;
        for ( i = 0; i < helpers.size(); i++ ) {
            if ( helpers.get(i).pngExportSupported() ) {
                helpers.get(i).exportPNG_24bitRgb(os, img);
                return;
            }
        }
        VSDK.reportMessage(null, VSDK.WARNING, 
            "ImagePersistence",
            "Helper class not available, not saving PNG image");
    }
 
    public static void exportPNG(File fd, Image img)
    {
        try {
            int i;
            for ( i = 0; i < helpers.size(); i++ ) {
                if ( helpers.get(i).pngExportSupported() ) {
                    helpers.get(i).exportPNG(fd, img);
                    return;
                }
            }
        }
        catch ( Exception e ) {
            VSDK.reportMessage(null, VSDK.WARNING, 
                "ImagePersistence",
                "Error saving PNG image");
        }
    }

    public static void exportPNG_24bitRgb(OutputStream os, Image img) throws Exception
    {
        int i;
        for ( i = 0; i < helpers.size(); i++ ) {
            if ( helpers.get(i).pngExportSupported() ) {
                helpers.get(i).exportPNG_24bitRgb(os, img);
                return;
            }
        }
        VSDK.reportMessage(null, VSDK.WARNING, 
            "ImagePersistence",
            "Helper class not available, not saving PNG image");
    }

    /**
    This method writes the contents of the specified image to a file in 
    binary RGB PPM format (i.e. P6 PPM sub-format). Returns true if everything
    works fine, false if something fails, like a permission access denied
    or if storage device runs out of space.
    @param fd
    @param img
    @return 
    */
    public static boolean exportPPM(File fd, Image img)
    {
        try {
            BufferedOutputStream writer;
            FileOutputStream fos;
            fos = new FileOutputStream(fd);
            writer = new BufferedOutputStream(fos);

            String line1 = "P6\n";
            String line2 = "# Image generated by VitralSDK (http://vitral.sf.net)\n";
            String line3 = img.getXSize() + " " + img.getYSize() + "\n";
            String line4 = "255\n";
            byte arr[];

            arr = line1.getBytes();
            writer.write(arr, 0, arr.length);
            arr = line2.getBytes();
            writer.write(arr, 0, arr.length);
            arr = line3.getBytes();
            writer.write(arr, 0, arr.length);
            arr = line4.getBytes();
            writer.write(arr, 0, arr.length);

            RGBPixel p;
            int x;
            int y;
            for ( y = 0; y < img.getYSize(); y++ ) {
                for ( x = 0; x < img.getXSize(); x++ ) {
                    p = img.getPixelRgb(x, y);
                    writer.write(p.r);
                    writer.write(p.g);
                    writer.write(p.b);
                }
            }

            writer.flush();
            writer.close();
            fos.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
    This method writes the contents of the specified image to a file in 
    binary RGB uncompressed BMP format. Returns true if everything
    works fine, false if something fails, like a permission access denied
    or if storage device runs out of space.
    @param fd
    @param img
    @return 
    */
    public static boolean exportBMP(File fd, Image img)
    {
        try {
            BufferedOutputStream writer;
            FileOutputStream fos;
            fos = new FileOutputStream(fd);
            writer = new BufferedOutputStream(fos);

            int y, x;

            //- Write BMP header ----------------------------------------------
            byte magic[] = new byte[2];
            magic[0] = 'B';
            magic[1] = 'M';

            writeBytes(writer, magic);
            writeLongLE(writer, img.getXSize()*img.getYSize()*3 + 54);
            writeLongLE(writer, 0);
            writeLongLE(writer, 54);

            //- Write Windows V3 DIB header -----------------------------------
            writeLongLE(writer, 40);
            writeLongLE(writer, img.getXSize());
            writeLongLE(writer, img.getYSize());
            writeSignedShortLE(writer, 1);
            writeSignedShortLE(writer, 24);
            writeLongLE(writer, 0);
            writeLongLE(writer, 16);
            writeLongLE(writer, 2835);
            writeLongLE(writer, 2835);
            writeLongLE(writer, 0);
            writeLongLE(writer, 0);

            //- Manejo de imagenes nativas de 24 bits por pixel ---------------
            RGBPixel pixel;

            for ( y = img.getYSize() - 1; y >= 0; y-- ) {
                for ( x = 0; x < img.getXSize() ; x++ ) {
                    pixel = img.getPixelRgb(x, y);
                    writer.write(pixel.b);
                    writer.write(pixel.g);
                    writer.write(pixel.r);
                }
            }

            writer.flush();
            writer.close();
            fos.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
    This method writes the contents of the specified image to a file in 
    binary GrayScale PPM format (i.e. P5 PPM sub-format). Returns true if
    everything works fine, false if something fails, like a permission access
    denied or if storage device runs out of space.
    @param fd
    @param img
    @return 
    */
    public static boolean exportPNM(File fd, Image img)
    {
        try {
            BufferedOutputStream writer;
            FileOutputStream fos;
            fos = new FileOutputStream(fd);
            writer = new BufferedOutputStream(fos);

            String line1 = "P5\n";
            String line2 = "# Image generated by VitralSDK (http://vitral.sf.net)\n";
            String line3 = img.getXSize() + " " + img.getYSize() + "\n";
            String line4 = "255\n";
            byte arr[];

            arr = line1.getBytes();
            writer.write(arr, 0, arr.length);
            arr = line2.getBytes();
            writer.write(arr, 0, arr.length);
            arr = line3.getBytes();
            writer.write(arr, 0, arr.length);
            arr = line4.getBytes();
            writer.write(arr, 0, arr.length);

            int x;
            int y;
            for ( y = 0; y < img.getYSize(); y++ ) {
                for ( x = 0; x < img.getXSize(); x++ ) {
                    writer.write(img.getPixel8bitGrayScale(x, y));
                }
            }

            writer.flush();
            writer.close();
            fos.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }
    
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
