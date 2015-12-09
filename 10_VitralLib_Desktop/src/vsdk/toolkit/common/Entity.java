//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 2 2006 - Oscar Chavarro: Original base version                    =
//===========================================================================

package vsdk.toolkit.common;

// Java basic classes
import java.lang.reflect.Method;
import java.io.Serializable;
import java.util.ArrayList;

/**
This class is a base superclass for all classes in the VSDK model (as of
Vitral applications are based upon Model-View-Controller or MVC design
pattern). Note that this class supports two functionalities:
  - As implementing the Serializable interface, this permits to serialize
    all of the
*/
public class Entity extends ModelElement implements Serializable
{
    public static final long serialVersionUID = 20150218L;

    /// Constants used for operations of type getSizeInBytes
    public static final int BYTE_SIZE_IN_BYTES = 1;
    public static final int INT_SIZE_IN_BYTES = 4;
    public static final int LONG_SIZE_IN_BYTES = 8;
    public static final int FLOAT_SIZE_IN_BYTES = 4;
    public static final int DOUBLE_SIZE_IN_BYTES = 8;
    public static final int VECTOR3D_SIZE_IN_BYTES = 24;
    public static final int COLORRGB_SIZE_IN_BYTES = 24;
    public static final int POINTER_SIZE_IN_BYTES = 8;

    /**
    This is a value used for the standard java serialization mechanism to
    keep track of software versions.  To avoid warning at compilation time
    and to ease to keep compatibility tracking of software structure changes
    in retrieving old saved data, it is suggested that all Entity's in VSDK
    define this value.  The proposed number to asign is the concatenation of
    8 digits YYYYMMDD, for year, month and day respectively.
    */
    //public static final long serialVersionUID = 20060502L;

    /**
    Each Entity object in the VSDK model should be responsible for calculating
    the size in bytes that occupies in RAM, including its own attributes and
    the aggregated objects (note that the associated objects only must count
    the size of the references).  If the class doen't overload this method,
    a 0 size is assumed.

    This is important for applications implementing memory chaching schema.
    @return the number of bytes current object ocupies in RAM when loaded
    */
    public int getSizeInBytes()
    {
        return 0;
    }

    /**
    Calculate a set of variable specs in "type:name" format, from method set and
    detecting pair of get / set methods.
    @return
    */
    public ArrayList<String> getEncapsulatedVariables()
    {
        int i;
        int j;
        Class<?> c;
        Method methods[];
        Method m;
        Class<?> t[];
        Class<?> r;

        c = this.getClass();
        methods = c.getMethods();

        // Stage 1: get a list of getters with no argument and a single return
        ArrayList<Method> getters = new ArrayList<Method>();
        for ( i = 0; i < methods.length; i++ ) {
            m = methods[i];
            if ( m.getName().startsWith("get") ) {
                t = m.getParameterTypes();
                r = m.getReturnType();

                if ( t.length == 0 && typeIsSupported(r) ) {
                    getters.add(m);
                }
            }
        }

        // Stage 2: get a list of setters with a single argument and with no
        // return
        ArrayList<Method> setters = new ArrayList<Method>();
        for ( i = 0; i < methods.length; i++ ) {
            m = methods[i];
            if ( m.getName().startsWith("set") ) {
                t = m.getParameterTypes();
                r = m.getReturnType();
                if ( t.length == 1 && r.getName().equals("void")) {
                    setters.add(m);
                }

            }
        }

        // Stage 3: report get/set pairs with the same name and parameter type
        ArrayList<String> variableSpecs;
        variableSpecs = new ArrayList<String>();
        for ( i = 0; i < getters.size(); i++ ) {
            String gname;
            m = getters.get(i);
            gname = m.getName().substring(3, m.getName().length());
            r = m.getReturnType();
            for ( j = 0; j < setters.size(); j++ ) {
                String sname = setters.get(j).getName().substring(
                    3, setters.get(j).getName().length());
                t = setters.get(j).getParameterTypes();
                if ( t.length == 1 && gname.equals(sname) &&
                     t[0].getName().equals(r.getName()) ) {
                    String s = gname.toLowerCase();
                    String variableName = s.substring(0, 1) +
                        gname.substring(1);
                    variableSpecs.add(r.getName() + ":" + variableName);
                    break;
                }
            }
        }

        return variableSpecs;
    }

    private boolean typeIsSupported(Class<?> r)
    {
        String n = r.getName();

        if ( n.equals("long") ) {
            return true;
        }
        else if ( n.equals("int") ) {
            return true;
        }
        else if ( n.equals("float") ) {
            return true;
        }
        else if ( n.equals("double") ) {
            return true;
        }
        else if ( n.equals("byte") ) {
            return true;
        }
        else if ( n.equals("boolean") ) {
            return true;
        }
        else if ( n.equals("char") ) {
            return true;
        }
        else if ( n.equals("short") ) {
            return true;
        }
        else if ( n.equals("java.lang.String") ) {
            return true;
        }
        else if ( n.equals("vsdk.toolkit.common.linealAlgebra.Vector2D") ) {
            return true;
        }
        else if ( n.equals("vsdk.toolkit.common.linealAlgebra.Vector3D") ) {
            return true;
        }
        else if ( n.equals("vsdk.toolkit.common.linealAlgebra.Vector4D") ) {
            return true;
        }
        else if ( n.equals("vsdk.toolkit.common.linealAlgebra.ColorRgb") ) {
            return true;
        }
        else if ( n.equals("vsdk.toolkit.common.linealAlgebra.Matrix4x4") ) {
            return true;
        }
        return false;
    }

    /**
    Just to do not the inheritance chain.
    @return
    @throws CloneNotSupportedException
    */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        super.clone();
        return null;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
