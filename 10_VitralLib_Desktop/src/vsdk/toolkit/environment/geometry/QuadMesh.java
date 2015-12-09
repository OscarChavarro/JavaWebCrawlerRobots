//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 12 2008 - Oscar Chavarro: Original base version                   =
//===========================================================================

package vsdk.toolkit.environment.geometry;

// Java basic classes
import java.util.ArrayList;

// VitralSDK classes
import vsdk.toolkit.common.Ray;
import vsdk.toolkit.common.Vertex;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.environment.scene.SimpleBody;

/**
This class represents a "basic" quad mesh. Its model is based in a set
of vertexes and triangles (the edges are not store explicitly, and there
can not be an edge not forming part of a quad).

As every model class or `Entity` in VSDK, this class only can represent
(store in memory) the mesh model. It doesn't provide persistence or rendering
functionality, as this could be found at `io` and `render` packages.
Nevertheles, this class will be highly coupled with both of those, so
making any change here will impact highly that code.

This class does not ensure nor impose data integrity, and this will be the 
sole responsability of the cooperating utilities and applications.

\todo  Document more this class (include samples and data structure diagrams)
*/
public class QuadMesh extends Surface {

//= Class attributes ========================================================

    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20080512L;

    // Basic mesh data model
    private String name = "default";

    // Consecutive triads of x/y/z point coordinates
    private double[] vertexPositions;

    // Consecutive triads of nx/ny/nz vertex normal coordinates
    private double[] vertexNormals;

    // Consecutive triads of nx/ny/nz vertex binormal coordinates (can be null)
    private double[] vertexBinormals;

    // Consecutive triads of nx/ny/nz vertex tangent coordinates (can be null)
    private double[] vertexTangents;

    // Consecutive triads of r/g/b vertex color components (can be null)
    private double[] vertexColors;

    // Consecutive pairs of u/v vertex texture coordinates (can be null)
    private double[] vertexUvs;

    private ArrayList<ArrayList<Integer>> incidentQuadsPerVertexArray;

    /// Consecutive quads of p0/p1/p2/p3 indices into the vertex arrays
    private int[] quadIndices;

    // Auxiliary components for data model
    //private Material[] materials;
    //private Image[] textures;

    // Auxiliary data structures for storage of parcial results and 
    // preprocessing
    private double[] minMax;
    private int selectedQuad;
    private SimpleBody boundingVolume;
    private GeometryIntersectionInformation lastInfo;
    private Ray lastRay;
    private TriangleMeshGroup triangleMeshGroupCache;

//= Basic class management methods ==========================================

    public QuadMesh() {
        lastInfo = new GeometryIntersectionInformation();
        lastRay = null;
        minMax = null;
        boundingVolume = null;
        triangleMeshGroupCache = null;

        vertexPositions = null;
        vertexNormals = null;
        vertexBinormals = null;
        vertexTangents = null;
        vertexColors = null;
        vertexUvs = null;
        quadIndices = null;
        incidentQuadsPerVertexArray = null;
    }

    public String getName() {
        return this.name;
    }

    public void getVertexAt(int i, Vertex vertex) {
        vertex.position.x = vertexPositions[3*i];
        vertex.position.y = vertexPositions[3*i+1];
        vertex.position.z = vertexPositions[3*i+2];
        vertex.normal.x = vertexNormals[3*i];
        vertex.normal.y = vertexNormals[3*i+1];
        vertex.normal.z = vertexNormals[3*i+2];
        vertex.binormal.x = vertexBinormals[3*i];
        vertex.binormal.y = vertexBinormals[3*i+1];
        vertex.binormal.z = vertexBinormals[3*i+2];
        vertex.tangent.x = vertexTangents[3*i];
        vertex.tangent.y = vertexTangents[3*i+1];
        vertex.tangent.z = vertexTangents[3*i+2];
        vertex.u = vertexUvs[2*i];
        vertex.v = vertexUvs[2*i+1];
    }

    public void setName(String name) {
        this.name = name;
    }

