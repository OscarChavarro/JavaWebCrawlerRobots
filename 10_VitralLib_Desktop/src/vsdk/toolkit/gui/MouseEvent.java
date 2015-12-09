//===========================================================================

package vsdk.toolkit.gui;

public class MouseEvent extends PresentationElement
{
    //public static final int MOUSE_FIRST = 500;
    //public static final int MOUSE_LAST = 507;
    //public static final int MOUSE_CLICKED = 500;
    //public static final int MOUSE_PRESSED = 501;
    //public static final int MOUSE_RELEASED = 502;
    //public static final int MOUSE_MOVED = 503;
    //public static final int MOUSE_ENTERED = 504;
    //public static final int MOUSE_EXITED = 505;
    //public static final int MOUSE_DRAGGED = 506;
    //public static final int MOUSE_WHEEL = 507;
    //public static final int NOBUTTON = 0;

    public static final int BUTTON1 = 1;
    public static final int BUTTON2 = 2;
    public static final int BUTTON3 = 3;
    public static final int BUTTON1_DOWN_MASK = 1024;
    public static final int BUTTON2_DOWN_MASK = 2048;
    public static final int BUTTON3_DOWN_MASK = 4096;

    private int x;
    private int y;
    private int button;
    private int modifiers;
    private int clicks; // used for wheel mouse movements

    public int getClicks()
    {
        return clicks;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getButton()
    {
        return button;
    }

    public int getModifiers()
    {
        return modifiers;
    }

    public void setClicks(int clicks)
    {
        this.clicks = clicks;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public void setButton(int button)
    {
        this.button = button;
    }

    public void setModifiers(int modifiers)
    {
        this.modifiers = modifiers;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
