//===========================================================================

// Java basic classes
import java.io.IOException;

// Swing/Awt classes
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;

/**
*/
public class TranslatorRobot extends RobotUtils {
    public static String translateUsingGoogle(
        String inputText, String en, String es) {
        
        try {
            Toolkit.getDefaultToolkit();
            Robot r;
            r = new Robot();
            
            // PART A: Clear input text
            r.delay(400);
            r.mouseMove(660, 300);
            r.delay(100);
            r.mouseMove(660, 301);
            click(r);
            click(r);

            // PART B: Paste original language text
            ClipboardOwner clipboardOwner;
            clipboardOwner = null;
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            
            writeString(inputText, clipboard, clipboardOwner, r);
            
            // PART C: Query translate
            r.delay(100);
            r.mouseMove(950, 260);
            click(r);
            r.mouseMove(955, 260);
            r.delay(500);
            click(r);
            r.delay(500);
            r.mouseMove(955, 270);
            r.delay(100);
            r.mouseMove(955, 275);
            r.delay(100);
            r.mouseMove(955, 279);

            // PART D: Select answer
            r.delay(500);
            r.mouseMove(1100, 400);
            r.delay(100);
            r.mouseMove(1100, 400);
            
            r.delay(500);
            click(r);
            r.delay(100);
            click(r);
            r.delay(500);
            
            // PART E: Copy answer
            r.keyPress(KeyEvent.VK_META);
            r.delay(200);
            r.keyPress(KeyEvent.VK_C);
            r.delay(500);
            r.keyRelease(KeyEvent.VK_C);
            r.delay(200);
            r.keyRelease(KeyEvent.VK_META);
            
            String data;
            
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            data = (String)clipboard.getData(DataFlavor.stringFlavor); 
            return data;
        }
        catch ( AWTException ex ) {
            System.out.println("ERROR A");
        } 
        catch (UnsupportedFlavorException ex) {
            System.out.println("ERROR B");
        } 
        catch (IOException ex) {
            System.out.println("ERROR C");
        }
        return "<ERROR>";
    }
    
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
