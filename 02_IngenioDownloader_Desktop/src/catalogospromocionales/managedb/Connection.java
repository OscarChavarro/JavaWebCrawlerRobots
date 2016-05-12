package catalogospromocionales.managedb;

import databaseMongo.IngenioDatabaseConnection;

/**
 * Created by gerardo on 11/05/16.
 */
public class Connection {


    public static  IngenioDatabaseConnection conn = null;

    public static IngenioDatabaseConnection getConnection() {
        if(conn==null){
           conn = new IngenioDatabaseConnection(
                    "localhost", 27017, "catalogospromocionales", null);
        }
            return  conn;
    }


}
