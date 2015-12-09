//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - December 9 2006 - Oscar Chavarro: Original base version               =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [FERC1996] Fercoq, Robin. "3D Studio Material Library File Format",     =
//=     internet document posted at alt.3d and alt.3d-studio                =
//=     (usenet lists), revision 0.1, may 1996                              =
//= [PITT1994] Pitts, Jim. "3D Studio File Format", internet document       =
//=     posted at alt.3d and alt.3d-studio (usenet lists), december 1994    =
//= [VANV1997] van Velsen, Martin. Fercoq, Robin. Szilvasy, Albert.         =
//=     "3D Studio File Format (rewritten)", internet document posted at    =
//=     alt.3d and alt.3d-studio (usenet lists), revision 0.93, january     =
//=     1997.                                                               =
//===========================================================================

package vsdk.toolkit.io.geometry;

// Java basic classes
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

// VSDK Classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.Vertex;
import vsdk.toolkit.common.Triangle;
import vsdk.toolkit.environment.Background;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.environment.Material;
import vsdk.toolkit.environment.Light;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.TriangleMesh;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.environment.scene.SimpleScene;
import vsdk.toolkit.media.Image;
import vsdk.toolkit.media.RGBImage;
import vsdk.toolkit.io.PersistenceElement;
import vsdk.toolkit.io.image.ImagePersistence;

class _Reader3dsMaterialMapping extends PersistenceElement
{
    public String materialName;
    public String textureFilename;
    public int associatedTriangles[];
}

class _Reader3dsChunk extends PersistenceElement
{
    //=======================================================================
    public int id;
    public long length;

    //=======================================================================
    // Main level 3DS Fileformat chunk identifiers
    public static final int ID_MAIN                     = 0x4D4D;

    // Scene element 3DS Fileformat chunk identifiers
    public static final int ID_MESH                     = 0x3D3D;
    public static final int ID_BACKGROUND_COLOR         = 0x1200;
    public static final int ID_AMBIENT_COLOR            = 0x2100;
    public static final int ID_MATERIAL                 = 0xAFFF;
    public static final int ID_MATERIAL_NAME            = 0xA000;
    public static final int ID_MATERIAL_AMBIENT         = 0xA010;
    public static final int ID_MATERIAL_DIFFUSE         = 0xA020;
    public static final int ID_MATERIAL_SPECULAR        = 0xA030;
    public static final int ID_MATERIAL_SHININESS_EXP   = 0xA040;
    public static final int ID_MATERIAL_SHININESS_KTE   = 0xA041;
    public static final int ID_MATERIAL_TRANSPARENCY    = 0xA050;
    public static final int ID_MATERIAL_TRANSPARENCY_F  = 0xA052;
    public static final int ID_MATERIAL_TRANSPARENCYADD = 0xA083;
    public static final int ID_MATERIAL_FACEMAP         = 0xA088;
    public static final int ID_MATERIAL_TRANSPARENCY_FI = 0xA08A;
    public static final int ID_MATERIAL_WIRETHICKNESS   = 0xA08E;
    public static final int ID_MATERIAL_REFLECT_BLUR    = 0xA053;
    public static final int ID_MATERIAL_TWOSIDED        = 0xA081;
    public static final int ID_MATERIAL_WIREON          = 0xA085;
    public static final int ID_MATERIAL_WIRE_THICKNESS  = 0xA087;
    public static final int ID_MATERIAL_SOFTEN          = 0xA08C;
    public static final int ID_MATERIAL_TYPE            = 0xA100;
    public static final int ID_MATERIAL_TEXTURE1        = 0xA200;
    public static final int ID_MATERIAL_TEXTURE1MASK    = 0xA33E;
    public static final int ID_MATERIAL_TEXTURE2        = 0xA33A;
    public static final int ID_MATERIAL_TEXTURE2MASK    = 0xA340;
    public static final int ID_MATERIAL_MAPREFLECTION   = 0xA220;
    public static final int ID_MATERIAL_MAPREFLECTIONM  = 0xA34C;
    public static final int ID_MATERIAL_MAPSELFIL       = 0xA33D;
    public static final int ID_MATERIAL_MAPSELFILMASK   = 0xA34A;
    public static final int ID_MATERIAL_AUTOREFLECTION  = 0xA310;
    public static final int ID_MATERIAL_BUMPMAP         = 0xA230;
    public static final int ID_MATERIAL_BUMPMAPMASK     = 0xA344;
    public static final int ID_MATERIAL_MAPOPACITY      = 0xA210;
    public static final int ID_MATERIAL_MAPOPACITYMASK  = 0xA342;
    public static final int ID_MATERIAL_MAPSPECULAR     = 0xA204;
    public static final int ID_MATERIAL_MAPSPECULARMASK = 0xA348;
    public static final int ID_MATERIAL_MAPSHINE        = 0xA33C;
    public static final int ID_MATERIAL_MAPSHINEMASK    = 0xA346;
    public static final int ID_MATERIAL_MAPFILENAME     = 0xA300;
    public static final int ID_MATERIAL_MAPOPTIONS      = 0xA351;
    public static final int ID_MATERIAL_MAPFILTERBLUR   = 0xA353;
    public static final int ID_MATERIAL_MAP1USCALE      = 0xA354;
    public static final int ID_MATERIAL_MAP1VSCALE      = 0xA356;
    public static final int ID_MATERIAL_MAPUOFFSET      = 0xA358;
    public static final int ID_MATERIAL_MAPVOFFSET      = 0xA35A;
    public static final int ID_MATERIAL_MAPROTANGLE     = 0xA35C;
    public static final int ID_MATERIAL_MAPTINT1A       = 0xA360;
    public static final int ID_MATERIAL_MAPTINT2A       = 0xA362;
    public static final int ID_MATERIAL_MAPTINT1R       = 0xA364;
    public static final int ID_MATERIAL_MAPTINT1G       = 0xA366;
    public static final int ID_MATERIAL_MAPTINT1B       = 0xA368;
    public static final int ID_OBJECT_BLOCK             = 0x4000;
    public static final int ID_TRIANGLE_MESH            = 0x4100;
    public static final int ID_VERTEX_LIST              = 0x4110;
    public static final int ID_TRIANGLE_LIST            = 0x4120;
    public static final int ID_MATERIAL_MAPPING_TABLE   = 0x4130;
    public static final int ID_SMOOTH_LIST              = 0x4150;
    public static final int ID_MAP_LIST                 = 0x4140;
    public static final int ID_MATRIX                   = 0x4160;
    public static final int ID_TRI_MAPPING_STANDARD     = 0x4170;
    public static final int ID_OBJECT_IS_VISIBLE        = 0x4165;
    public static final int ID_TRI_VERTEX_OPTIONS       = 0x4111;
    public static final int ID_LIGHT                    = 0x4600;
    public static final int ID_SPOTLIGHT                = 0x4610;
    public static final int ID_LIT_OFF                  = 0x4620;
    public static final int ID_LIT_UNKNOWN_01           = 0x465A;
    public static final int ID_CAMERA                   = 0x4700;
    public static final int ID_OBJECT_UNKNOWN_01        = 0x4710;
    public static final int ID_OBJECT_UNKNOWN_02        = 0x4720;
    public static final int ID_EDIT_CONFIG1             = 0x0100;
    public static final int ID_EDIT_CONFIG2             = 0x3E3D;
    public static final int ID_EDIT_VIEW_P1             = 0x7012;
    public static final int ID_EDIT_VIEW_P2             = 0x7011;
    public static final int ID_EDIT_VIEW_P3             = 0x7020;
    public static final int ID_TOP                      = 0x0001;
    public static final int ID_BOTTOM                   = 0x0002;
    public static final int ID_LEFT                     = 0x0003;
    public static final int ID_RIGHT                    = 0x0004;
    public static final int ID_FRONT                    = 0x0005;
    public static final int ID_BACK                     = 0x0006;
    public static final int ID_USER                     = 0x0007;
    public static final int ID_CAMERA_VIEW              = 0xFFFF;
    public static final int ID_LIGHT_VIEW               = 0x0009;
    public static final int ID_COLOR_RGB1               = 0x0010;
    public static final int ID_COLOR_RGB2               = 0x0011;
    public static final int ID_COLOR_RGB3               = 0x0012;
    public static final int ID_AMOUNT                   = 0x0030;
    public static final int ID_VIEWPORT                 = 0x7001;
    public static final int ID_EDIT_UNKNOWN_01          = 0x1100;
    public static final int ID_EDIT_UNKNOWN_02          = 0x1201;
    public static final int ID_EDIT_UNKNOWN_03          = 0x1300;
    public static final int ID_EDIT_UNKNOWN_04          = 0x1400;
    public static final int ID_EDIT_UNKNOWN_05          = 0x1420;
    public static final int ID_EDIT_UNKNOWN_06          = 0x1450;
    public static final int ID_EDIT_UNKNOWN_07          = 0x1500;
    public static final int ID_EDIT_UNKNOWN_08          = 0x2200;
    public static final int ID_EDIT_UNKNOWN_09          = 0x2201;
    public static final int ID_EDIT_UNKNOWN_10          = 0x2210;
    public static final int ID_EDIT_UNKNOWN_11          = 0x2300;
    public static final int ID_EDIT_UNKNOWN_12          = 0x2302;
    public static final int ID_EDIT_UNKNOWN_13          = 0x2000;
    public static final int ID_EDIT_UNKNOWN_14          = 0x3000;

