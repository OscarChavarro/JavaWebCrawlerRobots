//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - August 8 2005 - Gabriel Sarmiento / Lina Rojas: Original base version =
//= - May 2 2006 - Oscar Chavarro: quality check, doIntersection doc/test   =
//= - May 3 2006 - Oscar Chavarro: fixed doIntersection error when testing  =
//=       back facing triangles                                             =
//= - November 6 2006 - Oscar Chavarro: introduced bounding box and normal  =
//=       interpolation                                                     =
//= - November 13 2006 - Oscar Chavarro: re-structured and tested           =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [DACH2006] Dachille, Frank IX. Kaufman, Arie. "Incremental Triangle     =
//=     Voxelization", Center for Visual Computing and Department of        =
//=     Computer Science, State University of New York at Stony Brook, 2000 =
//===========================================================================

package vsdk.toolkit.environment.geometry;

// Java basic classes
import java.util.ArrayList;

// VitralSDK classes
import vsdk.toolkit.common.ArrayListOfInts;
import vsdk.toolkit.common.ArrayListOfDoubles;
import vsdk.toolkit.common.Triangle;
import vsdk.toolkit.common.Ray;
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.Vertex;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.media.Image;
import vsdk.toolkit.environment.Material;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.gui.ProgressMonitor;
import vsdk.toolkit.processing.ComputationalGeometry;

/**
This class represents a "basic" triangle mesh. Its model is based in a set
of vertexes and triangles (the edges are not store explicitly, and there
can not be an edge not forming part of a triangle).

This basic triangle mesh model can be associated with one and only one
material (this could change in future), but can have multiple textures,
and each texture can be mapped to a different set of triangles.

As every model class or `Entity` in VSDK, this class only can represent
(store in memory) the mesh model. It doesn't provide persistence or rendering
functionality, as this could be found at `io` and `render` packages.
Nevertheles, this class will be highly coupled with both of those, so
making any change here will impact highly that code.

This class does not ensure nor impose data integrity, and this will be the 
sole responsability of the cooperating utilities and applications.

\todo  Document more this class (include samples and data structure diagrams)
\todo  Generalize the material usage model, to conform similarly to current
      texture usage (i.e. allow multiple materials per mesh)
\todo  Make sure this is always using good names with complete words on it
      (rename methods and attributes)
\todo  Extend the model to allow dangling edges
*/
public class TriangleMesh extends Surface {

//= Class attributes ========================================================

    /// Check the general attribute description in superclass Entity.
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")

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

    // Consecutive values of boolean. If true, then vertex is selected.
    private boolean[] vertexSelections;

    // Consecutive pairs of u/v vertex texture coordinates (can be null)
    private double[] vertexUvs;

    private ArrayList<ArrayList<Integer>> incidentTrianglesPerVertexArray;

    /// Consecutive triads of p0/p1/p2 indices into the vertex arrays
    private int[] triangleIndices;
    /// Consecutive triads of nx/ny/nz for normal vectors, one per triangle
    private double[] triangleNormals;

    // Auxiliary components for data model
    private Material[] materials;
    private Image[] textures;

    /**
    textureRanges is a 2D array which contents mappings between the
    `triangles` and `textures` sets. Each pair 
    <textureRanges[i][0], textureRanges[i][1]> means that the triangles
    from textureRanges[i-1][0] to textureRanges[i][0] (from triangle 0 
    when i = 0), are associated with the texture textureRanges[i][1]-1
    (or no texture for unspecified range or when textureRanges[i][1]
    contains a value out of textures array bounds or a value of 0).
    */
    private int[][] textureRanges;

    /**
    materialRanges is a 2D array which contents mappings between the
    `triangles` and `materials` sets. Each pair 
    <materialRanges[i][0], materialRanges[i][1]> means that the triangles
    from materialRanges[i-1][0] to materialRanges[i][0] (from triangle 0 
    when i = 0), are associated with the material materialRanges[i][1]
    (or no material for unspecified range or when materialRanges[i][1]
    contains a value out of materials array bounds).
    */
    private int[][] materialRanges;

    // Auxiliary data structures for storage of parcial results and 
    // preprocessing
    private double[] minMax;
    private int selectedTriangle;
    private SimpleBody boundingVolume;
    private GeometryIntersectionInformation lastInfo;
    private Ray lastRay;
    private TriangleMeshGroup triangleMeshGroupCache;

//= Basic class management methods ==========================================

    public TriangleMesh() {
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
        vertexSelections = null;
        triangleIndices = null;
        triangleNormals = null;
        incidentTrianglesPerVertexArray = null;
    }

    private double[] cloneDoubleArray(double in[])
    {
        double[] out = null;

        if ( in != null ) {
            out = new double[in.length];
            int i;
            for ( i = 0; i < in.length; i++ ) {
                out[i] = in[i];
            }
        }

        return out;
    }

    private boolean[] cloneBooleanArray(boolean in[])
    {
        boolean[] out = null;

        if ( in != null ) {
            out = new boolean[in.length];
            int i;
            for ( i = 0; i < in.length; i++ ) {
                out[i] = in[i];
            }
        }

        return out;
    }

    private int[] cloneIntArray(int in[])
    {
        int[] out = null;

        if ( in != null ) {
            out = new int[in.length];
            int i;
            for ( i = 0; i < in.length; i++ ) {
                out[i] = in[i];
            }
        }

        return out;
    }

    /**
    \todo  copy full structure!
    @return a new TriangleMesh containing an exact copy of the original
    @throws java.lang.CloneNotSupportedException
    */
    @Override
    public TriangleMesh clone() throws CloneNotSupportedException
    {
        super.clone();
        TriangleMesh other = new TriangleMesh();

        other.name = this.name;
        other.vertexPositions = cloneDoubleArray(this.vertexPositions);
        other.vertexPositions = cloneDoubleArray(this.vertexPositions);
        other.vertexNormals = cloneDoubleArray(this.vertexNormals);
        other.vertexBinormals = cloneDoubleArray(this.vertexBinormals);
        other.vertexTangents = cloneDoubleArray(this.vertexTangents);
        other.vertexColors = cloneDoubleArray(this.vertexColors);
        other.vertexUvs = cloneDoubleArray(this.vertexUvs);
        other.triangleNormals = cloneDoubleArray(this.triangleNormals);
        other.minMax = cloneDoubleArray(this.minMax);

        other. vertexSelections = cloneBooleanArray(this.vertexSelections);
        other.triangleIndices = cloneIntArray(this.triangleIndices);
        other.selectedTriangle = this.selectedTriangle;

        // Pending!
        other.incidentTrianglesPerVertexArray = null;
        other.materials = null;
        other.textures = null;
        other.textureRanges = null;
        other.materialRanges = null;
        other.boundingVolume = null;
        other.lastInfo = null;
        other.lastRay = null;
        other.triangleMeshGroupCache = null;
         
/*
        ArrayList<ArrayList<Integer>> incidentTrianglesPerVertexArray;
        Material[] materials;
        Image[] textures;
        int[][] textureRanges;
        int[][] materialRanges;
        int selectedTriangle;
        SimpleBody boundingVolume;
        GeometryIntersectionInformation lastInfo;
        Ray lastRay;
        TriangleMeshGroup triangleMeshGroupCache;
*/
        return other;
    }

