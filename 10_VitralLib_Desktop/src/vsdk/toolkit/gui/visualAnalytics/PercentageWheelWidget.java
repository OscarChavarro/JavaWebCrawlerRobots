//===========================================================================
package vsdk.toolkit.gui.visualAnalytics;

// VSDK classes
import vsdk.toolkit.common.ColorRgb;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.gui.PresentationElement;

/**

*/
public class PercentageWheelWidget extends PresentationElement {
    private VisualVariableSet dataset;
    
    /// A number in the range (0.0, outerRadius)
    private double innerRadius;
    
    /// A number in the range (innerRadius, 1.0-borderWidth]
    private double outerRadius;
    
    /// Width of the wheel border in the range (0.0, 1.0)
    private double borderWidth;
    
    /// A number to split 360.0 degrees on used to draw wheel
    private double aproximationSteps;
    
    /// Geometric configuration
    private Vector3D position;
    private double scale;
    
    /// Interaction control. -1 means "no selection"
    private int selectedSector;
    private int highligtedSector;
    private String defaultTitle;
    
    /// Graphic design configuration
    private ColorRgb borderBackgroundColor;
    private ColorRgb borderSelectedColor;
    private ColorRgb borderHighlightedColor;
    private ColorRgb sectorForegroundColor;
    private ColorRgb sectorLineColor;
    private ColorRgb wheelBackgroundColor;
    private ColorRgb trackLineColor;
    private ColorRgb selectedSectorColor;
    private ColorRgb highlightedSectorColor;

    public PercentageWheelWidget(VisualVariableSet dataset)
    {
        this.dataset = dataset;
        innerRadius = 0.2; // 20%
        borderWidth = 0.07; // 7%
        outerRadius = 1.0 - borderWidth;
        
        // Should be entirely divisible by dataset size!
        aproximationSteps = 160.0;

        borderBackgroundColor = new ColorRgb(0.38671, 0.38671, 0.38671);
        sectorLineColor = new ColorRgb(0.1289, 0.1289, 0.1289);
        sectorForegroundColor = new ColorRgb(1, 1, 0);
        wheelBackgroundColor = new ColorRgb(43.0/256.0, 43.0/256.0, 43.0/256.0);
        trackLineColor = new ColorRgb(60.0/256.0, 60.0/256.0, 60.0/256.0);
        selectedSectorColor = new ColorRgb(1, 1, 1);
        highlightedSectorColor = new ColorRgb(1, 1, 0.5);
        borderSelectedColor = new ColorRgb(1, 1, 1);
        borderHighlightedColor  = new ColorRgb(146.0/256.0, 146.0/256.0, 146.0/256.0);

        position = new Vector3D(0, 0, 0);
    
        scale = 1.0;
        
        selectedSector = -1;
        highligtedSector = -1;
        
        defaultTitle = "Select Issue";
    }

    /**
    @return the innerRadius
    */
    public double getInnerRadius() {
        return innerRadius;
    }

    /**
    @param innerRadius the innerRadius to set
    */
    public void setInnerRadius(double innerRadius) {
        this.innerRadius = innerRadius;
    }

    /**
    @return the outerRadius
    */
    public double getOuterRadius() {
        return outerRadius;
    }

    /**
    @param outerRadius the outerRadius to set
    */
    public void setOuterRadius(double outerRadius) {
        this.outerRadius = outerRadius;
    }

    /**
    @return the aproximationSteps
    */
    public double getAproximationSteps() {
        return aproximationSteps;
    }

    /**
    @param aproximationStep the aproximationSteps to set
    */
    public void setAproximationSteps(double aproximationStep) {
        this.aproximationSteps = aproximationStep;
    }

    /**
    @return the borderWidth
    */
    public double getBorderWidth() {
        return borderWidth;
    }

    /**
    @param boderWidth the borderWidth to set
    */
    public void setBorderWidth(double boderWidth) {
        this.borderWidth = boderWidth;
    }

    /**
    @return the borderBackgroundColor
    */
    public ColorRgb getBorderBackgroundColor() {
        return borderBackgroundColor;
    }

    /**
    @param borderBackgroundColor the borderBackgroundColor to set
    */
    public void setBorderBackgroundColor(ColorRgb borderBackgroundColor) {
        this.borderBackgroundColor = borderBackgroundColor;
    }

