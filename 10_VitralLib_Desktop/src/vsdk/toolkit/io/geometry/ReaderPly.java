//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 27 2008 - Oscar Chavarro: Original base version                   =
//===========================================================================

package vsdk.toolkit.io.geometry;

// Java basic classes
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

// VSDK Classes
import vsdk.toolkit.common.ArrayListOfInts;
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.environment.Material;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.TriangleMesh;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.environment.scene.SimpleScene;
import vsdk.toolkit.io.PersistenceElement;

class _ReaderPlyElement extends PersistenceElement
{
    private static final int TYPE_SIGNED_CHARACTER = 1;
    private static final int TYPE_UNSIGNED_CHARACTER = 2;
    private static final int TYPE_SIGNED_SHORT_INTEGER = 3;
    private static final int TYPE_UNSIGNED_SHORT_INTEGER = 4;
    private static final int TYPE_SIGNED_INTEGER = 5;
    private static final int TYPE_UNSIGNED_INTEGER = 6;
    private static final int TYPE_FLOAT = 7;
    private static final int TYPE_DOUBLE = 8;
    private static final int TYPE_INVALID = 0;

    private String elementName;
    private int elementCount;
    private int elementType; // One of the "TYPE_*" constants
    private int colorType; // One of the "TYPE_*" constants
    private int listCountType; // One of the "TYPE_*" constants
    private int elementFamily;

    private TriangleMesh mesh;
    private int currentPropertyIndex;
    private int xindex;
    private int yindex;
    private int zindex;
    private int rindex;
    private int gindex;
    private int bindex;
    private int listindex;
    private ArrayListOfInts skipTypes;

    public _ReaderPlyElement(StringTokenizer headerLine, TriangleMesh mesh)
    {
        this.mesh = mesh;
        elementName = headerLine.nextToken();
        elementCount = Integer.parseInt(headerLine.nextToken());
        currentPropertyIndex = 0;
        xindex = -1;
        yindex = -1;
        zindex = -1;
        rindex = -1;
        gindex = -1;
        bindex = -1;
        listindex = -1;
        skipTypes = new ArrayListOfInts(10);
    }

    private int getType(String t)
    {
        if ( t.equals("float32") || t.equals("float") ) {
            return TYPE_FLOAT;
        }
        else if ( t.equals("uint8") || t.equals("uchar") ) {
            return TYPE_UNSIGNED_CHARACTER;
        }
        else if ( t.equals("int32") || t.equals("int") ) {
            return TYPE_SIGNED_INTEGER;
        }
        return TYPE_INVALID;
    }

    public boolean addProperty(StringTokenizer line)
    {
        String var;
        String type;
        int skiptype;

        type = line.nextToken();
        skiptype = getType(type);

        if ( elementName.equals("vertex") ) {
            var = line.nextToken();
            if ( var.equals("x") ) {
                elementType = skiptype;
                xindex = currentPropertyIndex;
            }
            else if ( var.equals("y") ) {
                elementType = skiptype;
                yindex = currentPropertyIndex;
            }
            else if ( var.equals("z") ) {
                elementType = skiptype;
                zindex = currentPropertyIndex;
            }
            if ( var.equals("r") || var.equals("red") ) {
                colorType = skiptype;
                rindex = currentPropertyIndex;
            }
            if ( var.equals("g") || var.equals("green") ) {
                colorType = skiptype;
                gindex = currentPropertyIndex;
            }
            if ( var.equals("b") || var.equals("blue") ) {
                colorType = skiptype;
                bindex = currentPropertyIndex;
            }
        }
        if ( elementName.equals("face") ) {
            if ( type.equals("list") ) {
                type = line.nextToken();
                listCountType = getType(type);
                type = line.nextToken();
                elementType = getType(type);
                listindex = currentPropertyIndex;
            }
        }
        skipTypes.add(skiptype);

        currentPropertyIndex++;
        return true;
    }

