//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - August 8 2005 - David Diaz: Original base version                     =
//= - May 18 2006 - David Diaz: bug fixes                                   =
//= - May 22 2006 - David Diaz/Oscar Chavarro: documentation added          =
//= - November 13 2006 - Oscar Chavarro: re-structured and tested           =
//= - November 19 2006 - Oscar Chavarro: re-structured and tested - using   =
//=       private class _ReaderObjVertex and simplified TriangleMesh design =
//= - May 4 2007 - Oscar Chavarro: added support for not well formed objs   =
//===========================================================================

package vsdk.toolkit.io.geometry;

// Java basic classes
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

// VitralSDK classes
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.Triangle;
import vsdk.toolkit.common.Vertex;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.media.RGBAImage;
import vsdk.toolkit.environment.Background;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.environment.Material;
import vsdk.toolkit.environment.Light;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.TriangleMesh;
import vsdk.toolkit.environment.geometry.TriangleMeshGroup;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.environment.scene.SimpleScene;
import vsdk.toolkit.io.image.ImagePersistence;
import vsdk.toolkit.io.PersistenceElement;

//===========================================================================

/**
Class _ReaderObjVertex contains indexes to different Arrays inside a ReaderObj.
The objective of this class is to provide a temporary mapping between original
information in an Alias Wavefront Object file (.obj) and VSDK's TriangleMesh
representation. It is used only by the ReaderObj class for format conversion
of geometric data.
*/
class _ReaderObjVertex extends PersistenceElement
{
    public int vertexPositionIndex;
    public int vertexNormalIndex;
    public int vertexTextureCoordinateIndex;

    public _ReaderObjVertex()
    {
        vertexPositionIndex = -1;
        vertexNormalIndex = -1;
        vertexTextureCoordinateIndex = -1;
    }

    public _ReaderObjVertex(_ReaderObjVertex other)
    {
        vertexPositionIndex = other.vertexPositionIndex;
        vertexTextureCoordinateIndex = other.vertexTextureCoordinateIndex;
        vertexNormalIndex = other.vertexNormalIndex;
    }
    
    /**
    Pending to check why Java ask for overloading of current method. Not clear
    how should be made.
    */
    @Override
    public int hashCode()
    {
        return vertexNormalIndex + 
            10*vertexPositionIndex + 
            100*vertexTextureCoordinateIndex;
    }
    
    @Override
    public boolean equals(Object alien)
    {
        if ( !(alien instanceof _ReaderObjVertex) ) return false;
        _ReaderObjVertex other = (_ReaderObjVertex)alien;

        return !(other.vertexPositionIndex != this.vertexPositionIndex ||
            other.vertexNormalIndex != this.vertexNormalIndex ||
            other.vertexTextureCoordinateIndex !=
            this.vertexTextureCoordinateIndex);
    }

    @Override
    public String toString()
    {
        return "Vertex " + vertexPositionIndex + " / " + vertexNormalIndex
            + " / " + vertexTextureCoordinateIndex;
    }
}

//===========================================================================

