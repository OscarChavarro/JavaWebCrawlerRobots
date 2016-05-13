package catalogospromocionales.core;

import catalogospromocionales.model.Category;
import catalogospromocionales.model.Product;
import webcrawler.IngenioTaggedHtml;
import webcrawler.TagSegment;
import catalogospromocionales.utils.htmlparser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sarah on 08/05/16.
 */
public class ParserCategory extends htmlparser {

    public static final String ENDPOINT_CATEGORIA = "promocionales/";
    public static final String ENDPOINT_PRODUCTOS = "/p/";
    public static final String URL = "http://www.catalogospromocionales.com";

    public static final String ENDPOINT_NEXT_PAGE = "/Catalogo/Default.aspx?id={1}&Page={2}";

//    http://www.catalogospromocionales.com/promocionales/plasticos-con-stylus.html
//    http://www.catalogospromocionales.com



    public ArrayList<Product> manageProductOfCategory(Category category, ArrayList<String> cookie){

        System.out.println("Procesando categoria url:"+category.getUrl());
        System.out.println("Procesando categoria nombre:"+category.getName());
        System.out.println("-------------------");
        ArrayList<Product> products = processPageOfCategory(category.getUrl(),cookie);
        if(products.size()>0)
        {
            category.setId(products.get(0).getCatId());
            ArrayList<Product> restProducts = indexProductsOfCategory(category.getId(),2,cookie);
            products.addAll(restProducts);
        }
        return products;
    }


    public ArrayList<Category> indexCategories(ArrayList<String> cookies) {
        ArrayList<Category> ctegoriasurl = new ArrayList<Category>();
        IngenioTaggedHtml pageProcessor = new IngenioTaggedHtml();
        pageProcessor.getInternetPage(URL + "/seccion/subcategorias.html", cookies, false);
        int count = 0;
        for (int indexSegment = 0; indexSegment < pageProcessor.segmentList.size(); indexSegment++) {
            TagSegment segment = pageProcessor.segmentList.get(indexSegment);
            if (isEndPoint(segment, ENDPOINT_CATEGORIA)) {
                String name = getContent(indexSegment, pageProcessor.segmentList);
                String url = getParameter(segment.getTagParameters(), HREF).trim().replaceAll("\"", "");
                if (!name.isEmpty()) {
                    count++;
                    ctegoriasurl.add(new Category("-1", url, name));
                }
            }
        }
//        System.out.println("count ategorias" + count);
        return ctegoriasurl;
    }

    public ArrayList<Product> indexProductsOfCategory(String categoryId, int startIndex,ArrayList<String> cookies){
        ArrayList<Product> productos = new ArrayList<>();
        ArrayList<Product> result = null;

        String path= ENDPOINT_NEXT_PAGE.replace("{1}",categoryId);
        do{
            result = processPageOfCategory(path.replace("{2}",startIndex+""),cookies);
            System.out.println(path.replace("{2}",startIndex+""));
            if(result.size()==0) {
                System.out.println("no se encontraron productos en la paina");
            }
            else{
                productos.addAll(result);
            }
            System.out.println("Indexando pagina: " + startIndex);
            startIndex++;
        }while (result.size()>0);
        return productos;
    }

    public ArrayList<Product> processPageOfCategory(String categoryPath, ArrayList<String> cookies) {

        int indexNombrePath = 2;
        int indexIdProduct = 3;
        int indexIdCategoria = 4;
        String idCategoria = null;
        IngenioTaggedHtml pageProcessor = new IngenioTaggedHtml();
        HashMap<String,Product > products= new HashMap<>();

        try {
            pageProcessor.getInternetPage(URL + categoryPath, cookies, false);
            for (int indexSegment = 0; indexSegment < pageProcessor.segmentList.size(); indexSegment++) {
                TagSegment segment = pageProcessor.segmentList.get(indexSegment);
                if (isEndPoint(segment, ENDPOINT_PRODUCTOS)) {
//                    print(segment);
                    String href = (getParameter(segment.getTagParameters(), HREF).trim().replaceAll("\"", ""));
                    String path[] = href.split("/");
                    String idProducto = path[indexIdProduct];
                    idCategoria = path[indexIdCategoria];
                    String nombrePath = path[indexNombrePath];

                    if(products.get(idProducto)==null) {
                        System.out.println("Indexando producto: "+idProducto);
                        products.put(idProducto,new Product(idProducto,nombrePath,href,idCategoria));

                    }

                }
            }

        } catch (Exception ex) {
        }
        return (new ArrayList<Product>(products.values()));
    }







    public static void  main(String[] args) {

        ParserCategory categoriaParser = new ParserCategory();
        ArrayList<String> cookies = categoriaParser.getCookie();
        System.out.println("cookie ready");



        if(true) return;


        Category cat = new Category("","/promocionales/antiestres.html","antiestres");
        ArrayList<Product> productosCat = categoriaParser.manageProductOfCategory(cat,cookies);
        System.out.println("Numero de productos:"+productosCat.size());
        System.out.println("Cat id:"+ cat.getId());

        // test listado de productos d euna categoria
        ArrayList<Product> productos = null;
        int index=1;
        do{
            productos = categoriaParser.processPageOfCategory("/Catalogo/Default.aspx?id=71&Page="+index,cookies);
            if(productos.size()==0)
                System.out.println("no se encontraron productos en la paina");

            System.out.println("-------------------------------------------");
            System.out.println("------------" + index + "------------------");
            int indexP=0;
            for(Product producto:productos)
            {
                System.out.println((++indexP)+" :"+producto);
            }

            index++;
        }while (productos!=null && productos.size()>0);


//
        //         // DESCARGAR LISTADO DE CATEGORIAS
       ArrayList<Category> listCategorias = categoriaParser.indexCategories(cookies);
        for (Category categoria : listCategorias) {
            System.out.println(categoria);
        }


        ArrayList<Product> list =categoriaParser.indexProductsOfCategory("71",1,cookies);
        System.out.println("Numero de productos:"+list.size());
    }

}
