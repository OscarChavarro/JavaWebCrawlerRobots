
// Java basic classes
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.StringTokenizer;

// VSDK classes
import vsdk.toolkit.io.PersistenceElement;

// Application specific classes
import databaseMongo.DatabaseConnection80mil;
import databaseMongo.model.ContactData;

/**
*/
public class EmailListCsvImporter {
    private static final String path = 
        "/Users/oscar/usr/Abako/BodegasDeDatos/listadosDeCorreos/csv_total/extendidos/enBogota";

    /**
    @param directoryPath
    @param numberOfFilesToProcess
    @return
    @throws Exception 
    */
    private static int traverseFilesAndProcess(
        String directoryPath, int numberOfFilesToProcess)
        throws Exception 
    {
        int fileCounter = 0;
        File fd;
        fd = new File(directoryPath);
        if ( !fd.exists() ) {
            System.out.println("ERROR: Folder not found: " + directoryPath);
            return 0;
        }
        if ( !fd.isDirectory() ) {
            System.out.println("Path " + directoryPath + " is not a folder");
            return 0;
        }
        
        File children[] = fd.listFiles();
        int i;
        
        for ( i = 0; i < children.length; i++ ) {
            File fi = children[i];
            if ( fi.isDirectory() ) {
                fileCounter +=
                    traverseFilesAndProcess(fi.getAbsolutePath(), numberOfFilesToProcess);
            }
            else if ( fi.isFile() ) {
                fileCounter++;
                processFile(fi, numberOfFilesToProcess, fileCounter);
            }
        }   
        return fileCounter;
    }

    /**
    @param directoryPath
    @return
    @throws Exception 
    */
    private static int traverseFilesAndCountFiles(String directoryPath) 
        throws Exception 
    {
        File fd;
        int fileCounter = 0;
        fd = new File(directoryPath);
        if ( !fd.exists() ) {
            System.out.println("ERROR: Folder not found: " + directoryPath);
            return 0;
        }
        if ( !fd.isDirectory() ) {
            System.out.println("Path " + directoryPath + " is not a folder");
            return 0;
        }
        
        File children[] = fd.listFiles();
        int i;
        
        for ( i = 0; i < children.length; i++ ) {
            File fi = children[i];
            if ( fi.isDirectory() ) {
                fileCounter += traverseFilesAndCountFiles(fi.getAbsolutePath());
            }
            else if ( fi.isFile() ) {
                fileCounter++;
            }
        }   
        return fileCounter;
    }

    /**
    @param fi
    @param n
    @param i
    @throws Exception 
    */
    private static void processFile(File fi, int n, int i) throws Exception
    {
        System.out.println("  - [" + i + "/" + n + "]: " + fi.getName());

        FileInputStream fis;
        BufferedInputStream bis;
        
        fis = new FileInputStream(fi);
        bis = new BufferedInputStream(fis);
        int lineCounter = 0;

        while ( bis.available() > 0 ) {
            String line = PersistenceElement.readAsciiLine(bis);
            processLine(line);
            lineCounter++;
        }

        System.out.println("    . " + lineCounter + " lines");
    }

    /**
    @param line 
    */
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

    /**
    @param token
    @return 
    */
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

    /**
    @param email
    */
    private static void addToken(String email) {
        ContactData c;
        c = new ContactData();
        c.setEmail(email);
        DatabaseConnection80mil.insertContactMongo(c);
    }

    /**
    @param args 
    */
    public static void main(String args[])
    {
        try {
            int numberOfFiles = traverseFilesAndCountFiles(path);
            traverseFilesAndProcess(path, numberOfFiles);
        }
        catch ( Exception e ) {
            System.out.println("ERROR!");
        }
    }
}
