//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - March 16 2006 - Oscar Chavarro: Original base version                 =
//===========================================================================

package vsdk.toolkit.gui;

// Java basic classes
import java.util.ArrayList;

// VSDK classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.common.Ray;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.environment.Material;
import vsdk.toolkit.environment.geometry.Arrow;
import vsdk.toolkit.environment.geometry.Box;
import vsdk.toolkit.environment.geometry.Cone;
import vsdk.toolkit.environment.geometry.Geometry;
import vsdk.toolkit.environment.geometry.InfinitePlane;
import vsdk.toolkit.environment.geometry.ParametricCurve;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.processing.GeometricModeler;

public class TranslateGizmo extends Gizmo {
    /// Internal transformation state
    private Matrix4x4 T;

    private Camera camera;

    /// Geometric model based in primitive instancing: primitive concretions
    private final Arrow arrowModel;
    private final Cone cylinderModel;
    private final Box boxModel;
    private final Cone coneModel;

    /// Geometric model based in primitive instancing: primitive instances
    /// This list is always of size 12, and its elements follow the order 
    /// indicated in the values of the *_ELEMENT constants of this class.
    ArrayList<SimpleBody> elementInstances;
    ArrayList<SimpleBody> elementInstances3dsmax;

    /// Internal element selection state
    public static final int X_AXIS_ELEMENT = 1;
    public static final int Y_AXIS_ELEMENT = 2;
    public static final int Z_AXIS_ELEMENT = 3;
    public static final int XYY_SEGMENT_ELEMENT = 4;
    public static final int XYX_SEGMENT_ELEMENT = 5;
    public static final int YZZ_SEGMENT_ELEMENT = 6;
    public static final int YZY_SEGMENT_ELEMENT = 7;
    public static final int XZZ_SEGMENT_ELEMENT = 8;
    public static final int XZX_SEGMENT_ELEMENT = 9;
    public static final int XY_BOX_ELEMENT = 10;
    public static final int YZ_BOX_ELEMENT = 11;
    public static final int XZ_BOX_ELEMENT = 12;

    public static final int NULL_GROUP = 0;
    public static final int X_AXIS_GROUP = 1;
    public static final int Y_AXIS_GROUP = 2;
    public static final int Z_AXIS_GROUP = 3;
    public static final int XY_PLANE_GROUP = 4;
    public static final int YZ_PLANE_GROUP = 5;
    public static final int XZ_PLANE_GROUP = 6;

    private static final double SEGMENT_LENGHT = 0.32;
    private static final double SEGMENT_WIDTH = 0.02;
    private static final double BOX_SIDE = 0.3;
    private static final double BOX_HEIGHT = 0.01;
    private static final double ARROW_LENGHT = 1.0;

    private int aparentSizeInPixels;

    /// Interaction state
    private int persistentSelection;
    private int volatileSelection;

    //private Robot awtRobot;
    //private boolean skipRobot;
    private int oldmousex;
    private int oldmousey;
    private Vector3D lastDeltaPosition;
    private boolean selectedResizing;
    private double currentScale;

    private boolean active;

    public TranslateGizmo(Camera cam)
    {
        aparentSizeInPixels = 100;
        persistentSelection = X_AXIS_GROUP;
        volatileSelection = NULL_GROUP;

        // Total arrow lenght = 0.2 empty + 0.5 base + 0.3 head
        arrowModel = new Arrow(0.5*ARROW_LENGHT, 0.3*ARROW_LENGHT, 0.025, 0.05);
        cylinderModel = new Cone(SEGMENT_WIDTH, SEGMENT_WIDTH, SEGMENT_LENGHT);
        boxModel = new Box(BOX_SIDE, BOX_SIDE, BOX_HEIGHT);
        coneModel = new Cone(0.05, 0, 0.3*ARROW_LENGHT);

        elementInstances = new ArrayList<SimpleBody>();
        for ( int i = 0; i < 12; i++ ) {
            SimpleBody r = new SimpleBody();
            elementInstances.add(r);
        }

        elementInstances3dsmax = new ArrayList<SimpleBody>();
        for ( int i = 0; i < 15; i++ ) {
            SimpleBody r = new SimpleBody();
            elementInstances3dsmax.add(r);
        }

        setCamera(cam);
        //awtRobot = null;
        //skipRobot = false;
        oldmousex = 0;
        oldmousey = 0;
        selectedResizing = true;
        currentScale = 1.0;
        active = false;
        lastDeltaPosition = new Vector3D();
    }

