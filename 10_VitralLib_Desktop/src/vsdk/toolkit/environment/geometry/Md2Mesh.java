/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vsdk.toolkit.environment.geometry;

// Java basic classes
import java.util.ArrayList;
import vsdk.toolkit.media.Image;

// VitralSDK classes

/**
 *
 * @author 
 */
public class Md2Mesh {
    public int ident;                  // magic number: "IDP2" (844121161)
    public int version;                // version: must be 8
    public int skinWidth;              // texture width
    public int skinHeight;             // texture height
    public int frameSize;              // size in bytes of a frame
    public int numSkins;               // number of skins
    public int numVertices;            // number of vertices per frame
    public int numTexCoords;           // number of texture coordinates
    public int numTriangles;           // number of triangles
    public int numGlCommands;          // number of opengl commands
    public int numFrames;              // number of frames
    public int offsetSkins;            // offset skin data
    public int offsetTexCoords;        // offset texture coordinate data
    public int offsetTriangles;        // offset triangle data
    public int offsetFrames;           // offset frame data
    public int offsetGlCommands;       // offset OpenGL command data
    public int offsetEnd;              // offset end of file
  
    public ArrayList<String> skinNames = new ArrayList<String>();
    public Image[] skins;
    public ArrayList<String> frameNames = new ArrayList<String>();
    public ArrayList<float[]> frameVertices = new ArrayList<float[]>();
    public ArrayList<short[]> frameNormalIndices = new ArrayList<short[]>();
    public ArrayList<float[]> glCmdTexCoordsStrip = new ArrayList<float[]>();
    public ArrayList<int[]> glCmdVertIndexStrip = new ArrayList<int[]>();
    public ArrayList<float[]> glCmdTexCoordsFan = new ArrayList<float[]>();
    public ArrayList<int[]> glCmdVertIndexFan = new ArrayList<int[]>();
    public float[]  texCoords;
    public int[][] triangles;
    private ArrayList<_AnimationInfo> aniInfos = new ArrayList<_AnimationInfo>();
    private float frameTimeSeg = 0.25f;
    private float elapsedTimeSeg;
//    private int currentFrame=0;
    private short currentAnimationInd=0;
    private short maxAnimationInd=0;

    public short getMaxAnimationInd() {
        return maxAnimationInd;
    }
    public void setMaxAnimationInd(short maxAnimationInd) {
        this.maxAnimationInd = maxAnimationInd;
    }
    public short getCurrentAnimationInd() {
        return currentAnimationInd;
    }
    public void setCurrentAnimationInd(short currentAnimationInd) {
        this.currentAnimationInd = (short)(currentAnimationInd % (maxAnimationInd+1));
    }
    public float getFrameTimeSeg() {
        return frameTimeSeg;
    }
    public void setFrameTimeSeg(float frameTimeSeg) {
        this.frameTimeSeg = frameTimeSeg;
    }
    public float getElapsedTimeSeg() {
        return elapsedTimeSeg;
    }
    public void setElapsedTimeSeg(float elapsedTimeSeg) {
        this.elapsedTimeSeg = elapsedTimeSeg;
    }
//    public int getCurrentFrame() {
//        return currentFrame;
//    }
//    public void setCurrentFrame(int currentFrame) {
//        this.currentFrame = currentFrame % numFrames;
//    }
    
//    public int getCurrentFrameInc() {
//        if(currentFrame == numFrames)// All the animations.
//            currentFrame = 0;
//        else
//            ++currentFrame; 
//        return currentFrame;
//    };
    
    // If frameNames is empty return 0 and 0 in outStartEnd array.
    public void returnStartEndAnim(short inIndex, short[] outStartEnd) {
        if(frameNames.isEmpty ()) {
            outStartEnd[0] = 0;
            outStartEnd[1] = 0;
            return;
        }
        if(aniInfos.isEmpty()) {
            fillAniInfo();
        }
        outStartEnd[0] = aniInfos.get(inIndex).start;
        outStartEnd[1] = aniInfos.get(inIndex).end;
    }
    
    // If frameNames is empty or inNameAnim is not found, return 0 and 0 in
    // in outStartEnd array.
    public void returnStartEndAnim(String inNameAnim, short[] outStartEnd) {
        _AnimationInfo ai;
        
        if(frameNames.isEmpty ()) {
            outStartEnd[0] = 0;
            outStartEnd[1] = 0;
            return;
        }
        if(aniInfos.isEmpty()) {
            fillAniInfo();
        }
        ai = getAniInfo(inNameAnim);
        if(ai != null){
            outStartEnd[0] = ai.start;
            outStartEnd[1] = ai.end;
        }
        else {
            outStartEnd[0] = 0;
            outStartEnd[1] = 0;            
        }
    }
    