    public String getName() {
        return this.name;
    }

    public void getVertexAt(int i, Vertex vertex) {
        vertex.position.x = vertexPositions[3*i];
        vertex.position.y = vertexPositions[3*i+1];
        vertex.position.z = vertexPositions[3*i+2];
        if ( vertexNormals != null ) {
            vertex.normal.x = vertexNormals[3*i];
            vertex.normal.y = vertexNormals[3*i+1];
            vertex.normal.z = vertexNormals[3*i+2];
        }
        if ( vertexBinormals != null ) {
            vertex.binormal.x = vertexBinormals[3*i];
            vertex.binormal.y = vertexBinormals[3*i+1];
            vertex.binormal.z = vertexBinormals[3*i+2];
        }
        if ( vertexTangents != null ) {
            vertex.tangent.x = vertexTangents[3*i];
            vertex.tangent.y = vertexTangents[3*i+1];
            vertex.tangent.z = vertexTangents[3*i+2];
        }
        if ( vertexUvs != null ) {
            vertex.u = vertexUvs[2*i];
            vertex.v = vertexUvs[2*i+1];
        }
    }

    public Material[] getMaterials() {
        return this.materials;
    }

    public Image[] getTextures() {
        return this.textures;
    }

    public Image getTextureAt(int index) {
        return this.textures[index];
    }

    public void setName(String name) {
        this.name = name;
    }

    public void initVertexPositionsArray(int n)
    {
        vertexPositions = new double[n*3];
    }

    public void initVertexNormalsArray()
    {
        int n = vertexPositions.length / 3;
        if ( vertexNormals == null ||
             vertexNormals.length != vertexPositions.length ) {
            vertexNormals = new double[n*3];
        }
    }

    public void initVertexBinormalsArray()
    {
        int n = vertexPositions.length / 3;
        if ( vertexBinormals == null ||
             vertexBinormals.length != vertexPositions.length ) {
            vertexBinormals = new double[n*3];
        }
    }

    public void initVertexTangentsArray()
    {
        int n = vertexPositions.length / 3;
        if ( vertexTangents == null ||
             vertexTangents.length != vertexPositions.length ) {
            vertexTangents = new double[n*3];
        }
    }

    public void initVertexColorsArray()
    {
        int n = vertexPositions.length / 3;
        if ( vertexColors == null ||
             vertexColors.length != vertexPositions.length ) {
            vertexColors = new double[n*3];
        }
    }

    public void initVertexUvsArray()
    {
        int n = vertexPositions.length / 3;
        if ( vertexUvs == null ||
             vertexUvs.length/2 != vertexPositions.length/3 ) {
            vertexUvs = new double[n*2];
        }
    }

    public void initIncidentTrianglesPerVertexArray() {
        incidentTrianglesPerVertexArray = new ArrayList<ArrayList<Integer>>();
    }

    public void detachColors()
    {
        vertexColors = null;
    }

    public void detachNormals()
    {
        vertexNormals = null;
    }

    public void detachUvs()
    {
        vertexUvs = null;
    }

    /**
    This method provides a clear structured form of defining the mesh vertexes,
    but it is inefficient. Its use is discouraged for applications manipulating
    big meshes.
    @param vertexes
    @param withNormals
    @param withBinormals
    @param withTangents
    @param withUvs
    */
    public void setVertexes(Vertex[] vertexes,
                            boolean withNormals, boolean withBinormals,
                            boolean withTangents, boolean withUvs) {
        int n, i;

        n = vertexes.length;

        initVertexPositionsArray(n);
        if ( withNormals ) {
            initVertexNormalsArray();
        }
        if ( withBinormals ) {
            initVertexBinormalsArray();
        }
        if ( withTangents ) {
            initVertexTangentsArray();
        }
        if ( withUvs ) {
            initVertexUvsArray();
        }

        for ( i = 0; i < n; i++ ) {
            vertexPositions[3*i] = vertexes[i].position.x;
            vertexPositions[3*i+1] = vertexes[i].position.y;
            vertexPositions[3*i+2] = vertexes[i].position.z;
            if ( vertexNormals != null ) {
                vertexNormals[3*i] = vertexes[i].normal.x;
                vertexNormals[3*i+1] = vertexes[i].normal.y;
                vertexNormals[3*i+2] = vertexes[i].normal.z;
            }
            if ( vertexBinormals != null ) {
                vertexBinormals[3*i] = vertexes[i].binormal.x;
                vertexBinormals[3*i+1] = vertexes[i].binormal.y;
                vertexBinormals[3*i+2] = vertexes[i].binormal.z;
            }
            if ( vertexTangents != null ) {
                vertexTangents[3*i] = vertexes[i].tangent.x;
                vertexTangents[3*i+1] = vertexes[i].tangent.y;
                vertexTangents[3*i+2] = vertexes[i].tangent.z;
            }
            if ( vertexUvs != null ) {
                vertexUvs[2*i] = vertexes[i].u;
                vertexUvs[2*i+1] = vertexes[i].v;
            }
        }

        boundingVolume = null;
    }

    public void initTriangleArrays(int n)
    {
        triangleIndices = new int[n*3];
        triangleNormals = new double [n*3];
    }

    /**
    This method provides a clear structured form of defining the mesh triangles,
    but it is inefficient. Its use is discouraged for applications manipulating
    big meshes.
    @param triangles
    */
    public void setTriangles(Triangle[] triangles) {
        int n, i;

        n = triangles.length;

        initTriangleArrays(n);

        for ( i = 0; i < n; i++ ) {
            triangleIndices[3*i] = triangles[i].p0;
            triangleIndices[3*i+1] = triangles[i].p1;
            triangleIndices[3*i+2] = triangles[i].p2;
            if ( triangleNormals != null ) {
                triangleNormals[3*i] = triangles[i].normal.x;
                triangleNormals[3*i+1] = triangles[i].normal.y;
                triangleNormals[3*i+2] = triangles[i].normal.z;
            }
        }

        boundingVolume = null;
    }

    public void setTextures(Image[] textures) {
        this.textures = textures;
    }

