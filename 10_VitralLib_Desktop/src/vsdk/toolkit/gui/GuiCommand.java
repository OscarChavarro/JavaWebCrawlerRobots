//===========================================================================
package vsdk.toolkit.gui;

import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.media.RGBImage;
import vsdk.toolkit.media.RGBAImage;
import vsdk.toolkit.media.RGBPixel;
import vsdk.toolkit.media.RGBAPixel;

/**
This class plays a role of leaf on an n-ary tree in the composite design 
pattern.
*/
public class GuiCommand extends GuiElement {
    private String id;
    private String name;
    private String briefDescription;
    private String help; // Could contain HTML tags
    private RGBAImage icon;
    private RGBImage iconTransparency;
    private RGBAImage secondaryIcon;
    private RGBImage secondaryIconTransparency;

    /**
    
    */
    public GuiCommand()
    {
        id = null;
        name = null;
        briefDescription = null;
        help = null;
        icon = null;
        iconTransparency = null;
        secondaryIcon = null;
        secondaryIconTransparency = null;
    }

    /**
    
    @return 
    */
    public String getId()
    {
        return id;
    }

    /**
    
    @return 
    */
    public String getName()
    {
        return name;
    }

    /**
    
    @return 
    */
    public String getBriefDescription()
    {
        return briefDescription;
    }

    /**
    
    @return 
    */
    public String getHelp()
    {
        return help;
    }

    /**
    
    @return 
    */
    public RGBAImage getIcon()
    {
        return icon;
    }

    /**
    
    @return 
    */
    public RGBImage getIconTransparency()
    {
        return iconTransparency;
    }

    /**
    
    */
    public void applyTransparency()
    {
        if ( icon == null || iconTransparency == null ) {
            return;
        }

        int xlimit, ylimit, x, y;

        xlimit = Math.min(icon.getXSize(), iconTransparency.getXSize());
        ylimit = Math.min(icon.getYSize(), iconTransparency.getYSize());

        RGBPixel in;
        RGBAPixel out;
        int r, g, b, a;

        for ( y = 0; y < ylimit; y++ ) {
            for ( x = 0; x < xlimit; x++ ) {
                in = iconTransparency.getPixel(x, y);
                r = VSDK.signedByte2unsignedInteger(in.r);
                g = VSDK.signedByte2unsignedInteger(in.g);
                b = VSDK.signedByte2unsignedInteger(in.b);
                a = (r + g + b) / 3;
                out = icon.getPixel(x, y);
                out.a = VSDK.unsigned8BitInteger2signedByte(a);
                icon.putPixel(x, y, out);
            }
        }
    }

    /**
    
    */
    public void applySecondTransparency()
    {
        if ( secondaryIcon == null || secondaryIconTransparency == null ) {
            return;
        }

        int xlimit, ylimit, x, y;

        xlimit = Math.min(secondaryIcon.getXSize(), secondaryIconTransparency.getXSize());
        ylimit = Math.min(secondaryIcon.getYSize(), secondaryIconTransparency.getYSize());

        RGBPixel in;
        RGBAPixel out;
        int r, g, b, a;

        for ( y = 0; y < ylimit; y++ ) {
            for ( x = 0; x < xlimit; x++ ) {
                in = secondaryIconTransparency.getPixel(x, y);
                r = VSDK.signedByte2unsignedInteger(in.r);
                g = VSDK.signedByte2unsignedInteger(in.g);
                b = VSDK.signedByte2unsignedInteger(in.b);
                a = (r + g + b) / 3;
                out = secondaryIcon.getPixel(x, y);
                out.a = VSDK.unsigned8BitInteger2signedByte(a);
                secondaryIcon.putPixel(x, y, out);
            }
        }
    }

    /**
    
    @param i 
    */
    public void setId(String i)
    {
        id = i;
    }

    /**
    
    @param n 
    */
    public void setName(String n)
    {
        name = n;
    }

    /**
    
    @param b 
    */
    public void setBrief(String b)
    {
        briefDescription = b;
    }

    /**
    
    @param h 
    */
    public void setHelp(String h)
    {
        help = h;
    }

    /**
    
    @param h 
    */
    public void appendToHelp(String h)
    {
        if ( help != null ) {
            help = help + h;
        }
        else {
            help = h;
        }
    }

    /**
    
    @param i 
    */
    public void setIcon(RGBAImage i)
    {
        icon = i;
    }

    /**
    
    @param i 
    */
    public void setIconTransparency(RGBImage i)
    {
        iconTransparency = i;
    }

    /**
    
    @return 
    */
    @Override
    public String toString()
    {
        String msg =  "  - Command [" + id + "]:\n";
        msg = msg + "    . Name: " + name + "\n";
        msg = msg + "    . Brief description: " + briefDescription + "\n";
        if ( icon == null ) {
            msg = msg + "    . No icon image\n";
          }
          else {
              msg = msg + "    . Icon image of size (" + icon.getXSize() +
                  ", " + icon.getYSize() + ")\n";
        }
        return msg;
    }

    /**
    @return the secondaryIcon
    */
    public RGBAImage getSecondaryIcon()
    {
        return secondaryIcon;
    }

    /**
    @param secondaryIcon the secondaryIcon to set
    */
    public void setSecondaryIcon(RGBAImage secondaryIcon)
    {
        this.secondaryIcon = secondaryIcon;
    }

    /**
    @return the secondaryIconTransparency
    */
    public RGBImage getSecondaryIconTransparency()
    {
        return secondaryIconTransparency;
    }

    /**
    @param secondaryIconTransparency the secondaryIconTransparency to set
    */
    public void setSecondaryIconTransparency(RGBImage secondaryIconTransparency)
    {
        this.secondaryIconTransparency = secondaryIconTransparency;
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
