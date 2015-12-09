//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - June 22 2008 - Oscar Chavarro: Original base version                  =
//===========================================================================

package vsdk.toolkit.gui;

/**
A `ViewportWindow` represents an specific 2D area, to be used as the viewport
inside a canvas or any GUI element / frame / dialog. Any specific GUI
technology used by a VitralSDK program (i.e. Jogl, Awt, Swing, PostScript area)
may contain one ore more `ViewportWindow`s, arranged geometrically in 2D,
ussually forming a partition (or at least a set of non overlaping 2D
rectangles).

The ViewportWindow covers an area defined in terms of percentages.  For example,
a GUI window canvas with a single ViewportWindow covering all of its area,
will have a view located at start percent (0%, 0%) with size (100%, 100%). When
the containing canvas resizes, the internal `ViewportWindow`s will resize
accordingly and will keep its ocupation percentages updated (see 
`viewportStartXPercent`, `viewportStartYPercent`, `viewportSizeXPercent` and
`viewportSizeYPercent` attributes).

A ViewportWindow can be managed by a ViewportWindowSet (forming in that way
a multiple viewport application). A ViewportWindow has an associated border
(which can be zero). Current VitralSDK visualization process should be
projected on the ViewportWindow's defined area, leaving `border` pixels
out of the projection area (the border is assigned a color and drawed to
mark the limits between ViewportWindow's).

A ViewportWindow can be `active` or inactive.  An active status means that
current viewport is visible and should be rendered. An inactive status means
that viewport is not visible and thus should not be rendered. This behavior
is designed to support applications with multiple views, where some of the
views are not rendered all the time.

A ViewportWindow can be `selected`. In applications with multiple viewports
sharing one windows canvas, this is usually used to distinguish a viewport
by drawing its border in some highlighted color, and give feedback to the
user about the viewport recieving interaction events.
*/
public class ViewportWindow extends PresentationElement
{
    // Occupancy specification with respect to containing canvas
    protected double viewportStartXPercent;
    protected double viewportStartYPercent;
    protected double viewportSizeXPercent;
    protected double viewportSizeYPercent;

    protected int viewportBorder; // In pixels

    protected boolean selected;
    protected boolean active;

    public ViewportWindow()
    {
        viewportStartXPercent = 0.0;
        viewportStartYPercent = 0.0;
        viewportSizeXPercent = 1.0;
        viewportSizeYPercent = 1.0;

        viewportBorder = 2;

        selected = true;
        active = true;
    }

    public int getBorder()
    {
        return viewportBorder;
    }

    public void setBorder(int b)
    {
        viewportBorder = b;
    }

    public double getViewportStartXPercent()
    {
        return viewportStartXPercent;
    }

    public double getViewportStartYPercent()
    {
        return viewportStartYPercent;
    }

    public double getViewportSizeXPercent()
    {
        return viewportSizeXPercent;
    }

    public double getViewportSizeYPercent()
    {
        return viewportSizeYPercent;
    }

    public void setViewportStartXPercent(double viewportStartXPercent)
    {
        this.viewportStartXPercent = viewportStartXPercent;
    }

    public void setViewportStartYPercent(double viewportStartYPercent)
    {
        this.viewportStartYPercent = viewportStartYPercent;
    }

    public void setViewportSizeXPercent(double viewportSizeXPercent)
    {
        this.viewportSizeXPercent = viewportSizeXPercent;
    }

    public void setViewportSizeYPercent(double viewportSizeYPercent)
    {
        this.viewportSizeYPercent = viewportSizeYPercent;
    }

    public void setSelected(boolean val)
    {
        selected = val;
    }

    public boolean getSelected()
    {
        return selected;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean val)
    {
        active = val;
    }

    /**
    Given a point (x, y) on viewport's percent space, this method determines
    if the point is inside the viewport window.
    */
    public boolean inside(double x, double y)
    {
        if ( x >= viewportStartXPercent &&
             x <= viewportStartXPercent + viewportSizeXPercent &&
             y >= viewportStartYPercent &&
             y <= viewportStartYPercent + viewportSizeYPercent ) {
            return true;
        }
        return false;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
