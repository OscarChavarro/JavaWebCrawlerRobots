//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - October 30 2007 - Oscar Chavarro: Original base version               =
//===========================================================================

package vsdk.toolkit.common.linealAlgebra;

import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.FundamentalEntity;

/**
This class is a data structure that represents a 4x4 matrix
 */

public class MatrixNxM extends FundamentalEntity
{
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20071030L;

    private int numRows;    // This is the number of rows in the matrix
    private int numColumns; // This is the number of columns in the matrix

    /** This is a N by M array of type double that represents the data of the 
        matrix */
    private double M[][];

    /**
    This constructor builds the NxM identity matrix
    @param n
    @param m
    @throws java.lang.Exception
    */
    public MatrixNxM(int n, int m) throws Exception
    {
        if ( n <= 0 || m <= 0 ) {
            throw new Exception("Invalid matrix size!");
        }
        numRows = n;
        numColumns = m;
        M = new double[n][m];
        identity();
    }

    /**
     This constructor builds a matrix given an existing matrix, and copies
     its contents to the newly created one.
     @param B The matrix used to build this matrix data from
     */
    public MatrixNxM(MatrixNxM B)
    {
        this.numRows = B.numRows;
        this.numColumns = B.numColumns;
        M = new double[numRows][numColumns];
        int row, column;

        for ( row = 0; row < numRows; row++ ) {
            for ( column = 0; column < numColumns; column++ ) {
                M[row][column] = B.M[row][column];
            }
        }
    }

    /**
    This methods changes current matrix to be the NxM identity matrix
    */
    public final void identity()
    {
        int i, j;
        for ( i = 0; i < numRows; i++ ) {
            for ( j = 0; j < numColumns; j++ ) {
                if ( i == j ) {
                    M[i][j] = 1.0;
                }
                else {
                    M[i][j] = 0.0;
                }
            }
        }
    }

    public double getNumRows()
    {
        return numRows;
    }

    public double getNumColumns()
    {
        return numColumns;
    }

    public double getVal(int row, int column) throws Exception
    {
        if ( row < 0 || row >= numRows || column < 0 || column >= numColumns ) {
            throw new Exception("Invalid matrix position [" + row + "][" + column + "]");
        }
        return M[row][column];
    }

    public void setVal(int row, int column, double val) throws Exception
    {
        if ( row < 0 || row >= numRows || column < 0 || column >= numColumns ) {
            throw new Exception("Invalid matrix position [" + row + "][" + column + "]");
        }
        M[row][column] = val;
    }

    public MatrixNxM inverse() throws Exception
    {
        MatrixNxM i = new MatrixNxM(this);
        i.invert();
        return i;
    }

    /**
    Converts current matrix into it's invert matrix.
    @throws java.lang.Exception
    */
    public void invert() throws Exception
    {
        double d = determinant();

        if ( Math.abs(d) < VSDK.EPSILON ) {
            throw new Exception("Trying to invert a matrix with zero determinant!");
        }

        double a = 1/d;

        MatrixNxM N = cofactors(), N2;
        N.transpose();

        N2 = N.multiply(a);
        this.M = N2.M;
    }

    public MatrixNxM cofactors() throws Exception
    {
        MatrixNxM N = new MatrixNxM(numRows, numColumns);
        int row, column;
        double minor3x3[] = new double[9];
        double sign;

        MatrixNxM minor;

        for ( row = 0; row < numRows; row++ ) {
            for ( column = 0; column < numColumns; column++ ) {
                minor = buildMinor(row, column);
                if ( (row+column) % 2 == 0 ) {
                    sign = 1;
                }
                else {
                    sign = -1;
                }
                N.M[row][column] =
                    sign*minor.determinant();
            }
        }
        return N;
    }

    /**
     Converts current matrix into it's transpose matrix.
     */
    public void transpose()
    {
        double R[][] = new double[numColumns][numRows];

        int row, column;

        for ( row = 0; row < numRows; row++ ) {
            for ( column = 0; column < numColumns; column++ ) {
                R[column][row] = M[row][column];
            }
        }

        int b;
        b = numColumns;
        numColumns = numRows;
        numRows = b;

        M = R;
    }

    /**
    Multiply this matrix by a scalar, note that this method doesn't modify 
    this matrix. 
    @param a The scalar by whom this matrix will be multiplied
    @return A new Matrix3D that contains the value of current matrix 
    multiplied by the input parameter.
    @throws java.lang.Exception
    */
    public final MatrixNxM multiply(double a) throws Exception
    {
        MatrixNxM R = new MatrixNxM(numRows, numColumns);
        int row, column;

        for ( row = 0; row < numRows; row++ ) {
            for ( column = 0; column < numColumns; column++ ) {
                R.M[row][column] = a*M[row][column];
            }
        }
        return R;
    }

