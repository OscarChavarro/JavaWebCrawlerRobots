/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package databaseMongo.model;

import vsdk.toolkit.common.VSDK;

/**
 *
 * @author oscar
 */
public class NameElement implements Comparable<NameElement> {
    private String name;
    private long apareancesCount;
    private double positionAverage;
    private double positionCount;

    public NameElement() {
        apareancesCount = 0;
        positionAverage = 0;
        positionCount = 0;
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
}
