//===========================================================================   

// Java basic classes
import java.io.IOException;

// Swing/Awt classes                                                            
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;

// Toolkit classes
import awt.RobotUtils;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
*/
public class LocateFacebookUserByEmail {
    public static void main(String args[])
    {
        try {
            Toolkit.getDefaultToolkit();
            Robot r;
            r = new Robot();
            
            r.delay(2000);
            searchFacebookAccountByMail(r, "angelapolo96@gmail.com");
        } 
        catch (AWTException ex) {
            
        }
    }

    private static void searchFacebookAccountByMail(Robot r, String email) {
        try {
            int xBase = 1440;
            int yBase = 0;
            
            // PART A: select facebook search area
            r.delay(500);
            r.mouseMove(xBase + 800, yBase + 130);
            r.delay(100);
            r.mouseMove(xBase + 802, yBase + 130);
            RobotUtils.click(r);
            
            // PART B: paste email into Facebook search area
            ClipboardOwner clipboardOwner;
            clipboardOwner = null;
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            RobotUtils.writeStringCP(email, clipboard, clipboardOwner, r);
            r.delay(1000);
            
            // PART C: select browser URL text area
            r.delay(500);
            r.mouseMove(xBase + 800, yBase + 75);
            r.delay(100);
            r.mouseMove(xBase + 802, yBase + 75);
            RobotUtils.click(r);
            
            // PART D: copy URL into clipboard
            RobotUtils.copyMacro(r);
            String response;
            
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            
            response = (String)clipboard.getData(DataFlavor.stringFlavor);
            
            String validated = checkIfUrlIsProfile(response);
            if ( validated != null ) {
                System.out.println("This is a valid profile: " + validated);
            }            
        } 
        catch (UnsupportedFlavorException ex) {

        } 
        catch (IOException ex) {

        }
    }

    /**
    Return facebook URL if this is a valid profile, null if not
    */
    private static String checkIfUrlIsProfile(String url) {
        if ( url.contains("https://www.facebook.com/search/top/") ) {
            return null;
        }
        return url.replace("?fref=ts", "");
    }
}

//===========================================================================   
//= EOF                                                                     =
//===========================================================================   
