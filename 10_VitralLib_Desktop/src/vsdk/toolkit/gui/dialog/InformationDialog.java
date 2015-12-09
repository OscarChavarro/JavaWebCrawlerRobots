//===========================================================================
package vsdk.toolkit.gui.dialog;

// Java basic classes
import java.util.ArrayList;

// VSDK classes
import vsdk.toolkit.gui.HudIcon;

/**
*/
public class InformationDialog {
    private String title;
    private String contents;

    /// Changes in the range [0, 1.0]. 1.0 means to fully display dialog,
    /// other values are used in popup and popdown animation process.
    private double animationParameter;
    private double animationDelta;
    private int dialogState;

    private final ArrayList<HudIcon> dialogIcons;

    public static final int OPENING = 1;
    public static final int NORMAL = 2;
    public static final int CLOSING = 3;
    public static final int CLOSED = 4;

    public InformationDialog()
    {
        title = "Empty dialog";
        contents = "Empty contents";
        animationDelta = 0.1;
        animationParameter = 0.0;
        dialogState = OPENING;
        dialogIcons = new ArrayList<HudIcon>();
    }
    
    /**
    @return the title
    */
    public String getTitle() {
        return title;
    }

    /**
    @param title the title to set
    */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
    @return the contents
    */
    public String getContents() {
        return contents;
    }

    /**
    @param contents the contents to set
    */
    public void setContents(String contents) {
        this.contents = contents;
    }

    /**
    @return the animationParameter
    */
    public double getAnimationParameter() {
        return animationParameter;
    }

    /**
    @param animationParameter the animationParameter to set
    */
    public void setAnimationParameter(double animationParameter) {
        this.animationParameter = animationParameter;
        if ( this.animationParameter < 0.0 ) {
            this.animationParameter = 0.0;
            dialogState = CLOSED;
        }
        if ( this.animationParameter > 1.0 ) {
            this.animationParameter = 1.0;
            dialogState = NORMAL;
        }
    }

    /**
    @return the animationDelta
    */
    public double getAnimationDelta() {
        return animationDelta;
    }

    /**
    @param animationDelta the animationDelta to set
    */
    public void setAnimationDelta(double animationDelta) {
        this.animationDelta = animationDelta;
    }

    /**
    @return the dialogState
    */
    public int getDialogState() {
        return dialogState;
    }

    /**
    @param dialogState the dialogState to set
    */
    public void setDialogState(int dialogState) {
        this.dialogState = dialogState;
    }

    /**
    @return the dialogIcons
    */
    public ArrayList<HudIcon> getDialogIcons() {
        return dialogIcons;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
