//===========================================================================
package vsdk.toolkit.gui;

import java.util.ArrayList;

public class GuiButtonGroup extends GuiElement
{
    private final ArrayList<GuiCommand> commandReferenceList;
    private String name;

    private boolean showText;
    private boolean showIcons;
    private boolean showTitle;
    private int direction;

    public static final int HORIZONTAL = 1;
    public static final int VERTICAL = 2;

    public GuiButtonGroup(Gui parent)
    {
        commandReferenceList = new ArrayList<GuiCommand>();
        context = parent;
    }

    public void setShowText(boolean f)
    {
        showText = f;
    }

    public void setShowIcons(boolean f)
    {
        showIcons = f;
    }

    public void setTitle(boolean f)
    {
        showTitle = f;
    }

    public void setDirection(int d)
    {
        direction = d;
    }

    public int getDirection()
    {
        return direction;
    }

    public boolean isShowTextSet()
    {
        return showText;
    }

    public boolean isShowIconsSet()
    {
        return showIcons;
    }

    public boolean isShowTitleSet()
    {
        return showTitle;
    }

    public ArrayList<GuiCommand> getCommands()
    {
        return commandReferenceList;
    }

    public void setName(String n)
    {
        name = n;
    }

    public String getName()
    {
        return name;
    }

    public void addCommandByName(String commandName)
    {
        GuiCommand command = context.getCommandByName(commandName);

        if ( command != null ) {
            commandReferenceList.add(command);
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
