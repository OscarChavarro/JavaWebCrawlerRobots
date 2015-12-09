//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 19 2008 - Oscar Chavarro: Original base version                   =
//===========================================================================

package vsdk.toolkit.io.geometry;

// Java basic classes
import java.util.ArrayList;
import java.io.OutputStream;
import java.io.BufferedOutputStream;

// VSDK Classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.TriangleMesh;
import vsdk.toolkit.environment.geometry.FunctionalExplicitSurface;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.environment.scene.SimpleScene;
import vsdk.toolkit.io.PersistenceElement;

public class WriterVtk extends PersistenceElement {

    private static void
    exportMesh(OutputStream inOutputStream, TriangleMesh mesh)
        throws Exception
    {
        double v[] = mesh.getVertexPositions();
        double n[] = mesh.getVertexNormals();
        int t[] = mesh.getTriangleIndexes();

        String line;
        writeAsciiLine(inOutputStream, "DATASET POLYDATA");

        //-----------------------------------------------------------------
        line = "POINTS " + (v.length/3) + " float";
        writeAsciiLine(inOutputStream, line);
        int i;
        float val;
        for ( i = 0; i < v.length; i++ ) {
            val = (float)(v[i]*1000.0);
            writeFloatBE(inOutputStream, val);
        }
        writeAsciiLine(inOutputStream, "");

        //-----------------------------------------------------------------
        line = "POLYGONS " + (t.length / 3) + " " + ((t.length/3)*4);
        writeAsciiLine(inOutputStream, line);
        int p = 3;
        for ( i = 0; i < t.length/3; i++ ) {
            writeLongBE(inOutputStream, p);
            writeLongBE(inOutputStream, t[3*i+0]);
            writeLongBE(inOutputStream, t[3*i+1]);
            writeLongBE(inOutputStream, t[3*i+2]);
        }
        writeAsciiLine(inOutputStream, "");

        //-----------------------------------------------------------------
        if ( n != null ) {
            line = "CELL_DATA " + (t.length / 3);
            writeAsciiLine(inOutputStream, line);
            line = "POINT_DATA " + (v.length/3);
            writeAsciiLine(inOutputStream, line);
            line = "NORMALS Normals float";
            writeAsciiLine(inOutputStream, line);
            for ( i = 0; i < v.length/3; i++ ) {
                val = (float)(n[3*i+0]);
                writeFloatBE(inOutputStream, val);
                val = (float)(n[3*i+1]);
                writeFloatBE(inOutputStream, val);
                val = (float)(n[3*i+2]);
                writeFloatBE(inOutputStream, val);
            }
            writeAsciiLine(inOutputStream, "");
        }
        inOutputStream.close();
    }

    public static void
    exportEnvironment(OutputStream inOutputStream, SimpleScene inScene)
        throws Exception
    {
        BufferedOutputStream bos;
        if ( inOutputStream instanceof BufferedOutputStream ) {
            bos = (BufferedOutputStream)inOutputStream;
        }
        else {
            bos = new BufferedOutputStream(inOutputStream);
        }

        //-----------------------------------------------------------------
        writeAsciiLine(bos, "# vtk DataFile Version 3.0");
        writeAsciiLine(bos, "vtk output");
        writeAsciiLine(bos, "BINARY");

        //-----------------------------------------------------------------
        ArrayList<SimpleBody> objs;
        Geometry g;
        TriangleMesh mesh;
        boolean exported = false;

        objs = inScene.getSimpleBodies();
        long baseVertexStart = 0;

        int i;
        for ( i = 0; i < objs.size(); i++ ) {

            //-----------------------------------------------------------------
            g = objs.get(i).getGeometry();
            mesh = null;
            if ( g instanceof FunctionalExplicitSurface ) {
                mesh = ((FunctionalExplicitSurface)g).getInternalTriangleMesh();
            }
            else if ( g instanceof TriangleMesh ) {
                mesh = (TriangleMesh)g;
            }
            else {
                VSDK.reportMessage(null, VSDK.WARNING, "WriterVtk.exportEnvironment", "Current writer implementation only supports writing of triangle meshes. Object skipped.");
            }

            //-----------------------------------------------------------------
            if ( mesh != null ) {
                if ( !exported ) {
                    exportMesh(bos, mesh);
                    exported = true;
                }
                else {
                    VSDK.reportMessage(null, VSDK.WARNING, "WriterVtk.exportEnvironment", "Current writer implementation only supports writing ONE triangle meshes. Only first mesh exported, remaining meshes skipped.");
                }
            }
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
