package databaseMongo.model;

import java.net.InetAddress;

public class EmailElement implements Comparable<EmailElement>
{
    private String domainName;
    private long count;
    private boolean valid;

    public EmailElement()
    {
	count = 0;
	valid = false;
    }

    public boolean getValid()
    {
	return valid;
    }

    public void setValid(boolean v)
    {
	valid = v;
    }
    
    public String getDomainName()
    {
	return domainName;
    }

    public void setDomainName(String n)
    {
	domainName = n;
    }

    public long getCount()
    {
	return count;
    }

    public void setCount(long c)
    {
	count = c;
    }

    public void computeValidity()
    {
        InetAddress arrayOfIps[];
	System.out.println("    ** Checking domain: " + domainName);
	
	try {
   	    arrayOfIps = InetAddress.getAllByName(domainName);
	    valid = true;
	}
	catch ( Exception e ) {
	    System.out.println("    **** Invalid domain name");
	    valid = false;
	}
    }
    
    @Override
    public int compareTo(EmailElement other)
    {
	if ( this.valid && !other.valid ) {
	    return 1;
	}
	else if ( !this.valid && other.valid ) {
	    return -1;
	}
	else {
            if ( this.count > other.count ) {
                return 1;
            }
            else if ( this.count < other.count ) {
                return -1;
            }
            else {
                return this.domainName.compareTo(other.domainName);
            }
        }
    }
}
