//===========================================================================
package model.databaseMysql;

// Java basic classes
import java.util.ArrayList;

// JDBC classes
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// VSDK classes
import vsdk.toolkit.common.VSDK;

// Application specific classes
import model.databaseMysql.model.Property;

/**
*/
public class ThemeForestDatabaseConnection {
    private static Connection connection;

    static {
        connection = null;
    }

    /**
    Connect with the database.
    @return Connection
    */
    public static Connection createSqlConnection() {
        if ( connection != null ) {
            return connection;
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String databaseServer = "jdbc:mysql://aavad8x11q8eb5.cmmin6twanba.us-east-1.rds.amazonaws.com:3306/domoly_metrocuadrado_import";
            String databaseUser = "ebroot";
            String databasePassword = "awspruebis$$";

            connection = DriverManager.getConnection(
                databaseServer, databaseUser, databasePassword);
        }
        catch ( ClassNotFoundException e ) {
            System.out.println("ERROR: cannot connect to driver " + e.getMessage());
            connection = null;
        }
        catch ( SQLException e ) {
            System.out.println(e.getMessage());
            connection = null;
        }
        finally {
            System.out.println(
                "[DatabaseConnection] SQL connection stablished: " +
                    connection);
        }

        if ( connection == null ) {
            System.out.println("ERROR: CONNECTION NOT AVAILABLE");
        }
        return connection;
    }

    /**
    @return
    */
    public static ArrayList<Property> getProperties() {
        if ( createSqlConnection() == null ) {
            return null;
        }

        ArrayList<Property> arr = new ArrayList<Property>();

        Property p;
        p = new Property();

        String sqlStatement = "SELECT * FROM property;";

        try {
            Statement sqlData;
            sqlData = connection.createStatement();
            sqlData.executeQuery(sqlStatement);
            ResultSet rs = sqlData.getResultSet();
            int count;
            for ( count = 0; rs.next(); count++ ) {
                p.importFromJdbcRow(rs);
                arr.add(p);
            }
            System.out.println("Fetched " + count + " properties");
            sqlData.close();
        }
        catch ( SQLException e ) {
            VSDK.reportMessageWithException(null, VSDK.FATAL_ERROR,
                    "MetroCuadradoDatabaseConnection.getSiteCoordinates", "SQL error", e);
        }

        return arr;
    }

    /**
    @param p
    */
    public void insertProperty(Property p)
    {
        if ( createSqlConnection() == null ) {
            return;
        }

        String sqlStatement = "INSERT INTO property " + p.exportMysqlValues();

        try {
            Statement sqlData;
            sqlData = connection.createStatement();
            sqlData.execute(sqlStatement);
            sqlData.close();
        }
        catch ( SQLException e ) {
            System.out.println(sqlStatement);
            VSDK.reportMessageWithException(null, VSDK.FATAL_ERROR,
                "MetroCuadradoDatabaseConnection.insertProperty", "SQL error", e);
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
