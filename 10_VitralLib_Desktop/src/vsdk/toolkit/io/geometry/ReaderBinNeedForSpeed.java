//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - June 4 2008 - Oscar Chavarro: Original base version                   =
//===========================================================================

package vsdk.toolkit.io.geometry;

// Java basic classes
import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;

// VSDK Classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.environment.Background;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.environment.Material;
import vsdk.toolkit.environment.Light;
import vsdk.toolkit.environment.geometry.TriangleMesh;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.environment.scene.SimpleScene;
import vsdk.toolkit.media.Image;
import vsdk.toolkit.media.RGBAPixel;
import vsdk.toolkit.media.RGBAImage;
import vsdk.toolkit.io.PersistenceElement;

public class ReaderBinNeedForSpeed extends PersistenceElement
{
    private static final long MAGIC_ZEROES = 0x00000000;
    private static final long MAGIC_FILESTART = 0x00134002;
    private static final long MAGIC_OBJECTINIT = 0x00134011;
    private static final long MAGIC_OBJECTSTART = 0x0012F800;
    private static final long MAGIC_MESHTEXTURES = 0x00134012;
    private static final long MAGIC_MESHNUMS = 0x00134900;
    private static final long MAGIC_MESHVERTICES = 0x00134B01;
    private static final long MAGIC_MESHTRIANGLES = 0x00134B03;

    private static final long MAGIC_MESHBITMASKS = 0x00134B02;
    private static final long MAGIC_MESHUNKNOWN2 = 0x00134013;
    private static final long MAGIC_MESHUNKNOWN3 = 0x0013401A;
    private static final long MAGIC_MESHUNKNOWN4 = 0x00134017;
    private static final long MAGIC_MESHUNKNOWN5 = 0x00134018; // related to V
    private static final long MAGIC_MESHUNKNOWN6 = 0x00134019;
    private static final long MAGIC_MESHUNKNOWN7 = 0x00134000;
    private static final long MAGIC_MESHUNKNOWN8 = 0x00039202;

    private static final long MAGIC_TABLEUNKNOWN1 = 0x00134003;
    private static final long MAGIC_TABLEUNKNOWN2 = 0x00134004;

    private static long skippedKeys;

    private static ArrayList<RGBAImage> images = null;
    private static ArrayList<Long> ids = null;
    private static ArrayList<String> labels = null;

    public static void setTextures(ArrayList<RGBAImage> a, 
                                    ArrayList<Long> b,
                                    ArrayList<String> c)
    {
        images = a;
        ids = b;
        labels = c;
    }

    private static long readChunkStart(InputStream is, byte arr[]) throws Exception
    {
        long param = readLongLE(is); // Typically size in bytes of current chunk data

        do {
            readBytes(is, arr);
            skippedKeys++;
        } while ( isFill(arr) );

        return param;
    }

    private static boolean isFill(byte arr[])
    {
        int i;
        for ( i = 0; i < arr.length; i++ ) {
            if ( arr[i] != 0x11 ) {
                return false;
            }
        }
        return true;
    }

    private static Material defaultMaterial()
    {
        Material m = new Material();

        m.setAmbient(new ColorRgb(0.2, 0.2, 0.2));
        m.setDiffuse(new ColorRgb(0.9, 0.9, 0.9));
        m.setSpecular(new ColorRgb(1, 1, 1));
        m.setDoubleSided(true);
        return m;
    }

    private static void fillImageWithColor(RGBAImage img, RGBAPixel p)
    {
        int xxx, yyy;
        for ( xxx = 0; xxx < img.getXSize(); xxx++ ) {
            for ( yyy = 0; yyy < img.getYSize(); yyy++ ) {
                img.putPixel(xxx, yyy, p);
            }
        }
    }

