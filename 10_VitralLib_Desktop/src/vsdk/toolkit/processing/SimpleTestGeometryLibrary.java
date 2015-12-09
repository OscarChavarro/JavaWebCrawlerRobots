//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - Apr 20 2008 - Oscar Chavarro: Original base version                   =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [APPE1967] Appel, Arthur. "The notion of quantitative invisivility and  =
//=          the machine rendering of solids". Proceedings, ACM National    =
//=          meeting 1967.                                                  =
//= [MANT1986] Mantyla Martti. "Boolean Operations of 2-Manifolds through   =
//=     Vertex Neighborhood Classification". ACM Transactions on Graphics,  =
//=     Vol. 5, No. 1, January 1986, pp. 1-29.                              =
//= [MANT1988] Mantyla Martti. "An Introduction To Solid Modeling",         =
//=     Computer Science Press, 1988.                                       =
//===========================================================================

package vsdk.toolkit.processing;

// VitralSDK classes
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.environment.geometry.Box;
import vsdk.toolkit.environment.geometry.PolyhedralBoundedSolid;

/**
This is a utility class containing a lot of geometry examples (mostly
geometry generating procedures). This is a companion class for the
`ComputationalGeometry` and `GeometricModeler` classes, which holds
geometrical querys and other geometry generation procedures.

This class is comprised of static methods, depending only of VSDK's Entity's.
From a design point of view, it can be view as an "strategy" design pattern
in the sense that encapsulates algorithms. It also could be viewd as a "factory"
or "abstract factory", as it is a class for creating objects, following
the data hierarchy of interface "geometry".

An important characteristic of this class is that the simple geometries that
creates, are usually "inspired" (or "borrowed") from classic textbooks and
papers from the computer graphics academic comunity. Most of the methods
contain references to figures and other data published by original authors.

The simple geometry test object provided here are useful because:
  - They are procedurally generated. That means that no input/output is used.
    This is useful in developing environments where input/output is not
    available, or where its operation is not ported yet (for example mobile
    devices), so this class can be used for testing VitralSDK software
    infrastructure, befor input/output operation are made available.
  - For algorithm benchmarking. Usually, original authors has invented
    this simple test objects for testing algorithms and measuring its
    performance.  When reimplementing the original algorithms in VitralSDK,
    running them with similar test objects is useful when comparing
    VitralSDK's implementation performance.
*/
public class SimpleTestGeometryLibrary extends ProcessingElement
{
    /**
    This method uses basic blocks and constructive solid geometry to build up
    a test object similar to the one appearing in the lower part of figure
    [APPE1967].7. Note that this method returns a solid with two shells with
    a total of 54 vertices, 84 edges and 32 faces, as expected from description
    reported in [APPE1967]. From that paper, a hidden line calculation of this
    object consumes 6.5 seconds of CPU time on an IBM 7094 mainframe. That
    reported time could be useful when benchmarking hidden line and other
    visualization algorithms :) This method is provided for benchmarking and
    comparison purposes!
    */
    public static PolyhedralBoundedSolid createTestObjectAPPE1967_3()
    {
        PolyhedralBoundedSolid a;
        PolyhedralBoundedSolid b;

        a = createTestObjectAPPE1967_1();
        b = createTestObjectAPPE1967_2();

        return GeometricModeler.setOp(a, b, GeometricModeler.UNION);
    }

