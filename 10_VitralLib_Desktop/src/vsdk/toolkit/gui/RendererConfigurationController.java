package vsdk.toolkit.gui;

import vsdk.toolkit.common.RendererConfiguration;

public class RendererConfigurationController extends Controller {

  private RendererConfiguration qualitySelection;

  public RendererConfigurationController() {

  }

  public RendererConfigurationController(RendererConfiguration qualitySelection) {
    this.qualitySelection = qualitySelection;
  }

  public void setRendererConfiguration(RendererConfiguration q)
  {
      qualitySelection = q;
  }

  public boolean processKeyPressedEvent(KeyEvent keyEvent) {
    boolean updated = false;
    char unicode_id;
    int st;

    switch ( keyEvent.keycode ) {
        case KeyEvent.KEY_F1:
          qualitySelection.changePoints();
          updated = true;
          break;
        case KeyEvent.KEY_F2:
          qualitySelection.changeWires();
          updated = true;
          break;
        case KeyEvent.KEY_F3:
          qualitySelection.changeSurfaces();
          updated = true;
          break;
        case KeyEvent.KEY_F4:
          qualitySelection.changeBoundingVolume();
          updated = true;
          break;
        case KeyEvent.KEY_F5:
          qualitySelection.changeNormals();
          updated = true;
          break;
        case KeyEvent.KEY_F6:
          qualitySelection.changeTrianglesNormals();
          updated = true;
          break;
        case KeyEvent.KEY_F7:
          st = qualitySelection.getShadingType();
          if ( st == RendererConfiguration.SHADING_TYPE_NOLIGHT ) {
              st = RendererConfiguration.SHADING_TYPE_FLAT;
            }
            else if ( st == RendererConfiguration.SHADING_TYPE_FLAT ) {
              st = RendererConfiguration.SHADING_TYPE_GOURAUD;
            }
            else if ( st == RendererConfiguration.SHADING_TYPE_GOURAUD ) {
              st = RendererConfiguration.SHADING_TYPE_PHONG;
            }
            else {
              st = RendererConfiguration.SHADING_TYPE_NOLIGHT;
            }
          ;
          qualitySelection.setShadingType(st);
          updated = true;
          break;
        case KeyEvent.KEY_F8:
          qualitySelection.changeTexture();
          updated = true;
          break;
        case KeyEvent.KEY_F9:
          qualitySelection.changeBumpMap();
          updated = true;
          break;
    }
    return updated;
  }

  public boolean processKeyReleasedEvent(KeyEvent keyEvent) {
      return false;
  }

}