    public static RGBAPixel classifyTextureColor(long id)
    {
        RGBAPixel p;
        byte maxp = (byte)0xff;
        byte minp = (byte)0x00;
        p = new RGBAPixel();

        if ( id == 0x5DD503ADL ) {
            p.r = minp; p.g = maxp; p.b = minp;
        }
        else if ( id == 0x96D34D66L ) {
            p.r = minp; p.g = minp; p.b = maxp;
        }
        else if ( id == 0x18E440E0L ) {
            p.r = maxp; p.g = maxp; p.b = minp;
        }
        else if ( id == 0x71ABC1B9L ) {
            p.r = minp; p.g = maxp; p.b = maxp;
        }
        else if ( id == 0xA2BF8E18L ) {
            // Car body color on PEAUGEOT
            p.r = maxp; p.g = maxp; p.b = minp;
        }
        // Known common texture identifiers from Need for Speed Underground:
        // Base elements
/*
        if (
            id == 0x12A4882BL || id == 0x1B0763A0L || 
            id == 0x21551B25L || id == 0x2433DF4EL || id == 0x29C86189L ||
            id == 0x3ED0EF41L || id == 0x3EDA2CA3L || id == 0x4C66A479L ||
            id == 0x4CDA56E4L || id == 0x4CDEBFCAL || 
            id == 0x5B979314L || id == 0x5E534BFAL || id == 0x60F8B13CL ||
            id == 0x67076CD2L || id == 0x6B79283AL || id == 0x7491F7BFL ||
            id == 0x77A20245L || id == 0x7B9B8F22L ||
            id == 0x7EDD4496L || id == 0x9509E20EL || id == 0x96B8F88BL ||
            id == 0x9A6D73B2L || id == 0x9B50E870L || id == 0x9F92A08CL ||
            id == 0xA43D9817L || id == 0xA6754656L ||
            id == 0xB3DC27ABL || id == 0xB87C200AL || id == 0xB8F3415CL ||
            id == 0xBA40B34DL || id == 0xBDD2BC4EL || id == 0xCABB1DB4L ||
            id == 0xCC5226DFL || id == 0xD195BE56L || id == 0xD689A31BL ||
            id == 0xE3F59A82L || id == 0xE490B3FCL || id == 0xE7E4EF49L ||
            id == 0xECBB8329L || id == 0xECBB832AL || id == 0xEFD842C5L ||
            id == 0xF95D432FL || id == 0xFC42D35CL || id == 0x7B220DDFL ||
            id == 0x027A43A7L || id == 0x06BCBD59L || id == 0x0AB88F5DL ||
            id == 0x0ECB3D67L || id == 0x11D467B1L || id == 0x12466978L ||
            id == 0x1C8BF43EL || id == 0x5799E60BL ) {    
            p.r = minp; p.g = maxp; p.b = minp;
        }
*/
        else {
            p.r = maxp; p.g = maxp; p.b = maxp;
        }

        p.a = maxp;
        return p;
    }