    public int getAparentSizeInPixels()
    {
        return aparentSizeInPixels;
    }

    public void setAparentSizeInPixels(int du)
    {
        aparentSizeInPixels = du;
    }

    public final void setCamera(Camera cam)
    {
        camera = cam;
    }

    public Camera getCamera()
    {
        return camera;
    }

    public ArrayList<SimpleBody> getElements()
    {
        return elementInstances;
    }

    public ArrayList<SimpleBody> getElements3dsmax()
    {
        int i;
        SimpleBody r, o, r2;
        Geometry g;
        Material red = createMaterial(0.78, 0, 0);
        Material green = createMaterial(0, 0.61, 0);
        Material blue = createMaterial(0, 0, 0.76);

        Matrix4x4 R = new Matrix4x4(T);
        R.M[3][0] = 0.0;
        R.M[3][1] = 0.0;
        R.M[3][2] = 0.0;
        R.M[0][3] = 0.0;
        R.M[1][3] = 0.0;
        R.M[2][3] = 0.0;
        R.M[3][3] = 1.0;
        Matrix4x4 subR = new Matrix4x4();
        Matrix4x4 eleR, eleRi;
        Vector3D subP;
        Vector3D eleP;

        coneModel.setBaseRadius(currentScale*0.05);
        coneModel.setHeight(currentScale*0.3*ARROW_LENGHT);
        boxModel.setSize(currentScale*(BOX_SIDE+0.025), currentScale*(BOX_SIDE+0.025), currentScale*BOX_HEIGHT);

        //-----------------------------------------------------------------
        ParametricCurve lineModel;
        ParametricCurve segmentModel;

        lineModel = GeometricModeler.createLine(0, 0, 0,
            0, 0, currentScale*0.7);

        segmentModel = GeometricModeler.createLine(0, 0, 0,
            0, 0, currentScale*SEGMENT_LENGHT);

        //-----------------------------------------------------------------
        for ( i = 0; i < elementInstances.size(); i++ ) {
            r = elementInstances3dsmax.get(i);
            o = elementInstances.get(i);

            r.setMaterial(o.getMaterial()); 
            r.setRotation(o.getRotation());
            r.setRotationInverse(o.getRotationInverse());

            g = o.getGeometry();
            if ( g != null && g instanceof Arrow ) {
                r.setGeometry(coneModel);
                switch ( i ) {
                  case 0:
                    // Rotation
                    subR.axisRotation(Math.toRadians(90.0), 0, 1, 0);
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(0, 0, currentScale*0.7*ARROW_LENGHT);
                    eleP = eleR.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                    r.setMaterial(red); 
                    break;
                  case 1:
                    // Rotation
                    subR.axisRotation(Math.toRadians(90.0), -1, 0, 0);
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(0, 0, currentScale*0.7*ARROW_LENGHT);
                    eleP = eleR.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                    r.setMaterial(green); 
                    break;
                  case 2:
                    // Rotation
                    subR = new Matrix4x4();
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(0, 0, currentScale*0.7*ARROW_LENGHT);
                    eleP = eleR.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                    r.setMaterial(blue); 
                    break;
                }

                r2 = elementInstances3dsmax.get(i+12);
                r2.setMaterial(o.getMaterial()); 
                r2.setRotation(o.getRotation());
                r2.setRotationInverse(o.getRotationInverse());
                r2.setPosition(o.getPosition());
                r2.setGeometry(lineModel);
            }
            else if ( g != null && g instanceof Cone ) {
                r.setPosition(o.getPosition());
                r.setGeometry(segmentModel);
            }
            else {
                r.setPosition(o.getPosition());
                r.setGeometry(g);
            }
        }

        return elementInstances3dsmax;
    }

    private Material createMaterial(double r, double g, double b)
    {
        Material m = new Material();

        m.setAmbient(new ColorRgb(0.2, 0.2, 0.2));
        m.setDiffuse(new ColorRgb(r, g, b));
        m.setSpecular(new ColorRgb(1, 1, 1));
        return m;
    }