    /**
    This method is used to read data from file, while ignoring its unknown
    contents.
    */
    private void skipRead(_ReaderPlyElementReader reader, int type) throws Exception
    {
        switch ( type ) {
          case TYPE_SIGNED_CHARACTER:
            reader.readSignedCharacterText();
            break;
          case TYPE_UNSIGNED_CHARACTER:
            reader.readUnsignedCharacterText();
            break;
          case TYPE_SIGNED_SHORT_INTEGER:
            reader.readSignedShortIntegerText();
            break;
          case TYPE_UNSIGNED_SHORT_INTEGER:
            reader.readUnsignedShortIntegerText();
            break;
          case TYPE_SIGNED_INTEGER:
            reader.readSignedIntegerText();
            break;
          case TYPE_UNSIGNED_INTEGER:
            reader.readUnsignedIntegerText();
            break;
          case TYPE_FLOAT:
            reader.readFloatText();
            break;
          case TYPE_DOUBLE:
            reader.readDoubleText();
            break;
        }
    }

    private boolean readVertexData(_ReaderPlyElementReader reader, int i, double v[], double c[]) throws Exception
    {
        int j;
        double val;

        for ( j = 0; j < currentPropertyIndex; j++ ) {
            if ( j == xindex || j == yindex || j == zindex ) {
                if ( elementType == TYPE_FLOAT ) {
                    val = reader.readFloatText();
                    if ( j == xindex ) {
                        v[3*i+0] = val;
                    }
                    else if ( j == yindex ) {
                        v[3*i+1] = val;
                    }
                    else if ( j == zindex ) {
                        v[3*i+2] = val;
                    }
                }
                else {
                    VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readVertexData",
                    "Wrong element type!");
                    return false;
                }
            }
            else if ( j == rindex || j == gindex || j == bindex ) {
                if ( colorType == TYPE_FLOAT ) {
                    val = reader.readFloatText();
                    if ( j == rindex ) {
                        c[3*i+0] = val;
                    }
                    else if ( j == gindex ) {
                        c[3*i+1] = val;
                    }
                    else if ( j == bindex ) {
                        c[3*i+2] = val;
                    }
                }
                if ( colorType == TYPE_UNSIGNED_CHARACTER ) {
                    int cc = reader.readUnsignedCharacterText();
                    val = ((double)cc) / 255.0;
                    if ( j == rindex ) {
                        c[3*i+0] = val;
                    }
                    else if ( j == gindex ) {
                        c[3*i+1] = val;
                    }
                    else if ( j == bindex ) {
                        c[3*i+2] = val;
                    }
                }
                else {
                    VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readVertexData",
                    "Wrong element type!");
                    return false;
                }
            }
            else {
                skipRead(reader, skipTypes.get(j));
            }
        }
        return true;
    }

    private boolean readPolygonData(_ReaderPlyElementReader reader, int i, ArrayListOfInts triangles) throws Exception
    {
        int n;
        int j;
        int val;
        int p0 = 0;
        int p1 = 0;
        int p2;

        for ( j = 0; j < currentPropertyIndex; j++ ) {
            if ( j == listindex ) {
                if ( listCountType == TYPE_UNSIGNED_CHARACTER ) {
                    n = reader.readUnsignedCharacterText();
                }
                else {
                    VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readPolygonData",
                    "Wrong list count element type!");
                    return false;
                }
                for ( j = 0; j < n; j++ ) {
                    if ( elementType == TYPE_SIGNED_INTEGER ) {
                        val = reader.readSignedIntegerText();
    
                        if( j == 0 ) {
                            p0 = val;
                        }
                        else if( j == 1 ) {
                            p1 = val;
                        }
                        else {
                            p2 = val;
                            // Add a triangle over <p0, p1, p2>
                            triangles.add(p0);
                            triangles.add(p1);
                            triangles.add(p2);
                            //
                            p1 = val;
                        }
                    }
                    else {
                        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readPolygonData",  
                            "Wrong list element type!");
                        return false;
                    }
                }
            }
            else {
                skipRead(reader, skipTypes.get(j));
            }
        }
        return true;
    }

    public boolean processInput(_ReaderPlyElementReader reader) throws Exception
    {
        //-----------------------------------------------------------------
        int i;

        if ( elementName.equals("vertex") ) {
            mesh.initVertexPositionsArray(elementCount);
            double v[];
            v = mesh.getVertexPositions();
            double c[] = null;

            if ( rindex != -1 && gindex != -1 && bindex != -1 ) {
                mesh.initVertexColorsArray();
                c = mesh.getVertexColors();
            }
            else {
                rindex = -1;
                gindex = -1;
                bindex = -1;
            }

            for ( i = 0; i < elementCount; i++ ) {
                if ( !readVertexData(reader, i, v, c) ) {
                    return false;
                }
            }

        }
        //-----------------------------------------------------------------
        else if ( elementName.equals("face") ) {
            ArrayListOfInts triangles;
            triangles = new ArrayListOfInts(2000000);

            for ( i = 0; i < elementCount; i++ ) {
                if ( !readPolygonData(reader, i, triangles) ) {
                    return false;
                }
            }

            //-----------------------------------------------------------------
            int t[];

            mesh.initTriangleArrays(triangles.size()/3);
            t = mesh.getTriangleIndexes();

            for ( i = 0; i < triangles.size(); i++ ) {
                t[i] = triangles.get(i);
            }
            //triangles.array = null;
        }

        return true;
    }
}