    // Scene control 3DS Fileformat chunk identifiers
    public static final int ID_KEYFRAMER                = 0xB000;
    public static final int ID_KEYFRAMER_FRAMES         = 0xB008;
    public static final int ID_KEYFRAMER_UNKNOWN_01     = 0xB00A;
    public static final int ID_KEYFRAMER_UNKNOWN_02     = 0xB009;
    public static final int ID_KEYFRAMER_OBJDES         = 0xB002;
    public static final int ID_KEYFRAMER_OBJHIERARCH    = 0xB010;
    public static final int ID_KEYFRAMER_OBJDUMMYNAME   = 0xB011;
    public static final int ID_KEYFRAMER_OBJUNKNOWN_01  = 0xB013;
    public static final int ID_KEYFRAMER_OBJUNKNOWN_02  = 0xB014;
    public static final int ID_KEYFRAMER_OBJUNKNOWN_03  = 0xB015;
    public static final int ID_KEYFRAMER_OBJPIVOT       = 0xB020;
    public static final int ID_KEYFRAMER_OBJUNKNOWN_04  = 0xB021;
    public static final int ID_KEYFRAMER_OBJUNKNOWN_05  = 0xB022;

    // Unknown ids
    public static final int ID_UNKNOWN_01               = 0x0000;
    public static final int ID_UNKNOWN_02               = 0x000E;
    public static final int ID_UNKNOWN_03               = 0x0031;
    public static final int ID_UNKNOWN_04               = 0x003C;
    public static final int ID_UNKNOWN_05               = 0x003F;
    public static final int ID_UNKNOWN_06               = 0x0040;
    public static final int ID_UNKNOWN_07               = 0x0068;
    public static final int ID_UNKNOWN_08               = 0x0074;
    public static final int ID_UNKNOWN_09               = 0x00BE;
    public static final int ID_UNKNOWN_10               = 0x00C0;
    public static final int ID_UNKNOWN_11               = 0x00D3;
    public static final int ID_UNKNOWN_12               = 0x0182;
    public static final int ID_UNKNOWN_13               = 0x0402;
    public static final int ID_UNKNOWN_14               = 0x042C;
    public static final int ID_UNKNOWN_15               = 0x0500;
    public static final int ID_UNKNOWN_16               = 0x0800;
    public static final int ID_UNKNOWN_17               = 0x0A46;
    public static final int ID_UNKNOWN_18               = 0x0B00;
    public static final int ID_UNKNOWN_19               = 0x0E70;
    public static final int ID_UNKNOWN_20               = 0x1101;
    public static final int ID_UNKNOWN_21               = 0x1301;
    public static final int ID_UNKNOWN_22               = 0x1410;
    public static final int ID_UNKNOWN_23               = 0x1430;
    public static final int ID_UNKNOWN_24               = 0x1440;
    public static final int ID_UNKNOWN_25               = 0x1460;
    public static final int ID_UNKNOWN_26               = 0x1470;
    public static final int ID_UNKNOWN_27               = 0x1734;
    public static final int ID_UNKNOWN_28               = 0x2301;
    public static final int ID_UNKNOWN_29               = 0x2303;
    public static final int ID_UNKNOWN_30               = 0x3238;
    public static final int ID_UNKNOWN_31               = 0x334B;
    public static final int ID_UNKNOWN_32               = 0x3D3E;
    public static final int ID_UNKNOWN_33               = 0x3ECC;
    public static final int ID_UNKNOWN_34               = 0x3F19;
    public static final int ID_UNKNOWN_35               = 0x3F80;
    public static final int ID_UNKNOWN_36               = 0x4010;
    public static final int ID_UNKNOWN_37               = 0x4011;
    public static final int ID_UNKNOWN_38               = 0x4012;
    public static final int ID_UNKNOWN_39               = 0x4014;
    public static final int ID_UNKNOWN_40               = 0x4015;
    public static final int ID_UNKNOWN_41               = 0x4016;
    public static final int ID_UNKNOWN_42               = 0x4017;
    public static final int ID_UNKNOWN_43               = 0x4181;
    public static final int ID_UNKNOWN_44               = 0x4182;
    public static final int ID_UNKNOWN_45               = 0x4190;
    public static final int ID_UNKNOWN_46               = 0x41FA;
    public static final int ID_UNKNOWN_47               = 0x434E;
    public static final int ID_UNKNOWN_48               = 0x4650;
    public static final int ID_UNKNOWN_49               = 0x5DDC;
    public static final int ID_UNKNOWN_50               = 0x6769;
    public static final int ID_UNKNOWN_51               = 0x8000;
    public static final int ID_UNKNOWN_52               = 0xA080;
    public static final int ID_UNKNOWN_53               = 0xA082;
    public static final int ID_UNKNOWN_54               = 0xA084;
    public static final int ID_UNKNOWN_55               = 0xA240;
    public static final int ID_UNKNOWN_56               = 0xA250;
    public static final int ID_UNKNOWN_57               = 0xA320;
    public static final int ID_UNKNOWN_58               = 0xA321;
    public static final int ID_UNKNOWN_59               = 0xA322;
    public static final int ID_UNKNOWN_60               = 0xA324;
    public static final int ID_UNKNOWN_61               = 0xA325;
    public static final int ID_UNKNOWN_62               = 0xA326;
    public static final int ID_UNKNOWN_63               = 0xA328;
    public static final int ID_UNKNOWN_64               = 0xA32A;
    public static final int ID_UNKNOWN_65               = 0xA32C;
    public static final int ID_UNKNOWN_66               = 0xA32D;
    public static final int ID_UNKNOWN_67               = 0xAE6C;
    public static final int ID_UNKNOWN_68               = 0xC82A;
    public static final int ID_UNKNOWN_69               = 0xD567;
    public static final int ID_UNKNOWN_70               = 0xDE49;
    public static final int ID_UNKNOWN_71               = 0xE759;
    public static final int ID_UNKNOWN_72               = 0xEC04;

