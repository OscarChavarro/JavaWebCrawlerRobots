
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.io.PersistenceElement;

/**
*/
public class TextListCutterInSegments {
    public static void main(String args[]) 
    {
        try {
            TextListCutterInSegments instance;
            instance = new TextListCutterInSegments();
            
            instance.cutListInSegments("./etc/emaillist.txt", 200);
        }
        catch ( Exception e ) {
            
        }
    }

    private void cutListInSegments(String filename, int segmentSize) 
        throws Exception
    {
        int segmentId = 1;
        File fd = new File(filename);
        FileInputStream fis;
        fis = new FileInputStream(fd);
        BufferedInputStream bis;
        bis = new BufferedInputStream(fis);

        ArrayList<String> currentSegment;
        currentSegment = new ArrayList<String>();
        
        while ( bis.available() > 0 ) {
            String l;
            l = PersistenceElement.readAsciiLine(bis);
            currentSegment.add(l);
            if ( currentSegment.size() == segmentSize ) {
                dumpSegment(currentSegment, segmentId);
                currentSegment.clear();
                segmentId++;
            }
        }
        if ( !currentSegment.isEmpty() ) {
            dumpSegment(currentSegment, segmentId);
            currentSegment.clear();
        }
        bis.close();
        fis.close();
    }

    private void dumpSegment(ArrayList<String> currentSegment, int segmentId) 
        throws Exception
    {
        int i;
        String filename = "./output/segments/segment_" + 
            VSDK.formatNumberWithinZeroes(segmentId, 3) + ".csv";
        File fd = new File(filename);
        FileOutputStream fos;
        fos = new FileOutputStream(fd);
        BufferedOutputStream bos;
        bos = new BufferedOutputStream(fos);
        for ( i = 0; i < currentSegment.size(); i++ ) {
            PersistenceElement.writeAsciiLine(bos, currentSegment.get(i));
        }
        bos.flush();
        bos.close();
        fos.flush();
        fos.close();
    }
}
