//===========================================================================
           /***************************************************
            *   An instructional Ray-Tracing Renderer written
            *   for MIT 6.837  Fall '98 by Leonard McMillan.
            *   Modified by Tomas Lozano-Perez for Fall '01
            *   Modified by Oscar Chavarro for Spring '04 
            *   FUSM 05061.
            *   Modified by Oscar Chavarro for PUJ Vitral 
            *   VSDK '05, '06, '10
            ****************************************************/
//===========================================================================

package vsdk.toolkit.io.geometry;

// Java classes
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

// VSDK classes
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.environment.Light;
import vsdk.toolkit.environment.Material;
import vsdk.toolkit.environment.Background;
import vsdk.toolkit.environment.SimpleBackground;
import vsdk.toolkit.environment.CubemapBackground;
import vsdk.toolkit.environment.geometry.Sphere;
import vsdk.toolkit.environment.geometry.Box;
import vsdk.toolkit.environment.geometry.Cone;
import vsdk.toolkit.environment.scene.SimpleBody;
import vsdk.toolkit.environment.scene.SimpleScene;
import vsdk.toolkit.media.RGBAImage;
import vsdk.toolkit.io.image.ImagePersistence;
import vsdk.toolkit.io.PersistenceElement;

/**
This class implements an scene reader based on the instructional raytracer
from Tomas Lozano-Perez from computer graphics class at MIT on spring 2001,
and from Leonard McMillan 1998. This material was adapted by Oscar Chavarro
for computer graphics classes at Colombia, and later as anoter scene reader
for Vitral.
*/
public class ReaderMitScene extends PersistenceElement
{
    // Debug flag
    private static final boolean showDebugMessages = false;

    // Simple scene
    public Camera currentCamera;
    public Background currentBackground;

    // Viewport size information
    public int viewportXSize;
    public int viewportYSize;

    public ReaderMitScene()
    {
        currentCamera = new Camera();
        currentBackground = new SimpleBackground();
        ((SimpleBackground)currentBackground).setColor(0, 0, 0);

        viewportXSize = 320;
        viewportYSize = 240;
    }

    private void
    showDebugMessage(String m)
    {
        if ( showDebugMessages ) {
            System.out.println(m);
        }
    }

    private double
    readNumber(StreamTokenizer st) throws IOException {
        if (st.nextToken() != StreamTokenizer.TT_NUMBER) {
            System.err.println("ERROR: number expected in line "+st.lineno());
            throw new IOException(st.toString());
        }
        return st.nval;
    }

