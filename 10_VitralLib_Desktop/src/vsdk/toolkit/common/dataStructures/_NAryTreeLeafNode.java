//===========================================================================
package vsdk.toolkit.common.dataStructures;

/**
NAry tree nodes organized from composite structural design pattern.
@param <T>
*/
public class _NAryTreeLeafNode<T> extends _NAryTreeNode<T> {
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final long serialVersionUID = 20150218L;
    
    public _NAryTreeLeafNode(final T inInfo)
    {
        super(inInfo);
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