    /**
    This method uses basic blocks and constructive solid geometry to build up
    a test object similar to the one appearing in the upper part of figure
    [APPE1967].7.
    */
    public static PolyhedralBoundedSolid createTestObjectAPPE1967_2()
    {
        PolyhedralBoundedSolid a;
        PolyhedralBoundedSolid b;
        PolyhedralBoundedSolid c;
        PolyhedralBoundedSolid d;
        PolyhedralBoundedSolid ab;
        PolyhedralBoundedSolid cd;
        PolyhedralBoundedSolid abcd;
        Matrix4x4 T;
        Box box;

        //-----------------------------------------------------------------
        T = new Matrix4x4();
        T.translation(0.3, 0.1+0.4, 0.1+0.4);
        box = new Box(new Vector3D(0.6, 0.2, 0.2));
        a = box.exportToPolyhedralBoundedSolid();
        a.applyTransformation(T);
        a.validateModel();

        //-----------------------------------------------------------------
        T = new Matrix4x4();
        T.translation(0.5, 0.5, 0.5);
        box = new Box(new Vector3D(0.2, 0.2, 1.0));
        b = box.exportToPolyhedralBoundedSolid();
        b.applyTransformation(T);
        b.validateModel();

        //-----------------------------------------------------------------
        T = new Matrix4x4();
        T.translation(0.7, 0.5, 0.9);
        box = new Box(new Vector3D(0.6, 0.2, 0.2));
        c = box.exportToPolyhedralBoundedSolid();
        c.applyTransformation(T);
        c.validateModel();

        //-----------------------------------------------------------------
        T = new Matrix4x4();
        T.translation(0.9, 0.5, 0.9);
        box = new Box(new Vector3D(0.2, 1.0, 0.2));
        d = box.exportToPolyhedralBoundedSolid();
        d.applyTransformation(T);
        d.validateModel();

        //-----------------------------------------------------------------
        ab = GeometricModeler.setOp(a, b, GeometricModeler.UNION);
        cd = GeometricModeler.setOp(c, d, GeometricModeler.UNION);
        abcd = GeometricModeler.setOp(ab, cd, GeometricModeler.UNION);
        return abcd;
    }

    /**
    This method uses basic blocks and constructive solid geometry to build up
    a test object similar to the one appearing in the middle of figure
    [APPE1967].7.
    */
    public static PolyhedralBoundedSolid createTestObjectAPPE1967_1()
    {
        PolyhedralBoundedSolid a;
        PolyhedralBoundedSolid b;
        PolyhedralBoundedSolid c;
        PolyhedralBoundedSolid d;
        PolyhedralBoundedSolid e;
        PolyhedralBoundedSolid f;
        PolyhedralBoundedSolid g;
        PolyhedralBoundedSolid h;
        PolyhedralBoundedSolid ac;
        PolyhedralBoundedSolid bd;
        PolyhedralBoundedSolid eg;
        PolyhedralBoundedSolid fh;
        PolyhedralBoundedSolid abcd;
        PolyhedralBoundedSolid efgh;
        PolyhedralBoundedSolid total;
        Matrix4x4 T;
        Box box;

        //-----------------------------------------------------------------
        T = new Matrix4x4();
        T.translation(0.5, 0.1, 0.1);
        box = new Box(new Vector3D(1, 0.2, 0.2));
        a = box.exportToPolyhedralBoundedSolid();
        a.applyTransformation(T);
        a.validateModel();

        //-----------------------------------------------------------------
        T = new Matrix4x4();
        T.translation(0.5, 0.9, 0.1);
        box = new Box(new Vector3D(1, 0.2, 0.2));
        b = box.exportToPolyhedralBoundedSolid();
        b.applyTransformation(T);
        b.validateModel();

        //-----------------------------------------------------------------
        T = new Matrix4x4();
        T.translation(0.1, 0.5, 0.1);
        box = new Box(new Vector3D(0.2, 1, 0.2));
        c = box.exportToPolyhedralBoundedSolid();
        c.applyTransformation(T);
        c.validateModel();

        //-----------------------------------------------------------------
        T = new Matrix4x4();
        T.translation(0.9, 0.5, 0.1);
        box = new Box(new Vector3D(0.2, 1, 0.2));
        d = box.exportToPolyhedralBoundedSolid();
        d.applyTransformation(T);
        d.validateModel();

        //-----------------------------------------------------------------
        ac = GeometricModeler.setOp(a, c, GeometricModeler.UNION);
        bd = GeometricModeler.setOp(b, d, GeometricModeler.UNION);
        abcd = GeometricModeler.setOp(bd, ac, GeometricModeler.UNION);

        //-----------------------------------------------------------------
        T = new Matrix4x4();
        T.translation(0.1, 0.5, 0.1);
        box = new Box(new Vector3D(0.2, 1, 0.2));
        e = box.exportToPolyhedralBoundedSolid();
        e.applyTransformation(T);
        e.validateModel();

        //-----------------------------------------------------------------
        T = new Matrix4x4();
        T.translation(0.1, 0.5, 0.9);
        box = new Box(new Vector3D(0.2, 1, 0.2));
        f = box.exportToPolyhedralBoundedSolid();
        f.applyTransformation(T);
        f.validateModel();

        //-----------------------------------------------------------------
        T = new Matrix4x4();
        T.translation(0.1, 0.1, 0.5);
        box = new Box(new Vector3D(0.2, 0.2, 1));
        g = box.exportToPolyhedralBoundedSolid();
        g.applyTransformation(T);
        g.validateModel();

        //-----------------------------------------------------------------
        T = new Matrix4x4();
        T.translation(0.1, 0.9, 0.5);
        box = new Box(new Vector3D(0.2, 0.2, 1));
        h = box.exportToPolyhedralBoundedSolid();
        h.applyTransformation(T);
        h.validateModel();

        //-----------------------------------------------------------------
        eg = GeometricModeler.setOp(e, g, GeometricModeler.UNION);
        fh = GeometricModeler.setOp(f, h, GeometricModeler.UNION);
        efgh = GeometricModeler.setOp(eg, fh, GeometricModeler.UNION);
        total = GeometricModeler.setOp(abcd, efgh, GeometricModeler.UNION);

        return total;
    }

