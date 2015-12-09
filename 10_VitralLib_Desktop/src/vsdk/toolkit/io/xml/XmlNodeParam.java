//===========================================================================
package vsdk.toolkit.io.xml;

import vsdk.toolkit.io.PersistenceElement;

public class XmlNodeParam extends PersistenceElement
{
    public String param;
    public String value;

    public XmlNodeParam(String param)
    {
        this.param = new String(param);
        value = null;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