    /**
    This method updates the data structure contained in the `elementInstances`
    array starting from the given parameters.
    - translation is the position of the center of the gizmo
    - rotation is the rotation matrix containing the orientation of the gizmo
    - if autosize is false, initialdu, initialdv and camera parameters are not
      used, and the gizmo doesn't change its current size. If autosize is true,
      the gizmo size is changed such as from the current camera, the gizmo
      projection fit a 2D area of initialdu * initialdv pixels.
    - modelType must be one of the following values: MODEL_FOR_GRAVITY or
      MODEL_FOR_DISPLAY. Depending on this value the size of current
      geometric elements could change.
      @param translation
      @param rotation
      @param autosize
      @param initialdu
      @param camera
    */
    public void calculateGeometryState(Vector3D translation, Matrix4x4 rotation,
                                       boolean autosize, int initialdu,
                                       Camera camera)
    {
        //-----------------------------------------------------------------
        int i;
        Material red = createMaterial(0.78, 0, 0);
        Material green = createMaterial(0, 0.61, 0);
        Material blue = createMaterial(0, 0, 0.76);
        Material yellow = createMaterial(1, 1, 0);
        Material yellowTransparent = createMaterial(1, 1, 0);

        yellowTransparent.setOpacity(0.2);

        int currentSelection;
        if ( volatileSelection == NULL_GROUP ) {
            currentSelection = persistentSelection;
        }
        else {
            currentSelection = volatileSelection;
        }

        Matrix4x4 R = new Matrix4x4(T);
        R.M[3][0] = 0.0;
        R.M[3][1] = 0.0;
        R.M[3][2] = 0.0;
        R.M[0][3] = 0.0;
        R.M[1][3] = 0.0;
        R.M[2][3] = 0.0;
        R.M[3][3] = 1.0;
        Matrix4x4 subR = new Matrix4x4();
        Matrix4x4 eleR, eleRi;
        Vector3D subP;
        Vector3D eleP;

        camera.updateVectors();

        if ( selectedResizing ) {
            Vector3D a = new Vector3D();
            Vector3D b = new Vector3D();
            Vector3D p = getPosition();
            Vector3D right = camera.getLeft().multiply(-1);

            right.normalize();
            camera.projectPointUsingRayMethod(p, a);
            camera.projectPointUsingRayMethod(p.add(right), b);
            double factor = VSDK.vectorDistance(a, b);
            currentScale = ((double)initialdu)/factor;
        }
        double scale = currentScale;

        arrowModel.setBaseLength(scale*0.5*ARROW_LENGHT);
        arrowModel.setHeadLength(scale*0.3*ARROW_LENGHT);
        arrowModel.setBaseRadius(scale*0.025);
        arrowModel.setHeadRadius(scale*0.05);
        cylinderModel.setBaseRadius(scale*SEGMENT_WIDTH);
        cylinderModel.setTopRadius(scale*SEGMENT_WIDTH);
        cylinderModel.setHeight(scale*SEGMENT_LENGHT);
        boxModel.setSize(scale*BOX_SIDE, scale*BOX_SIDE, scale*BOX_HEIGHT);

        //-----------------------------------------------------------------
        Vector3D front = camera.getFront();
        Vector3D axisI = new Vector3D(1, 0, 0);
        Vector3D axisJ = new Vector3D(0, 1, 0);
        Vector3D axisK = new Vector3D(0, 0, 1);
        boolean orthogonalCamera;
        boolean iPar;
        boolean jPar;
        boolean kPar;

        orthogonalCamera = (camera.getProjectionMode() == Camera.PROJECTION_MODE_ORTHOGONAL);
        iPar = (Math.abs(front.dotProduct(axisI)) > 1.0-VSDK.EPSILON);
        jPar = (Math.abs(front.dotProduct(axisJ)) > 1.0-VSDK.EPSILON);
        kPar = (Math.abs(front.dotProduct(axisK)) > 1.0-VSDK.EPSILON);

        int index;
        for ( i = 0, index = 1;
              index <= 12 && i < elementInstances.size();
              index++, i++ ) {
            SimpleBody r = elementInstances.get(i);
            r.setGeometry(null);
            switch ( index ) {
              case X_AXIS_ELEMENT:
                if ( !(orthogonalCamera && iPar) ) {
                    // Basic model
                    r.setGeometry(arrowModel);
                    if ( currentSelection == X_AXIS_GROUP ||
                         currentSelection == XY_PLANE_GROUP ||
                         currentSelection == XZ_PLANE_GROUP ) {
                        r.setMaterial(yellow); 
                    }
                    else {
                        r.setMaterial(red);
                    }
                    // Rotation
                    subR.axisRotation(Math.toRadians(90.0), 0, 1, 0);
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(0, 0, scale*0.2*ARROW_LENGHT);
                    eleP = eleR.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                }
                break;
              case Y_AXIS_ELEMENT:
                  if ( !(orthogonalCamera && jPar) ) {
                    // Basic model
                    r.setGeometry(arrowModel);
                    if ( currentSelection == Y_AXIS_GROUP ||
                         currentSelection == XY_PLANE_GROUP ||
                         currentSelection == YZ_PLANE_GROUP ) {
                        r.setMaterial(yellow); 
                    }
                    else {
                        r.setMaterial(green);
                    }
                    // Rotation
                    subR.axisRotation(Math.toRadians(90.0), -1, 0, 0);
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(0, 0, scale*0.2*ARROW_LENGHT);
                    eleP = eleR.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                }
                break;
              case Z_AXIS_ELEMENT:
                if ( !(orthogonalCamera && kPar) ) {
                    // Basic model
                    r.setGeometry(arrowModel);
                    if ( currentSelection == Z_AXIS_GROUP ||
                         currentSelection == YZ_PLANE_GROUP ||
                         currentSelection == XZ_PLANE_GROUP ) {
                        r.setMaterial(yellow); 
                    }
                    else {
                        r.setMaterial(blue);
                    }
                    // Rotation
                    subR = new Matrix4x4();
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(0, 0, scale*0.2*ARROW_LENGHT);
                    eleP = eleR.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                }
                break;
              case XYY_SEGMENT_ELEMENT:
                if ( !(orthogonalCamera && (iPar || jPar)) ) {
                    // Basic model
                    r.setGeometry(cylinderModel);
                    if ( currentSelection == XY_PLANE_GROUP ) {
                        r.setMaterial(yellow); 
                    }
                    else {
                        r.setMaterial(green);
                    }
                    // Rotation
                    subR = new Matrix4x4();
                    subR.axisRotation(Math.toRadians(90.0), 0, 1, 0);
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(0, scale*SEGMENT_LENGHT, 0);
                    eleP = eleR.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                }
                break;
              case XYX_SEGMENT_ELEMENT:
                if ( !(orthogonalCamera && (iPar || jPar)) ) {
                    // Basic model
                    r.setGeometry(cylinderModel);
                    if ( currentSelection == XY_PLANE_GROUP ) {
                        r.setMaterial(yellow); 
                    }
                    else {
                        r.setMaterial(red);
                    }
                    // Rotation
                    subR = new Matrix4x4();
                    subR.axisRotation(Math.toRadians(90.0), -1, 0, 0);
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(scale*SEGMENT_LENGHT, 0, 0);
                    eleP = eleR.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                }
                break;
              case YZZ_SEGMENT_ELEMENT:
                // Basic model
                if ( !(orthogonalCamera && (jPar || kPar)) ) {
                    r.setGeometry(cylinderModel);
                    if ( currentSelection == YZ_PLANE_GROUP ) {
                        r.setMaterial(yellow); 
                    }
                    else {
                        r.setMaterial(blue);
                    }
                    // Rotation
                    subR = new Matrix4x4();
                    subR.axisRotation(Math.toRadians(90.0), -1, 0, 0);
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(0, 0, scale*SEGMENT_LENGHT);
                    eleP = R.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                }
                break;
              case YZY_SEGMENT_ELEMENT:
                if ( !(orthogonalCamera && (jPar || kPar)) ) {
                    // Basic model
                    r.setGeometry(cylinderModel);
                    if ( currentSelection == YZ_PLANE_GROUP ) {
                        r.setMaterial(yellow); 
                    }
                    else {
                        r.setMaterial(green);
                    }
                    // Rotation
                    subR = new Matrix4x4();
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(0, scale*SEGMENT_LENGHT, 0);
                    eleP = R.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                }
                break;
              case XZZ_SEGMENT_ELEMENT:
                if ( !(orthogonalCamera && (iPar || kPar)) ) {
                    // Basic model
                    r.setGeometry(cylinderModel);
                    if ( currentSelection == XZ_PLANE_GROUP ) {
                        r.setMaterial(yellow); 
                    }
                    else {
                        r.setMaterial(blue);
                    }
                    // Rotation
                    subR = new Matrix4x4();
                    subR.axisRotation(Math.toRadians(90.0), 0, 1, 0);
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(0, 0, scale*SEGMENT_LENGHT);
                    eleP = R.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                }
                break;
              case XZX_SEGMENT_ELEMENT:
                if ( !(orthogonalCamera && (iPar || kPar)) ) {
                    // Basic model
                    r.setGeometry(cylinderModel);
                    if ( currentSelection == XZ_PLANE_GROUP ) {
                        r.setMaterial(yellow); 
                    }
                    else {
                        r.setMaterial(red);
                    }
                    // Rotation
                    subR = new Matrix4x4();
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(scale*SEGMENT_LENGHT, 0, 0);
                    eleP = R.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                }
                break;
              case XY_BOX_ELEMENT:
                if ( !(orthogonalCamera && (iPar || jPar)) ) {
                    // Basic model
                    r.setGeometry(null);
                    if ( currentSelection != XY_PLANE_GROUP ) {
                        break;
                    }
                    r.setGeometry(boxModel);
                    r.setMaterial(yellowTransparent); 
                    // Rotation
                    subR = new Matrix4x4();
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(scale*BOX_SIDE/2, scale*BOX_SIDE/2, 0);
                    eleP = R.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                }
                break;
              case YZ_BOX_ELEMENT:
                if ( !(orthogonalCamera && (jPar || kPar)) ) {
                    // Basic model
                    r.setGeometry(null);
                    if ( currentSelection != YZ_PLANE_GROUP ) {
                        break;
                    }
                    r.setGeometry(boxModel);
                    r.setMaterial(yellowTransparent); 
                    // Rotation
                    subR = new Matrix4x4();
                    subR.axisRotation(Math.toRadians(90.0), 0, 1, 0);
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(0, scale*BOX_SIDE/2, scale*BOX_SIDE/2);
                    eleP = R.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                }
                break;
              case XZ_BOX_ELEMENT:
                if ( !(orthogonalCamera && (iPar || kPar)) ) {
                    // Basic model
                    r.setGeometry(null);
                    if ( currentSelection != XZ_PLANE_GROUP ) {
                        break;
                    }
                    r.setGeometry(boxModel);
                    r.setMaterial(yellowTransparent); 
                    // Rotation
                    subR = new Matrix4x4();
                    subR.axisRotation(Math.toRadians(90.0), 1, 0, 0);
                    eleR = R.multiply(subR);
                    r.setRotation(eleR);
                    eleRi = new Matrix4x4(eleR);
                    eleRi.invert();
                    r.setRotationInverse(eleRi);
                    // Translation
                    subP = new Vector3D(scale*BOX_SIDE/2, 0, scale*BOX_SIDE/2);
                    eleP = R.multiply(subP).add(getPosition());
                    r.setPosition(eleP);
                }
                break;
            }
        }
    }

