//===========================================================================
package vsdk.toolkit.render.swing;

// GUI JDK classes (Awt + Swing)
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// VSDK classes
import vsdk.toolkit.gui.PresentationElement;

public class SwingEventListener extends PresentationElement implements ActionListener
{
    private String commandName;
    private ActionListener executor;

    SwingEventListener (String c, ActionListener e)
    {
        commandName = c;
        executor = e;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        ActionEvent e2;

        e2 = new ActionEvent(this, 1, commandName);
        executor.actionPerformed(e2);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