abstract class _ReaderPlyElementReader extends PersistenceElement
{
    protected InputStream parentInputStream;
    public _ReaderPlyElementReader(InputStream is)
    {
        parentInputStream = is;
    }
    public abstract int readSignedCharacterText() throws Exception;
    public abstract int readUnsignedCharacterText() throws Exception;
    public abstract int readSignedShortIntegerText() throws Exception;
    public abstract int readUnsignedShortIntegerText() throws Exception;
    public abstract int readSignedIntegerText() throws Exception;
    public abstract int readUnsignedIntegerText() throws Exception;
    public abstract float readFloatText() throws Exception;
    public abstract float readDoubleText() throws Exception;
}

class _ReaderPlyElementReaderAscii extends _ReaderPlyElementReader
{
    private static String separators;

    public _ReaderPlyElementReaderAscii(InputStream is)
    {
        super(is);
        separators = " \t\n\r";
    }

    @Override
    public int readSignedCharacterText() throws Exception
    {
        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readSignedCharacter",
            "Operation not implemented!");
        return 0;
    }

    @Override
    public int readUnsignedCharacterText() throws Exception
    {
        String token;
        do {
            token = readAsciiToken(parentInputStream, separators.getBytes());
        } while( token == null || token.length() < 1 );
        return Integer.parseInt(token);
    }

    @Override
    public int readSignedShortIntegerText() throws Exception
    {
        String token;
        do {
            token = readAsciiToken(parentInputStream, separators.getBytes());
        } while( token == null || token.length() < 1 );
        return Integer.parseInt(token);
    }

    @Override
    public int readUnsignedShortIntegerText() throws Exception
    {
        String token;
        do {
            token = readAsciiToken(parentInputStream, separators.getBytes());
        } while( token == null || token.length() < 1 );
        return Integer.parseInt(token);
    }

    @Override
    public int readSignedIntegerText() throws Exception
    {
        String token;
        do {
            token = readAsciiToken(parentInputStream, separators.getBytes());
        } while( token == null || token.length() < 1 );
        return Integer.parseInt(token);
    }

    @Override
    public int readUnsignedIntegerText() throws Exception
    {
        String token;
        do {
            token = readAsciiToken(parentInputStream, separators.getBytes());
        } while( token == null || token.length() < 1 );
        return Integer.parseInt(token);
    }

    @Override
    public float readFloatText() throws Exception
    {
        String token;
        do {
            token = readAsciiToken(parentInputStream, separators.getBytes());
        } while( token == null || token.length() < 1 );
        return Float.parseFloat(token);
    }

    @Override
    public float readDoubleText() throws Exception
    {
        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readDouble",
            "Operation not implemented!");
        return 0;
    }
}

class _ReaderPlyElementReaderBinaryBigEndian extends _ReaderPlyElementReader
{
    public _ReaderPlyElementReaderBinaryBigEndian(InputStream is)
    {
        super(is);
    }

    @Override
    public int readSignedCharacterText() throws Exception
    {
        byte arr[] = new byte[1];
        readBytes(parentInputStream, arr);
        return (int)arr[0];
    }

    @Override
    public int readUnsignedCharacterText() throws Exception
    {
        byte arr[] = new byte[1];
        readBytes(parentInputStream, arr);
        return VSDK.signedByte2unsignedInteger(arr[0]);
    }

