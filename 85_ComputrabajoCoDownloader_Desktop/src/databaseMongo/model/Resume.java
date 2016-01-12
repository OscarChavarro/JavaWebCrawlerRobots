package databaseMongo.model;

/**
*/
public class Resume extends JdbcEntity {
    private String _id;
    private String documentId;
    private String sourceUrl;
    private String name;
    private String profilePictureUrl;
    private String lastLoginDate;
    private String lastUpdateDate;
    private String registrationDate;
    private String email;    
    private String phone;
    private String location;
    private String pair;
    private String jobSearchStatus;
    private String wantedPayment;
    private String descriptionTitle;
    private String resumeLink;    
    private String htmlContent;
    private int age;
    private int emailStatus;
    private boolean drivingLicense;
    private boolean hasVehicle;
    private boolean availableForTravel;
    private boolean availableForRelocation;
    private boolean currentlyWorking;
    private String country;

    public Resume() {
        this.country = "co";
        emailStatus = 0;
    }

    /**
     * @return the lastUpdateDate
     */
    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * @param lastUpdateDate the lastUpdateDate to set
     */
    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    /**
     * @return the profilePicture
     */
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    /**
     * @param profilePicture the profilePicture to set
     */
    public void setProfilePictureUrl(String profilePicture) {
        this.profilePictureUrl = profilePicture;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the age
     */
    public int getAge() {
        return age;
    }

    /**
     * @param age the age to set
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * @return the pair
     */
    public String getPair() {
        return pair;
    }

    /**
     * @param pair the pair to set
     */
    public void setPair(String pair) {
        this.pair = pair;
    }

    /**
     * @return the jobSearchStatus
     */
    public String getJobSearchStatus() {
        return jobSearchStatus;
    }

    /**
     * @param jobSearchStatus the jobSearchStatus to set
     */
    public void setJobSearchStatus(String jobSearchStatus) {
        this.jobSearchStatus = jobSearchStatus;
    }

    /**
     * @return the wantedPayment
     */
    public String getWantedPayment() {
        return wantedPayment;
    }

    /**
     * @param wantedPayment the wantedPayment to set
     */
    public void setWantedPayment(String wantedPayment) {
        this.wantedPayment = wantedPayment;
    }

    /**
     * @return the descriptionTitle
     */
    public String getDescriptionTitle() {
        return descriptionTitle;
    }

    /**
     * @param descriptionTitle the descriptionTitle to set
     */
    public void setDescriptionTitle(String descriptionTitle) {
        this.descriptionTitle = descriptionTitle;
    }

    /**
     * @return the resumeLink
     */
    public String getResumeLink() {
        return resumeLink;
    }

    /**
     * @param resumeLink the resumeLink to set
     */
    public void setResumeLink(String resumeLink) {
        this.resumeLink = resumeLink;
    }

    /**
     * @return the htmlContent
     */
    public String getHtmlContent() {
        return htmlContent;
    }

    /**
     * @param htmlContent the htmlContent to set
     */
    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    /**
     * @return the registrationDate
     */
    public String getRegistrationDate() {
        return registrationDate;
    }

    /**
     * @param registrationDate the registrationDate to set
     */
    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * @return the lastLoginDate
     */
    public String getLastLoginDate() {
        return lastLoginDate;
    }

    /**
     * @param lastLoginDate the lastLoginDate to set
     */
    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
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

    /**
     * @return the sourceUrl
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * @param sourceUrl the sourceUrl to set
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setBinaryValue(String id, boolean value) {
        if ( id.contains("empleo") ) {
            currentlyWorking = value;
        }
        else if ( id.contains("conducir") ) {
            drivingLicense = value;
        }
        else if ( id.contains("propio") ) {
            hasVehicle = value;
        }
        else if ( id.contains("viajar") ) {
            availableForTravel = value;
        }
        else if ( id.contains("residencia") ) {
            availableForRelocation = value;
        }
    }

    /**
     * @return the drivingLicense
     */
    public boolean isDrivingLicense() {
        return drivingLicense;
    }

    /**
     * @param drivingLicense the drivingLicense to set
     */
    public void setDrivingLicense(boolean drivingLicense) {
        this.drivingLicense = drivingLicense;
    }

    /**
     * @return the hasVehicle
     */
    public boolean isHasVehicle() {
        return hasVehicle;
    }

    /**
     * @param hasVehicle the hasVehicle to set
     */
    public void setHasVehicle(boolean hasVehicle) {
        this.hasVehicle = hasVehicle;
    }

    /**
     * @return the availableForTravel
     */
    public boolean isAvailableForTravel() {
        return availableForTravel;
    }

    /**
     * @param availableForTravel the availableForTravel to set
     */
    public void setAvailableForTravel(boolean availableForTravel) {
        this.availableForTravel = availableForTravel;
    }

    /**
     * @return the availableForRelocation
     */
    public boolean isAvailableForRelocation() {
        return availableForRelocation;
    }

    /**
     * @param availableForRelocation the availableForRelocation to set
     */
    public void setAvailableForRelocation(boolean availableForRelocation) {
        this.availableForRelocation = availableForRelocation;
    }

    /**
     * @return the currentlyWorking
     */
    public boolean isCurrentlyWorking() {
        return currentlyWorking;
    }

    /**
     * @param currentlyWorking the currentlyWorking to set
     */
    public void setCurrentlyWorking(boolean currentlyWorking) {
        this.currentlyWorking = currentlyWorking;
    }

    /**
     * @return the _id
     */
    public String get_id() {
        return _id;
    }

    /**
     * @param _id the _id to set
     */
    public void set_id(String _id) {
        this._id = _id;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the documentId
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * @param documentId the documentId to set
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * @return the emailStatus
     */
    public int getEmailStatus() {
        return emailStatus;
    }

    /**
     * @param emailStatus the emailStatus to set
     */
    public void setEmailStatus(int emailStatus) {
        this.emailStatus = emailStatus;
    }
}
