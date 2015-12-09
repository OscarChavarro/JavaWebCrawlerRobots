//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - March 16 2006 - Oscar Chavarro: Original base version                 =
//===========================================================================

package vsdk.toolkit.gui;

import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.linealAlgebra.Vector3D;

public class RotateGizmo extends Gizmo {

  private Matrix4x4 T;

  public void setTransformationMatrix(Matrix4x4 T)
  {
      this.T = T;
  }

  public Matrix4x4 getTransformationMatrix()
  {
      return T;
  }

  public boolean processMouseEvent(MouseEvent mouseEvent)
  {
      return false;
  }

  public boolean processKeyPressedEvent(KeyEvent keyEvent)
  {
      char unicode_id;
      int keycode;
      double deltaMov = Math.toRadians(1.0);
      boolean updateNeeded = false;

      unicode_id = keyEvent.unicode_id;
      keycode = keyEvent.keycode;

      Matrix4x4 delta;
      Vector3D up, front, left;

      delta = new Matrix4x4();
      //up = new Vector3D(T.M[0][2], T.M[1][2], T.M[2][2]);
      up = new Vector3D(0, 0, 1);
      up.normalize();

      //front = new Vector3D(T.M[0][0], T.M[1][0], T.M[2][0]);
      front = new Vector3D(0, 1, 0);
      front.normalize();

      //left = new Vector3D(T.M[0][1], T.M[1][1], T.M[2][1]);
      left = new Vector3D(1, 0, 0);
      left.normalize();

      if ( unicode_id != KeyEvent.KEY_NONE ) {
            switch ( unicode_id ) {
              // Position
              case 'x':
                delta.axisRotation(-deltaMov, left.x, left.y, left.z);
                updateNeeded = true;
                break;
              case 'X':
                delta.axisRotation(deltaMov, left.x, left.y, left.z);
                updateNeeded = true;
                break;
              case 'y':
                delta.axisRotation(-deltaMov, front.x, front.y, front.z);
                updateNeeded = true;
                break;
              case 'Y':
                delta.axisRotation(deltaMov, front.x, front.y, front.z);
                updateNeeded = true;
                break;
              case 'z':
                delta.axisRotation(-deltaMov, up.x, up.y, up.z);
                updateNeeded = true;
                break;
              case 'Z':
                delta.axisRotation(deltaMov, up.x, up.y, up.z);
                updateNeeded = true;
                break; 
            }
      }

      T = T.multiply(delta);

      return updateNeeded;
  }

  public boolean processKeyReleasedEvent(KeyEvent mouseEvent)
  {
      return false;
  }

  public boolean processMousePressedEvent(MouseEvent e)
  {
      return false;
  }

  public boolean processMouseReleasedEvent(MouseEvent e)
  {
      return false;
  }

  public boolean processMouseClickedEventAwt(MouseEvent e)
  {
      return false;
  }

  public boolean processMouseMovedEvent(MouseEvent e)
  {
      return false;
  }

  public boolean processMouseDraggedEvent(MouseEvent e)
  {
      return false;
  }

  public boolean processMouseWheelEvent(MouseEvent e)
  {
      return false;
  }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
