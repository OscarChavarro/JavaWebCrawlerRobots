//===========================================================================
package vsdk.toolkit.gui;

public class ExceptionGuiParseError extends Exception {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20140314L;

    @Override
    public String toString(){
        return "Parse error reading GUI data";
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
