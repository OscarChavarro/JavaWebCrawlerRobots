//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - July 22 2007 - Oscar Chavarro: Original base version                  =
//===========================================================================

package vsdk.toolkit.io.geometry;

// Java basic classes
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.StreamTokenizer;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

// VSDK Classes
import vsdk.toolkit.common.ArrayListOfDoubles;
import vsdk.toolkit.common.ArrayListOfInts;
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.environment.Background;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.environment.Material;
import vsdk.toolkit.environment.Light;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.TriangleMeshGroup;
import vsdk.toolkit.environment.geometry.TriangleMesh;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.environment.scene.SimpleScene;
import vsdk.toolkit.io.PersistenceElement;

class _ReaderAseTriangleCache extends PersistenceElement implements Comparable <_ReaderAseTriangleCache>
{
    public int p0;
    public int p1;
    public int p2;
    public int materialId;

    public _ReaderAseTriangleCache(int p0, int p1, int p2, int materialId)
    {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.materialId = materialId;
    }

    @Override
    public int compareTo(_ReaderAseTriangleCache other) {
        if ( this.materialId > other.materialId ) {
            return 1;
        }
        else if ( this.materialId < other.materialId ) {
            return -1;
        }
        return 0;
    }
}

class _ReaderAseMeshCache extends PersistenceElement
{
    public ArrayList<_ReaderAseTriangleCache> triangles;

    public _ReaderAseMeshCache() {
        triangles = new ArrayList<_ReaderAseTriangleCache>();
    }

    public void addTriangle(int p0, int p1, int p2, int materialId)
    {
        _ReaderAseTriangleCache elem;

        elem = new _ReaderAseTriangleCache(p0, p1, p2, materialId);
        triangles.add(elem);
    }

    public void sort()
    {
        Collections.sort(triangles);
    }

    public TriangleMesh exportToMesh(ArrayListOfDoubles vertexData, int[] vertexMap)
    {
        int i;
        TriangleMesh mesh;
        mesh = new TriangleMesh();

        for ( i = 0; i < vertexMap.length; i++ ) {
            vertexMap[i] = -1;
        }

        _ReaderAseTriangleCache elem;
        int n = vertexMap.length;
        int count = 0;

        for ( i = 0; i < triangles.size(); i++ ) {
            elem = triangles.get(i);
            if ( elem.p0 < 0 || elem.p0 >= n ||
                 elem.p1 < 0 || elem.p1 >= n ||
                 elem.p2 < 0 || elem.p2 >= n ) {
                continue;
            }

            if ( vertexMap[elem.p0] < 0 ) {
                vertexMap[elem.p0] = count;
                count++;
            }
            if ( vertexMap[elem.p1] < 0 ) {
                vertexMap[elem.p1] = count;
                count++;
            }
            if ( vertexMap[elem.p2] < 0 ) {
                vertexMap[elem.p2] = count;
                count++;
            }
        }

        mesh.initVertexPositionsArray(count);

        double v[];
        v = mesh.getVertexPositions();

        int j;
        for ( i = 0; i < n; i++ ) {
            if ( vertexMap[i] >= 0 ) {
                j = vertexMap[i];
                v[3*j+0] = vertexData.get(3*i+0)/100.0;
                v[3*j+1] = vertexData.get(3*i+1)/100.0;
                v[3*j+2] = vertexData.get(3*i+2)/100.0;
            }
        }

        int t[];
        mesh.initTriangleArrays(triangles.size());
        t = mesh.getTriangleIndexes();

        for ( i = 0; i < triangles.size(); i++ ) {
            elem = triangles.get(i);
            if ( elem.p0 < 0 || elem.p0 >= n ||
                 elem.p1 < 0 || elem.p1 >= n ||
                 elem.p2 < 0 || elem.p2 >= n ) {
                continue;
            }
            t[3*i+0] = vertexMap[elem.p0];
            t[3*i+1] = vertexMap[elem.p1];
            t[3*i+2] = vertexMap[elem.p2];
        }

        mesh.calculateNormals();

        return mesh;
    }
}