    public Vector3D getPosition()
    {
        Vector3D p = new Vector3D(T.M[0][3], T.M[1][3], T.M[2][3]);
        return p;
    }

    public void setPosition(Vector3D p)
    {
        T.M[0][3] = p.x;
        T.M[1][3] = p.y;
        T.M[2][3] = p.z;
    }

    public void setTransformationMatrix(Matrix4x4 T)
    {
        this.T = T;

        Matrix4x4 R = new Matrix4x4(T);
        R.M[3][0] = 0.0;
        R.M[3][1] = 0.0;
        R.M[3][2] = 0.0;
        R.M[0][3] = 0.0;
        R.M[1][3] = 0.0;
        R.M[2][3] = 0.0;
        R.M[3][3] = 1.0;
        calculateGeometryState(getPosition(), 
                               R, selectedResizing, aparentSizeInPixels,
                               camera);
    }

    public Matrix4x4 getTransformationMatrix()
    {
        return T;
    }

    public boolean processMouseEvent(MouseEvent mouseEvent)
    {
        oldmousex = mouseEvent.getX();
        oldmousey = mouseEvent.getY();
        return false;
    }

    public boolean processKeyPressedEvent(KeyEvent keyEvent)
    {
        char unicode_id;
        int keycode;
        double deltaMov = 0.1;
        boolean updateNeeded = false;

        unicode_id = keyEvent.unicode_id;
        keycode = keyEvent.keycode;

        Matrix4x4 R = new Matrix4x4(T);
        R.M[3][0] = 0.0;
        R.M[3][1] = 0.0;
        R.M[3][2] = 0.0;
        R.M[0][3] = 0.0;
        R.M[1][3] = 0.0;
        R.M[2][3] = 0.0;
        R.M[3][3] = 1.0;

        selectedResizing = true;
        calculateGeometryState(new Vector3D(T.M[0][3], T.M[1][3], T.M[2][3]), 
                               R, selectedResizing, aparentSizeInPixels, 
                               camera);

        if ( unicode_id != KeyEvent.KEY_NONE ) {
            switch ( unicode_id ) {
              // Position
              case 'x':
                T.M[0][3] -= deltaMov;
                updateNeeded = true;
                break;
              case 'X':
                T.M[0][3] += deltaMov;
                updateNeeded = true;
                break;
              case 'y':
                T.M[1][3] -= deltaMov;
                updateNeeded = true;
                break;
              case 'Y':
                T.M[1][3] += deltaMov;
                updateNeeded = true;
                break;
              case 'z':
                T.M[2][3] -= deltaMov;
                updateNeeded = true;
                break;
              case 'Z':
                T.M[2][3] += deltaMov;
                updateNeeded = true;
                break; 
            }
        }

        return updateNeeded;
    }

