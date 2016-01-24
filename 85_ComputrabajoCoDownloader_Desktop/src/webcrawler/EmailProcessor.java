package webcrawler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.mongodb.DBObject;

import vsdk.toolkit.io.PersistenceElement;

import databaseMongo.model.EmailElement;

public class EmailProcessor 
{
    public static void reportEmailElements(
        HashMap<String, EmailElement> elements
    )
    {
	Collection<EmailElement> s = elements.values();
	System.out.println("- ENCOUNTERED EMAIL ELEMENTS: " +
	   elements.size());

	ArrayList<EmailElement> sorted;
	sorted = new ArrayList<EmailElement>();
	for ( EmailElement ee : s ) {
	    sorted.add(ee);
	}
	Collections.sort(sorted);
	int i;
        try {
            File fd = new File("./output/reports/emailDomains.csv");
            FileOutputStream fos;
	    fos = new FileOutputStream(fd);
            BufferedOutputStream bos;
            bos = new BufferedOutputStream(fos);
            for (i = 0; i < sorted.size(); i++) {
                String l;
                EmailElement n = sorted.get(i);
		String v;

		v = n.getValid() ? "VALID" : "INVALID";
		
                l = v + "\t" + n.getDomainName() + "\t" + n.getCount();
                PersistenceElement.writeAsciiLine(bos, l);
            }
            bos.close();
            fos.close();
        } 
        catch (Exception ex) {
        }
    }
    
    public static String getDomainFromEmail(String email)
    {
	String l = email.toLowerCase();
        if ( !l.contains("@") ) {
	    return null;
	}

	StringTokenizer parser = new StringTokenizer(l, "@");
	String token;
	token = parser.nextToken();
	token = parser.nextToken();

	if ( token == null || token.length() < 1 ) {
	    return null;
	}
	return token;
    }

    public static void processEmail(
        DBObject o,
	int index,
	int n,
	HashMap<String, EmailElement> elements,
	boolean reportAdvances)
    {
	if ( o == null ) {
	    return;
	}
	if ( o.get("email") == null ) {
	    return;
	}
        String email = o.get("email").toString();

        if ( reportAdvances ) {
            System.out.println("    . Emails: [" + email + "]");
        }

        String token = getDomainFromEmail(email);
	if ( token == null ) {
	    return;
	}

	EmailElement ee;
        if ( elements.containsKey(token) ) {
	    ee = elements.get(token);
	    ee.setCount(ee.getCount() + 1);
	}
	else {
	    ee = new EmailElement();
	    ee.setCount(1);
	    ee.setDomainName(token);
	    ee.computeValidity();
	    elements.put(token, ee);
	}
    }
}
