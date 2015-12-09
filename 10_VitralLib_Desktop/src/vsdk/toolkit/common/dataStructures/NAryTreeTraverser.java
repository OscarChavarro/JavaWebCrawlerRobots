//===========================================================================
package vsdk.toolkit.common.dataStructures;

import vsdk.toolkit.common.Entity;

/**
*/
public abstract class NAryTreeTraverser extends Entity {
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final long serialVersionUID = 20150218L;
    
    public abstract void start();
    public abstract void end();
    public abstract void visit(Object inElement, final int inLevel);
    
    protected String formatHeader(final int inLevel)
    {
        String head = "";
        
        if ( inLevel == 2 ) {
            head += "  - ";
        }
        else if ( inLevel == 3 ) {
            head += "      . ";
        }
        else if ( inLevel >= 4 ) {
            int i;
            head += "        ";
            for ( i = 4; i < inLevel; i++ ) {
                    
                head += "  ";
            }
        }
        
        return head;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
