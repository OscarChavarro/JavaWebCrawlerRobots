//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - October 14 2007 - Oscar Chavarro: Original base version               =
//===========================================================================

package vsdk.toolkit.common;

public class _AlgebraicExpressionVariableNode extends _AlgebraicExpressionNode
{
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20071014L;

    private AlgebraicExpression parent;
    private String name;

    public _AlgebraicExpressionVariableNode(AlgebraicExpression parent, String name)
    {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public double eval() throws AlgebraicExpressionException
    {
        double val = parent.getVariableValue(name);
        return val;
    }

    @Override
    public String toString()
    {
        String msg;

        msg = name;
        return msg;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