    @Override
    public int readSignedShortIntegerText() throws Exception
    {
        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readSignedShortInteger",
            "Operation not implemented!");
        return 0;
    }

    @Override
    public int readUnsignedShortIntegerText() throws Exception
    {
        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readUnsignedShortInteger",
            "Operation not implemented!");
        return 0;
    }

    @Override
    public int readSignedIntegerText() throws Exception
    {
        return (int)readLongBE(parentInputStream);
    }

    @Override
    public int readUnsignedIntegerText() throws Exception
    {
        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readUnsignedInteger",
            "Operation not implemented!");
        return 0;
    }

    @Override
    public float readFloatText() throws Exception
    {
        return readFloatBE(parentInputStream);
    }

    @Override
    public float readDoubleText() throws Exception
    {
        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readDouble",
            "Operation not implemented!");
        return 0;
    }
}

class _ReaderPlyElementReaderBinaryLittleEndian extends _ReaderPlyElementReader
{
    public _ReaderPlyElementReaderBinaryLittleEndian(InputStream is)
    {
        super(is);
    }

    @Override
    public int readSignedCharacterText() throws Exception
    {
        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readSignedCharacter",
            "Operation not implemented!");
        return 0;
    }

    @Override
    public int readUnsignedCharacterText() throws Exception
    {
        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readUnsignedCharacter",
            "Operation not implemented!");
        return 0;
    }

    @Override
    public int readSignedShortIntegerText() throws Exception
    {
        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readSignedShortInteger",
            "Operation not implemented!");
        return 0;
    }

    @Override
    public int readUnsignedShortIntegerText() throws Exception
    {
        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readUnsignedShortInteger",
            "Operation not implemented!");
        return 0;
    }

    @Override
    public int readSignedIntegerText() throws Exception
    {
        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readSignedInteger",
            "Operation not implemented!");
        return 0;
    }

    @Override
    public int readUnsignedIntegerText() throws Exception
    {
        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readUnsignedInteger",
            "Operation not implemented!");
        return 0;
    }

    @Override
    public float readFloatText() throws Exception
    {
        return readFloatLE(parentInputStream);
    }

    @Override
    public float readDoubleText() throws Exception
    {
        VSDK.reportMessage(this, VSDK.FATAL_ERROR, "readDouble",
            "Operation not implemented!");
        return 0;
    }
}

/**
Warning: this class is using operations which could be replaced with already
existing methods on PersistenceElement class.
 */
public class ReaderPly extends PersistenceElement
{
    private static _ReaderPlyElementReader elementReader = null;
    private static ArrayList<_ReaderPlyElement> elements = null;

    private static Material defaultMaterial()
    {
        Material m = new Material();

        m.setAmbient(new ColorRgb(0.2, 0.2, 0.2));
        m.setDiffuse(new ColorRgb(0.5, 0.9, 0.5));
        m.setSpecular(new ColorRgb(1, 1, 1));
        m.setDoubleSided(true);
        return m;
    }

    private static SimpleBody addThing(Geometry g,
        ArrayList<SimpleBody> inoutSimpleBodiesArray)
    {
        if ( inoutSimpleBodiesArray == null ) return null;

        SimpleBody thing;

        thing = new SimpleBody();
        thing.setGeometry(g);
        thing.setPosition(new Vector3D());
        thing.setRotation(new Matrix4x4());
        thing.setRotationInverse(new Matrix4x4());
        thing.setMaterial(defaultMaterial());
        inoutSimpleBodiesArray.add(thing);
        return thing;
    }