    public void initVertexPositionsArray(int n)
    {
        vertexPositions = new double[n*3];
        //vertexNormals = new double[n*3];
        //vertexBinormals = new double[n*3];
        //vertexTangents = new double[n*3];
        //vertexUvs = new double[n*2];
        //vertexColors = null;
        incidentQuadsPerVertexArray = new ArrayList<ArrayList<Integer>>();
    }

    public void initVertexColorsArray()
    {
        int n = getNumVertices();
        vertexColors = new double[n*3];
    }

    public void initVertexNormalsArray()
    {
        int n = getNumVertices();
        vertexNormals = new double[n*3];
    }

    /**
    This method provides a clear structured form of defining the mesh vertexes,
    but it is inefficient. Its use is discouraged for applications manipulating
    big meshes.
    @param vertexes
    */
    public void setVertexes(Vertex[] vertexes) {
        int n, i;

        n = vertexes.length;

        initVertexPositionsArray(n);
        initVertexNormalsArray();
        //initVertexBinormalsArray();
        //initVertexTangentsArray();

        for ( i = 0; i < n; i++ ) {
            vertexPositions[3*i] = vertexes[i].position.x;
            vertexPositions[3*i+1] = vertexes[i].position.y;
            vertexPositions[3*i+2] = vertexes[i].position.z;
            vertexNormals[3*i] = vertexes[i].normal.x;
            vertexNormals[3*i+1] = vertexes[i].normal.y;
            vertexNormals[3*i+2] = vertexes[i].normal.z;
            //vertexBinormals[3*i] = vertexes[i].binormal.x;
            //vertexBinormals[3*i+1] = vertexes[i].binormal.y;
            //vertexBinormals[3*i+2] = vertexes[i].binormal.z;
            //vertexTangents[3*i] = vertexes[i].tangent.x;
            //vertexTangents[3*i+1] = vertexes[i].tangent.y;
            //vertexTangents[3*i+2] = vertexes[i].tangent.z;
            //vertexUvs[2*i] = vertexes[i].u;
            //vertexUvs[2*i+1] = vertexes[i].v;
        }

        boundingVolume = null;
    }

    public void initQuadArrays(int n)
    {
        quadIndices = new int[n*4];
    }

    /**
    Given a vertex structure and an `i` position, this method copies
    the information from the structure in to the i-th vertex arrays position.
    PRE: 0 <= i < vertexPositions.length/3
    @param i
    @param vertex
    */
    public void setVertexAt(int i, Vertex vertex) {
        vertexPositions[3*i] = vertex.position.x;
        vertexPositions[3*i+1] = vertex.position.y;
        vertexPositions[3*i+2] = vertex.position.z;
        vertexNormals[3*i] = vertex.normal.x;
        vertexNormals[3*i+1] = vertex.normal.y;
        vertexNormals[3*i+2] = vertex.normal.z;
        vertexBinormals[3*i] = vertex.binormal.x;
        vertexBinormals[3*i+1] = vertex.binormal.y;
        vertexBinormals[3*i+2] = vertex.binormal.z;
        vertexTangents[3*i] = vertex.tangent.x;
        vertexTangents[3*i+1] = vertex.tangent.y;
        vertexTangents[3*i+2] = vertex.tangent.z;
        vertexUvs[2*i] = vertex.u;
        vertexUvs[2*i+1] = vertex.v;

        boundingVolume = null;
    }

    /**
    Given a triangle structure and an `i` position, this method copies
    the information from the structure in to the i-th triangle arrays position.
    PRE: 0 <= i < vertexPositions.length/3
    @param i
    @param p0
    @param p1
    @param p2
    @param p3
    */
    public void setQuadAt(int i, int p0, int p1, int p2, int p3) {
        quadIndices[4*i] = p0;
        quadIndices[4*i+1] = p1;
        quadIndices[4*i+2] = p2;
        quadIndices[4*i+3] = p2;

        boundingVolume = null;
    }

