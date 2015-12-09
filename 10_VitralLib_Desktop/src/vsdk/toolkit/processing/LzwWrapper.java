package vsdk.toolkit.processing;

import java.io.InputStream;
import java.io.OutputStream;

public class LzwWrapper extends ProcessingElement
{
    static {
        System.loadLibrary("LZW");
    }

    public static native void decompress(InputStream in, OutputStream out) throws Exception;
    public static native void decompressWithSize(InputStream in, OutputStream out, long size) throws Exception;
    //public static native void compress(InputStream in, OutputStream out) throws Exception;
}