    public void
    importEnvironment(InputStream is, SimpleScene theScene) throws Exception {
        Reader parsero = new BufferedReader(new InputStreamReader(is));
        StreamTokenizer st = new StreamTokenizer(parsero);
        st.commentChar('#');
        boolean fin_de_lectura = false;
        Material currentMaterial;

        // Material por defecto...
        /*
        currentMaterial = new Material(0.8f, 0.2f, 0.9f, 
                                       0.2f, 0.4f, 0.4f, 
                                       10.0f, 0f, 0f, 1f);
        */
        currentMaterial = new Material();
        currentMaterial.setAmbient(new ColorRgb(0.8*0.2, 0.2*0.2, 0.9*0.2));
        currentMaterial.setDiffuse(new ColorRgb(0.8*0.4, 0.2*0.4, 0.9*0.4));
        currentMaterial.setSpecular(new ColorRgb(0.8*0.4, 0.2*0.4, 0.9*0.4));
        currentMaterial.setReflectionCoefficient(0);
        currentMaterial.setRefractionCoefficient(0);
        currentMaterial.setPhongExponent(10);
        SimpleBody thing;
        Matrix4x4 R, Ri;
        double yaw_actual = 0;
        double pitch_actual = 0;
        double roll_actual = 0;

        while ( !fin_de_lectura ) {
          switch ( st.nextToken() ) {
            case StreamTokenizer.TT_WORD:
              if ( st.sval.equals("sphere") ) {
                  Vector3D c = new Vector3D(readNumber(st), 
                                            readNumber(st), 
                                            readNumber(st));
                  double r = readNumber(st);

                  showDebugMessage("sphere");
                  thing = new SimpleBody();
                  thing.setGeometry(new Sphere(r));
                  thing.setMaterial(currentMaterial);

                  R = new Matrix4x4();
                  R.eulerAnglesRotation(yaw_actual, pitch_actual, roll_actual);
                  thing.setRotation(R);
                  Ri = new Matrix4x4(R);
                  Ri.invert();
                  thing.setRotationInverse(Ri);
                  thing.setPosition(c);
                  theScene.addBody(thing);
                }
                else if ( st.sval.equals("cube") ) {
                  Vector3D c = new Vector3D(readNumber(st), 
                                            readNumber(st), 
                                            readNumber(st));
                  double r = readNumber(st);

                  showDebugMessage("cube");
                  thing = new SimpleBody();
                  thing.setGeometry(new Box(r, r, r));
                  thing.setMaterial(currentMaterial);
                  R = new Matrix4x4();
                  R.eulerAnglesRotation(yaw_actual, pitch_actual, roll_actual);
                  thing.setRotation(R);
                  Ri = new Matrix4x4(R);
                  Ri.invert();
                  thing.setRotationInverse(Ri);
                  thing.setPosition(c);
                  theScene.addBody(thing);
                } 
                else if ( st.sval.equals("cylinder") ) {
                  Vector3D c = new Vector3D(readNumber(st), 
                                            readNumber(st), 
                                            readNumber(st));
                  double r1 = readNumber(st);
                  double r2 = readNumber(st);
                  double h = readNumber(st);

                  showDebugMessage("cylinder");
                  thing = new SimpleBody();
                  thing.setGeometry(new Cone(r1, r2, h));
                  thing.setMaterial(currentMaterial);
                  R = new Matrix4x4();
                  R.eulerAnglesRotation(yaw_actual, pitch_actual, roll_actual);
                  thing.setRotation(R);
                  Ri = new Matrix4x4(R);
                  Ri.invert();
                  thing.setRotationInverse(Ri);
                  thing.setPosition(c);
                  theScene.addBody(thing);
                }
                /*
                else if (st.sval.equals("triangles")) {
                  showDebugMessage("triangles");
                  thing = new SimpleBody();
                  thing.setGeometry(new MESH(st));
                  thing.setMaterial(currentMaterial);
                  theScene.addBody(thing);
                } 
                */
                else if (st.sval.equals("viewport")) {
                  showDebugMessage("viewport");

                  viewportXSize = (int)readNumber(st);
                  viewportYSize = (int)readNumber(st);
                }
                else if (st.sval.equals("eye")) {
                  showDebugMessage("eye");
                  currentCamera.setPosition(new Vector3D(readNumber(st), 
                                                  readNumber(st), 
                                                  readNumber(st)));
                }
                else if (st.sval.equals("lookat")) {
                  showDebugMessage("lookat");
                  currentCamera.setFocusedPositionMaintainingOrthogonality(new Vector3D(readNumber(st), 
                                                      readNumber(st), 
                                                      readNumber(st)));
                }
                else if (st.sval.equals("up")) {
                  showDebugMessage("up");
                  currentCamera.setUpDirect(new Vector3D(readNumber(st), 
                                            readNumber(st), 
                                            readNumber(st)));
                }
                else if (st.sval.equals("fov")) {
                  showDebugMessage("fov");
                  currentCamera.setFov(readNumber(st));
                }
                else if (st.sval.equals("background")) {
                  showDebugMessage("background");
                  currentBackground = new SimpleBackground();
                  ((SimpleBackground)currentBackground).setColor(readNumber(st), 
                                 readNumber(st), 
                                 readNumber(st));
                }
                else if (st.sval.equals("backgroundcubemap")) {

            RGBAImage front, right, back, left, down, up;

                    try {

            System.out.print("  - Loading background images: 1");
            front = ImagePersistence.importRGBA(
                        new File("../../../etc/cubemaps/dorise1/entorno0.jpg"));
            System.out.print("2");
            right = ImagePersistence.importRGBA(
                        new File("../../../etc/cubemaps/dorise1/entorno1.jpg"));
            System.out.print("3");
            back = ImagePersistence.importRGBA(
                        new File("../../../etc/cubemaps/dorise1/entorno2.jpg"));
            System.out.print("4");
            left = ImagePersistence.importRGBA(
                        new File("../../../etc/cubemaps/dorise1/entorno3.jpg"));
            System.out.print("5");
            down = ImagePersistence.importRGBA(
                        new File("../../../etc/cubemaps/dorise1/entorno4.jpg"));
            System.out.print("6");
            up = ImagePersistence.importRGBA(
                        new File("../../../etc/cubemaps/dorise1/entorno5.jpg"));
            System.out.println(" OK!");

            currentBackground = 
                new CubemapBackground(currentCamera, 
                                      front, right, back, left, down, up);

                    }
                    catch (Exception e) {
                        System.err.println("Error armando el cubemap!");
                        System.exit(0);
                    }
                }
                else if (st.sval.equals("light")) {
                  showDebugMessage("light");
                  double r = readNumber(st);
                  double g = readNumber(st);
                  double b = readNumber(st);
                  if ( st.nextToken() != StreamTokenizer.TT_WORD ) {
                      System.err.println("ERROR: in line "+st.lineno() + 
                                         " at "+st.sval);
                      throw new IOException(st.toString());
                  }
                  if ( st.sval.equals("ambient") ) {
                      showDebugMessage("ambient");
                      theScene.addLight(new Light(Light.AMBIENT, null, new ColorRgb(r,g,b)));
                    }
                    else if ( st.sval.equals("directional") ) {
                      showDebugMessage("directional");
                      Vector3D v = new Vector3D(readNumber(st), 
                                            readNumber(st), 
                                            readNumber(st));
                      theScene.addLight(new Light(Light.DIRECTIONAL, v, new ColorRgb(r,g,b)));
                    } 
                    else if ( st.sval.equals("point") ) {
                      showDebugMessage("point");
                      Vector3D v = new Vector3D(readNumber(st), 
                                            readNumber(st), 
                                            readNumber(st));
                      theScene.addLight(new Light(Light.POINT, v, new ColorRgb(r, g, b)));
                    } 
                    else {
                      System.err.println("ERROR: in line " + 
                                         st.lineno()+" at "+st.sval);
                      throw new IOException(st.toString());
                    }
                }
                else if ( st.sval.equals("rotation") ) {
                  showDebugMessage("rotation");
                  yaw_actual = readNumber(st);
                  pitch_actual = readNumber(st);
                  roll_actual = readNumber(st);
                }
                else if ( st.sval.equals("surface") ) {
                  showDebugMessage("surface");
                  double r = readNumber(st);
                  double g = readNumber(st);
                  double b = readNumber(st);
                  double ka = readNumber(st);
                  double kd = readNumber(st);
                  double ks = readNumber(st);
                  double ns = readNumber(st);
                  double kr = readNumber(st);
                  double kt = readNumber(st);
                  double index = readNumber(st);
                  /*
                  currentMaterial = new Material(r, g, b, 
                                                ka, kd, ks, 
                                                ns, kr, kt, index);
                  */
                  currentMaterial = new Material();
                  currentMaterial.setAmbient(new ColorRgb(r*ka, g*ka, b*ka));
                  currentMaterial.setDiffuse(new ColorRgb(r*kd, g*kd, b*kd));
                  currentMaterial.setSpecular(new ColorRgb(r*ks, g*ks, b*ks));
                  currentMaterial.setPhongExponent(ns);
                  currentMaterial.setReflectionCoefficient(kr);
                  currentMaterial.setRefractionCoefficient(kt);
                }
              ;
              break;
            default:
              fin_de_lectura = true;
              break;
          } // switch
        } // while
        is.close();
        if ( st.ttype != StreamTokenizer.TT_EOF ) {
            System.err.println("ERROR: in line "+st.lineno()+" at "+st.sval);
            throw new IOException(st.toString());
        }

        Vector3D l, f, u;

        f = new Vector3D(currentCamera.getFront());
        u = new Vector3D(currentCamera.getUp());
        l = new Vector3D(f.crossProduct(u));
        l = l.multiply(-1);
        l.normalize();

        currentCamera.setLeftDirect( l );

        theScene.addBackground(currentBackground);
        theScene.addCamera(currentCamera);
        theScene.setActiveCameraIndex(0);
        theScene.setActiveBackgroundIndex(0);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
