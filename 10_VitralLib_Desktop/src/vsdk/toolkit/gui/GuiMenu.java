//===========================================================================
package vsdk.toolkit.gui;

import java.util.ArrayList;

public class GuiMenu extends GuiMenuElement{
    private ArrayList <GuiMenuElement> children;
    private String name;
    private char mnemonic;
    private String accelerator;

    public GuiMenu(Gui c)
    {
        context = c;
        children = new ArrayList<GuiMenuElement>();
        name = null;
    }

    public ArrayList <GuiMenuElement> getChildren()
    {
        return children;
    }

    public void setName(String n)
    {
        name = processSimplifiedName(n);
        mnemonic = processMnemonic(n);
        accelerator = processAccelerator(n);
    }

    public void addChild(GuiMenuElement i)
    {
        children.add(i);
    }

    @Override
    public String toString(int level)
    {
        String leadingSpace = "";
        int j;

        for ( j = 0; j < level; j++ ) {
            leadingSpace = leadingSpace + "  ";
        }

        String msg = leadingSpace + "Menu \"" + name + "\"\n";

        int i;

        for ( i = 0; i < children.size(); i++ ) {
            msg = msg + (children.get(i)).toString(level+1);
        }

        return msg;
    }

    @Override
    public String toString()
    {
        return toString(0);
    }

    public String getName()
    {
        return name;
    }

    public char getMnemonic()
    {
        return mnemonic;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
