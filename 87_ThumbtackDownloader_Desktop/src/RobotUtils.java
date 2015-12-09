
import java.awt.Robot;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
*/
public class RobotUtils {
    protected static void click(Robot r) {
        r.mousePress(InputEvent.BUTTON1_MASK);
        r.delay(200);
        r.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    protected static void click2(Robot r) {
        r.mousePress(InputEvent.BUTTON3_MASK);
        r.delay(200);
        r.mouseRelease(InputEvent.BUTTON3_MASK);
    }

    protected static void writeString(
        String inputText, 
        Clipboard clipboard, 
        ClipboardOwner clipboardOwner, 
        Robot r) 
    {
        int i;
        
        for ( i = 0; i < inputText.length(); i++ ) {
            char c = Character.toLowerCase(inputText.charAt(i));
            
            int keycode = KeyEvent.VK_SPACE;
                        
            switch ( c ) {
                case '0': keycode = KeyEvent.VK_0; break;
                case '1': keycode = KeyEvent.VK_1; break;
                case '2': keycode = KeyEvent.VK_2; break;
                case '3': keycode = KeyEvent.VK_3; break;
                case '4': keycode = KeyEvent.VK_4; break;
                case '5': keycode = KeyEvent.VK_5; break;
                case '6': keycode = KeyEvent.VK_6; break;
                case '7': keycode = KeyEvent.VK_7; break;
                case '8': keycode = KeyEvent.VK_8; break;
                case '9': keycode = KeyEvent.VK_9; break;
                case 'a': keycode = KeyEvent.VK_A; break;
                case 'b': keycode = KeyEvent.VK_B; break;
                case 'c': keycode = KeyEvent.VK_C; break;
                case 'd': keycode = KeyEvent.VK_D; break;
                case 'e': keycode = KeyEvent.VK_E; break;
                case 'f': keycode = KeyEvent.VK_F; break;
                case 'g': keycode = KeyEvent.VK_G; break;
                case 'h': keycode = KeyEvent.VK_H; break;
                case 'i': keycode = KeyEvent.VK_I; break;
                case 'j': keycode = KeyEvent.VK_J; break;
                case 'k': keycode = KeyEvent.VK_K; break;
                case 'l': keycode = KeyEvent.VK_L; break;
                case 'm': keycode = KeyEvent.VK_M; break;
                case 'n': keycode = KeyEvent.VK_N; break;
                case 'o': keycode = KeyEvent.VK_O; break;
                case 'p': keycode = KeyEvent.VK_P; break;
                case 'q': keycode = KeyEvent.VK_Q; break;
                case 'r': keycode = KeyEvent.VK_R; break;
                case 's': keycode = KeyEvent.VK_S; break;
                case 't': keycode = KeyEvent.VK_T; break;
                case 'u': keycode = KeyEvent.VK_U; break;
                case 'v': keycode = KeyEvent.VK_V; break;
                case 'w': keycode = KeyEvent.VK_W; break;
                case 'x': keycode = KeyEvent.VK_X; break;
                case 'y': keycode = KeyEvent.VK_Y; break;
                case 'z': keycode = KeyEvent.VK_Z; break;
                case '.': keycode = KeyEvent.VK_PERIOD; break;
                case ':': keycode = KeyEvent.VK_COLON; break;
                case '/': keycode = KeyEvent.VK_SLASH; break;
                case '&': keycode = KeyEvent.VK_AMPERSAND; break;
                case '?': keycode = KeyEvent.VK_SPACE; break;
            }
            
            r.keyPress(keycode);
            r.delay(10);
            r.keyRelease(keycode);
            r.delay(10);
        }
    }
    
    protected static void writeStringCP(
        String inputText, 
        Clipboard clipboard, 
        ClipboardOwner clipboardOwner, 
        Robot r) 
    {
        
        StringSelection stringSelection = new StringSelection(inputText);
        clipboard.setContents(stringSelection, clipboardOwner);
        
        r.keyPress(KeyEvent.VK_META);
        r.delay(100);
        r.keyPress(KeyEvent.VK_V);
        r.delay(100);
        r.keyRelease(KeyEvent.VK_V);
        r.delay(100);
        r.keyRelease(KeyEvent.VK_META);
        r.delay(100);

        int keycode = KeyEvent.VK_ENTER;

        r.keyPress(keycode);
        r.delay(10);
        r.keyRelease(keycode);
        r.delay(10);
    }
    
}
