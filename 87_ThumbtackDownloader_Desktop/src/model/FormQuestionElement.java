package model;

//===========================================================================
/**
*/
public class FormQuestionElement {
    private String value;
    private String type;
    private String content;
    private String name;
    private String className;
    private String id;
    private String placeholder;

    public FormQuestionElement()
    {
        value = "";
        type = "";
        content = "";
        name = "";
        className = "";
        id = "";
        placeholder = "";
    }
    
    public String exportToReport()
    {
        return "E(" + this.toString() + ") , ";
    }
    
    public boolean isEmpty()
    {
        return (value == null || value.length() == 0) &&
                (content == null || content.length() == 0) &&
                (className == null || className.length() == 0) &&
                (id == null || id.length() == 0) &&
                (placeholder == null || placeholder.length() == 0) &&
                (name == null || name.length() == 0);
    }
    
    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public String toString()
    {
        String msg;
        
        msg = "Type: " + type;
        if ( value != null && value.length() > 0 ) {
            msg += "| value: " + value;
        }
        if ( name != null && name.length() > 0 ) {
            msg += "| name: " + name;
        }
        if ( content != null && content.length() > 0 ) {
            msg += "| content: " + content;        
        }
        if ( className != null && className.length() > 0 ) {
            msg += "| class: " + className;        
        }
        if ( id != null && id.length() > 0 ) {
            msg += "| id: " + id;        
        }
        if ( placeholder != null && placeholder.length() > 0 ) {
            msg += "| placeholder: " + placeholder;
        }
        return msg;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the placeholder
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * @param placeholder the placeholder to set
     */
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public void removeEnters() {
        if ( value != null && value.length() > 0 ) {
            value = value.replace("\n", "");
        }
        if ( name != null && name.length() > 0 ) {
            name = name.replace("\n", "");
        }
        if ( content != null && content.length() > 0 ) {
            content = content.replace("\n", "");      
        }
        if ( className != null && className.length() > 0 ) {
            className = className.replace("\n", "");    
        }
        if ( id != null && id.length() > 0 ) {
            id = id.replace("\n", "");      
        }
        if ( placeholder != null && placeholder.length() > 0 ) {
            placeholder = placeholder.replace("\n", "");
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
