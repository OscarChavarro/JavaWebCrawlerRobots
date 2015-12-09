//===========================================================================
package vsdk.toolkit.gui;

/**
This class represents the concept of event language as explained in section
[FOLE1992.10.6] and figure [FOLE1992.10.24].
*/
public abstract class CommandListener extends PresentationElement {
    public abstract boolean executeCommand(String commandId);
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
