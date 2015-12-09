//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - October 26 2007 - Oscar Chavarro: Original base version               =
//===========================================================================

package vsdk.toolkit.io.geometry;

// Java basic classes
import java.util.ArrayList;
import java.io.OutputStream;

// VSDK Classes
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.TriangleMesh;
import vsdk.toolkit.environment.geometry.FunctionalExplicitSurface;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.environment.scene.SimpleScene;
import vsdk.toolkit.io.PersistenceElement;

class _WriterGtsEdge extends PersistenceElement
{
    public int from;
    public int to;
}

class _WriterGtsTriangle extends PersistenceElement
{
    public int p0;
    public int p1;
    public int p2;
}

public class WriterGts extends PersistenceElement {

    private static int
    addEdge(ArrayList<_WriterGtsEdge> edges, int from, int to)
    {
        _WriterGtsEdge e;
        int i;

        for ( i = 0; i < edges.size(); i++ ) {
            e = edges.get(i);
            if ( (e.from == from && e.to == to) ||
                 (e.from == to && e.to == from) ) {
                return i;
            }
        }

        i = edges.size();
        e = new _WriterGtsEdge();
        e.from = from;
        e.to = to;
        edges.add(e);
        return i;
    }

    private static long
    exportMesh(OutputStream inOutputStream, TriangleMesh mesh, long offset)
        throws Exception
    {
        int nv = mesh.getNumVertices();
        int nt = mesh.getNumTriangles();
        ArrayList<_WriterGtsEdge> edges;
        ArrayList<_WriterGtsTriangle> triangles;
        edges = new ArrayList<_WriterGtsEdge>();
        triangles = new ArrayList<_WriterGtsTriangle>();
        _WriterGtsEdge e;
        _WriterGtsTriangle tt;
        int i;

        //- Compute edges -------------------------------------------------
        int t[] = mesh.getTriangleIndexes();
        for ( i = 0; i < nt; i++ ) {
            tt = new _WriterGtsTriangle();
            tt.p0 = addEdge(edges, t[3*i], t[3*i+1]);
            tt.p1 = addEdge(edges, t[3*i+1], t[3*i+2]);
            tt.p2 = addEdge(edges, t[3*i+2], t[3*i]);
            triangles.add(tt);
        }

        //- Write GTS header ----------------------------------------------
        writeAsciiLine(inOutputStream, "" + nv + " " + edges.size() + " " + triangles.size() + " GtsSurface GtsFace GtsEdge GtsVertex");

        //- Write vertices ------------------------------------------------
        double v[] = mesh.getVertexPositions();
        for ( i = 0; i < nv; i++ ) {
            writeAsciiLine(inOutputStream, "" + v[3*i] + " " +
                v[3*i+1] + " " + v[3*i+2]);
        }

        //- Write edges ---------------------------------------------------
        for ( i = 0; i < edges.size(); i++ ) {
            e = edges.get(i);
            writeAsciiLine(inOutputStream, "" + (e.from+1) + " " + (e.to+1));
        }

        //- Write triangles -----------------------------------------------
        for ( i = 0; i < triangles.size(); i++ ) {
            tt = triangles.get(i);
            writeAsciiLine(inOutputStream, "" + (tt.p0+1) + " " + (tt.p1+1) + " " + (tt.p2+1));
        }

        return offset + nv;
    }

    public static void
    exportEnvironment(OutputStream inOutputStream, SimpleScene inScene)
        throws Exception
    {
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