    public boolean processKeyReleasedEvent(KeyEvent keyEvent)
    {
        return false;
    }

    public boolean processMousePressedEvent(MouseEvent e)
    {
        lastDeltaPosition = new Vector3D();
        //lastDeltaPosition = calculateDeltaPosition(mouseEvent);
        //Vector3D p = calculateInteractionPosition(e); // Not working!
        //* HOW TO REFACTOR FROM HERE *************************************

        //- Configure sub-interaction technique from active element -------
        int currentSelection;
        if ( volatileSelection == NULL_GROUP ) {
            currentSelection = persistentSelection;
        }
        else {
            currentSelection = volatileSelection;
        }

        Vector3D v = null;
        int interactionTechnique = 0;

        switch ( currentSelection ) {
          case X_AXIS_GROUP: 
            v = new Vector3D(1, 0, 0);
            interactionTechnique = 1; // Vector
            break;
          case Y_AXIS_GROUP: 
            v = new Vector3D(0, 1, 0);
            interactionTechnique = 1; // Vector
            break;
          case Z_AXIS_GROUP: 
            v = new Vector3D(0, 0, 1);
            interactionTechnique = 1; // Vector
            break;
          case XY_PLANE_GROUP: 
            v = new Vector3D(0, 0, 1);
            interactionTechnique = 2; // Plane
            break;
          case YZ_PLANE_GROUP: 
            v = new Vector3D(1, 0, 0);
            interactionTechnique = 2; // Plane
            break;
          case XZ_PLANE_GROUP: 
            v = new Vector3D(0, 1, 0);
            interactionTechnique = 2; // Plane
            break;
        }

        if ( v == null ) {
            oldmousex = e.getX();
            oldmousey = e.getY();
            return false;
        }

        //- Implement interaction technique for selected element ----------
        Vector3D o = getPosition();
        Vector3D p = new Vector3D(0, 0, 0);
        Ray r;
        InfinitePlane plane;
        int mousex = e.getX();
        int mousey = e.getY();
        Vector3D deltapos = new Vector3D();

        camera.updateVectors();

        if ( interactionTechnique == 2 ) {
            r = camera.generateRay(mousex, mousey);
            if ( r.direction.dotProduct(v) > 0 ) {
                v = v.multiply(-1);
            }
            plane = new InfinitePlane(v, o);
            if ( !plane.doIntersection(r) ) {
                oldmousex = e.getX();
                oldmousey = e.getY();
                p = o;
            }
            else {
                p = r.direction.multiply(r.t).add(r.origin);
            }
        }
        else if ( interactionTechnique == 1 ) {
            boolean accountForU = false;
            boolean accountForV = false;
            Vector3D left, up;

            left = camera.getLeft();
            up = camera.getUp();

            v.normalize();
            left.normalize();
            up.normalize();

            if ( Math.abs(v.dotProduct(left)) > Math.cos(Math.toRadians(80.0)) ) {
                accountForU = true;
            }
            if ( Math.abs(v.dotProduct(up)) > Math.cos(Math.toRadians(80.0)) ) {
                accountForV = true;
            }

            if ( accountForU && !accountForV ) {
                plane = camera.calculateUPlaneAtPixel(mousex, mousey);
            }
            else if ( accountForV && !accountForU ) {
                plane = camera.calculateVPlaneAtPixel(mousex, mousey);
            }
            else if ( accountForU && accountForV ) {
                if ( (mousex-oldmousex) > (mousey-oldmousey) ) {
                    plane = camera.calculateUPlaneAtPixel(mousex, mousey);
                }
                else {
                    plane = camera.calculateVPlaneAtPixel(mousex, mousey);
                }
            }
            else {
                oldmousex = e.getX();
                oldmousey = e.getY();
                return false;
            }

            r = new Ray(o, v);
            Ray r2 = new Ray(o, v);
            if ( !plane.doIntersectionWithNegative(r) ) {
                oldmousex = e.getX();
                oldmousey = e.getY();
                return false;
            }
            p = r.origin.add(r.direction.multiply(r.t));
        }
        oldmousex = e.getX();
        oldmousey = e.getY();
        //* HOW TO REFACTOR TO HERE ***************************************
        lastDeltaPosition = p.substract(getPosition());

        return false;
    }