    /**
    This method multiplies an input matrix by this matrix, the result is a 
    new matrix and current matrix is not modified.
    @param other The matrix by whom this matrix will be multiplied
    @return The matrix result of the multiplication.
    @throws java.lang.Exception
    */
    public MatrixNxM multiply(MatrixNxM other) throws Exception
    {
        if ( this.numColumns != other.numRows ) {
            throw new Exception("When multiplying matrices, first operand number of columns must match second operand number of rows.");
        }

        MatrixNxM R = new MatrixNxM(this.numRows, other.numColumns);
        int row_a, column_b, row_b;
        double acumulado;

        for( row_a = 0; row_a < R.numRows; row_a++ ) {
            for( column_b = 0; column_b < R.numColumns; column_b++ ) {
                acumulado = 0;
                for( row_b = 0; row_b < 4; row_b++ ) {
                    acumulado += M[row_a][row_b]*other.M[row_b][column_b];
                }
                R.M[row_a][column_b] = acumulado;
            }
        }
        return R;
    }

/*
    private void fillMinor(double minor3x3[], int rowPivot, int columnPivot)
    {
        int i, j;
        int index = 0;

        for ( i = 0; i < 4; i++ ) {
            for ( j = 0; j < 4; j++ ) {
                if ( i != rowPivot && j != columnPivot ) {
                    minor3x3[index] = M[i][j];
                    index++;
                }
            }
        }
    }
*/

    public MatrixNxM buildMinor(int row, int column) throws Exception
    {
        if ( numColumns <= 1 || numRows <= 1 ) {
            throw new Exception("Matrix must be at least of size 2x2 to have a minor matrix!");
        }

        if ( row < 0 || row >= numRows ||
             column < 0 || column >= numColumns ) {
            throw new Exception("Invalid pivot position for minor matrix!");
        }

        MatrixNxM minor = new MatrixNxM(numRows-1, numColumns-1);

        int r1, r2, c1, c2;

        for ( r1 = 0, r2 = 0; r1 < numRows; r1++ ) {
            if ( r1 == row ) continue;
            for ( c1 = 0, c2 = 0; c1 < numColumns; c1++ ) {
                if ( c1 == column ) continue;
                minor.setVal(r2, c2, getVal(r1, c1));
                c2++;
            }
            r2++;
        }

        return minor;
    }

    /**
    This method computes the determinant of this matrix.
    @return this matrix determinant
    @throws java.lang.Exception
    */
    public double determinant() throws Exception
    {
        if ( numColumns != numRows ) {
            throw new Exception("Matrix must be square to have a determinant");
        }

        if ( numColumns == 1 ) {
            return M[0][0];
        }

        int i, j;
        double acum = 0;
        int sign;
        MatrixNxM minor;

        i = 0;
        for ( j = 0, sign = 1; j < numColumns; j++, sign *= -1 ) {
            minor = buildMinor(i, j);
            acum += ((double)sign)*minor.determinant()*M[i][j];
        }

        return acum;
    }

    /**
     This method creates an String representation of this matrix, suitable
     for human interpretation. Note that the matrix values are formated,
     so precision is lost in sake of readability.
     @return The String representation of this matrix
     */
    @Override
    public String toString()
    {
        String msg;

        msg = "\n------------------------------\n";
        int row, column, pos;

        msg = msg + "  - Matrix of " + numRows + " rows by " + numColumns + " columns\n";

        for ( row = 0; row < numRows; row++, pos++ ) {
            for ( pos = 0, column = 0; column < numColumns; column++ ) {
                msg = msg + VSDK.formatDouble(M[row][column]) + " ";
            }
            msg = msg + "\n";
        }
        msg = msg + "------------------------------\n";
        return msg;
    }

/*
    public double[] exportToDoubleArrayRowOrder()
    {
        double array[] = new double[16];
        int i, j, k;
        for ( i = 0, k = 0; i < 4; i++ ) {
            for ( j = 0; j < 4; j++, k++ ) {
                array[k] = M[i][j];
            }
        }
        return array;
    }
*/
/*
    public float[] exportToFloatArrayRowOrder()
    {
        float array[] = new float[16];
        int i, j, k;
        for ( i = 0, k = 0; i < 4; i++ ) {
            for ( j = 0; j < 4; j++, k++ ) {
                array[k] = (float)M[i][j];
            }
        }
        return array;
    }
*/

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
