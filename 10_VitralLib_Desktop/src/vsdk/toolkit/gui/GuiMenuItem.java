//===========================================================================
package vsdk.toolkit.gui;

public class GuiMenuItem extends GuiMenuElement
{
    private String name;
    private String commandName;
    private boolean isSeparatorFlag;
    private char mnemonic;
    private String accelerator;

    public boolean isSeparator()
    {
        return isSeparatorFlag;
    }

    public GuiMenuItem(Gui c)
    {
        context = c;
        name = null;
        commandName = null;
        isSeparatorFlag = false;
        mnemonic = 0;
    }

    public void setName(String n)
    {
        name = processSimplifiedName(n);
        mnemonic = processMnemonic(n);
        accelerator = processAccelerator(n);
    }

    public String getName()
    {
        if ( name == null ) return "No Name";
        return name;
    }

    public String getCommandName()
    {
        if ( commandName == null ) return "IDC_NO_COMMAND";
        return commandName;
    }

    public void setCommandName(String a)
    {
        commandName = a;
    }

    public void addModifier(String m)
    {
        if ( m.equals("CHECKED") ) {
        }
        else if ( m.equals("GRAYED") ) {
        }
        else if ( m.equals("UNCHEKED") ) {
        }
        else if ( m.equals("SEPARATOR") ) {
            isSeparatorFlag = true;
        }
        else {
            setCommandName(m);
        }
    }

    @Override
    public String toString(int level)
    {
        String leadingSpace = "";
        int j;

        for ( j = 0; j < level; j++ ) {
            leadingSpace = leadingSpace + "  ";
        }

        String msg = leadingSpace + " - MenuItem ";

        if ( isSeparatorFlag ) {
            msg = msg + "--- SEPARATOR ---";
          }
          else {
            msg = msg + "\"" + name + "\"";
        }

        msg = msg + "\n";

        return msg;
    }

    @Override
    public String toString()
    {
        return toString(0);
    }

    public char getMnemonic()
    {
        return mnemonic;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