    public boolean processMouseReleasedEvent(MouseEvent e)
    {
        selectedResizing = true;
        calculateGeometryState(getPosition(), T, selectedResizing, 
                               aparentSizeInPixels, camera);
        return true;
    }

    public boolean processMouseClickedEvent(MouseEvent e)
    {
        selectedResizing = true;
        calculateGeometryState(getPosition(), T, selectedResizing, 
                               aparentSizeInPixels, camera);
        int previousSelection = volatileSelection;

        if ( volatileSelection == NULL_GROUP ) {
            previousSelection = persistentSelection;
        }

        persistentSelection = calculateSelection(e.getX(), e.getY());

        if ( persistentSelection == NULL_GROUP ) {
            persistentSelection = previousSelection;
        }

        if ( persistentSelection != previousSelection ) {
            return true;
        }

        return false;
    }

    /**
    Given a pixel coordinate, this method traces a ray from current camera
    to gizmo's geometry and determines the constituent element selected.
    */
    private int calculateSelection(int x, int y)
    {
        camera.updateVectors();
        Ray r = camera.generateRay(x, y);
        double nearestDistance = Double.MAX_VALUE;
        int nearestElement = -1;
        int index = 1, i;

        /* Note that box elements are only for display, they do not affect
           gravity selections */
        for ( i = 0; index <= 9 && i < elementInstances.size(); index++, i++ ) {
            r.t = Double.MAX_VALUE;
            SimpleBody gi = elementInstances.get(i);

            if ( gi.getGeometry() != null && gi.doIntersection(r) &&
                 r.t < nearestDistance ) {
                nearestDistance = r.t;
                nearestElement = index;
            }
        }

        int selection = NULL_GROUP;

        switch ( nearestElement ) {
          case X_AXIS_ELEMENT: selection = X_AXIS_GROUP; break;
          case Y_AXIS_ELEMENT: selection = Y_AXIS_GROUP; break;
          case Z_AXIS_ELEMENT: selection = Z_AXIS_GROUP; break;
          case XYY_SEGMENT_ELEMENT: selection = XY_PLANE_GROUP; break;
          case XYX_SEGMENT_ELEMENT: selection = XY_PLANE_GROUP; break;
          case YZZ_SEGMENT_ELEMENT: selection = YZ_PLANE_GROUP; break;
          case YZY_SEGMENT_ELEMENT: selection = YZ_PLANE_GROUP; break;
          case XZZ_SEGMENT_ELEMENT: selection = XZ_PLANE_GROUP; break;
          case XZX_SEGMENT_ELEMENT: selection = XZ_PLANE_GROUP; break;
        }

        active = false;
        if ( selection != NULL_GROUP ) {
            active = true;
        }

        return selection;
    }

