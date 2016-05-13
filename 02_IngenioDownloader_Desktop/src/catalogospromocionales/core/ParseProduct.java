package catalogospromocionales.core;

import catalogospromocionales.model.Product;
import catalogospromocionales.utils.htmlparser;
import webcrawler.IngenioTaggedHtml;
import webcrawler.TagSegment;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gerardo on 12/05/16.
 */
public class ParseProduct extends htmlparser {


    public void parseProduct(Product producto, ArrayList<String> cookies) {

        System.out.println("asdasdas");
        IngenioTaggedHtml pageProcessor = new IngenioTaggedHtml();
        HashMap<String, Product> products = new HashMap<>();
        pageProcessor.getInternetPage("http://www.catalogospromocionales.com/p/mini-set-de-fondue-rhombus/6779/115", cookies, false);
        for (int indexSegment = 0; indexSegment < pageProcessor.segmentList.size(); indexSegment++) {
            TagSegment segment = pageProcessor.segmentList.get(indexSegment);
//            print(segment);

            setImage(producto, pageProcessor.segmentList, indexSegment);
            setNombre(producto, pageProcessor.segmentList, indexSegment);
            setRef(producto, pageProcessor.segmentList, indexSegment);
            setDescription(producto, pageProcessor.segmentList, indexSegment);
        }
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
            System.out.println(segmentList.get(indexSegment + 2).getContent());
            producto.setNombreArticulo(segmentList.get(indexSegment + 2).getContent());
        }
    }

    private void setRef(Product producto, ArrayList<TagSegment> segmentList, int indexSegment) {
        TagSegment segment = segmentList.get(indexSegment);
        if (segment.insideTag && segment.getContent().contains("class=\"prodRef\"")) {
            System.out.println(segmentList.get(indexSegment + 1).getContent());
            producto.setReferencia(segmentList.get(indexSegment + 1).getContent());
        }
    }

    private void setDescription(Product producto, ArrayList<TagSegment> segmentList, int indexSegment) {
        TagSegment segment = segmentList.get(indexSegment);
        StringBuilder content = new StringBuilder();
        if (segment.insideTag && segment.getContent().contains("class=\"prodDescr\"")) {
//            System.out.println(segmentList.get(indexSegment + 1).getContent());
//            producto.setReferencia(segmentList.get(indexSegment + 1).getContent());
            indexSegment= indexSegment+1;

            do{

                segment = segmentList.get(indexSegment);
                if(!segment.insideTag) {
                    content.append(StringEscapeUtils.unescapeHtml3(segment.getContent()));
                }
                indexSegment++;
            }while (!segment.getContent().equals("</p>") && indexSegment<segmentList.size());

            producto.setReferencia(content.toString());
            System.out.println(content);

        }
    }


    public static void main(String[] args) {

        Product product = new Product();
        ParseProduct parseProduct = new ParseProduct();
        ArrayList<String> cookies = parseProduct.getCookie();
        parseProduct.parseProduct(product, cookies);
        System.out.println("img:"+product.getUrlImage());

    }


}
