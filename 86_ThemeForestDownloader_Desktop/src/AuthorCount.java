/**
*/
public class AuthorCount {
    private String author;
    private long count;

    public AuthorCount(String a)
    {
        author = a;
        count = 1;
    }

    public void increment()
    {
        count++;
    }

    /**
     * @return the author
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }

    /**
     * @return the count
     */
    public long getCount()
    {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(long count)
    {
        this.count = count;
    }
}