public class ReaderAse extends PersistenceElement
{
    private static ArrayList <String> ids = new ArrayList<String>();

// Identifiers found inside a test set of 7588 .ase files imported from .max
/*
*3DSMAX_ASCIIEXPORT
*BITMAP
*BITMAP_FILTER
*BITMAP_INVERT
*BOUNDINGBOX_MAX
*BOUNDINGBOX_MIN
*CAMERA_FAR
*CAMERA_FOV
*CAMERA_HITHER
*CAMERA_NEAR
*CAMERAOBJECT
*CAMERA_SETTINGS
*CAMERA_TDIST
*CAMERA_TYPE
*CAMERA_YON
*COMMENT
*CONTROL_BEZIER_POS_KEY
*CONTROL_BEZIER_SCALE_KEY
*CONTROL_FLOAT_KEY
*CONTROL_FLOAT_SAMPLE
*CONTROL_POINT3_KEY
*CONTROL_POINT3_SAMPLE
*CONTROL_POS_BEZIER
*CONTROL_POS_KEY
*CONTROL_POS_LINEAR
*CONTROL_POS_SAMPLE
*CONTROL_POS_TCB
*CONTROL_POS_TRACK
*CONTROL_ROT_BEZIER
*CONTROL_ROT_KEY
*CONTROL_ROT_LINEAR
*CONTROL_ROT_SAMPLE
*CONTROL_ROT_TCB
*CONTROL_ROT_TRACK
*CONTROL_SCALE_BEZIER
*CONTROL_SCALE_SAMPLE
*CONTROL_SCALE_TCB
*CONTROL_SCALE_TRACK
*CONTROL_TCB_POS_KEY
*CONTROL_TCB_ROT_KEY
*CONTROL_TCB_SCALE_KEY
*GEOMOBJECT
*GROUP
*HELPER_CLASS
*HELPEROBJECT
*INHERIT_POS
*INHERIT_ROT
*INHERIT_SCL
*LIGHT_ABSMAPBIAS
*LIGHT_ASPECT
*LIGHT_ATTNEND
*LIGHT_ATTNSTART
*LIGHT_COLOR
*LIGHT_EXCLUDED
*LIGHT_EXCLUDED_AFFECT_ILLUM
*LIGHT_EXCLUDED_AFFECT_SHADOW
*LIGHT_EXCLUDED_INCLUDE
*LIGHT_EXCLUDELIST
*LIGHT_FALLOFF
*LIGHT_HOTSPOT
*LIGHT_INTENS
*LIGHT_MAPBIAS
*LIGHT_MAPRANGE
*LIGHT_MAPSIZE
*LIGHT_NUMEXCLUDED
*LIGHTOBJECT
*LIGHT_OVERSHOOT
*LIGHT_RAYBIAS
*LIGHT_SETTINGS
*LIGHT_SHADOWS
*LIGHT_SPOTSHAPE
*LIGHT_TDIST
*LIGHT_TYPE
*LIGHT_USEGLOBAL
*LIGHT_USELIGHT
*MAP_AMBIENT
*MAP_AMOUNT
*MAP_BUMP
*MAP_CLASS
*MAP_DIFFUSE
*MAP_FILTERCOLOR
*MAP_GENERIC
*MAP_NAME
*MAP_OPACITY
*MAP_REFLECT
*MAP_REFRACT
*MAP_SELFILLUM
*MAP_SHINE
*MAP_SHINESTRENGTH
*MAP_SPECULAR
*MAP_SUBNO
*MAP_TYPE
*MATERIAL
*MATERIAL_AMBIENT
*MATERIAL_CLASS
*MATERIAL_COUNT
*MATERIAL_DIFFUSE
*MATERIAL_FACEMAP
*MATERIAL_FALLOFF
*MATERIAL_LIST
*MATERIAL_NAME
*MATERIAL_REF
*MATERIAL_SELFILLUM
*MATERIAL_SHADING
*MATERIAL_SHINE
*MATERIAL_SHINESTRENGTH
*MATERIAL_SOFTEN
*MATERIAL_SPECULAR
*MATERIAL_TRANSPARENCY
*MATERIAL_TWOSIDED
*MATERIAL_WIRE
*MATERIAL_WIRESIZE
*MATERIAL_WIREUNITS
*MATERIAL_XP_FALLOFF
*MATERIAL_XP_TYPE
*MESH
*MESH_FACE
*MESH_FACE_LIST
*MESH_FACENORMAL
*MESH_MTLID
*MESH_NORMALS
*MESH_NUMCVERTEX
*MESH_NUMFACES
*MESH_NUMTVERTEX
*MESH_NUMTVFACES
*MESH_NUMVERTEX
*MESH_SMOOTHING
*MESH_TFACE
*MESH_TFACELIST
*MESH_TVERT
*MESH_TVERTLIST
*MESH_VERTEX
*MESH_VERTEX_LIST
*MESH_VERTEXNORMAL
*NODE_NAME
*NODE_PARENT
*NODE_TM
*NODE_VISIBILITY_TRACK
*NUMSUBMTLS
*PROP_CASTSHADOW
*PROP_MOTIONBLUR
*PROP_RECVSHADOW
*SCENE
*SCENE_AMBIENT_STATIC
*SCENE_BACKGROUND_ANIM
*SCENE_BACKGROUND_STATIC
*SCENE_ENVMAP
*SCENE_FILENAME
*SCENE_FIRSTFRAME
*SCENE_FRAMESPEED
*SCENE_LASTFRAME
*SCENE_TICKSPERFRAME
*SHAPE_CLOSED
*SHAPE_LINE
*SHAPE_LINECOUNT
*SHAPEOBJECT
*SHAPE_VERTEXCOUNT
*SHAPE_VERTEX_INTERP
*SHAPE_VERTEX_KNOT
*SUBMATERIAL
*TIMEVALUE
*TM_ANIMATION
*TM_POS
*TM_ROTANGLE
*TM_ROTAXIS
*TM_ROW0
*TM_ROW1
*TM_ROW2
*TM_ROW3
*TM_SCALE
*TM_SCALEAXIS
*TM_SCALEAXISANG
*UVW_ANGLE
*UVW_BLUR
*UVW_BLUR_OFFSET
*UVW_NOISE_LEVEL
*UVW_NOISE_PHASE
*UVW_NOISE_SIZE
*UVW_NOUSE_AMT
*UVW_U_OFFSET
*UVW_U_TILING
*UVW_V_OFFSET
*UVW_V_TILING
*WIREFRAME_COLOR
*/

