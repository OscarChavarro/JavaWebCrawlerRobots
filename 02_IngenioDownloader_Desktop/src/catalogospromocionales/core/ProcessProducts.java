package catalogospromocionales.core;

import catalogospromocionales.managedb.CategoryDb;
import catalogospromocionales.managedb.Connection;
import catalogospromocionales.managedb.ProductDb;
import catalogospromocionales.model.Category;
import catalogospromocionales.model.Product;
import catalogospromocionales.parser.ParseProduct;
import catalogospromocionales.parser.ParserCategory;
import catalogospromocionales.utils.HtmlParser;
import vsdk.toolkit.common.VSDK;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by sarah on 08/05/16.
 */
public class ProcessProducts {


    public static void main(String[] args) throws URISyntaxException, IOException {

        ArrayList<String> cookies = HtmlParser.getCookie(); // se busca una sola vez el cookie por cestiones de desempeño

        ArrayList<Category> listCategory =  downloadAndUpdateCategories(cookies);
        for(Category categoria:listCategory) {
            downloadProductsOfcategory(categoria, cookies);
        }

    }


    public static void tetCase() throws IOException {
        ArrayList<String> cookies = HtmlParser.getCookie(); // se busca una sola vez el cookie por cestiones de desempeño
        Category categoria = new Category("", "/promocionales/antiestres.html", "antiestres");
        CategoryDb catalogoManager = new CategoryDb(Connection.getConnection());
        catalogoManager.createCategoria(categoria);
        downloadProductsOfcategory(categoria, cookies);
    }


    public static ArrayList<Category> downloadAndUpdateCategories(ArrayList<String> cookies) {


        ParserCategory categoriaParser = new ParserCategory();
        CategoryDb catalogoManager = new CategoryDb(Connection.getConnection());

        //   DESCARGAR LISTADO DE CATEGORIAS y los registra e la base de datos si no estan registrados
        ArrayList<Category> listCategorias = categoriaParser.indexCategories(cookies);
        System.out.println("Numero de categorias:" + listCategorias.size());


        for (Category categoria : listCategorias) {
            // registra las categorias en la based de datos
            catalogoManager.createCategoria(categoria);
            System.out.println(categoria);
        }

        return listCategorias;

    }


    public static ArrayList<Product> downloadProductsOfcategory(Category categoria, ArrayList<String> cookies) throws IOException {
        ArrayList<Product> products = new ArrayList<Product>();
        ParserCategory categoriaParser = new ParserCategory();
        ProductDb producManager = new ProductDb(Connection.getConnection());
        CategoryDb catalogoManager = new CategoryDb(Connection.getConnection());
        ParseProduct parseProduct = new ParseProduct();

        products = categoriaParser.manageProductOfCategory(categoria, cookies);
        catalogoManager.updateIdCategoriaByUrl(categoria);

        for (Product newProduct : products) {

           try{

               producManager.create(newProduct);
               System.out.println("Agregando producto:" + newProduct.getId());
               newProduct = parseProduct.parseProduct(newProduct, HtmlParser.getCookie());
               producManager.update(newProduct);
               parseProduct.DownloadImage(newProduct, "./output/images/");
           } catch (Exception ex){
//               VSDK.reportMessageWithException(null, VSDK.FATAL_ERROR,
//                       "buildProductEntryFromPage", "Parse error", ex);
               System.out.println("---------------Error-----------------------");
               System.out.println("Error al procesar"+ex.getMessage());
               System.out.println("---------------Error-----------------------");

           }

        }

        return products;

    }


}
