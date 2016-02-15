//===========================================================================

package webcrawler;

// Java classes
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ComputrabajoTagSegment
{
    public boolean insideTag;
    private String content;
    ArrayList<Byte> contentv;

    public ComputrabajoTagSegment()
    {
        insideTag = false;
        content = "";
        contentv = new ArrayList<Byte>();
    }

    public ComputrabajoTagSegment(ComputrabajoTagSegment other)
    {
        this.insideTag = other.insideTag;
        this.content = other.content;
    }

    public void append(byte b)
    {
        byte ab[] = new byte[1];
        ab[0] = b;
        content += new String(ab, Charset.forName("UTF-8"));
        contentv.add(b);
    }

    public String getTagName()
    {
        if ( insideTag == false ) {
            return null;
        }
        String copy = content;
        StringTokenizer auxStringTokenizer;
        auxStringTokenizer = new StringTokenizer(copy, "<>\t\n\r ");

        try {
            String tagName = auxStringTokenizer.nextToken();

            if ( tagName.length() > 0 && tagName.charAt(0) == '!' ) {
                tagName = "!COMMENT";
            }
            tagName = tagName.toUpperCase();
            return tagName;
        }
        catch ( Exception e ) { 
            System.out.println("ERROR:ACannot parse " + copy);
            return "";
        }

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
        ArrayList<ComputrabajoTagParameter> params;
        int i;
        ComputrabajoTagParameter p;
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

    /**
    PRE: content String is previously loaded
    @return 
    */
    public ArrayList<ComputrabajoTagParameter> getTagParameters()
    {
        ArrayList<ComputrabajoTagParameter> list = new ArrayList<ComputrabajoTagParameter>();
        ComputrabajoTagParameter novo = null;

        int i;
        char c;
        boolean rompiendo = true;
        String acum = "";
        
        int situation = 1; // 1: name, 2: value
        int prevSituation = 1; // 1: name, 2: value
        
        String contentm = getContent();
        for ( i = 0; i < contentm.length(); i++ ) {
            c = contentm.charAt(i);
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
                    novo = new ComputrabajoTagParameter();
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
                novo = new ComputrabajoTagParameter();
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

    public String getContent() {
        int i;
        byte arr[] = new byte [contentv.size()];
        
        for ( i = 0; i < contentv.size(); i++ ) {
            arr[i] = contentv.get(i);
        }
        return new String(arr, Charset.forName("UTF-8"));
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
