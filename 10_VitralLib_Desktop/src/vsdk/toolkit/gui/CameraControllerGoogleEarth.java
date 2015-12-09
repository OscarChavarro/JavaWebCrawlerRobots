//===========================================================================
package vsdk.toolkit.gui;

import vsdk.toolkit.common.Ray;
import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.environment.geometry.InfinitePlane;

/**
*/
public class CameraControllerGoogleEarth extends CameraController {
    private Camera camera;
    private double jumpStep;
    private int xOld;
    private int yOld;

    /**
    @param camera
    */
    public CameraControllerGoogleEarth(Camera camera) {
        this.camera = camera;
        jumpStep = 0.00000000000000001;
    }

    /**
    @param mouseEvent
    @return true if current event leads to a model update, false if not.
    */
    public boolean processMouseEvent(MouseEvent mouseEvent) {

        return false; //To change body of generated methods, choose Tools | Templates.
    }

    /**
    @param e
    @return true if current event leads to a model update, false if not.
    */
    @Override
    public boolean processMousePressedEvent(MouseEvent e) {
        setxOld(e.getX());
        setyOld(e.getY());

        //Procedimiento para selección de lineas
        camera.updateVectors();
        Ray RayA = camera.generateRay(getxOld(), getyOld());

        return false;
    }

    /**
    @param e
    @return true if current event leads to a model update, false if not.
    */
    @Override
    public boolean processMouseReleasedEvent(MouseEvent e) {

        return false;
    }

    /**
    @param e
    @return true if current event leads to a model update, false if not.
    */
    @Override
    public boolean processMouseClickedEvent(MouseEvent e) {

        return false;
    }

    /**
    @param e
    @return true if current event leads to a model update, false if not.
    */
    @Override
    public boolean processMouseMovedEvent(MouseEvent e) {

        return false;
    }

    /**
    @param e
    @return true if current event leads to a model update, false if not.
    */
    @Override
    public boolean processMouseDraggedEvent(MouseEvent e) {

        //---------------------------------------------------------------------
        // 1. Posición inicial y final
        int PrevX = getxOld();
        int PrevY = getyOld();

        int x = e.getX();
        int y = e.getY();

        //---------------------------------------------------------------------
        // 2. Calculo rayo 1 y 2
        Ray RayA = camera.generateRay(PrevX, PrevY);
        Ray RayB = camera.generateRay(x, y);

        //----------------------------------------------------------------------
        //3. Intercepción con plano infinito
        InfinitePlane infinitePlane = new InfinitePlane(new Vector3D(0, 0, 1), new Vector3D(0, 0, 0));
        infinitePlane.doIntersection(RayA);
        infinitePlane.doIntersection(RayB);

        //----------------------------------------------------------------------
        //4. Distancia entre RayA y RayB
        Vector3D pA = new Vector3D();
        Vector3D pB = new Vector3D();

        pA.x = RayA.origin.x + (RayA.direction.x * RayA.t);
        pA.y = RayA.origin.y + (RayA.direction.y * RayA.t);
        pA.z = RayA.origin.z + (RayA.direction.z * RayA.t);

        pB.x = RayB.origin.x + (RayB.direction.x * RayB.t);
        pB.y = RayB.origin.y + (RayB.direction.y * RayB.t);
        pB.z = RayB.origin.z + (RayB.direction.z * RayB.t);

        Vector3D d = pB.substract(pA);

        //----------------------------------------------------------------------
        //5. Mover la cámara
        camera.getPosition().x = camera.getPosition().x - d.x;
        camera.getPosition().y = camera.getPosition().y - d.y;

        setxOld(x);
        setyOld(y);

        return true;
    }

