//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - December 8 2006 - Oscar Chavarro: Original base version               =
//===========================================================================

package vsdk.toolkit.io.geometry;

// Java basic classes
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

// VSDK Classes
import vsdk.toolkit.environment.scene.SimpleScene;
import vsdk.toolkit.io.PersistenceElement;

public class EnvironmentPersistence extends PersistenceElement {

    public static void
    importEnvironment(File inSceneFileFd, SimpleScene inoutScene)
        throws Exception
    {
        String type = extractExtensionFromFile(inSceneFileFd).toLowerCase();

        if ( type.equals("obj") ) {
            ReaderObj.importEnvironment(inSceneFileFd, inoutScene);
        }
        else if ( type.equals("3ds") ) {
            InputStream is = new FileInputStream(inSceneFileFd);
            String pathname = inSceneFileFd.getParentFile().getAbsolutePath();
            String sourcename = inSceneFileFd.getName();
            Reader3ds.importEnvironment(is, pathname, sourcename, inoutScene);
        }
        else if ( type.equals("gts") ) {
            ReaderGts.importEnvironment(inSceneFileFd, inoutScene);
        }
        else if ( type.equals("ply") ) {
            ReaderPly.importEnvironment(inSceneFileFd, inoutScene);
        }
        else if ( type.equals("ase") ) {
            ReaderAse.importEnvironment(inSceneFileFd, inoutScene);
        }
        else if ( type.equals("wrl") || type.equals("gz") ) {
            ReaderVrml.importEnvironment(inSceneFileFd, inoutScene);
        }
        else if ( type.equals("vtk") ) {
            ReaderVtk.importEnvironment(inSceneFileFd, inoutScene);
        }
        else if ( type.equals("bin") ) {
            ReaderBinNeedForSpeed.importEnvironment(inSceneFileFd, inoutScene);
        }
    }

    public static void
    exportEnvironmentObj(OutputStream inOutputStream, SimpleScene inScene)
        throws Exception
    {
        WriterObj.exportEnvironment(inOutputStream, inScene);
    }

    public static void
    exportEnvironmentGts(OutputStream inOutputStream, SimpleScene inScene)
        throws Exception
    {
        WriterGts.exportEnvironment(inOutputStream, inScene);
    }

    public static void
    exportEnvironmentVtk(OutputStream inOutputStream, SimpleScene inScene)
        throws Exception
    {
        WriterVtk.exportEnvironment(inOutputStream, inScene);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
