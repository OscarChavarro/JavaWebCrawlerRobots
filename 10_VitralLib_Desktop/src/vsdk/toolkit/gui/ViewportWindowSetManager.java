//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - June 22 2008 - Oscar Chavarro: Original base version (promoted from   =
//=   previous "ViewportOrganizer" class at SceneEditorApplication example).=
//===========================================================================

package vsdk.toolkit.gui;

// Basic Java classes
import java.util.ArrayList;

// VitralSDK classes

public class ViewportWindowSetManager extends PresentationElement
{
    private int selectedViewIndex;
    private int globalViewportXSize;
    private int globalViewportYSize;
    private int viewOrderStyle;
    private boolean fullViewport;
    private ArrayList<ViewportWindow> views;

    public ViewportWindowSetManager()
    {
        globalViewportXSize = 0;
        globalViewportYSize = 0;
        fullViewport = false;
        viewOrderStyle = 0;
        selectedViewIndex = 0;
        views = new ArrayList<ViewportWindow>();
    }

    public ArrayList<ViewportWindow> getViews()
    {
        return views;
    }

    public int getViewOrderStyle()
    {
        return viewOrderStyle;
    }

    public void setViewOrderStyle(int o)
    {
        viewOrderStyle = o;
    }

    public boolean isFullViewportScreen()
    {
        return fullViewport;
    }

    public void setFullViewportScreen(boolean val)
    {
        fullViewport = val;
    }

    public void toogleFullViewportScreen()
    {
        if ( fullViewport ) {
            fullViewport = false;
        }
        else {
            fullViewport = true;
        }
    }

    public int getSelectedViewIndex()
    {
        return selectedViewIndex;
    }

    public void setSelectedViewIndex(int i)
    {
        selectedViewIndex = i;
    }

    public void setGlobalViewportXSize(int x)
    {
        globalViewportXSize = x;
    }

    public void setGlobalViewportYSize(int y)
    {
        globalViewportYSize = y;
    }

    public int getGlobalViewportXSize()
    {
        return globalViewportXSize;
    }

    public int getGlobalViewportYSize()
    {
        return globalViewportYSize;
    }

    public ViewportWindow getSelectedViewFromPointerPosition(int x, int y, boolean changeSelection)
    {
        ViewportWindow view;
        ViewportWindow theView = null;

        //-----------------------------------------------------------------
        int i;
        double xpercent;
        double ypercent;

        xpercent = ((double)x) / ((double)getGlobalViewportXSize());
        ypercent = 1-((double)y) / ((double)getGlobalViewportYSize());

        for ( i = 0; i < views.size(); i++ ) {
            view = views.get(i);
            if ( view.isActive() && view.inside(xpercent, ypercent) ) {
                if ( changeSelection ) {
                    view.setSelected(true);
                    setSelectedViewIndex(i);
                }
                theView = view;
            }
            else {
                if ( changeSelection ) {
                    view.setSelected(false);
                }
            }
        }
        return theView;
    }

    private static void doLayout1(ViewportWindow view)
    {
        view.setActive(true);
        view.setViewportStartXPercent(0.0);
        view.setViewportStartYPercent(0.0);
        view.setViewportSizeXPercent(1.0);
        view.setViewportSizeYPercent(1.0);
    }

    private static void doLayout2(ArrayList <ViewportWindow> views, int style)
    {
        views.get(0).setActive(true);
        views.get(1).setActive(true);
        if ( style == 0 ) {
            views.get(0).setViewportStartXPercent(0.0);
            views.get(0).setViewportStartYPercent(0.0);
            views.get(0).setViewportSizeXPercent(0.5);
            views.get(0).setViewportSizeYPercent(1.0);

            views.get(1).setViewportStartXPercent(0.5);
            views.get(1).setViewportStartYPercent(0.0);
            views.get(1).setViewportSizeXPercent(0.5);
            views.get(1).setViewportSizeYPercent(1.0);
        }
        else {
            views.get(0).setViewportStartXPercent(0.0);
            views.get(0).setViewportStartYPercent(0.5);
            views.get(0).setViewportSizeXPercent(1.0);
            views.get(0).setViewportSizeYPercent(0.5);

            views.get(1).setViewportStartXPercent(0.0);
            views.get(1).setViewportStartYPercent(0.0);
            views.get(1).setViewportSizeXPercent(1.0);
            views.get(1).setViewportSizeYPercent(0.5);
        }
    }