    /**
    This method builds a test solid for evaluating the splitting algorithm in
    a controlled way. The generated object is similar to that shown on figures
    [MANT1986].4., [MANT1986].5., [MANT1986].8., [MANT1986].10.,
    [MANT1988].14.2., [MANT1988].14.3., and [MANT1988].14.6.

    Generated solid is interesting when splitting with respect to the plane
    Z=0.3 because stress the splitting algorithm to consider multiple vertex
    classification cases.
    */
    public static PolyhedralBoundedSolid createTestObjectMANT1986_1()
    {
        PolyhedralBoundedSolid solid;

        solid = new PolyhedralBoundedSolid();
        solid.mvfs(new Vector3D(0.00, 0.40, 0.00), 1, 1);
        solid.smev(1, 1, 2, new Vector3D(0.94, 0.40, 0.00));
        solid.smev(1, 2, 3, new Vector3D(0.94, 0.40, 0.46));
        solid.smev(1, 3, 4, new Vector3D(0.60, 0.40, 0.30));
        solid.smev(1, 4, 5, new Vector3D(0.37, 0.40, 0.30));
        solid.smev(1, 5, 6, new Vector3D(0.18, 0.40, 0.46));
        solid.smev(1, 6, 7, new Vector3D(0.00, 0.40, 0.30));
        solid.mef(1, 1, 7, 6, 1, 2, 2);

        Matrix4x4 T = new Matrix4x4();
        T.translation(0, -0.4, 0);
        GeometricModeler.translationalSweepExtrudeFacePlanar(
            solid, solid.findFace(1), T);

        return solid;
    }

