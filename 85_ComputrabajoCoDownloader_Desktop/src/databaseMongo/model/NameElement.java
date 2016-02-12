package databaseMongo.model;

import vsdk.toolkit.common.VSDK;

/**
*/
public class NameElement implements Comparable<NameElement> {
    private String name;
    private long apareancesCount;
    private double positionAverage;
    private double positionCount;
    private int knownToBe; // 0: unknown, 1: male, 2: female, 3: lastname
    public static final int GENRE_UNKNOWN = 0;
    public static final int GENRE_MALE = 1;
    public static final int GENRE_FEMALE = 2;
    public static final int GENRE_LASTNAME = 3;

    public NameElement() {
        apareancesCount = 0;
        positionAverage = 0;
        positionCount = 0;
        knownToBe = 0;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public void addElement(double position)
    {
        apareancesCount++;
        positionCount += position;
        positionAverage = ((double)positionCount) / ((double)apareancesCount);
        
    }

    /**
     * @return the apareancesCount
     */
    public long getApareancesCount() {
        return apareancesCount;
    }

    /**
     * @return the positionAverage
     */
    public double getPositionAverage() {
        return positionAverage;
    }
    
    @Override
    public String toString()
    {
        String msg;
        msg = name + ": N=" + apareancesCount + 
            ", P=" + VSDK.formatDouble(positionAverage);
        return msg;
    }

    @Override
    public int compareTo(NameElement o) 
    {
        
        if ( this.apareancesCount > o.apareancesCount ) {
            return 1;
        }
        else if ( this.apareancesCount < o.apareancesCount ) {
            return -1;
        }
        return this.name.compareTo(o.name);
        
        /*
        if ( this.positionAverage > o.positionAverage + 0.01 ) {
            return 1;
        }
        else if ( this.positionAverage < o.positionAverage - 0.01 ) {
            return -1;
        }
        
        return this.name.compareTo(o.name);        
        */
    }

    /**
     * @return the knownToBe
     */
    public int getKnownToBe() {
        return knownToBe;
    }

    /**
     * @param knownToBe the knownToBe to set
     */
    public void setKnownToBe(int knownToBe) {
        this.knownToBe = knownToBe;
    }
}