    /**
    @param e
    @return true if current event leads to a model update, false if not.
    */
    @Override
    public boolean processMouseWheelEvent(MouseEvent e) {
        // Local copy of the Camera's internal parameters
        Vector3D eyePosition;
        Vector3D focusedPosition;
        Matrix4x4 R; // Camera rotation matrix
        int projectionMode;
        double fov;
        double orthogonalZoom;
        double nearPlaneDistance;
        double farPlaneDistance;

        // 1. Obtain a copy of the camera's internal parameters
        eyePosition = camera.getPosition();
        focusedPosition = camera.getFocusedPosition();
        R = camera.getRotation();
        projectionMode = camera.getProjectionMode();
        fov = camera.getFov();
        orthogonalZoom = camera.getOrthogonalZoom();
        nearPlaneDistance = camera.getNearPlaneDistance();
        farPlaneDistance = camera.getFarPlaneDistance();

        int clicks = e.getClicks();//();
        boolean updated = false;
        double altura = eyePosition.z;
        //------------------------------------------------------------
        if (clicks < 0) {

            //Cambia el delta para que el zoom vaya acorde al tamaño de la imagen
            double expo = Math.round(Math.log10(eyePosition.z)) - 1;
            jumpStep = Math.pow(10, expo);//

            //Limite inferiror
            if ((eyePosition.z - jumpStep) <= 12) {
                return false;
            } //Fotos 0.0000000000000000000001

            nearPlaneDistance = altura * 0.1;
            farPlaneDistance = altura * 110;

            eyePosition.z -= jumpStep;
            focusedPosition.z -= jumpStep;

            updated = true;
        } else if (clicks > 0) {
            //Limite superior
            if ((eyePosition.z + jumpStep) >= Math.pow(10, 24)) {
                return false;
            }//Para las fotos Math.pow(10, 25)

            //Cambia el delta para que el zoom vaya acorde al tamaño de la imagen
            jumpStep = Math.pow(10, Math.round(Math.log10(eyePosition.z)) - 1);

            altura = eyePosition.z;

            nearPlaneDistance = altura * 0.1;
            farPlaneDistance = altura * 110;

            eyePosition.z += jumpStep;
            focusedPosition.z += jumpStep;
            updated = true;
        }

         // 4. Update camera's internal parameters from local copy
        //      R.eulerAnglesRotation(yaw, pitch, roll);
        camera.setPosition(eyePosition);
        camera.setFocusedPositionMaintainingOrthogonality(focusedPosition);
        camera.setRotation(R);
        camera.setOrthogonalZoom(orthogonalZoom);
        camera.setFov(fov);
        camera.setProjectionMode(projectionMode);
        camera.setNearPlaneDistance(nearPlaneDistance);
        camera.setFarPlaneDistance(farPlaneDistance);

        return updated;
    }

    /**
    @return current camera being under control
    */
    @Override
    public Camera getCamera() {
        return camera;
    }

