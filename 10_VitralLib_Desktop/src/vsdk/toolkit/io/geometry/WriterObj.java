//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - October 15 2007 - Oscar Chavarro: Original base version               =
//===========================================================================

package vsdk.toolkit.io.geometry;

// Java basic classes
import java.util.ArrayList;
import java.io.OutputStream;

// VSDK Classes
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.TriangleMesh;
import vsdk.toolkit.environment.geometry.FunctionalExplicitSurface;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.environment.scene.SimpleScene;
import vsdk.toolkit.io.PersistenceElement;

public class WriterObj extends PersistenceElement {

    private static long
    exportMesh(OutputStream inOutputStream, TriangleMesh mesh, long offset)
        throws Exception
    {
        int nv = mesh.getNumVertices();
        int nt = mesh.getNumTriangles();
        double vp[] = mesh.getVertexPositions();
        double vn[] = mesh.getVertexNormals();
        double vuv[] = mesh.getVertexUvs();
        int i;
        Vector3D p, n, vpi, vni;
        Matrix4x4 R = new Matrix4x4();

        R.axisRotation(Math.toRadians(-90), new Vector3D(1, 0, 0));

        //-----------------------------------------------------------------
        vpi = new Vector3D();
        writeAsciiLine(inOutputStream, "# " + nv + " vertex positions");
        for ( i = 0; i < nv; i++ ) {
            vpi.x = vp[3*i];
            vpi.y = vp[3*i+1];
            vpi.z = vp[3*i+2];
            p = R.multiply(vpi);
            writeAsciiLine(inOutputStream, "v " + p.x + " " + p.y + " " + p.z);
        }

        //-----------------------------------------------------------------
        writeAsciiLine(inOutputStream, "# " + nv + " vertex texture coordinates");
        for ( i = 0; i < nv && vuv != null; i++ ) {
            writeAsciiLine(inOutputStream, "vt " + vuv[2*i] + " " + vuv[2*i+1]);
        }

        //-----------------------------------------------------------------
        vni = new Vector3D();
        writeAsciiLine(inOutputStream, "# " + nv + " vertex normals");
        for ( i = 0; i < nv && vn != null; i++ ) {
            vni.x = vn[3*i];
            vni.y = vn[3*i+1];
            vni.z = vn[3*i+2];
            n = R.multiply(vni);
            writeAsciiLine(inOutputStream, "vn " + n.x + " " + n.y + " " + n.z);
        }

        //-----------------------------------------------------------------
        int t[] = mesh.getTriangleIndexes();

        writeAsciiLine(inOutputStream, "# " + nt + " triangles");
            long n0, n1, n2;
        writeAsciiLine(inOutputStream, "o NewObject");
        for ( i = 0; i < nt; i++ ) {
            n0 = t[3*i] + offset + 1;
            n1 = t[3*i+1] + offset + 1;
            n2 = t[3*i+2] + offset + 1;
            if ( vuv != null && vn != null ) {
                writeAsciiLine(inOutputStream, "f " + 
                    n0 + "/" + n0 + "/" + n0 + " " +
                    n1 + "/" + n1 + "/" + n1 + " " +
                    n2 + "/" + n2 + "/" + n2);
            }
            else {
                writeAsciiLine(inOutputStream, "f " + 
                    n0 + " " +
                    n1 + " " +
                    n2);
            }
        }

        return offset + nv;
    }

    public static void
    exportEnvironment(OutputStream inOutputStream, SimpleScene inScene)
        throws Exception
    {
        //-----------------------------------------------------------------
        writeAsciiLine(inOutputStream, "# OBJ File generated with VitralSDK.");
        writeAsciiLine(inOutputStream, "# http://sophia.javeriana.edu.co/~ochavarr");
        //-----------------------------------------------------------------
        ArrayList<SimpleBody> objs;
        Geometry g;
        TriangleMesh mesh;

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

            //-----------------------------------------------------------------
            if ( mesh != null ) {
                baseVertexStart += exportMesh(inOutputStream, mesh, baseVertexStart);
            }
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