/**
The class ReaderObj provides wavefront obj loading functionality. Wavefront
obj is a 3d object format used to describe polygon meshes; it is capable of
storing vertex, vertex normal, vertex texture, faces, material, texture and
other maps information.
By the use of extensions, it has the potential to describe more information.
The original Wavefront format is not well standarized, so many variations
could exist. This code currently manages only triangle faces, and interprets
other polygons to triangle fans.

\todo  Perhaps "ReaderObj" is not the best name for this class, as in the
future should support exporting (writing) operations. It could be renamed
to something as "PersistenceObj".
*/
public class ReaderObj extends PersistenceElement
{
    /**
    This method reads an Alias/Wavefront .obj file in ASCII form from the 
    given filename. A wavefront obj file can have many objects within, so this 
    method returns a group of objects rather than a single one.

    Even though a wavefront obj file has many objects, all the objects in 
    the file share a common set of vertexes; this loader only stores for a 
    TriangleMesh the vertexes that it uses, not all the array of vertexes.

    For a mesh to have a material, the matrial file has to be in the same
    folder as the mesh file; the same statement can be given about the
    textures and other maps.

    \todo  should not recieve a filename, but a previously opened stream, to
    make it independent of filesystems, and generalize it to URLs or whatever
    other connection.
    */
    private static TriangleMeshGroup read(String fileName) throws IOException
    {
        //- Geometric data and geometric attributes extracted from file ---
        ArrayList<Vector3D> vertexPositionsArray;
        ArrayList<Vector3D> vertexNormalsArray;
        ArrayList<Vector3D> vertexTextureCoordinatesArray;

        vertexPositionsArray = new ArrayList<Vector3D>();
        vertexNormalsArray = new ArrayList<Vector3D>();
        vertexTextureCoordinatesArray = new ArrayList<Vector3D>();

        //- Topology data extracted from file -----------------------------
        // _ReaderObjVertex[] will always be of size 3 (3 vertexes groups)
        ArrayList<_ReaderObjVertex[]> triangleDatasetsArray;

        triangleDatasetsArray = new ArrayList<_ReaderObjVertex[]>();

        //- Accumulated states for currently builded geometric object -----
        String nextGeometricObjectName;
        ArrayList<RGBAImage> nextTexturesArray;
        ArrayList<Material> nextMaterialsArray;

        nextGeometricObjectName = "OBJ_default_material";
        nextTexturesArray = new ArrayList<RGBAImage>();
        nextMaterialsArray = new ArrayList<Material>();        

        //- Aditional support data structures -----------------------------
        ArrayList<TriangleMesh> meshGroup;
        ArrayList<ArrayList<int[]>> texture_span_triangleRange_table;
        ArrayList<int[]> auxInitialTextureMapping;
        ArrayList<int[]> material_triangleRange_table;
        HashMap<String, RGBAImage> texturesHashMap;
        HashMap<String, Material> materialsHashMap;
        int textureIndex;

        meshGroup = new ArrayList<TriangleMesh>();
        texturesHashMap = new HashMap<String, RGBAImage>();
        materialsHashMap = new HashMap<String, Material>();        
        textureIndex = 0;

        texture_span_triangleRange_table = new ArrayList<ArrayList<int[]>>();
        auxInitialTextureMapping = new ArrayList<int[]>();
        auxInitialTextureMapping.add(new int[2]);
        texture_span_triangleRange_table.add(auxInitialTextureMapping);

        material_triangleRange_table = new ArrayList<int[]>();

        //- Geometry object processing from file / control -------------------
        BufferedReader br;
        String lineOfText;

        br = new BufferedReader(new FileReader(fileName));

        while ( (lineOfText = br.readLine()) != null ) {
            // Build material library
            if ( lineOfText.startsWith("mtllib ") ) {
                materialsHashMap = readMaterials(lineOfText, fileName);
            }
            // Change active material
            if ( lineOfText.startsWith("usemtl ") ) {
                //
                String auxMaterialName;
                StringTokenizer auxStringTokenizer;
                auxStringTokenizer = new StringTokenizer(lineOfText, " ");
                auxStringTokenizer.nextToken();
                auxMaterialName = auxStringTokenizer.nextToken();
                nextMaterialsArray.add(materialsHashMap.get(auxMaterialName));
                //
                int auxMaterialRange[];
                auxMaterialRange = new int[2];
                auxMaterialRange[0] = triangleDatasetsArray.size();
                auxMaterialRange[1] = nextMaterialsArray.size()-1;
                material_triangleRange_table.add(auxMaterialRange);
            }
            // Add a vertex
            if ( lineOfText.startsWith("v ") ) {
                vertexPositionsArray.add(readVertex(lineOfText));
            }
            // Add a normal
            if ( lineOfText.startsWith("vn ") ) {
                vertexNormalsArray.add(readVertex(lineOfText));
            }
            // Add a texture coordinate
            if ( lineOfText.startsWith("vt ") ) {
                vertexTextureCoordinatesArray.add(
                    readVertexTexture(lineOfText));
            }
            // Read faces as triangles sets
            if ( lineOfText.startsWith("f ") ) {
                try {
                    // Note that only first 3 vertexes for each polygon are
                    // processed
                    ArrayList<_ReaderObjVertex[]> auxTriangleFanSet;
                    auxTriangleFanSet = readPolygonAsTriangleFan(lineOfText);
                    _ReaderObjVertex newVertexSet[];

                    for( int i = 0; i < auxTriangleFanSet.size(); i++ ) {
                        newVertexSet = auxTriangleFanSet.get(i);
                        triangleDatasetsArray.add(newVertexSet);
                    }

                    //
                    ArrayList<int[]> actRanges;
                    actRanges =
                        texture_span_triangleRange_table.get(textureIndex);
                    //int[] lastRange = actRanges.get(actRanges.size()-1);
                    //lastRange[1] = triangleDatasetsArray.size();
                }
                catch( NoSuchElementException nsee ) {
                }
            }
            // File specified textures management
            if ( lineOfText.startsWith("usemap ") ) {
                // Put texture in hash map or select it from hash map
                String auxTextureName;
                StringTokenizer auxStringTokenizer;
                auxStringTokenizer = new StringTokenizer(lineOfText, " ");
                auxStringTokenizer.nextToken();
                auxTextureName = auxStringTokenizer.nextToken();
                if ( !texturesHashMap.containsKey(auxTextureName) ) {
                    RGBAImage auxTexture;
                    auxTexture = obtainTextureFromFile(lineOfText, fileName);
                    if ( auxTexture == null ) {
                        textureIndex = 0;
                    }
                    else {
                        nextTexturesArray.add(auxTexture);
                        texture_span_triangleRange_table.add(
                            new ArrayList<int[]>());
                        textureIndex = nextTexturesArray.size();
                    }
                    texturesHashMap.put(auxTextureName, auxTexture);
                }
                else {
                    RGBAImage auxTexture = texturesHashMap.get(auxTextureName);
                    if( auxTexture == null ) {
                        textureIndex = 0;
                    }
                    else if( !nextTexturesArray.contains(auxTexture) ) {
                        nextTexturesArray.add(auxTexture);
                        texture_span_triangleRange_table.add(
                            new ArrayList<int[]>());
                        textureIndex = nextTexturesArray.size();
                    }
                    else {
                        textureIndex=nextTexturesArray.indexOf(auxTexture)+1;
                    }
                }
                // Add selected texture to current object texture definition
                ArrayList<int[]> actRanges;
                actRanges = texture_span_triangleRange_table.get(textureIndex);
                int[] newRange=new int[2];
                newRange[0] = triangleDatasetsArray.size();
                actRanges.add(newRange);
            }
            // Object building
            if ( lineOfText.startsWith("o ") || lineOfText.startsWith("g ") ) {
                if ( vertexPositionsArray.size() > 0 ) {
                    addMeshToGroup(meshGroup,
                                   nextGeometricObjectName,
                                   vertexPositionsArray,
                                   vertexNormalsArray,
                                   vertexTextureCoordinatesArray,
                                   triangleDatasetsArray,
                                   nextTexturesArray,
                                   texture_span_triangleRange_table,
                                   nextMaterialsArray,
                                   material_triangleRange_table);
                }

                // Process next object name
                StringTokenizer auxStringTokenizer;
                auxStringTokenizer = new StringTokenizer(lineOfText, " ");
                auxStringTokenizer.nextToken();
                nextGeometricObjectName = auxStringTokenizer.nextToken();

                // Clear accumulated states variables
                if ( vertexPositionsArray.size() > 0 ) {
                    nextTexturesArray = new ArrayList<RGBAImage>();
                    nextMaterialsArray = new ArrayList<Material>();
                    triangleDatasetsArray = new ArrayList<_ReaderObjVertex[]>();
                    material_triangleRange_table = new ArrayList<int[]>();
                    texture_span_triangleRange_table =
                        new ArrayList<ArrayList<int[]>>();
                    auxInitialTextureMapping = new ArrayList<int[]>();
                    int[] auxInitialRange = new int[2];
                    auxInitialRange[0] = auxInitialRange[1] = 0;
                    auxInitialTextureMapping.add(auxInitialRange);
                    texture_span_triangleRange_table.add(auxInitialTextureMapping);
                    textureIndex = 0;
                }
            }
        }

        // Build the last mesh from remaining vertexes, if any
        if ( vertexPositionsArray.size() > 0 ) {
            addMeshToGroup(meshGroup,
                           nextGeometricObjectName,
                           vertexPositionsArray,
                           vertexNormalsArray,
                           vertexTextureCoordinatesArray,
                           triangleDatasetsArray,
                           nextTexturesArray, texture_span_triangleRange_table,
                           nextMaterialsArray, material_triangleRange_table);
        }

        //-----------------------------------------------------------------
        TriangleMeshGroup finalTriangleMeshGroup = new TriangleMeshGroup();
        TriangleMesh tm;
        int i;

        for( i = 0; i < meshGroup.size(); i++ ) {
            tm = meshGroup.get(i);
            if ( tm.getNumVertices() > 0 ) {
                finalTriangleMeshGroup.addMesh(tm);
            }
        }
        return finalTriangleMeshGroup;
    }

