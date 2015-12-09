import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.StringTokenizer;

import vsdk.toolkit.io.PersistenceElement;

import databaseMongo.DatabaseConnection80mil;
import databaseMongo.model.ContactData;

/**
*/
public class EmailListCsvImporter {
    private static final String path = "/Users/oscar/usr/Abako/BodegasDeDatos/listadosDeCorreos/csv_total/";
    public static void main(String args[])
    {
        try {
            int n = traverseFilesAndCount(path);
            traverseFilesAndProcess(path, n);
        }
        catch ( Exception e ) {
            System.out.println("ERROR!");
        }
    }

    private static int traverseFilesAndProcess(String path, int n)
        throws Exception 
    {
        int counter = 0;
        File fd;
        fd = new File(path);
        if ( !fd.exists() ) {
            System.out.println("ERROR: Folder not found: " + path);
            return 0;
        }
        if ( !fd.isDirectory() ) {
            System.out.println("Path " + path + " is not a folder");
            return 0;
        }
        
        File children[] = fd.listFiles();
        int i;
        
        for ( i = 0; i < children.length; i++ ) {
            File fi = children[i];
            if ( fi.isDirectory() ) {
                counter +=
                    traverseFilesAndProcess(fi.getAbsolutePath(), n);
            }
            else if ( fi.isFile() ) {
                counter++;
                processFile(fi, n, counter);
            }
        }   
        return counter;
    }

    private static int traverseFilesAndCount(String path) throws Exception {
        File fd;
        int count = 0;
        fd = new File(path);
        if ( !fd.exists() ) {
            System.out.println("ERROR: Folder not found: " + path);
            return 0;
        }
        if ( !fd.isDirectory() ) {
            System.out.println("Path " + path + " is not a folder");
            return 0;
        }
        
        File children[] = fd.listFiles();
        int i;
        
        for ( i = 0; i < children.length; i++ ) {
            File fi = children[i];
            if ( fi.isDirectory() ) {
                count += traverseFilesAndCount(fi.getAbsolutePath());
            }
            else if ( fi.isFile() ) {
                count++;
            }
        }   
        return count;
    }

    private static void processFile(File fi, int n, int i) throws Exception
    {
        System.out.println("  - [" + i + "/" + n + "]: " + fi.getName());
        
        FileInputStream fis;
        BufferedInputStream bis;
        
        fis = new FileInputStream(fi);
        bis = new BufferedInputStream(fis);
        int lineCount = 0;
        
        while ( bis.available() > 0 ) {
            String line = PersistenceElement.readAsciiLine(bis);
            processLine(line);
            lineCount++;
        }
        
        System.out.println("    . " + lineCount + " lines");
        
    }

    private static void processLine(String line) {
        StringTokenizer parser;
        parser = new StringTokenizer(line, ", ");
        int i;
        
        for ( i = 0; parser.hasMoreTokens(); i++ ) {
            String token = parser.nextToken();
            if ( isMail(token) ) {
                addToken(token);
            }
        }
    }

    private static boolean isMail(String token) {
        if ( token == null || token.length() <= 0 ) {
            return false;
        }
        int n = token.length();                
        int i;
        char c;
        int numberOfAts = 0;
        for ( i = 0; i < n; i++ ) {
            c = token.charAt(i);
            if ( c == '@' ) {
                numberOfAts++;
            }
        }
        if ( numberOfAts == 0 ) {
            return false;
        }
        else if ( numberOfAts != 1 ) {
            //System.out.println("  * Invalid email (more than one @): " + token);
            return false;
        }

        StringTokenizer parser = new StringTokenizer(token, "@");
        
        if ( !parser.hasMoreTokens() ) {
            //System.out.println("  * Invalid email (empty user): " + token);
            return false;
        }

        String user = parser.nextToken();
        
        if ( user == null || user.length() <= 0 ) {
            //System.out.println("  * Invalid email (empty user): " + token);
            return false;
        }

        if ( !parser.hasMoreTokens() ) {
            //System.out.println("  * Invalid email (empty domain): " + token);
            return false;
        }
        
        String domain = parser.nextToken();

        if ( domain == null || domain.length() <= 0 ) {
            //System.out.println("  * Invalid email (empty domain): " + token);
            return false;
        }

        int m = domain.length();
        if ( domain.charAt(0) == '.' ) {
            //System.out.println("  * Invalid email (bad domain): " + token);
            return false;            
        }

        if ( domain.charAt(m-1) == '.' ) {
            String retry = domain.substring(0, m-1);
            return isMail(retry);
        }

        if ( !domain.contains(".") ) {
            //System.out.println("  * Invalid email (bad domain): " + token);
            return false;
        }

        return true;
    }

    private static void addToken(String email) {
        ContactData c;
        c = new ContactData();
        c.setEmail(email);
        DatabaseConnection80mil.insertContactMongo(c);
    }
}
