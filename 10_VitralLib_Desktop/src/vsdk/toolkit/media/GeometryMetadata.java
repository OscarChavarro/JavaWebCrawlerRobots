//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 23 2007 - Oscar Chavarro: Original base version                   =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [BUST2005] Bustos, Benjamin. Keim, Daniel A. Saupe, Dietmar. Schreck,   =
//=     Tobias. Vranic, Dejan V. "Feature-Based Similarity Search in 3D     =
//=     Databases". ACM Computing Surveys, Vol. 37, No 4, December 2005,    =
//=     pp. 345-387.                                                        =
//= [FUNK2003], Funkhouser, Thomas.  Min, Patrick. Kazhdan, Michael. Chen,  =
//=     Joyce. Halderman, Alex. Dobkin, David. Jacobs, David. "A Search     =
//=     Engine for 3D Models", ACM Transactions on Graphics, Vol 22. No1.   =
//=     January 2003. Pp. 83-105                                            =
//===========================================================================

package vsdk.toolkit.media;

import java.util.ArrayList;

/**
This class is a container for a set of different ShapeDescriptor's for an
associated Geometry. This class is designed to contain non-geometric data
associated with geometry, or "metadata".  Such a metadata is usefull for
indexing, querying and matching of 3D models, as described in [FUNK2003].
*/
public class GeometryMetadata extends MediaEntity
{
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20070523L;

    private static long lastId = 0;

    private long id;
    private String objectFilename;
    private ArrayList<ShapeDescriptor> descriptorsList;

    /**
    Given a pair of GeometryMetadata elements, this method computes the
    MinskowskiDistance between feature vectors as described in [BUST2005],
    if shape descriptors are "comparable".

    Two sets of shape descriptors are "comparable" if they are of the same
    type, in the same order and with the same number of features.

    If given shape descriptors are not comparable, this method return infinite
    distance.
    @param other geometry metadata to be compared with current (`this`) set
    @param s Minskowski factor. If s == 1, this method returns the Manhattan
    distance in the Nth-dimensional space.  If s == 2, this method returns
    the euclidean distance in the Nth-dimensional space.
    */
    public double doMinskowskiDistance(GeometryMetadata other, double s, String subGroup)
    {
        int i, j;
        ShapeDescriptor a = null, b = null, aa, bb;
        double av[], bv[];
        double acum = 0;

        //-----------------------------------------------------------------
        for ( i = 0; i < this.descriptorsList.size(); i++ ) {
            aa = this.descriptorsList.get(i);
            if ( aa.getLabel().equals(subGroup) ) {
                a = aa;
            }
        }
        if ( a == null ) {
            return Double.MAX_VALUE;
        }

        //-----------------------------------------------------------------
        for ( i = 0; i < other.descriptorsList.size(); i++ ) {
            bb = other.descriptorsList.get(i);
            if ( bb.getLabel().equals(subGroup) ) {
                b = bb;
            }
        }
        if ( b == null ) {
            return Double.MAX_VALUE;
        }

        //-----------------------------------------------------------------
        av = a.getFeatureVector();
        bv = b.getFeatureVector();
        if ( av.length != bv.length ) {
            return Double.MAX_VALUE;
        }
        for ( j = 0; j < av.length; j++ ) {
            acum += Math.pow(Math.abs(av[j] - bv[j]), s);
        }

        return Math.pow(acum, 1/s);
    }


    public GeometryMetadata()
    {
        lastId++;
        id = lastId;
        objectFilename = null;
        descriptorsList = new ArrayList<ShapeDescriptor>();
    }

    public void setId(long id)
    {
        this.id = id;
        if ( lastId < id ) lastId = id;
    }

    public long getId()
    {
        return id;
    }

    public void setFilename(String filename)
    {
        if ( filename != null && filename.length() > 0 ) {
            objectFilename = filename;
        }
        else {
            objectFilename = null;
        }
    }

    public String getFilename()
    {
        return objectFilename;
    }

    public ArrayList<ShapeDescriptor> getDescriptors()
    {
        return descriptorsList;
    }

    public ShapeDescriptor getDescriptorByName(String name)
    {
        int i;
        ShapeDescriptor s;

        for ( i = 0; i < descriptorsList.size(); i++ ) {
            s = descriptorsList.get(i);
            if ( s.getLabel().equals(name) ) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        String msg = objectFilename;
        msg += "\n    . " + descriptorsList.size() + " shape descriptors\n";
        int i;
        for ( i = 0; i < descriptorsList.size(); i++ ) {
            msg += "        . " + descriptorsList.get(i).getClass().getName();
        }
        return msg;
    }

    @Override
    public void finalize()
    {
        int i;
        for ( i = 0; i < descriptorsList.size(); i++ ) {
            descriptorsList.set(i, null);
        }
        for ( i = 0; descriptorsList.size() > 0; i++ ) {
            descriptorsList.remove(0);
        }
        objectFilename = null;
        descriptorsList = null;
        try {
            super.finalize();
        } catch (Throwable ex) {
           
        }
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
