//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - October 14 2007 - Oscar Chavarro: Original base version               =
//===========================================================================

package vsdk.toolkit.common;

public abstract class _AlgebraicExpressionNode extends FundamentalEntity
{
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20071014L;

    public abstract double eval() throws AlgebraicExpressionException;

    @Override
    public abstract String toString();
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
