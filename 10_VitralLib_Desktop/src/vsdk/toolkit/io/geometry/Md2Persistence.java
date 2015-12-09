/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vsdk.toolkit.io.geometry;

// Java basic classes
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

// VitralSDK classes
import vsdk.toolkit.environment.geometry.Md2Mesh;
import vsdk.toolkit.io.PersistenceElement;
import vsdk.toolkit.io.image.ImagePersistence;
import vsdk.toolkit.media.Image;
import vsdk.toolkit.media.RGBImage;


/**
 *
 * @author 
 */
public class Md2Persistence extends PersistenceElement {
    //private final Md2Mesh md2Mesh = new Md2Mesh();
    
    public Md2Persistence() {
    }
    
    // Returns true if loaded successfully.
    public final boolean read(String inFileName, String inTexture, Md2Mesh outMd2Mesh) throws IOException{
        int i,j,k,nVertPrimitive;
        byte[] b;
        byte[] nameB = new byte[64];
        String name;
        boolean triStrip;
        
        //InputStream is = new FileInputStream(new File(fileName));
        RandomAccessFile file = new RandomAccessFile(inFileName, "r");
        
        // Read the header.
        //readHeader(is);
        if(!readHeader(file, outMd2Mesh)) return false; // The file is not a md2 file version 8
        // Read the full paths of the skins.
        file.seek(outMd2Mesh.offsetSkins);
        for(i=0;i<outMd2Mesh.numSkins;++i){
            file.readFully (nameB);
            name = new String(nameB,"US-ASCII");
            name = name.trim();
            outMd2Mesh.skinNames.add(name);
        }
        // Read texture coordinates.
        outMd2Mesh.texCoords = new float[outMd2Mesh.numTexCoords*2];
        file.seek(outMd2Mesh.offsetTexCoords);
        for(i=0;i<outMd2Mesh.numTexCoords;++i){
            short coord;
            
            b = new byte[2];
            file.read(b);
            coord = (short)byteArray2signedShortLE(b,0);
            outMd2Mesh.texCoords[i*2]   = coord/(float)outMd2Mesh.skinWidth;
            file.read(b);
            coord = (short)byteArray2signedShortLE(b,0);
            outMd2Mesh.texCoords[i*2+1] = coord/(float)outMd2Mesh.skinHeight;
        }
        // Read triangles
        // Each triangle has three vertex indices and three tex. coord. indices.
        outMd2Mesh.triangles = new int[outMd2Mesh.numTriangles*2][3];
        file.seek(outMd2Mesh.offsetTriangles);
        for(i=0;i<outMd2Mesh.numTriangles;++i){
            int coord;
            
            b = new byte[2];
            for(j=0;j<3;++j){
                file.read(b);
                // This is because the number in the file is an unsigned short.
                coord = byteArray2signedShortLE(b,0) & 0xFFFF;
                outMd2Mesh.triangles[i*2][j] = coord;
            }
            for(j=0;j<3;++j){
                file.read(b);
                coord = byteArray2signedShortLE(b,0) & 0xFFFF;
                outMd2Mesh.triangles[i*2+1][j] = coord;  
            }
        }
        // Read frames.
        float scale[] = new float[3];
        float translate[] = new float[3];

        nameB = new byte[16];
        outMd2Mesh.frameVertices.ensureCapacity(outMd2Mesh.numFrames);
        outMd2Mesh.frameNormalIndices.ensureCapacity(outMd2Mesh.numFrames);
        file.seek(outMd2Mesh.offsetFrames);
        for(i=0;i<outMd2Mesh.numFrames;++i) {
            float[] frameVertices;
            short[] frameNormalIndices;
            short coord, normalIndex;
            
            b = new byte[4];
            for(j=0;j<3;++j) {
                file.read(b);
                scale[j] = byteArray2floatLE(b,0);
            }
            for(j=0;j<3;++j) {
                file.read(b);
                translate[j] = byteArray2floatLE(b,0);
            }
            file.readFully(nameB);
            name = new String(nameB,"US-ASCII");
            name = name.trim();
            outMd2Mesh.frameNames.add(name);
            outMd2Mesh.frameVertices.add(new float[outMd2Mesh.numVertices*3]);
            outMd2Mesh.frameNormalIndices.add(new short[outMd2Mesh.numVertices]);
            frameVertices = outMd2Mesh.frameVertices.get(i);
            frameNormalIndices = outMd2Mesh.frameNormalIndices.get(i);
            b = new byte[1];
            for(j=0;j<outMd2Mesh.numVertices;++j){
                for(k=0;k<3;++k) {
                    file.read(b);
                    coord = (short)(b[0] & 0xFF); //The number in the data is unsigned.
                    frameVertices[j*3+k] = coord*scale[k] + translate[k];                    
                }
                file.read(b);
                normalIndex = (short)(b[0] & 0xFF); //The number in the data is unsigned.
                frameNormalIndices[j] = normalIndex;
            }
        }
        // Read OpenGL commands.
        b = new byte[4];
        file.seek(outMd2Mesh.offsetGlCommands);
        file.read(b);
        nVertPrimitive = (int)byteArray2longLE(b,0);
        triStrip = true;
        if(nVertPrimitive<0){
            triStrip = false;
            nVertPrimitive = -nVertPrimitive;
        }
        while(nVertPrimitive!=0) {
            float[] texCoords;
            int[] vertIndices;

            if(triStrip){
                texCoords = new float[nVertPrimitive*2];
                outMd2Mesh.glCmdTexCoordsStrip.add(texCoords);
                vertIndices = new int[nVertPrimitive];
                outMd2Mesh.glCmdVertIndexStrip.add(vertIndices);
            }
            else{
                texCoords = new float[nVertPrimitive*2];
                outMd2Mesh.glCmdTexCoordsFan.add(texCoords);
                vertIndices = new int[nVertPrimitive];
                outMd2Mesh.glCmdVertIndexFan.add(vertIndices);                    
            }
            for(i=0;i<nVertPrimitive;++i){
                file.read(b);
                texCoords[i*2] = byteArray2floatLE(b,0);
                file.read(b);
                texCoords[i*2+1] = byteArray2floatLE(b,0);
                file.read(b);
                vertIndices[i] = (int)byteArray2longLE(b,0);
            }
            file.read(b);
            nVertPrimitive = (int)byteArray2longLE(b,0);
            triStrip = true;
            if(nVertPrimitive<0){
                triStrip = false;
                nVertPrimitive = -nVertPrimitive;
            }
        }
//        short temp=0,tempArr[] = new short[2];
//        String nameAnim="stand";
//        outMd2Mesh.returnStartEndAnim(temp,tempArr);
//        outMd2Mesh.returnStartEndAnim(nameAnim,tempArr);
        file.close();
        
        if(outMd2Mesh.numSkins == 0)
            outMd2Mesh.skins = new Image[1];
        else
            outMd2Mesh.skins = new Image[outMd2Mesh.numSkins];
        // For now, only one image.
        outMd2Mesh.skins[0] = loadImagefile(inTexture);
        return true;
    }
    