    /**
    @param camera
    */
    @Override
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    /**
    @param keyEvent
    @return true if current event leads to a model update, false if not.
    */
    @Override
    public boolean processKeyPressedEvent(vsdk.toolkit.gui.KeyEvent keyEvent) {
        // Local copy of the Camera's internal parameters
        Vector3D eyePosition;
        Vector3D focusedPosition;
        Matrix4x4 R; // Camera rotation matrix
        int projectionMode;
        double fov;
        double orthogonalZoom;
        double nearPlaneDistance;
        double farPlaneDistance;

        // Internal variables to control the interaction
        char unicode_id;
        int keycode;
        double yaw;
        double pitch;
        double roll;
        double angleInc;
        boolean updated = false;
        double EPSILON = 0.0001;

        // 1. Obtain a copy of the camera's internal parameters
        eyePosition = camera.getPosition();
        focusedPosition = camera.getFocusedPosition();
        R = camera.getRotation();
        projectionMode = camera.getProjectionMode();
        fov = camera.getFov();
        orthogonalZoom = camera.getOrthogonalZoom();
        nearPlaneDistance = camera.getNearPlaneDistance();
        farPlaneDistance = camera.getFarPlaneDistance();

        // 2. Calculate variables used for interaction manipulation
        yaw = R.obtainEulerYawAngle();
        pitch = R.obtainEulerPitchAngle();
        roll = R.obtainEulerRollAngle();

        if (fov > 90) {
            angleInc = Math.toRadians(10);
        } else if (fov > 45) {
            angleInc = Math.toRadians(5);
        } else if (fov > 15) {
            angleInc = Math.toRadians(2.5);
        } else if (fov > 5) {
            angleInc = Math.toRadians(1);
        } else {
            angleInc = Math.toRadians(0.1);
        }

        // 3. Event processing: update the copy of the camera's internal parameters
        switch (keyEvent.keycode) {
            case vsdk.toolkit.gui.KeyEvent.KEY_UP:
                pitch -= angleInc;
                if (pitch < Math.toRadians(-90)) {
                    pitch = Math.toRadians(-90);
                }
                updated = true;
                break;
            case vsdk.toolkit.gui.KeyEvent.KEY_DOWN:
                pitch += angleInc;
                if (pitch > Math.toRadians(90)) {
                    pitch = Math.toRadians(90);
                }
                updated = true;
                break;
            case vsdk.toolkit.gui.KeyEvent.KEY_LEFT:
                yaw += angleInc;
                while (yaw >= Math.toRadians(360)) {
                    yaw -= Math.toRadians(360);
                }
                updated = true;
                break;
            case vsdk.toolkit.gui.KeyEvent.KEY_RIGHT:
                yaw -= angleInc;
                while (yaw < 0) {
                    yaw += Math.toRadians(360);
                }
                updated = true;
                break;

            // Position
            case vsdk.toolkit.gui.KeyEvent.KEY_x:
                eyePosition.x -= jumpStep;
                focusedPosition.x -= jumpStep;
                updated = true;
                break;
            case vsdk.toolkit.gui.KeyEvent.KEY_X:
                eyePosition.x += jumpStep;
                focusedPosition.x += jumpStep;
                updated = true;
                break;
            case vsdk.toolkit.gui.KeyEvent.KEY_y:
                eyePosition.y -= jumpStep;
                focusedPosition.y -= jumpStep;
                updated = true;
                break;
            case vsdk.toolkit.gui.KeyEvent.KEY_Y:
                eyePosition.y += jumpStep; //focusedPosition.y += deltaMov;
                updated = true;
                break;
            case vsdk.toolkit.gui.KeyEvent.KEY_z:
                //Cambia el delta para que el zoom vaya acorde al tamaño de la imagen
                double expo = Math.round(Math.log10(eyePosition.z)) - 1;
                jumpStep = Math.pow(10, expo);//

                double altura = eyePosition.z;

                //Limite inferiror
                if ((eyePosition.z - jumpStep) <= 12) {
                    break;
                } //Fotos 0.0000000000000000000001

                nearPlaneDistance = altura * 0.1;
                farPlaneDistance = altura * 110;

                eyePosition.z -= jumpStep;
                focusedPosition.z -= jumpStep;
                updated = true;
                break;
              //---------------------------------------------------------------

            case vsdk.toolkit.gui.KeyEvent.KEY_Z:

                //Limite superior
                if ((eyePosition.z + jumpStep) >= Math.pow(10, 4)) {
                    break;
                }//Para las fotos Math.pow(10, 25)

                //Cambia el delta para que el zoom vaya acorde al tamaño de la imagen
                jumpStep = Math.pow(10, Math.round(Math.log10(eyePosition.z)) - 1);

                altura = eyePosition.z;

                nearPlaneDistance = altura * 0.1;
                farPlaneDistance = altura * 110;

                eyePosition.z += jumpStep;
                focusedPosition.z += jumpStep;
                updated = true;
                break;
            // Rotation
            case vsdk.toolkit.gui.KeyEvent.KEY_S:
                roll -= Math.toRadians(5);
                while (roll < 0) {
                    roll += Math.toRadians(360);
                }
                updated = true;
                break;
            case vsdk.toolkit.gui.KeyEvent.KEY_s:
                roll += Math.toRadians(5);
                while (roll > Math.toRadians(360)) {
                    roll -= Math.toRadians(360);
                }
                updated = true;
                break;

            // View volume modification
            case vsdk.toolkit.gui.KeyEvent.KEY_A:
                if (camera.getProjectionMode() == Camera.PROJECTION_MODE_ORTHOGONAL) {
                    orthogonalZoom /= 2;
                } else {
                    if (fov < 0.1 - EPSILON) {
                        fov += 0.1;
                    } else if (fov < 1 - EPSILON) {
                        fov++;
                    } else if (fov < 175 - EPSILON) {
                        fov += 5;
                    }
                }
                updated = true;
                break;
            case vsdk.toolkit.gui.KeyEvent.KEY_a:
                if (camera.getProjectionMode() == Camera.PROJECTION_MODE_ORTHOGONAL) {
                    orthogonalZoom *= 2;
                } else {
                    if (fov > 5 + EPSILON) {
                        fov -= 5;
                    } else if (fov > 1 + EPSILON) {
                        fov--;
                    } else if (fov > 0.1 + EPSILON) {
                        fov -= 0.1;
                    }
                }
                updated = true;
                break;

            case vsdk.toolkit.gui.KeyEvent.KEY_N:
                nearPlaneDistance = nearPlaneDistance + 0.5;// augmentLogarithmic(nearPlaneDistance, EPSILON);
                updated = true;
                break;
            case vsdk.toolkit.gui.KeyEvent.KEY_n:
                nearPlaneDistance = nearPlaneDistance - 0.5;//diminishLogarithmic(nearPlaneDistance, EPSILON);
                updated = true;
                break;

            case vsdk.toolkit.gui.KeyEvent.KEY_F:
                farPlaneDistance = farPlaneDistance + 0.5;//augmentLogarithmic(farPlaneDistance, EPSILON);
                updated = true;
                break;
            case vsdk.toolkit.gui.KeyEvent.KEY_f:
                farPlaneDistance = farPlaneDistance - 0.5;//diminishLogarithmic(farPlaneDistance, EPSILON);
                updated = true;
                break;

            case vsdk.toolkit.gui.KeyEvent.KEY_p: // Rote el modo de proyeccion
                switch (projectionMode) {
                    case Camera.PROJECTION_MODE_PERSPECTIVE:
                        projectionMode = Camera.PROJECTION_MODE_ORTHOGONAL;
                        break;
                    default:
                        projectionMode = Camera.PROJECTION_MODE_PERSPECTIVE;
                        break;
                }
                updated = true;
                break;

            // Queries
            case vsdk.toolkit.gui.KeyEvent.KEY_i:
                System.out.println(camera);
                break;
        }

        // 4. Update camera's internal parameters from local copy
        R.eulerAnglesRotation(yaw, pitch, roll);

        camera.setPosition(eyePosition);
        camera.setFocusedPositionMaintainingOrthogonality(focusedPosition);
        camera.setRotation(R);
        camera.setOrthogonalZoom(orthogonalZoom);
        camera.setFov(fov);
        camera.setProjectionMode(projectionMode);
        camera.setNearPlaneDistance(nearPlaneDistance);
        camera.setFarPlaneDistance(farPlaneDistance);

        return updated;
    }

