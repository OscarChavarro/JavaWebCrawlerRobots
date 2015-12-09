//===========================================================================

package webcrawler;

// Java classes
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class TagSegment
{
    public boolean insideTag;
    public String content;

    public TagSegment()
    {
        insideTag = false;
        content = "";
    }

    public TagSegment(TagSegment other)
    {
        this.insideTag = other.insideTag;
        this.content = other.content;
    }

    public void append(byte b)
    {
        byte ab[] = new byte[1];
        ab[0] = b;
        content += new String(ab, Charset.forName("ISO-8859-1"));
    }

    public String getTagName()
    {
        if ( insideTag == false ) {
            return null;
        }
        String copy = content;
        StringTokenizer auxStringTokenizer;
        auxStringTokenizer = new StringTokenizer(copy, "<>\t\n\r ");

        String tagName = auxStringTokenizer.nextToken();

        if ( tagName.length() > 0 && tagName.charAt(0) == '!' ) {
            tagName = "!COMMENT";
        }
        tagName = tagName.toUpperCase();
        return tagName;
    }

    /**
    Returns null if parameter does not exist, "" (empty string) if parameter
    exists but has no value set.  Otherwise return the parameter value for
    the given parameter name.
    @param parameterName
    @return 
    */
    public String getTagParameterValue(String parameterName)
    {
        ArrayList<TagParameter> params;
        int i;
        TagParameter p;
        String upperParameter, upperSearch;

        upperParameter = parameterName.toUpperCase();
        params = getTagParameters();
        for ( i = 0; i < params.size(); i++ ) {
            p = params.get(i);
            upperSearch = p.name.toUpperCase();
            if ( upperSearch.equals(upperParameter) ) {
                return p.value;
            }
        }
        return null;
    }

    public ArrayList<TagParameter> getTagParameters()
    {
        ArrayList<TagParameter> list = new ArrayList<TagParameter>();
        TagParameter novo = null;

        int i;
        char c;
        boolean rompiendo = true;
        String acum = "";
        int situation = 1; // 1: name, 2: value
        int prevSituation = 1; // 1: name, 2: value


        for ( i = 0; i < content.length(); i++ ) {
            c = content.charAt(i);
            if ( c == '\"' ) {
                rompiendo = !rompiendo;
            }

            if ( rompiendo && 
                ( c == ' ' || c == '\t' || c == '=' || 
                  c == '<' || c == '>' ) ) {

                if ( c == '=' ) {
                    situation = 2;
                }

                if ( acum.length() <= 0 ) continue;

                if ( prevSituation == 1 ) {
                    novo = new TagParameter();
                    novo.name = acum;
                    novo.value = "";
                    list.add(novo);
                }
                else {
                    novo.value = acum;
                }

                acum = "";
                prevSituation = situation;
                situation = 1;
            }
            else {
                acum += c;
            }
        }

        if ( acum.length() > 0 ) {
            if ( prevSituation == 1 ) {
                novo = new TagParameter();
                novo.name = acum;
                novo.value = "";
                list.add(novo);
            }
            else {
                novo.value = acum;
            }
        }
        return list;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