    //=======================================================================
    public static String
    chunkToString(int chunkid)
    {
        String chunkname;
        
        switch ( chunkid ) {
          case ID_MAIN:
            chunkname = "ID_MAIN";
            break;
          case ID_MESH:
            chunkname = "ID_MESH";
            break;
          case ID_BACKGROUND_COLOR:
            chunkname = "ID_BACKGROUND_COLOR";
            break;
          case ID_AMBIENT_COLOR:
            chunkname = "ID_AMBIENT_COLOR";
            break;
          case ID_MATERIAL:
            chunkname = "ID_MATERIAL";
            break;
          case ID_MATERIAL_NAME:
            chunkname = "ID_MATERIAL_NAME";
            break;
          case ID_MATERIAL_AMBIENT:
            chunkname = "ID_MATERIAL_AMBIENT";
            break;
          case ID_MATERIAL_DIFFUSE:
            chunkname = "ID_MATERIAL_DIFFUSE";
            break;
          case ID_MATERIAL_SPECULAR:
            chunkname = "ID_MATERIAL_SPECULAR";
            break;
          case ID_MATERIAL_SHININESS_EXP:
            chunkname = "ID_MATERIAL_SHININESS_EXP";
            break;
          case ID_MATERIAL_SHININESS_KTE:
            chunkname = "ID_MATERIAL_SHININESS_KTE";
            break;
          case ID_MATERIAL_TRANSPARENCY:
            chunkname = "ID_MATERIAL_TRANSPARENCY";
            break;
          case ID_MATERIAL_TRANSPARENCYADD:
            chunkname = "ID_MATERIAL_TRANSPARENCYADD";
            break;
          case ID_MATERIAL_TRANSPARENCY_F:
            chunkname = "ID_MATERIAL_TRANSPARENCY_F";
            break;
          case ID_MATERIAL_TRANSPARENCY_FI:
            chunkname = "ID_MATERIAL_TRANSPARENCY_FI";
            break;
          case ID_MATERIAL_REFLECT_BLUR:
            chunkname = "ID_MATERIAL_REFLECT_BLUR";
            break;
          case ID_MATERIAL_WIRETHICKNESS:
            chunkname = "ID_MATERIAL_WIRETHICKNESS";
            break;
          case ID_MATERIAL_FACEMAP:
            chunkname = "ID_MATERIAL_FACEMAP";
            break;
          case ID_MATERIAL_TWOSIDED:
            chunkname = "ID_MATERIAL_TWOSIDED";
            break;
          case ID_MATERIAL_WIREON:
            chunkname = "ID_MATERIAL_WIREON";
            break;
          case ID_MATERIAL_WIRE_THICKNESS:
            chunkname = "ID_MATERIAL_WIRE_THICKNESS";
            break;
          case ID_MATERIAL_SOFTEN:
            chunkname = "ID_MATERIAL_SOFTEN";
            break;
          case ID_MATERIAL_TYPE:
            chunkname = "ID_MATERIAL_TYPE";
            break;
          case ID_MATERIAL_TEXTURE1:
            chunkname = "ID_MATERIAL_TEXTURE1";
            break;
          case ID_MATERIAL_TEXTURE1MASK:
            chunkname = "ID_MATERIAL_TEXTURE1MASK";
            break;
          case ID_MATERIAL_TEXTURE2:
            chunkname = "ID_MATERIAL_TEXTURE2";
            break;
          case ID_MATERIAL_TEXTURE2MASK:
            chunkname = "ID_MATERIAL_TEXTURE2MASK";
            break;
          case ID_MATERIAL_MAPREFLECTION:
            chunkname = "ID_MATERIAL_MAPREFLECTION";
            break;
          case ID_MATERIAL_MAPREFLECTIONM:
            chunkname = "ID_MATERIAL_MAPREFLECTIONM";
            break;
          case ID_MATERIAL_MAPSELFIL:
            chunkname = "ID_MATERIAL_MAPSELFIL";
            break;
          case ID_MATERIAL_MAPSELFILMASK:
            chunkname = "ID_MATERIAL_MAPSELFILMASK";
            break;
          case ID_MATERIAL_MAPFILENAME:
            chunkname = "ID_MATERIAL_MAPFILENAME";
            break;
          case ID_MATERIAL_MAPOPTIONS:
            chunkname = "ID_MATERIAL_MAPOPTIONS";
            break;
          case ID_MATERIAL_MAPFILTERBLUR:
            chunkname = "ID_MATERIAL_MAPFILTERBLUR";
            break;
          case ID_MATERIAL_MAPUOFFSET:
            chunkname = "ID_MATERIAL_MAPUOFFSET";
            break;
          case ID_MATERIAL_MAPVOFFSET:
            chunkname = "ID_MATERIAL_MAPVOFFSET";
            break;
          case ID_MATERIAL_MAP1USCALE:
            chunkname = "ID_MATERIAL_MAP1USCALE";
            break;
          case ID_MATERIAL_MAP1VSCALE:
            chunkname = "ID_MATERIAL_MAP1VSCALE";
            break;
          case ID_MATERIAL_MAPROTANGLE:
            chunkname = "ID_MATERIAL_MAPROTANGLE";
            break;
          case ID_MATERIAL_MAPTINT1A:
            chunkname = "ID_MATERIAL_MAPTINT1A";
            break;
          case ID_MATERIAL_MAPTINT2A:
            chunkname = "ID_MATERIAL_MAPTINT2A";
            break;
          case ID_MATERIAL_MAPTINT1R:
            chunkname = "ID_MATERIAL_MAPTINT1R";
            break;
          case ID_MATERIAL_MAPTINT1G:
            chunkname = "ID_MATERIAL_MAPTINT1G";
            break;
          case ID_MATERIAL_MAPTINT1B:
            chunkname = "ID_MATERIAL_MAPTINT1B";
            break;
          case ID_OBJECT_BLOCK:
            chunkname = "ID_OBJECT_BLOCK";
            break;
          case ID_TRIANGLE_MESH:
            chunkname = "ID_TRIANGLE_MESH";
            break;
          case ID_VERTEX_LIST:
            chunkname = "ID_VERTEX_LIST";
            break;
          case ID_TRIANGLE_LIST:
            chunkname = "ID_TRIANGLE_LIST";
            break;
          case ID_MATERIAL_MAPPING_TABLE:
            chunkname = "ID_MATERIAL_MAPPING_TABLE";
            break;
          case ID_SMOOTH_LIST:
            chunkname = "ID_SMOOTH_LIST";
            break;
          case ID_MAP_LIST:
            chunkname = "ID_MAP_LIST";
            break;
          case ID_MATRIX:
            chunkname = "ID_MATRIX";
            break;
          case ID_TRI_MAPPING_STANDARD:
            chunkname = "ID_TRI_MAPPING_STANDARD";
            break;
          case ID_OBJECT_IS_VISIBLE:
            chunkname = "ID_OBJECT_IS_VISIBLE";
            break;
          case ID_TRI_VERTEX_OPTIONS:
            chunkname = "ID_TRI_VERTEX_OPTIONS";
            break;
          case ID_LIGHT:
            chunkname = "ID_LIGHT";
            break;
          case ID_SPOTLIGHT:
            chunkname = "ID_SPOTLIGHT";
            break;
          case ID_LIT_OFF:
            chunkname = "ID_LIT_OFF";
            break;
          case ID_LIT_UNKNOWN_01:
            chunkname = "ID_LIT_UNKNOWN_01";
            break;
          case ID_CAMERA:
            chunkname = "ID_CAMERA";
            break;
          case ID_OBJECT_UNKNOWN_01:
            chunkname = "ID_OBJECT_UNKNOWN_01";
            break;
          case ID_OBJECT_UNKNOWN_02:
            chunkname = "ID_OBJECT_UNKNOWN_02";
            break;
          case ID_EDIT_CONFIG1:
            chunkname = "ID_EDIT_CONFIG1";
            break;
          case ID_EDIT_CONFIG2:
            chunkname = "ID_EDIT_CONFIG2";
            break;
          case ID_EDIT_VIEW_P1:
            chunkname = "ID_EDIT_VIEW_P1";
            break;
          case ID_EDIT_VIEW_P2:
            chunkname = "ID_EDIT_VIEW_P2";
            break;
          case ID_EDIT_VIEW_P3:
            chunkname = "ID_EDIT_VIEW_P3";
            break;
          case ID_TOP:
            chunkname = "ID_TOP";
            break;
          case ID_BOTTOM:
            chunkname = "ID_BOTTOM";
            break;
          case ID_LEFT:
            chunkname = "ID_LEFT";
            break;
          case ID_RIGHT:
            chunkname = "ID_RIGHT";
            break;
          case ID_FRONT:
            chunkname = "ID_FRONT";
            break;
          case ID_BACK:
            chunkname = "ID_BACK";
            break;
          case ID_USER:
            chunkname = "ID_USER";
            break;
          case ID_CAMERA_VIEW:
            chunkname = "ID_CAMERA_VIEW";
            break;
          case ID_LIGHT_VIEW:
            chunkname = "ID_LIGHT_VIEW";
            break;
          case ID_COLOR_RGB1:
            chunkname = "ID_COLOR_RGB1";
            break;
          case ID_COLOR_RGB2:
            chunkname = "ID_COLOR_RGB2";
            break;
          case ID_COLOR_RGB3:
            chunkname = "ID_COLOR_RGB3";
            break;
          case ID_AMOUNT:
            chunkname = "ID_AMOUNT";
            break;
          case ID_VIEWPORT:
            chunkname = "ID_VIEWPORT";
            break;
          case ID_EDIT_UNKNOWN_01:
            chunkname = "ID_EDIT_UNKNOWN_01";
            break;
          case ID_EDIT_UNKNOWN_02:
            chunkname = "ID_EDIT_UNKNOWN_02";
            break;
          case ID_EDIT_UNKNOWN_03:
            chunkname = "ID_EDIT_UNKNOWN_03";
            break;
          case ID_EDIT_UNKNOWN_04:
            chunkname = "ID_EDIT_UNKNOWN_04";
            break;
          case ID_EDIT_UNKNOWN_05:
            chunkname = "ID_EDIT_UNKNOWN_05";
            break;
          case ID_EDIT_UNKNOWN_06:
            chunkname = "ID_EDIT_UNKNOWN_06";
            break;
          case ID_EDIT_UNKNOWN_07:
            chunkname = "ID_EDIT_UNKNOWN_07";
            break;
          case ID_EDIT_UNKNOWN_08:
            chunkname = "ID_EDIT_UNKNOWN_08";
            break;
          case ID_EDIT_UNKNOWN_09:
            chunkname = "ID_EDIT_UNKNOWN_09";
            break;
          case ID_EDIT_UNKNOWN_10:
            chunkname = "ID_EDIT_UNKNOWN_10";
            break;
          case ID_EDIT_UNKNOWN_11:
            chunkname = "ID_EDIT_UNKNOWN_11";
            break;
          case ID_EDIT_UNKNOWN_12:
            chunkname = "ID_EDIT_UNKNOWN_12";
            break;
          case ID_EDIT_UNKNOWN_13:
            chunkname = "ID_EDIT_UNKNOWN_13";
            break;
          case ID_EDIT_UNKNOWN_14:
            chunkname = "ID_EDIT_UNKNOWN_14";
            break;
          case ID_KEYFRAMER:
            chunkname = "ID_KEYFRAMER";
            break;
          case ID_KEYFRAMER_FRAMES:
            chunkname = "ID_KEYFRAMER_FRAMES";
            break;
          case ID_KEYFRAMER_UNKNOWN_01:
            chunkname = "ID_KEYFRAMER_UNKNOWN_01";
            break;
          case ID_KEYFRAMER_UNKNOWN_02:
            chunkname = "ID_KEYFRAMER_UNKNOWN_02";
            break;
          case ID_KEYFRAMER_OBJDES:
            chunkname = "ID_KEYFRAMER_OBJDES";
            break;
          case ID_KEYFRAMER_OBJHIERARCH:
            chunkname = "ID_KEYFRAMER_OBJHIERARCH";
            break;
          case ID_KEYFRAMER_OBJDUMMYNAME:
            chunkname = "ID_KEYFRAMER_OBJDUMMYNAME";
            break;
          case ID_KEYFRAMER_OBJUNKNOWN_01:
            chunkname = "ID_KEYFRAMER_OBJUNKNOWN_01";
            break;
          case ID_KEYFRAMER_OBJUNKNOWN_02:
            chunkname = "ID_KEYFRAMER_OBJUNKNOWN_02";
            break;
          case ID_KEYFRAMER_OBJUNKNOWN_03:
            chunkname = "ID_KEYFRAMER_OBJUNKNOWN_03";
            break;
          case ID_KEYFRAMER_OBJPIVOT:
            chunkname = "ID_KEYFRAMER_OBJPIVOT";
            break;
          case ID_KEYFRAMER_OBJUNKNOWN_04:
            chunkname = "ID_KEYFRAMER_OBJUNKNOWN_04";
            break;
          case ID_KEYFRAMER_OBJUNKNOWN_05:
            chunkname = "ID_KEYFRAMER_OBJUNKNOWN_05";
            break;
          case ID_MATERIAL_MAPSHINE:    chunkname = "ID_MATERIAL_MAPSHINE";    break;
          case ID_MATERIAL_MAPSHINEMASK:
            chunkname = "ID_MATERIAL_MAPSHINEMASK";
            break;
          case ID_MATERIAL_AUTOREFLECTION:
            chunkname = "ID_MATERIAL_AUTOREFLECTION";
            break;
          case ID_MATERIAL_BUMPMAP:
            chunkname = "ID_MATERIAL_BUMPMAP";
            break;
          case ID_MATERIAL_BUMPMAPMASK:
            chunkname = "ID_MATERIAL_BUMPMAPMASK";
            break;
          case ID_MATERIAL_MAPOPACITY:
            chunkname = "ID_MATERIAL_MAPOPACITY";
            break;
          case ID_MATERIAL_MAPOPACITYMASK:
            chunkname = "ID_MATERIAL_MAPOPACITYMASK";
            break;
          case ID_MATERIAL_MAPSPECULAR:
            chunkname = "ID_MATERIAL_MAPSPECULAR";
            break;
          case ID_MATERIAL_MAPSPECULARMASK:
            chunkname = "ID_MATERIAL_MAPSPECULARMASK";
            break;
          case ID_UNKNOWN_01:    chunkname = "ID_UNKNOWN_01";    break;
          case ID_UNKNOWN_02:    chunkname = "ID_UNKNOWN_02";    break;
          case ID_UNKNOWN_03:    chunkname = "ID_UNKNOWN_03";    break;
          case ID_UNKNOWN_04:    chunkname = "ID_UNKNOWN_04";    break;
          case ID_UNKNOWN_05:    chunkname = "ID_UNKNOWN_05";    break;
          case ID_UNKNOWN_06:    chunkname = "ID_UNKNOWN_06";    break;
          case ID_UNKNOWN_07:    chunkname = "ID_UNKNOWN_07";    break;
          case ID_UNKNOWN_08:    chunkname = "ID_UNKNOWN_08";    break;
          case ID_UNKNOWN_09:    chunkname = "ID_UNKNOWN_09";    break;
          case ID_UNKNOWN_10:    chunkname = "ID_UNKNOWN_10";    break;
          case ID_UNKNOWN_11:    chunkname = "ID_UNKNOWN_11";    break;
          case ID_UNKNOWN_12:    chunkname = "ID_UNKNOWN_12";    break;
          case ID_UNKNOWN_13:    chunkname = "ID_UNKNOWN_13";    break;
          case ID_UNKNOWN_14:    chunkname = "ID_UNKNOWN_14";    break;
          case ID_UNKNOWN_15:    chunkname = "ID_UNKNOWN_15";    break;
          case ID_UNKNOWN_16:    chunkname = "ID_UNKNOWN_16";    break;
          case ID_UNKNOWN_17:    chunkname = "ID_UNKNOWN_17";    break;
          case ID_UNKNOWN_18:    chunkname = "ID_UNKNOWN_18";    break;
          case ID_UNKNOWN_19:    chunkname = "ID_UNKNOWN_19";    break;
          case ID_UNKNOWN_20:    chunkname = "ID_UNKNOWN_20";    break;
          case ID_UNKNOWN_21:    chunkname = "ID_UNKNOWN_21";    break;
          case ID_UNKNOWN_22:    chunkname = "ID_UNKNOWN_22";    break;
          case ID_UNKNOWN_23:    chunkname = "ID_UNKNOWN_23";    break;
          case ID_UNKNOWN_24:    chunkname = "ID_UNKNOWN_24";    break;
          case ID_UNKNOWN_25:    chunkname = "ID_UNKNOWN_25";    break;
          case ID_UNKNOWN_26:    chunkname = "ID_UNKNOWN_26";    break;
          case ID_UNKNOWN_27:    chunkname = "ID_UNKNOWN_27";    break;
          case ID_UNKNOWN_28:    chunkname = "ID_UNKNOWN_28";    break;
          case ID_UNKNOWN_29:    chunkname = "ID_UNKNOWN_29";    break;
          case ID_UNKNOWN_30:    chunkname = "ID_UNKNOWN_30";    break;
          case ID_UNKNOWN_31:    chunkname = "ID_UNKNOWN_31";    break;
          case ID_UNKNOWN_32:    chunkname = "ID_UNKNOWN_32";    break;
          case ID_UNKNOWN_33:    chunkname = "ID_UNKNOWN_33";    break;
          case ID_UNKNOWN_34:    chunkname = "ID_UNKNOWN_34";    break;
          case ID_UNKNOWN_35:    chunkname = "ID_UNKNOWN_35";    break;
          case ID_UNKNOWN_36:    chunkname = "ID_UNKNOWN_36";    break;
          case ID_UNKNOWN_37:    chunkname = "ID_UNKNOWN_37";    break;
          case ID_UNKNOWN_38:    chunkname = "ID_UNKNOWN_38";    break;
          case ID_UNKNOWN_39:    chunkname = "ID_UNKNOWN_39";    break;
          case ID_UNKNOWN_40:    chunkname = "ID_UNKNOWN_40";    break;
          case ID_UNKNOWN_41:    chunkname = "ID_UNKNOWN_41";    break;
          case ID_UNKNOWN_42:    chunkname = "ID_UNKNOWN_42";    break;
          case ID_UNKNOWN_43:    chunkname = "ID_UNKNOWN_43";    break;
          case ID_UNKNOWN_44:    chunkname = "ID_UNKNOWN_44";    break;
          case ID_UNKNOWN_45:    chunkname = "ID_UNKNOWN_45";    break;
          case ID_UNKNOWN_46:    chunkname = "ID_UNKNOWN_46";    break;
          case ID_UNKNOWN_47:    chunkname = "ID_UNKNOWN_47";    break;
          case ID_UNKNOWN_48:    chunkname = "ID_UNKNOWN_48";    break;
          case ID_UNKNOWN_49:    chunkname = "ID_UNKNOWN_49";    break;
          case ID_UNKNOWN_50:    chunkname = "ID_UNKNOWN_50";    break;
          case ID_UNKNOWN_51:    chunkname = "ID_UNKNOWN_51";    break;
          case ID_UNKNOWN_52:    chunkname = "ID_UNKNOWN_52";    break;
          case ID_UNKNOWN_53:    chunkname = "ID_UNKNOWN_53";    break;
          case ID_UNKNOWN_54:    chunkname = "ID_UNKNOWN_54";    break;
          case ID_UNKNOWN_55:    chunkname = "ID_UNKNOWN_55";    break;
          case ID_UNKNOWN_56:    chunkname = "ID_UNKNOWN_56";    break;
          case ID_UNKNOWN_57:    chunkname = "ID_UNKNOWN_57";    break;
          case ID_UNKNOWN_58:    chunkname = "ID_UNKNOWN_58";    break;
          case ID_UNKNOWN_59:    chunkname = "ID_UNKNOWN_59";    break;
          case ID_UNKNOWN_60:    chunkname = "ID_UNKNOWN_60";    break;
          case ID_UNKNOWN_61:    chunkname = "ID_UNKNOWN_61";    break;
          case ID_UNKNOWN_62:    chunkname = "ID_UNKNOWN_62";    break;
          case ID_UNKNOWN_63:    chunkname = "ID_UNKNOWN_63";    break;
          case ID_UNKNOWN_64:    chunkname = "ID_UNKNOWN_64";    break;
          case ID_UNKNOWN_65:    chunkname = "ID_UNKNOWN_65";    break;
          case ID_UNKNOWN_66:    chunkname = "ID_UNKNOWN_66";    break;
          case ID_UNKNOWN_67:    chunkname = "ID_UNKNOWN_67";    break;
          case ID_UNKNOWN_68:    chunkname = "ID_UNKNOWN_68";    break;
          case ID_UNKNOWN_69:    chunkname = "ID_UNKNOWN_69";    break;
          case ID_UNKNOWN_70:    chunkname = "ID_UNKNOWN_70";    break;
          case ID_UNKNOWN_71:    chunkname = "ID_UNKNOWN_71";    break;
          case ID_UNKNOWN_72:    chunkname = "ID_UNKNOWN_72";    break;
          default:
            byte a, b;
            a = (byte)((chunkid & 0xFF00) >> 8);
            b = (byte)((chunkid & 0x00FF));
            chunkname = "<Unknown id 0x" + 
                VSDK.formatByteAsHex(a) + VSDK.formatByteAsHex(b) + ">";
        }
        return chunkname;
    }

