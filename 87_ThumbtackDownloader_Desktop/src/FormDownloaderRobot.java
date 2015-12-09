//===========================================================================

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import vsdk.toolkit.common.VSDK;

/**
*/
public class FormDownloaderRobot extends RobotUtils {
    public static void downloadForm(ProfessionalService s, int i)
    {
        try {
            Toolkit.getDefaultToolkit();
            Robot r;
            r = new Robot();
            
            // PART A: Select browser url
            r.delay(400);
            r.mouseMove(660, 300);
            r.delay(100);
            r.mouseMove(660, 301);
            click(r);

            r.delay(400);
            r.mouseMove(660, 80);
            r.delay(100);
            r.mouseMove(660, 81);
            click(r);

            // PART B: Paste url
            ClipboardOwner clipboardOwner;
            clipboardOwner = null;
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            
            writeStringCP(s.getLink(), clipboard, clipboardOwner, r);       
            
            // PART C: wait for form to be loaded
            Thread.sleep(5000);

            // PART D: save current page
            r.delay(400);
            r.mouseMove(100, 300);
            r.delay(100);
            r.mouseMove(100, 301);
            click2(r);

            r.delay(400);
            r.mouseMove(120, 370);
            r.delay(100);
            r.mouseMove(120, 374);
            click(r);
            
            r.delay(400);
            writeStringCP(VSDK.formatNumberWithinZeroes(i, 4), clipboard, clipboardOwner, r); 
        }
        catch ( AWTException ex ) {
            System.out.println("ERROR A");
        }        
        catch ( InterruptedException ex ) {
            System.out.println("ERROR B");
        }        
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
