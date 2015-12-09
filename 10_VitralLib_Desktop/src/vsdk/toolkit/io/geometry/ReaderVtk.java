//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - August 1 2007 - Oscar Chavarro: Original base version                 =
//===========================================================================

package vsdk.toolkit.io.geometry;

// Java basic classes
import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

// VSDK Classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.Vertex;
import vsdk.toolkit.environment.Background;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.environment.Light;
import vsdk.toolkit.environment.geometry.TriangleMesh;
import vsdk.toolkit.environment.geometry.TriangleStripMesh;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.environment.scene.SimpleScene;
import vsdk.toolkit.io.PersistenceElement;

public class ReaderVtk extends PersistenceElement
{
    private static Vector3D points[] = null;
    private static Vector3D normals[] = null;
    private static long stripData[] = null;
    private static long polygonData[] = null;
    private static int numPolygons;

    private static boolean
    importVtkFragment(InputStream fis) throws Exception
    {
        String vtkDataFragment;
        int numElements;
        String elementType;
        vtkDataFragment = readAsciiLine(fis);
        int i, j;
        long numPointsInStrip;
        int numIndexesInStripSet;
        int numIndexesInPolygonSet;
        StringTokenizer auxStringTokenizer;

        if ( vtkDataFragment != null && vtkDataFragment.startsWith("POINTS ") ) {
            //System.out.print("Reading points... ");
            auxStringTokenizer = new StringTokenizer(vtkDataFragment, " ");
            auxStringTokenizer.nextToken();
            numElements = Integer.parseInt(auxStringTokenizer.nextToken());
            elementType = auxStringTokenizer.nextToken();
            if ( elementType.startsWith("float") ) {
                points = new Vector3D[numElements];
                for ( i = 0; i < numElements; i++ ) {
                    points[i] = new Vector3D();
                    points[i].x = readFloatBE(fis) / 1000.0;
                    points[i].y = readFloatBE(fis) / 1000.0;
                    points[i].z = readFloatBE(fis) / 1000.0;
                }
                //System.out.println("Ok.");
            }
            else {
                VSDK.reportMessage(null, VSDK.WARNING,
                           "ReaderVtk.importVtkFragment",
                           "Current implementation does not implement reading points from type " + elementType);
                return false;
            }
            readAsciiLine(fis); // Closing string
        }
        else if ( vtkDataFragment != null && vtkDataFragment.startsWith("TRIANGLE_STRIPS ") ) {
            //System.out.print("Reading triangle strips... ");
            auxStringTokenizer = new StringTokenizer(vtkDataFragment, " ");
            auxStringTokenizer.nextToken();
            /*numElements = */Integer.parseInt(auxStringTokenizer.nextToken());
            numIndexesInStripSet = Integer.parseInt(auxStringTokenizer.nextToken());
            stripData = new long[numIndexesInStripSet];
            for ( i = 0; i < numIndexesInStripSet; i++ ) {
                stripData[i] = readLongBE(fis);
            }
            readAsciiLine(fis); // Closing string
            //System.out.println("Ok.");
        }
        else if ( vtkDataFragment != null && vtkDataFragment.startsWith("POLYGONS ") ) {
            //System.out.print("Reading polygons... ");

            auxStringTokenizer = new StringTokenizer(vtkDataFragment, " ");
            auxStringTokenizer.nextToken();
            numPolygons = Integer.parseInt(auxStringTokenizer.nextToken());
            numIndexesInPolygonSet = Integer.parseInt(auxStringTokenizer.nextToken());
            polygonData = new long[numIndexesInPolygonSet];
            for ( i = 0; i < numIndexesInPolygonSet; i++ ) {
                polygonData[i] = readLongBE(fis);
            }
            readAsciiLine(fis); // Closing string

            //System.out.println("Ok.");
        }
        else if ( vtkDataFragment != null && vtkDataFragment.startsWith("CELL_DATA ") ) {
            // Just ignore...
        }
        else if ( vtkDataFragment != null && vtkDataFragment.startsWith("POINT_DATA ") ) {
            auxStringTokenizer = new StringTokenizer(vtkDataFragment, " ");
            auxStringTokenizer.nextToken();
            numElements = Integer.parseInt(auxStringTokenizer.nextToken());
            vtkDataFragment = readAsciiLine(fis);
            if ( vtkDataFragment != null && vtkDataFragment.startsWith("NORMALS ") ) {
                auxStringTokenizer = new StringTokenizer(vtkDataFragment, " ");
                auxStringTokenizer.nextToken();
                auxStringTokenizer.nextToken();
                elementType = auxStringTokenizer.nextToken();
                if ( elementType.startsWith("float") ) {
                    //System.out.print("Reading normals... ");
                    normals = new Vector3D[numElements];
                    for ( i = 0; i < numElements; i++ ) {
                        normals[i] = new Vector3D();
                        normals[i].x = readFloatBE(fis);
                        normals[i].y = readFloatBE(fis);
                        normals[i].z = readFloatBE(fis);
                    }
                    readAsciiLine(fis); // Closing string
                    //System.out.println("Ok.");
                }
                else {
                    VSDK.reportMessage(null, VSDK.WARNING,
                           "ReaderVtk.importVtkFragment",
                           "Current implementation can not read normals in data type " + elementType);
                    return false;
                }
            }
            else {
                VSDK.reportMessage(null, VSDK.WARNING,
                           "ReaderVtk.importVtkFragment",
                           "Current implementation does not implement reading point data from " + vtkDataFragment);
                return false;
            }
        }
        else {
            VSDK.reportMessage(null, VSDK.WARNING,
                           "ReaderVtk.importVtkFragment",
                           "Current implementation does not implement reading data from " + vtkDataFragment);
            return false;
        }
        return true;
    }

