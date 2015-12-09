//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - October 14 2007 - Oscar Chavarro: Original base version               =
//===========================================================================

package vsdk.toolkit.common;

public class AlgebraicExpressionException extends VSDKException
{
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20071014L;

    public AlgebraicExpressionException()
    {

    }

    public AlgebraicExpressionException(String message)
    {
        super(message);
    }

    public AlgebraicExpressionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AlgebraicExpressionException(Throwable cause)
    {
        super(cause);
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
