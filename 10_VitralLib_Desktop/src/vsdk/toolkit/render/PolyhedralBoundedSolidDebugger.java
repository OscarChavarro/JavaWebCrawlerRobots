//===========================================================================

package vsdk.toolkit.render;

import vsdk.toolkit.environment.geometry.PolyhedralBoundedSolid;

/**
This class follows the Singleton design pattern. Its sole function is to
create an offline renderer for debugging PolyhedralBoundedSolid objects.

As current implementation of the offline renderer is implemented on
JOGL, it is possible this causes trouble on some platforms. In the event
of a compiling error due to unavailable JOGL libraries, this class could
be reprogrammed to return null.
*/
public abstract class PolyhedralBoundedSolidDebugger extends RenderingElement
{
    public static PolyhedralBoundedSolidDebugger createOfflineRenderer()
    {
        return loadPluginHelper(
            "vsdk.toolkit.render.jogl.JoglPolyhedralBoundedSolidDebugger");
    }

    private static PolyhedralBoundedSolidDebugger 
    loadPluginHelper(String className)
    {
        ClassLoader cl = PolyhedralBoundedSolidDebugger.class.getClassLoader();
        try {
            Class<? extends Object> handle;
            handle = cl.loadClass(className);
            Object o;
            o = handle.newInstance();
            if ( o instanceof PolyhedralBoundedSolidDebugger ) {
                return (PolyhedralBoundedSolidDebugger)o;
            }
        }
        catch ( Exception e ) {
            // Class is not available... (not a problem)
        }
	return null;
    }

    public abstract void execute(PolyhedralBoundedSolid solid, String filename);
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