    /**
    This method builds a test sample pair of solids for evaluating
    the set operations algorithm in a controlled way.
    The generated objects are similar to that shown on figures
    [MANT1986].11. and [MANT1988].15.4.
    This set correspond to the simpler of all cases for CSG operations
    test, and its processing in set operations are characterized by
    the following consecuences:
      - Only the vertex-face classifier is called (can be processed
        without using a verte-vertex classifier).
      - On the vertex-face classifier, the second stage (reclassification
        on sectors) is not used, due to non coplanar cases on neigborhoods.
    */
    public static PolyhedralBoundedSolid[] createTestObjectPairMANT1986_2()
    {
        PolyhedralBoundedSolid operands[];
        PolyhedralBoundedSolid a, b;

        operands = new PolyhedralBoundedSolid[2];

        //-----------------------------------------------------------------
        Matrix4x4 R = new Matrix4x4();
        R.translation(0.5, 0.25, 0.3);

        Box box = new Box(new Vector3D(1, 0.5, 0.6));
        a = box.exportToPolyhedralBoundedSolid();
        a.applyTransformation(R);
        a.validateModel();

        //-----------------------------------------------------------------
        R = new Matrix4x4();
        R.translation(0.5+0.24, 0.25-0.18, 0.3+0.42);

        box = new Box(new Vector3D(1, 0.5, 0.6));
        b = box.exportToPolyhedralBoundedSolid();
        b.applyTransformation(R);
        b.validateModel();

        //-----------------------------------------------------------------
        operands[0] = a;
        operands[1] = b;

        return operands;
    }

    /**
    This method builds a test sample pair of solids for evaluating
    the set operations algorithm in a controlled way.
    The generated objects are similar to that shown on figures
    [MANT1986].12. and [MANT1988].15.5.
    The generated simple solids pair is interesting because in the
    current positions their vertices touches in several different ways,
    so a vertex neighborhood classifier is stressed to manage different
    cases. Some faces between the solid are overlaping, making this sample
    example a "difficult" one for set operations.
    */
    public static PolyhedralBoundedSolid[] createTestObjectPairMANT1986_3()
    {
        PolyhedralBoundedSolid operands[];
        PolyhedralBoundedSolid a, b;

        operands = new PolyhedralBoundedSolid[2];

        //-----------------------------------------------------------------
        a = new PolyhedralBoundedSolid();
        a.mvfs(         new Vector3D(0.00+0.05, 0.42+0.05, 0.00+0.05), 1, 1);
        a.smev(1, 1, 2, new Vector3D(0.92+0.05, 0.42+0.05, 0.00+0.05));
        a.smev(1, 2, 3, new Vector3D(0.92+0.05, 0.42+0.05, 0.72+0.05));
        a.smev(1, 3, 4, new Vector3D(0.70+0.05, 0.42+0.05, 0.72+0.05));
        a.smev(1, 4, 5, new Vector3D(0.70+0.05, 0.42+0.05, 0.18+0.05));
        a.smev(1, 5, 6, new Vector3D(0.00+0.05, 0.42+0.05, 0.18+0.05));
        a.mef(1, 1, 6, 5, 1, 2, 2);
        a.validateModel();

        Matrix4x4 T = new Matrix4x4();
        T.translation(0, -0.42, 0);
        GeometricModeler.translationalSweepExtrudeFacePlanar(
            a, a.findFace(1), T);

        //-----------------------------------------------------------------
        Matrix4x4 R = new Matrix4x4();
        R.translation(0.05 +0.58/2.0+(0.92-0.58) /*+ 0.0001*/,
                      0.05 + 0.42/2.0 - 0.42/2.0,
                      0.05 + 0.18/2.0 + 0.18 /*+ 0.0001*/);

        Box box = new Box(new Vector3D(0.58, 0.42, 0.18));
        b = box.exportToPolyhedralBoundedSolid();
        b.applyTransformation(R);
        b.validateModel();

        //-----------------------------------------------------------------
        operands[0] = a;
        operands[1] = b;

        return operands;
    }