    public _Reader3dsChunk()
    {
        id = 0x0000;
        length = 0;
    }

    @Override
    public String toString()
    {
        String msg;
        msg = "CHUNK3DS type [" +  chunkToString(id) + "], length ["
            + length + "]";
        return msg;
    }

    public void readHeader(InputStream is) throws Exception
    {
        id = readSignedShortLE(is);
        length = readLongLE(is);
    }
}

/**
The class Reader3ds provides 3DStudio loading functionality.  The .3ds
fileformat was the original binary fileformat for ancient 3DStudio program
from Kinetix/Discreet originally deployed for the PC/DOS platform.  The
format was later upgraded to .MAX (not compatible) in windows version of
the program, known as "3DStudio MAX". However, current versions of 3DStudio
MAX support backward compatibility importing and exporting to old 3DStudio
format, and several files exists today persisted in this format.

This is currently a Java/VitralSDK based implementation of the algorithms
and data structures as described in [PITT1994], [FERC1996] and [VANV1997].

\todo  Perhaps "Reader3ds" is not the best name for this class, as in the
future should support exporting (writing) operations. It could be renamed
to something as "Persistence3ds". Some chunks are not being processed.
*/
public class Reader3ds extends PersistenceElement
{
    // Accumulated object parts
    private static ArrayList<Vector3D> currentVertexPositionArray = null;
    private static ArrayList<_Reader3dsMaterialMapping> currentMaterialMappingArray = null;
    private static TriangleMesh currentTriangleMesh = null;
    private static Triangle currentTrianglesList[] = null;
    private static double currentUTextureMapping[] = null;
    private static double currentVTextureMapping[] = null;

