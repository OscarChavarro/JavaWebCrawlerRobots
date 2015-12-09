
//===========================================================================

package vsdk.toolkit.io.image;

// Basic JDK classes
import java.io.File;
import java.nio.ByteBuffer;

// JOGL classes
import com.jogamp.opengl.util.texture.spi.DDSImage;
import com.jogamp.opengl.util.texture.spi.DDSImage.ImageInfo;

// VitralSDK classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.media.RGBImage;
import vsdk.toolkit.media.RGBAImage;
import vsdk.toolkit.io.PersistenceElement;

/**
This class provides to the ImagePersistence class some support for image
data persistence, using methods from the JOGL API Library.

Do not use this class directly, its methods are here to be called from
ImagePersistence class only.
*/
public class ImagePersistenceJogl extends ImagePersistenceHelper
{
    public boolean rgbFormatSupported(String fileExtension)
    {
        if( fileExtension.equals("dds") ) {
            return true;
        }
        return false;
    }

    public boolean rgbaFormatSupported(String fileExtension)
    {
        if( fileExtension.equals("dds") ) {
            return true;
        }
        return false;
    }

    public RGBImage importRGB(File inImageFd)
    {
        RGBImage retImage = new RGBImage();
        try {
            DDSImage dximage = DDSImage.read(inImageFd);
            //System.out.println("Reading DirectX texture: ");
            //System.out.println("  - Number of mipmap levels: " + dximage.getNumMipMaps());
            int i;
            int maxindex = 0;
            ImageInfo maxinfo = dximage.getMipMap(0);
            //dximage.debugPrint();
            for ( i = 0; i < dximage.getNumMipMaps(); i++ ) {
                ImageInfo info;
                info = dximage.getMipMap(i);
                //System.out.println("   . " + info.getWidth() + " x " + info.getHeight());

                if ( info.getWidth() > dximage.getMipMap(i).getWidth() ) {
                    maxindex = i;
                    maxinfo = info;
                }

            }

            if ( maxinfo.isCompressed() ) {
                VSDK.reportMessage(null, VSDK.WARNING, "importRGB",
                "Compressed image subformats unsupported - file \"" + inImageFd.getAbsolutePath() + "\"");
                retImage.init(64, 64);
                retImage.createTestPattern();
                return retImage;
            }

            ByteBuffer bb = maxinfo.getData();
            retImage.init(maxinfo.getWidth(), maxinfo.getHeight());
            int format = dximage.getPixelFormat();
            if ( format == DDSImage.D3DFMT_R8G8B8 ) {
                VSDK.reportMessage(null, VSDK.WARNING, "importRGB",
                "Subformat flat not supported for file \"" + inImageFd.getAbsolutePath() + "\"");
                retImage.createTestPattern();
            }
            else if ( format == DDSImage.D3DFMT_A8R8G8B8 ) {
                int x, y;
                byte r, g, b, a;
                for ( y = 0; y < retImage.getYSize(); y++ ) {
                    for ( x = 0; x < retImage.getXSize(); x++ ) {
                        r = bb.get();
                        g = bb.get();
                        b = bb.get();
                        a = bb.get();
                        retImage.putPixel(x, y, r, g, b);
                    }
                }
            }
            else if ( format == DDSImage.D3DFMT_X8R8G8B8 ) {
                int x, y;
                byte r, g, b, a;
                for ( y = 0; y < retImage.getYSize(); y++ ) {
                    for ( x = 0; x < retImage.getXSize(); x++ ) {
                        r = bb.get();
                        g = bb.get();
                        b = bb.get();
                        a = bb.get();
                        retImage.putPixel(x, y, r, g, b);
                    }
                }
            }
            else {
                VSDK.reportMessage(null, VSDK.WARNING, "importRGB",
                "Subformat (?) not supported for file \"" + inImageFd.getAbsolutePath() + "\"");
                retImage.createTestPattern();
            }
            return retImage;
        }
        catch ( Exception e ) {
              VSDK.reportMessage(null, VSDK.ERROR, "importRGB",
                                 "Cannot import image file \"" + inImageFd.getAbsolutePath() + "\"");
            return null;
        }
    }

