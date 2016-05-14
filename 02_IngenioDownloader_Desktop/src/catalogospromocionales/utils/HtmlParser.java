package catalogospromocionales.utils;

import webcrawler.IngenioTaggedHtml;
import webcrawler.TagParameter;
import webcrawler.TagSegment;

import java.util.ArrayList;

/**
 * Created by sarah on 08/05/16.
 */
public class HtmlParser {

    public static final String HREF ="href";
    public static String URL_COOKIE = "http://www.catalogospromocionales.com/Catalogo/Default.aspx";
    public static final String START_TR ="TR";
    public static final String END_TR ="/TR";

    public static final String START_TD ="TD";
    public static final String END_TD ="/TD";

    public static final String START_TABLE ="TABLE";
    public static final String END_TABLE ="/TABLE";
    public static final String END_TABLE_BODY ="/TBODY";



    public   void print (TagSegment segment){
//        if(segment.insideTag){
//        System.out.println("----------------------------");
        System.out.println(segment.getContent());
        System.out.println(segment.getTagName());
        System.out.println("-----------------");
//             System.out.println(segment.insideTag);
//        System.out.println(segment.getTagParameters().size());
//            segment.getTagParameters().forEach(parameter -> ProcessProducts.print(parameter));

//        }
    }


    public  void print(TagParameter parameter){
        System.out.println(parameter.name + ":" + parameter.value);
    }

    public  String getParameter(ArrayList<TagParameter> params, String paramName){
        for (TagParameter parameter:params) {
//            System.out.println(parameter.name+":"+parameter.value);
            if(parameter.name.equals(paramName)){
                return parameter.value;
            }
        }
        return null;
    }

    public  boolean isEndPoint(TagSegment segment, String enpoint){
        if(!segment.insideTag) return false;
        if(!segment.getContent().contains(HREF)) return false;
        if(!segment.getContent().contains(enpoint)) return false;
        return true;
    }

    public  String getContent(int index,ArrayList<TagSegment> list){
        if(list.get(index+1).insideTag) return "";
        return list.get(index+1).getContent().trim().replace("&nbsp;", " ");
    }

    public static ArrayList<String>  getCookie() {
        ArrayList<String> cookies = new ArrayList<String>();
        IngenioTaggedHtml pageProcessor = new IngenioTaggedHtml();
        pageProcessor.getInternetPage(URL_COOKIE, cookies, false);
        return cookies;
    }

    public static ArrayList<String> getCookie(String cookieUrl) {
        ArrayList<String> cookies = new ArrayList<String>();
        IngenioTaggedHtml pageProcessor = new IngenioTaggedHtml();
        pageProcessor.getInternetPage(cookieUrl, cookies, false);
        return cookies;
    }

}
