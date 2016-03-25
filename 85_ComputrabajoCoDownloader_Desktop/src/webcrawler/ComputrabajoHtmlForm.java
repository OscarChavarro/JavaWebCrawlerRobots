//===========================================================================
package webcrawler;

import java.util.ArrayList;
import java.net.URLEncoder;

class ComputrabajoFormInputA
{
    public String name;
    public String value;
    public ComputrabajoFormInputA() {
	name = null;
	value = null;
    }
}

public class ComputrabajoHtmlForm
{
    public String actionUrl;
    public String method;
    ArrayList<ComputrabajoFormInputA> inputs;

    public ComputrabajoHtmlForm()
    {
        inputs = new ArrayList<ComputrabajoFormInputA>();
    }

    public String exportAsUrlQueryString()
    {
        int i = 0;
        //String cad = new String("[" + method + "] - " + actionUrl + "?");
	String cad = "";
        ComputrabajoFormInputA f = null;

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

    public void configure(ArrayList<ComputrabajoTagParameter> tag)
    {
	int i;
        ComputrabajoTagParameter t;
        String name;

        for ( i = 0; i < tag.size(); i++ ) {
            t = tag.get(i);
            name = t.name.toUpperCase();
	    if ( name.equals("METHOD") ) {
                method = ComputrabajoTaggedHtml.trimQuotes(t.value.toUpperCase());
	    }
	    if ( name.equals("ACTION") ) {
                actionUrl = ComputrabajoTaggedHtml.trimQuotes(t.value);
	    }
        }
    }

    public void addInputFromTag(ArrayList<ComputrabajoTagParameter> tag)
    {
        int i;
        ComputrabajoTagParameter p;
        String name;
        boolean activeTag = false;
        ComputrabajoFormInputA input = new ComputrabajoFormInputA();

        for ( i = 0; i < tag.size(); i++ ) {
            p = tag.get(i);
            name = p.name.toUpperCase();
            if ( i == 0 && !name.equals("INPUT") ) {
                return;
            }
            if ( name.equals("NAME") ) {
                input.name = ComputrabajoTaggedHtml.trimQuotes(p.value);
                activeTag = true;
            }
            if ( name.equals("VALUE") ) {
                input.value = ComputrabajoTaggedHtml.trimQuotes(p.value);
            }
        }
        if ( activeTag ) {
            inputs.add(input);
        }
    }

    public void elimEntry(String key)
    {
        int i;
        ComputrabajoFormInputA f;
        
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
        ComputrabajoFormInputA f;
        
        for ( i = 0; i < inputs.size(); i++ ) {
            f = inputs.get(i);
	    if ( f.name.equals(var) ) {
		f.value = val;
		return;
	    }
	}
	f = new ComputrabajoFormInputA();
	f.name = var;
	f.value = val;
        inputs.add(f);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
