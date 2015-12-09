//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - October 15 2007 - Oscar Chavarro: Original base version               =
//===========================================================================

package vsdk.toolkit.environment.geometry;

// VitralSDK classes
import vsdk.toolkit.common.AlgebraicExpression;
import vsdk.toolkit.common.AlgebraicExpressionException;
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.Ray;
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.gui.ProgressMonitor;

/**

*/
public class FunctionalExplicitSurface extends Surface
{
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20071015L;

    private AlgebraicExpression xyFunction;
    private String functionExpression;
    private double minx;
    private double miny;
    private double minz;
    private double maxx;
    private double maxy;
    private double maxz;
    private int nx;
    private int ny;
    private TriangleMesh internalGeometry;

    public FunctionalExplicitSurface(String fxy)
    {
        init(fxy);
    }

    private void init(String fxy)
    {
        functionExpression = fxy;
        xyFunction = new AlgebraicExpression();
        try {
            xyFunction.setExpression(fxy);
        }
        catch ( AlgebraicExpressionException e ) {
            VSDK.reportMessage(this, VSDK.WARNING, 
                "constructor",
                "Cannot create algebraic expression for \"" + fxy + "\":\n" + e);
            try {
                xyFunction.setExpression("0");
            }
            catch ( AlgebraicExpressionException e2 ) {
                VSDK.reportMessage(this, VSDK.FATAL_ERROR, 
                    "constructor",
                    "So bad. Something is wrong with algebraic expressions!:\n" + e2);
            }
        }
        minx = miny = minz = -1.0;
        maxx = maxy = maxz = 1.0;
        nx = 10;
        ny = 10;
        updateInternalGeometry();
    }

    public String getFunctionExpression()
    {
        return functionExpression;
    }

    public void setBounds(double minx, double miny, double minz,
                          double maxx, double maxy, double maxz)
    {
        this.minx = minx;
        this.miny = miny;
        this.minz = minz;
        this.maxx = maxx;
        this.maxy = maxy;
        this.maxz = maxz;
        updateInternalGeometry();
    }

    public void setTesselationHint(int tesx, int tesy)
    {
        nx = tesx;
        ny = tesy;
        updateInternalGeometry();
    }

    public int getTesselationHintX()
    {
        return nx;
    }

    public int getTesselationHintY()
    {
        return ny;
    }

    public double getMinXBound()
    {
        return minx;
    }

    public double getMinYBound()
    {
        return miny;
    }

    public double getMinZBound()
    {
        return minz;
    }

    public double getMaxXBound()
    {
        return maxx;
    }

    public double getMaxYBound()
    {
        return maxy;
    }

    public double getMaxZBound()
    {
        return maxz;
    }

    private int coord(int nx, int ny, int ix, int iy)
    {
        return ((nx+1)*iy) + ix;
    }

    private void updateInternalGeometry()
    {
        //-----------------------------------------------------------------
        // Size of each tile in x direction
        double dx = (maxx - minx) / ((double)nx); 
        // Size of each tile in y direction
        double dy = (maxy - miny) / ((double)ny);

        // Temporary variable
        double x;
        double y;
        int ix;
        int iy;
        int index;

        internalGeometry = new TriangleMesh();

        //-----------------------------------------------------------------
        internalGeometry.initVertexPositionsArray((nx+1)*(ny+1));
        double v[];
        double z;

        v = internalGeometry.getVertexPositions();
        try {
            index = 0;
            for ( iy = 0, y = miny; iy <= ny; iy++, y += dy ) {
                xyFunction.defineValue("y", y);
                for ( ix = 0, x = minx; ix <= nx; ix++, x += dx ) {
                    xyFunction.defineValue("x", x);
                    z = xyFunction.eval();
                    if ( z > maxz ) {
                        z = maxz;
                    }
                    if ( z < minz ) {
                        z = minz;
                    }
                    v[3*index] = x;
                    v[3*index+1] = y;
                    v[3*index+2] = z;
                    index++;
                }
            }
        }
        catch ( AlgebraicExpressionException e ) {
            VSDK.reportMessage(this, VSDK.WARNING, 
                "constructor",
                "Cannot evaluate algebraic expression!" + e);
            return;
        }

        //-----------------------------------------------------------------
        internalGeometry.initTriangleArrays(nx*ny*2);
        int t[];

        index = 0;
        t = internalGeometry.getTriangleIndexes();
        for ( iy = 0; iy < ny; iy++ ) {
            for ( ix = 0; ix < nx; ix++ ) {
                t[3*index] = coord(nx, ny, ix, iy);
                t[3*index+1] = coord(nx, ny, ix+1, iy);
                t[3*index+2] = coord(nx, ny, ix+1, iy+1);
                index++;

                t[3*index] = coord(nx, ny, ix, iy);
                t[3*index+1] = coord(nx, ny, ix+1, iy+1);
                t[3*index+2] = coord(nx, ny, ix, iy+1);
                index++;
            }
        }

        //-----------------------------------------------------------------
        internalGeometry.calculateNormals();
    }

    public TriangleMesh getInternalTriangleMesh()
    {
        return internalGeometry;
    }

    /**
    Check the general interface contract in superclass method
    Geometry.getMinMax.
    @return a new 6 valued double array containing the coordinates of a min-max
    bounding box for current geometry.
    */
    @Override
    public double[] getMinMax() {
        return internalGeometry.getMinMax();
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doIntersection.

    \todo  Should not delegate work over tesselated geometry version. Should
    evaluate directly from algebraic function surface!
    @param inOut_Ray
    @return true if given ray intersects current FunctionalExplicitSurface
    */
    @Override
    public boolean
    doIntersection(Ray inOut_Ray) {
        return internalGeometry.doIntersection(inOut_Ray);
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
    doExtraInformation(Ray inRay, double inT,
                                   GeometryIntersectionInformation outData) {
        internalGeometry.doExtraInformation(inRay, inT, outData);
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doContainmentTest.
    @param p
    @param distanceTolerance
    @return INSIDE, OUTSIDE or LIMIT constant value 
    */
    @Override
    public int doContainmentTest(Vector3D p, double distanceTolerance)
    {
        return internalGeometry.doContainmentTest(p, distanceTolerance);
    }

    /**
    Check the general interface contract in superclass method
    Geometry.doVoxelization.
    @param vv
    @param M
    @param reporter
    */
    @Override
    public void
    doVoxelization(VoxelVolume vv, Matrix4x4 M, ProgressMonitor reporter)
    {
        internalGeometry.doVoxelization(vv, M, reporter);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
