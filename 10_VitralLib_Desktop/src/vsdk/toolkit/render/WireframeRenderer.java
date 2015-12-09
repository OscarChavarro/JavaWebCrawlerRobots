//===========================================================================
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [FOLE1992] Foley, vanDam, Feiner, Hughes. "Computer Graphics,           =
//=          principles and practice" - second edition, Addison Wesley,     =
//=          1992.                                                          =
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - December 30 2007 - Oscar Chavarro: Original base version              =
//===========================================================================

package vsdk.toolkit.render;

// Java classes
import java.util.ArrayList;

// VitralSDK classes
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.linealAlgebra.Vector4D;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.environment.geometry.Surface;
import vsdk.toolkit.environment.geometry.Solid;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.TriangleMesh;
import vsdk.toolkit.environment.geometry.PolyhedralBoundedSolid;
import vsdk.toolkit.environment.geometry.TriangleMeshGroup;
import vsdk.toolkit.environment.geometry.polyhedralBoundedSolidNodes._PolyhedralBoundedSolidEdge;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.media.Calligraphic2DBuffer;

public class WireframeRenderer extends RenderingElement
{
    /**
    Given a 3D line (with endpoints `cp0` and `cp1`), previously clipped
    against the current view volume, this method projects the line in to the
    projection plane by applying a projection transformation specified by 
    `Proj`, and adds the resulting 2D line to the Calligraphic2DBuffer
    `lineSet`.
    */
    private static void addLine(Calligraphic2DBuffer lineSet,
                                Vector3D cp0, Vector3D cp1, Matrix4x4 Proj,
                                Camera c) {
        //-----------------------------------------------------------------
        Vector4D hp0, hp1; // Clipped points in homogeneous space
        Vector4D pp0, pp1; // Projected points

        double f;
        f = 1;// f = (c.getFarPlaneDistance() - c.getNearPlaneDistance())/20;

        hp0 = new Vector4D(cp0);
        hp1 = new Vector4D(cp1);
        pp0 = Proj.multiply(hp0);
        pp0.divideByW();
        pp1 = Proj.multiply(hp1);
        pp1.divideByW();
        lineSet.add2DLine(pp0.x*f, pp0.y*f, pp1.x*f, pp1.y*f);
    }

    private static void processBrep(SimpleBody body, Matrix4x4 P,
                        Calligraphic2DBuffer outLineSet,
                        Camera inCamera)
    {
        //-----------------------------------------------------------------
        PolyhedralBoundedSolid brep;

        brep = body.getGeometry().exportToPolyhedralBoundedSolid();
        if ( brep == null ) return;

        //-----------------------------------------------------------------
        int i;
        Vector3D mp0, mp1;         // Edge points
        Vector3D cp0, cp1;         // Clipped points
        Matrix4x4 M;

        M = body.getTransformationMatrix();
        cp0 = new Vector3D();
        cp1 = new Vector3D();

        for ( i = 0; i < brep.edgesList.size(); i++ ) {
            _PolyhedralBoundedSolidEdge e = brep.edgesList.get(i);
            int start, end;
            start = e.getStartingVertexId();
            end = e.getEndingVertexId();
            if ( start >= 0 && end >= 0 ) {
                mp0 = e.rightHalf.startingVertex.position;
                mp1 = e.leftHalf.startingVertex.position;
                if ( mp0 != null && mp1 != null ) {
                    mp0 = M.multiply(mp0);
                    mp1 = M.multiply(mp1);
                    if ( inCamera.clipLineCohenSutherlandCanonicVolume(mp0, mp1, cp0, cp1) ) {
                        addLine(outLineSet, cp0, cp1, P, inCamera);
                    }

                }
            }
        }
    }

    private static void processMesh(SimpleBody body, Matrix4x4 P,
                        Calligraphic2DBuffer outLineSet,
                        Camera inCamera)
    {
        int j;                     // subobject index
        int t;                     // triangle index
        TriangleMeshGroup mg;
        TriangleMesh mesh;
        int nv;
        int nt;
        double v[];
        int tr[];
        Matrix4x4 M;               // Modelview matrix
        int p0, p1, p2;
        Vector3D mp0, mp1;         // Mesh points
        Vector3D cp0, cp1;         // Clipped points

        mg = body.getGeometry().exportToTriangleMeshGroup();
        if ( mg == null ) return;

        mp0 = new Vector3D();
        mp1 = new Vector3D();
        cp0 = new Vector3D();
        cp1 = new Vector3D();

        M = body.getTransformationMatrix();
        for ( j = 0; j < mg.getMeshes().size(); j++ ) {
            mesh = mg.getMeshes().get(j);
            nv = mesh.getNumVertices();
            nt = mesh.getNumTriangles();
            v = mesh.getVertexPositions();
            tr = mesh.getTriangleIndexes();
            for ( t = 0; t < nt; t++ ) {
                p0 = tr[3*t];
                p1 = tr[3*t+1];
                p2 = tr[3*t+2];

                mp0.x = v[3*p0];
                mp0.y = v[3*p0+1];
                mp0.z = v[3*p0+2];
                mp1.x = v[3*p1];
                mp1.y = v[3*p1+1];
                mp1.z = v[3*p1+2];
                mp0 = M.multiply(mp0);
                mp1 = M.multiply(mp1);
                if ( inCamera.clipLineCohenSutherlandCanonicVolume(mp0, mp1, cp0, cp1) ) {
                    addLine(outLineSet, cp0, cp1, P, inCamera);
                }

                mp0.x = v[3*p1];
                mp0.y = v[3*p1+1];
                mp0.z = v[3*p1+2];
                mp1.x = v[3*p2];
                mp1.y = v[3*p2+1];
                mp1.z = v[3*p2+2];
                mp0 = M.multiply(mp0);
                mp1 = M.multiply(mp1);
                if ( inCamera.clipLineCohenSutherlandCanonicVolume(mp0, mp1, cp0, cp1) ) {
                    addLine(outLineSet, cp0, cp1, P, inCamera);
                }

                mp0.x = v[3*p2];
                mp0.y = v[3*p2+1];
                mp0.z = v[3*p2+2];
                mp1.x = v[3*p0];
                mp1.y = v[3*p0+1];
                mp1.z = v[3*p0+2];
                mp0 = M.multiply(mp0);
                mp1 = M.multiply(mp1);
                if ( inCamera.clipLineCohenSutherlandCanonicVolume(mp0, mp1, cp0, cp1) ) {
                    addLine(outLineSet, cp0, cp1, P, inCamera);
                }
            }
        }
    }

    public static void execute(Calligraphic2DBuffer outLineSet,
                        ArrayList <SimpleBody> inSimpleBodyArray,
                        Camera inCamera)
    {
        //- Calligraphic rendering of lines in to 2D line buffer ----------
        int i;                     // Index inside objects list
        Matrix4x4 P;               // Projection matrix
        Geometry g;

        P = new Matrix4x4();
        P.canonicalPerspectiveProjection();

        for ( i = 0; i < inSimpleBodyArray.size(); i++ ) {
            g = inSimpleBodyArray.get(i).getGeometry();
            if ( g instanceof Surface ) {
                processMesh(inSimpleBodyArray.get(i), P, outLineSet, inCamera);
            }
            else if ( g instanceof Solid ) {
                processBrep(inSimpleBodyArray.get(i), P, outLineSet, inCamera);
            }
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