    public boolean isActive()
    {
        return active;
    }

    public boolean processMouseMovedEvent(MouseEvent e)
    {
        oldmousex = e.getX();
        oldmousey = e.getY();

        selectedResizing = true;
        calculateGeometryState(getPosition(), T, selectedResizing, 
                               aparentSizeInPixels, camera);
        int previousSelection = volatileSelection;

        if ( volatileSelection == NULL_GROUP ) {
            previousSelection = persistentSelection;
        }

        volatileSelection = calculateSelection(e.getX(), e.getY());

        if ( volatileSelection != previousSelection ) {
            return true;
        }

        return false;
    }

    /* TO REFACTOR CODE! */
    private Vector3D calculateInteractionPosition(MouseEvent e)
    {
        return null;
    }

    public boolean processMouseDraggedEvent(MouseEvent e)
    {
        //- If it is called as an automatic reposition, do nothing --------
/*
        if ( skipRobot ) {
            skipRobot = false;
            oldmousex = e.getX();
            oldmousey = e.getY();
            return false;
        }
*/

        //Vector3D p = calculateInteractionPosition(e); // Not working!
        //* HOW TO REFACTOR FROM HERE *************************************

        //- Configure sub-interaction technique from active element -------
        int currentSelection;
        if ( volatileSelection == NULL_GROUP ) {
            currentSelection = persistentSelection;
        }
        else {
            currentSelection = volatileSelection;
        }

        Vector3D v = null;
        int interactionTechnique = 0;

        switch ( currentSelection ) {
          case X_AXIS_GROUP: 
            v = new Vector3D(1, 0, 0);
            interactionTechnique = 1; // Vector
            break;
          case Y_AXIS_GROUP: 
            v = new Vector3D(0, 1, 0);
            interactionTechnique = 1; // Vector
            break;
          case Z_AXIS_GROUP: 
            v = new Vector3D(0, 0, 1);
            interactionTechnique = 1; // Vector
            break;
          case XY_PLANE_GROUP: 
            v = new Vector3D(0, 0, 1);
            interactionTechnique = 2; // Plane
            break;
          case YZ_PLANE_GROUP: 
            v = new Vector3D(1, 0, 0);
            interactionTechnique = 2; // Plane
            break;
          case XZ_PLANE_GROUP: 
            v = new Vector3D(0, 1, 0);
            interactionTechnique = 2; // Plane
            break;
        }

        if ( v == null ) {
            oldmousex = e.getX();
            oldmousey = e.getY();
            return false;
        }

        //- Implement interaction technique for selected element ----------
        Vector3D o = getPosition();
        Vector3D p = new Vector3D(0, 0, 0);
        Ray r;
        InfinitePlane plane;
        int mousex = e.getX();
        int mousey = e.getY();
        Vector3D deltapos = new Vector3D();

        camera.updateVectors();

        if ( interactionTechnique == 2 ) {
            r = camera.generateRay(mousex, mousey);
            if ( r.direction.dotProduct(v) > 0 ) {
                v = v.multiply(-1);
            }
            plane = new InfinitePlane(v, o);
            if ( !plane.doIntersection(r) ) {
                oldmousex = e.getX();
                oldmousey = e.getY();
                p = o;
            }
            else {
                p = r.direction.multiply(r.t).add(r.origin);
            }
        }
        else if ( interactionTechnique == 1 ) {
            boolean accountForU = false;
            boolean accountForV = false;
            Vector3D left, up;

            left = camera.getLeft();
            up = camera.getUp();

            v.normalize();
            left.normalize();
            up.normalize();

            if ( Math.abs(v.dotProduct(left)) > Math.cos(Math.toRadians(80.0)) ) {
                accountForU = true;
            }
            if ( Math.abs(v.dotProduct(up)) > Math.cos(Math.toRadians(80.0)) ) {
                accountForV = true;
            }

            if ( accountForU && !accountForV ) {
                plane = camera.calculateUPlaneAtPixel(mousex, mousey);
            }
            else if ( accountForV && !accountForU ) {
                plane = camera.calculateVPlaneAtPixel(mousex, mousey);
            }
            else if ( accountForU && accountForV ) {
                if ( (mousex-oldmousex) > (mousey-oldmousey) ) {
                    plane = camera.calculateUPlaneAtPixel(mousex, mousey);
                }
                else {
                    plane = camera.calculateVPlaneAtPixel(mousex, mousey);
                }
            }
            else {
                oldmousex = e.getX();
                oldmousey = e.getY();
                return false;
            }

            r = new Ray(o, v);
            Ray r2 = new Ray(o, v);
            if ( !plane.doIntersectionWithNegative(r) ) {
                oldmousex = e.getX();
                oldmousey = e.getY();
                return false;
            }
            p = r.origin.add(r.direction.multiply(r.t));
        }
        oldmousex = e.getX();
        oldmousey = e.getY();
        //* HOW TO REFACTOR TO HERE ***************************************

        setPosition(p.substract(lastDeltaPosition));
        selectedResizing = false;

        //- Automatic cursor repositioning constrain ----------------------
        // THIS IS NOT WORKING NICELY!
/*
        try {
            if ( awtRobot == null ) {
                awtRobot = new Robot();
            }

            Vector3D pp = new Vector3D();
            Vector3D base = p.add(deltapos);

            camera.projectPointUsingRayMethod(base, pp);

            Point global = e.getComponent().getLocationOnScreen();
            //awtRobot.mouseMove((int)pp.x+global.x, (int)pp.y+global.y);
            skipRobot = true;
        }
        catch ( Exception ex ) {
            System.err.println(ex);
        }
*/

        return true;
    }

    public boolean processMouseWheelEvent(MouseEvent e)
    {
        return false;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
