package vsdk.toolkit.io.image;

import java.nio.ByteBuffer;

import vsdk.toolkit.io.PersistenceElement;

public class NativeImageReaderWrapper extends PersistenceElement
{
    public static boolean available = false;
    static {
        if ( verifyLibrary("NativeImageReader") ) {
            System.loadLibrary("NativeImageReader");
            available = true;
        }
    }

    public static native void readPngHeader(_NativeImageReaderWrapperHeaderInfo header, String filename) throws Exception;
    public static native void readPngDataRGB(_NativeImageReaderWrapperHeaderInfo header, ByteBuffer arr) throws Exception;
    public static native void readPngDataRGBA(_NativeImageReaderWrapperHeaderInfo header, ByteBuffer arr) throws Exception;
}
