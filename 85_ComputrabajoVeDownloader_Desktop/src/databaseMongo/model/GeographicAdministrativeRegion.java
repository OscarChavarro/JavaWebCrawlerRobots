package databaseMongo.model;

import java.util.TreeSet;
import webcrawler.TaggedHtml;

/**
*/
public class GeographicAdministrativeRegion 
    implements Comparable<GeographicAdministrativeRegion> 
{
    private final TreeSet<String> subRegions;
    private String nameSpa;

    public GeographicAdministrativeRegion()
    {
        subRegions = new TreeSet<String>();
    }
            
    /**
     * @return the subRegions
     */
    public TreeSet<String> getSubRegions() {
        return subRegions;
    }

    /**
     * @return the nameSpa
     */
    public String getNameSpa() {
        return nameSpa;
    }

    /**
     * @param nameSpa the nameSpa to set
     */
    public void setNameSpa(String nameSpa) {
        this.nameSpa = TaggedHtml.trimSpaces(nameSpa);
    }

    public void insertSubarea(String subarea) {
        subarea = TaggedHtml.trimSpaces(subarea);
        if ( !subRegions.contains(subarea) ) {
            subRegions.add(subarea);
        }
    }

    @Override
    public String toString()
    {
        String msg;
        
        msg = "* Area [" + nameSpa + "]: \n";
        for ( String s : subRegions ) {
            msg += "  - " + s + "\n";
        }
        
        return msg;
    }

    @Override
    public int compareTo(GeographicAdministrativeRegion o) {
        return this.nameSpa.compareTo(o.nameSpa);
    }
}
