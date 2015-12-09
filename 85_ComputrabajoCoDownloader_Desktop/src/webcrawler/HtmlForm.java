//===========================================================================
package webcrawler;

import java.util.ArrayList;
import java.net.URLEncoder;

class FormInput
{
    public String name;
    public String value;
    public FormInput() {
	name = null;
	value = null;
    }
}

public class HtmlForm
{
    public String actionUrl;
    public String method;
    ArrayList<FormInput> inputs;

    public HtmlForm()
    {
        inputs = new ArrayList<FormInput>();
    }

    public String exportAsUrlQueryString()
    {
        int i = 0;
        //String cad = new String("[" + method + "] - " + actionUrl + "?");
	String cad = "";
        FormInput f = null;

	try {
        for ( i = 0; i < inputs.size(); i++ ) {
            f = inputs.get(i);
	    if ( f.value == null ) f.value = "";
            cad += URLEncoder.encode(f.name, "UTF-8") + "=" +
		URLEncoder.encode(f.value, "UTF-8");
            if ( i < inputs.size() - 1 ) {
                cad += "&";
            } 
        }
	} catch ( Exception e ) {
	    System.err.println("Error encoding URL");
	    System.err.println("  - Form with " + inputs.size() + " inputs");
	    System.err.println("  - Partial string: " + cad);
	    System.err.println("  - i: " + i);
	    System.err.println("  - inputs[i]: " + f.name + " / " + f.value);
	}
        return cad;
    }

    public void configure(ArrayList<TagParameter> tag)
    {
	int i;
        TagParameter t;
        String name;

        for ( i = 0; i < tag.size(); i++ ) {
            t = tag.get(i);
            name = t.name.toUpperCase();
	    if ( name.equals("METHOD") ) {
                method = TaggedHtml.trimQuotes(t.value.toUpperCase());
	    }
	    if ( name.equals("ACTION") ) {
                actionUrl = TaggedHtml.trimQuotes(t.value);
	    }
        }
    }

    public void addInputFromTag(ArrayList<TagParameter> tag)
    {
        int i;
        TagParameter p;
        String name;
        boolean activeTag = false;
        FormInput input = new FormInput();

        for ( i = 0; i < tag.size(); i++ ) {
            p = tag.get(i);
            name = p.name.toUpperCase();
            if ( i == 0 && !name.equals("INPUT") ) {
                return;
            }
            if ( name.equals("NAME") ) {
                input.name = TaggedHtml.trimQuotes(p.value);
                activeTag = true;
            }
            if ( name.equals("VALUE") ) {
                input.value = TaggedHtml.trimQuotes(p.value);
            }
        }
        if ( activeTag ) {
            inputs.add(input);
        }
    }

    public void elimEntry(String key)
    {
        int i;
        FormInput f;
        
        for ( i = 0; i < inputs.size(); i++ ) {
            f = inputs.get(i);
	    if ( f.name.equals(key) ) {
		inputs.remove(i);
		return;
	    }
	}
    }

    public boolean searchValue(String key)
    {
        int i;
        
        for ( i = 0; i < inputs.size(); i++ ) {
	    if ( inputs.get(i).name.equals(key) ) {
		return true;
	    }
	}
	return false;
    }

    public void setValue(String var, String val)
    {
        int i;
        FormInput f;
        
        for ( i = 0; i < inputs.size(); i++ ) {
            f = inputs.get(i);
	    if ( f.name.equals(var) ) {
		f.value = val;
		return;
	    }
	}
	f = new FormInput();
	f.name = var;
	f.value = val;
        inputs.add(f);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
