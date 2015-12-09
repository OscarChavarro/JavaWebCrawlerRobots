//===========================================================================
package vsdk.toolkit.common.dataStructures;

import vsdk.toolkit.common.Entity;

/**
NAry tree nodes organized from composite structural design pattern.
@param <T>
*/
public abstract class _NAryTreeNode<T> extends Entity {
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final long serialVersionUID = 20150218L;
    private T data;

    /**
    @param inInfo 
    */
    public _NAryTreeNode(final T inInfo)
    {
        data = inInfo;
    }
        
    /**
    @return the data
    */
    public T getData() {
        return data;
    }

    /**
    @param data the data to set
    */
    public void setData(T data) {
        this.data = data;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
