package vsdk.toolkit.io.image;

import vsdk.toolkit.io.PersistenceElement;

public class _NativeImageReaderWrapperHeaderInfo extends PersistenceElement
{
    public long xSize;
    public long ySize;
    public long channels;

    // Warning: only tested on 64bit environments!
    public long nativePointer;
}
