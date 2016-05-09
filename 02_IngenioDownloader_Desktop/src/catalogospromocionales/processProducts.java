package catalogospromocionales;

import com.mongodb.DBCollection;
import databaseMongo.IngenioDatabaseConnection;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarah on 08/05/16.
 */
public class processProducts {

    public static void main(String[] args) throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {

        IngenioDatabaseConnection databaseConnection;
        DBCollection categoriasCollection;
        DBCollection productosCollection;
        DBCollection marPicoCategory;
        databaseConnection = new IngenioDatabaseConnection(
                "localhost", 27017, "catalogospromocionales", null);
        categoriasCollection = databaseConnection.createMongoCollection("categorias");
        productosCollection = databaseConnection.createMongoCollection("productos");
        List<String> categorias = catalogoCategorias.procesar(categoriasCollection);

//        List<String> categorias = new ArrayList<>();
//        categorias.add("/promocionales/antiestres.html");
//        /promocionales/antiestres.html

        // procesamos la primera pagina para obtener el id de lacategoria
        for(String categoriaPath:categorias){
            System.out.println(categoriaPath);
            int indexPage = 2; //
            String idCAtegoria =  catalogoCategorias.procesarPaginaCatalogo(productosCollection,categoriaPath);
            catalogoCategorias.updateIdCategoria(categoriasCollection,categoriaPath,idCAtegoria);
            while(idCAtegoria!=null){
                idCAtegoria = catalogoCategorias.procesarPaginaCatalogo(productosCollection,"/Catalogo/Default.aspx?id="+idCAtegoria+"&Page="+indexPage);
//                idCAtegoria = catalogoCategorias.procesarPaginaCatalogo(productosCollection,"/Catalogo/Default.aspx?id=252&Page=2");
//                idCAtegoria = catalogoCategorias.procesarPaginaCatalogo(productosCollection, URLEncoder.encode("/Catalogo/Default.aspx?id=252&Page=2","UTF-8"));
//                http://www.catalogospromocionales.com/Catalogo/Default.aspx?id=252&Page=2
                System.out.println(idCAtegoria);
                indexPage++;
            }
//            http://www.catalogospromocionales.com/Catalogo/Default.aspx?id=252&Page=2

        }

    }


}