    private static boolean processHeader(InputStream is, TriangleMesh internalGeometry) throws Exception
    {
        String line, token;
        StringTokenizer auxStringTokenizer;

        //-----------------------------------------------------------------
        line = readAsciiLine(is).toLowerCase();

        if ( !line.equals("ply") ) {
            VSDK.reportMessage(null, VSDK.ERROR,
                "ReaderPly.processHeader",
                "Invalid PLY header: wrong magic line. Should be \"ply\".");
            return false;
        }

        //-----------------------------------------------------------------
        boolean headerDone = false;
        _ReaderPlyElement currentElement = null;

        elements = new ArrayList<_ReaderPlyElement>();

        do {
            line = readAsciiLine(is);

            auxStringTokenizer = new StringTokenizer(line, " \t");
            token = auxStringTokenizer.nextToken().toLowerCase();

            if ( token.equals("format") ) {
                token = auxStringTokenizer.nextToken().toLowerCase();
    
                if ( token.equals("ascii") ) {
                    elementReader = new _ReaderPlyElementReaderAscii(is);
                }
                else if ( token.equals("binary_big_endian") ) {
                    elementReader = new _ReaderPlyElementReaderBinaryBigEndian(is);
                }
                else if ( token.equals("binary_little_endian") ) {
                    elementReader = new _ReaderPlyElementReaderBinaryLittleEndian(is);
                }
                else {
                    VSDK.reportMessage(null, VSDK.ERROR,
                        "ReaderPly.processHeader",
                        "Invalid PLY header: unsupported PLY subformat \"" + token + "\".");
                    return false;
                }

                double version;
                version = Double.parseDouble(auxStringTokenizer.nextToken());
                if ( version > 1.0 + VSDK.EPSILON ) {
                    VSDK.reportMessage(null, VSDK.WARNING,
                        "ReaderPly.processHeader",
                                       "Untested PLY file version " + VSDK.formatDouble(version) + ", reading could fail.");
                }

            }
            else if ( token.equals("comment") || token.equals("obj_info") ) {
                // Skip line
            }
            else if ( token.equals("end_header") ) {
                headerDone = true;
            }
            else if ( token.equals("element") ) {
                currentElement = new _ReaderPlyElement(auxStringTokenizer, internalGeometry);
                elements.add(currentElement);
            }
            else if ( token.equals("property") ) {
                if ( currentElement != null ) {
                    if ( !currentElement.addProperty(auxStringTokenizer) ) {
                        return false;
                    }
                }
            }
            else {
                VSDK.reportMessage(null, VSDK.WARNING,
                    "ReaderPly.processHeader",
                    "Unknown header line \"" + token + "\", ignoring.");
            }
        } while( !headerDone );
        //-----------------------------------------------------------------
        return true;
    }

    public static int compareValue(double a, double b, double tolerance)
    {
        double delta;

        delta = Math.abs(a - b);
        if ( delta < tolerance ) {
            return 0;
        }
        else if ( a > b ) {
            return 1;
        }
        return -1;
    }

    public static void
    importEnvironment(File inSceneFileFd, SimpleScene inoutSimpleScene)
        throws Exception
    {
        //-----------------------------------------------------------------
        TriangleMesh internalGeometry;
        internalGeometry = new TriangleMesh();

        FileInputStream fis;
        BufferedInputStream bis;

        fis = new FileInputStream(inSceneFileFd);
        bis = new BufferedInputStream(fis);

        try {
            if ( !processHeader(bis, internalGeometry) ) {
                VSDK.reportMessage(null, VSDK.ERROR,
                    "ReaderPly.importEnvironment", "Invalid PLY header!");
            }
            int i;
            for ( i = 0; i < elements.size(); i++ ) {
                if ( !elements.get(i).processInput(elementReader) ) {
                    return;
                }
            }
        }
        catch ( Exception e ) {
            VSDK.reportMessage(null, VSDK.ERROR,
                               "ReaderPly.importEnvironment", "Error reading PLY data!" + e);
        }

        //-----------------------------------------------------------------
        bis.close();
        fis.close();

        //-----------------------------------------------------------------
        double c[];
        c = internalGeometry.getVertexColors();
        boolean allColorsAreTheSame = true;
        double r = 1.0, g = 1.0, b = 1.0;

        if ( c != null ) {
            int i;

            r = c[0];
            g = c[1];
            b = c[2];

            for ( i = 1; i < c.length/3; i++ ) {
                if ( compareValue(r, c[3*i+0], VSDK.EPSILON) != 0 ||
                     compareValue(g, c[3*i+1], VSDK.EPSILON) != 0 ||
                     compareValue(b, c[3*i+2], VSDK.EPSILON) != 0 ) {
                    allColorsAreTheSame = false;
                    break;
                }
            }
        }

        if ( allColorsAreTheSame ) {
            internalGeometry.detachColors();
        }
        c = internalGeometry.getVertexColors();

        //-----------------------------------------------------------------
        SimpleBody thing;

        if ( c == null ) {
            internalGeometry.calculateNormals();
        }
        thing = addThing(internalGeometry, inoutSimpleScene.getSimpleBodies());

        if ( allColorsAreTheSame && thing != null ) {
            thing.getMaterial().setDiffuse(new ColorRgb(r, g, b));
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
