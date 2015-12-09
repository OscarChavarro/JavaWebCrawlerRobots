
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import vsdk.toolkit.common.Entity;

import webcrawler.TaggedHtml;

/**
*/
public class Project extends Entity implements Comparable<Project> {
    private String id;
    private String url;
    private String author;
    private String price;
    private String salesCount;
    private String title;
    private String creationDate;
    private String lastUpdateDate;
    private String widgetReady;
    private String compatibleBrowsers;
    private String compatibleWith;
    private String softwareVersion;
    private String themeForestFiles;
    private String columns;
    private String documentation;
    private String layout;
    private String tags;
    private String highResolution;
    private String framework;
    private String numberOfComments;
    private String ranking;
    private String numberOfRankings;

    private final ArrayList<String> categories;

    public Project()
    {
        categories = new ArrayList<String>();
        ranking = "-1";
        numberOfRankings = "0";
    }

    private String reportDataColumns(
        String var,
        TreeSet<String> group)
    {
        String c = "";
        int i = 0;
        for ( String sc : group ) {
            if ( var != null && sc.contains(var) ) {
                c += "1";
            }
            else {
                c += "0";
            }
            if ( i < group.size() - 1 ) {
                c += "\t";
            }
            i++;
        }
        return c;
    }

    public void addCategory(String c)
    {
        getCategories().add(TaggedHtml.trimQuotes(c));
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = TaggedHtml.trimQuotes(url);
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author.replace("/user/", "");
    }

    /**
     * @return the price
     */
    public String getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(String price) {
        this.price = price;
    }

    /**
     * @return the salesCount
     */
    public String getSalesCount() {
        return salesCount;
    }

    /**
     * @param salesCount the salesCount to set
     */
    public void setSalesCount(String salesCount) {
        this.salesCount = salesCount;
    }

        /**
     * @return the numberOfComments
     */
    public String getNumberOfComments()
    {
        return numberOfComments;
    }

    /**
     * @param numberOfComments the numberOfComments to set
     */
    public void setNumberOfComments(String numberOfComments)
    {
        this.numberOfComments = numberOfComments;
    }

    /**
     * @return the ranking
     */
    public String getRanking()
    {
        return ranking;
    }

    /**
     * @param ranking the ranking to set
     */
    public void setRanking(String ranking)
    {
        this.ranking = ranking;
    }

    /**
     * @return the numberOfRankings
     */
    public String getNumberOfRankings()
    {
        return numberOfRankings;
    }