    public int getNumVertices()
    {
        if ( vertexPositions == null ) return 0;
        return vertexPositions.length/3;
    }

    public int getNumQuads()
    {
        if ( quadIndices == null ) return 0;
        return quadIndices.length/4;
    }

    public double[] getVertexPositions()
    {
        return vertexPositions;
    }

    public double[] getVertexNormals()
    {
        return vertexNormals;
    }

    public double[] getVertexBinormals()
    {
        return vertexBinormals;
    }

    public double[] getVertexTangents()
    {
        return vertexTangents;
    }

    public double[] getVertexColors()
    {
        return vertexColors;
    }

    public double[] getVertexUvs()
    {
        return vertexUvs;
    }

    public int[] getQuadIndices()
    {
        return quadIndices;
    }

//= Fundamental geometry operations methods =================================

    public void calculateNormals() {
        // TODO!
    }

    /** Needed for supplying the Geometry.getMinMax operation */
    private void calculateMinMaxPositions() {
        boundingVolume = null;
        if ( minMax == null ) {
            minMax = new double[6];

            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;
            double maxZ = -Double.MAX_VALUE;
            int i;

            for ( i = 0; i < getNumVertices(); i++ ) {
                double x = vertexPositions[3*i+0];
                double y = vertexPositions[3*i+1];
                double z = vertexPositions[3*i+2];

                if ( x < minX ) minX = x;
                if ( y < minY ) minY = y;
                if ( z < minZ ) minZ = z;
                if ( x > maxX ) maxX = x;
                if ( y > maxY ) maxY = y;
                if ( z > maxZ ) maxZ = z;
            }
            minMax[0] = minX;
            minMax[1] = minY;
            minMax[2] = minZ;
            minMax[3] = maxX;
            minMax[4] = maxY;
            minMax[5] = maxZ;
        }
    }

    /**
    Provides an object to text report convertion, optimized for human
    readability and debugging. Do not use for serialization or persistence
    purposes.
    @return a human readable representation of current QuadMesh information
    */
    @Override
    public String toString() {
        String msg;
        int i;
        double mm[];

        mm = getMinMax();
        Vector3D p;

        msg = "- QuadMesh ------------------------------------------------------------\n";
        msg += "  - Number of quads:" + getNumQuads() + "\n";
        msg += "  - Number of vertexes:" + getNumVertices() + "\n";
        p = new Vector3D(mm[0], mm[1], mm[2]);
        msg += "  - MINMAX: " + p;
        p = new Vector3D(mm[3], mm[4], mm[5]);
        msg += " - " + p + "\n";
        msg += "---------------------------------------------------------------------------\n";
        return msg;
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doIntersection.
    @param inOut_Ray
    @return true if given ray intersects current QuadMesh
    */
    @Override
    public boolean
    doIntersection(Ray inOut_Ray) {
        // TODO!
        return false;
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doExtraInformation.
    @param inT
    */
    @Override
    public void
    doExtraInformation(Ray inRay, double inT,
                                   GeometryIntersectionInformation outData) {
        // TODO!
    }

    /**
    @return INSIDE, OUTSIDE or LIMIT constant value
    */
    @Override
    public int doContainmentTest(Vector3D p, double distanceTolerance)
    {
        // TODO!
        return OUTSIDE;
    }

    /**
    Check the general interface contract in superclass method
    Geometry.getMinMax.
    @return a new 6 valued double array containing the coordinates of a min-max
    bounding box for current geometry.
    */
    @Override
    public double[] getMinMax() {
        if ( minMax == null ) {
            calculateMinMaxPositions();
        }
        return minMax;
    }

    // Todo!
    @Override
    public TriangleMeshGroup exportToTriangleMeshGroup()
    {
        /*
        if ( triangleMeshGroupCache == null ) {
            triangleMeshGroupCache = new TriangleMeshGroup();
            triangleMeshGroupCache.addMesh(this);
        }
        return triangleMeshGroupCache;*/
        return new TriangleMeshGroup();
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