    private static void doLayout3(ArrayList <ViewportWindow> views, int style)
    {
        //-----------------------------------------------------------------
        double Pstart[][] = new double[3][2];
        double Psize[][] = new double[3][2];
        double p00 = 0;
        double p50 = 0.5;
        double p33 = 1.0/3.0;
        double p66 = 2.0/3.0;
        double p100 = 1;
        int i;

        //-----------------------------------------------------------------
        switch ( style % 6 ) {
          case 0:
            Pstart[0][0] = p00; Pstart[0][1] = p00;
            Pstart[1][0] = p00; Pstart[1][1] = p50;
            Pstart[2][0] = p50; Pstart[2][1] = p00;
            Psize[0][0] = p50; Psize[0][1] = p50;
            Psize[1][0] = p50; Psize[1][1] = p50;
            Psize[2][0] = p50; Psize[2][1] = p100;
            break;
          case 1:
            Pstart[0][0] = p00; Pstart[0][1] = p00;
            Pstart[1][0] = p50; Pstart[1][1] = p00;
            Pstart[2][0] = p50; Pstart[2][1] = p50;
            Psize[0][0] = p50; Psize[0][1] = p100;
            Psize[1][0] = p50; Psize[1][1] = p50;
            Psize[2][0] = p50; Psize[2][1] = p50;
            break;
          case 2:
            Pstart[0][0] = p00; Pstart[0][1] = p00;
            Pstart[1][0] = p50; Pstart[1][1] = p00;
            Pstart[2][0] = p00; Pstart[2][1] = p50;
            Psize[0][0] = p50; Psize[0][1] = p50;
            Psize[1][0] = p50; Psize[1][1] = p50;
            Psize[2][0] = p100; Psize[2][1] = p50;
            break;
          case 3:
            Pstart[0][0] = p00; Pstart[0][1] = p00;
            Pstart[1][0] = p00; Pstart[1][1] = p50;
            Pstart[2][0] = p50; Pstart[2][1] = p50;
            Psize[0][0] = p100; Psize[0][1] = p50;
            Psize[1][0] = p50; Psize[1][1] = p50;
            Psize[2][0] = p50; Psize[2][1] = p50;
            break;
          case 4:
            Pstart[0][0] = p00; Pstart[0][1] = p00;
            Pstart[1][0] = p33; Pstart[1][1] = p00;
            Pstart[2][0] = p66; Pstart[2][1] = p00;
            Psize[0][0] = p33; Psize[0][1] = p100;
            Psize[1][0] = p33; Psize[1][1] = p100;
            Psize[2][0] = p33; Psize[2][1] = p100;
            break;
          case 5:
            Pstart[0][0] = p00; Pstart[0][1] = p00;
            Pstart[1][0] = p00; Pstart[1][1] = p33;
            Pstart[2][0] = p00; Pstart[2][1] = p66;
            Psize[0][0] = p100; Psize[0][1] = p33;
            Psize[1][0] = p100; Psize[1][1] = p33;
            Psize[2][0] = p100; Psize[2][1] = p33;
            break;
        }
        //-----------------------------------------------------------------
        for ( i = 0; i < 3; i++ ) {
            views.get(i).setActive(true);
            views.get(i).setViewportStartXPercent(Pstart[i][0]);
            views.get(i).setViewportStartYPercent(Pstart[i][1]);
            views.get(i).setViewportSizeXPercent(Psize[i][0]);
            views.get(i).setViewportSizeYPercent(Psize[i][1]);
        }
    }

