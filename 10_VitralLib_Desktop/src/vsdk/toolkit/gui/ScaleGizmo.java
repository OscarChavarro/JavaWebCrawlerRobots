//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - March 16 2006 - Oscar Chavarro: Original base version                 =
//===========================================================================

package vsdk.toolkit.gui;

import vsdk.toolkit.common.linealAlgebra.Matrix4x4;
import vsdk.toolkit.common.linealAlgebra.Vector3D;

public class ScaleGizmo extends Gizmo {

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
      double deltaMov = 1.1;
      boolean updateNeeded = false;

      unicode_id = keyEvent.unicode_id;
      keycode = keyEvent.keycode;

      Vector3D s = new Vector3D(T.M[0][0], T.M[1][1], T.M[2][2]);

      if ( unicode_id != KeyEvent.KEY_NONE ) {
            switch ( unicode_id ) {
              // Position
              case 'x':
                s.x /= deltaMov;
                updateNeeded = true;
                break;
              case 'X':
                s.x *= deltaMov;
                updateNeeded = true;
                break;
              case 'y':
                s.y /= deltaMov;
                updateNeeded = true;
                break;
              case 'Y':
                s.y *= deltaMov;
                updateNeeded = true;
                break;
              case 'z':
                s.z /= deltaMov;
                updateNeeded = true;
                break;
              case 'Z':
                s.z *= deltaMov;
                updateNeeded = true;
                break; 
            }
        }
        else {
            switch ( keycode ) {
              case KeyEvent.KEY_UP:
              case KeyEvent.KEY_RIGHT:
                s.x *= deltaMov;
                s.y *= deltaMov;
                s.z *= deltaMov;
                updateNeeded = true;
                break;

              case KeyEvent.KEY_LEFT:
              case KeyEvent.KEY_DOWN:
                s.x /= deltaMov;
                s.y /= deltaMov;
                s.z /= deltaMov;
                updateNeeded = true;
                break;
            }
      }

      T.M[0][0] = s.x;
      T.M[1][1] = s.y;
      T.M[2][2] = s.z;

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

  public boolean processMouseClickedEvent(MouseEvent e)
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
