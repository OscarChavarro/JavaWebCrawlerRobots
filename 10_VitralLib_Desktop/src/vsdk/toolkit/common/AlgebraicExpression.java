//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - October 14 2007 - Oscar Chavarro: Original base version               =
//===========================================================================

package vsdk.toolkit.common;

// Java basic classes
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.StreamTokenizer;
import java.io.StringReader;

// VSDK classes

/**
A `AlgebraicExpression` is an algebraic expression composed of algebraic
variables, unary and binary operators (including basic logarithmic, exponential,
and trigonometric functions) with a set of values for algebraic variables
that can be evaluated, giving as a result a `double` value.

This class establishes a Facade (Facade design pattern for all regular
expression operations) and plays a user role in a composite design pattern
with _AlgebraicExpression*Node classes.
*/
public class AlgebraicExpression extends FundamentalEntity
{
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20071014L;

    private _AlgebraicExpressionNode root;
    private HashMap<String, Double> values;

    public AlgebraicExpression()
    {
        values = new HashMap<String, Double>();
        defineValue("PI", Math.PI);
        defineValue("E", Math.E);
        root = null;
    }

    public final void
    defineValue(String name, double val)
    {
        values.put(name, new Double(val));
    }

    @Override
    public String toString()
    {
        String msg;

        if ( root == null ) {
            msg = "<Invalid Expression>";
        }
        else {
            msg = root.toString();
        }

        return msg;
    }

    public double getVariableValue(String name) throws AlgebraicExpressionException
    {
        Double val;
        val = values.get(name);
        if ( val == null ) {
            throw new AlgebraicExpressionException("AlgebraicExpression.getVariableValue: Variable \"" + name + "\" not defined");
        }
        return val.doubleValue();
    }

    private boolean isOperator(String cad)
    {
        boolean answer;
        if ( cad.length() > 1 ) return false;
        switch ( cad.charAt(0) ) {
          case '+':
          case '-':
          case '*':
          case '/':
          case '^':
            answer = true;
            break;
          default:
            answer = false;
            break;
        }
        return answer;
    }

