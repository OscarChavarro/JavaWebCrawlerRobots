package webcrawler;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
*/
public class ProductVariant {
    private String reference;
    private String description;
    private int quantityFontibon;
    private int quantityCelta;
    private int quantityTotal;
    private boolean referenceSet;
    private boolean descriptionSet;
    private boolean quantityFontibonSet;
    private boolean quantityCeltaSet;
    private boolean quantityTotalSet;

    public ProductVariant()
    {
        reference = "<DESCONOCIDO>";
        description = "<DESCONOCIDO>";
        quantityFontibon = -1;
        quantityCelta = -2;
        quantityTotal = -3;
        referenceSet = false;
        descriptionSet = false;
        quantityFontibonSet = false;
        quantityCeltaSet = false;
        quantityTotalSet = false;
    }

    public boolean isValid()
    {
        return referenceSet &&
            descriptionSet &&
            quantityFontibonSet &&
            quantityCeltaSet &&
            quantityTotalSet;

    }
    
    /**
     * @return the reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * @param reference the reference to set
     */
    public void setReference(String reference) {
        this.reference = reference;
        referenceSet = true;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
        descriptionSet = true;
    }

    /**
     * @return the quantityFontibon
     */
    public int getQuantityFontibon() {
        return quantityFontibon;
    }

    /**
     * @param quantityFontibon the quantityFontibon to set
     */
    public void setQuantityFontibon(int quantityFontibon) {
        this.quantityFontibon = quantityFontibon;
        quantityFontibonSet = true;
    }

    /**
     * @return the quantityCelta
     */
    public int getQuantityCelta() {
        return quantityCelta;
    }

    /**
     * @param quantityCelta the quantityCelta to set
     */
    public void setQuantityCelta(int quantityCelta) {
        this.quantityCelta = quantityCelta;
        quantityCeltaSet = true;
    }

    /**
     * @return the quantityTotal
     */
    public int getQuantityTotal() {
        return quantityTotal;
    }

    /**
     * @param quantityTotal the quantityTotal to set
     */
    public void setQuantityTotal(int quantityTotal) {
        this.quantityTotal = quantityTotal;
        quantityTotalSet = true;
    }

    public void setCompoundString(String content) {
        StringTokenizer parser = new StringTokenizer(content, "/ ");
        setReference(parser.nextToken());
        try {
            setDescription(parser.nextToken());
        }
        catch ( NoSuchElementException e ) {
            System.out.println("Invalid content: " + content);
            setDescription("[INDEFINIDO]");
        }
    }
    
    @Override
    public String toString()
    {
        String msg;
        msg = reference + " | " + description + " | " + quantityFontibon  + 
            " | " + quantityCelta + " | " + quantityTotal;
        if ( isValid() ) {
            msg += " | VALID";
        }
        else {
            msg += " | INVALID";
        }
        return msg;
    }    
}
