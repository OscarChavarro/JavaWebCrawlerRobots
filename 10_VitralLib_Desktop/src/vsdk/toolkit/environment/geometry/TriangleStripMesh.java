//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - August 8 2007 - Oscar Chavarro: Original base version                 =
//===========================================================================

package vsdk.toolkit.environment.geometry;

// Java basic classes

// VitralSDK classes
import vsdk.toolkit.common.Vertex;
import vsdk.toolkit.common.Ray;
import vsdk.toolkit.environment.scene.SimpleBody;

public class TriangleStripMesh extends Surface {

//= Class attributes ========================================================

    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20060807L;

    // Basic mesh data model
    private String name = "default";
    private Vertex[] vertexes;
    private int[][] strips;

    // Auxiliary data structures for storage of parcial results and 
    // preprocessing
    private double[] minMax;
    private SimpleBody boundingVolume;

//= Basic class management methods ==========================================

    public TriangleStripMesh() {
        boundingVolume = null;
        minMax = null;
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

            for ( i = 0; i < vertexes.length; i++ ) {
                double x = vertexes[i].getPosition().x;
                double y = vertexes[i].getPosition().y;
                double z = vertexes[i].getPosition().z;

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

    public Vertex[] getVertexes() {
        return this.vertexes;
    }

    public Vertex getVertexAt(int index) {
        return this.vertexes[index];
    }

    public void setVertexes(Vertex[] vertexes) {
        this.vertexes = vertexes;
        boundingVolume = null;
    }

    public void setStrips(int indexes[][])
    {
        this.strips = indexes;
    }

    public int[][] getStrips()
    {
        return strips;
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doIntersection.

    \todo  Method not implemented!
    @param inOut_Ray
    @return true if given ray intersects current TriangleStripMesh
    */
    @Override
    public boolean
    doIntersection(Ray inOut_Ray) {
        return false;
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doExtraInformation.

    \todo  Method not implemented!
    @param inT
    */
    @Override
    public void
    doExtraInformation(Ray inRay, double inT,
                                   GeometryIntersectionInformation outData) {

    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
