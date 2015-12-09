//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 12 2008 - Oscar Chavarro: Original base version                   =
//===========================================================================

package vsdk.toolkit.io.geometry;

// Java basic classes
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;

// VSDK Classes
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.environment.Material;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.QuadMesh;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.environment.scene.SimpleScene;
import vsdk.toolkit.io.PersistenceElement;

public class ViewpointBinaryPersistence extends PersistenceElement {

    private static Material defaultMaterial()
    {
        Material m = new Material();

        m.setAmbient(new ColorRgb(0.2, 0.2, 0.2));
        m.setDiffuse(new ColorRgb(0.5, 0.9, 0.5));
        m.setSpecular(new ColorRgb(1, 1, 1));
        return m;
    }

    private static void addThing(Geometry g,
        ArrayList<SimpleBody> inoutSimpleBodiesArray)
    {
        if ( inoutSimpleBodiesArray == null ) return;

        SimpleBody thing;

        thing = new SimpleBody();
        thing.setGeometry(g);
        thing.setPosition(new Vector3D());
        thing.setRotation(new Matrix4x4());
        thing.setRotationInverse(new Matrix4x4());
        thing.setMaterial(defaultMaterial());
        inoutSimpleBodiesArray.add(thing);
    }

    public static void
    importViewpointPolygonBinary(InputStream inGeometryFd, InputStream inColorFd, SimpleScene inoutScene)
        throws Exception
    {
        long binVersion;
        long primitiveType;
        QuadMesh mesh;
        BufferedInputStream gIs = new BufferedInputStream(inGeometryFd);
        BufferedInputStream cIs = new BufferedInputStream(inColorFd);

        binVersion = readLongBE(gIs);
        if ( binVersion != 0 ) {
            VSDK.reportMessage(null, VSDK.WARNING, "importViewpointPolygonBinary", "Wrong file format");
            return;
        }

        primitiveType = readLongBE(gIs);
        if ( primitiveType != 0x07 ) {
            VSDK.reportMessage(null, VSDK.WARNING, "importViewpointPolygonBinary", "Primitive type [" + primitiveType + "] not supported.");
            return;
        }

        mesh = new QuadMesh();

        long numIndexes;
        long numQuads;
        long numVertices;

        numQuads = readLongBE(gIs);
        numIndexes = readLongBE(gIs);
        numVertices = readLongBE(gIs);

        mesh.initVertexPositionsArray((int)numVertices);
        mesh.initVertexNormalsArray();
        mesh.initVertexColorsArray();
        mesh.initQuadArrays((int)numQuads);

        double v[];
        double n[];
        double c[];

        v = mesh.getVertexPositions();
        n = mesh.getVertexNormals();
        c = mesh.getVertexColors();
        int q[] = mesh.getQuadIndices();

        int i;

        for ( i = 0; i < numVertices; i++ ) {
            v[3*i] = readFloatBE(gIs);
            v[3*i+1] = readFloatBE(gIs);
            v[3*i+2] = readFloatBE(gIs);
        }

        for ( i = 0; i < numVertices; i++ ) {
            n[3*i] = readFloatBE(gIs);
            n[3*i+1] = readFloatBE(gIs);
            n[3*i+2] = readFloatBE(gIs);
        }

        for ( i = 0; i < numQuads; i++ ) {
            q[4*i] = (int)readLongBE(gIs);
            q[4*i+1] = (int)readLongBE(gIs);
            q[4*i+2] = (int)readLongBE(gIs);
            q[4*i+3] = (int)readLongBE(gIs);
            q[4*i]--;
            q[4*i+1]--;
            q[4*i+2]--;
            q[4*i+3]--;
        }

        double dummy;
        for ( i = 0; i < numVertices; i++ ) {
            c[3*i] = readFloatBE(cIs);
            c[3*i+1] = readFloatBE(cIs);
            c[3*i+2] = readFloatBE(cIs);
            dummy = readFloatBE(cIs);
        }

        addThing(mesh, inoutScene.getSimpleBodies());
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