    public static void
    importEnvironment(File inSceneFileFd, SimpleScene inoutSimpleScene)
        throws Exception
    {
        //-----------------------------------------------------------------
        ArrayList<SimpleBody> simpleBodiesArray = inoutSimpleScene.getSimpleBodies();
        ArrayList<Light> lightsArray = inoutSimpleScene.getLights();
        ArrayList<Background> backgroundsArray = inoutSimpleScene.getBackgrounds();
        ArrayList<Camera> camerasArray = inoutSimpleScene.getCameras();

        //System.out.println("Reading " + inSceneFileFd.getAbsolutePath());

        //-----------------------------------------------------------------
        FileInputStream fis = new FileInputStream(inSceneFileFd);
        BufferedInputStream bis = new BufferedInputStream(fis);

        //-----------------------------------------------------------------
        String header;

        header = readAsciiLine(bis);

        if ( header == null ||
             header.length() < 1 ||
             !header.startsWith("# vtk DataFile Version ") ) {
            VSDK.reportMessage(null, VSDK.WARNING,
                           "ReaderVtk.importEnvironment",
                           "Bad header, not in VTK format.");
            return;
        }

        //-----------------------------------------------------------------
        String vtkHeader;
        String vtkBinaryMode;

        /*header = */ readAsciiLine(bis);
        vtkBinaryMode = readAsciiLine(bis);

        if ( vtkBinaryMode == null ||
             vtkBinaryMode.length() < 1 ||
             !vtkBinaryMode.startsWith("BINARY") ) {
            VSDK.reportMessage(null, VSDK.WARNING,
                           "ReaderVtk.importEnvironment",
                           "Current reader implementation only supports BINARY data representation.\n" + vtkBinaryMode + " mode found and not supported.");
            return;
        }

        //-----------------------------------------------------------------
        String vtkDataset;


        vtkDataset = readAsciiLine(bis);
        if ( vtkDataset == null ||
             vtkDataset.length() < 1 ||
             !vtkDataset.startsWith("DATASET") ) {
            VSDK.reportMessage(null, VSDK.WARNING,
                           "ReaderVtk.importEnvironment",
                           "DATASET not defined!");
            return;
        }

        String datasetType;
        datasetType = vtkDataset.substring(8);

        //-----------------------------------------------------------------
        int resting;
        do {
            if ( !importVtkFragment(bis) ) return;
            resting = bis.available();
        } while ( resting > 0 );

        //-----------------------------------------------------------------
        int acum;
        int i, j;
        long deltaTam;

        Vertex[] vertexes = new Vertex[points.length];
        Vector3D n;
        for ( i = 0; i < points.length; i++ ) {
            if ( i < normals.length ) {
                n = new Vector3D(normals[i]);
            }
            else {
                n = new Vector3D(0, 0, 1);
            }
            vertexes[i] = new Vertex(points[i], n);
        }

        if ( stripData != null ) {
            // Count strips
            for ( acum = 0, i = 0; i < stripData.length; i++, acum++ ) {
                deltaTam = stripData[i];
                for ( j = 0; j < deltaTam; j++ ) {
                    i++;
                }
            }

            // Build strips
            int strips[][];

            strips = new int[acum][];

            for ( acum = 0, i = 0; i < stripData.length; i++, acum++ ) {
                deltaTam = stripData[i];
                strips[acum] = new int[(int)deltaTam];
                for ( j = 0; j < deltaTam; j++ ) {
                    i++;
                    strips[acum][j] = (int)stripData[i];
                }
            }

            // Add triangle strip mesh
            SimpleBody newThing = new SimpleBody();
            TriangleStripMesh geometryStrip = new TriangleStripMesh();

            geometryStrip.setVertexes(vertexes);
            geometryStrip.setStrips(strips);
            newThing.setGeometry(geometryStrip);
            simpleBodiesArray.add(newThing);
        }

        //-----------------------------------------------------------------
        boolean warningDisplayed = false;

        if ( polygonData != null ) {
            TriangleMesh triangleMesh = new TriangleMesh();
            triangleMesh.setVertexes(vertexes, true, false, false, false);

            triangleMesh.initTriangleArrays(numPolygons);

            int t[] = triangleMesh.getTriangleIndexes();
            // 
            long p;
            int nn;
            nn = 0;
            for ( i = 0; i < polygonData.length; i++ ) {
                p = polygonData[i];
                if ( !warningDisplayed && p != 3  ) {
                    VSDK.reportMessage(null, VSDK.WARNING,
                           "ReaderVtk.importEnvironment",
                           "Current implementation does not manage general polygon meshes, only triangles being added");
                    warningDisplayed = true;
                    continue;
                }
                for ( j = 0; j < p; j++ ) {
                    i++;
                    t[nn] = (int)(polygonData[i]);
                    nn++;
                }
            }
            //triangleMesh.calculateNormals();

            // Add triangle strip mesh
            SimpleBody newThing = new SimpleBody();

            newThing.setGeometry(triangleMesh);
            simpleBodiesArray.add(newThing);
        }

        //-----------------------------------------------------------------
        //System.out.println("VTK import done.");
        bis.close();
        fis.close();

    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