    /**
    @return the dataset
    */
    public VisualVariableSet getDataset() {
        return dataset;
    }

    /**
    @param dataset the dataset to set
    */
    public void setDataset(VisualVariableSet dataset) {
        this.dataset = dataset;
    }

    /**
    @return the sectorLineColor
    */
    public ColorRgb getSectorLineColor() {
        return sectorLineColor;
    }

    /**
    @param sectorLineColor the sectorLineColor to set
    */
    public void setSectorLineColor(ColorRgb sectorLineColor) {
        this.sectorLineColor = sectorLineColor;
    }

    /**
    @return the sectorForegroundColor
    */
    public ColorRgb getSectorForegroundColor() {
        return sectorForegroundColor;
    }

    /**
    @param sectorBackgroundColor the sectorForegroundColor to set
    */
    public void setSectorForegroundColor(ColorRgb sectorBackgroundColor) {
        this.sectorForegroundColor = sectorBackgroundColor;
    }

    /**
    @return the wheelBackgroundColor
    */
    public ColorRgb getWheelBackgroundColor() {
        return wheelBackgroundColor;
    }

    /**
    @param wheelBackgroundColor the wheelBackgroundColor to set
    */
    public void setWheelBackgroundColor(ColorRgb wheelBackgroundColor) {
        this.wheelBackgroundColor = wheelBackgroundColor;
    }

    /**
    @return the trackLineColor
    */
    public ColorRgb getTrackLineColor() {
        return trackLineColor;
    }

    /**
    @param trackLineColor the trackLineColor to set
    */
    public void setTrackLineColor(ColorRgb trackLineColor) {
        this.trackLineColor = trackLineColor;
    }

    /**
    @return the position
    */
    public Vector3D getPosition() {
        return position;
    }

    /**
    @param position the position to set
    */
    public void setPosition(Vector3D position) {
        this.position = position;
    }

    /**
    @return the scale
    */
    public double getScale() {
        return scale;
    }

    /**
    @param scale the scale to set
    */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
    @return the selectedSector
    */
    public int getSelectedSector() {
        return selectedSector;
    }

    /**
    @param selectedSector the selectedSector to set
    */
    public void setSelectedSector(int selectedSector) {
        this.selectedSector = selectedSector;
    }

    /**
    @return the highligtedSector
    */
    public int getHighligtedSector() {
        return highligtedSector;
    }

    /**
    @param highligtedSector the highligtedSector to set
    */
    public void setHighligtedSector(int highligtedSector) {
        this.highligtedSector = highligtedSector;
    }

    /**
    @return the selectedSectorColor
    */
    public ColorRgb getSelectedSectorColor() {
        return selectedSectorColor;
    }

    /**
    @param selectedSectorColor the selectedSectorColor to set
    */
    public void setSelectedSectorColor(ColorRgb selectedSectorColor) {
        this.selectedSectorColor = selectedSectorColor;
    }

    /**
    @return the highlightedSectorColor
    */
    public ColorRgb getHighlightedSectorColor() {
        return highlightedSectorColor;
    }

    /**
    @param highlightedSectorColor the highlightedSectorColor to set
    */
    public void setHighlightedSectorColor(ColorRgb highlightedSectorColor) {
        this.highlightedSectorColor = highlightedSectorColor;
    }

    /**
    @return the borderSelectedColor
    */
    public ColorRgb getBorderSelectedColor() {
        return borderSelectedColor;
    }

    /**
    @param borderSelectedColor the borderSelectedColor to set
    */
    public void setBorderSelectedColor(ColorRgb borderSelectedColor) {
        this.borderSelectedColor = borderSelectedColor;
    }

    /**
    @return the borderHighlightedColor
    */
    public ColorRgb getBorderHighlightedColor() {
        return borderHighlightedColor;
    }

    /**
    @param borderHighlightedColor the borderHighlightedColor to set
    */
    public void setBorderHighlightedColor(ColorRgb borderHighlightedColor) {
        this.borderHighlightedColor = borderHighlightedColor;
    }

    /**
    @return the defaultTitle
    */
    public String getDefaultTitle() {
        return defaultTitle;
    }

    /**
    @param defaultTitle the defaultTitle to set
    */
    public void setDefaultTitle(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
