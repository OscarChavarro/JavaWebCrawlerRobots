//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 30 2008 - Oscar Chavarro: Original base version                   =
//===========================================================================

package vsdk.toolkit.io.geometry;

// Java basic classes
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.StreamTokenizer;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

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
import vsdk.toolkit.environment.geometry.TriangleMesh;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.environment.scene.SimpleScene;
import vsdk.toolkit.io.PersistenceElement;

public class ReaderVrml extends PersistenceElement
{
    private static BufferedReader processHeader(File inSceneFileFd)
        throws Exception
    {
        BufferedReader br;

        //-----------------------------------------------------------------
        FileInputStream fis = new FileInputStream(inSceneFileFd);

        byte magic[];
        magic = new byte[1];
        readBytes(fis, magic);

        if ( magic[0] != '#' ) {
            // Trying to process file as gzipped wrl
            fis.close();

            try {
                fis = new FileInputStream(inSceneFileFd);
                GZIPInputStream gis = new GZIPInputStream(fis);

                readBytes(gis, magic);
                if ( magic[0] != '#' ) {
                    return null;
                }

                br = new BufferedReader(new InputStreamReader(gis));
                return br;
            }
            catch ( Exception e ) {
                System.out.println(inSceneFileFd.getAbsolutePath() + "*** - ERROR: Wrong format (not a gzipped file)!");
                return null;
            }
        }
        fis.close();

        //-----------------------------------------------------------------
        br = new BufferedReader(new FileReader(inSceneFileFd));
        return br;
    }

    private static void skipGroup(StreamTokenizer parser)
    {
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

    private static boolean readVertexPositions(StreamTokenizer parser, TriangleMesh mesh)
    {
        //-----------------------------------------------------------------
        int tokenType;
        int level = 0;
        boolean inside = false;

        ArrayListOfDoubles vals = new ArrayListOfDoubles(100000);

        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                break;
            }

            switch ( tokenType ) {
              case StreamTokenizer.TT_EOL: break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_WORD:
                if ( parser.sval.toLowerCase().equals("point") ) {
                    inside = true;
                }
                else if ( inside ) {
                    double val;
                    try {
                        val = Double.parseDouble(parser.sval);
                    }
                    catch ( Exception e ) {
                        System.out.println("Parse error trying to read double at readVertexPositions, number expected, recieved \"" + parser.sval + "\".");
                        return false;
                    }
                    vals.add(val);
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
            if ( level == 0 && inside ) {
                break;
            }
        } while ( tokenType != StreamTokenizer.TT_EOF );

        //-----------------------------------------------------------------
        int i;
        double v[];

        mesh.initVertexPositionsArray(vals.size());
        v = mesh.getVertexPositions();
        for ( i = 0; i < vals.size(); i++ ) {
            v[i] = vals.get(i);
        }
        return true;
    }

    private static boolean readVertexColors(StreamTokenizer parser, TriangleMesh mesh)
    {
        //-----------------------------------------------------------------
        int tokenType;
        int level = 0;
        int i = 0;
        boolean inside = false;

        ArrayListOfDoubles vals = new ArrayListOfDoubles(100000);

        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                break;
            }
            switch ( tokenType ) {
              case StreamTokenizer.TT_EOL: break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_WORD:
                if ( parser.sval.toLowerCase().equals("color") && i > 1 ) {
                    inside = true;
                }
                else if ( inside && parser.sval.charAt(0) == 'e' ) {
                    // Exponent!
                }
                else if ( inside ) {
                    double val;
                    try {
                        val = Double.parseDouble(parser.sval);
                    }
                    catch ( Exception e ) {
                        System.out.println("Parse error trying to read double at readVertexColors, number expected, recieved \"" + parser.sval + "\".");
                        return false;
                    }
                    vals.add(val);
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
            if ( level == 0 && inside ) {
                break;
            }
            i++;
        } while ( tokenType != StreamTokenizer.TT_EOF );

        //-----------------------------------------------------------------
        mesh.initVertexColorsArray();
        double c[];
        c = mesh.getVertexColors();
        for ( i = 0; i < vals.size() && i < c.length; i++ ) {
            c[i] = vals.get(i);
        }
        return true;
    }

    private static boolean readPolygons(StreamTokenizer parser, TriangleMesh mesh)
    {
        //-----------------------------------------------------------------
        int tokenType;
        int level = 0;
        boolean inside = false;

        ArrayListOfInts vals = new ArrayListOfInts(100000);

        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                break;
            }

            switch ( tokenType ) {
              case StreamTokenizer.TT_EOL: break;
              case StreamTokenizer.TT_EOF: break;
              case StreamTokenizer.TT_WORD:
                int val;
                try {
                    val = Integer.parseInt(parser.sval);
                }
                catch ( Exception e ) {
                    System.out.println("Parse error trying to read integer at readPolygons, number expected, recieved \"" + parser.sval + "\".");
                    return false;
                }
                vals.add(val);
                break;
              default:
                if ( parser.ttype != '\"' ) {
                    // Only supposed to contain '{' or '}'
                    String report;
                    report = parser.toString();
                    if ( report.length() >= 8 ) {
                        char content = report.charAt(7);
                        if ( content == '[' ) {
                            level++;
                          }
                          else if ( content == ']' ) {
                            level--;
                        }
                    }
                }
                break;
            }
            if ( level == 0 && inside ) {
                break;
            }
        } while ( tokenType != StreamTokenizer.TT_EOF );