    private static void
    addMeshToGroup(
        ArrayList<TriangleMesh> meshGroup,
        String nextGeometricObjectName,
        ArrayList<Vector3D> vertexPositionsArray,
        ArrayList<Vector3D> vertexNormalsArray,
        ArrayList<Vector3D> vertexTextureCoordinatesArray,
        ArrayList<_ReaderObjVertex[]> triangleDatasetsArray,
        ArrayList<RGBAImage> nextTexturesArray,
        ArrayList<ArrayList<int[]>> texture_span_triangleRange_table,
        ArrayList<Material> nextMaterialsArray,
        ArrayList<int[]> material_triangleRange_table
    )
    {
        int i;
        TriangleMesh newTriangleMesh;
        newTriangleMesh = new TriangleMesh();

        //- If there are no specified materials, add a default one --------
        if ( nextMaterialsArray.isEmpty() ) {
            Material m;
            m = new Material();
            m.setName("default obj material");
            m.setDoubleSided(false);
            nextMaterialsArray.add(m);
        }

        //- Convert vertex data from obj format to VSDK format ------------
        ArrayList<_ReaderObjVertex> finalVertexes;        
        HashMap<_ReaderObjVertex, Integer> usedCombinedVertexes;

        finalVertexes = new ArrayList<_ReaderObjVertex>();
        usedCombinedVertexes = new HashMap<_ReaderObjVertex, Integer>();
        int combinedVertexCount = 0;

        for( i = 0; i < triangleDatasetsArray.size(); i++ ) {
            _ReaderObjVertex p1 = triangleDatasetsArray.get(i)[0];
            _ReaderObjVertex p2 = triangleDatasetsArray.get(i)[1];
            _ReaderObjVertex p3 = triangleDatasetsArray.get(i)[2];

            if ( !usedCombinedVertexes.containsKey(p1) ) {
                usedCombinedVertexes.put(p1, combinedVertexCount);
                combinedVertexCount++;
                finalVertexes.add(new _ReaderObjVertex(p1));
            }
            p1.vertexPositionIndex = usedCombinedVertexes.get(p1);

            if ( !usedCombinedVertexes.containsKey(p2) ) {
                usedCombinedVertexes.put(p2, combinedVertexCount);
                combinedVertexCount++;
                finalVertexes.add(new _ReaderObjVertex(p2));
            }
            p2.vertexPositionIndex = usedCombinedVertexes.get(p2);
            
            if ( !usedCombinedVertexes.containsKey(p3) ) {
                usedCombinedVertexes.put(p3, combinedVertexCount);
                combinedVertexCount++;
                finalVertexes.add(new _ReaderObjVertex(p3));
            }
            p3.vertexPositionIndex = usedCombinedVertexes.get(p3);
        }

        //- Build the mesh vertexes ---------------------------------------
        Vertex newVertexArray[];
        int ti, ni;
        Vector3D p, n;
        Matrix4x4 R = new Matrix4x4();

        R.axisRotation(Math.toRadians(90), new Vector3D(1, 0, 0));
        newVertexArray = new Vertex[finalVertexes.size()];
        for ( i = 0; i < finalVertexes.size(); i++ ) {
            // Position
            p = vertexPositionsArray.get(
                finalVertexes.get(i).vertexPositionIndex-1);
            p = R.multiply(p);
            newVertexArray[i] = new Vertex(p);
            // Texture coordinates
            ti = finalVertexes.get(i).vertexTextureCoordinateIndex - 1;
            if ( ti >= 0 ) {
                newVertexArray[i].u = vertexTextureCoordinatesArray.get(ti).x;
                newVertexArray[i].v = vertexTextureCoordinatesArray.get(ti).y;
            }
            else {
                newVertexArray[i].u = newVertexArray[i].v = 0.0;
            }
            // Normals
            ni = finalVertexes.get(i).vertexNormalIndex - 1;
            if ( ni >= 0 && ni < vertexNormalsArray.size() ) {
                n = vertexNormalsArray.get(ni);
                n = R.multiply(n);
                newVertexArray[i].setNormal(n);
            }
            else {
                newVertexArray[i].setNormal(new Vector3D(0, 0, 0));
            }
        }
        newTriangleMesh.setVertexes(newVertexArray, true, false, false, true);
        
        //- Build the mesh triangles --------------------------------------
        Triangle newTriangleArray[];

        newTriangleArray = new Triangle[triangleDatasetsArray.size()];
        for ( i = 0; i < newTriangleArray.length; i++ ) {
            newTriangleArray[i] = new Triangle();
            newTriangleArray[i].p0 =
               triangleDatasetsArray.get(i)[0].vertexPositionIndex;
            newTriangleArray[i].p1 =
               triangleDatasetsArray.get(i)[1].vertexPositionIndex;
            newTriangleArray[i].p2 =
               triangleDatasetsArray.get(i)[2].vertexPositionIndex;
        }
        newTriangleMesh.setTriangles(newTriangleArray);
        
        //- Process materials ---------------------------------------------
        Material materials[];
        materials = new Material[nextMaterialsArray.size()];

        for ( i = 0; i < materials.length; i++ ) {
            materials[i] = nextMaterialsArray.get(i);
            if ( materials[i] == null ) {
                materials[i] = new Material();
                materials[i].setDoubleSided(false);
            }
        }
        newTriangleMesh.setMaterials(materials);

        //- Process material ranges ---------------------------------------
        int auxMaterialRange[];
        auxMaterialRange = new int[2];
        auxMaterialRange[0] = triangleDatasetsArray.size();
        auxMaterialRange[1] = nextMaterialsArray.size()-1;
        material_triangleRange_table.add(auxMaterialRange);

        int materialRanges[][];

        materialRanges = new int[material_triangleRange_table.size()][2];
        for ( i = 1; i < material_triangleRange_table.size(); i++ ) {
            materialRanges[i][0] = material_triangleRange_table.get(i)[0];
            materialRanges[i][1] = material_triangleRange_table.get(i-1)[1];
        }
        newTriangleMesh.setMaterialRanges(materialRanges);

        //- Process textures ----------------------------------------------
        RGBAImage newTextureArray[];

        newTextureArray = new RGBAImage[nextTexturesArray.size()];
        for ( i = 0; i < newTextureArray.length; i++ ) {
            newTextureArray[i]=nextTexturesArray.get(i);
        }
        newTriangleMesh.setTextures(newTextureArray);

        //- Process texture ranges ----------------------------------------
        int numTextureSpans = 0;
        for ( int textureIndex = 0;
              textureIndex < texture_span_triangleRange_table.size();
              textureIndex++ ) {
            for( int j = 0;
                 j < texture_span_triangleRange_table.get(textureIndex).size();
                 j++ ) {
                numTextureSpans++;
            }
        }        

        int textureRanges[][] = new int[numTextureSpans][2];
        i = 0;
        for ( int textureIndex = 0;
              textureIndex < texture_span_triangleRange_table.size();
              textureIndex++ ) {
            for( int j = 0;
                 j < texture_span_triangleRange_table.get(textureIndex).size();
                 j++ ) {
                textureRanges[i][0] =
                  texture_span_triangleRange_table.get(textureIndex).get(j)[1];
                textureRanges[i][1] = textureIndex;
                i++;
            }
        }
        quickSortTriangleRange(textureRanges, 0, textureRanges.length-1);
        newTriangleMesh.setTextureRanges(textureRanges);

        //- Finalize mesh and add to group --------------------------------
        if ( vertexNormalsArray.size() < 0 ) {
            newTriangleMesh.calculateNormals();
        }
        newTriangleMesh.reorientateNormals();
        newTriangleMesh.setName(nextGeometricObjectName);
        meshGroup.add(newTriangleMesh);
    }