    private _AnimationInfo getAniInfo(String nameAnim) {
        for(_AnimationInfo ai : aniInfos) {
            if(ai.name.equals(nameAnim.trim())) return ai;
        }
        return null;
    }
    
    private void fillAniInfo() {
        short i,pos;
        String fName;
        _AnimationInfo aniInfoT;
        
        aniInfoT = new _AnimationInfo();
        // aniInfoT.name initially must be different from any frame name,
        // this is guaranteed with a length superior to 16.
        aniInfoT.name = "12345678901234567890";
        for(i=0;i<frameNames.size ();++i) {
            fName = frameNames.get(i);
            fName = fName.trim();
            pos = (short)fName.indexOf('\u0000');// \u0000 = null character.
            if(pos == -1)
                pos = (short)fName.length();
            fName = fName.substring(0, pos);
            pos--;
            while(pos!=-1 && Character.isDigit(fName.charAt(pos)))
                --pos;
            fName = fName.substring(0, pos+1);
            if(!fName.equals(aniInfoT.name)) {
                if(i != 0) {
                    aniInfoT.end = (short)(i-1);
                    aniInfos.add(aniInfoT);
                }
                aniInfoT = new _AnimationInfo();
                aniInfoT.name = fName;
                aniInfoT.start = i;
            }
        }
        // The last animation(needs the 'i' final value).
        aniInfoT.end = (short)(i-1);
        aniInfos.add(aniInfoT);
        maxAnimationInd = (short)(aniInfos.size()-1);
    }
    
