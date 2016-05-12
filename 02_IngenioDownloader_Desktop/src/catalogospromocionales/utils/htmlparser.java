package catalogospromocionales.utils;

import webcrawler.TagParameter;
import webcrawler.TagSegment;

import java.util.ArrayList;

/**
 * Created by sarah on 08/05/16.
 */
public class htmlparser {

    public static final String HREF ="href";

    public static  void print (TagSegment segment){
//        if(segment.insideTag){
//        System.out.println("----------------------------");
        System.out.println(segment.getContent());
//             System.out.println(segment.insideTag);
//        System.out.println(segment.getTagParameters().size());
//            segment.getTagParameters().forEach(parameter -> ProcessProducts.print(parameter));

//        }
    }


    public static void print(TagParameter parameter){
        System.out.println(parameter.name + ":" + parameter.value);
    }

    public static String getParameter(ArrayList<TagParameter> params, String paramName){
        for (TagParameter parameter:params) {
//            System.out.println(parameter.name+":"+parameter.value);
            if(parameter.name.equals(paramName)){
                return parameter.value;
            }
        }
        return null;
    }

    public static boolean isEndPoint(TagSegment segment, String enpoint){
        if(!segment.insideTag) return false;
        if(!segment.getContent().contains(HREF)) return false;
        if(!segment.getContent().contains(enpoint)) return false;
        return true;
    }

    public static String getContent(int index,ArrayList<TagSegment> list){
        if(list.get(index+1).insideTag) return "";
        return list.get(index+1).getContent().trim().replace("&nbsp;", " ");
    }


}
