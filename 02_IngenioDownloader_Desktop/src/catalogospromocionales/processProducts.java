package catalogospromocionales;

import com.mongodb.DBCollection;
import databaseMongo.IngenioDatabaseConnection;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import webcrawler.IngenioTaggedHtml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        databaseConnection = new IngenioDatabaseConnection(
                "localhost", 27017, "catalogospromocionales", null);
        categoriasCollection = databaseConnection.createMongoCollection("categorias");
        productosCollection = databaseConnection.createMongoCollection("productos");
        ArrayList<String> cookies = catalogoCategorias.getCookie();
        List<String> categorias = catalogoCategorias.procesar(categoriasCollection,cookies);

        for(String categoriaPath:categorias){
            System.out.println(categoriaPath);
            int indexPage = 2; //
            String idCAtegoria =  catalogoCategorias.procesarPaginaCatalogo(productosCollection,categoriaPath,cookies);
            catalogoCategorias.updateIdCategoria(categoriasCollection,categoriaPath,idCAtegoria);
            System.out.println(idCAtegoria);
            while(idCAtegoria!=null){
                idCAtegoria = catalogoCategorias.procesarPaginaCatalogo(productosCollection,"/Catalogo/Default.aspx?id="+idCAtegoria+"&Page="+indexPage,cookies);
//                System.out.println(idCAtegoria);
                indexPage++;
            }
        }

    }


}
