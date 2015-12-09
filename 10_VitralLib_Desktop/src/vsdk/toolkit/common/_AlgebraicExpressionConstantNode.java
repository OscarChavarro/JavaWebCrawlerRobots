//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - October 14 2007 - Oscar Chavarro: Original base version               =
//===========================================================================

package vsdk.toolkit.common;

public class _AlgebraicExpressionConstantNode extends _AlgebraicExpressionNode
{
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20071014L;

    private double val;
    public _AlgebraicExpressionConstantNode(double val)
    {
        this.val = val;
    }

    @Override
    public double eval() throws AlgebraicExpressionException
    {
        return val;
    }

    @Override
    public String toString()
    {
        String msg;

        msg = "" + VSDK.formatDouble(val);
        return msg;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