    /**
    Reads an RGBAImage from a .dds (DirectDraw Surface) file format
    source.
    /todo recieve an input stream instead of a file.
    */
    public RGBAImage importRGBA(File inImageFd)
    {
         RGBAImage retImage = new RGBAImage();

         try {
            DDSImage dximage = DDSImage.read(inImageFd);
            //System.out.println("Reading DirectX texture: ");
            //System.out.println("  - Number of mipmap levels: " + dximage.getNumMipMaps());
            int i;
            int maxindex = 0;
            ImageInfo maxinfo = dximage.getMipMap(0);
            //dximage.debugPrint();
            for ( i = 0; i < dximage.getNumMipMaps(); i++ ) {
                ImageInfo info;
                info = dximage.getMipMap(i);
                //System.out.println("   . " + info.getWidth() + " x " + info.getHeight());

                if ( info.getWidth() > dximage.getMipMap(i).getWidth() ) {
                    maxindex = i;
                    maxinfo = info;
                }

            }

            int format = dximage.getPixelFormat();

            ByteBuffer bb = maxinfo.getData();
            retImage.init(maxinfo.getWidth(), maxinfo.getHeight());

            if ( maxinfo.isCompressed() && format == DDSImage.D3DFMT_DXT3 ) {
                // 4-bit nonpremultiplied alpha
                //System.out.println("Compressed format: [D3DFMT_DXT3]");

                int x, xx, y, yy, dx;
                byte r, g, b, a;
                dx = retImage.getXSize();

                int n; // Number of 128bits (16bytes) compression chunks
                int j; // Chunk index
                int k, kk; // Byte index inside the chunk
                int l; // Pixel counter;
                n = retImage.getXSize() * retImage.getYSize() / 16;
                byte[] chunk = new byte[16];

                x = 0;
                y = 0;
                for ( j = 0; j < n; j++ ) {
                    // Read compression chunk
                    for ( k = 0; k < 16; k++ ) {
                        chunk[k] = bb.get();
                    }

                    xx = 0;
                    yy = 0;
                    for ( l = 0; l < 16; l++ ) {
                        // First 64 bits (8bytes) are alpha channels for
                        // 16 input pixels (4bits per alpha pixel).
                        // Second 64 bits (8bytes) are RGB colors for
                        // 16 input pixels (4 bits per color pixel).
                        kk = ((l) / 2);
                        int aval = VSDK.signedByte2unsignedInteger(chunk[kk]);
                        int cval = VSDK.signedByte2unsignedInteger(chunk[8+kk]);
                        if ( (l % 2) != 0 ) {
                            aval = (aval >> 4) * 16;
                            cval = (cval >> 4) * 16;
                        }
                        else {
                            aval = (aval & 0x0F) * 16;
                            cval = (aval & 0x0F) * 16;
                        }

                        // Put pixel (to ckeck!)
                        a = (byte) aval;
                        r = VSDK.unsigned8BitInteger2signedByte(((cval&0x01))*255);
                        g = VSDK.unsigned8BitInteger2signedByte(((cval&0x02)>>1)*255);
                        b = VSDK.unsigned8BitInteger2signedByte(((cval&0x04)>>2)*255);

                        retImage.putPixel(x + xx, y+yy, r, g, b, a);

                        // Go to next pixel
                        xx++;
                        if ( xx >= 4 ) {
                            xx = 0;
                            yy++;
                        }
                    }
                    x += 4;
                    if ( x >= dx ) {
                        x = 0;
                        y += 4;
                    }
                }
            }
            else if ( maxinfo.isCompressed() ) {
                VSDK.reportMessage(null, VSDK.WARNING, "importRGBA",
                "Compressed image subformats unsupported - file \"" + inImageFd.getAbsolutePath() + "\"");
                retImage.createTestPattern();
                if ( format == DDSImage.D3DFMT_DXT1 ) {
                    // 1-bit alpha
                    System.out.println("Compressed format: [D3DFMT_DXT1]");
                }
                else if ( format == DDSImage.D3DFMT_DXT2 ) {
                    // 4-bit premultiplied alpha
                    System.out.println("Compressed format: [D3DFMT_DXT2]");
                }
                else if ( format == DDSImage.D3DFMT_DXT4 ) {
                    // interpolated premultiplied alpha
                    System.out.println("Compressed format: [D3DFMT_DXT4]");
                }
                else if ( format == DDSImage.D3DFMT_DXT5 ) {
                    // interpolated nonpremultiplied alpha
                    System.out.println("Compressed format: [D3DFMT_DXT5]");
                }
                else {
                    System.out.println("Compressed format: [*UNKNOWN*]");
                }
                return retImage;
            }
            else if ( format == DDSImage.D3DFMT_R8G8B8 ) {
                //System.out.println("[D3DFMT_R8G8B8]");

                VSDK.reportMessage(null, VSDK.WARNING, "importRGBA",
                "Subformat flat not supported for file \"" + inImageFd.getAbsolutePath() + "\"");
                retImage.createTestPattern();
            }
            else if ( format == DDSImage.D3DFMT_A8R8G8B8 ) {
                //System.out.println("[D3DFMT_A8R8G8B8]");

                int x, y;
                byte r, g, b, a;
                for ( y = 0; y < retImage.getYSize(); y++ ) {
                    for ( x = 0; x < retImage.getXSize(); x++ ) {
                        b = bb.get();
                        g = bb.get();
                        r = bb.get();
                        a = bb.get();
                        retImage.putPixel(x, y, r, g, b, a);
                    }
                }
            }
            else if ( format == DDSImage.D3DFMT_X8R8G8B8 ) {
                //System.out.println("[D3DFMT_X8R8G8B8]");

                int x, y;
                byte r, g, b, a;
                for ( y = 0; y < retImage.getYSize(); y++ ) {
                    for ( x = 0; x < retImage.getXSize(); x++ ) {
                        b = bb.get();
                        g = bb.get();
                        r = bb.get();
                        a = bb.get();
                        retImage.putPixel(x, y, r, g, b, a);
                    }
                }
            }
            else {
                //System.out.println("[**INVALID**]");

                VSDK.reportMessage(null, VSDK.WARNING, "importRGBA",
                "Subformat (?) not supported for file \"" + inImageFd.getAbsolutePath() + "\"");
                retImage.createTestPattern();
            }
            return retImage;
        }
        catch ( Exception e ) {
              VSDK.reportMessage(null, VSDK.ERROR, "importRGBA",
                 "Cannot import image file \"" + inImageFd.getAbsolutePath() + 
                 "\"" + e);
              return null;
        }
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