    // Returns false if the file is not a md2 file version 8.
    public boolean readHeader(RandomAccessFile file, Md2Mesh outMd2Mesh) throws IOException
    {
        byte[] b = new byte[4];
        
        file.read(b);
        //outMd2Mesh.ident = readIntLE(is);
        outMd2Mesh.ident = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.version = (int)byteArray2longLE(b,0);
        if(outMd2Mesh.ident != 844121161 || outMd2Mesh.version != 8) return false;
        file.read(b);
        outMd2Mesh.skinWidth = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.skinHeight = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.frameSize = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.numSkins = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.numVertices = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.numTexCoords = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.numTriangles = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.numGlCommands = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.numFrames = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.offsetSkins = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.offsetTexCoords = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.offsetTriangles = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.offsetFrames = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.offsetGlCommands = (int)byteArray2longLE(b,0);
        file.read(b);
        outMd2Mesh.offsetEnd = (int)byteArray2longLE(b,0);
        return true;
    }
    private static Image loadImagefile(String imageFilename)
    {
        RGBImage img;

        if ( imageFilename == null || imageFilename.length() < 1 ) {
            return null;
        }
        try {
            img = ImagePersistence.importRGB(new File(imageFilename));
        }
        catch (Exception e) {
            System.err.println("Error: could not read the image file \"" + imageFilename + "\".");
            System.err.println("Check you have access to that file from current working directory.");
            System.err.println(e);
            img = new RGBImage();
            img.init(64, 64);
            img.createTestPattern();
        }
        return img;
    }
}
