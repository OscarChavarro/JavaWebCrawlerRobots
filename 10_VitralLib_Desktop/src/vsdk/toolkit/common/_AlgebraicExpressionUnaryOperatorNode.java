//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - October 14 2007 - Oscar Chavarro: Original base version               =
//===========================================================================

package vsdk.toolkit.common;

public class _AlgebraicExpressionUnaryOperatorNode extends _AlgebraicExpressionNode
{
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20071014L;

    private AlgebraicExpression parent;
    private String operatorName;
    private _AlgebraicExpressionNode operand;

    public _AlgebraicExpressionUnaryOperatorNode(AlgebraicExpression parent, String operatorName)
    {
        this.parent = parent;
        this.operatorName = operatorName;
        operand = null;
    }

    public void setOperand(_AlgebraicExpressionNode operand)
    {
        this.operand = operand;
    }

    @Override
    public double eval() throws AlgebraicExpressionException
    {
        double operandValue = operand.eval();
        double val = Double.NaN;

        if ( operatorName.equals("sin") ) {
            val = Math.sin(operandValue);
        }
        else if ( operatorName.equals("cos") ) {
            val = Math.cos(operandValue);
        }
        else if ( operatorName.equals("tan") ) {
            val = Math.tan(operandValue);
        }
        else if ( operatorName.equals("asin") ) {
            val = Math.asin(operandValue);
        }
        else if ( operatorName.equals("acos") ) {
            val = Math.acos(operandValue);
        }
        else if ( operatorName.equals("atan") ) {
            val = Math.atan(operandValue);
        }
        else if ( operatorName.equals("abs") ) {
            val = Math.abs(operandValue);
        }
        else if ( operatorName.equals("cbrt") ) {
            //val = Math.cbrt(operandValue);
            val = Math.pow(operandValue, 1.0/3.0);
        }
        else if ( operatorName.equals("ceil") ) {
            val = Math.ceil(operandValue);
        }
        else if ( operatorName.equals("sinh") ) {
            val = Math.sinh(operandValue);
        }
        else if ( operatorName.equals("cosh") ) {
            val = Math.cosh(operandValue);
        }
        else if ( operatorName.equals("tanh") ) {
            val = Math.tanh(operandValue);
        }
        else if ( operatorName.equals("toDegrees") ) {
            val = Math.toDegrees(operandValue);
        }
        else if ( operatorName.equals("toRadians") ) {
            val = Math.toRadians(operandValue);
        }
        else if ( operatorName.equals("exp") ) {
            val = Math.exp(operandValue);
        }
        else if ( operatorName.equals("floor") ) {
            val = Math.floor(operandValue);
        }
        else if ( operatorName.equals("log") ) {
            val = Math.log(operandValue);
        }
        else if ( operatorName.equals("ln") ) {
            val = Math.log(operandValue);
        }
        else if ( operatorName.equals("log10") ) {
            val = Math.log10(operandValue);
        }
        else if ( operatorName.equals("sqrt") ) {
            val = Math.sqrt(operandValue);
        }
        else if ( operatorName.equals("-") ) {
            val = -operandValue;
        }
        else {
            throw new AlgebraicExpressionException("Unknown unary operator or function \"" + operatorName + "\"");
        }
        return val;
    }

    @Override
    public String toString()
    {
        String msg;

        msg = operatorName + "(";
        msg += operand.toString();
        msg += ")";
        return msg;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