    /**
    This method creates a simple test model pair of objects similar to that
    shown on figure [MANT1988].6.13. These two extrusion solids are an
    interesting example of view profiles, where a 3D solid can be reconstructed
    from its left and front view using boolean set operations (a.k.a. 
    "profile set operations" at section [MANT1988].6.4.2.).
    */
    public static PolyhedralBoundedSolid[] createTestObjectPairMANT1988_6_13()
    {
        PolyhedralBoundedSolid operands[];

        operands = new PolyhedralBoundedSolid[2];

        //-----------------------------------------------------------------
        PolyhedralBoundedSolid leftView;
        leftView = new PolyhedralBoundedSolid();
        leftView.mvfs(new Vector3D(0, 1, 0), 1, 1);
        leftView.smev(1, 1, 2, new Vector3D(3.1/3.7, 1, 0));
        leftView.smev(1, 2, 3, new Vector3D(3.1/3.7, 1, 0.6/3.7));
        leftView.smev(1, 3, 4, new Vector3D(1.6/3.7, 1, 1.2/3.7));
        leftView.smev(1, 4, 5, new Vector3D(0, 1, 1.2/3.7));
        leftView.mef(1, 1, 5, 4, 1, 2, 2);

        Matrix4x4 T = new Matrix4x4();
        T.translation(0, -1.0, 0);
        GeometricModeler.translationalSweepExtrudeFacePlanar(
            leftView, leftView.findFace(1), T);

        //-----------------------------------------------------------------
        PolyhedralBoundedSolid frontView;
        frontView = new PolyhedralBoundedSolid();
        frontView.mvfs(new Vector3D(0, 0, 0), 1, 1);
        frontView.smev(1, 1, 2, new Vector3D(0, 1, 0));
        frontView.smev(1, 2, 3, new Vector3D(0, 1, 1.2/3.7));
        frontView.smev(1, 3, 4, new Vector3D(0, 2.8/3.7, 1.2/3.7));
        frontView.smev(1, 4, 5, new Vector3D(0, 2.8/3.7, 0.3/3.7));
        frontView.smev(1, 5, 6, new Vector3D(0, 0.9/3.7, 0.3/3.7));
        frontView.smev(1, 6, 7, new Vector3D(0, 0.9/3.7, 1.2/3.7));
        frontView.smev(1, 7, 8, new Vector3D(0, 0, 1.2/3.7));
        frontView.mef(1, 1, 8, 7, 1, 2, 2);

        T = new Matrix4x4();
        T.translation(1, 0, 0);
        GeometricModeler.translationalSweepExtrudeFacePlanar(
            frontView, frontView.findFace(1), T);

        //-----------------------------------------------------------------
        operands[0] = leftView;
        operands[1] = frontView;

        return operands;
    }

    /**
    This method creates a simple test model pair of objects similar to that
    shown on figure [MANT1988].15.1. These two extrusion solids are an
    interesting example of view profiles, where a 3D solid can be reconstructed
    from its left and front view using boolean set operations (a.k.a. 
    "profile set operations" at section [MANT1988].6.4.2.). As commented
    on section [MANT1988].15.1., this simple pair of solids are difficult
    to intersect due to its overlaping faces.
    */
    public static PolyhedralBoundedSolid[] createTestObjectPairMANT1988_15_1()
    {
        PolyhedralBoundedSolid operands[];

        operands = new PolyhedralBoundedSolid[2];

        //-----------------------------------------------------------------
        PolyhedralBoundedSolid leftView;
        leftView = new PolyhedralBoundedSolid();
        leftView.mvfs(new Vector3D(0, 1, 0), 1, 1);
        leftView.smev(1, 1, 2, new Vector3D(1, 1, 0));
        leftView.smev(1, 2, 3, new Vector3D(1, 1, 0.25));
        leftView.smev(1, 3, 4, new Vector3D(1.0/3.0, 1, 0.25));
        leftView.smev(1, 4, 5, new Vector3D(1.0/3.0, 1, 1));
        leftView.smev(1, 5, 6, new Vector3D(0, 1, 1));
        leftView.mef(1, 1, 6, 5, 1, 2, 2);

        Matrix4x4 T = new Matrix4x4();
        T.translation(0, -1.0, 0);
        GeometricModeler.translationalSweepExtrudeFacePlanar(
            leftView, leftView.findFace(1), T);

        //-----------------------------------------------------------------
        PolyhedralBoundedSolid frontView;
        frontView = new PolyhedralBoundedSolid();
        frontView.mvfs(new Vector3D(0, 0, 0), 1, 1);
        frontView.smev(1, 1, 2, new Vector3D(0, 1, 0));
        frontView.smev(1, 2, 3, new Vector3D(0, 1, 7.0/12.0));
        frontView.smev(1, 3, 4, new Vector3D(0, 5.0/6.0, 1));
        frontView.smev(1, 4, 5, new Vector3D(0, 1.0/6.0, 1));
        frontView.smev(1, 5, 6, new Vector3D(0, 0, 7.0/12.0));
        frontView.mef(1, 1, 6, 5, 1, 2, 2);

        T = new Matrix4x4();
        T.translation(1, 0, 0);
        GeometricModeler.translationalSweepExtrudeFacePlanar(
            frontView, frontView.findFace(1), T);

        //-----------------------------------------------------------------
        operands[0] = leftView;
        operands[1] = frontView;

        return operands;
    }

