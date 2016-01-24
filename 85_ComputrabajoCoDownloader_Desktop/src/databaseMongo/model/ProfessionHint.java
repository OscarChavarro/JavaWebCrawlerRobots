package databaseMongo.model;

/**
*/
public class ProfessionHint implements Comparable<ProfessionHint> {
    private String content;
    private long apareancesCount;

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the apareancesCount
     */
    public long getApareancesCount() {
        return apareancesCount;
    }

    /**
     * @param apareancesCount the apareancesCount to set
     */
    public void setApareancesCount(long apareancesCount) {
        this.apareancesCount = apareancesCount;
    }

    public void incrementCount() {
        apareancesCount++;
    }
    
    @Override
    public String toString()
    {
        return "" + apareancesCount + ": " + content;
    }

    @Override
    public int compareTo(ProfessionHint o) {
        if ( this.apareancesCount < o.apareancesCount ) {
            return -1;
        }
        else if ( this.apareancesCount > o.apareancesCount ) {
            return 1;
        }
        return this.content.compareTo(o.content);
    }
}