        //-----------------------------------------------------------------
        int i;
        int n;
        int triangleCount;
        int index;

        for ( i = 0, n = 0, triangleCount = 0; i < vals.size(); i++ ) {
            index = vals.get(i);
            if ( index == -1 ) {
                if ( n >= 3 ) {
                    triangleCount += (n-2);
                }
                n = 0;
            }
            else {
                n++;
            }
        }

        int t[];
        int p0 = 0;
        int p1 = 0;
        int p2;
        int j;
        int k;
        int tindex;

        mesh.initTriangleArrays(triangleCount);
        t = mesh.getTriangleIndexes();

        for ( i = 0, n = 0, tindex = 0; i < vals.size(); i++ ) {
            index = vals.get(i);
            if ( index == -1 ) {
                for ( j = n, k = 0; n >=3 && j > 0; j--, k++ ) {
                    index = vals.get(i-j);

                    if( k == 0 ) {
                        p0 = index;
                    }
                    else if( k == 1 ) {
                        p1 = index;
                    }
                    else {
                        p2 = index;
                        // Add a triangle over <p0, p1, p2>
                        t[3*tindex+0] = p0;
                        t[3*tindex+1] = p1;
                        t[3*tindex+2] = p2;
                        tindex++;
                        //
                        p1 = index;
                    }
                }
                n = 0;
            }
            else {
                n++;
            }
        }
        return true;
    }

    private static void processTransformationChildren(
        StreamTokenizer parser,
        ArrayList<SimpleBody> simpleBodiesArray,
        ArrayList<Light> lightsArray,
        ArrayList<Background> backgroundsArray,
        ArrayList<Camera> camerasArray,
        Matrix4x4 M)
    {
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
                processGroup(parser, simpleBodiesArray, lightsArray, backgroundsArray, camerasArray, M);
                break;
              default:
                if ( parser.ttype != '\"' ) {
                    // Only supposed to contain '{' or '}'
                    String report;
                    report = parser.toString();
                    if ( report.length() >= 8 ) {
                        char content = report.charAt(7);
                        if ( content == '[' ) {
                            level++;
                          }
                          else if ( content == ']' ) {
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

    private static Matrix4x4 processRotation(StreamTokenizer parser)
    {
        Matrix4x4 R = new Matrix4x4();
        int i = 0;
        double vals[];
        vals = new double[4];
        int tokenType;

        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( Exception e ) {
                return R;
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
                    System.out.println("Parse error trying to read double at processRotation, number expected, recieved \"" + parser.sval + "\".");
                    return null;
                }
                vals[i] = val;
                break;
              default:
                return R;
            }
            i++;
        } while ( tokenType != StreamTokenizer.TT_EOF && i <= 3 );

        R.axisRotation(vals[3], vals[0], vals[1], vals[2]);
        return R;
    }

    private static void processTransformGroup(
        StreamTokenizer parser,
        ArrayList<SimpleBody> simpleBodiesArray,
        ArrayList<Light> lightsArray,
        ArrayList<Background> backgroundsArray,
        ArrayList<Camera> camerasArray,
        Matrix4x4 M)
    {
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
                if ( parser.sval.equals("rotation") ) {
                    Matrix4x4 R;
                    R = processRotation(parser);
                    if ( R == null ) {
                        return;
                    }
                    M = M.multiply(R);
                }
                else if ( parser.sval.equals("children") ) {
                    processTransformationChildren(parser, simpleBodiesArray, lightsArray, backgroundsArray, camerasArray, M);
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
                return;
            }
        } while ( tokenType != StreamTokenizer.TT_EOF );
    }

    private static boolean readBoolean(StreamTokenizer parser)
    {
        int tokenType;
        boolean val = false;

        try {
            tokenType = parser.nextToken();
        }
        catch ( Exception e ) {
            return false;
        }
        switch ( tokenType ) {
          case StreamTokenizer.TT_EOL: break;
          case StreamTokenizer.TT_EOF: break;
          case StreamTokenizer.TT_WORD:
            if ( parser.sval.toLowerCase().equals("true") ) {
                val = true;
            }
            break;
          default:
            return false;
        }
        return val;
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

    private static TriangleMesh processIndexedFaceGroup(
        StreamTokenizer parser)
    {
        int tokenType;
        int level = 0;
        TriangleMesh mesh;

        mesh = new TriangleMesh();

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
                if ( parser.sval.equals("solid") ) {
                    // Value ignored!
                    readBoolean(parser);
                }
                if ( parser.sval.equals("colorPerVertex") ) {
                    // Value ignored!
                    readBoolean(parser);
                }
                else if ( parser.sval.equals("coord") ) {
                    if ( !readVertexPositions(parser, mesh) ) {
                        return null;
                    }
                }
                else if ( parser.sval.equals("color") ) {
                    if ( !readVertexColors(parser, mesh) ) {
                        return null;
                    }
                }
                else if ( parser.sval.equals("coordIndex") ) {
                    if ( !readPolygons(parser, mesh) ) {
                        return null;
                    }
                }
                else {
                    skipGroup(parser);
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
        return mesh;
    }

    private static Geometry processGeometryGroup(
        StreamTokenizer parser,
        ArrayList<SimpleBody> simpleBodiesArray,
        ArrayList<Light> lightsArray,
        ArrayList<Background> backgroundsArray,
        ArrayList<Camera> camerasArray,
        Matrix4x4 M)
    {
        int tokenType;
        int level = 0;
        Geometry g = null;

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
                if ( parser.sval.equals("IndexedFaceSet") ) {
                    g = processIndexedFaceGroup(parser);
                }
                else {
                    skipGroup(parser);
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
        return g;
    }

    private static void processShapeGroup(
        StreamTokenizer parser,
        ArrayList<SimpleBody> simpleBodiesArray,
        ArrayList<Light> lightsArray,
        ArrayList<Background> backgroundsArray,
        ArrayList<Camera> camerasArray,
        Matrix4x4 M)
    {
        int tokenType;
        int level = 0;
        SimpleBody thing;
        Matrix4x4 R = new Matrix4x4(M);
        Vector3D position = new Vector3D(M.M[0][3], M.M[1][3], M.M[2][3]);
        Geometry g = null;

        R.M[0][3] = 0.0;
        R.M[1][3] = 0.0;
        R.M[2][3] = 0.0;
        R.M[3][0] = 0.0;
        R.M[3][1] = 0.0;
        R.M[3][2] = 0.0;
        R.M[3][3] = 1.0;

        thing = new SimpleBody();
        thing.setPosition(position);
        thing.setRotation(R);
        thing.setRotationInverse(R.inverse());
        thing.setMaterial(defaultMaterial());

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
                if ( parser.sval.equals("geometry") ) {
                    g = processGeometryGroup(parser, simpleBodiesArray, lightsArray, backgroundsArray, camerasArray, M);
                }
                else {
                    processGroup(parser, simpleBodiesArray, lightsArray, backgroundsArray, camerasArray, M);
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
        if ( g != null ) {
            thing.setGeometry(g);
            simpleBodiesArray.add(thing);
        }
    }

    private static void processGroup(
        StreamTokenizer parser,
        ArrayList<SimpleBody> simpleBodiesArray,
        ArrayList<Light> lightsArray,
        ArrayList<Background> backgroundsArray,
        ArrayList<Camera> camerasArray,
        Matrix4x4 M)
    {
        if ( parser.sval.equals("WorldInfo") ) {
            skipGroup(parser);
        }
        else if ( parser.sval.equals("Transform") ) {
            processTransformGroup(parser, simpleBodiesArray, lightsArray, backgroundsArray, camerasArray, M);
        }
        else if ( parser.sval.equals("Shape") ) {
            processShapeGroup(parser, simpleBodiesArray, lightsArray, backgroundsArray, camerasArray, M);
        }
        else {
            skipGroup(parser);
        }
    }

    public static void
    importEnvironment(File inSceneFileFd, SimpleScene inoutSimpleScene)
        throws Exception
    {
        BufferedReader br;

        br = processHeader(inSceneFileFd);

        if ( br == null ) {
            return;
        }

        //-----------------------------------------------------------------
        ArrayList<SimpleBody> simpleBodiesArray;
        ArrayList<Light> lightsArray;
        ArrayList<Background> backgroundsArray;
        ArrayList<Camera> camerasArray;

        simpleBodiesArray = inoutSimpleScene.getSimpleBodies();
        lightsArray = inoutSimpleScene.getLights();
        backgroundsArray = inoutSimpleScene.getBackgrounds();
        camerasArray = inoutSimpleScene.getCameras();

        //-----------------------------------------------------------------
        StreamTokenizer parser = new StreamTokenizer(br);

        parser.resetSyntax();
        parser.eolIsSignificant(true);
        parser.slashSlashComments(false);
        parser.slashStarComments(false);
        parser.commentChar('#');
        parser.quoteChar('\"');
        parser.whitespaceChars(' ', ' ');
        parser.whitespaceChars(',', ',');
        parser.whitespaceChars('\t', '\t');
        parser.whitespaceChars('\n', '\n');
        parser.whitespaceChars('\r', '\r');
        parser.wordChars('A', 'Z');
        parser.wordChars('a', 'z');
        parser.wordChars('0', '9');
        parser.wordChars('&', '&');
        parser.wordChars('(', '(');
        parser.wordChars(')', ')');
        parser.wordChars('-', '-');
        parser.wordChars('_', '_');
        parser.wordChars('.', '.');
        parser.wordChars('/', '/');
        parser.wordChars('!', '!');
        parser.wordChars('$', '$');
        parser.wordChars('%', '%');
        parser.wordChars('\'', '\'');
        parser.wordChars('*', '*');
        parser.wordChars('+', '+');
        parser.wordChars(':', ':');
        parser.wordChars(';', ';');
        parser.wordChars('<', '<');
        parser.wordChars('=', '=');
        parser.wordChars('>', '>');
        parser.wordChars('?', '?');
        parser.wordChars('@', '@');
        parser.wordChars('|', '|');
        parser.wordChars('\\', '\\');
        parser.wordChars('~', '~');
        parser.wordChars(169, 169); // Copyright
        parser.wordChars('`', '`');
        // Note that following delimiters are outsiders: '{', '}', '[', ']'.

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
                Matrix4x4 M;

                M = new Matrix4x4();
                processGroup(parser, simpleBodiesArray, lightsArray, backgroundsArray, camerasArray, M);
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
                        if ( content == '{' ) {
                            //System.out.println("{ MARK");
                            level++;
                          }
                          else if ( content == '}' ) {
                            //System.out.println("} MARK");
                            level--;
                          } else {
                            // Nothing is done, as this is and unknown token,
                            // posibly corresponding to an empty token (i.e.
                            // a comment line with no real information)
                            //System.out.println("Other: " + content);
                        }
                    }
                }
                break;
            }
        } while ( tokenType != StreamTokenizer.TT_EOF );

/*
        System.out.print(inSceneFileFd.getAbsolutePath());

        if ( level == 0 ) { 
            System.out.println(" OKOK!");
        }
        else {
            System.out.println(" BADBAD: Final level " + level);
        }
        System.exit(0);
*/
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