    /**
     * @param numberOfRankings the numberOfRankings to set
     */
    public void setNumberOfRankings(String numberOfRankings)
    {
        this.numberOfRankings = numberOfRankings;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the categories
     */
    public ArrayList<String> getCategories()
    {
        return categories;
    }

    private String reportCategoryColumns(TreeSet<String> group) {
        int i = 0;
        int j;
        String c = "";
        for ( String sc : group ) {
            boolean contained = false;
            for ( j = 0; j < getCategories().size(); j++ ) {
                if ( getCategories().get(j).equals(sc) ) {
                    contained = true;
                    break;
                }
            }
            if ( contained ) {
                c += "1";
            }
            else {
                c += "0";
            }
            if ( i < group.size() - 1 ) {
                c += "\t";
            }
            i++;
        }
        return c;
    }

    /**
     * @return the creationDate
     */
    public String getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the lastUpdateDate
     */
    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * @param lastUpdateDate the lastUpdateDate to set
     */
    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    /**
     * @return the widgetReady
     */
    public String getWidgetReady() {
        return widgetReady;
    }

    /**
     * @param widgetReady the widgetReady to set
     */
    public void setWidgetReady(String widgetReady) {
        this.widgetReady = widgetReady;
    }

    /**
     * @return the compatibleBrowsers
     */
    public String getCompatibleBrowsers() {
        return compatibleBrowsers;
    }

    /**
     * @param compatibleBrowsers the compatibleBrowsers to set
     */
    public void setCompatibleBrowsers(String compatibleBrowsers) {
        this.compatibleBrowsers = compatibleBrowsers;
    }

    /**
     * @return the compatibleWith
     */
    public String getCompatibleWith() {
        return compatibleWith;
    }

    /**
     * @param compatibleWith the compatibleWith to set
     */
    public void setCompatibleWith(String compatibleWith) {
        this.compatibleWith = compatibleWith;
    }

    /**
     * @return the softwareVersion
     */
    public String getSoftwareVersion() {
        return softwareVersion;
    }

    /**
     * @param softwareVersion the softwareVersion to set
     */
    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    /**
     * @return the themeForestFiles
     */
    public String getThemeForestFiles() {
        return themeForestFiles;
    }

    /**
     * @param themeForestFiles the themeForestFiles to set
     */
    public void setThemeForestFiles(String themeForestFiles) {
        this.themeForestFiles = themeForestFiles;
    }

    /**
     * @return the columns
     */
    public String getColumns() {
        return columns;
    }

    /**
     * @param columns the columns to set
     */
    public void setColumns(String columns) {
        this.columns = columns;
    }

    /**
     * @return the documentation
     */
    public String getDocumentation() {
        return documentation;
    }

    /**
     * @param documentation the documentation to set
     */
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    /**
     * @return the layout
     */
    public String getLayout() {
        return layout;
    }

    /**
     * @param layout the layout to set
     */
    public void setLayout(String layout) {
        this.layout = layout;
    }

    /**
     * @return the tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * @return the highResolution
     */
    public String getHighResolution() {
        return highResolution;
    }

    /**
     * @param highResolution the highResolution to set
     */
    public void setHighResolution(String highResolution) {
        this.highResolution = highResolution;
    }

    /**
     * @return the framework
     */
    public String getFramework() {
        return framework;
    }

    /**
     * @param framework the framework to set
     */
    public void setFramework(String framework) {
        this.framework = framework;
    }

    @Override
    public int compareTo(Project o) {
        return getId().compareTo(o.getId());
    }

    @Override
    public String toString()
    {
        String c;
        if ( getCategories().isEmpty() ) {
            c = "<EMPTY>";
        }
        else {
            int i;
            c = "";
            for ( i = 0; i < getCategories().size(); i++ ) {
                c = c + getCategories().get(i);
                if ( i < getCategories().size() - 1 ) {
                    c += ";";
                }
            }
        }

        /*
        return "Project id = " + id + "\n" +
            "  - Title: " + title + "\n" +
            "  - Url: " + url + "\n" +
            "  - Author: " + author + "\n" +
            "  - Price: " + price + "\n" +
            "  - Sales count: " + salesCount + "\n" +
            "  - Categories: " + c + "\n";
        */

        String price2 = price.replace("$", "");

        return TaggedHtml.trimQuotes(id) + "\t" +
            TaggedHtml.trimSpaces(title) + "\t" +
            url + "\t" +
            TaggedHtml.trimQuotes(author) + "\t" +
            price2 + "\t" +
            salesCount + "\t" +
            c;
    }

    public String toStringWithCategories(TreeSet<String> sortedCategories)
    {
        String c;

        c = reportCategoryColumns(sortedCategories);

        String price2 = price.replace("$", "");

        return TaggedHtml.trimQuotes(id) + "\t" +
            TaggedHtml.trimSpaces(title) + "\t" +
            "http://themeforest.com" + TaggedHtml.trimQuotes(url) + "\t" +
            TaggedHtml.trimQuotes(author) + "\t" +
            price2 + "\t" +
            salesCount + "\t" + c;
    }


    public String toStringExtended(TreeSet<String> sortedCategories)
    {
        String extended =
            creationDate + "\t" +
            lastUpdateDate + "\t" +
            ranking + "\t" +
            numberOfRankings + "\t" +
            widgetReady + "\t" +
            compatibleBrowsers + "\t" +
            compatibleWith + "\t" +
            softwareVersion + "\t" +
            themeForestFiles + "\t" +
            columns + "\t" +
            documentation + "\t" +
            layout + "\t" +
            tags + "\t" +
            getHighResolution() + "\t" +
            getFramework();

        String c;

        c = reportCategoryColumns(sortedCategories);

        String price2 = price.replace("$", "");

        return TaggedHtml.trimQuotes(id) + "\t" +
            TaggedHtml.trimSpaces(title) + "\t" +
            "http://themeforest.com" + TaggedHtml.trimQuotes(url) + "\t" +
            TaggedHtml.trimQuotes(author) + "\t" +
            price2 + "\t" +
            salesCount + "\t" + extended + "\t" + c ;
    }

    public String toStringComplete(
        TreeSet<String> sortedCategoriesValues,
        TreeSet<String> widgetReadyValues,
        TreeSet<String> compatibleBrowsersValues,
        TreeSet<String> compatibleWithValues,
        TreeSet<String> softwareVersionValues,
        TreeSet<String> themeForestFilesValues,
        TreeSet<String> columnsValues,
        TreeSet<String> documentationValues,
        TreeSet<String> layoutValues,
        TreeSet<String> tagsValues,
        TreeSet<String> highResolutionValues,
        TreeSet<String> frameworkValues,
        HashMap<String, AuthorCount> authorValues)
    {
        String extended =
            creationDate + "\t" +
            lastUpdateDate + "\t" +
            ranking + "\t" +
            numberOfRankings + "\t" +
            authorValues.get(author).getCount();
            //widgetReady + "\t" +
            //compatibleBrowsers + "\t" +
            //compatibleWith + "\t" +
            //softwareVersion + "\t" +
            //themeForestFiles + "\t" +
            //columns + "\t" +
            //documentation + "\t" +
            //layout + "\t" +
            //tags + "\t" +
            //highResolution + "\t" +
            //framework;

        String price2 = price.replace("$", "");

        return TaggedHtml.trimQuotes(id) + "\t" +
            TaggedHtml.trimSpaces(title) + "\t" +
            "http://themeforest.com" + TaggedHtml.trimQuotes(url) + "\t" +
            TaggedHtml.trimQuotes(author) + "\t" +
            price2 + "\t" +
            salesCount + "\t" + extended + "\t" +
            reportCategoryColumns(sortedCategoriesValues) + "\t" +
            reportDataColumns(widgetReady, widgetReadyValues) + "\t" +
            reportDataColumns(compatibleBrowsers, compatibleBrowsersValues) + "\t" +
            reportDataColumns(compatibleWith, compatibleWithValues) + "\t" +
            reportDataColumns(softwareVersion, softwareVersionValues) + "\t" +
            reportDataColumns(themeForestFiles, themeForestFilesValues) + "\t" +
            reportDataColumns(columns, columnsValues) + "\t" +
            reportDataColumns(documentation, documentationValues) + "\t" +
            reportDataColumns(layout, layoutValues) + "\t" +
            reportDataColumns(tags, tagsValues) + "\t" +
            reportDataColumns(highResolution, highResolutionValues) + "\t" +
            reportDataColumns(framework, frameworkValues) + "\t";
    }

}

