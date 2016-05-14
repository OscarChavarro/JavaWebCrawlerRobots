package catalogospromocionales.parser;

import catalogospromocionales.model.Existencia;
import catalogospromocionales.model.Product;
import catalogospromocionales.utils.ParseHtmlTable;
import catalogospromocionales.utils.HtmlParser;
import webcrawler.IngenioTaggedHtml;
import webcrawler.TagSegment;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by gerardo on 12/05/16.
 */
public class ParseProduct extends HtmlParser {


    public static final String ENDPOINT_CATEGORIA = "promocionales/";
    public static final String ENDPOINT_PRODUCTOS = "/p/";
    public static final String URL = "http://www.catalogospromocionales.com";

    public static final String ENDPOINT_NEXT_PAGE = "/Catalogo/Default.aspx?id={1}&Page={2}";
    IngenioTaggedHtml pageProcessor = new IngenioTaggedHtml();

    public Product parseProduct(Product producto, ArrayList<String> cookies) {



        System.out.println();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println("Descargando producto:"+URL + producto.getPath() + " start:" +dateFormat.format(date)); //2014/08/06 15:59:48
        pageProcessor.getInternetPage(URL + producto.getPath(), cookies, false);
        date = new Date();
        System.out.println("fin dsacrga :"+dateFormat.format(date)); //2014/08/06 15:59:48
        for (int indexSegment = 0; indexSegment < pageProcessor.segmentList.size(); indexSegment++) {
            TagSegment segment = pageProcessor.segmentList.get(indexSegment);
//            print(segment);
            setImage(producto, pageProcessor.segmentList, indexSegment);
            setNombre(producto, pageProcessor.segmentList, indexSegment);
            setRef(producto, pageProcessor.segmentList, indexSegment);
            setDescription(producto, pageProcessor.segmentList, indexSegment);
            unidades(producto, pageProcessor.segmentList, indexSegment);
            empaques(producto, pageProcessor.segmentList, indexSegment);
            existencia(producto, pageProcessor.segmentList, indexSegment);
        }
        date = new Date();
        System.out.println("fin procesamiento :"+dateFormat.format(date)); //2014/08/06 15:59:48
        return producto;

    }


    public boolean DownloadImage(Product producto, String outputPath) throws IOException {


        File fd = new File(outputPath+"/"+producto.getId()+".jpg");
        if ( fd.exists() ) {
            return false;
        }

        File path;
        path = new File(outputPath);
        if ( !path.exists() ) {
            path.mkdirs();
        }



        URL url =new URL(producto.getUrlImage());
        byte array[] = new byte[1000];
        URLConnection urlCon = url.openConnection();
        InputStream is = urlCon.getInputStream();
        FileOutputStream fos = new FileOutputStream(fd);
        int read = is.read(array);
        while (read > 0) {
            fos.write(array, 0, read);
            read = is.read(array);
        }
        is.close();
        fos.close();

        return true;

    }


    private void setImage(Product producto, ArrayList<TagSegment> segmentList, int indexSegment) {
        TagSegment segment = segmentList.get(indexSegment);
        if (segment.insideTag && segment.getContent().contains("img-top")) {
            producto.setUrlImage(getParameter(segmentList.get(indexSegment + 3).getTagParameters(), "src").replace("'", "").replace("\"", ""));
        }
    }

    private void setNombre(Product producto, ArrayList<TagSegment> segmentList, int indexSegment) {
        TagSegment segment = segmentList.get(indexSegment);
        if (segment.insideTag && segment.getContent().contains("class=\"prodsCol2\"")) {
            producto.setNombreArticulo(segmentList.get(indexSegment + 2).getContent());
        }
    }

    private void setRef(Product producto, ArrayList<TagSegment> segmentList, int indexSegment) {
        TagSegment segment = segmentList.get(indexSegment);
        if (segment.insideTag && segment.getContent().contains("class=\"prodRef\"")) {
            producto.setReferencia(segmentList.get(indexSegment + 1).getContent());
        }
    }

    private void setDescription(Product producto, ArrayList<TagSegment> segmentList, int indexSegment) {
        TagSegment segment = segmentList.get(indexSegment);
        StringBuilder content = new StringBuilder();
        if (segment.insideTag && segment.getContent().contains("class=\"prodDescr\"")) {
            indexSegment = indexSegment + 1;
            do {
                segment = segmentList.get(indexSegment);
                if (!segment.insideTag) {
                    content.append(StringEscapeUtils.unescapeHtml3(segment.getContent()));
                }
                indexSegment++;
            } while (!segment.getContent().equals("</p>") && indexSegment < segmentList.size());

            producto.setDescripcion(content.toString());

        }
    }

    private void unidades(Product producto, ArrayList<TagSegment> segmentList, int indexSegment) {
        ArrayList<ArrayList<String>> table;
        TagSegment segment = segmentList.get(indexSegment);
        if (segment.insideTag && segment.getTagName().equals(START_TABLE) && segment.getContent().contains("table-list")) {
            table = ParseHtmlTable.processTable(segmentList, indexSegment);
            if (table.size() > 0 && table.get(0).size() > 0)
                producto.setUnidaes(table.get(0).get(1));
        }

    }

    private void empaques(Product producto, ArrayList<TagSegment> segmentList, int indexSegment) {
        //table-list
        ArrayList<ArrayList<String>> table;
        TagSegment segment = segmentList.get(indexSegment);
        if (segment.insideTag && segment.getTagName().equals(START_TABLE) && segment.getContent().contains("table-list")) {
            table = ParseHtmlTable.processTable(segmentList, indexSegment);
            if (table.size() > 1 && table.get(1).size() > 0) {
                producto.setEmpaque(table.get(1).get(1));
            }
        }

    }


    private void existencia(Product producto, ArrayList<TagSegment> segmentList, int indexSegment) {
        ArrayList<ArrayList<String>> tableExistencia;
        TagSegment segment = segmentList.get(indexSegment);
        if (segment.insideTag && segment.getTagName().equals(START_TABLE) && segment.getContent().contains("tableInfoProd")) {
            tableExistencia = ParseHtmlTable.processTable(segmentList, indexSegment);
            if (tableExistencia.size() > 1) {

                if (producto.getExistenca() == null)
                    producto.setExistenca(new ArrayList<Existencia>());

                for (int index = 1; index < tableExistencia.size(); index++) {
                    producto.getExistenca().add(new Existencia(tableExistencia.get(index).get(0), Integer.parseInt(tableExistencia.get(index).get(1))));
                }
                producto.setEmpaque(tableExistencia.get(1).get(1));
            }
        }

    }


    public static void main(String[] args) throws IOException {

        Product product = new Product("1731","Silla Plegable con Brazos","/p/silla-plegable-con-brazos/1731/395","395");
        ParseProduct parseProduct = new ParseProduct();
        ArrayList<String> cookies = HtmlParser.getCookie();
        parseProduct.parseProduct(product, cookies);
        System.out.println("img:" + product.getUrlImage());
        System.out.println(product);
        for (Existencia existencia : product.getExistenca()) {
            System.out.println(existencia);
        }

        System.out.println(parseProduct.DownloadImage(product,"./output/images/"));
    }




}