    private static void quickSortTriangleRange(int a[][], int izq, int der)
    {
        int i = izq;
        int j = der;
        int pivote = a[(izq+der)/2][0];
        int aux0;
        int aux1;
        do {
            while ( a[i][0] < pivote ) i++;
            while ( a[j][0] > pivote ) j--;
            if ( i <= j ) {
                aux0 = a[i][0];
                a[i][0] = a[j][0];
                a[j][0] = aux0;
                aux1 = a[i][1];
                a[i][1] = a[j][1];
                a[j][1] = aux1;
                i++;
                j--;
            }
        } while ( i <= j );
        if ( izq < j ) quickSortTriangleRange(a, izq, j);
        if ( i < der ) quickSortTriangleRange(a, i, der);
    }

    private static RGBAImage
    obtainTextureFromFile(String lineOfText, String fileName)
    {
        StringTokenizer st = new StringTokenizer(lineOfText, " ");
        st.nextToken(); //usemap
        String dirObj = new File(fileName).getParentFile().getAbsolutePath();
        String nomImage = st.nextToken();
        if ( nomImage.equals("(null)") ) {
            return null;
        }
        try
        {
            return ImagePersistence.importRGBA(
              new File(dirObj+System.getProperty("file.separator")+nomImage));
        }
        catch( Exception inre ) {
            return null;
        }
    }
    
