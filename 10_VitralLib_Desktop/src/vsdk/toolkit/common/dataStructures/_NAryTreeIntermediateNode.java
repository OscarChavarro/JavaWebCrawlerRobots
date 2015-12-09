//===========================================================================
package vsdk.toolkit.common.dataStructures;

import java.util.ArrayList;

/**
NAry tree nodes organized from composite structural design pattern.
@param <T>
*/
public class _NAryTreeIntermediateNode<T> extends _NAryTreeNode<T> {
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final long serialVersionUID = 20150218L;
    
    private final ArrayList<_NAryTreeNode<T>> children;

    public _NAryTreeIntermediateNode(T inInfo)
    {
        super(inInfo);
        children = new ArrayList<_NAryTreeNode<T>>();
    }

    /**
    @return the children
    */
    public ArrayList<_NAryTreeNode<T>> getChildren() {
        return children;
    }
    
    public boolean
    replaceChild(
        final _NAryTreeNode<T> inOldNode, 
        final _NAryTreeNode<T> inNewNode)
    {
        int i;

        _NAryTreeIntermediateNode<T> subtree;
        subtree = new _NAryTreeIntermediateNode<T>(inOldNode.getData());
        subtree.getChildren().add(inNewNode);

        for ( i = 0; i < children.size(); i++ ) {
            if ( children.get(i) == inOldNode ) {
                children.remove(i);
                children.add(i, subtree);
                return true;
            }
        }
        return false;        
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
