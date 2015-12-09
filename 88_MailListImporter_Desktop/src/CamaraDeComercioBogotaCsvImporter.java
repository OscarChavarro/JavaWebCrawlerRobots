import databaseMongo.DatabaseConnection80mil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.StringTokenizer;

import vsdk.toolkit.io.PersistenceElement;

import databaseMongo.model.CamaraDeComercioData;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.TreeSet;

/**
*/
public class CamaraDeComercioBogotaCsvImporter {
    private static final String path = 
        "/Users/oscar/usr/Abako/BodegasDeDatos/camaraDeComercioDeBogota/csv/";

    private static int traverseFilesAndProcess(
        String path, int n, ArrayList<CamaraDeComercioData> dataset)
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
                    traverseFilesAndProcess(fi.getAbsolutePath(), n, dataset);
            }
            else if ( fi.isFile() ) {
                counter++;
                processFile(fi, n, counter, dataset);
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

    private static void processFile(
        File fi, int n, int i, ArrayList<CamaraDeComercioData> dataset) 
        throws Exception
    {
        System.out.println("  - [" + i + "/" + n + "]: " + fi.getName());
        
        FileInputStream fis;
        BufferedInputStream bis;
        
        fis = new FileInputStream(fi);
        bis = new BufferedInputStream(fis);
        int lineCount = 0;
        
        while ( bis.available() > 0 ) {
            String line = PersistenceElement.readAsciiLine(bis);
            processLine(line, dataset);
            lineCount++;
            
            if ( lineCount % 10000 == 0 ) {
                System.out.println("  - Processing line " + lineCount);
            }
        }
        
        System.out.println("    . " + lineCount + " lines");
        
    }

    private static void processLine(
        String line, 
        ArrayList<CamaraDeComercioData> dataset) 
    {
        int i;
        int counter = 0;
        
        for ( i = 0; i < line.length(); i++ ) {
            char c = line.charAt(i);
            if ( c == ',' ) {
                counter++;
            }
        }

        if ( counter != 20  ) {
            System.out.println("  - String: [" + line + "]");
            System.out.println("    . Commas: " + counter);
            System.exit(1);
        }
        
        StringTokenizer parser = new StringTokenizer(line, ",");
        CamaraDeComercioData c = new CamaraDeComercioData();
        for ( i = 1; parser.hasMoreTokens(); i++ ) {
            String token = parser.nextToken();
            switch ( i ) {
              case 1:
		c.setCdcbCompanyRegistrationNumber(token);
                break;
              case 2:
		c.setCdcbCompanyName(token);
                break;
              case 3:
		c.setCdcbCompanyRegistrationUpdateYear(token);
                break;
              case 4:
		c.setCdcbCompanyAddress(token);
                break;
              case 5:
		c.setCdcbCompanyPhone1(token);
                break;
              case 6:
		c.setCdcbCompanyPhone2(token);
                break;
              case 7:
		c.setCdcbLegalRepresentativeEmail(token);
                break;
              case 8:
		c.setCdcbCompanyGeographicRegionLevel6(token);
                break;
              case 9:
		c.setCdcbCompanyMoneyCapital(token);
                break;
              case 10:
		c.setCdcbCompanySizeType(token);
                break;
              case 11:
		c.setCdcbCompanySizeNumberOfEmployees(token);
                break;
              case 12:
		c.setCdcbCompanyNitNumber(token);
                break;
              case 13:
		c.setCdcbCompanyCiiuNumber(token);
                break;
              case 14:
		c.setCdcbEconomicActivity(token);
                break;
              case 15:
		c.setCdcbLegalRepresentativeName(token);
                break;
              case 16:
		c.setCdcbCompanyGeographicOfficeSite(token);
                break;
              case 17:
		c.setCdcbCompanyAffiliateStatus(token);
                break;
              case 18:
		c.setCdcbCompanyType(token);
                break;
              case 19:
		c.setCdcbCompanyGeographicRegionLevel8(token);
                break;
              case 20:
		c.setCdcbCompanyGeographicRegionLevel10(token);
                break;
              case 21:
		c.setCdcbCompanyChapter11Status(token);
		// Y acá se inserta
                dataset.add(c);
		break;
	      default:
                System.out.println("ERROR: Dato erroneo");
                System.exit(1);
		break;
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
        //ContactData c;
        //c = new ContactData();
        //c.setEmail(email);
        //DatabaseConnection80mil.insertContactMongo(c);
    }
    
    private static void postProcessDataset(
        ArrayList<CamaraDeComercioData> dataset) 
    {
        long n = dataset.size();
        System.out.println("Processing " + n + " elements:"); 
        int i;

        TreeSet<String> economicActivities;
        economicActivities = new TreeSet<String>();

        TreeSet<String> ciiuCodes;
        ciiuCodes = new TreeSet<String>();
        
        // Build intermediate data structures
        for ( i = 1; i < n; i++ ) {
            // 0 index is not included since this is the header label
            String s = dataset.get(i).getCdcbEconomicActivity();
            if ( !economicActivities.contains(s) ) {
                economicActivities.add(s);
            }
            s = dataset.get(i).getCdcbCompanyCiiuNumber();
            if ( !ciiuCodes.contains(s) ) {
                ciiuCodes.add(s);
            }
            
            if ( i % 10000 == 0 ) {
                System.out.println("  * Building intermediate tables: " +
                    i + " / " + n);
            }
        }

        reportSet(economicActivities, "Economic activities", "./output/economicActivities.txt");
        reportSet(ciiuCodes, "Ciiu codes", "./output/ciiuCodes.txt");
        
        // Export to mongo
        for ( i = 1; i < n; i++ ) {
            // 0 index is not included since this is the header label
            DatabaseConnection80mil.insertCompanyContactMongo(dataset.get(i));
            if ( i % 10000 == 0 ) {
                System.out.println("  * Inserting data to mongo database: " +
                    i + " / " + n);
            }
        }
    }

    private static void reportSet(TreeSet<String> ss, String title, String filename) {
        try {
            File fd = new File(filename);
            FileOutputStream fos = new FileOutputStream(fd);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            
            System.out.println(title + " found: " + ss.size());
            for ( String ea : ss ) {
                //System.out.println("  - " + ea);
                PersistenceElement.writeAsciiLine(bos, ea);
            }
            bos.close();
        }
        catch ( Exception e ) {
            
        }
    }

    public static void main(String args[])
    {
        try {
            int n = traverseFilesAndCount(path);
            ArrayList<CamaraDeComercioData> dataset;
            dataset = new ArrayList<CamaraDeComercioData>(400000);
            traverseFilesAndProcess(path, n, dataset);
            
            postProcessDataset(dataset);
            
        }
        catch ( Exception e ) {
            System.out.println("ERROR!");
        }
    }

}