    public void setMaterials(Material[] materials) {
        this.materials = materials;
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
    @param triangle
    */
    public void setTriangleAt(int i, Triangle triangle) {
        triangleIndices[3*i] = triangle.p0;
        triangleIndices[3*i+1] = triangle.p1;
        triangleIndices[3*i+2] = triangle.p2;
        if ( triangleNormals != null ) {
            triangleNormals[3*i] = triangle.normal.x;
            triangleNormals[3*i+1] = triangle.normal.y;
            triangleNormals[3*i+2] = triangle.normal.z;
        }

        boundingVolume = null;
    }

    public int getNumVertices()
    {
        if ( vertexPositions == null ) return 0;
        return vertexPositions.length/3;
    }

    public int getNumTriangles()
    {
        if ( triangleIndices == null ) return 0;
        return triangleIndices.length/3;
    }

    public boolean[] getVertexSelections()
    {
        if ( vertexSelections == null ||
             vertexSelections.length != (vertexPositions.length/3) ) {
            vertexSelections = new boolean[vertexPositions.length/3];
            int i;
            for ( i = 0; i < vertexSelections.length; i++ ) {
                vertexSelections[i] = false;
            }
        }
        return vertexSelections;
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

    public int[] getTriangleIndexes()
    {
        return triangleIndices;
    }

    public double[] getTriangleNormals()
    {
        return triangleNormals;
    }

    public void setTextureAt(int index, Image image) {
        this.textures[index] = image;
        boundingVolume = null;
    }

//= Methods for managing textureRanges ======================================

    public int[][] getTextureRanges() {
        return textureRanges;
    }

    /**
    Note this always returns an array with two (2) integers: the first one
    is an index to `triangles` array, the second one is an index to the
    `textures` array.
    @param spanRange
    @return an integer array for textures ranges indexes
    */
    public int[] getTextureRangeAt(int spanRange) {
        return textureRanges[spanRange];
    }

    public void setTextureRanges(int ranges[][]) {
        textureRanges = ranges;
    }

//= Methods for managing materialRanges =====================================

    public int[][] getMaterialRanges() {
        return materialRanges;
    }

    /**
    Note this always returns an array with two (2) integers: the first one
    is an index to `triangles` array, the second one is an index to the
    `materials` array.
    @param spanRange
    @return a integer array with material ranges indexes
    */
    public int[] getMaterialRangeAt(int spanRange) {
        return materialRanges[spanRange];
    }

    public void setMaterialRanges(int ranges[][]) {
        materialRanges = ranges;
    }

//= Fundamental geometry operations methods =================================

    public void calculateNormals() {
        boundingVolume = null;
        Vector3D tn;
        int i, j;

        Vector3D v1 = new Vector3D();
        Vector3D v2 = new Vector3D();
        Vector3D v3 = new Vector3D();

        for ( i = 0; i < getNumTriangles(); i++ ) {
            v1.x = vertexPositions[3*triangleIndices[3*i+0]+0];
            v1.y = vertexPositions[3*triangleIndices[3*i+0]+1];
            v1.z = vertexPositions[3*triangleIndices[3*i+0]+2];
            v2.x = vertexPositions[3*triangleIndices[3*i+1]+0];
            v2.y = vertexPositions[3*triangleIndices[3*i+1]+1];
            v2.z = vertexPositions[3*triangleIndices[3*i+1]+2];
            v3.x = vertexPositions[3*triangleIndices[3*i+2]+0];
            v3.y = vertexPositions[3*triangleIndices[3*i+2]+1];
            v3.z = vertexPositions[3*triangleIndices[3*i+2]+2];

            double ax = v2.x - v1.x;
            double ay = v2.y - v1.y;
            double az = v2.z - v1.z;

            double bx = v3.x - v2.x;
            double by = v3.y - v2.y;
            double bz = v3.z - v2.z;

            Vector3D a = new Vector3D(ax, ay, az);
            Vector3D b = new Vector3D(bx, by, bz);

            a.normalize();
            b.normalize();

            tn = a.crossProduct(b);
            tn.normalize();
            triangleNormals[3*i] = tn.x;
            triangleNormals[3*i+1] = tn.y;
            triangleNormals[3*i+2] = tn.z;
        }

        initIncidentTrianglesPerVertexArray();
        for ( i = 0; i < getNumVertices(); i++ ) {
            incidentTrianglesPerVertexArray.add(new ArrayList<Integer>());
        }

        for ( i = 0; i < getNumTriangles(); i++ ) {
            incidentTrianglesPerVertexArray.get(triangleIndices[3*i+0]).add(i);
            incidentTrianglesPerVertexArray.get(triangleIndices[3*i+1]).add(i);
            incidentTrianglesPerVertexArray.get(triangleIndices[3*i+2]).add(i);
        }

        Vector3D n = new Vector3D();

        initVertexNormalsArray();

        for ( i = 0; i < getNumVertices(); i++ ) {
            n.x = n.y = n.z = 0.0;
            for ( j = 0; j < incidentTrianglesPerVertexArray.get(i).size(); j++ ) {
                int ii;
                ii = incidentTrianglesPerVertexArray.get(i).get(j);
                n.x += triangleNormals[3*ii+0];
                n.y += triangleNormals[3*ii+1];
                n.z += triangleNormals[3*ii+2];
            }
            n.normalize();
            vertexNormals[3*i] = n.x;
            vertexNormals[3*i+1] = n.y;
            vertexNormals[3*i+2] = n.z;
        }
    }

    /**
    In the triangle meshes the vector normal directions must be consistent with
    the triangle normal, which direction depends on triangle order (clockwise
    or counterclockwise).

    This method checks for inverted normals, and invert them to make it
    consistent with triangle orientation order.

    Note that this methods recalculate triangle surface normals.
    */
    public void reorientateNormals()
    {
        int i;
        Vector3D a, b, c;
        Vector3D u, v, n, an, bn, cn;

        a = new Vector3D();
        b = new Vector3D();
        c = new Vector3D();
        an = new Vector3D();
        bn = new Vector3D();
        cn = new Vector3D();
        for ( i = 0; i < getNumTriangles(); i++ ) {

            a.x = vertexPositions[3*triangleIndices[3*i]+0];
            a.y = vertexPositions[3*triangleIndices[3*i]+1];
            a.z = vertexPositions[3*triangleIndices[3*i]+2];
            b.x = vertexPositions[3*triangleIndices[3*i+1]+0];
            b.y = vertexPositions[3*triangleIndices[3*i+1]+1];
            b.z = vertexPositions[3*triangleIndices[3*i+1]+2];
            c.x = vertexPositions[3*triangleIndices[3*i+2]+0];
            c.y = vertexPositions[3*triangleIndices[3*i+2]+1];
            c.z = vertexPositions[3*triangleIndices[3*i+2]+2];

            an.x = vertexNormals[3*triangleIndices[3*i]+0];
            an.y = vertexNormals[3*triangleIndices[3*i]+1];
            an.z = vertexNormals[3*triangleIndices[3*i]+2];
            bn.x = vertexNormals[3*triangleIndices[3*i+1]+0];
            bn.y = vertexNormals[3*triangleIndices[3*i+1]+1];
            bn.z = vertexNormals[3*triangleIndices[3*i+1]+2];
            cn.x = vertexNormals[3*triangleIndices[3*i+2]+0];
            cn.y = vertexNormals[3*triangleIndices[3*i+2]+1];
            cn.z = vertexNormals[3*triangleIndices[3*i+2]+2];

            u = b.substract(a);
            v = c.substract(a);
            n = u.crossProduct(v);
            n.normalize();
            if ( triangleNormals != null ) {
                triangleNormals[3*i+0] = n.x;
                triangleNormals[3*i+1] = n.y;
                triangleNormals[3*i+2] = n.z;
            }
            if ( n.dotProduct(an) < 0 ) {
                an = an.multiply(-1);
                vertexNormals[3*triangleIndices[3*i]+0] = an.x;
                vertexNormals[3*triangleIndices[3*i]+1] = an.y;
                vertexNormals[3*triangleIndices[3*i]+2] = an.z;
            }
            if ( n.dotProduct(bn) < 0 ) {
                bn = bn.multiply(-1);
                vertexNormals[3*triangleIndices[3*i+1]+0] = bn.x;
                vertexNormals[3*triangleIndices[3*i+1]+1] = bn.y;
                vertexNormals[3*triangleIndices[3*i+1]+2] = bn.z;
            }
            if ( n.dotProduct(cn) < 0 ) {
                cn = cn.multiply(-1);
                vertexNormals[3*triangleIndices[3*i+2]+0] = cn.x;
                vertexNormals[3*triangleIndices[3*i+2]+1] = cn.y;
                vertexNormals[3*triangleIndices[3*i+2]+2] = cn.z;
            }
        }
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
    This method is supposed to be a friend of TriangleMesh related objects.
    The method is used to query the last intersected triangle, after a
    positive called to the doIntersection method.
    @return selected triangle
    */
    public int doIntersectionInformation() {
        return selectedTriangle;
    }

    /**
    Provides an object to text report convertion, optimized for human
    readability and debugging. Do not use for serialization or persistence
    purposes.
    @return human readable representation of current TriangleMesh information
    */
    @Override
    public String toString() {
        String msg;
        int i;
        double mm[];

        mm = getMinMax();
        Vector3D p;

        msg = "- TriangleMesh ------------------------------------------------------------\n";
        msg += "  - Number of triangles:" + getNumTriangles() + "\n";
        msg += "  - Number of vertexes:" + getNumVertices() + "\n";
        p = new Vector3D(mm[0], mm[1], mm[2]);
        msg += "  - MINMAX: " + p;
        p = new Vector3D(mm[3], mm[4], mm[5]);
        msg += " - " + p + "\n";
        if ( materials == null ) {
            msg += "  - No materials available!\n";
          }
          else {
            msg += "  - " + materials.length + " materials\n";
        }

        if ( materialRanges == null ) {
            msg += "  - No material ranges association table available!\n";
        }
        else {
            msg += "  - " + materialRanges.length + " material spans:\n";
            for ( i = 0; i < materialRanges.length; i++ ) {
                msg += "    . " + materialRanges[i][0] + " -> " + materialRanges[i][1] + "\n";
            }
        }

        if ( textures == null ) {
            msg += "  - No textures available!\n";
          }
          else {
            msg += "  - " + textures.length + " textures\n";
        }

        if ( textureRanges == null ) {
            msg += "  - No texture ranges association table available!\n";
        }
        else {
            msg += "  - " + textureRanges.length + " texture spans:\n";
            for ( i = 0; i < textureRanges.length; i++ ) {
                msg += "    . " + textureRanges[i][0] + " -> " + textureRanges[i][1] + "\n";
            }
        }

        msg += "---------------------------------------------------------------------------\n";
        return msg;
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doIntersection.
    @param inOut_Ray
    @return true if given ray intersects current TriangleMesh
    */
    @Override
    public boolean
    doIntersection(Ray inOut_Ray) {
        int i;                // Index for iterating triangles
        boolean intersection; // true if intersection founded
        double min_t;         // Shortest distance founded so far
        Vector3D v0, v1, v2;  // Positions of the three triangle points
        Vector3D p;           // Point of intersection between ray and plane
        Vector3D n;           // Normal at point of intersection
        Ray myRay = new Ray(inOut_Ray);

        //-----------------------------------------------------------------
        // Bounding volume check
        if ( boundingVolume == null ) {
            double[] mm = getMinMax();
            Vector3D size, center;
            size = new Vector3D(mm[3]-mm[0], mm[4]-mm[1], mm[5]-mm[2]);
            center = new Vector3D((mm[3]+mm[0])/2,
                                  (mm[4]+mm[1])/2,
                                  (mm[5]+mm[2])/2);
            boundingVolume = new SimpleBody();
            boundingVolume.setPosition(center);
            boundingVolume.setGeometry(new Box(size));
        }
        if ( !boundingVolume.doIntersection(inOut_Ray) ) {
            return false;
        }

        //-----------------------------------------------------------------
        // Initialization values for search algorithm
        min_t = Double.MAX_VALUE;
        intersection = false;
        selectedTriangle = 0;
        p = new Vector3D();
        n = new Vector3D();

        // For each triangle in the mesh ...
        int nt = getNumTriangles();
        v0 = new Vector3D();
        v1 = new Vector3D();
        v2 = new Vector3D();
        for ( i = 0; i < nt; i++ ) {
            // The Triangle i has vertices <v0, v1, v2>
            v0.x = vertexPositions[3*triangleIndices[3*i+0]+0];
            v0.y = vertexPositions[3*triangleIndices[3*i+0]+1];
            v0.z = vertexPositions[3*triangleIndices[3*i+0]+2];
            v1.x = vertexPositions[3*triangleIndices[3*i+1]+0];
            v1.y = vertexPositions[3*triangleIndices[3*i+1]+1];
            v1.z = vertexPositions[3*triangleIndices[3*i+1]+2];
            v2.x = vertexPositions[3*triangleIndices[3*i+2]+0];
            v2.y = vertexPositions[3*triangleIndices[3*i+2]+1];
            v2.z = vertexPositions[3*triangleIndices[3*i+2]+2];

            if ( ComputationalGeometry.doIntersectionWithTriangle(myRay, v0, v1, v2, p, n) ) {
                if ( myRay.t < min_t ) {
                    lastInfo.p = p;
                    lastInfo.n = n;
                    inOut_Ray.t = myRay.t;
                    lastRay = inOut_Ray;
                    min_t = myRay.t;
                    selectedTriangle = i;
                    intersection = true;
                }
            }
        }
        return intersection;
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
        //-----------------------------------------------------------------
        Vector3D p0, p1, p2;  // Positions of the three triangle points
        Vector3D n0, n1, n2;  // Normals of the three triangle points
        double u0, v0, u1, v1, u2, v2; // Texture coordinates

        p0 = new Vector3D();
        p1 = new Vector3D();
        p2 = new Vector3D();
        n0 = new Vector3D();
        n1 = new Vector3D();
        n2 = new Vector3D();

        //if ( withInterpolation ) {
            p0.x = vertexPositions[3*triangleIndices[3*selectedTriangle+0]+0];
            p0.y = vertexPositions[3*triangleIndices[3*selectedTriangle+0]+1];
            p0.z = vertexPositions[3*triangleIndices[3*selectedTriangle+0]+2];
            p1.x = vertexPositions[3*triangleIndices[3*selectedTriangle+1]+0];
            p1.y = vertexPositions[3*triangleIndices[3*selectedTriangle+1]+1];
            p1.z = vertexPositions[3*triangleIndices[3*selectedTriangle+1]+2];
            p2.x = vertexPositions[3*triangleIndices[3*selectedTriangle+2]+0];
            p2.y = vertexPositions[3*triangleIndices[3*selectedTriangle+2]+1];
            p2.z = vertexPositions[3*triangleIndices[3*selectedTriangle+2]+2];

            // Obtain barycentric coordinates for point p
            // Method taken from wikipedia
            double A, B, C, D, E, F, G, H, I;
            double lambda0, lambda1, lambda2;

            A = p0.x - p2.x;
            B = p1.x - p2.x;
            C = p2.x - lastInfo.p.x;
            D = p0.y - p2.y;
            E = p1.y - p2.y;
            F = p2.y - lastInfo.p.y;
            G = p0.z - p2.z;
            H = p1.z - p2.z;
            I = p2.z - lastInfo.p.z;

            // Point interpolation of three vertex positions
            lambda0 = (B*(F+I)-C*(E+H))/(A*(E+H)-B*(D+G));
            lambda1 = (A*(F+I)-C*(D+G))/(B*(D+G)-A*(E+H));
            lambda2 = 1-lambda0-lambda1;

            // Normal interpolation
            n0.x = vertexNormals[3*triangleIndices[3*selectedTriangle+0]+0];
            n0.y = vertexNormals[3*triangleIndices[3*selectedTriangle+0]+1];
            n0.z = vertexNormals[3*triangleIndices[3*selectedTriangle+0]+2];
            n1.x = vertexNormals[3*triangleIndices[3*selectedTriangle+1]+0];
            n1.y = vertexNormals[3*triangleIndices[3*selectedTriangle+1]+1];
            n1.z = vertexNormals[3*triangleIndices[3*selectedTriangle+1]+2];
            n2.x = vertexNormals[3*triangleIndices[3*selectedTriangle+2]+0];
            n2.y = vertexNormals[3*triangleIndices[3*selectedTriangle+2]+1];
            n2.z = vertexNormals[3*triangleIndices[3*selectedTriangle+2]+2];
            lastInfo.n = n0.multiply(lambda0).
                add(n1.multiply(lambda1).
                add(n2.multiply(lambda2)));

            // Texture map coordinates interpolation
            u0 = vertexUvs[2*triangleIndices[3*selectedTriangle+0]+0];
            v0 = vertexUvs[2*triangleIndices[3*selectedTriangle+0]+1];
            u1 = vertexUvs[2*triangleIndices[3*selectedTriangle+1]+0];
            v1 = vertexUvs[2*triangleIndices[3*selectedTriangle+1]+1];
            u2 = vertexUvs[2*triangleIndices[3*selectedTriangle+2]+0];
            v2 = vertexUvs[2*triangleIndices[3*selectedTriangle+2]+1];
            lastInfo.u = u0*lambda0 + u1*lambda1 + u2*lambda2;
            lastInfo.v = v0*lambda0 + v1*lambda1 + v2*lambda2;
        //}

        lastInfo.n.normalize();

        // Normal is always pointed "outwards" with respect to 
        // the triangle (this manages the issue of back-facing
        // normals)
        if ( lastInfo.n.dotProduct(lastRay.direction) >= 0 ) {
            lastInfo.n = lastInfo.n.multiply(-1);
        }

        //-----------------------------------------------------------------
        outData.clone(lastInfo);

        //-----------------------------------------------------------------
        if ( materials != null ) {
            outData.material = materials[0];
        }
        if ( materialRanges != null ) {
            for ( int i = 0; i < materialRanges.length-1 ; i++ ) {
                if ( selectedTriangle >= materialRanges[i][0] &&
                     selectedTriangle < materialRanges[i+1][0] ) {
                    outData.material = materials[materialRanges[i+1][1]];
                    break;
                }
            }
        }
        outData.texture = null;
        if ( textureRanges != null ) {
            for ( int i = 0; i < textureRanges.length-1 ; i++ ) {
                if ( selectedTriangle >= textureRanges[i][0] &&
                     selectedTriangle < textureRanges[i+1][0] ) {
                    outData.texture = textures[textureRanges[i+1][1]-1];
                    break;
                }
            }
        }
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doContainmentTest.
    \todo  Check efficiency for this implementation. Note that for the
    special application of volume rendering generation, it is better
    to provide another method, to add voxels after a path following
    over the line.
    @return INSIDE, OUTSIDE or LIMIT constant value
    */
    @Override
    public int doContainmentTest(Vector3D p, double distanceTolerance)
    {
        int i;
        int status;
        Vector3D p0, p1, p2;

        p0 = new Vector3D();
        p1 = new Vector3D();
        p2 = new Vector3D();

        for ( i = 0; i < getNumTriangles(); i++ ) {
            p0.x = vertexPositions[3*triangleIndices[3*i+0]+0];
            p0.y = vertexPositions[3*triangleIndices[3*i+0]+1];
            p0.z = vertexPositions[3*triangleIndices[3*i+0]+2];
            p1.x = vertexPositions[3*triangleIndices[3*i+1]+0];
            p1.y = vertexPositions[3*triangleIndices[3*i+1]+1];
            p1.z = vertexPositions[3*triangleIndices[3*i+1]+2];
            p2.x = vertexPositions[3*triangleIndices[3*i+2]+0];
            p2.y = vertexPositions[3*triangleIndices[3*i+2]+1];
            p2.z = vertexPositions[3*triangleIndices[3*i+2]+2];
            status = ComputationalGeometry.triangleContainmentTest(
                p0, p1, p2, p, distanceTolerance);
            if ( status != OUTSIDE ) {
                return LIMIT;
            }
        }

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

    /**
    Check the general interface contract in superclass method
    Geometry.doVoxelization.

    Current method follows the voxelization algorithm strategy proposed
    in [DACH2000], but actual implementation only accounts for binary voxels.
    It is spected that with few changes, this algorithm manages the scalar
    (multivalued) voxel case for antialiased voxelization.
    */
    @Override
    public void
    doVoxelization(VoxelVolume vv, Matrix4x4 M, ProgressMonitor reporter)
    {
        // The `*Geom` variables are in geometry space
        Vector3D p0Geom, p1Geom, p2Geom;
        // The `*Volume` variables are in voxel space
        Vector3D p0Volume, p1Volume, p2Volume, minpVolume, maxpVolume, pVolume;
        // Voxel volume control
        int minI, minJ, minK;
        int maxI, maxJ, maxK;
        int i, j, k;
        // Structural algorithm control variables
        int t;
        int status;
        Matrix4x4 Minv = M.inverse();
        double triangleMinmax[] = new double[6];
        double distanceTolerance;

        minpVolume = new Vector3D();
        maxpVolume = new Vector3D();

        p0Geom = new Vector3D();
        p1Geom = new Vector3D();
        p2Geom = new Vector3D();

        for ( t = 0; t < getNumTriangles(); t++ ) {
            // Process i-th triangle in mesh
            p0Geom.x = vertexPositions[3*triangleIndices[3*t+0]+0];
            p0Geom.y = vertexPositions[3*triangleIndices[3*t+0]+1];
            p0Geom.z = vertexPositions[3*triangleIndices[3*t+0]+2];
            p1Geom.x = vertexPositions[3*triangleIndices[3*t+1]+0];
            p1Geom.y = vertexPositions[3*triangleIndices[3*t+1]+1];
            p1Geom.z = vertexPositions[3*triangleIndices[3*t+1]+2];
            p2Geom.x = vertexPositions[3*triangleIndices[3*t+2]+0];
            p2Geom.y = vertexPositions[3*triangleIndices[3*t+2]+1];
            p2Geom.z = vertexPositions[3*triangleIndices[3*t+2]+2];
            // Obtain triangle in voxel coordinates
            p0Volume = Minv.multiply(p0Geom);
            p1Volume = Minv.multiply(p1Geom);
            p2Volume = Minv.multiply(p2Geom);
            // Obtain triangle minmax
            ComputationalGeometry.triangleMinMax(p0Volume, p1Volume, p2Volume,
                                                 triangleMinmax);
            minpVolume.x = triangleMinmax[0];
            minpVolume.y = triangleMinmax[1];
            minpVolume.z = triangleMinmax[2];
            maxpVolume.x = triangleMinmax[3];
            maxpVolume.y = triangleMinmax[4];
            maxpVolume.z = triangleMinmax[5];
            minI = vv.getNearestIFromX(minpVolume.x);
            minJ = vv.getNearestJFromY(minpVolume.y);
            minK = vv.getNearestKFromZ(minpVolume.z);
            maxI = vv.getNearestIFromX(maxpVolume.x);
            maxJ = vv.getNearestJFromY(maxpVolume.y);
            maxK = vv.getNearestKFromZ(maxpVolume.z);

            // Rasterize triangle in voxel space
            distanceTolerance = 2.0 / (double)vv.getXSize();
            for ( i = minI; i <= maxI; i++ ) {
                for ( j = minJ; j <= maxJ; j++ ) {
                    for ( k = minK; k <= maxK; k++ ) {
                        pVolume = vv.getVoxelPosition(i, j, k);
                        status = ComputationalGeometry.triangleContainmentTest(
                            p0Volume, p1Volume, p2Volume,
                            pVolume, distanceTolerance);
                        if ( status != OUTSIDE ) {
                            vv.putVoxel(i, j, k, (byte)255);
                        }
                    }
                }
            }
        }
    }

    @Override
    public TriangleMeshGroup exportToTriangleMeshGroup()
    {
        if ( triangleMeshGroupCache == null ) {
            triangleMeshGroupCache = new TriangleMeshGroup();
            triangleMeshGroupCache.addMesh(this);
        }
        return triangleMeshGroupCache;
    }

    /**
    This method does the following:
    1. Detect all triangles with "incorrect" vertex indices and removes them.
    2. Detect all vertices without incident triangles.
    3. Reacomodate mesh representation arrays to fit the new situation. This
       can imply that some triangle indices gets updated.
    */
    public void compact()
    {
        vertexNormals = null;

        //- Count triangle incidences on vertices -------------------------
        boolean count[];
        int i;
        int a, b, c;
        int n;

        n = vertexPositions.length/3;
        count = new boolean[n];

        for ( i = 0; i < n; i++ ) {
            count[i] = false;
        }

        for ( i = 0; i < triangleIndices.length; i++ ) {
            a = triangleIndices[i];
            if ( a >= 0 && a < n ) {
                count[a] = true;
            }
        }

        //- Calculate new index map ---------------------------------------
        int j = 0;
        int map[];

        map = new int[n];
        for ( i = 0; i < n; i++ ) {
            if ( count[i] ) {
                map[i] = j;
                j++;
            }
            else {
                map[i] = -1;
            }
        }

        //- Calculate new vertex arrays -----------------------------------

        lastInfo = new GeometryIntersectionInformation();
        lastRay = null;
        minMax = null;
        boundingVolume = null;
        triangleMeshGroupCache = null;

        double oldVertexPositions[] = vertexPositions;
        double oldVertexNormals[] = vertexNormals;

        initVertexPositionsArray(j);
        if ( oldVertexNormals != null ) {
            initVertexNormalsArray();
        }

        j = 0;
        for ( i = 0; i < n; i++ ) {
            if ( count[i] ) {
                vertexPositions[3*j+0] = oldVertexPositions[3*i+0];
                vertexPositions[3*j+1] = oldVertexPositions[3*i+1];
                vertexPositions[3*j+2] = oldVertexPositions[3*i+2];
                if ( oldVertexNormals != null ) {
                    vertexNormals[3*j+0] = oldVertexNormals[3*i+0];
                    vertexNormals[3*j+1] = oldVertexNormals[3*i+1];
                    vertexNormals[3*j+2] = oldVertexNormals[3*i+2];
                }
                j++;
            }
        }

        //- Calculate new triangle indices arrays -------------------------
        j = 0;
        int oldTriangleIndices[] = triangleIndices;

        for ( i = 0; i < oldTriangleIndices.length/3; i++ ) {
            a = oldTriangleIndices[3*i+0];
            b = oldTriangleIndices[3*i+1];
            c = oldTriangleIndices[3*i+2];
            if ( a < 0 || a >= n || b < 0 || b >= n || c < 0 || c >= n ) {
                // This triangle is not copied
            }
            else {
                j++;
            }
        }

        initTriangleArrays(j);
        j = 0;
        for ( i = 0; i < oldTriangleIndices.length/3; i++ ) {
            a = oldTriangleIndices[3*i+0];
            b = oldTriangleIndices[3*i+1];
            c = oldTriangleIndices[3*i+2];
            if ( a < 0 || a >= n || b < 0 || b >= n || c < 0 || c >= n ) {
                // This triangle is not copied
            }
            else {
                triangleIndices[3*j+0] = map[oldTriangleIndices[3*i+0]];
                triangleIndices[3*j+1] = map[oldTriangleIndices[3*i+1]];
                triangleIndices[3*j+2] = map[oldTriangleIndices[3*i+2]];
                j++;
            }
        }

    }

    /**
    This method removes all currently selected vertices, and its
    corresponding incident triangles.

    Note that this method also removes "dangling" vertices, selected or not.
    */
    public void removeSelectedVertices()
    {
        //-----------------------------------------------------------------
        if ( vertexSelections == null ) {
            return;
        }

        vertexNormals = null;
        vertexBinormals = null;
        vertexTangents = null;

        //-----------------------------------------------------------------
        int i, a, b, c, n;
        n = vertexPositions.length/3;
        for ( i = 0; i < triangleIndices.length/3; i++ ) {
            a = triangleIndices[3*i+0];
            b = triangleIndices[3*i+1];
            c = triangleIndices[3*i+2];
            if ( a < 0 || a >= n || b < 0 || b >= n || c < 0 || c >= n ||
                 vertexSelections[a] || vertexSelections[b] ||
                 vertexSelections[c] ) {
                triangleIndices[3*i+0] = -1;
                triangleIndices[3*i+1] = -1;
                triangleIndices[3*i+2] = -1;
            }
        }

        //-----------------------------------------------------------------
        compact();

/*
        int i;
        for ( i = 0; i < vertexPositions.length/3; i++ ) {
            if ( vertexSelections[i] ) {
                vertexPositions[3*i] += 0.01;
            }
        }
*/
        //-----------------------------------------------------------------
        vertexSelections = null;

        calculateNormals();
    }

    private void appendVertices(ArrayListOfDoubles ev)
    {
        int i;
        double newVertexPositions[] = new double[vertexPositions.length + ev.size()];

        for ( i = 0; i < vertexPositions.length; i++ ) {
            newVertexPositions[i] = vertexPositions[i];
        }
        for ( i = 0; i < ev.size(); i++ ) {
            newVertexPositions[i+vertexPositions.length] = ev.get(i);
        }

        vertexPositions = newVertexPositions;
    }

    private void appendTriangles(ArrayListOfInts et)
    {
        int i;
        int newTriangleIndices[] = new int[triangleIndices.length + et.size()];

        for ( i = 0; i < triangleIndices.length; i++ ) {
            newTriangleIndices[i] = triangleIndices[i];
        }

        for ( i = 0; i < et.size(); i++ ) {
            newTriangleIndices[i+triangleIndices.length] = et.get(i);
        }

        triangleIndices = newTriangleIndices;
    }

    private void
    simpleTriangleCut(InfinitePlane p,
                      ArrayListOfDoubles extraVertices,
                      ArrayListOfInts extraTriangles, 
                      int nv, int i,
                      Vector3D p1, Vector3D p2, Vector3D p3)
    {
        Vector3D a, b, ma = null, mb = null;
        GeometryIntersectionInformation gia = new GeometryIntersectionInformation();
        GeometryIntersectionInformation gib = new GeometryIntersectionInformation();
        a = (p2.substract(p1));
        b = (p3.substract(p1));
        a.normalize();
        b.normalize();

        Ray ra = new Ray(p1, a);
        Ray rb = new Ray(p1, b);

        if ( p.doIntersectionWithNegative(ra) ) {
            p.doExtraInformation(ra, ra.t, gia);
            ma = gia.p;
        }
        if ( p.doIntersectionWithNegative(rb) ) {
            p.doExtraInformation(rb, rb.t, gib);
            mb = gib.p;
        }

        extraTriangles.add(i);

        extraTriangles.add(((extraVertices.size()/3 + nv)));

        if ( ma == null || mb == null /*|| extraVertices == null*/ ) {
            VSDK.reportMessage(this, VSDK.WARNING, "simpleTriangleCut",
            "extraVertices is null!");
            return;
	    }

        extraVertices.add(ma.x);
        extraVertices.add(ma.y);
        extraVertices.add(ma.z);

        extraTriangles.add(((extraVertices.size()/3 + nv)));

        extraVertices.add(mb.x);
        extraVertices.add(mb.y);
        extraVertices.add(mb.z);

        triangleIndices[3*i+0] = -1;
        triangleIndices[3*i+1] = -1;
        triangleIndices[3*i+2] = -1;
    }

    private void
    halfTriangleCut(InfinitePlane p,
                    ArrayListOfDoubles extraVertices,
                    ArrayListOfInts extraTriangles, 
                    int nv, int i, int j,
                    Vector3D p1, Vector3D p2, Vector3D p3)
    {
        Vector3D a, b, ma = null;
        GeometryIntersectionInformation gia = new GeometryIntersectionInformation();
        GeometryIntersectionInformation gib = new GeometryIntersectionInformation();
        a = (p2.substract(p3));
        a.normalize();

        Ray ra = new Ray(p2, a);

        if ( p.doIntersectionWithNegative(ra) ) {
            p.doExtraInformation(ra, ra.t, gia);
            ma = gia.p;
        }

        extraTriangles.add(i);

        extraTriangles.add(((extraVertices.size()/3 + nv)));

        if ( ma == null /*|| extraVertices == null*/ ) {
            VSDK.reportMessage(this, VSDK.WARNING, "simpleTriangleCut",
            "extraVertices is null!");
            return;
    	}

        extraVertices.add(ma.x);
        extraVertices.add(ma.y);
        extraVertices.add(ma.z);

        extraTriangles.add(j);

        triangleIndices[3*i+0] = -1;
        triangleIndices[3*i+1] = -1;
        triangleIndices[3*i+2] = -1;
    }

    private void
    doubleTriangleCut(InfinitePlane p,
                      ArrayListOfDoubles extraVertices,
                      ArrayListOfInts extraTriangles, 
                      int nv, int i, int j,
                      Vector3D p1, Vector3D p2, Vector3D p3)
    {
        Vector3D a, b, ma = null, mb = null;
        GeometryIntersectionInformation gia = new GeometryIntersectionInformation();
        GeometryIntersectionInformation gib = new GeometryIntersectionInformation();
        a = (p1.substract(p3));
        b = (p2.substract(p3));
        a.normalize();
        b.normalize();

        Ray ra = new Ray(p3, a);
        Ray rb = new Ray(p3, b);

        if ( p.doIntersectionWithNegative(ra) ) {
            p.doExtraInformation(ra, ra.t, gia);
            ma = gia.p;
        }
        if ( p.doIntersectionWithNegative(rb) ) {
            p.doExtraInformation(rb, rb.t, gib);
            mb = gib.p;
        }

        //-----------------------------------------------------------------
        extraTriangles.add(3*triangleIndices[3*i+0]+0);

        extraTriangles.add(((extraVertices.size()/3 + nv)));

        if ( ma == null || mb == null /*|| extraVertices == null*/ ) {
            VSDK.reportMessage(this, VSDK.WARNING, "simpleTriangleCut",
            "extraVertices is null!");
            return;
   	    }

        extraVertices.add(ma.x);
        extraVertices.add(ma.y);
        extraVertices.add(ma.z);

        extraTriangles.add(((extraVertices.size()/3 + nv)));

        extraTriangles.add(i); // for next tri.
        extraTriangles.add(((extraVertices.size()/3 + nv))); 
        extraTriangles.add(j);

        extraVertices.add(mb.x);
        extraVertices.add(mb.y);
        extraVertices.add(mb.z);

        //-----------------------------------------------------------------
        triangleIndices[3*i+0] = -1;
        triangleIndices[3*i+1] = -1;
        triangleIndices[3*i+2] = -1;
    }

    /**
    Given current mesh and a plane p, this method modifies the current mesh,
    leaving on it only the triangles which lies on the INSIDE part of the
    plane `p`. For triangles cutting the plane, new triangles are generated.
    @param p
    */
    public void
    slice(InfinitePlane p)
    {
        int i;
        Vector3D p1, p2, p3;
        p1 = new Vector3D();
        p2 = new Vector3D();
        p3 = new Vector3D();
        int t1, t2, t3;

        ArrayListOfInts extraTriangles = new ArrayListOfInts(10);
        ArrayListOfDoubles extraVertices = new ArrayListOfDoubles(10);

        int nv = vertexPositions.length/3;

        for ( i = 0; i < triangleIndices.length/3; i++ ) {

            p1.x = vertexPositions[3*triangleIndices[3*i+0]+0];
            p1.y = vertexPositions[3*triangleIndices[3*i+0]+1];
            p1.z = vertexPositions[3*triangleIndices[3*i+0]+2];

            p2.x = vertexPositions[3*triangleIndices[3*i+1]+0];
            p2.y = vertexPositions[3*triangleIndices[3*i+1]+1];
            p2.z = vertexPositions[3*triangleIndices[3*i+1]+2];

            p3.x = vertexPositions[3*triangleIndices[3*i+2]+0];
            p3.y = vertexPositions[3*triangleIndices[3*i+2]+1];
            p3.z = vertexPositions[3*triangleIndices[3*i+2]+2];

            t1 = p.doContainmentTestHalfSpace(p1, VSDK.EPSILON);
            t2 = p.doContainmentTestHalfSpace(p2, VSDK.EPSILON);
            t3 = p.doContainmentTestHalfSpace(p3, VSDK.EPSILON);

            if ( (t1 == OUTSIDE && t2 == OUTSIDE && t3 == OUTSIDE) ||
                 (t1 == LIMIT && t2 == OUTSIDE && t3 == OUTSIDE) ||
                 (t1 == OUTSIDE && t2 == LIMIT && t3 == OUTSIDE) ||
                 (t1 == OUTSIDE && t2 == OUTSIDE && t3 == LIMIT) ||
                 (t1 == LIMIT && t2 == LIMIT && t3 == OUTSIDE) ||
                 (t1 == LIMIT && t2 == OUTSIDE && t3 == LIMIT) ||
                 (t1 == OUTSIDE && t2 == LIMIT && t3 == LIMIT) 
               ) {
                triangleIndices[3*i+0] = -1;
                triangleIndices[3*i+1] = -1;
                triangleIndices[3*i+2] = -1;
            }
            else if ( t1 == LIMIT && t2 == LIMIT && t3 == LIMIT ) {
                Vector3D a, b, n;
                a = p2.substract(p1);
                b = p3.substract(p1);
                n = a.crossProduct(b);
                if ( n.dotProduct(p.getNormal()) > 0 ) {
                    triangleIndices[3*i+0] = -1;
                    triangleIndices[3*i+1] = -1;
                    triangleIndices[3*i+2] = -1;
                }
            }
            else if (
                 (t1 == LIMIT && t2 == INSIDE && t3 == INSIDE) ||
                 (t1 == INSIDE && t2 == LIMIT && t3 == INSIDE) ||
                 (t1 == INSIDE && t2 == INSIDE && t3 == LIMIT) ||
                 (t1 == LIMIT && t2 == LIMIT && t3 == INSIDE) ||
                 (t1 == LIMIT && t2 == INSIDE && t3 == LIMIT) ||
                 (t1 == INSIDE && t2 == LIMIT && t3 == LIMIT) ||
                 (t1 == INSIDE && t2 == INSIDE && t3 == INSIDE)
                     )
            {
                // Untested?
            }
            else {
                if ( t1 == INSIDE && t2 == OUTSIDE && t3 == OUTSIDE ) {
                    simpleTriangleCut(p, extraVertices, extraTriangles, nv, 3*triangleIndices[3*i+0]+0, p1, p2, p3);
                }
                else if ( t2 == INSIDE && t1 == OUTSIDE && t3 == OUTSIDE ) {
                    simpleTriangleCut(p, extraVertices, extraTriangles, nv, 3*triangleIndices[3*i+0]+0, p2, p1, p3);
                }
                else if ( t3 == INSIDE && t1 == OUTSIDE && t2 == OUTSIDE ) {
                    simpleTriangleCut(p, extraVertices, extraTriangles, nv, 3*triangleIndices[3*i+0]+0, p3, p1, p2);
                }
                else if ( t1 == INSIDE && t2 == INSIDE && t3 == OUTSIDE ) {
                    doubleTriangleCut(p, extraVertices, extraTriangles, nv, 3*triangleIndices[3*i+0]+0, 3*triangleIndices[3*i+0]+1, p1, p2, p3);
                }
                else if ( t1 == INSIDE && t3 == INSIDE && t2 == OUTSIDE ) {
                    doubleTriangleCut(p, extraVertices, extraTriangles, nv, 3*triangleIndices[3*i+0]+0, 3*triangleIndices[3*i+0]+2, p1, p3, p2);
                }
                else if ( t2 == INSIDE && t3 == INSIDE && t1 == OUTSIDE ) {
                    doubleTriangleCut(p, extraVertices, extraTriangles, nv, 3*triangleIndices[3*i+0]+1, 3*triangleIndices[3*i+0]+2, p2, p3, p1);
                }


                else if ( t1 == INSIDE && t2 == LIMIT && t3 == OUTSIDE ) {
                    halfTriangleCut(p, extraVertices, extraTriangles, nv, 3*triangleIndices[3*i+0]+0, 3*triangleIndices[3*i+0]+1, p2, p1, p3);
                }
                else if ( t2 == INSIDE && t1 == LIMIT && t3 == OUTSIDE ) {
                    halfTriangleCut(p, extraVertices, extraTriangles, nv, 3*triangleIndices[3*i+0]+0, 3*triangleIndices[3*i+0]+0, p1, p2, p3);
                }
                else if ( t2 == INSIDE && t3 == LIMIT && t1 == OUTSIDE ) {
                    halfTriangleCut(p, extraVertices, extraTriangles, nv, 3*triangleIndices[3*i+0]+0, 3*triangleIndices[3*i+0]+2, p3, p2, p1);
                }
                else if ( t3 == INSIDE && t2 == LIMIT && t1 == OUTSIDE ) {
                    System.out.println("MyCase4");
                    halfTriangleCut(p, extraVertices, extraTriangles, nv, 3*triangleIndices[3*i+0]+0, 3*triangleIndices[3*i+0]+1, p2, p3, p1);
                }
                else if ( t3 == INSIDE && t1 == LIMIT && t2 == OUTSIDE ) {
                    halfTriangleCut(p, extraVertices, extraTriangles, nv, 3*triangleIndices[3*i+0]+0, 3*triangleIndices[3*i+0]+0, p1, p3, p2);
                }
                else if ( t1 == INSIDE && t3 == LIMIT && t2 == OUTSIDE ) {
                    halfTriangleCut(p, extraVertices, extraTriangles, nv, 3*triangleIndices[3*i+0]+0, 3*triangleIndices[3*i+0]+2, p3, p1, p2);
                }
                else {
                    System.out.println("Unhandled case: ");
                    System.out.println("  - P1: " + t1);
                    System.out.println("  - P2: " + t2);
                    System.out.println("  - P3: " + t3);
                }
            }
        }

        //-----------------------------------------------------------------
        appendVertices(extraVertices);
        appendTriangles(extraTriangles);

        //-----------------------------------------------------------------
        compact();

        //-----------------------------------------------------------------
        calculateNormals();
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