    private static SimpleBody
    readBody(InputStream is) throws Exception
    {
        byte arr4[] = new byte[4];
        long nt;
        long nv;
        long size;
        int i;

        //-----------------------------------------------------------------
        if ( !skipKeysUntil(is, MAGIC_OBJECTSTART) ||
             !skipKeysUntil(is, MAGIC_OBJECTSTART) ||
             !skipKeysUntil(is, MAGIC_ZEROES) ) {
            System.out.println("Bad body start! Aborting.");
            System.exit(0);
        }

        String name;
        name = readAsciiFixedSizeString(is, 28);

        //-----------------------------------------------------------------
        long key;

        key = readLongLE(is);

        if ( key != MAGIC_MESHTEXTURES ) {
            System.out.println("Bad name end! Aborting.");
            System.exit(0);
        }
        int ntextures;
        ntextures = (int)(readLongLE(is) / 8);

        boolean debug = true;

        if ( debug ) {
            System.err.println("Part " + name + " with " + ntextures + " textures");
        }
        long textureid;
        long skip;

        ArrayList<RGBAImage> localTextures = new ArrayList<RGBAImage>();

        for ( i = 0; i < ntextures; i++ ) {
            textureid = readLongLE(is);
            if ( debug ) {
                System.err.print("  - " + VSDK.formatIntAsHex((int)textureid));
            }
            skip = readLongLE(is);
            if ( skip != 0 ) {
                System.out.println("Texture chunk error!");
                System.exit(0);
            }
            RGBAImage img = null;
            if ( ids != null ) {
                for ( int j = 0; j < ids.size(); j++ ) {
                    long id = ids.get(j).longValue();
                    if ( id == textureid ) {
                        if ( debug ) {
                            System.err.println(" OK");
                        }
                        img = images.get(j);
                        j = ids.size() + 1;
                    }
                }
            }
            if ( img == null ) {
                img = new RGBAImage();
                img.init(64, 64);

                //img.createTestPattern();

                RGBAPixel p;
                p = classifyTextureColor(textureid);
                fillImageWithColor(img, p);

                if ( debug ) {
                    System.err.println(" NOT FOUND");
                }
            }
            localTextures.add(img);
        }

        Image[] textures = null;
        if ( localTextures.size() > 0 ) {
            textures = new Image[localTextures.size()];
            for ( i = 0; i < localTextures.size(); i++ ) {
                textures[i] = localTextures.get(i);
            }
        }

        //-----------------------------------------------------------------
        if ( !skipKeysUntil(is, MAGIC_MESHNUMS) ) {
            System.out.println("No mesh found! Aborting.");
            System.exit(0);
        }
        readChunkStart(is, arr4);
        skipKeys(is, 8); // ??

        nt = readLongLE(is);
        skipKeys(is, 3); // ??

        nv = readLongLE(is);
        skipKeys(is, 2); // ??

        if ( debug ) {
            //System.err.println(name + " (NT: " + nt + ", NV: " + nv + ")");
        }

        if ( !skipKeysUntil(is, MAGIC_MESHVERTICES) ) {
            System.out.println("No mesh found! Aborting.");
            System.exit(0);
        }

        //-----------------------------------------------------------------
        TriangleMesh mesh = new TriangleMesh();
        mesh.initVertexPositionsArray((int)nv);
        mesh.initVertexNormalsArray();
        mesh.initVertexUvsArray();
        double vp[];
        double vt[];
        double vn[];
        vp = mesh.getVertexPositions();
        vt = mesh.getVertexUvs();
        vn = mesh.getVertexNormals();

        if ( nv == 0 ) {
            return null;
        }

        readChunkStart(is, arr4);
        vp[0] = byteArray2floatLE(arr4, 0);

        /////
        long val;
        ArrayList<Long> vals = new ArrayList<Long>();
        /////

        for ( i = 0; i < nv; i++ ) {
            // x
            if ( i != 0 ) {
                vp[3*i+0] = readFloatLE(is);
            }
            vp[3*i+1] = readFloatLE(is); // y
            vp[3*i+2] = readFloatLE(is); // z
            vn[3*i+0] = readFloatLE(is); // nx
            vn[3*i+1] = readFloatLE(is); // ny
            vn[3*i+2] = readFloatLE(is); // nz
            val = readLongLE(is); // ??

            /////
            long a, b, c;
            a = (val >> 16) & 0x000000FF;
            b = (val >> 8) & 0x000000FF;
            c = (val) & 0x000000FF;

            //if ( a != b || a != c || b != c ) {
                int index;
                Long l = new Long(val);
                index = java.util.Collections.binarySearch(vals, l);
                if ( index < 0 ) {
                    vals.add((-index)-1, l);
                }
            //}
            /////

            vt[2*i+0] = readFloatLE(is); // u
            vt[2*i+1] = 1 - readFloatLE(is); // v
        }

        /////

        for ( i = 0; debug && i <  vals.size(); i++ ) {
            System.err.println("  - Part flags: " + VSDK.formatIntAsHex(vals.get(i).intValue()) + "\n");
        }

        /////

        //-----------------------------------------------------------------
        key = readLongLE(is);
        if ( key != MAGIC_MESHBITMASKS ) {
            System.out.println("Warning: missed binary stream! Check model" + name + "!");
            return null;
        }
        size = readChunkStart(is, arr4);
        //System.out.println(name + " SIZE: " + (size) + ", NTEXTURES " + numTextureSpans + ", RATIO: " + (((double)size)/60.0)/(double)numTextureSpans);

        long ntable = size/2;

        long a = 0;
        int j;

        for ( i = 0; i < ntextures; i++ ) {
            for ( j = 0; j < 15; j++ ) {
                if ( j== 0 && i == 0 ) {
                    a = byteArray2signedShortLE(arr4, 0);
                }
                else {
                    a = readLongLE(is);
                }
            }
        }

        //-----------------------------------------------------------------
        //key = readLongLE(is);
        //if ( key != MAGIC_MESHTRIANGLES ) {
        if ( !skipKeysUntil(is, MAGIC_MESHTRIANGLES) ) {
            System.out.println("No mesh found! Aborting.");
            System.exit(0);
        }

        //-----------------------------------------------------------------
        mesh.initTriangleArrays((int)nt);
        int t[];

        t = mesh.getTriangleIndexes();

        readChunkStart(is, arr4);
        t[0] = byteArray2signedShortLE(arr4, 0);
        t[1] = byteArray2signedShortLE(arr4, 2);

        for ( i = 0; i < nt; i++ ) {
            if ( i != 0 ) {
                t[3*i+0] = readSignedShortLE(is);
                t[3*i+1] = readSignedShortLE(is);
            }
            t[3*i+2] = readSignedShortLE(is);
        }

        if ( (nt % 2) != 0 ) {
            readSignedShortLE(is);
        }

        //-----------------------------------------------------------------
        if ( localTextures.size() > 0 ) {
            mesh.setTextures(textures);

            if ( name.contains("PEUGOT_KIT00_HEADLIGHT_A") ) {
                int ranges[][] = new int[3][2];
                ranges[1][0] = (int)nt - 142;
                ranges[1][1] = 1;
                ranges[0][0] = (int)nt - 284;
                ranges[0][1] = 2;
                ranges[2][0] = (int)nt;
                ranges[2][1] = 3;
                mesh.setTextureRanges(ranges);
            }
            else {
                int ranges[][] = new int[1][2];
                ranges[0][0] = (int)nt;
                ranges[0][1] = 1;
                mesh.setTextureRanges(ranges);
            }

            Material[] ma = new Material[1];
            ma[0] = defaultMaterial();
            int mranges[][] = new int[1][2];
            mranges[0][0] = (int)nt;
            mranges[0][1] = 0;
            mesh.setMaterials(ma);
            mesh.setMaterialRanges(mranges);
        }

        //mesh.calculateNormals();

        //-----------------------------------------------------------------
        boolean endIt = false;
        do {
            long surpriseMe = readLongLE(is);
            if ( surpriseMe == MAGIC_MESHUNKNOWN4 || 
                 surpriseMe == MAGIC_MESHUNKNOWN5 ||
                 surpriseMe == MAGIC_MESHUNKNOWN6 ) {
                skippedKeys = 0;
                size = readChunkStart(is, arr4);
                //System.out.println(name + ": SKIPPING CHUNK " + VSDK.formatIntAsHex((int)surpriseMe) + ", size " + (size/4 - skippedKeys));
                skipKeys(is, (int)(size/4)-1);
            }
            else {
                endIt = true;
            }
        } while ( !endIt );

        //-----------------------------------------------------------------
        SimpleBody thing;
        Vector3D position = new Vector3D();
        Matrix4x4 R = new Matrix4x4();
        Material material = defaultMaterial();

        thing = new SimpleBody();
        thing.setName(name);
        thing.setPosition(position);
        thing.setRotation(R);
        thing.setRotationInverse(R.inverse());
        thing.setMaterial(material);
        thing.setGeometry(mesh);

        //-----------------------------------------------------------------

        return thing;
    }

