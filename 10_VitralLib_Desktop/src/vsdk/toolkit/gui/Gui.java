//===========================================================================
package vsdk.toolkit.gui;

// Java classes
import java.util.ArrayList;
import java.util.HashMap;

// VSDK classes
import vsdk.toolkit.gui.variable.GuiVariable;

/**
In order to understand this class, the following concepts must be taken into
account: - GuiVariable - GuiCommand - Reflection (introspection) design
pattern - Menubars, buttons bars, and other are based upon GuiCommands -
Dialogs are based upon GuiVariables and GuiCommands - Dialogs and menus are
hierarchical
*/
public class Gui extends PresentationElement {
    // Basic / fundamental / atomic elements

    private ArrayList<GuiCommand> commandList;
    private ArrayList<GuiVariable> variableList;
    private HashMap<String, String> messagesTable;
    // Composite elements
    private GuiMenu menubar;
    private ArrayList<GuiMenu> popupMenuList;
    private ArrayList<GuiButtonGroup> buttonGroupList;
    private ArrayList<GuiDialog> dialogList;

    public ArrayList<GuiDialog> getDialogList() {
        return dialogList;
    }

    public void setDialogList(ArrayList<GuiDialog> dialogList) {
        this.dialogList = dialogList;
    }

    public Gui() {
        menubar = null;
        popupMenuList = new ArrayList<GuiMenu>();
        commandList = new ArrayList<GuiCommand>();
        buttonGroupList = new ArrayList<GuiButtonGroup>();
        messagesTable = new HashMap<String, String>();
        /*
         * TODO variableList should be hashMap
         */
        variableList = new ArrayList<GuiVariable>();
        dialogList = new ArrayList<GuiDialog>();
    }

    public void addMessage(String id, String message) {
        messagesTable.put(id, message);
    }

    public String getMessage(String id) {
        String msg;

        msg = messagesTable.get(id);

        if (msg == null) {
            return id;
        }
        return msg;
    }

    public void setMenubar(GuiMenu m) {
        menubar = m;
    }

    public GuiMenu getMenubar() {
        return menubar;
    }

    public GuiCommand getCommandByName(String name) {
        GuiCommand command = null;
        GuiCommand candidate;
        int i;

        for (i = 0; i < commandList.size(); i++) {
            candidate = commandList.get(i);
            if (candidate.getId().equals(name)) {
                command = candidate;
                break;
            }
        }
        return command;
    }

    public GuiButtonGroup getButtonGroup(String name) 
    {
        if (name == null) {
            return null;
        }

        GuiButtonGroup group = null, candidate;
        int i;

        for (i = 0; i < buttonGroupList.size(); i++) {
            candidate = buttonGroupList.get(i);
            if ( candidate.getName().equals(name) ) {
                group = candidate;
                break;
            }
        }
        return group;
    }

    public GuiMenu getPopup(String name) {
        GuiMenu menu = null;
        GuiMenu candidate;

        int i;
        for (i = 0; i < popupMenuList.size(); i++) {
            candidate = popupMenuList.get(i);
            if (candidate.getName().equals(name)) {
                menu = candidate;
                break;
            }
        }
        return menu;
    }

    public void addPopupMenu(GuiMenu p) {
        popupMenuList.add(p);
    }

    public void addCommand(GuiCommand c) {
        commandList.add(c);
    }

    public void addButtonGroup(GuiButtonGroup b) {
        buttonGroupList.add(b);
    }

    public void addDialog(GuiDialog dialog) {
        dialogList.add(dialog);
    }

    public void addVariable(GuiVariable variable) {
        variableList.add(variable);
    }
    
    public GuiVariable getVariableByName(String name){
        int i;
        for(i = 0; i < variableList.size(); i++){
            if(variableList.get(i).getName().equals(name)){
                return variableList.get(i);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String msg = "= Gui report =========================================================\n";
        msg = msg + "Gui cache structure contains " + popupMenuList.size()
                + " popup submenu structures registered\n";
        msg = msg + "Gui cache structure contains " + commandList.size()
                + " commands registered\n";

        int i;
        GuiCommand command;
        for (i = 0; i < commandList.size(); i++) {
            command = commandList.get(i);
            msg = msg + command;
        }

        if (menubar == null) {
            msg = msg + "There is NO menubar!";
        } else {
            msg = msg + "There is a menubar active, called \"" + menubar.getName() + "\"\n";
            msg = msg + "Dumping menubar tree structure...\n";
            msg = msg + menubar;
        }

        /**
         * TODO print lists of GUIDialosg and variables pending
         */
        
        //-----------------------------------------------------------------------------------
        GuiDialog dialog;
        for (i = 0; i < dialogList.size(); i++) {
            dialog = dialogList.get(i);
            msg = msg + dialog.toString();
        }

        GuiVariable variable;
        for (i = 0; i < variableList.size(); i++) {
            variable = variableList.get(i);
            msg = msg + variable.toString();
        }
        //-----------------------------------------------------------------------------------
        msg = msg + "===========================================================================\n";

        return msg;
    }
}
//===========================================================================
//= EOF                                                                     =
//===========================================================================