    /**
    This method reads a polygon from a face line. It returns a set of triangles
    as an ArrayList of matrices. For each matrix, there is the information of
    a single triangle, where there are 3 vertexes with: vertex position index,
    texture coordinates index and vertex normal index.
    Note that the triangle set is builded as a triangle fan: the first vertex
    (p0) is a pivot which is fixed for all triangles, the second point 
    determines the first triangle edge, and for each following vertex,
    a new triangle is builded.
    */
    private static ArrayList<_ReaderObjVertex[]>
    readPolygonAsTriangleFan(String lineOfText) {
        ArrayList<_ReaderObjVertex[]> ret;
        StringTokenizer st = new StringTokenizer(lineOfText, " \n\r\t");
        st.nextToken(); // The "f" token
        int numberOfTokens = st.countTokens();
        _ReaderObjVertex p0 = null;
        _ReaderObjVertex p1 = null;
        _ReaderObjVertex p2;
        _ReaderObjVertex[] aux;
        int i;

        ret = new ArrayList<_ReaderObjVertex[]>();
        for( i = 0; i < numberOfTokens; i++ ) {
            String token = st.nextToken();
            _ReaderObjVertex indexes = readFaceVertex(token);

            if( i == 0 ) {
                p0 = indexes;
            }
            else if( i == 1 ) {
                p1 = indexes;
            }
            else {
                p2 = indexes;

                aux = new _ReaderObjVertex[3];
                aux[0] = new _ReaderObjVertex(p0);
                aux[1] = new _ReaderObjVertex(p1);
                aux[2] = new _ReaderObjVertex(p2);
                ret.add(aux);

                p1 = new _ReaderObjVertex(p2);
            }
        }
        return ret;
    }