    private static void doLayout4(ArrayList <ViewportWindow> views, int style)
    {
        //-----------------------------------------------------------------
        double Pstart[][] = new double[4][2];
        double Psize[][] = new double[4][2];
        double p00 = 0;
        double p25 = 0.25;
        double p33 = 1.0/3.0;
        double p50 = 0.5;
        double p66 = 2.0/3.0;
        double p75 = 0.25;
        double p100 = 1;
        int i;

        //-----------------------------------------------------------------
        switch ( style % 7 ) {
          case 0:
            Pstart[0][0] = p00; Pstart[0][1] = p00;
            Pstart[3][0] = p00; Pstart[3][1] = p33;
            Pstart[2][0] = p00; Pstart[2][1] = p66;
            Pstart[1][0] = p33; Pstart[1][1] = p00;
            Psize[0][0] = p33; Psize[0][1] = p33;
            Psize[3][0] = p33; Psize[3][1] = p33;
            Psize[2][0] = p33; Psize[2][1] = p33;
            Psize[1][0] = p66; Psize[1][1] = p100;
            break;
          case 1:
            Pstart[0][0] = p00; Pstart[0][1] = p00;
            Pstart[1][0] = p50; Pstart[1][1] = p00;
            Pstart[2][0] = p00; Pstart[2][1] = p50;
            Pstart[3][0] = p50; Pstart[3][1] = p50;
            Psize[0][0] = p50; Psize[0][1] = p50;
            Psize[1][0] = p50; Psize[1][1] = p50;
            Psize[2][0] = p50; Psize[2][1] = p50;
            Psize[3][0] = p50; Psize[3][1] = p50;
            break;
          case 2:
            Pstart[0][0] = p00; Pstart[0][1] = p00;
            Pstart[1][0] = p50; Pstart[1][1] = p00;
            Pstart[2][0] = p50; Pstart[2][1] = p33;
            Pstart[3][0] = p50; Pstart[3][1] = p66;
            Psize[0][0] = p50; Psize[0][1] = p100;
            Psize[1][0] = p50; Psize[1][1] = p33;
            Psize[2][0] = p50; Psize[2][1] = p33;
            Psize[3][0] = p50; Psize[3][1] = p33;
            break;
          case 3:
            Pstart[0][0] = p00; Pstart[0][1] = p00;
            Pstart[1][0] = p33; Pstart[1][1] = p00;
            Pstart[2][0] = p66; Pstart[2][1] = p00;
            Pstart[3][0] = p00; Pstart[3][1] = p50;
            Psize[0][0] = p33; Psize[0][1] = p50;
            Psize[1][0] = p33; Psize[1][1] = p50;
            Psize[2][0] = p33; Psize[2][1] = p50;
            Psize[3][0] = p100; Psize[3][1] = p50;
            break;
          case 4:
            Pstart[0][0] = p00; Pstart[0][1] = p00;
            Pstart[1][0] = p00; Pstart[1][1] = p50;
            Pstart[2][0] = p33; Pstart[2][1] = p50;
            Pstart[3][0] = p66; Pstart[3][1] = p50;
            Psize[0][0] = p100; Psize[0][1] = p50;
            Psize[1][0] = p33; Psize[1][1] = p50;
            Psize[2][0] = p33; Psize[2][1] = p50;
            Psize[3][0] = p33; Psize[3][1] = p50;
            break;
        }
        //-----------------------------------------------------------------
        for ( i = 0; i < 4; i++ ) {
            views.get(i).setActive(true);
            views.get(i).setViewportStartXPercent(Pstart[i][0]);
            views.get(i).setViewportStartYPercent(Pstart[i][1]);
            views.get(i).setViewportSizeXPercent(Psize[i][0]);
            views.get(i).setViewportSizeYPercent(Psize[i][1]);
        }
    }

    private static int doLayout(ArrayList <ViewportWindow> views, int selectedForFullScreen, int style)
    {
        int i, selected = 0;
        ViewportWindow view;

        if ( selectedForFullScreen >= 0 &&
             selectedForFullScreen < views.size() ) {
            for ( i = 0; i < views.size(); i++ ) {
                view = views.get(i);
                if ( i == selectedForFullScreen ) {
                    view.setActive(true);
                    view.setSelected(true);
                    doLayout1(view);
                    selected = i;
                }
                else {
                    view.setActive(false);
                    view.setSelected(false);
                }
            }
        }
        else {
            //-----------------------------------------------------------------
            boolean isSelected = false;
            for ( i = 0; i < views.size(); i++ ) {
                view = views.get(i);
                if ( !isSelected && view.getSelected() ) {
                    isSelected = true;
                    selected = i;
                    continue;
                }
                else if ( isSelected ) {
                    view.setSelected(false);
                }
            }
            if ( !isSelected && views.size() > 0 ) {
                views.get(0).setSelected(true);
                selected = 0;
            }

            //-----------------------------------------------------------------
            switch ( views.size() ) {
              case 0:
                System.out.println("Warning: NO VIEWS TO ORDER!");
                break;
            case 1:  doLayout1(views.get(0)); break;
              case 2:  doLayout2(views, style%2); break;
              case 3:  doLayout3(views, style%6); break;
              case 4:  doLayout4(views, style%5); break;
              default:
                System.out.println("Warning: Not supported layout, selecting first view full screen!");
                doLayout1(views.get(0));
                break;
            }
        }
        return selected;
    }

    public void updateLayout()
    {
        setSelectedViewIndex(doLayout(views, fullViewport?selectedViewIndex:-1, viewOrderStyle));
    }

    public int countActiveViews()
    {
        int i;
        ViewportWindow view;
        int n = 0;
         
        for ( i = 0; i < getViews().size(); i++ ) {
            view = getViews().get(i);

            if ( view.isActive() ) {
                n++;
            }
        }
        return n;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