    /**
    @param keyEvent
    @return true if current event leads to a model update, false if not.
    */
    @Override
    public boolean processKeyReleasedEvent(KeyEvent keyEvent) {
        return false; //To change body of generated methods, choose Tools | Templates.
    }

    /**
    @param factor
    */
    @Override
    public void setDeltaMovement(double factor) {

    }

    /**
    @return the xOld
    */
    public int getxOld() {
        return xOld;
    }

    /**
    @param xOld the xOld to set
    */
    public void setxOld(int xOld) {
        this.xOld = xOld;
    }

    /**
    @return the yOld
    */
    public int getyOld() {
        return yOld;
    }

    /**
    @param yOld the yOld to set
    */
    public void setyOld(int yOld) {
        this.yOld = yOld;
    }

    /**
    @param jumpValue
    */
    public void zoomOut(double jumpValue) {
        Vector3D eyePosition;
        Vector3D focusedPosition;

        double nearPlaneDistance;
        double farPlaneDistance;

        eyePosition = camera.getPosition();
        focusedPosition = camera.getFocusedPosition();

        double altura = eyePosition.z;

        nearPlaneDistance = altura * 0.1;
        farPlaneDistance = altura * 110;

        eyePosition.z += jumpValue;
        focusedPosition.z += jumpValue;

        camera.setPosition(eyePosition);
        camera.setFocusedPositionMaintainingOrthogonality(focusedPosition);
        camera.setNearPlaneDistance(nearPlaneDistance);
        camera.setFarPlaneDistance(farPlaneDistance);

    }

    /**
    @param jumpValue
    */
    public void zoomIn(double jumpValue) {

        Vector3D eyePosition;
        Vector3D focusedPosition;

        double nearPlaneDistance;
        double farPlaneDistance;

        eyePosition = camera.getPosition();
        focusedPosition = camera.getFocusedPosition();

        double altura = eyePosition.z;
        if ((eyePosition.z - jumpValue) >= 12) {
            nearPlaneDistance = altura * 0.1;
            farPlaneDistance = altura * 110;

            eyePosition.z -= jumpValue;
            focusedPosition.z -= jumpValue;

            camera.setPosition(eyePosition);
            camera.setFocusedPositionMaintainingOrthogonality(focusedPosition);
            camera.setNearPlaneDistance(nearPlaneDistance);
            camera.setFarPlaneDistance(farPlaneDistance);
        }
    }
    
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