    // Current environment building elements
    private static Material currentBuildingMaterial = null;
    private static String currentTextureFilename = null;
    private static ColorRgb currentColor = null;
    private static int currentAmount = 0;
    private static ArrayList<Material> currentMaterialArray = null;
    private static ArrayList<String> currentTextureFilenamesArray = null;
    private static ArrayList<SimpleBody> currentSimpleBodiesArray = null;
    private static String workingDirectory = null;

    // Error reporting attributes
    private static boolean bumpNotImplementedReported = false;
    private static boolean t2NotImplementedReported = false;

    private static Material defaultMaterial()
    {
        Material m = new Material();

        m.setAmbient(new ColorRgb(0.2, 0.2, 0.2));
        m.setDiffuse(new ColorRgb(0.5, 0.9, 0.5));
        m.setSpecular(new ColorRgb(1, 1, 1));
        return m;
    }

    private static Material resolveMaterial(String name)
    {
        int i;
        Material m;

        for ( i = 0; i < currentMaterialArray.size(); i++ ) {
            m = currentMaterialArray.get(i);
            if ( name.equals(m.getName()) ) {
                return m;
            }
        }
        return defaultMaterial();
    }

    private static int resolveMaterialIndex(String name)
    {
        int i;
        Material m;

        for ( i = 0; i < currentMaterialArray.size(); i++ ) {
            m = currentMaterialArray.get(i);
            if ( name.equals(m.getName()) ) {
                return i;
            }
        }
        return -1;
    }

    private static Image loadImagefile(String imageFilename)
    {
        //-----------------------------------------------------------------
        if ( imageFilename == null || imageFilename.length() < 1 ) {
            return null;
        }

        //-----------------------------------------------------------------
        RGBImage img;
        String fullFilename = workingDirectory + "/" + imageFilename.toLowerCase();

        try {
            img = ImagePersistence.importRGB(new File(fullFilename));
        }
        catch (Exception e) {
            System.err.println("Error: could not read the image file \"" + fullFilename + "\".");
            System.err.println("Check you have access to that file from current working directory.");
            System.err.println(e);
            img = new RGBImage();
            img.init(64, 64);
            img.createTestPattern();
        }
        return img;
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

    private static String indent(int level) {
        String tab;
        switch ( level ) {
          case 0: tab = ""; break;
          case 1: tab = "  - "; break;
          case 2: tab = "    . "; break;
          default:
            tab = "      (" + level + ")-> ";
            break;
        }
        return tab;
    }

    private static boolean checkChunkHierarchy(_Reader3dsChunk son, 
                                               _Reader3dsChunk father)
    {
        if (
(father == null) ||
(father.id == _Reader3dsChunk.ID_MAIN &&
    son.id == _Reader3dsChunk.ID_MESH) ||
(father.id == _Reader3dsChunk.ID_MAIN &&
    son.id == _Reader3dsChunk.ID_BOTTOM) ||
(father.id == _Reader3dsChunk.ID_MAIN &&
    son.id == _Reader3dsChunk.ID_KEYFRAMER) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_TOP) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_MATERIAL) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_OBJECT_BLOCK) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_CONFIG1) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_VIEWPORT) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_01) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_02) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_03) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_04) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_05) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_06) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_07) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_08) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_09) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_10) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_11) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_12) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_13) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_EDIT_UNKNOWN_14) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_AMBIENT_COLOR) ||
(father.id == _Reader3dsChunk.ID_MESH &&
    son.id == _Reader3dsChunk.ID_BACKGROUND_COLOR) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_NAME) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_AMBIENT) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_DIFFUSE) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_SPECULAR) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_TYPE) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_SHININESS_EXP) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_SHININESS_KTE) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_TRANSPARENCY) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_TRANSPARENCY_F) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_TRANSPARENCY_FI) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_REFLECT_BLUR) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_TWOSIDED) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_WIREON) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_WIRE_THICKNESS) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_SOFTEN) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_TEXTURE1) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_TEXTURE2) ||
(father.id == _Reader3dsChunk.ID_MATERIAL &&
    son.id == _Reader3dsChunk.ID_MATERIAL_BUMPMAP) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_TEXTURE1 &&
    son.id == _Reader3dsChunk.ID_AMOUNT) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_TEXTURE1 &&
    son.id == _Reader3dsChunk.ID_MATERIAL_MAPOPTIONS) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_TEXTURE1 &&
    son.id == _Reader3dsChunk.ID_MATERIAL_MAPFILTERBLUR) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_TEXTURE1 &&
    son.id == _Reader3dsChunk.ID_MATERIAL_MAPUOFFSET) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_TEXTURE1 &&
    son.id == _Reader3dsChunk.ID_MATERIAL_MAPVOFFSET) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_TEXTURE1 &&
    son.id == _Reader3dsChunk.ID_MATERIAL_MAP1USCALE) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_TEXTURE1 &&
    son.id == _Reader3dsChunk.ID_MATERIAL_MAP1VSCALE) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_TEXTURE1 &&
    son.id == _Reader3dsChunk.ID_MATERIAL_MAPFILENAME) ||