    public static float[][] anorms =
    {
        {-0.525731f, 0.000000f, 0.850651f}, 
        {-0.442863f, 0.238856f, 0.864188f}, 
        {-0.295242f, 0.000000f, 0.955423f}, 
        {-0.309017f, 0.500000f, 0.809017f}, 
        {-0.162460f, 0.262866f, 0.951056f}, 
        {0.000000f, 0.000000f, 1.000000f}, 
        {0.000000f, 0.850651f, 0.525731f}, 
        {-0.147621f, 0.716567f, 0.681718f}, 
        {0.147621f, 0.716567f, 0.681718f}, 
        {0.000000f, 0.525731f, 0.850651f}, 
        {0.309017f, 0.500000f, 0.809017f}, 
        {0.525731f, 0.000000f, 0.850651f}, 
        {0.295242f, 0.000000f, 0.955423f}, 
        {0.442863f, 0.238856f, 0.864188f}, 
        {0.162460f, 0.262866f, 0.951056f}, 
        {-0.681718f, 0.147621f, 0.716567f}, 
        {-0.809017f, 0.309017f, 0.500000f}, 
        {-0.587785f, 0.425325f, 0.688191f}, 
        {-0.850651f, 0.525731f, 0.000000f}, 
        {-0.864188f, 0.442863f, 0.238856f}, 
        {-0.716567f, 0.681718f, 0.147621f}, 
        {-0.688191f, 0.587785f, 0.425325f}, 
        {-0.500000f, 0.809017f, 0.309017f}, 
        {-0.238856f, 0.864188f, 0.442863f}, 
        {-0.425325f, 0.688191f, 0.587785f}, 
        {-0.716567f, 0.681718f, -0.147621f}, 
        {-0.500000f, 0.809017f, -0.309017f}, 
        {-0.525731f, 0.850651f, 0.000000f}, 
        {0.000000f, 0.850651f, -0.525731f}, 
        {-0.238856f, 0.864188f, -0.442863f}, 
        {0.000000f, 0.955423f, -0.295242f}, 
        {-0.262866f, 0.951056f, -0.162460f}, 
        {0.000000f, 1.000000f, 0.000000f}, 
        {0.000000f, 0.955423f, 0.295242f}, 
        {-0.262866f, 0.951056f, 0.162460f}, 
        {0.238856f, 0.864188f, 0.442863f}, 
        {0.262866f, 0.951056f, 0.162460f}, 
        {0.500000f, 0.809017f, 0.309017f}, 
        {0.238856f, 0.864188f, -0.442863f}, 
        {0.262866f, 0.951056f, -0.162460f}, 
        {0.500000f, 0.809017f, -0.309017f}, 
        {0.850651f, 0.525731f, 0.000000f}, 
        {0.716567f, 0.681718f, 0.147621f}, 
        {0.716567f, 0.681718f, -0.147621f}, 
        {0.525731f, 0.850651f, 0.000000f}, 
        {0.425325f, 0.688191f, 0.587785f}, 
        {0.864188f, 0.442863f, 0.238856f}, 
        {0.688191f, 0.587785f, 0.425325f}, 
        {0.809017f, 0.309017f, 0.500000f}, 
        {0.681718f, 0.147621f, 0.716567f}, 
        {0.587785f, 0.425325f, 0.688191f}, 
        {0.955423f, 0.295242f, 0.000000f}, 
        {1.000000f, 0.000000f, 0.000000f}, 
        {0.951056f, 0.162460f, 0.262866f}, 
        {0.850651f, -0.525731f, 0.000000f}, 
        {0.955423f, -0.295242f, 0.000000f}, 
        {0.864188f, -0.442863f, 0.238856f}, 
        {0.951056f, -0.162460f, 0.262866f}, 
        {0.809017f, -0.309017f, 0.500000f}, 
        {0.681718f, -0.147621f, 0.716567f}, 
        {0.850651f, 0.000000f, 0.525731f}, 
        {0.864188f, 0.442863f, -0.238856f}, 
        {0.809017f, 0.309017f, -0.500000f}, 
        {0.951056f, 0.162460f, -0.262866f}, 
        {0.525731f, 0.000000f, -0.850651f}, 
        {0.681718f, 0.147621f, -0.716567f}, 
        {0.681718f, -0.147621f, -0.716567f}, 
        {0.850651f, 0.000000f, -0.525731f}, 
        {0.809017f, -0.309017f, -0.500000f}, 
        {0.864188f, -0.442863f, -0.238856f}, 
        {0.951056f, -0.162460f, -0.262866f}, 
        {0.147621f, 0.716567f, -0.681718f}, 
        {0.309017f, 0.500000f, -0.809017f}, 
        {0.425325f, 0.688191f, -0.587785f}, 
        {0.442863f, 0.238856f, -0.864188f}, 
        {0.587785f, 0.425325f, -0.688191f}, 
        {0.688191f, 0.587785f, -0.425325f}, 
        {-0.147621f, 0.716567f, -0.681718f}, 
        {-0.309017f, 0.500000f, -0.809017f}, 
        {0.000000f, 0.525731f, -0.850651f}, 
        {-0.525731f, 0.000000f, -0.850651f}, 
        {-0.442863f, 0.238856f, -0.864188f}, 
        {-0.295242f, 0.000000f, -0.955423f}, 
        {-0.162460f, 0.262866f, -0.951056f}, 
        {0.000000f, 0.000000f, -1.000000f}, 
        {0.295242f, 0.000000f, -0.955423f}, 
        {0.162460f, 0.262866f, -0.951056f}, 
        {-0.442863f, -0.238856f, -0.864188f}, 
        {-0.309017f, -0.500000f, -0.809017f}, 
        {-0.162460f, -0.262866f, -0.951056f}, 
        {0.000000f, -0.850651f, -0.525731f}, 
        {-0.147621f, -0.716567f, -0.681718f}, 
        {0.147621f, -0.716567f, -0.681718f}, 
        {0.000000f, -0.525731f, -0.850651f}, 
        {0.309017f, -0.500000f, -0.809017f}, 
        {0.442863f, -0.238856f, -0.864188f}, 
        {0.162460f, -0.262866f, -0.951056f}, 
        {0.238856f, -0.864188f, -0.442863f}, 
        {0.500000f, -0.809017f, -0.309017f}, 
        {0.425325f, -0.688191f, -0.587785f}, 
        {0.716567f, -0.681718f, -0.147621f}, 
        {0.688191f, -0.587785f, -0.425325f}, 
        {0.587785f, -0.425325f, -0.688191f}, 
        {0.000000f, -0.955423f, -0.295242f}, 
        {0.000000f, -1.000000f, 0.000000f}, 
        {0.262866f, -0.951056f, -0.162460f}, 
        {0.000000f, -0.850651f, 0.525731f}, 
        {0.000000f, -0.955423f, 0.295242f}, 
        {0.238856f, -0.864188f, 0.442863f}, 
        {0.262866f, -0.951056f, 0.162460f}, 
        {0.500000f, -0.809017f, 0.309017f}, 
        {0.716567f, -0.681718f, 0.147621f}, 
        {0.525731f, -0.850651f, 0.000000f}, 
        {-0.238856f, -0.864188f, -0.442863f}, 
        {-0.500000f, -0.809017f, -0.309017f}, 
        {-0.262866f, -0.951056f, -0.162460f}, 
        {-0.850651f, -0.525731f, 0.000000f}, 
        {-0.716567f, -0.681718f, -0.147621f}, 
        {-0.716567f, -0.681718f, 0.147621f}, 
        {-0.525731f, -0.850651f, 0.000000f}, 
        {-0.500000f, -0.809017f, 0.309017f}, 
        {-0.238856f, -0.864188f, 0.442863f}, 
        {-0.262866f, -0.951056f, 0.162460f}, 
        {-0.864188f, -0.442863f, 0.238856f}, 
        {-0.809017f, -0.309017f, 0.500000f}, 
        {-0.688191f, -0.587785f, 0.425325f}, 
        {-0.681718f, -0.147621f, 0.716567f}, 
        {-0.442863f, -0.238856f, 0.864188f}, 
        {-0.587785f, -0.425325f, 0.688191f}, 
        {-0.309017f, -0.500000f, 0.809017f}, 
        {-0.147621f, -0.716567f, 0.681718f}, 
        {-0.425325f, -0.688191f, 0.587785f}, 
        {-0.162460f, -0.262866f, 0.951056f}, 
        {0.442863f, -0.238856f, 0.864188f}, 
        {0.162460f, -0.262866f, 0.951056f}, 
        {0.309017f, -0.500000f, 0.809017f}, 
        {0.147621f, -0.716567f, 0.681718f}, 
        {0.000000f, -0.525731f, 0.850651f}, 
        {0.425325f, -0.688191f, 0.587785f}, 
        {0.587785f, -0.425325f, 0.688191f}, 
        {0.688191f, -0.587785f, 0.425325f}, 
        {-0.955423f, 0.295242f, 0.000000f}, 
        {-0.951056f, 0.162460f, 0.262866f}, 
        {-1.000000f, 0.000000f, 0.000000f}, 
        {-0.850651f, 0.000000f, 0.525731f}, 
        {-0.955423f, -0.295242f, 0.000000f}, 
        {-0.951056f, -0.162460f, 0.262866f}, 
        {-0.864188f, 0.442863f, -0.238856f}, 
        {-0.951056f, 0.162460f, -0.262866f}, 
        {-0.809017f, 0.309017f, -0.500000f}, 
        {-0.864188f, -0.442863f, -0.238856f}, 
        {-0.951056f, -0.162460f, -0.262866f}, 
        {-0.809017f, -0.309017f, -0.500000f}, 
        {-0.681718f, 0.147621f, -0.716567f}, 
        {-0.681718f, -0.147621f, -0.716567f}, 
        {-0.850651f, 0.000000f, -0.525731f}, 
        {-0.688191f, 0.587785f, -0.425325f}, 
        {-0.587785f, 0.425325f, -0.688191f}, 
        {-0.425325f, 0.688191f, -0.587785f}, 
        {-0.425325f, -0.688191f, -0.587785f}, 
        {-0.587785f, -0.425325f, -0.688191f}, 
        {-0.688191f, -0.587785f, -0.425325f}
    };
}

class _AnimationInfo {
    String name;
    //int number; // Not the number of frames but the order.
    short start;
    short end;
    public _AnimationInfo() {
    }
    public _AnimationInfo(_AnimationInfo ai) {
        name = ai.name;
        start = ai.start;
        end = ai.end;
    }
}
/*
// Compressed vertex
class _md2Vertex {
   public final byte[] v = new byte[3];         // Unsigned position
   public byte normalIndex;                     // Unsigned normal vector index
};

// Texture coords
class _md2TexCoord
{
  public short s;
  public short t;
};

// Triangle info
class _md2Triangle
{
  public short[] vertex = new short[3];   // Unsigned vertex indices of the triangle
  public short[] st = new short[3];       // Unsigned tex. coord. indices
};

// Model frame
class _md2Frame
{
  public float[] scale = new float[3];            // scale factor
  public float[] translate = new float[3];        // translation vector
  public byte[] name = new byte[16];              // frame name
  public _md2Vertex[] verts;                      // list of frame's vertices
};
*/