//===========================================================================
package vsdk.toolkit.gui;

import java.io.File;
import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.environment.Camera;
import vsdk.toolkit.io.image.ImagePersistence;
import vsdk.toolkit.media.Image;
import vsdk.toolkit.media.RGBAImage;
import vsdk.toolkit.media.RGBAPixel;
import vsdk.toolkit.media.RGBPixel;

/**
Class used to manage HUD based interactions based on button like images with
interaction area.
*/
public class HudIcon extends PresentationElement {
    /// Image to show, can be null
    private RGBAImage image;   
    
    /// Image coordinate to locate inside framebuffer. Can be negative following
    /// X11 geometry style
    private int x;
    private int y;

    /// Can be 0, in that case size is taken from image size
    private int xSize;
    private int ySize;
    
    /// URL to link when user does it click
    private int keyEvent;

    public HudIcon(int x, int y, int xSize, int ySize, String imageFilename, int keyEvent)
    {
        init(x, y, xSize, ySize, keyEvent, imageFilename);
    }

    /**
    Given a (x, y) coordinate in screen reference system (with 0, 0 coordinate
    on top left corner of screen), this method determines if that coordinate
    falls between current hud icon.
    @param inX
    @param inY
    @return true if current coordinate lies inside HudIcon with respect to
    camera's viewport configuration, false otherwise
    */
    public boolean doIntersection(int inX, int inY, Camera c)
    {
        int currentXSize = xSize;
        int currentYSize = ySize;
        
        if ( currentXSize <= 0 ) {
            currentXSize = image.getXSize();
        } 

        if ( currentYSize <= 0 ) {
            currentYSize = image.getYSize();
        } 
        
        int xx = x;
        int yy = y;
        
        if ( xx < 0 ) {
            xx = -xx;
            xx = (int)c.getViewportXSize() - image.getXSize() - xx;
        }
        if ( yy < 0 ) {
            yy = -yy;
            yy = (int)c.getViewportYSize() - image.getYSize() - yy;
        }
        
        return inX >= xx && inX <= xx + currentXSize &&
            inY >= yy && inY <= yy + currentYSize;
    }
    
    /**
    Given a (x, y) coordinate in screen reference system (with 0, 0 coordinate
    on top left corner of screen), this method determines if that coordinate
    falls between current hud icon. This version takes into account a gravity
    range around base rectangles.
    @param inX
    @param inY
    @param gravity
    @param c
    @return true if current coordinate lies inside HudIcon with respect to
    camera's viewport configuration, false otherwise
    */
    public boolean doIntersectionWithGravity(int inX, int inY, int gravity, 
        Camera c)
    {
        if ( doIntersection(inX, inY, c) ) {
            return true;
        }
        int currentXSize = xSize;
        int currentYSize = ySize;
        
        if ( currentXSize <= 0 ) {
            currentXSize = image.getXSize();
        } 

        if ( currentYSize <= 0 ) {
            currentYSize = image.getYSize();
        } 
        
        int xx = x;
        int yy = y;
        
        if ( xx < 0 ) {
            xx = -xx;
            xx = (int)c.getViewportXSize() - image.getXSize() - xx;
        }
        if ( yy < 0 ) {
            yy = -yy;
            yy = (int)c.getViewportYSize() - image.getYSize() - yy;
        }

        return inX >= (xx-gravity) && inX <= (xx+gravity) + currentXSize &&
            inY >= (yy-gravity) && inY <= (yy+gravity) + currentYSize;
    }
    
    /**
    Generates a segment of HTML1 code to encode current icon over a map for
    simple image interaction.
    @param baseUrl
    @return a string representing contents 
    */
    public String getMapTag(String baseUrl)
    {
        return "    <AREA SHAPE=\"rect\" COORDS=\"" + 
                getX() + ", " + 
                getY() + ", " +
                (getX() + xSize) + ", " + 
                (getY() + ySize) + "\" HREF=\"" + 
                baseUrl + KeyEvent.getKeyName(getKeyEvent()) + "\" " +
                "style=\"cursor: default;\"" + 
                "/>";
    }
    
    /**
    Modifies output image to add current image over it in the corresponding
    coordinates
    @param output Image to where pixels will be drawn
    */
    public void overWritePixels(Image output)
    {

        if ( image == null ) {
            return;
        }

        int myXSize = image.getXSize();
        int myYSize = image.getYSize();
        int xi, yi;
        RGBAPixel origin = new RGBAPixel();
        RGBPixel target = new RGBPixel();

        for ( xi = 0; xi < myXSize; xi++ ) {
            for ( yi = 0; yi < myYSize; yi++ ) {
                image.getPixelRgba(xi, yi, origin);
                target.r = origin.r;
                target.g = origin.g;
                target.b = origin.b;
                if ( VSDK.signedByte2unsignedInteger(origin.a) > 250 ) {
                    output.putPixelRgb(xi + getX(), yi + getY(), target);
                }
            }
        }
        
    }
    
    private void init(int x, int y, int xSize, int ySize, int keyEvent, String imageFilename) {
        this.setX(x);
        this.setY(y);
        this.xSize = xSize;
        this.ySize = ySize;
        this.keyEvent = keyEvent;
        
        try {
            image = null;
            
            if ( imageFilename != null ) {
                image = ImagePersistence.importRGBA(new File(imageFilename));
                if ( xSize == 0 ) {
                    this.xSize = image.getXSize();
                }
                if ( ySize == 0 ) {
                    this.ySize = image.getYSize();
                }
            }
            
        }
        catch (Exception e) {
            VSDK.reportMessageWithException(this, VSDK.WARNING, "WebIcon", "Image not found", e);
        }
    }
    
    /**
    @return the image
    */
    public RGBAImage getImage() {
        return image;
    }

    /**
    @param image the image to set
    */
    public void setImage(RGBAImage image) {
        this.image = image;
    }
    
    @Override
    public String toString()
    {
        String msg;
        
        if ( image != null ) { 
            msg = "ICON -> x:" + getX() + " y: " + getY() + " xSize: " + 
                    xSize + " ySize: " + ySize +
                " img: LOADED keyEvent: " + KeyEvent.getKeyName(getKeyEvent()) + "\n";
        }
        else {
            msg = "ICON -> x:" + getX() + " y: " + getY() + " xSize: " + 
                    xSize + " ySize: " + ySize +
                " img: null keyEvent: " + KeyEvent.getKeyName(getKeyEvent()) + "\n";
        }
        
        return msg;
    }

    /**
    @return the x
    */
    public int getX() {
        return x;
    }

    /**
    @param x the x to set
    */
    public void setX(int x) {
        this.x = x;
    }

    /**
    @return the y
    */
    public int getY() {
        return y;
    }

    /**
    @param y the y to set
    */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return the keyEvent
     */
    public int getKeyEvent() {
        return keyEvent;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
