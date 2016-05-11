package catalogospromocionales;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import databaseMongo.IngenioDatabaseConnection;
import webcrawler.IngenioTaggedHtml;
import webcrawler.TagParameter;
import webcrawler.TagSegment;
import catalogospromocionales.utils.htmlparser;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarah on 08/05/16.
 */
public class catalogoCategorias extends htmlparser {

    public static final String ENDPOINT_CATEGORIA = "promocionales/";
    public static final String ENDPOINT_PRODUCTOS = "/p/";
    public static final String URL = "http://www.catalogospromocionales.com";
//    http://www.catalogospromocionales.com/promocionales/plasticos-con-stylus.html
//    http://www.catalogospromocionales.com


    public static ArrayList<String> getCookie(){
        ArrayList<String> cookies = new ArrayList<String>();
        IngenioTaggedHtml pageProcessor = new IngenioTaggedHtml();
        pageProcessor.getInternetPage("http://www.catalogospromocionales.com/Catalogo/Default.aspx?id=265&page=7", cookies, false);
        return cookies;
    }


    public static ArrayList<String> procesar(DBCollection categoriaCollection,ArrayList<String> cookies) {

        ArrayList<String> ctegoriasurl = new ArrayList<String>();
        IngenioTaggedHtml pageProcessor = new IngenioTaggedHtml();
        pageProcessor.getInternetPage(URL + "/seccion/subcategorias.html", cookies, false);
        int count = 0;
        for (int indexSegment = 0; indexSegment < pageProcessor.segmentList.size(); indexSegment++) {
            TagSegment segment = pageProcessor.segmentList.get(indexSegment);
            if (isEndPoint(segment, ENDPOINT_CATEGORIA)) {
//                print(segment);
                if (!getContent(indexSegment, pageProcessor.segmentList).isEmpty()) {
                    DBObject searchKey = new BasicDBObject("url", getParameter(segment.getTagParameters(), HREF).trim().replaceAll("\"", ""));
                    DBCursor cursor = categoriaCollection.find(searchKey);
                    if (!cursor.hasNext()) {
                        count++;
                        BasicDBObject ca = new BasicDBObject();
                        ca.append("nombre", getContent(indexSegment, pageProcessor.segmentList));
                        ca.append("url", getParameter(segment.getTagParameters(), HREF).trim().replaceAll("\"", ""));
                        ctegoriasurl.add(getParameter(segment.getTagParameters(), HREF).trim().replaceAll("\"", ""));
                        categoriaCollection.insert(ca);
                    }
                }
            }
        }
        System.out.println("count ategorias" + count);
        return ctegoriasurl;
    }

    public static String procesarPaginaCatalogo(DBCollection productosCollecton, String categoriaPath,ArrayList<String> cookies) {
        try {

            int indexNombrePath = 2;
            int indexIdProduct = 3;
            int indexIdCategoria = 4;
            String idCategoria = null;
            IngenioTaggedHtml pageProcessor = new IngenioTaggedHtml();
            System.out.println(URL + categoriaPath);
            pageProcessor.getInternetPage(URL + categoriaPath, cookies, false);

            for (int indexSegment = 0; indexSegment < pageProcessor.segmentList.size(); indexSegment++) {
                TagSegment segment = pageProcessor.segmentList.get(indexSegment);
//                print(segment);
                if (isEndPoint(segment, ENDPOINT_PRODUCTOS)) {
                    String href = (getParameter(segment.getTagParameters(), HREF).trim().replaceAll("\"", ""));
                    System.out.println(href);
                    String path[] = href.split("/");
                    String idProducto = path[indexIdProduct];
                    idCategoria = path[indexIdCategoria];
                    String nombrePath = path[indexNombrePath];
                    DBObject searchKey = new BasicDBObject("id", idProducto);
                    if (productosCollecton.find(searchKey).hasNext()) {
//                        System.out.println(searchKey + " ya existe");
                    } else {
//                        System.out.println("Entro");
                        BasicDBObject producto = new BasicDBObject();
                        producto.append("id", idProducto);
                        producto.append("cat_id", idCategoria);
                        producto.append("nombrePath", nombrePath);
                        productosCollecton.insert(producto);
                    }

                }

            }

            return idCategoria;
        } catch (Exception ex) {

        }
        return null;
    }

    public static void updateIdCategoria(DBCollection categoriaCollection, String url, String idCategoria) {
        if (idCategoria == null || idCategoria.isEmpty()) return;
        BasicDBObject query = new BasicDBObject("url", url);
//      BasicDBObject update = new BasicDBObject("id_cat",idCategoria);
        DBObject update = new BasicDBObject();
        update.put("$set", new BasicDBObject("id_cat", idCategoria));
        categoriaCollection.findAndModify(query, update);
    }

}