    private static void skipGroup(StreamTokenizer parser, boolean debug)
    {
        int tokenType;
        int level = 0;

        if ( debug ) {
            System.out.println("Skipping group " + parser.sval);
        }

        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                break;
            }
            switch (tokenType) {
              case StreamTokenizer.TT_EOL: break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_WORD: break;
              default:
                if ( parser.ttype != '\"' ) {
                    // Only supposed to contain '{' or '}'
                    String report;
                    report = parser.toString();
                    if ( report.length() >= 8 ) {
                        char content = report.charAt(7);
                        if ( content == '{' ) {
                            level++;
                          }
                          else if ( content == '}' ) {
                            level--;
                        }
                    }
                }
                break;
            }
            if ( level == 0 ) {
                return;
            }
        } while ( tokenType != StreamTokenizer.TT_EOF );
    }

    private static ColorRgb readColorRgb(StreamTokenizer parser)
    {
        int tokenType;
        ColorRgb c = new ColorRgb(1, 1, 1);
        int i = 0;
        double vals[];
        vals = new double[3];

        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                return c;
            }
            switch (tokenType) {
              case StreamTokenizer.TT_EOL: break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_WORD:
                double val;
                try {
                    val = Double.parseDouble(parser.sval);
                }
                catch ( Exception e ) {
                    System.out.println("Parse error trying to read double at readColorRgb, number expected, recieved \"" + parser.sval + "\".");
                    return c;
                }
                vals[i] = val;
                break;
              default:
                return c;
            }
            i++;
        } while ( tokenType != StreamTokenizer.TT_EOF && i <= 2 );

        c.r = vals[0];
        c.g = vals[1];
        c.b = vals[2];
        return c;
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

    private static void readVertex(StreamTokenizer parser, ArrayListOfDoubles vertexData)
    {
        int tokenType;
        int i = 0;
        double vals[];
        vals = new double[4];
        int index;

        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                return;
            }
            switch ( tokenType ) {
              case StreamTokenizer.TT_EOL: break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_WORD:
                double val = 0;
                try {
                    if ( i == 0 ) {
                        index = Integer.parseInt(parser.sval);
                        if ( index != vertexData.size()/3 ) {
                            System.out.println("readVertex: non consecutive vertex list!");
                            System.exit(0);
                        }
                    }
                    else {
                        val = Double.parseDouble(parser.sval);
                    }
                }
                catch ( Exception e ) {
                    System.out.println("Parse error trying to read double at readVertex, number expected, recieved \"" + parser.sval + "\".");
                    return;
                }
                vals[i] = val;
                break;
              default:
                return;
            }
            i++;
        } while ( tokenType != StreamTokenizer.TT_EOF && i <= 3 );

        vertexData.add(vals[1]);
        vertexData.add(vals[2]);
        vertexData.add(vals[3]);
    }

    private static int[] parseGroupsIds(String groups) throws Exception
    {
        StringTokenizer auxStringTokenizer;
        auxStringTokenizer = new StringTokenizer(groups, ",");
        int count = auxStringTokenizer.countTokens();

        int g[] = new int[count];
        int i;

        for ( i = 0; i < count; i++ ) {
            g[i] = Integer.parseInt(auxStringTokenizer.nextToken());
        }

        return g;
    }

    private static void readTriangle(StreamTokenizer parser, ArrayListOfInts triangleData)
    {
        int tokenType;
        int i = 0;
        int vals[];
        vals = new int[5]; // p0, p1, p2, smothgroupid, materialid
        int index = -1;
        String groups = null;

        parser.wordChars(',', ',');

        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                return;
            }
            switch ( tokenType ) {
              case StreamTokenizer.TT_EOL: break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_WORD:
                double val = 0;
                try {
                    // Smooth group
                    if ( i == 15 && !parser.sval.equals("*MESH_MTLID") ) {
                        groups = "-1";
                    }
                    else if ( i == 14 ) {
                        groups = parser.sval;
                    }

                    if ( (i == 1 && !parser.sval.equals("A:")) ||
                         (i == 3 && !parser.sval.equals("B:")) ||
                         (i == 5 && !parser.sval.equals("C:")) ||
                         (i == 7 && !parser.sval.equals("AB:")) ||
                         (i == 9 && !parser.sval.equals("BC:")) ||
                         (i == 11 && !parser.sval.equals("CA:")) ||
                         (i == 13 && !parser.sval.equals("*MESH_SMOOTHING")) 
                         ) {
                        System.out.println("Error in triangle format!");
                        System.out.println("  - Line " + parser.lineno());
                        System.out.println("  - Token " + i);
                        System.exit(0);
                    }
                    else if ( i == 2 ) {
                        // P0
                        vals[0] = Integer.parseInt(parser.sval);
                    }
                    else if ( i == 4 ) {
                        // P1
                        vals[1] = Integer.parseInt(parser.sval);
                    }
                    else if ( i == 6 ) {
                        // P2
                        vals[2] = Integer.parseInt(parser.sval);
                    }
                    else if ( i == 16 ) {
                        // Material ID
                        vals[4] = Integer.parseInt(parser.sval);
                    }
                }
                catch ( Exception e ) {
                    System.out.println("Parse error trying to read int at readTriangle, number expected, recieved \"" + parser.sval + "\", token " + i + ".");
                    return;
                }
                break;
              default:
                i = 666;
                break;
            }
            i++;
        } while ( tokenType != StreamTokenizer.TT_EOF && i <= 16 );
        parser.whitespaceChars(',', ',');

        //-----------------------------------------------------------------
        int g[] = null;
        try {
            g = parseGroupsIds(groups);
        }
        catch ( Exception e ) {
            System.out.println("Error reading smoothing groups!");
            System.exit(0);
        }

        int j;

        for ( i = 0; i < g.length; i++ ) {
            vals[3] = g[i];
            for ( j = 0; j < 5; j++ ) {
                triangleData.add(vals[j]);
            }
        }
    }

    private static void processMeshVertexListGroup(StreamTokenizer parser, ArrayListOfDoubles vertexData)
    {
        //-----------------------------------------------------------------
        int tokenType;
        int level = 0;

        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                break;
            }
            switch (tokenType) {
              case StreamTokenizer.TT_EOL: break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_WORD:
                if ( parser.sval.startsWith("*") ) {
                    String node = parser.sval.toUpperCase();
                    if ( node.equals("*MESH_VERTEX") ) {
                        readVertex(parser, vertexData);
                    }
                    else {
                        // Unknown unhandled cases
                        skipGroup(parser, true);
                    }
                }
                else {
                    System.out.println("UNMANAGED WORD " + parser.sval);
                }
                break;
              default:
                if ( parser.ttype != '\"' ) {
                    // Only supposed to contain '{' or '}'
                    String report;
                    report = parser.toString();
                    if ( report.length() >= 8 ) {
                        char content = report.charAt(7);
                        if ( content == '{' ) {
                            level++;
                          }
                          else if ( content == '}' ) {
                            level--;
                        }
                    }
                }
                break;
            }
            if ( level == 0 ) {
                break;
            }
        } while ( tokenType != StreamTokenizer.TT_EOF );

        //-----------------------------------------------------------------
    }

    private static void processMeshFaceListGroup(StreamTokenizer parser, ArrayListOfInts triangleData)
    {
        //-----------------------------------------------------------------
        int tokenType;
        int level = 0;

        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                break;
            }
            switch (tokenType) {
              case StreamTokenizer.TT_EOL: break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_WORD:
                if ( parser.sval.startsWith("*") ) {
                    String node = parser.sval.toUpperCase();
                    if ( node.equals("*MESH_FACE") ) {
                        readTriangle(parser, triangleData);
                    }
                    else {
                        // Unknown unhandled cases
                        skipGroup(parser, true);
                    }
                }
                else {
                    System.out.println("UNMANAGED WORD " + parser.sval);
                }
                break;
              default:
                if ( parser.ttype != '\"' ) {
                    // Only supposed to contain '{' or '}'
                    String report;
                    report = parser.toString();
                    if ( report.length() >= 8 ) {
                        char content = report.charAt(7);
                        if ( content == '{' ) {
                            level++;
                          }
                          else if ( content == '}' ) {
                            level--;
                        }
                    }
                }
                break;
            }
            if ( level == 0 ) {
                break;
            }
        } while ( tokenType != StreamTokenizer.TT_EOF );

        //-----------------------------------------------------------------
    }

    private static TriangleMeshGroup processMeshGroup(StreamTokenizer parser)
    {
        //-----------------------------------------------------------------
        int tokenType;
        int level = 0;
        ArrayListOfDoubles vertexData = new ArrayListOfDoubles(1000000);
        ArrayListOfInts triangleData = new ArrayListOfInts(1000000);

        System.out.println("--- Processing mesh group " + parser.sval);

        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                break;
            }
            switch (tokenType) {
              case StreamTokenizer.TT_EOL: break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_WORD:
                if ( parser.sval.startsWith("*") ) {
                    String node = parser.sval.toUpperCase();
                    if ( node.equals("*MESH_NUMVERTEX") ||
                         node.equals("*MESH_NUMFACES")
                       ) {
                        // Redundant blocks explicity ignored
                        skipGroup(parser, false);
                    }
                    else if ( node.equals("*MESH_VERTEX_LIST") ) {
                        processMeshVertexListGroup(parser, vertexData);
                    }
                    else if ( node.equals("*MESH_FACE_LIST") ) {
                        processMeshFaceListGroup(parser, triangleData);
                    }
                    else if ( node.equals("*TIMEVALUE") 
                        ) {
                        // Known unhandled cases
                        skipGroup(parser, false);
                    }
                    else {
                        // Unknown unhandled cases
                        skipGroup(parser, true);
                    }
                }
                else {
                    System.out.println("UNMANAGED WORD " + parser.sval);
                }
                break;
              default:
                if ( parser.ttype != '\"' ) {
                    // Only supposed to contain '{' or '}'
                    String report;
                    report = parser.toString();
                    if ( report.length() >= 8 ) {
                        char content = report.charAt(7);
                        if ( content == '{' ) {
                            level++;
                          }
                          else if ( content == '}' ) {
                            level--;
                        }
                    }
                }
                break;
            }
            if ( level == 0 ) {
                break;
            }
        } while ( tokenType != StreamTokenizer.TT_EOF );

        //-----------------------------------------------------------------
        TriangleMeshGroup mg;
        mg = new TriangleMeshGroup();

        //-----------------------------------------------------------------
        // Place each triangle in a set. There is a set for each smooth
        // group identifier.
        HashMap<Integer, _ReaderAseMeshCache> smoothGroups;

        smoothGroups = new HashMap<Integer, _ReaderAseMeshCache>();

        int i;
        int p0, p1, p2, smoothGroupId, materialId;
        for ( i = 0; i < triangleData.size()/5; i++ ) {
            _ReaderAseMeshCache group;

            p0 = triangleData.get(5*i+0);
            p1 = triangleData.get(5*i+1);
            p2 = triangleData.get(5*i+2);
            smoothGroupId = triangleData.get(5*i+3);

            materialId = triangleData.get(5*i+4);
            group = smoothGroups.get(new Integer(smoothGroupId));
            if ( group == null ) {
                group = new _ReaderAseMeshCache();
                smoothGroups.put(new Integer(smoothGroupId), group);
            }
            group.addTriangle(p0, p1, p2, materialId);
        }

        //-----------------------------------------------------------------
        // Sort each set by material id, in preparation for material range
        // subgrouping inside each triangle mesh.
        Collection<_ReaderAseMeshCache> set;

        set = smoothGroups.values();
        for ( Iterator<_ReaderAseMeshCache> it = set.iterator();
              it.hasNext(); ) {
            _ReaderAseMeshCache e;
            e = it.next();
            e.sort();
        }

        //-----------------------------------------------------------------
        // Export each smoothing group as a triangle mesh
        TriangleMesh mesh;
        int vertexIndexMap[];

        System.out.println("Resulting hashmap size: " + smoothGroups.size());

        vertexIndexMap = new int[vertexData.size()/3];
        Iterator<_ReaderAseMeshCache> it;
        for ( i = 0, it = set.iterator(); it.hasNext(); i++ ) {
            _ReaderAseMeshCache e;
            e = it.next();

            mesh = e.exportToMesh(vertexData, vertexIndexMap);
            if ( mesh != null ) {
                mg.addMesh(mesh);
            }
        }

        //-----------------------------------------------------------------
        //System.exit(0);
        return mg;
    }

    private static void processGeomobjectGroup(
        StreamTokenizer parser,
        ArrayList<SimpleBody> simpleBodiesArray)
    {
        int tokenType;
        int level = 0;
        SimpleBody thing;
        Vector3D position = new Vector3D();
        Matrix4x4 R = new Matrix4x4();
        Geometry g = null;
        Material material = defaultMaterial();

        thing = new SimpleBody();

        System.out.println("=== Processing body:");
        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                break;
            }
            switch (tokenType) {
              case StreamTokenizer.TT_EOL: break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_WORD:
                if ( parser.sval.startsWith("*") ) {
                    String node = parser.sval.toUpperCase();
                    if ( node.equals("*MESH") ) {
                        g = processMeshGroup(parser);
                    }
                    else if ( node.equals("*WIREFRAME_COLOR") ) {
                        ColorRgb c;
                        c = readColorRgb(parser);
                        // Unhandled color!
                    }
                    else if ( node.equals("*NODE_NAME") ) {
                        try { 
                            parser.nextToken(); 
                        }
                        catch ( Exception e ) { 
                        }
                        thing.setName(parser.sval);
                    }
                    else {
                        skipGroup(parser, true);
                    }
                }
                else {
                    System.out.println("UNMANAGED WORD " + parser.sval);
                }
                break;
              default:
                if (parser.ttype == '\"') {
                    //System.out.println("STRING " + parser.sval);
                  }
                  else {
                    // Only supposed to contain '{' or '}'
                    String report;
                    report = parser.toString();
                    if ( report.length() >= 8 ) {
                        char content = report.charAt(7);
                        if (content == '{') {
                            level++;
                          }
                          else if (content == '}') {
                            level--;
                          } else {
                            // Nothing is done, as this is and unknown token,
                            // posibly corresponding to an empty token (i.e.
                            // a comment line with no real information)
                        }
                    }
                }
                break;
            }
            if ( level == 0 ) {
                break;
            }
        } while ( tokenType != StreamTokenizer.TT_EOF );

        //-----------------------------------------------------------------
        if ( g != null ) {
            thing.setPosition(position);
            thing.setRotation(R);
            thing.setRotationInverse(R.inverse());
            thing.setMaterial(material);
            thing.setGeometry(g);
            simpleBodiesArray.add(thing);
        }
        System.out.println("===");
    }

    private static void processGroupGroup(
        StreamTokenizer parser,
        ArrayList<SimpleBody> simpleBodiesArray,
        ArrayList<Light> lightsArray,
        ArrayList<Background> backgroundsArray,
        ArrayList<Camera> camerasArray)
    {
        int tokenType;
        int level = 0;
        SimpleBody thing;
        Vector3D position = new Vector3D();
        Matrix4x4 R = new Matrix4x4();
        Geometry g = null;
        Material material = defaultMaterial();

        thing = new SimpleBody();

        System.out.println("*** PROCESSING GROUP!");
        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                break;
            }
            switch (tokenType) {
              case StreamTokenizer.TT_EOL: break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_WORD:
                if ( parser.sval.startsWith("*") ) {
                    processGroup(parser, simpleBodiesArray, lightsArray, backgroundsArray, camerasArray);
                }
                else {
                    System.out.println("UNMANAGED WORD " + parser.sval);
                }
                break;
              default:
                if ( parser.ttype == '\"' ) {
                    //System.out.println("STRING " + parser.sval);
                  }
                  else {
                    // Only supposed to contain '{' or '}'
                    String report;
                    report = parser.toString();
                    if ( report.length() >= 8 ) {
                        char content = report.charAt(7);
                        if (content == '{') {
                            //System.out.println("{ MARK");
                            level++;
                          }
                          else if (content == '}') {
                            //System.out.println("} MARK");
                              level--;
                          } else {
                            // Nothing is done, as this is and unknown token,
                            // posibly corresponding to an empty token (i.e.
                            // a comment line with no real information)
                        }
                    }
                }
                break;
            }
        } while ( tokenType != StreamTokenizer.TT_EOF );

        System.out.println("*** END PROCESSING GROUP!");

        //-----------------------------------------------------------------
        if ( g != null ) {
            thing.setPosition(position);
            thing.setRotation(R);
            thing.setRotationInverse(R.inverse());
            thing.setMaterial(material);
            thing.setGeometry(g);
            simpleBodiesArray.add(thing);
        }
        System.out.println("===");
    }

    private static void processGroup(
        StreamTokenizer parser,
        ArrayList<SimpleBody> simpleBodiesArray,
        ArrayList<Light> lightsArray,
        ArrayList<Background> backgroundsArray,
        ArrayList<Camera> camerasArray)
    {
        if ( parser.sval.equals("*GEOMOBJECT") ) {
            processGeomobjectGroup(parser, simpleBodiesArray);
        }
        else if ( parser.sval.equals("*GROUP") ) {
            processGroupGroup(parser, simpleBodiesArray, lightsArray, backgroundsArray, camerasArray);
        }
        else {
            skipGroup(parser, true);
        }
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
        BufferedReader br = new BufferedReader(new FileReader(inSceneFileFd));
        StreamTokenizer parser = new StreamTokenizer(br);

        parser.resetSyntax();
        parser.eolIsSignificant(true);
        parser.quoteChar('\"');
        parser.slashSlashComments(false);
        parser.slashStarComments(false);
        parser.whitespaceChars(' ', ' ');
        parser.whitespaceChars(',', ',');
        parser.whitespaceChars('\t', '\t');
        parser.whitespaceChars('\n', '\n');
        parser.whitespaceChars('\r', '\r');
        parser.wordChars('*', '*');
        parser.wordChars('0', '9');
        parser.wordChars('.', '.');
        parser.wordChars('A', 'Z');
        parser.wordChars('a', 'z');
        parser.wordChars('_', '_');
        parser.wordChars('`', '`');
        parser.wordChars('(', '(');
        parser.wordChars(')', ')');
        parser.wordChars('\'', '\'');
        parser.wordChars('+', '+');
        parser.wordChars('?', '?');
        parser.wordChars('!', '!');
        parser.wordChars('=', '=');
        parser.wordChars('&', '&');
        parser.wordChars('/', '/');
        parser.wordChars('#', '#');
        parser.wordChars('\\', '\\');
        parser.wordChars(':', ':');
        parser.wordChars('-', '-');

        // Important: do not use parsenumbers, as this functionality doesn't
        // recognize numbers in scientific notation (with exponents)
        //parser.parseNumbers();

        int tokenType;
        long line = 0;
        int group = 0;
        int level = 0;

        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                break;
            }
            switch (tokenType) {
              case StreamTokenizer.TT_EOL: break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_WORD:
                if ( parser.sval.startsWith("*") ) {
                    processGroup(parser, simpleBodiesArray, lightsArray, backgroundsArray, camerasArray);
                }
                else {
                    System.out.println("UNMANAGED WORD " + parser.sval);
                }
                break;
              default:
                if (parser.ttype == '\"') {
                    //System.out.println("STRING " + parser.sval);
                  }
                  else {
                    // Only supposed to contain '{' or '}'
                    String report;
                    report = parser.toString();
                    if ( report.length() >= 8 ) {
                        char content = report.charAt(7);
                        if (content == '{') {
                            //System.out.println("{ MARK");
                            level++;
                          }
                          else if (content == '}') {
                            //System.out.println("} MARK");
                              level--;
                          } else {
                            // Nothing is done, as this is and unknown token,
                            // posibly corresponding to an empty token (i.e.
                            // a comment line with no real information)
                        }
                    }
                }
                break;
            }
        } while ( tokenType != StreamTokenizer.TT_EOF );

        System.out.print(inSceneFileFd.getAbsolutePath());

        if ( level == 0 ) { 
            System.out.println(" OKOK!");
        }
        else {
            System.out.println(" BADBAD: Final level " + level);
        }
        //System.exit(0);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