    /**
    In some obj files (particulary those exported from 3DSMax) some array
    indexes contains negative numbers. This method is supposed to parse integer
    numbers from a string token, and returning the absolute value of it.
    */
    private static int readIndexInteger(String inToken)
    {
        int val = Integer.parseInt(inToken);
        if ( val < 0 ) val *= -1;
        return val;
    }

    /**
    Returns three indices: vertex position, texture coordinates and normal,
    as a vertex
    */    
    private static _ReaderObjVertex readFaceVertex(String lineOfText)
    {
        _ReaderObjVertex ret = new _ReaderObjVertex();

        StringTokenizer st=new StringTokenizer(lineOfText, "/");

        if ( st.countTokens() == 2 ) {
            if( lineOfText.endsWith("/") ) {
                // Has vertex and texture
                try {
                    ret.vertexPositionIndex = readIndexInteger(st.nextToken());
                }
                catch ( NumberFormatException nfe ) {
                    ret.vertexPositionIndex = -1;
                }
                try {
                    ret.vertexTextureCoordinateIndex =
                        readIndexInteger(st.nextToken());
                }
                catch ( NumberFormatException nfe ) {
                    ret.vertexTextureCoordinateIndex = -1;
                }
                ret.vertexNormalIndex = -1;
              }
              else {
                // Has vertex and normal
                try {
                    ret.vertexPositionIndex = readIndexInteger(st.nextToken());
                }
                catch ( NumberFormatException nfe ) {
                    ret.vertexPositionIndex = -1;
                }
                ret.vertexTextureCoordinateIndex=-1;
                try {
                    ret.vertexNormalIndex = readIndexInteger(st.nextToken());
                }
                catch ( NumberFormatException nfe ) {
                    ret.vertexNormalIndex = -1;
                }
            }
          }
          else {
            // Has all
            try {
                ret.vertexPositionIndex = readIndexInteger(st.nextToken());
            }
            catch ( NumberFormatException nfe ) {
                ret.vertexPositionIndex = -1;
            }
            try {
                ret.vertexTextureCoordinateIndex =
                    readIndexInteger(st.nextToken());
            }
            catch ( NumberFormatException nfe ) {
                ret.vertexTextureCoordinateIndex = -1;
            }
            try {
                ret.vertexNormalIndex = readIndexInteger(st.nextToken());
            }
            catch ( NumberFormatException nfe ) {
                ret.vertexNormalIndex = -1;
            }
        }
        return ret;
    }
    
