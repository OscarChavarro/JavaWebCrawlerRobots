//===========================================================================

package vsdk.toolkit.common;

public class ArrayListOfLongs extends FundamentalEntity {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20080527L;

    private int increment;
    private int assignedSize;    
  
    private int mysize; // Currently used elements
    private long array[];
    private long array2[];

    public ArrayListOfLongs(int increment) {
        this.increment = increment;
        array = new long[this.increment];
        assignedSize = this.increment;
        mysize = 0;
    }

    public int size()
    {
        return mysize;
    }

    public long get(int i)
    {
        return array[i];
    }

    public void set(int i, long val)
    {
        array[i] = val;
    }

    public void add(long val)
    {
        if ( mysize >= assignedSize ) {
            grow();
        }
        array[mysize] = val;
        mysize++;
    }

    private void grow()
    {
        array2 = new long[assignedSize+increment];
        assignedSize += increment;

        int i;
        for ( i = 0; i < mysize; i++ ) {
            array2[i] = array[i];
        }

        array = array2;
    }

    public void clean()
    {
        mysize = 0;
    }

    /**
    Implements the quick sort algorithm.
    @param array
    @param left0
    @param right0
    */
    public static void quicksort(long[] array, int left0, int right0)
    {
      int left = left0;
      int right = right0 + 1;
      long pivot, temp;

      pivot = array[left0];
      do {
        do left++; while (left <= right0 && array[left] < pivot);
        do right--; while (array[right] > pivot );
        if (left < right) {
          temp = array[left];
          array[left] = array[right];
          array[right] = temp;
        }
      } while (left <= right);

      temp = array[left0];
      array[left0] = array[right];
      array[right] = temp;

      if (left0 < right) quicksort(array, left0, right);
      if (left < right0) quicksort(array, left, right0);
    }

    /**
    Quicksort
    */
    public void sort() {
        quicksort(array, 0, mysize-1);
    }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
