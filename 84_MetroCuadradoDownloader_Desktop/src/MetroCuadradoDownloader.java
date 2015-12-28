//===========================================================================

// Java basic classes
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

// VSDK classes
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.io.PersistenceElement;

// Utilities classes
import databaseConnection.*;
import databaseMongo.model.Property;

// Toolkit classes
import webcrawler.TagSegment;
import webcrawler.TaggedHtml;

// Application specific classes
//import databaseMysqlMongo.MetroCuadradoDatabaseConnection;
//import databaseMysqlMongo.model.Property;

/**
*/
public class MetroCuadradoDownloader {
//    private static final MetroCuadradoDatabaseConnection databaseConnection;
	private static final DatabaseMongoConnection databaseConnection;

    static {
        databaseConnection = new DatabaseMongoConnection();
        DatabaseMongoConnection.createMongoConnection("localhost" , 27017, "domolyRobot", "landPropertyInSale_test");
    }

    /**
    @param page
    */
    private static void analyzeIndexPage(String page, String cityName, String cacheFilename)
    {
        HashMap<String, String> sites;
        sites = new HashMap<String, String>();

        downloadSitesFromIndexPage(page, sites, cityName);

        Set s;
        s = sites.keySet();

        // Export!
        try {
            File fd = new File(cacheFilename);
            FileOutputStream fos;
            fos = new FileOutputStream(fd);
            System.out.println("INMUEBLES: " + sites.size());
            for ( Object e : s ) {
                System.out.println("  - " + e.toString());
                String line = TaggedHtml.trimQuotes(e.toString());
                PersistenceElement.writeAsciiLine(fos, line);
            }
            fos.close();
        }
        catch ( Exception e ) {
            VSDK.reportMessageWithException(
                null, 
                VSDK.FATAL_ERROR, "analyzeIndexPage", "error en pagina", e);
        }
    }

    /**
    @param page
    @param sites
    */
    private static void downloadSitesFromIndexPage(
        String page,
        HashMap<String, String> sites,
        String cityName)
    {
        TaggedHtml pageProcessor;

        pageProcessor = new TaggedHtml();

        System.out.println("Bajando pagina inicial: " + page);
        List<String> cookies;
        cookies = pageProcessor.getInternetPage(page);
        //importLinkListFromIndexPage(pageProcessor, sites);
        //importIncrementalList(pageProcessor, sites);
        AtomicInteger pageNumber = new AtomicInteger(0);
        AtomicInteger pages = new AtomicInteger(0);

        String msg;
        boolean end;

        msg = JsonHeaderGenerator.doHeader(cityName, "", "");
        JsonHeaderGenerator.writeStringToFile(msg, "initHeader.json");
        
        System.out.println("Configurando para bajar en grupos de a 52 elementos");
        end = pageProcessor.postInternetPage(
            "http://www.metrocuadrado.com/web/busqueda/numeroResultados-52",
            pageNumber,
            pages,
            "initHeader.json",
            cookies,
            page);
        importIncrementalList(pageProcessor, sites);

        while ( !end ) {
            int n = (pageNumber.get()+1);
            int m = (pages.get());
            System.out.println("Ahora quiero ir a la pagina " + n + " de entre " + m);
            if ( n > m ) {
                break;
            }
            String url = "http://www.metrocuadrado.com/web/busqueda/pagina-" + n;
            //System.out.println("URL: " + url);
            
            //msg = JsonHeaderGenerator.doHeader("bogota", "Apartamento", "arriendo");
            msg = JsonHeaderGenerator.doHeader(cityName, "", "");
            JsonHeaderGenerator.writeStringToFile(msg, "header.json");
            
            end = pageProcessor.postInternetPage(
                url, pageNumber, pages, "header.json", cookies, page);
            importIncrementalList(pageProcessor, sites);

            //listPageTags(pageProcessor, sites);
            if ( n ==  m )  {
                end =  true;
            }
        }
    }