    private static boolean
    skipKeysUntil(InputStream is, long key) throws Exception
    {
        long i;

        do {
            try {
                i = readLongLE(is);
                skippedKeys++;
            }
            catch ( Exception e ) {
                return false;
            }
        } while ( i != key );
        return true;
    }

    private static void
    skipKeys(InputStream is, int n) throws Exception
    {
        int i;

        for ( i = 0; i < n; i++ ) {
            readLongLE(is);
        }
    }

    private static long processHeader(InputStream is) throws Exception
    {
        //-----------------------------------------------------------------
        long magic_chunk_id;
        long size;

        magic_chunk_id = readLongLE(is);
        size = readLongLE(is);

        //-----------------------------------------------------------------
        skipKeys(is, 4);

        long headerChunkKey = readLongLE(is);

        if ( headerChunkKey != MAGIC_FILESTART ) {
            System.out.println("Wrong header!");
            return 0;
        }

        long count = readLongLE(is);
        if ( count != 128 ) {
            System.out.println("Wrong header!");
            return 0;
        }

        skipKeys(is, 3);

        long n;
        n = readLongLE(is);

        //-----------------------------------------------------------------
        if ( !skipKeysUntil(is, MAGIC_TABLEUNKNOWN1) ) {
            System.out.println("Wrong header!");
            return 0;
        }
        long ntable = readLongLE(is)/8;

        if ( n != ntable ) {
            System.out.println("Wrong header!");
            return 0;
        }

        //System.out.println("Skipping unknown table with " + ntable + " elements.");
        int i;
        long a = 0;
        long b;

        for ( i = 0; i < ntable; i++ ) {
            a = readLongLE(is);
            b = readLongLE(is);
            //System.out.println(" - " + i + ": " + VSDK.formatIntAsHex((int)a));
            if ( b != 0 ) {
                System.out.println("Warning: wrong table entry!");
            }
        }

        //-----------------------------------------------------------------
        long key;

        key = readLongLE(is);
        if ( key != MAGIC_TABLEUNKNOWN2 ) {
            System.out.println("Wrong header!");
            return 0;
        }

        ntable = readLongLE(is)/8;
        //System.out.println("Skipping unknown table with " + ntable + " elements.");

        for ( i = 0; i < ntable; i++ ) {
            a = readLongLE(is);
            readLongLE(is);
        }

        //-----------------------------------------------------------------

        return n;
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

        System.out.println("Reading " + inSceneFileFd.getAbsolutePath());

        //-----------------------------------------------------------------
        FileInputStream fis = new FileInputStream(inSceneFileFd);
        BufferedInputStream bis = new BufferedInputStream(fis);

        //-----------------------------------------------------------------
        long n = processHeader(bis);
        long i;
        SimpleBody thing;
        boolean started = false;

        for ( i = 0; i < n; i++ ) {
            if ( started || skipKeysUntil(bis, MAGIC_OBJECTINIT) ) {
                thing = readBody(bis);
                if ( thing != null ) {
                    simpleBodiesArray.add(thing);
                }
                else {
                    System.out.println("Error!");
                    System.exit(0);                
                }
                started = false;
            }
        }

        //-----------------------------------------------------------------
        bis.close();
        fis.close();
        //System.exit(0);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
