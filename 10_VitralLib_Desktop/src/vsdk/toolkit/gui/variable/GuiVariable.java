//===========================================================================
package vsdk.toolkit.gui.variable;

import vsdk.toolkit.gui.GuiElement;

/**
A GuiVariable is a value stored at computer memory which has a type, and is
assigned to a name and that is inside a valid values range. For example, the
radius of an sphere has a valid value range expressed as an interval: "[0,
INF]". A current value for that variable could be the number "5.0", and its
name could be "r". This variable is of type "GuiDoubleVariable".

This class is the superclass of several other classes, each one representing
an specific variable type.

This class and its subclasses plays a client role in a reflection design
pattern.

This class plays a role of leaf on an n-ary tree in the composite design
pattern.
*/
public abstract class GuiVariable extends GuiElement {
    /// Variable names follows a convention of scope operator. Example:
    /// "position" is a global name, "camera.position" is the same variable
    /// under the "camera" scope. "scene.camera.position" could be a full
    /// hierarchy name for a variable inside the system.

    protected String name;
    protected String validRange;
    protected String initialvalue;

    public GuiVariable() {
        name = "";
        validRange = "";
        initialvalue = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getInitialvalue() {
        return initialvalue;
    }

    public void setInitialvalue(String initialvalue) {
        this.initialvalue = initialvalue;
    }

    @Override
    public String toString() {
        String msg = "";
        msg = msg + "VARIABLE:\n"
                + "     TYPE: " + this.getType() + "\n"
                + "     NAME: " + this.getName() + "\n"
                + "     INITIAL_VALUE: " + this.getInitialvalue() + "\n"
                + "     VALID_RANGE: " + this.getValidRange() + "\n";
        return msg;
    }

    /**
    Each variable has a type. Examples: "Integer", "Double", "String",
    "Vector3D".
    @return 
    */
    public abstract String getType();

    /**
    Gets the current String specifying valid value range. The returned String
    contains an specification expressed in Vitral GUI value ranges language.
     * @return 
    */
    public abstract String getValidRange();

    /**
    Sets the current String specifying valid value range. The returned String
    contains an specification expressed in Vitral GUI value ranges language.
     * @param vr
    */
    public abstract void setValidRange(String vr);
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