    private static Vector3D readVertex(String lineOfText)
    {
        Vector3D vert = new Vector3D();
        StringTokenizer st = new StringTokenizer(lineOfText);
        st.nextToken();
        vert.x = Double.parseDouble(st.nextToken());
        vert.y = Double.parseDouble(st.nextToken());
        vert.z = Double.parseDouble(st.nextToken());

        return vert;
    }

    private static Vector3D readVertexTexture(String lineOfText) {
        Vector3D vert = new Vector3D();
        StringTokenizer st = new StringTokenizer(lineOfText);
        st.nextToken();
        vert.x = Double.parseDouble(st.nextToken());
        vert.y = Double.parseDouble(st.nextToken());
        try {
            vert.z = Double.parseDouble(st.nextToken());
        }
        catch( Exception e ) {}
        return vert;
    }

    private static HashMap<String, Material>
    readMaterials(String material, String fileName) {
        HashMap<String, Material> ret = new HashMap<String, Material>();
        StringTokenizer st = new StringTokenizer(material, " ");
        st.nextToken(); // "mtlib" token
        File arc = new File(fileName);
        File dirArc = arc.getParentFile();
        String nomArc;
        nomArc = dirArc + System.getProperty("file.separator")+st.nextToken();
        
        try {
            BufferedReader in=new BufferedReader(new FileReader(nomArc));
            String lineOfText;

            Material activeMaterial=new Material();
            activeMaterial.setDoubleSided(false);
            activeMaterial.setName("default");

            while( (lineOfText = in.readLine()) != null ) {
                if ( lineOfText.startsWith("Ns") ) {
                    StringTokenizer stMat=new StringTokenizer(lineOfText, " ");
                    stMat.nextToken(); // Ns
                    activeMaterial.setPhongExponent(
                        Float.parseFloat(stMat.nextToken()));
                }
                if ( lineOfText.startsWith("Kd") ) {
                    StringTokenizer stMat=new StringTokenizer(lineOfText, " ");
                    stMat.nextToken(); // Kd
                    ColorRgb color=new ColorRgb();
                    color.r=Float.parseFloat(stMat.nextToken());
                    color.g=Float.parseFloat(stMat.nextToken());
                    color.b=Float.parseFloat(stMat.nextToken());
                    activeMaterial.setDiffuse(color);
                }
                if ( lineOfText.startsWith("Ka") ) {
                    StringTokenizer stMat=new StringTokenizer(lineOfText, " ");
                    stMat.nextToken(); // Ka
                    ColorRgb color=new ColorRgb();
                    color.r=Float.parseFloat(stMat.nextToken());
                    color.g=Float.parseFloat(stMat.nextToken());
                    color.b=Float.parseFloat(stMat.nextToken());
                    activeMaterial.setAmbient(color);
                }
                if ( lineOfText.startsWith("Ks") ) {
                    StringTokenizer stMat=new StringTokenizer(lineOfText, " ");
                    stMat.nextToken(); // Ks
                    ColorRgb color=new ColorRgb();
                    color.r=Float.parseFloat(stMat.nextToken());
                    color.g=Float.parseFloat(stMat.nextToken());
                    color.b=Float.parseFloat(stMat.nextToken());
                    activeMaterial.setSpecular(color);
                }
                if ( lineOfText.startsWith("d") ) {
                    StringTokenizer stMat=new StringTokenizer(lineOfText, " ");
                    stMat.nextToken(); // d
                    //activeMaterial.setAlpha(Float.parseFloat(stMat.nextToken()));
                }
                if ( lineOfText.startsWith("newmtl") ) {
                    StringTokenizer stMat=new StringTokenizer(lineOfText, " ");
                    stMat.nextToken();//newmtl
                    ret.put(activeMaterial.getName(), activeMaterial);
                    activeMaterial = new Material();
                    activeMaterial.setDoubleSided(false);
                    activeMaterial.setName(stMat.nextToken());
                }
            }
            ret.put(activeMaterial.getName(), activeMaterial);
        }
        catch( IOException ioe ) {
        }
        return ret;
    }

    private static Material defaultMaterial()
    {
        Material m = new Material();

        m.setAmbient(new ColorRgb(0.2, 0.2, 0.2));
        m.setDiffuse(new ColorRgb(0.5, 0.9, 0.5));
        m.setSpecular(new ColorRgb(1, 1, 1));
        m.setDoubleSided(false);
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
    importEnvironment(File inSceneFileFd, SimpleScene inoutSimpleScene)
        throws Exception
    {
        //-----------------------------------------------------------------
        ArrayList<SimpleBody> simpleBodiesArray = inoutSimpleScene.getSimpleBodies();
        ArrayList<Light> lightsArray = inoutSimpleScene.getLights();
        ArrayList<Background> backgroundsArray = inoutSimpleScene.getBackgrounds();
        ArrayList<Camera> camerasArray = inoutSimpleScene.getCameras();

        //-----------------------------------------------------------------
        TriangleMeshGroup mg;
        mg = read(inSceneFileFd.getAbsolutePath());
        addThing(mg, simpleBodiesArray);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
