//===========================================================================
package databaseMysqlMongo.model;

/**
*/
public class Property extends JdbcEntity {
    private int id;
    private String _id;
    private String url;
    private String phoneFixed;
    private String phoneMobile;
    private String address;
    private double latitudeDegrees;
    private double longitudeDegrees;
    private String block;
    private String blockCadastre;
    private int businessType;
    private int socialLevel;
    private double areaTotal;
    private double areaConstructed;
    private int numberOfRooms;
    private int numberOfBathrooms;
    private int numberOfParkingLots;
    private String businessCity;
    private double businessPriceAdmin;
    private double businessPriceLease;
    private double businessPriceSale;
    private String timeBuilt;

    /**
     * @return the id
     */
    public int getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id)
    {
        this.id = id;
    }

    /**
     * @return the id
     */
    public String get_id()
    {
        return _id;
    }

    /**
     * @param id the id to set
     */
    public void set_id(String id)
    {
        this._id = id;
    }

    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * @return the phoneFixed
     */
    public String getPhoneFixed()
    {
        return phoneFixed;
    }

    /**
     * @param phoneFixed the phoneFixed to set
     */
    public void setPhoneFixed(String phoneFixed)
    {
        this.phoneFixed = phoneFixed;
    }

    /**
     * @return the phoneMobile
     */
    public String getPhoneMobile()
    {
        return phoneMobile;
    }

    /**
     * @param phoneMobile the phoneMobile to set
     */
    public void setPhoneMobile(String phoneMobile)
    {
        this.phoneMobile = phoneMobile;
    }

    /**
     * @return the address
     */
    public String getAddress()
    {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address)
    {
        this.address = address;
    }

    /**
     * @return the latitudeDegrees
     */
    public double getLatitudeDegrees()
    {
        return latitudeDegrees;
    }

    /**
     * @param latitudeDegrees the latitudeDegrees to set
     */
    public void setLatitudeDegrees(double latitudeDegrees)
    {
        this.latitudeDegrees = latitudeDegrees;
    }

    /**
     * @return the longitudeDegrees
     */
    public double getLongitudeDegrees()
    {
        return longitudeDegrees;
    }

    /**
     * @param longitudeDegrees the longitudeDegrees to set
     */
    public void setLongitudeDegrees(double longitudeDegrees)
    {
        this.longitudeDegrees = longitudeDegrees;
    }

    /**
     * @return the block
     */
    public String getBlock()
    {
        return block;
    }

    /**
     * @param block the block to set
     */
    public void setBlock(String block)
    {
        this.block = block;
    }

    /**
     * @return the blockCadastre
     */
    public String getBlockCadastre()
    {
        return blockCadastre;
    }

    /**
     * @param blockCadastre the blockCadastre to set
     */
    public void setBlockCadastre(String blockCadastre)
    {
        this.blockCadastre = blockCadastre;
    }

    /**
     * @return the businessType
     */
    public int getBusinessType()
    {
        return businessType;
    }

    /**
     * @param businessType the businessType to set
     */
    public void setBusinessType(int businessType)
    {
        this.businessType = businessType;
    }

    /**
     * @return the socialLevel
     */
    public int getSocialLevel()
    {
        return socialLevel;
    }

    /**
     * @param socialLevel the socialLevel to set
     */
    public void setSocialLevel(int socialLevel)
    {
        this.socialLevel = socialLevel;
    }

    /**
     * @return the areaTotal
     */
    public double getAreaTotal()
    {
        return areaTotal;
    }

    /**
     * @param areaTotal the areaTotal to set
     */
    public void setAreaTotal(double areaTotal)
    {
        this.areaTotal = areaTotal;
    }

    /**
     * @return the areaConstructed
     */
    public double getAreaConstructed()
    {
        return areaConstructed;
    }

    /**
     * @param areaConstructed the areaConstructed to set
     */
    public void setAreaConstructed(double areaConstructed)
    {
        this.areaConstructed = areaConstructed;
    }

    /**
     * @return the numberOfRooms
     */
    public int getNumberOfRooms()
    {
        return numberOfRooms;
    }

    /**
     * @param numberOfRooms the numberOfRooms to set
     */
    public void setNumberOfRooms(int numberOfRooms)
    {
        this.numberOfRooms = numberOfRooms;
    }

    /**
     * @return the numberOfBathrooms
     */
    public int getNumberOfBathrooms()
    {
        return numberOfBathrooms;
    }

    /**
     * @param numberOfBathrooms the numberOfBathrooms to set
     */
    public void setNumberOfBathrooms(int numberOfBathrooms)
    {
        this.numberOfBathrooms = numberOfBathrooms;
    }

    /**
     * @return the numberOfParkingLots
     */
    public int getNumberOfParkingLots()
    {
        return numberOfParkingLots;
    }

    /**
     * @param numberOfParkingLots the numberOfParkingLots to set
     */
    public void setNumberOfParkingLots(int numberOfParkingLots)
    {
        this.numberOfParkingLots = numberOfParkingLots;
    }

    @Override
    public String toString()
    {
        String msg;

        msg = "\n  - URL: " + url + "\n";
        msg += "  - Latitude: " + latitudeDegrees + "\n";
        msg += "  - Longitude: " + longitudeDegrees + "\n";
        msg += "  - Fixed phone number: " + phoneFixed + "\n";
        msg += "  - Mobile phone number: " + phoneMobile + "\n";
        msg += "  - Address: " + address + "\n";
        msg += "  - Business price (monthly lease): " + businessPriceLease + "\n";
        msg += "  - Business price (monthly administration): " + businessPriceAdmin + "\n";
        msg += "  - Business price (sale): " + businessPriceSale + "\n";
        msg += "  - Social level: " + socialLevel + "\n";
        msg += "  - Area constructed: " + areaConstructed + "\n";
        msg += "  - Area total: " + areaTotal + "\n";
        msg += "  - Number of rooms: " + numberOfRooms + "\n";
        msg += "  - Number of batrooms: " + numberOfBathrooms + "\n";
        msg += "  - Number of parking lots: " + numberOfParkingLots + "\n";
        msg += "  - Block cadastre: " + blockCadastre + "\n";
        msg += "  - Block: " + block + "\n";

        return msg;
    }
    
    /**
     * @return the businessCity
     */
    public String getBusinessCity() {
        return businessCity;
    }

    /**
     * @param businessCity the businessCity to set
     */
    public void setBusinessCity(String businessCity) {
        this.businessCity = businessCity;
    }

    /**
     * @return the businessPriceAdmin
     */
    public double getBusinessPriceAdmin() {
        return businessPriceAdmin;
    }

    /**
     * @param businessPriceAdmin the businessPriceAdmin to set
     */
    public void setBusinessPriceAdmin(double businessPriceAdmin) {
        this.businessPriceAdmin = businessPriceAdmin;
    }

    /**
     * @return the businessPriceLease
     */
    public double getBusinessPriceLease() {
        return businessPriceLease;
    }

    /**
     * @param businessPriceLease the businessPriceLease to set
     */
    public void setBusinessPriceLease(double businessPriceLease) {
        this.businessPriceLease = businessPriceLease;
    }

    /**
     * @return the businessPriceSale
     */
    public double getBusinessPriceSale() {
        return businessPriceSale;
    }

    /**
     * @param businessPriceSale the businessPriceSale to set
     */
    public void setBusinessPriceSale(double businessPriceSale) {
        this.businessPriceSale = businessPriceSale;
    }
    
    /**
     * @return the timeBuilt
     */
    public String getTimeBuilt()
    {
        return timeBuilt;
    }

    /**
     * @param timeBuilt the timeBuilt to set
     */
    public void setTimeBuilt(String timeBuilt)
    {
        this.timeBuilt = timeBuilt;
    }


}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