    /**
    @param pageProcessor
    @param sites
    */
    private static void importLinkListFromIndexPage(TaggedHtml pageProcessor,
        HashMap<String, String> sites)
    {
        if ( pageProcessor.segmentList == null ) {
            System.out.println("Warning: empty page");
            return;
        }

        TagSegment ts;
        int i;
        int j;
        String n;
        String v;
        boolean insideMainDivTag = false;

        for ( i = 0; i < pageProcessor.segmentList.size() - 1; i++ ) {
            ts = pageProcessor.segmentList.get(i);

            if ( ts.getTagName() == null ) {
                continue;
            }
            
            if ( ts.getTagName().equals("DIV") ) {
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("id") && v.contains("rb_contResultados") ) {
                        insideMainDivTag = true;
                        break;
                    }
                    if ( n.equals("id") && v.contains("rb_contElemCent") ) {
                        insideMainDivTag = false;
                        break;
                    }
                }
            }
            else if ( ts.getTagName().equals("A") ) {
                boolean isItemProp = false;
                String url = "undefined";
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("href") ) {
                        url = v;
                    }
                    else if ( n.equals("itemprop") && v.contains("url") ) {
                        isItemProp = true;
                    }
                }
                if ( isItemProp && !sites.containsKey(url) && insideMainDivTag ) {
                    sites.put(url, url);
                }
            }
        }
    }

    /**
    @param pageProcessor
    @param sites
    */
    private static void importIncrementalList(
        TaggedHtml pageProcessor,
        HashMap<String, String> sites)
    {
        if ( pageProcessor.segmentList == null ) {
            System.out.println("Warning: empty page");
            return;
        }

        TagSegment ts;
        int i;
        int j;
        String n;
        String v;
        int count = 1;

        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            ts = pageProcessor.segmentList.get(i);

            if ( ts.getTagName() == null ) {
                continue;
            }

            if ( ts.getTagName().equals("A") ) {
                boolean isItemProp = false;
                String url = "undefined";
                for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                    n = ts.getTagParameters().get(j).name;
                    v = ts.getTagParameters().get(j).value;
                    if ( n.equals("href") ) {
                        url = v;
                    }
                    else if ( n.equals("itemprop") && v.contains("url") ) {
                        isItemProp = true;
                    }
                }
                if ( isItemProp && !sites.containsKey(url) ) {
                    url = url.replace("\\\"", "");
                    sites.put(url, url);
                    System.out.println("  - [" + count + "]" + url);
                    count++;
                }
            }
        }
    }

    /**
    @param pageProcessor
    */
    private static Property buildEntryFromPage(
        TaggedHtml pageProcessor, int businessType, String businessCity)
    {
        Property p;

        p = new Property();
        
        p.setBusinessType(businessType);
        p.setBusinessCity(businessCity);

        if ( pageProcessor.segmentList == null ) {
            System.out.println("Warning: empty page");
            return null;
        }

        TagSegment ts;
        String lastLink = null;
        int i;
        int j;
        String n;
        String v;

        String v0 = "";

        boolean doEstrato = false;
        boolean doTotalArea = false;
        boolean doConstructedArea = false;
        boolean doRooms = false;
        boolean doWc = false;
        boolean doParkings = false;
        boolean doCommonBlock = false;
        boolean doCatastralBlock = false;
        boolean doExtra = false;
        boolean doCell = false;
        boolean doPhone = false;
        boolean doAddress = false;
        boolean doPriceAdmin = false;
        boolean doTimeBuilt = false;
        boolean doPropertyType = false;
        String lastPriceType = "unknown";
        
        

        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            ts = pageProcessor.segmentList.get(i);
            

            if ( !ts.insideTag  ) {
				if  ( v0.contains("price") ) {
                    
                    if ( lastPriceType.equals("Valor arriendo:") ) {
                        p.setBusinessPriceLease(doubleNumber(ts.content));
                    }
                    else if ( lastPriceType.equals("Valor venta:") ) {
                        p.setBusinessPriceSale(doubleNumber(ts.content));
                    }
                    else {
                        System.out.println("ERROR: Precio desconocido [" +
                                lastPriceType + "]");
                        System.exit(9);
                    }
                }
                else if  ( ts.content.contains("Estrato") ) {
                    doEstrato = true;
                }
                else if ( doEstrato ) {
                    p.setSocialLevel(intNumber(ts.content));
                    doEstrato = false;
                }
                else if  ( ts.content.contains("rea Construida") ) {
                    doConstructedArea = true;
                }
                else if ( doConstructedArea ) {
                    p.setAreaConstructed(doubleNumber(ts.content));
                    doConstructedArea = false;
                }
                else if  ( ts.content.contains("rea:") ) {
                    doTotalArea = true;
                }
                else if ( doTotalArea ) {
                    p.setAreaTotal(doubleNumber(ts.content));
                    doTotalArea = false;
                }
                else if  ( ts.content.contains("Valor") ) {
                    lastPriceType = ts.content;
                }
                else if  ( ts.content.contains("Habitaciones:") ) {
                    doRooms = true;
                }
                else if ( doRooms ) {
                    p.setNumberOfRooms(intNumber(ts.content));
                    doRooms = false;
                }
                else if  ( ts.content.contains("Ba") && ts.content.contains("os:") ) {
                    doWc = true;
                }
                else if ( doWc ) {
                    p.setNumberOfBathrooms(intNumber(ts.content));
                    doWc = false;
                }
                else if  ( ts.content.contains("Gara") ) {
                    doParkings = true;
                }
                else if ( doParkings ) {
                    p.setNumberOfParkingLots(intNumber(ts.content));
                    doParkings = false;
                }
                else if  ( ts.content.contains("Nombre") && ts.content.contains("Barrio") ) {
                    doCommonBlock = true;
                }
                else if ( doCommonBlock ) {
                    p.setBlock(ts.content);
                    doCommonBlock = false;
                }
                else if  ( ts.content.contains("Barrio Ca") ) {
                    doCatastralBlock = true;
                }
                else if ( doCatastralBlock ) {
                    doCatastralBlock = false;
                    p.setBlockCadastre(ts.content);
                }
                else if  ( ts.content.contains("Tiempo de construido") ) {
                    doTimeBuilt = true;
                }
                else if ( doTimeBuilt ) {
                    p.setTimeBuilt(ts.content);
                    doTimeBuilt = false;
                }
                else if  ( ts.content.contains("Tipo Inmueble") ) {
                    doPropertyType  = true;
                }
                else if ( doPropertyType ) {
                    p.setPropertyType(ts.content);
                    doPropertyType = false;
                }
                else if  ( ts.content.contains("Ver otras cara") ) {
                    if ( lastLink != null ) {
                        String url = "http://www.metrocuadrado.com/" +
                            TaggedHtml.trimQuotes(lastLink);
                        TaggedHtml childProcessor = new TaggedHtml();

                        childProcessor.getInternetPage(url);
                        /*
                        Property c = buildEntryFromPage(childProcessor);

                        p.setPhoneFixed(c.getPhoneFixed());
                        p.setPhoneMobile(c.getPhoneMobile());
                        p.setAddress(c.getAddress());
                        */
                        lastLink = null;
                    }
                }

                if ( doPriceAdmin ) {
                    String dataSegment = ts.content.replace("\">", "");
                    dataSegment = TaggedHtml.trimSpaces(dataSegment);
                    if ( dataSegment.contains("$") && lastPriceType.equals("Valor admon:") ) {
                        lastPriceType = "Done";
                        p.setBusinessPriceAdmin(doubleNumber(dataSegment));
                    }
                    doPriceAdmin = false;
                }
                
                if ( doExtra ) {
                    String dataSegment = ts.content.replace("\">", "");
                    dataSegment = TaggedHtml.trimSpaces(dataSegment);

                    if ( dataSegment.contains("Tel:") ) {
                        doPhone = true;
                    }
                    else if ( doPhone ) {
                        doPhone = false;
                        p.setPhoneFixed(dataSegment);
                    }
                    if ( dataSegment.contains("Cel:") ) {
                        doCell = true;
                    }
                    else if ( doCell ) {
                        doCell = false;
                        p.setPhoneMobile(dataSegment);
                    }
                    if ( dataSegment.contains("Dir:") ) {
                        doAddress = true;
                    }
                    else if ( doAddress ) {
                        doAddress = false;
                        p.setAddress(dataSegment);
                    }
                }
            }
            
            if ( ts != null && ts.getTagName() != null && ts.getTagName().equals("STRONG") ) {
                doPriceAdmin = true;
            }
            
            for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                n = ts.getTagParameters().get(j).name;
                v = ts.getTagParameters().get(j).value;
                v0 = v;
                if ( n.equals("href") ) {
                    lastLink = v;
                }
                else if ( n.equals("data-content") ) {
                    doExtra = true;
                }

                if ( doExtra && n.equals("/a") ) {
                    doExtra = false;
                    //return p;
                }
                if ((n.equals("id")) && (v.contains("latitud")))
                {
                	p.setLatitudeDegrees(doubleNumberCoordinate(ts.getTagParameters().get(j+1).value));
                }
                
                if ((n.equals("id")) && (v.contains("longitud")))
                {
                	p.setLongitudeDegrees(doubleNumberCoordinate(ts.getTagParameters().get(j+1).value));
                }
            }
        }
        return p;
    }

    private static double doubleNumber(String n)
    {
        double d;
        try {
            String trimmed = crearNumbers(n);
            if ( trimmed.length() == 0 ) {
                return 0.0;
            }
            d = Double.parseDouble(trimmed);
        }
        catch ( NumberFormatException e ) {
            VSDK.reportMessageWithException(
                null, 
                VSDK.WARNING, "doubleNumber", 
                "error en formato de numero " + n, e);
            return -666.0;
        }
        return d;
    }

    private static double doubleNumberCoordinate(String n)
    {
        double d;
        try {
            String trimmed = crearNumbersCoordinate(n);
            if ( trimmed.length() == 0 ) {
                return 0.0;
            }
            d = Double.parseDouble(trimmed);
        }
        catch ( NumberFormatException e ) {
            VSDK.reportMessageWithException(
                null, 
                VSDK.WARNING, "doubleNumber", 
                "error en formato de numero " + n, e);
            return -666.0;
        }
        return d;
    }

    private static int intNumber(String n)
    {
        int d;
        try {
            String trimmed = crearNumbers(n);
            if ( trimmed.length() == 0 ) {
                return 0;
            }
            d = Integer.parseInt(trimmed);
        }
        catch ( NumberFormatException e ) {
            VSDK.reportMessageWithException(
                null, 
                VSDK.WARNING, "intNumber", 
                "error en formato de numero para el entero " + n, e);
            return -666;
        }
        return d;
    }

    /**
    @param pageProcessor
    */
    private static void listTagsFromPage(TaggedHtml pageProcessor)
    {
        if ( pageProcessor.segmentList == null ) {
            System.out.println("Warning: empty page");
            return;
        }

        TagSegment ts;
        int i;
        int j;
        String n;
        String v;

        boolean doNext = false;

        for ( i = 0; i < pageProcessor.segmentList.size(); i++ ) {
            ts = pageProcessor.segmentList.get(i);

            if ( !ts.insideTag && doNext ) {
                doNext = false;
                System.out.println(ts.content);
            }
            System.out.println("TAG: " + ts.getTagName());

            for ( j = 0; j < ts.getTagParameters().size(); j++ ) {
                n = ts.getTagParameters().get(j).name;
                v = ts.getTagParameters().get(j).value;

                System.out.println("  - " + n + " = " + v);
            }
        }
    }

    /**
    @param fd
    */
    private static void processIndexes(
        File fd, 
        int businessType,
        String businessCity)
    {
        try {
            FileInputStream fis;
            BufferedInputStream bis;

            fis = new FileInputStream(fd);
            bis = new BufferedInputStream(fis);
            int cont = 0;

            while ( bis.available() > 0 ) {
                String line;
                line = PersistenceElement.readAsciiLine(bis);
                processUrl(line, businessType, businessCity);
                cont++;
            }
            System.out.println("Se procesaron "+cont+" URL");

        }
        catch ( Exception e ) {
            VSDK.reportMessageWithException(
                null, 
                VSDK.FATAL_ERROR, "processIndexes", 
                "error procesando indices", e);
        }
    }

    /**
    @param url
    */
    private static void processUrl(
        String url, 
        int businessType, 
        String businessCity)
    {
        System.out.println("URL: " + url);
        TaggedHtml pageProcessor;

        if ( databaseConnection.existInMongoDatabase(url) ) {
            return;
        }

        pageProcessor = new TaggedHtml();

        pageProcessor.getInternetPage(url);

        Property p;

        p = buildEntryFromPage(pageProcessor, businessType, businessCity);

        if ( p != null ) {
            p.setUrl(url);
            //databaseConnection.insertPropertyMysql(p);
            databaseConnection.insertPropertyMongo(p);
        }

    }

    /**
    This method cleans some numbers as such "$1`300.000"
    */
    private static String crearNumbers(String content)
    {
        String s = "";
        int i;
        int numberOfPoints = 0;

        for ( i = 0; i < content.length(); i++ ) {
            char c = content.charAt(i);
            if ( Character.isDigit(c) || c == '-' ) {
                s += c;
            }
        }
        
        return s;
    }

    /**
    */
    private static String crearNumbersCoordinate(String content)
    {
        String s = "";
        int i;
        int numberOfPoints = 0;

        for ( i = 0; i < content.length(); i++ ) {
            char c = content.charAt(i);
            if ( Character.isDigit(c) || c == '.' || c == '-' ) {
                s += c;
            }
            if ( c == '.' ) {
                numberOfPoints++;
            }
        }

        if ( numberOfPoints > 1 ) {
            s = "";
            for ( i = 0; i < content.length(); i++ ) {
                char c = content.charAt(i);
                if ( Character.isDigit(c) || c == '-' ) {
                    s += c;
                }
            } 
        }
        
        return s;
    }

    /**
    @param baseUrl
    @param cityName
    @param cacheFilename
    */
    public static void downloadPageIndexes(String baseUrl, String cityName, String cacheFilename)
    {
        int i;

        try {
            String command[] = {"mkdir", "-p", "./database/"};
            Process p = Runtime.getRuntime().exec(command);
            //System.out.println("Creating folder " + bundle);
        }
        catch ( Exception e ) {
            VSDK.reportMessageWithException(
                null, 
                VSDK.FATAL_ERROR, "downloadPageIndexes", 
                "error creando carpeta", e);
        }

        analyzeIndexPage(baseUrl, cityName, cacheFilename);
    }

    private static String[][] calculateRegions()
    {
        String m[][] = {
            /*{"Abrego", "abrego"},
            {"Acacias", "acacias"},
            {"Acandi", "acandi"},
            {"Agua De Dios", "aguadedios"},
            {"Aguachica", "aguachica"},
            {"Aguadas", "aguadas"},
            {"Aguazul", "aguazul"},
            {"Alban", "alban"},
            {"Alcala", "alcala"},
            {"Almeida", "almeida"},
            {"Alvarado", "alvarado"},
            {"Amaga", "amaga"},
            {"Anapoima", "anapoima"},
            {"Angelópolis", "angelopolis"},
            {"Anolaima", "anolaima"},
            {"Antioquia", "antioquia"},
            {"Apartado", "apartado"},
            {"Apulo", "apulo"},
            {"Aquitania", "aquitania"},
            {"Aracataca", "aracataca"},
            {"Arauca", "arauca"},
            {"Arbelaez", "arbelaez"},
            {"Arcabuco", "arcabuco"},
            {"Ariguani", "ariguani"},
            {"Arjona", "arjona"},
            {"Armenia", "armenia"},
            {"Armenia", "armenia"},
            {"Armero Guayabal", "armeroguayabal"},
            {"Bahia Solano", "bahiasolano"},
            {"Balboa", "balboa"},
            {"Baranoa", "baranoa"},
            {"Baraya", "baraya"},
            {"Barbosa", "barbosa"},
            {"Barbosa", "barbosa"},
            {"Barichara", "barichara"},
            {"Barrancabermeja", "barrancabermeja"},
            {"Barranco De Loba", "barrancodeloba"},
            {"Barranquilla", "barranquilla"},
            {"Barú", "baru"},
            {"Belalcazar", "belalcazar"},
            {"Belen", "belen"},
            {"Bello", "bello"},
            {"Bello", "bello"},
            {"Beltran", "beltran"},
            {"Berbeo", "berbeo"},
            {"Betulia", "betulia"},
            {"Bituima", "bituima"},
            {"Bogotá", "bogota"},
            {"Bojaca", "bojaca"},
            {"Bojaya", "bojaya"},
            {"Bolivar", "bolivar"},
            {"Bolivar", "bolivar"},
            {"Boyaca", "boyaca"},
            {"Briceno", "briceno"},
            {"Briceño", "briceño"},
            {"Bucaramanga", "bucaramanga"},
            {"Buenaventura", "buenaventura"},
            {"Buenavista", "buenavista"},
            {"Buesaco", "buesaco"},
            {"Buga", "buga"},
            {"Bugalagrande", "bugalagrande"},
            {"Cabrera", "cabrera"},
            {"Cachipay", "cachipay"},
            {"Caicedonia", "caicedonia"},
            {"Cajicá", "cajica"},
            {"Calamar", "calamar"},
            {"Calarca", "calarca"},
            {"Caldas", "caldas"},
            {"Caldas", "caldas"},
            {"Cali", "cali"},
            {"Calima", "calima"},
            {"Campo De La Cruz", "campodelacruz"},
            {"Candelaria", "candelaria"},
            {"Caparrapi", "caparrapi"},
            {"Caqueza", "caqueza"},
            {"Carmen De Apicala", "carmendeapicala"},
            {"Carmen De Bolivar", "carmendebolivar"},
            {"Carmen De Carupa", "carmendecarupa"},
            {"Carmen De Viboral", "carmendeviboral"},
            {"Cartagena de Indias", "cartagenadeindias"},
            {"Cartago", "cartago"},
            {"Castilla La Nueva", "castillalanueva"},
            {"Cereté", "cerete"},
            {"Chaguani", "chaguani"},
            {"Chameza", "chameza"},
            {"Chaparral", "chaparral"},
            {"Charala", "charala"},
            {"Chía", "chia"},
            {"Chicoral", "chicoral"},
            {"Chinacota", "chinacota"},
            {"Chinauta", "chinauta"},
            {"Chipaque", "chipaque"},
            {"Chiquinquira", "chiquinquira"},
            {"Chiriguana", "chiriguana"},
            {"Chitaraque", "chitaraque"},
            {"Chivata", "chivata"},
            {"Choachi", "choachi"},
            {"Choconta", "choconta"},
            {"Cienaga", "cienaga"},
            {"Cienaga", "cienaga"},
            {"Cimitarra", "cimitarra"},
            {"Circasia", "circasia"},
            {"Circasia", "circasia"},
            {"Cocorna", "cocorna"},
            {"Coello", "coello"},
            {"Cogua", "cogua"},
            {"Colombia", "colombia"},
            {"Combita", "combita"},
            {"Concepcion", "concepcion"},
            {"Condoto", "condoto"},
            {"Confines", "confines"},
            {"Copacabana", "copacabana"},
            {"Cordoba", "cordoba"},
            {"Cordoba", "cordoba"},
            {"Corozal", "corozal"},
            {"Cota", "cota"},
            {"Coveñas", "covenas"},
            {"Coyaima", "coyaima"},
            {"Cubarral", "cubarral"},
            {"Cucunubá", "cucunuba"},
            {"Cúcuta", "cúcuta"},
            {"Cumaral", "cumaral"},
            {"Cumaral", "cumaral"},
            {"Cunday", "cunday"},
            {"Cundinamarca", "cundinamarca"},
            {"Curití", "curiti"},
            {"Dagua", "dagua"},
            {"Dibulla", "dibulla"},
            {"Don Matias", "don matias"},
            {"Dos Quebradas", "dos quebradas"},
            {"Duitama", "duitama"},
            {"El Cerrito", "el cerrito"},
            {"El Colegio", "el colegio"},
            {"El Dovio", "el dovio"},
            {"El Encanto", "el encanto"},
            {"El Guamo", "el guamo"},
            {"El Ocaso", "el ocaso"},
            {"El Penon", "el penon"},
            {"El Rosal", "el rosal"},
            {"Envigado", "envigado"},
            {"Espinal", "espinal"},
            {"Facatativa", "facatativa"},
            {"Falan", "falan"},
            {"Filandia", "filandia"},
            {"Firavitoba", "firavitoba"},
            {"Flandes", "flandes"},
            {"Florencia", "florencia"},
            {"Florida", "florida"},
            {"Floridablanca", "floridablanca"},
            {"Fomeque", "fomeque"},
            {"Fosca", "fosca"},
            {"Fredonia", "fredonia"},
            {"Fresno", "fresno"},
            {"Fuente De Oro", "fuente de oro"},
            {"Fundacion", "fundacion"},
            {"Funza", "funza"},
            {"Fuquene", "fuquene"},
            {"Fusagasuga", "fusagasuga"},
            {"Gachancipa", "gachancipa"},
            {"Gachantiva", "gachantiva"},
            {"Gacheta", "gacheta"},
            {"Galapa", "galapa"},
            {"Gambita", "gambita"},
            {"Garagoa", "garagoa"},
            {"Garzon", "garzon"},
            {"Genova", "genova"},
            {"Gigante", "gigante"},
            {"Ginebra", "ginebra"},
            {"Girardot", "girardot"},
            {"Girardota", "girardota"},
            {"Giron", "giron"},
            {"Gomez Plata", "gomez plata"},
            {"Granada", "granada"},
            {"Granada", "granada"},
            {"Granada", "granada"},
            {"Guaduas", "guaduas"},
            {"Guamal", "guamal"},
            {"Guamal", "guamal"},
            {"Guamo", "guamo"},
            {"Guarne", "guarne"},
            {"Guasca", "guasca"},
            {"Guatape", "guatape"},
            {"Guataqui", "guataqui"},
            {"Guatavita", "guatavita"},
            {"Guateque", "guateque"},
            {"Guayabal De Siquima", "guayabal de siquima"},
            {"Hispania", "hispania"},
            {"Honda", "honda"},
            {"Ibagué", "ibagué"},
            {"Icononzo", "icononzo"},
            {"Inirida", "inirida"},
            {"Ipiales", "ipiales"},
            {"Islas del Rosario", "islas del rosario"},
            {"Itagui", "itagui"},
            {"Iza", "iza"},
            {"Jamundi", "jamundi"},
            {"Jenesano", "jenesano"},
            {"Jerico", "jerico"},
            {"Jerusalen", "jerusalen"},
            {"Juan De Acosta", "juan de acosta"},
            {"La Calera", "la calera"},
            {"La Ceja", "la ceja"},
            {"La Cumbre", "la cumbre"},
            {"La Dorada", "la dorada"},
            {"La Estrella", "la estrella"},
            {"La Gloria", "la gloria"},
            {"La Mesa", "la mesa"},
            {"La Palma", "la palma"},
            {"La Pintada", "la pintada"},
            {"La Plata", "la plata"},
            {"La Primavera", "la primavera"},
            {"La Sierra", "la sierra"},
            {"La Tebaida", "la tebaida"},
            {"La Union", "la union"},
            {"La Vega", "la vega"},
            {"La Vega", "la vega"},
            {"La Victoria", "la victoria"},
            {"La Virginia", "la virginia"},
            {"Landazuri", "landazuri"},
            {"Lebrija", "lebrija"},
            {"Lejanias", "lejanias"},
            {"Lenguazaque", "lenguazaque"},
            {"Lerida", "lerida"},
            {"Leticia", "leticia"},
            {"Libano", "libano"},
            {"Los Santos", "los santos"},
            {"Luruaco", "luruaco"},
            {"Macanal", "macanal"},
            {"Macheta", "macheta"},
            {"Madrid", "madrid"},
            {"Maicao", "maicao"},
            {"Malambo", "malambo"},
            {"Mamonal", "mamonal"},
            {"Mani", "mani"},*/
            {"Manizales", "manizales"}/*,
            {"Manta", "manta"},
            {"Mapiripan", "mapiripan"},
            {"marandua", "marandua"},
            {"Marinilla", "marinilla"},
            {"Mariquita", "mariquita"},
            {"Marsella", "marsella"},
            {"Marulanda", "marulanda"},
            {"Medellín", "medellín"},
            {"Medina", "medina"},
            {"Melgar", "melgar"},
            {"Meseta", "meseta"},
            {"Mesitas del Colegio", "mesitas del colegio"},
            {"Miraflores", "miraflores"},
            {"Miraflores", "miraflores"},
            {"Mocoa", "mocoa"},
            {"Mompos", "mompos"},
            {"Mongui", "mongui"},
            {"Moniquira", "moniquira"},
            {"Monitos", "monitos"},
            {"Montebello", "montebello"},
            {"Montenegro", "montenegro"},
            {"Montería", "montería"},
            {"Monterrey", "monterrey"},
            {"Morelia", "morelia"},
            {"Mosquera", "mosquera"},
            {"Mosquera", "mosquera"},
            {"Narino", "narino"},
            {"Natagaima", "natagaima"},
            {"Neira", "neira"},
            {"Neiva", "neiva"},
            {"Nemocon", "nemocon"},
            {"Neusa", "neusa"},
            {"Nilo", "nilo"},
            {"Nimaima", "nimaima"},
            {"Nobsa", "nobsa"},
            {"Nocaima", "nocaima"},
            {"Nuevo Colon", "nuevo colon"},
            {"Oiba", "oiba"},
            {"Oicata", "oicata"},
            {"Orocue", "orocue"},
            {"Ortega", "ortega"},
            {"Pacho", "pacho"},
            {"Pailitas", "pailitas"},
            {"Paipa", "paipa"},
            {"Palermo", "palermo"},
            {"Palestina", "palestina"},
            {"Palestina", "palestina"},
            {"Palmas Del Socorro", "palmas del socorro"},
            {"Palmira", "palmira"},
            {"Palocabildo", "palocabildo"},
            {"Pamplona", "pamplona"},
            {"Pance", "pance"},
            {"Pandi", "pandi"},
            {"Paratebueno", "paratebueno"},
            {"Pasca", "pasca"},
            {"Pasto", "pasto"},
            {"Paya", "paya"},
            {"Paz De Ariporo", "paz de ariporo"},
            {"Peñol", "peñol"},
            {"Pereira", "pereira"},
            {"Pesca", "pesca"},
            {"Piedecuesta", "piedecuesta"},
            {"Piojo", "piojo"},
            {"Pitalito", "pitalito"},
            {"Pivijay", "pivijay"},
            {"Planeta Rica", "planeta rica"},
            {"Plato", "plato"},
            {"Popayán", "popayan"},
            {"Prado", "prado"},
            {"Providencia", "providencia"},
            {"Pueblo Rico", "pueblo rico"},
            {"Puebloviejo", "puebloviejo"},
            {"Puente Nacional", "puente nacional"},
            {"Puerto Asis", "puerto asis"},
            {"Puerto Berrio", "puerto berrio"},
            {"Puerto Boyaca", "puerto boyaca"},
            {"Puerto Carreño", "puerto carreño"},
            {"Puerto Colombia", "puerto colombia"},
            {"Puerto Gaitan", "puerto gaitan"},
            {"Puerto Lleras", "puerto lleras"},
            {"Puerto Lopez", "puerto lopez"},
            {"Puerto Narino", "puerto narino"},
            {"Puerto Rondon", "puerto rondon"},
            {"Puerto Salgar", "puerto salgar"},
            {"Puerto Tejada", "puerto tejada"},
            {"Puerto Triunfo", "puerto triunfo"},
            {"Puli", "puli"},
            {"Purificacion", "purificacion"},
            {"Quibdó", "quibdó"},
            {"Quimbaya", "quimbaya"},
            {"Quinchia", "quinchia"},
            {"Quipile", "quipile"},
            {"Ramiriqui", "ramiriqui"},
            {"Raquira", "raquira"},
            {"Restrepo", "restrepo"},
            {"Retiro", "retiro"},
            {"Ricaurte", "ricaurte"},
            {"Riofrio", "riofrio"},
            {"Riohacha", "riohacha"},
            {"Rionegro", "rionegro"},
            {"Risaralda", "risaralda"},
            {"Rivera", "rivera"},
            {"Rosas", "rosas"},
            {"Sabana De Torres", "sabana de torres"},
            {"Sabanalarga", "sabanalarga"},
            {"Sabaneta", "sabaneta"},
            {"Saboya", "saboya"},
            {"Sachica", "sachica"},
            {"Saldaña", "saldaña"},
            {"Salento", "salento"},
            {"Salgar", "salgar"},
            {"Samaca", "samaca"},
            {"San Agustin", "san agustin"},
            {"San Alberto", "san alberto"},
            {"San Andrés", "san andrés"},
            {"San Antero", "san antero"},
            {"San Antonio Del Tequendama", "san antonio del tequendama"},
            {"San Bernardo", "san bernardo"},
            {"San Bernardo Del Viento", "san bernardo del viento"},
            {"San Carlos Guaroa", "san carlos guaroa"},
            {"San Cayetano", "san cayetano"},
            {"San Francisco", "san francisco"},
            {"San Gil", "san gil"},
            {"San Jeronimo", "san jeronimo"},
            {"San José Del Guaviare", "san josé del guaviare"},
            {"San José Del Palmar", "san josé del palmar"},
            {"San Juan De Arama", "san juan de arama"},
            {"San Juan De Rioseco", "san juan de rioseco"},
            {"San Luis", "san luis"},
            {"San Luis De Gaceno", "san luis de gaceno"},
            {"San Martín", "san martín"},
            {"San Miguel De Sema", "san miguel de sema"},
            {"San Onofre", "san onofre"},
            {"San Pedro", "san pedro"},
            {"San Vicente", "san vicente"},
            {"San Vicente De Chucuri", "san vicente de chucuri"},
            {"Santa Ana", "santa ana"},
            {"Santa Barbara", "santa barbara"},
            {"Santa Catalina", "santa catalina"},
            {"Santa Isabel", "santa isabel"},
            {"Santa Lucia", "santa lucia"},
            {"Santa Maria", "santa maria"},
            {"Santa Marta", "santa marta"},
            {"Santa Rita", "santa rita"},
            {"Santa Rosa", "santa rosa"},
            {"Santa Rosa De Cabal", "santa rosa de cabal"},
            {"Santa Sofia", "santa sofia"},
            {"Santagueda", "santagueda"},
            {"Santander De Quilichao", "santander de quilichao"},
            {"Santandercito", "santandercito"},
            {"Santo Domingo", "santo domingo"},
            {"Santo Tomas", "santo tomas"},
            {"Santuario", "santuario"},
            {"Sasaima", "sasaima"},
            {"Sesquile", "sesquile"},
            {"Sibate", "sibate"},
            {"Siberia", "siberia"},
            {"Silvania", "silvania"},
            {"Simacota", "simacota"},
            {"Simijaca", "simijaca"},
            {"Sincelejo", "sincelejo"},
            {"Sitionuevo", "sitionuevo"},
            {"Soacha", "soacha"},
            {"Soata", "soata"},
            {"Socorro", "socorro"},
            {"Sogamoso", "sogamoso"},
            {"Soledad", "soledad"},
            {"Somondoco", "somondoco"},
            {"Sopetran", "sopetran"},
            {"Sopó", "sopó"},
            {"Soraca", "soraca"},
            {"Sotaquira", "sotaquira"},
            {"Sotara", "sotara"},
            {"Suaita", "suaita"},
            {"Suarez", "suarez"},
            {"Subachoque", "subachoque"},
            {"Subia", "subia"},
            {"Sucre", "sucre"},
            {"Suesca", "suesca"},
            {"Supata", "supata"},
            {"Susa", "susa"},
            {"Sutamarchan", "sutamarchan"},
            {"Sutatausa", "sutatausa"},
            {"Tabio", "tabio"},
            {"Tame", "tame"},
            {"Tauramena", "tauramena"},
            {"Tausa", "tausa"},
            {"Tena", "tena"},
            {"Tenjo", "tenjo"},
            {"Tenza", "tenza"},
            {"Tibacuy", "tibacuy"},
            {"Tibasosa", "tibasosa"},
            {"Tibirita", "tibirita"},
            {"Tibu", "tibu"},
            {"Tinjaca", "tinjaca"},
            {"Titiribi", "titiribi"},
            {"Tobia", "tobia"},
            {"Toca", "toca"},
            {"Tocaima", "tocaima"},
            {"Tocancipá", "tocancipa"},
            {"Togui", "togui"},
            {"Tolu", "tolu"},
            {"Tota", "tota"},
            {"Trujillo", "trujillo"},
            {"Tubara", "tubara"},
            {"Tulua", "tulua"},
            {"Tumaco", "tumaco"},
            {"Tunja", "tunja"},
            {"Turbaco", "turbaco"},
            {"Turmeque", "turmeque"},
            {"Tuta", "tuta"},
            {"Ubala", "ubala"},
            {"Ubaque", "ubaque"},
            {"Ubate", "ubate"},
            {"Ulloa", "ulloa"},
            {"Une", "une"},
            {"Utica", "utica"},
            {"Valdivia", "valdivia"},
            {"Valledupar", "valledupar"},
            {"Velez", "velez"},
            {"Venadillo", "venadillo"},
            {"Venecia", "venecia"},
            {"Venecia - Ospina Perez", "veneciaospinaperez"},
            {"Ventaquemada", "ventaquemada"},
            {"Vergara", "vergara"},
            {"Versalles", "versalles"},
            {"Viani", "viani"},
            {"Victoria", "victoria"},
            {"Villa De Leyva", "villa de leyva"},
            {"Villa Rosario", "villa rosario"},
            {"Villamaria", "villamaria"},
            {"Villanueva", "villanueva"},
            {"Villapinzon", "villapinzon"},
            {"Villavicencio", "villavicencio"},
            {"Villeta", "villeta"},
            {"Viota", "viota"},
            {"Yacopi", "yacopi"},
            {"Yaguara", "yaguara"},
            {"Yolombo", "yolombo"},
            {"Yopal", "yopal"},
            {"Yotoco", "yotoco"},
            {"Yumbo", "yumbo"},
            {"Zapatoca", "zapatoca"},
            {"Zarzal", "zarzal"},
            {"Zetaquira", "zetaquira"},
            {"Zipacon", "zipacon"},
            {"Zipaquira", "zipaquira"}*/
        };
        return m;
    }
    
    /**
    @param args
    */
    public static void main(String args [])
    {
        String m[][] = calculateRegions();
        int i;
        for ( i = 0; i < m.length; i++ ) {
            System.out.println("********* PROCESANDO: " + m[i][0] + " **********");
            String cityName = m[i][1];
            String cacheFilename = "cache_for_" + cityName + ".txt";
            File fd = new File(cacheFilename);

            if ( !fd.exists() ) {
                downloadPageIndexes("http://www.metrocuadrado.com/web/buscarFiltros/" + cityName + "-casa-venta", cityName, cacheFilename);
            }
            processIndexes(fd, 4, m[i][0]);
        }
        
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