    private _AlgebraicExpressionNode buildExpressionTree(ArrayList<String> tokens)
        throws AlgebraicExpressionException
    {
        //-----------------------------------------------------------------
        int i;
        int level;
        char c;

        //- Trim out embracing parenthesis from expression ----------------
        while ( tokens.get(0).equals("(") &&
                tokens.get(tokens.size()-1).equals(")") ) {
            level = 0;
            boolean trim = true;

            for ( i = 0; i < tokens.size(); i++ ) {
                c = tokens.get(i).charAt(0);
                if ( c == '(' ) {
                    level++;
                }
                else if ( c == ')' ) {
                    level--;
                }
                if ( level == 0 && i < (tokens.size()-1) ) {
                    trim = false;
                }
            }

            if ( !trim ) {
                break;
            }
            tokens.remove(tokens.size()-1);
            tokens.remove(0);
        }

        //- Trivial case: if I am a single token, I create myself here ----
        if ( tokens.size() == 1 ) {
            if ( isOperator(tokens.get(0)) ) {
                throw new AlgebraicExpressionException("Parse error, invalid placement for operator \'" + tokens.get(0).charAt(0) + "\'");
            }
            else if ( Character.isDigit(tokens.get(0).charAt(0)) ||
                      tokens.get(0).charAt(0) == '-' ) {
                return new _AlgebraicExpressionConstantNode(Double.parseDouble(tokens.get(0)));
            }
            else {
                return new _AlgebraicExpressionVariableNode(this, tokens.get(0));
            }
        }

        //- Recursive cases -----------------------------------------------
        ArrayList<Integer> topLevelConnectorIndexes;

        topLevelConnectorIndexes = new ArrayList<Integer>();

        // Search for top level connector operators
        level = 0;
        for ( i = 0; i < tokens.size(); i++ ) {
            c = tokens.get(i).charAt(0);
            if ( c == '(' ) {
                level++;
                continue;
            }
            else if ( c == ')' ) {
                level--;
                continue;
            }
            if ( level == 0 && isOperator(tokens.get(i)) ) {
                topLevelConnectorIndexes.add(new Integer(i));
            }
        }

        // Determine the top level connector
        if ( topLevelConnectorIndexes.size() <= 0 ) {
            // Try a funcional form expression
            _AlgebraicExpressionUnaryOperatorNode uo;
            uo = new _AlgebraicExpressionUnaryOperatorNode(this, tokens.get(0));
            tokens.remove(0);
            uo.setOperand(buildExpressionTree(tokens));
            return uo;
        }

        for ( i = 0; i < topLevelConnectorIndexes.size(); i++ ) {
        }

        int mainConnector = 0;

        // '^' goes with lower priority, will be overwritten if lower
        // precedent operator is later founded
        for ( i = 0; i < topLevelConnectorIndexes.size(); i++ ) {
            c = tokens.get(topLevelConnectorIndexes.get(i)).charAt(0);
            if ( c == '^' ) {
                mainConnector = topLevelConnectorIndexes.get(i);
                break;
            }
        }

        // '*' y '/' goes before `^`
        for ( i = 0; i < topLevelConnectorIndexes.size(); i++ ) {
            c = tokens.get(topLevelConnectorIndexes.get(i)).charAt(0);
            if ( c == '*' || c == '/' ) {
                mainConnector = topLevelConnectorIndexes.get(i);
                break;
            }
        }

        // '+' y '-' goes before the others
        for ( i = 0; i < topLevelConnectorIndexes.size(); i++ ) {
            c = tokens.get(topLevelConnectorIndexes.get(i)).charAt(0);
            if ( c == '+' || c == '-' ) {
                mainConnector = topLevelConnectorIndexes.get(i);
                break;
            }
        }

        char op = tokens.get(mainConnector).charAt(0);

        if ( op != '-' && mainConnector == -1 ) {
            throw new AlgebraicExpressionException("Operator \"" + op + "\" can not be used as a unary operator");
          }
          else if ( op == '-' && mainConnector == 0 ) {
            _AlgebraicExpressionUnaryOperatorNode uo;
            uo = new _AlgebraicExpressionUnaryOperatorNode(this, "-");
            tokens.remove(0);
            uo.setOperand(buildExpressionTree(tokens));
            return uo;
          }
          else {
            ArrayList<String> leftTokens;
            leftTokens = new ArrayList<String>();
            for ( i = 0; i < mainConnector && i < tokens.size(); i++ ) {
                leftTokens.add(tokens.get(i));
            }

            ArrayList<String> rightTokens;
            rightTokens = new ArrayList<String>();
            for ( i = mainConnector + 1; i < tokens.size(); i++ ) {
                rightTokens.add(tokens.get(i));
            }

            _AlgebraicExpressionBinaryOperatorNode bo;
            bo = new _AlgebraicExpressionBinaryOperatorNode(this, op);
            bo.setLeftOperand(buildExpressionTree(leftTokens));
            bo.setRightOperand(buildExpressionTree(rightTokens));
            return bo;
        }

        //-----------------------------------------------------------------
    }

    /**
    @param regexp
    @throws vsdk.toolkit.common.AlgebraicExpressionException
    */
    public void setExpression(String regexp) throws AlgebraicExpressionException
    {
        //-----------------------------------------------------------------
        ArrayList<String> tokens;
        StringReader stream = new StringReader(regexp);
        StreamTokenizer parser = new StreamTokenizer(stream);

        tokens = new ArrayList<String>();
        parser.resetSyntax();
        parser.eolIsSignificant(false);
        parser.slashSlashComments(false);
        parser.slashStarComments(false);
        parser.whitespaceChars(' ', ' ');
        parser.wordChars('A', 'Z');
        parser.wordChars('a', 'z');
        parser.wordChars('0', '9');
        parser.wordChars('_', '_');
        parser.ordinaryChar('+');
        parser.ordinaryChar('-');
        parser.ordinaryChar('*');
        parser.ordinaryChar('/');
        parser.ordinaryChar('(');
        parser.ordinaryChar(')');
        parser.ordinaryChar('^');
        parser.parseNumbers();

        int tokenType;

        do {
            try {
                tokenType = parser.nextToken();
            }
            catch ( IOException e ) {
                break;
            }
            switch (tokenType) {
              case StreamTokenizer.TT_EOL:
                break;
              case StreamTokenizer.TT_EOF:
                break;
              case StreamTokenizer.TT_NUMBER:
                tokens.add("" + parser.nval);
                break;
              case StreamTokenizer.TT_WORD:
                tokens.add(parser.sval);
                break;
              default:
                String content;
                content = parser.toString();
                if ( parser.ttype != '\"' ) {
                    tokens.add("" + content.charAt(7));
                }
                break;
            }
        } while ( tokenType != StreamTokenizer.TT_EOF );

        //-----------------------------------------------------------------
        root = buildExpressionTree(tokens);
    }

    public double eval() throws AlgebraicExpressionException
    {
        if ( root == null ) {
            throw new AlgebraicExpressionException("Null expression, can not evaluate.");
        }
        return root.eval();
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
