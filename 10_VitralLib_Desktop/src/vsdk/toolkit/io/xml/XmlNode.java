//===========================================================================
package vsdk.toolkit.io.xml;

import java.util.ArrayList;

import vsdk.toolkit.io.PersistenceElement;

public class XmlNode extends PersistenceElement
{
    private String name;
    private ArrayList<XmlNodeParam> params;
    private int sourceStart;
    private int sourceEnd;
    private boolean closed;
    private XmlNodeParam currentParam;

    // This is a non-tag segment
    public XmlNode(String name, String content)
    {
        XmlNodeParam p = new XmlNodeParam("content");
        p.value = content;

        this.name = name;
        params = new ArrayList<XmlNodeParam>();
        params.add(p);
        currentParam = null;

        sourceStart = 0;
        sourceEnd = 0;
        closed = false;
    }

    public XmlNode(String name)
    {
        this.name = name;
        params = new ArrayList<XmlNodeParam>();
        currentParam = null;

        sourceStart = 0;
        sourceEnd = 0;
        closed = false;
    }

    public boolean isClosed()
    {
        return closed;
    }

    public void setClosed(boolean c)
    {
        closed = c;
    }

    public boolean isEmpty()
    {
        if ( !name.equals("_NOTAG_") ) return false;
        if ( params.size() > 1  ) return false;
        if ( params.get(0).param.equals("contents") ) return false;

        char s[] = params.get(0).value.toCharArray();

        int i;
	for ( i = 0; i < s.length; i++ ) {
	    if ( !(s[i] == ' ' || s[i] == '\t')  ) return false;
	}

	return true;
    }

    public void addIdentifier(String val)
    {
        currentParam = new XmlNodeParam(val);
        params.add(currentParam);
    }

    public void addString(String val)
    {
        if ( currentParam != null ) {
            currentParam.value = val;
	}
    }

    /**
    Returns null if value not available.
    @param key
    @return 
    */
    public String getValueByParam(String key)
    {
        int i;
        for ( i = 0; i < params.size(); i++ ) {
	    if ( params.get(i).param.equals(key) ) {
                return params.get(i).value;
	    }
	}
        return null;
    }

    @Override
    public String toString()
    {
        String msg = getName();
        String val;
        int i;

	for ( i = 0; i < params.size(); i++ ) {
	    if ( i == 0 ) {
                msg += ": ";
	    }
	    msg = msg + params.get(i).param;
            val = params.get(i).value;
            if ( val != null && val.length() > 0 ) {
                msg += "=" + val;
	    }
            if ( i != params.size()-1 ) {
                msg += ", ";
	    }
	}

        return msg;
    }

    public String value(String param)
    {
        int i;

	for ( i = 0; i < params.size(); i++ ) {
	    if ( params.get(i).param.equals(param) ) {
                return params.get(i).value;
	    }
	}
	return null;
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
    @return the sourceStart
    */
    public int getSourceStart() {
        return sourceStart;
    }

    /**
    @param sourceStart the sourceStart to set
    */
    public void setSourceStart(int sourceStart) {
        this.sourceStart = sourceStart;
    }

    /**
    @return the sourceEnd
    */
    public int getSourceEnd() {
        return sourceEnd;
    }

    /**
    @param sourceEnd the sourceEnd to set
    */
    public void setSourceEnd(int sourceEnd) {
        this.sourceEnd = sourceEnd;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
