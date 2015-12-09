//===========================================================================
package vsdk.toolkit.common.dataStructures;

import java.util.ArrayList;
import vsdk.toolkit.common.FundamentalEntity;
import vsdk.toolkit.common.VSDK;

/**
VSDK implementation of a generic N-ary tree type. The following design criteria
was followed:
  - Tree root is always at level 1. Levels are counted from 1.
  - Tree is supposed to store unique values. Several algorithms will fail if
    this is not followed.
@param <T>
*/
public class NAryTree<T> extends FundamentalEntity {
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final long serialVersionUID = 20150218L;
    
    private _NAryTreeNode<T> root;

    /**
    Creates an empty tree.
    */
    public NAryTree()
    {
        root = null;
    }
    
    /**
    Creates a one level tree containing given data element in its root.
    @param inRootContent 
    */
    public NAryTree(T inRootContent)
    {
        root = (_NAryTreeNode<T>)new _NAryTreeLeafNode<T>(inRootContent);    
    }

    /**
    @return the root
    */
    public _NAryTreeNode<T> getRoot() {
        return root;
    }

    /**
    @param root the root to set
    */
    public void setRoot(_NAryTreeNode<T> root) {
        this.root = root;
    }

    /**
    @param inNode
    @param inKey
    @return 
    */
    private _NAryTreeNode<T> searchNodeByContent(
        final _NAryTreeNode<T> inNode, final T inKey)
    {
        if ( inNode == null ) {
            VSDK.reportMessage(this, VSDK.FATAL_ERROR, "searchNodeByContent", 
                "Null node not expected.");
            return null;
        }
        if ( inNode instanceof _NAryTreeIntermediateNode ) {
            _NAryTreeIntermediateNode<T> node;
            node = (_NAryTreeIntermediateNode<T>)inNode;
            _NAryTreeNode<T> candidate;
            ArrayList<_NAryTreeNode<T>> l;
            l = node.getChildren();
            int i;
            
            if ( node.getData().equals(inKey) ) {
                return node;
            }
            for ( i = 0; i < l.size(); i++ ) {
                candidate = searchNodeByContent(l.get(i), inKey);
                if ( candidate != null ) {
                    return candidate;
                }
            }
        }
        else if ( inNode instanceof _NAryTreeLeafNode ) {
            if ( inNode.getData().equals(inKey) ) {
                return inNode;
            }
        }
        else {
            VSDK.reportMessage(this, VSDK.FATAL_ERROR, "searchNodeByContent", 
                "unexpected node subclass " + inNode.getClass().getName());
        }        
        return null;
    }
    
    /**
    Given a key search data, this method searches the tree for given key.
    @param inKey
    @return containing node or null if element is not inside tree
    */
    public _NAryTreeNode<T> searchNodeByContent(final T inKey)
    {
        if (  getRoot() == null ) {
            return null;
        }
        return searchNodeByContent(getRoot(), inKey);
    }

    /**
    Given the data for an existing node, current method inserts a new child
    for the new data.
 
    User should take care in order to not insert several times the same data 
    value.

    @param inExistingNodeData
    @param inNewData
    @return 
    */
    public boolean addChild(
        final T inExistingNodeData, 
        final T inNewData) 
    {
        _NAryTreeNode<T> parent;
        parent = searchNodeByContent(inExistingNodeData);
        if ( parent == null ) {
            return false;
        }
        
        //---------------------------------------------------------------------
        _NAryTreeIntermediateNode<T> node;
        if ( parent instanceof _NAryTreeIntermediateNode ) {
            node = (_NAryTreeIntermediateNode<T>)parent;
            node.getChildren().add(new _NAryTreeLeafNode<T>(inNewData));
            return true;
        }
        else if ( parent instanceof _NAryTreeLeafNode ) {
            if ( parent == root ) {
                T old = root.getData();
                
                node = new _NAryTreeIntermediateNode<T>(old);
                node.getChildren().add(new _NAryTreeLeafNode<T>(inNewData));
                root = node;
            }
            else {
                _NAryTreeNode<T> grandpa;
                grandpa = searchParent(root, parent);
                if ( grandpa == null ) {
                    VSDK.reportMessage(this, VSDK.FATAL_ERROR, "addChild", 
                        "unexpected structure! ");                    
                    return false;
                }
                if ( grandpa instanceof _NAryTreeIntermediateNode ) {
                    node = (_NAryTreeIntermediateNode<T>)grandpa;
                    
                    node.replaceChild(
                        parent, new _NAryTreeLeafNode<T>(inNewData));
                }
            }
        }
        else {
            VSDK.reportMessage(this, VSDK.FATAL_ERROR, "addChild", 
                "unexpected node subclass " + parent.getClass().getName());
        }        
        
        return false;
    }
    
    /**
    @param inNode
    @param inKey
    @return 
    */
    private _NAryTreeNode<T> searchParent(
        _NAryTreeNode<T> inNode,
        _NAryTreeNode<T> inKey)
    {
        if ( inNode == null ) {
            VSDK.reportMessage(this, VSDK.FATAL_ERROR, "searchNodeByContent", 
                "Null node not expected.");
            return null;
        }
        if ( inNode instanceof _NAryTreeIntermediateNode ) {
            _NAryTreeIntermediateNode<T> node;
            node = (_NAryTreeIntermediateNode<T>)inNode;
            
            ArrayList<_NAryTreeNode<T>> l = node.getChildren();
            int i;
            for ( i = 0; i < l.size(); i++ ) {
                if ( l.get(i) == inKey ) {
                    return inNode;
                }
            }
            
            _NAryTreeNode<T> candidate;
            for ( i = 0; i < l.size(); i++ ) {
                candidate = searchParent(l.get(i), inKey);
                if ( candidate != null ) {
                    return candidate;
                }
            }
        }
        else if ( inNode instanceof _NAryTreeLeafNode ) {
            return null;
        }
        else {
            VSDK.reportMessage(this, VSDK.FATAL_ERROR, "searchNodeByContent", 
                "unexpected node subclass " + inNode.getClass().getName());
        }        
        return null;
        
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
