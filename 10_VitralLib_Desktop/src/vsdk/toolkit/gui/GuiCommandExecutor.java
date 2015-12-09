//===========================================================================
package vsdk.toolkit.gui;

import java.util.HashMap;

/**
*/
public abstract class GuiCommandExecutor extends PresentationElement {
    protected HashMap<Integer, String> commandCache;
    
    public GuiCommandExecutor()
    {
        commandCache = new HashMap<Integer, String>();
    }

    public void addIdToCommandCache(int id, String command)
    {
        Integer number = Integer.valueOf(id);
        commandCache.put(number, command);
    }
    
    public String getCommandFromId(int id)
    {
        Integer number = Integer.valueOf(id);
        return commandCache.get(number);
    }
    
    public abstract boolean executeMenuCommand(String inIdCommand);
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