    /**
    This method creates a simple test model pair of objects similar to that
    shown on figures [MANT1988].15.2. and [MANT1988].15.3. These pair of solid
    are interesing when the position of the wedge is such that its upper edge
    touches the upper face of the block.
    Situation can be: 
      -1: Holed object
      0: Limit case
      1: Open object
    */
    public static PolyhedralBoundedSolid[] createTestObjectPairMANT1988_15_2(
        int situation)
    {
        PolyhedralBoundedSolid operands[];

        operands = new PolyhedralBoundedSolid[2];

        //-----------------------------------------------------------------
        PolyhedralBoundedSolid block;
        Box box;
        Matrix4x4 T;

        T = new Matrix4x4();
        T.translation(0.25+0.1375, 0.5, 0.3);

        box = new Box(new Vector3D(0.5, 1, 0.6));
        block = box.exportToPolyhedralBoundedSolid();
        block.applyTransformation(T);
        block.validateModel();

        //-----------------------------------------------------------------
        PolyhedralBoundedSolid wedge;
        //double delta = 0.00001;
        double delta = 0.05;

        wedge = new PolyhedralBoundedSolid();

        switch ( situation ) {
          case 0: default:
            // Original on edge wedge, generating non manifold object
            wedge.mvfs(new Vector3D(0, 0.225, 0.3), 1, 1);
            wedge.smev(1, 1, 2, new Vector3D(0, 0.775, 0.3));
            wedge.smev(1, 2, 3, new Vector3D(0, 0.5, 0.6));
            break;

          case -1:
            // Lowered wedge, generating a closed holed object
            wedge.mvfs(new Vector3D(0, 0.225, 0.3-delta), 1, 1);
            wedge.smev(1, 1, 2, new Vector3D(0, 0.775, 0.3-delta));
            wedge.smev(1, 2, 3, new Vector3D(0, 0.5, 0.6-delta));
            break;

          case 1:
            // Raised wedge, generating an open object with no holes
            wedge.mvfs(new Vector3D(0, 0.225, 0.3+delta), 1, 1);
            wedge.smev(1, 1, 2, new Vector3D(0, 0.775, 0.3+delta));
            wedge.smev(1, 2, 3, new Vector3D(0, 0.5, 0.6+delta));
            break;
        }

        wedge.mef(1, 1, 3, 2, 1, 2, 2);

        T = new Matrix4x4();
        T.translation(0.775, 0, 0);
        GeometricModeler.translationalSweepExtrudeFacePlanar(
            wedge, wedge.findFace(1), T);

        //-----------------------------------------------------------------
        operands[0] = block;
        operands[1] = wedge;

        return operands;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