(father.id == _Reader3dsChunk.ID_TRIANGLE_LIST &&
    son.id == _Reader3dsChunk.ID_MATERIAL_MAPPING_TABLE) ||
(father.id == _Reader3dsChunk.ID_TRIANGLE_LIST &&
    son.id == _Reader3dsChunk.ID_SMOOTH_LIST) ||
(father.id == _Reader3dsChunk.ID_OBJECT_BLOCK &&
    son.id == _Reader3dsChunk.ID_TRIANGLE_MESH) ||
(father.id == _Reader3dsChunk.ID_OBJECT_BLOCK &&
    son.id == _Reader3dsChunk.ID_LIGHT) ||
(father.id == _Reader3dsChunk.ID_OBJECT_BLOCK &&
    son.id == _Reader3dsChunk.ID_CAMERA) ||
(father.id == _Reader3dsChunk.ID_TRIANGLE_MESH &&
    son.id == _Reader3dsChunk.ID_VERTEX_LIST) ||
(father.id == _Reader3dsChunk.ID_TRIANGLE_MESH &&
    son.id == _Reader3dsChunk.ID_TRIANGLE_LIST) ||
(father.id == _Reader3dsChunk.ID_TRIANGLE_MESH &&
    son.id == _Reader3dsChunk.ID_MAP_LIST) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_SHININESS_KTE &&
    son.id == _Reader3dsChunk.ID_AMOUNT) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_SHININESS_EXP &&
    son.id == _Reader3dsChunk.ID_AMOUNT) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_TRANSPARENCY &&
    son.id == _Reader3dsChunk.ID_AMOUNT) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_TRANSPARENCY_F &&
    son.id == _Reader3dsChunk.ID_AMOUNT) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_TRANSPARENCY_FI &&
    son.id == _Reader3dsChunk.ID_AMOUNT) ||
(father.id == _Reader3dsChunk.ID_MATERIAL_REFLECT_BLUR &&
    son.id == _Reader3dsChunk.ID_AMOUNT) ||
(father.id == _Reader3dsChunk.ID_TRIANGLE_MESH &&
    son.id == _Reader3dsChunk.ID_OBJECT_IS_VISIBLE) ||
(father.id == _Reader3dsChunk.ID_TRIANGLE_MESH &&
    son.id == _Reader3dsChunk.ID_TRI_VERTEX_OPTIONS) ||
(father.id == _Reader3dsChunk.ID_TRIANGLE_MESH && son.id == _Reader3dsChunk.ID_MATRIX) ||
((father.id == _Reader3dsChunk.ID_MATERIAL_AMBIENT || 
  father.id == _Reader3dsChunk.ID_MATERIAL_DIFFUSE || 
  father.id == _Reader3dsChunk.ID_MATERIAL_SPECULAR)  &&
    son.id == _Reader3dsChunk.ID_COLOR_RGB1) ||
((father.id == _Reader3dsChunk.ID_MATERIAL_AMBIENT || 
  father.id == _Reader3dsChunk.ID_MATERIAL_DIFFUSE || 
  father.id == _Reader3dsChunk.ID_MATERIAL_SPECULAR)  &&
    son.id == _Reader3dsChunk.ID_COLOR_RGB2) ||
