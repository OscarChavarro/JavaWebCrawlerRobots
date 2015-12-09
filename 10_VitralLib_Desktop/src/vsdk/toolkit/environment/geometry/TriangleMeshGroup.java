//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - August 8 2005 - Gabriel Sarmiento / Lina Rojas: Original base version =
//= - April 28 2006 - Oscar Chavarro: quality group                         =
//= - November 6 2006 - Oscar Chavarro: introduced bounding box             =
//===========================================================================

package vsdk.toolkit.environment.geometry;

import java.util.ArrayList;

import vsdk.toolkit.common.Ray;
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.gui.ProgressMonitor;

public class TriangleMeshGroup extends Surface {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20060502L;

    private ArrayList<TriangleMesh> meshes;
    private double[] MinMax;

    private GeometryIntersectionInformation lastInfo;
    private int[] intersectionInformation;

    private SimpleBody boundingVolume;

    public TriangleMeshGroup() {
        meshes = new ArrayList<TriangleMesh> ();
        MinMax = null;
        lastInfo = new GeometryIntersectionInformation();
        boundingVolume = null;
    }

    public TriangleMeshGroup(ArrayList<TriangleMesh> meshes) {
        this.meshes = meshes;
        lastInfo = new GeometryIntersectionInformation();
        boundingVolume = null;
    }

    public TriangleMeshGroup(TriangleMeshGroup group) {
        this.meshes = group.getMeshes();
        lastInfo = new GeometryIntersectionInformation();
        boundingVolume = null;
    }

    public ArrayList<TriangleMesh> getMeshes() {
        return this.meshes;
    }

    public void setMeshes(ArrayList<TriangleMesh> meshes) {
        boundingVolume = null;
        this.meshes = meshes;
    }

    public void addMesh(TriangleMesh mesh) {
        this.meshes.add(mesh);
        boundingVolume = null;
    }

    public TriangleMesh getMeshAt(int index) {
        return this.meshes.get(index);
    }

    /**
    @return a new 6 valued double array containing the coordinates of a min-max
    bounding box for current geometry.
    */
    @Override
    public double[] getMinMax() {
        if(MinMax==null)
            calculateMinMaxPositions();
        return this.MinMax;
    }

    /**
    Calculates the MinMax. Note that for each position in this array:
        0 - min (x)
        1 - min (y)
        2 - min (z)
        3 - max (x)
        4 - max (y)
        5 - max (z)
    */
    public void calculateMinMaxPositions() {
        if ( MinMax == null ) {
            MinMax = new double[6];

            boolean first = true;

            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE,
                minZ = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE,
                maxZ = Double.MIN_VALUE;

            for (int i = 0; i < meshes.size(); i++) {
                double[] minmax_mesh = meshes.get(i).getMinMax();
                double x = minmax_mesh[0];
                double y = minmax_mesh[1];
                double z = minmax_mesh[2];
                double X = minmax_mesh[3];
                double Y = minmax_mesh[4];
                double Z = minmax_mesh[5];

                if (first) {
                    minX = x;
                    maxX = X;
                    minY = y;
                    maxY = Y;
                    minZ = z;
                    maxZ = Z;
                    first = false;
                }

                if (x < minX) {
                    minX = x;
                }
                if (y < minY) {
                    minY = y;
                }
                if (z < minZ) {
                    minZ = z;
                }
                if (X > maxX) {
                    maxX = X;
                }
                if (Y > maxY) {
                    maxY = Y;
                }
                if (Z > maxZ) {
                    maxZ = Z;
                }
            }
            MinMax[0] = minX;
            MinMax[1] = minY;
            MinMax[2] = minZ;
            MinMax[3] = maxX;
            MinMax[4] = maxY;
            MinMax[5] = maxZ;
        }
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doIntersection.

    @param inOut_Ray
    @return true if given ray intersects current TriangleMeshGroup
    */
    @Override
    public boolean doIntersection(Ray inOut_Ray) {
        int i;                // Index for iterating meshes
        boolean intersection; // true if intersection founded
        double min_t;         // Shortest distance founded so far
        GeometryIntersectionInformation Info;

        // Initialization values for search algorithm
        min_t = Double.MAX_VALUE;
        intersection = false;
        Info = new GeometryIntersectionInformation();

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

        // Chain of responsability behavior design pattern with TriangleMesh
        for ( i= 0; i< meshes.size(); i++ ) {
            TriangleMesh mesh = meshes.get(i);
            Ray ray = new Ray(inOut_Ray);

            if ( mesh.doIntersection(ray) ) {
                mesh.doExtraInformation(ray, 0.0, Info);

                if ( ray.t < min_t ) {
                    min_t = ray.t;

                    // Stores standard doIntersection operation information
                    lastInfo.clone(Info);
                    inOut_Ray.t = ray.t;

                    // Stores the intersected mesh and the triangle intersected inside that mesh
                    intersectionInformation = new int[2];
                    intersectionInformation[0] = i;
                    intersectionInformation[1] = mesh.doIntersectionInformation();

                    intersection = true;
                }
            }
        }

        return intersection;
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doExtraInformation.
    @param inRay
    @param inT
    @param outData
    */
    @Override
    public void
    doExtraInformation(
        Ray inRay, 
        double inT,
        GeometryIntersectionInformation outData) {
        outData.clone(lastInfo);
    }

    public int[] doIntersectionInformation()
    {
        return intersectionInformation;
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
        TriangleMesh mesh;
        int status;
        int i;

        // Chain of responsability behavior design pattern with TriangleMesh
        for ( i= 0; i< meshes.size(); i++ ) {
            mesh = meshes.get(i);
            status = mesh.doContainmentTest(p, distanceTolerance);
            if ( status != OUTSIDE ) {
                return status;
            }
        }
        return OUTSIDE;
    }

    /**
    Provides an object to text report convertion, optimized for human
    readability and debugging. Do not use for serialization or persistence
    purposes.
    @return human readable report from current mesh group
    */
    @Override
    public String toString() {
        return "TriangleMeshGroup < #Mesh: " + this.meshes.size() + " >";
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doVoxelization.
    */
    @Override
    public void doVoxelization(VoxelVolume vv, Matrix4x4 M, ProgressMonitor reporter)
    {
        int i;

        // Chain of responsability behavior design pattern with TriangleMesh
        for ( i= 0; i< meshes.size(); i++ ) {
            meshes.get(i).doVoxelization(vv, M, reporter);
        }
    }

    @Override
    public TriangleMeshGroup exportToTriangleMeshGroup()
    {
        return this;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
