package catalogospromocionales.utils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import webcrawler.IngenioTaggedHtml;
import webcrawler.TagSegment;

import java.util.ArrayList;

/**
 * Created by gerardo on 13/05/16.
 */
public class ParseHtmlTable extends HtmlParser {

    /**
     *   Parse los datos de una tabla
     * @param segmentList
     * @param indexSegment
     * @return
     */
    public static ArrayList<ArrayList<String>> processTable(ArrayList<TagSegment> segmentList, int indexSegment) {
        ArrayList<ArrayList<String>>  content = new  ArrayList<ArrayList<String>>();
        TagSegment segment;
         for(int count = indexSegment;count<segmentList.size(); count++) {
             segment = segmentList.get(count);
             if(segment.insideTag && segment.getTagName().contains(END_TABLE) || segment.getTagName().contains(END_TABLE_BODY))
                 break;
             Pair<Integer,ArrayList<String>> result = parseContentTR(segmentList,count);
             content.add(result.getRight());
             count = result.getLeft();
         }
        return content;
    }


    /**
     *  Procesa el conteido del td de una tabla
     * @param segmentList
     * @param indexSegment
     * @param startTag
     * @param endTag
     * @return
     */
    public static ImmutablePair<Integer,String> parseContentTAG(ArrayList<TagSegment> segmentList, int indexSegment, String startTag ,String endTag) {
        TagSegment tdEnd;
        StringBuilder content = new StringBuilder();

        // buscar el tag inicial
        int count;
        for(count = indexSegment;count < segmentList.size(); count++){
            tdEnd = segmentList.get(count);
            if(tdEnd.insideTag && tdEnd.getTagName().equals(startTag))
            {
                break;
            }
        }

        // procesa el contenido mientas busca el tag final
        for(count= count+1;count < segmentList.size(); count++){
            tdEnd = segmentList.get(count);
            if(tdEnd.insideTag && tdEnd.getTagName().equals(endTag))
                break;
            if(!tdEnd.insideTag)  // solo texto
                content.append(" "+StringEscapeUtils.unescapeHtml4(tdEnd.getContent()));
        }
        return new ImmutablePair(count,content.toString().trim());

    }


    /**
     * Procesa los td que contiene el Tr de una tabla
     * @param segmentList
     * @param indexSegment
     * @return
     */
    public static ImmutablePair<Integer,ArrayList<String>> parseContentTR(ArrayList<TagSegment> segmentList, int indexSegment) {

        TagSegment tdEnd;
        ArrayList<String> content= new ArrayList<String>();
        int count;
        // busca el inicio de tr
        for(count = indexSegment;count < segmentList.size(); count++){
            tdEnd = segmentList.get(count);
            if(tdEnd.insideTag && tdEnd.getTagName().equals(START_TR))
            {
                break;
            }
        }

        // procesa los td dentro de los tr. busca el finl del tr
        for(count=count+1;count < segmentList.size(); count++){
            tdEnd = segmentList.get(count);
            if(tdEnd.insideTag && tdEnd.getTagName().equals(END_TR))
            {
                break;
            }
            ImmutablePair<Integer,String> result = parseContentTAG(segmentList,count,START_TD,END_TD);
            content.add(result.getRight());
            count= result.getLeft();
        }

        return new ImmutablePair(count,content);
    }


    public static void main(String[] args) {

        IngenioTaggedHtml pageProcessor = new IngenioTaggedHtml();

//        ParseHtmlTable proceess = new ParseHtmlTable();
        ArrayList<String> cookies = ParseHtmlTable.getCookie();
//        pageProcessor.getInternetPage("http://www.catalogospromocionales.com/p/mini-set-de-fondue-rhombus/6779/115", cookies, false);
//        pageProcessor.getInternetPage("http://www.catalogospromocionales.com/p/set-columbus/6083/297", cookies, false);
        pageProcessor.getInternetPage("http://www.catalogospromocionales.com/p/cobija-en-fleece-softy/3610/303", cookies, false);

        ArrayList<ArrayList<String>> tableExistencia=null;

        for (int indexSegment = 0; indexSegment < pageProcessor.segmentList.size(); indexSegment++) {
            TagSegment segment = pageProcessor.segmentList.get(indexSegment);

            if( segment.insideTag &&  segment.getTagName().equals(START_TABLE) && segment.getContent().contains("tableInfoProd"))
            {
                tableExistencia = ParseHtmlTable.processTable(pageProcessor.segmentList,indexSegment);
            }

            if( segment.insideTag &&  segment.getTagName().equals(START_TABLE) && segment.getContent().contains("table-list"))
            {
                ParseHtmlTable.processTable(pageProcessor.segmentList,indexSegment);
            }

        }


        System.out.println(tableExistencia.size()+"Numero de filas");
        System.out.println(tableExistencia.get(0).get(3) +" : "  + tableExistencia.get(1).get(3));

    }


}