((father.id == _Reader3dsChunk.ID_MATERIAL_AMBIENT || 
  father.id == _Reader3dsChunk.ID_MATERIAL_DIFFUSE || 
  father.id == _Reader3dsChunk.ID_MATERIAL_SPECULAR)  &&
    son.id == _Reader3dsChunk.ID_COLOR_RGB3)
        ) {
            return true;
        }

        if ( son.id == _Reader3dsChunk.ID_UNKNOWN_01 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_02 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_03 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_04 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_05 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_06 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_07 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_08 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_09 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_10 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_11 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_12 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_13 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_14 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_15 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_16 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_17 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_18 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_19 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_20 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_21 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_22 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_23 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_24 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_25 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_26 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_27 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_28 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_29 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_30 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_31 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_32 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_33 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_34 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_35 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_36 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_37 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_38 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_39 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_40 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_41 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_42 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_43 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_44 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_45 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_46 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_47 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_48 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_49 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_50 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_51 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_52 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_53 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_54 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_55 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_56 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_57 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_58 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_59 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_60 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_61 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_62 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_63 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_64 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_65 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_66 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_67 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_67 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_68 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_69 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_70 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_71 ||
             son.id == _Reader3dsChunk.ID_UNKNOWN_72 ) {
            // WARNING: Not implemented features!
            return true;
        }

        VSDK.reportMessage(null, VSDK.WARNING,
                           "Reader3ds.checkChunkHierarchy",
                           "" + _Reader3dsChunk.chunkToString(son.id) + 
                           " chunk is not supposed to be a level under a " +
                           _Reader3dsChunk.chunkToString(father.id) + " chunk");
        return false;
    }

    /**
    \todo  Process missing chunks. (skipped)
    */
    private static void processChunk(
        InputStream is, 
        _Reader3dsChunk currentChunk, _Reader3dsChunk parentChunk,
        int level) 
        throws Exception
    {
        //-----------------------------------------------------------------
        boolean skipChunk = false;

        if ( !checkChunkHierarchy(currentChunk, parentChunk) ) {
            skipChunk = true;
        }

        //-----------------------------------------------------------------
        int i;

        if ( (currentChunk.id == _Reader3dsChunk.ID_MAIN) ||
             (currentChunk.id == _Reader3dsChunk.ID_MESH) || 
             (currentChunk.id == _Reader3dsChunk.ID_OBJECT_BLOCK) ||
             (currentChunk.id == _Reader3dsChunk.ID_MATERIAL) ||
             (currentChunk.id == _Reader3dsChunk.ID_TRIANGLE_MESH)
           ) {
            //-------------------------------------------------------------
            long internalBytes = 6; // skip current chunk header

            // Build operations preprocessing phase
            if ( currentChunk.id == _Reader3dsChunk.ID_OBJECT_BLOCK ) {
                // Object block starts with a name string
                //System.out.print(indent(level) + "Reading object \"");
                String cad = readAsciiString(is);
                internalBytes += cad.length()+1;
                //System.out.println(cad + "\"");
            }
            if ( currentChunk.id == _Reader3dsChunk.ID_TRIANGLE_MESH ) {
                currentTriangleMesh = new TriangleMesh();
                currentMaterialMappingArray =
                    new ArrayList<_Reader3dsMaterialMapping>();
            }
            if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL ) {
                currentTextureFilename = null;
                currentBuildingMaterial = new Material();
                currentBuildingMaterial.setDoubleSided(false);
            }

            // Generic recursive block processing
            //System.out.println(indent(level) + currentChunk);

            // Processing of recursive chunks
            _Reader3dsChunk subChunk = new _Reader3dsChunk();

            do {
                subChunk.readHeader(is);
                processChunk(is, subChunk, currentChunk, level+1);
                internalBytes += subChunk.length;
            } while ( is.available() > 0 && 
                      (internalBytes < currentChunk.length) );

            // Build operations postprocessing phase
            if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL ) {
                currentMaterialArray.add(currentBuildingMaterial);
                currentTextureFilenamesArray.add(currentTextureFilename);
            }
            if ( currentChunk.id == _Reader3dsChunk.ID_TRIANGLE_MESH ) {
                // Vertex processing
                Vertex v[] = new Vertex[currentVertexPositionArray.size()];
                for ( i = 0; i < v.length; i++ ) {
                    v[i] = new Vertex(new Vector3D(currentVertexPositionArray.get(i)));
                }
                for ( i = 0;
                      i < v.length && currentUTextureMapping != null &&
                      i < currentUTextureMapping.length;
                      i++ ) {
                    v[i].u = currentUTextureMapping[i];
                    v[i].v = currentVTextureMapping[i];
                }
                currentTriangleMesh.setVertexes(v, true, false, false, true);

                // Triangle processing
                int numMappedTriangles = 0;
                _Reader3dsMaterialMapping map_i;
                for ( i = 0; i < currentMaterialMappingArray.size(); i++ ) {
                    map_i = currentMaterialMappingArray.get(i);
                    numMappedTriangles += map_i.associatedTriangles.length;
                }

                if ( numMappedTriangles <= 0 ) {
                    currentTriangleMesh.setTriangles(currentTrianglesList);
                }
                else {
                    Triangle newTrianglesList[];
                    Material newMaterials[];
                    Image newTextures[];
                    int newMaterialRanges[][];
                    int newTextureRanges[][];
                    int j, k, textureIndex;

                    newTrianglesList = new Triangle[numMappedTriangles];
                    newMaterials =
                        new Material[currentMaterialMappingArray.size()];
                    newTextures = new Image[currentMaterialMappingArray.size()];
                    newMaterialRanges = new int[currentMaterialMappingArray.size()][2];
                    newTextureRanges = new int[currentMaterialMappingArray.size()][2];

                    for ( i = 0, k = 0; 
                          i < currentMaterialMappingArray.size(); i++ ) {
                        map_i = currentMaterialMappingArray.get(i);
                        newMaterials[i] = resolveMaterial(map_i.materialName);
                        textureIndex = resolveMaterialIndex(map_i.materialName);
                        if ( textureIndex >= 0 ) {
                            newTextures[i] = loadImagefile(currentTextureFilenamesArray.get(textureIndex));
                          }
                          else {
                            newTextures[i] = null;
                        }

                        // This is checkpoint A
                        if ( newTrianglesList == null ) {
                            VSDK.reportMessage(null, VSDK.WARNING,
                            "Reader3ds.processChunk",
                            "null newTriangleList at checkpoint A.");
                        }
                        if ( currentTrianglesList == null ) {
                            VSDK.reportMessage(null, VSDK.WARNING,
                            "Reader3ds.processChunk",
                            "null currentTrianglesList at checkpoint A.");
                        }

                        for ( j = 0; 
                              j < map_i.associatedTriangles.length; j++ ) {
                            if ( k >= newTrianglesList.length ) {
                                VSDK.reportMessage(null, VSDK.WARNING,
                                "Reader3ds.processChunk",
                                "k to large at checkpoint A.");
                            }
                            if ( map_i.associatedTriangles[j] >=
                                 currentTrianglesList.length ) {
                                VSDK.reportMessage(null, VSDK.WARNING,
                                "Reader3ds.processChunk",
                                "map to large at checkpoint A.");
                            }
                            newTrianglesList[k] = 
                            currentTrianglesList[map_i.associatedTriangles[j]];
                            k++;
                        }
                        newMaterialRanges[i][0] = k;
                        newMaterialRanges[i][1] = i;
                        newTextureRanges[i][0] = k;
                        if ( newTextures[i] == null ) {
                            newTextureRanges[i][1] = 0;
                        }
                        else {
                            newTextureRanges[i][1] = i+1;
                        }
                    }
                    currentTriangleMesh.setTriangles(newTrianglesList);
                    currentTriangleMesh.setMaterials(newMaterials);
                    currentTriangleMesh.setTextures(newTextures);
                    currentTriangleMesh.setMaterialRanges(newMaterialRanges);
                    currentTriangleMesh.setTextureRanges(newTextureRanges);
                    currentTrianglesList = null;
                }

                // Mesh adition to environment
                currentTriangleMesh.calculateNormals();
                addThing(currentTriangleMesh, currentSimpleBodiesArray);
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_VERTEX_LIST ) {
            //-------------------------------------------------------------
            //System.out.println(indent(level) + currentChunk);
            int numVertexes = readSignedShortLE(is);
            //System.out.println(indent(level+1) + "  . Reading " + numVertexes +
            //                 " vertexes");

            currentVertexPositionArray = new ArrayList<Vector3D>();
            Vector3D p;
            for ( i = 0; i < numVertexes; i++ ) {
                p = new Vector3D();
                p.x = readFloatLE(is);
                p.y = readFloatLE(is);
                p.z = readFloatLE(is);
                currentVertexPositionArray.add(p);
            }
            if ( currentChunk.length-8-numVertexes*12 > 0 ) {
                is.skip(currentChunk.length-8-numVertexes*12);
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_TRIANGLE_LIST ) {
            //-------------------------------------------------------------
            //System.out.println(indent(level) + currentChunk);

            //----
            int numTriangles = readSignedShortLE(is);
            //System.out.println(indent(level+1) + "  . Reading " + numTriangles +
            //                 " triangles");

            int a, b, c, flags;

            currentTrianglesList = new Triangle[numTriangles];
            for ( i = 0; i < numTriangles; i++ ) {
                a = readSignedShortLE(is);
                b = readSignedShortLE(is);
                c = readSignedShortLE(is);
                flags = readSignedShortLE(is) & 0x07;
                /* Warning: dont know how to process this! */
                if ( (flags & 0x07) == 0x00 ||
                     (flags & 0x07) == 0x01 ||
                     (flags & 0x07) == 0x02 ||
                     (flags & 0x07) == 0x03 ||
                     (flags & 0x07) == 0x04 ||
                     (flags & 0x07) == 0x05 ||
                     (flags & 0x07) == 0x06 ||
                     (flags & 0x07) == 0x07 ) {
                    currentTrianglesList[i] = new Triangle(a, b, c);
                  }
                  else {
                    currentTrianglesList[i] = new Triangle(a, c, b);
                }
                //System.out.println("FACE: " + flags);
            }

            //----
            // skip current chunk header AND triangles
            long internalBytes = 8+numTriangles*8; 

            // Processing of recursive chunks
            _Reader3dsChunk subChunk = new _Reader3dsChunk();

            do {
                subChunk.readHeader(is);
                processChunk(is, subChunk, currentChunk, level+1);
                internalBytes += subChunk.length;
            } while ( is.available() > 0 && 
                      (internalBytes < currentChunk.length) );
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATRIX ) {
            //-------------------------------------------------------------
            //System.out.println(indent(level) + currentChunk);
            is.skip(12*4);
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_NAME ) {
            //-------------------------------------------------------------
            //System.out.println(indent(level) + currentChunk);
            String materialName = readAsciiString(is);
            if ( currentBuildingMaterial != null ) {
                currentBuildingMaterial.setName(materialName);
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_COLOR_RGB2 ) {
            //-------------------------------------------------------------
            //System.out.println(indent(level) + currentChunk);
            byte r[] = new byte[1];
            byte g[] = new byte[1];
            byte b[] = new byte[1];
            readBytes(is, r);
            readBytes(is, g);
            readBytes(is, b);
            if ( currentColor != null ) {
                currentColor.r = 
                    (double)(VSDK.signedByte2unsignedInteger(r[0])) / 255.0;
                currentColor.g = 
                    (double)(VSDK.signedByte2unsignedInteger(g[0])) / 255.0;
                currentColor.b = 
                    (double)(VSDK.signedByte2unsignedInteger(b[0])) / 255.0;
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_AMOUNT ) {
            //-------------------------------------------------------------
            //System.out.println(indent(level) + currentChunk);
            currentAmount = readSignedShortLE(is);
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_MAPFILENAME ) {
            //-------------------------------------------------------------
            currentTextureFilename = readAsciiString(is);
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_SHININESS_KTE ) {
            //System.out.println(indent(level) + currentChunk);
            currentAmount = 0;
            // Processing of recursive chunks
            _Reader3dsChunk subChunk = new _Reader3dsChunk();
            long internalBytes = 6; // Skip current chunk header
            do {
                subChunk.readHeader(is);
                processChunk(is, subChunk, currentChunk, level+1);
                internalBytes += subChunk.length;
            } while ( is.available() > 0 && 
                      (internalBytes < currentChunk.length) );
            if ( currentBuildingMaterial != null ) {
                // Warning: don't know well how to handle this (percent over
                // specular color?)
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_TRANSPARENCY ) {
            //System.out.println(indent(level) + currentChunk);
            currentAmount = 0;
            // Processing of recursive chunks
            _Reader3dsChunk subChunk = new _Reader3dsChunk();
            long internalBytes = 6; // Skip current chunk header
            do {
                subChunk.readHeader(is);
                processChunk(is, subChunk, currentChunk, level+1);
                internalBytes += subChunk.length;
            } while ( is.available() > 0 && 
                      (internalBytes < currentChunk.length) );
            if ( currentBuildingMaterial != null && currentAmount > 0 ) {
                if ( currentAmount > 100 ) currentAmount = 100;
                // Warning: not verified yet
                currentBuildingMaterial.setOpacity(1.0-((double)currentAmount)/100.0);
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_TRANSPARENCY_F ) {
            //System.out.println(indent(level) + currentChunk);
            currentAmount = 0;
            // Processing of recursive chunks
            _Reader3dsChunk subChunk = new _Reader3dsChunk();
            long internalBytes = 6; // Skip current chunk header
            do {
                subChunk.readHeader(is);
                processChunk(is, subChunk, currentChunk, level+1);
                internalBytes += subChunk.length;
            } while ( is.available() > 0 && 
                      (internalBytes < currentChunk.length) );
            if ( currentBuildingMaterial != null ) {
                // Warning: don't know well how to handle this
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_TRANSPARENCY_FI ) {
            // Warning: don't know well how to handle this
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_TWOSIDED ) {
            if ( currentBuildingMaterial != null ) {
                currentBuildingMaterial.setDoubleSided(true);
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_REFLECT_BLUR ) {
            //System.out.println(indent(level) + currentChunk);
            currentAmount = 0;
            // Processing of recursive chunks
            _Reader3dsChunk subChunk = new _Reader3dsChunk();
            long internalBytes = 6; // Skip current chunk header
            do {
                subChunk.readHeader(is);
                processChunk(is, subChunk, currentChunk, level+1);
                internalBytes += subChunk.length;
            } while ( is.available() > 0 && 
                      (internalBytes < currentChunk.length) );
            if ( currentBuildingMaterial != null ) {
                // Warning: don't know well how to handle this
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_SHININESS_EXP ) {
            //System.out.println(indent(level) + currentChunk);
            currentAmount = 0;
            // Processing of recursive chunks
            _Reader3dsChunk subChunk = new _Reader3dsChunk();
            long internalBytes = 6; // Skip current chunk header
            do {
                subChunk.readHeader(is);
                processChunk(is, subChunk, currentChunk, level+1);
                internalBytes += subChunk.length;
            } while ( is.available() > 0 && 
                      (internalBytes < currentChunk.length) );
            if ( currentBuildingMaterial != null ) {
                currentBuildingMaterial.setPhongExponent((double)currentAmount);
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_AMBIENT ) {
            //-------------------------------------------------------------
            //System.out.println(indent(level) + currentChunk);
            currentColor = new ColorRgb();
            // Processing of recursive chunks
            _Reader3dsChunk subChunk = new _Reader3dsChunk();
            long internalBytes = 6; // Skip current chunk header
            do {
                subChunk.readHeader(is);
                processChunk(is, subChunk, currentChunk, level+1);
                internalBytes += subChunk.length;
            } while ( is.available() > 0 && 
                      (internalBytes < currentChunk.length) );
            // Set color in the material
            if ( currentBuildingMaterial != null ) {
                currentBuildingMaterial.setAmbient(currentColor);
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_DIFFUSE ) {
            //-------------------------------------------------------------
            //System.out.println(indent(level) + currentChunk);
            currentColor = new ColorRgb();
            // Processing of recursive chunks
            _Reader3dsChunk subChunk = new _Reader3dsChunk();
            long internalBytes = 6; // Skip current chunk header
            do {
                subChunk.readHeader(is);
                processChunk(is, subChunk, currentChunk, level+1);
                internalBytes += subChunk.length;
            } while ( is.available() > 0 && 
                      (internalBytes < currentChunk.length) );
            // Set color in the material
            if ( currentBuildingMaterial != null ) {
                currentBuildingMaterial.setDiffuse(currentColor);
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_SPECULAR ) {
            //-------------------------------------------------------------
            //System.out.println(indent(level) + currentChunk);
            currentColor = new ColorRgb();
            // Processing of recursive chunks
            _Reader3dsChunk subChunk = new _Reader3dsChunk();
            long internalBytes = 6; // Skip current chunk header
            do {
                subChunk.readHeader(is);
                processChunk(is, subChunk, currentChunk, level+1);
                internalBytes += subChunk.length;
            } while ( is.available() > 0 && 
                      (internalBytes < currentChunk.length) );
            // Set color in the material
            if ( currentBuildingMaterial != null ) {
                currentBuildingMaterial.setSpecular(currentColor);
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_MAPPING_TABLE ) {
            //-------------------------------------------------------------
            //System.out.println(indent(level) + currentChunk);
            _Reader3dsMaterialMapping range = new _Reader3dsMaterialMapping();
            range.materialName = readAsciiString(is);
            int numMappings = readSignedShortLE(is);
            range.associatedTriangles = new int[numMappings];
            //System.out.println(indent(level+1) + "  . Reading " +
            //             numMappings + " material mappings for material \"" +
            //             range.materialName + "\"");
            for ( i = 0; i < numMappings; i++ ) {
                range.associatedTriangles[i] = readSignedShortLE(is);
            }
            currentMaterialMappingArray.add(range);
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MAP_LIST ) {
            //-------------------------------------------------------------
            int numVerticesUVMaps = readSignedShortLE(is);
            currentUTextureMapping = new double[numVerticesUVMaps];
            currentVTextureMapping = new double[numVerticesUVMaps];
            for ( i = 0; i < currentVTextureMapping.length; i++ ) {
                currentUTextureMapping[i] = readFloatLE(is);
                currentVTextureMapping[i] = readFloatLE(is);
            }
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_TEXTURE1 ) {
            //-------------------------------------------------------------
            // Prepare values
            currentAmount = 0;

            // Processing of recursive chunks
            _Reader3dsChunk subChunk = new _Reader3dsChunk();
            long internalBytes = 6; // Skip current chunk header
            do {
                subChunk.readHeader(is);
                processChunk(is, subChunk, currentChunk, level+1);
                internalBytes += subChunk.length;
            } while ( is.available() > 0 && 
                      (internalBytes < currentChunk.length) );

            // Texture building
            // Consider currentAmount
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_BUMPMAP ) {
            //-------------------------------------------------------------
            // WARNING: Not implemented feature!
            if ( !bumpNotImplementedReported ) {
                VSDK.reportMessage(null, VSDK.WARNING,
                           "Reader3ds.processChunk",
                           "Chunk ID_MATERIAL_BUMPMAP not implemented! " +
                           "Further error reporting disabled.");
                bumpNotImplementedReported = true;
            }
            skipChunk = true;
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_TEXTURE2 ) {
            //-------------------------------------------------------------
            // WARNING: Not implemented feature!
            if ( !t2NotImplementedReported ) {
                VSDK.reportMessage(null, VSDK.WARNING,
                           "Reader3ds.processChunk",
                           "Chunk ID_MATERIAL_TEXTURE2 not implemented! " +
                           "Further error reporting disabled.");
                t2NotImplementedReported = true;
            }
            skipChunk = true;
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_WIREON ) {
            //-------------------------------------------------------------
            // WARNING: Not implemented feature!
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_MAPOPTIONS ) {
            //-------------------------------------------------------------
            // WARNING: Not implemented feature!
            int dummyOptions = readSignedShortLE(is);
            //System.out.println("Options: " + dummyOptions);
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_MAPFILTERBLUR ) {
            //-------------------------------------------------------------
            // WARNING: Not implemented feature!
            double dummyBlur = readFloatLE(is);
            //System.out.println("Blur: " + dummyBlur);
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_MAPUOFFSET ) {
            //-------------------------------------------------------------
            // WARNING: Not implemented feature!
            double currentUOffset = readFloatLE(is);
            //System.out.println("DU: " + currentUOffset);
        }
        else if ( currentChunk.id == _Reader3dsChunk.ID_MATERIAL_MAPVOFFSET ) {
            //-------------------------------------------------------------
            // WARNING: Not implemented feature!
            double currentVOffset = readFloatLE(is);
            //System.out.println("DV: " + currentVOffset);
        }
        else {
            //-------------------------------------------------------------
            //System.out.println(indent(level) + currentChunk + " (skipped)");
            // Trivial case: unknown chunk
            skipChunk = true;
        }

        //-----------------------------------------------------------------
        if ( skipChunk ) {
            if ( currentChunk.length - 6 > 0 ) {
                is.skip(currentChunk.length - 6);
            }
        }

    }

    public static void
    importEnvironment(InputStream is, String pathname, String sourcename, SimpleScene inoutSimpleScene)
        throws Exception
    {
        //-----------------------------------------------------------------
        ArrayList<SimpleBody> simpleBodiesArray = inoutSimpleScene.getSimpleBodies();
        ArrayList<Light> lightsArray = inoutSimpleScene.getLights();
        ArrayList<Background> backgroundsArray = inoutSimpleScene.getBackgrounds();
        ArrayList<Camera> camerasArray = inoutSimpleScene.getCameras();

        //-----------------------------------------------------------------
        _Reader3dsChunk chunk = new _Reader3dsChunk();

        currentSimpleBodiesArray = simpleBodiesArray;
        currentMaterialArray = new ArrayList<Material>();
        currentTextureFilenamesArray = new ArrayList<String>();

        workingDirectory = pathname;

        //- Main level chunk hierarchy processing -------------------------
        chunk.readHeader(is);

        if ( chunk.id != _Reader3dsChunk.ID_MAIN ) {
            Exception e = new Exception("\"" + sourcename +
                "\" is not a 3DS format file, doesn't start with 0x4D4D " +
                "header chunk.");
            throw e;
        }

        processChunk(is, chunk, null, 0);

        //- Free resources and mark unused memory area for garbage collection
        is.close();
        currentVertexPositionArray = null;
        currentMaterialMappingArray = null;
        currentTriangleMesh = null;
        currentTrianglesList = null;
        currentBuildingMaterial = null;
        currentTextureFilename = null;
        currentColor = null;
        currentMaterialArray = null;
        currentTextureFilenamesArray = null;
        currentSimpleBodiesArray = null;
        workingDirectory = null;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
