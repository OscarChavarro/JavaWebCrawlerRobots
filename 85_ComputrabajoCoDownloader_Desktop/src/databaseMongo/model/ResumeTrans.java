package databaseMongo.model;

import java.util.Date;

/**
*/
public class ResumeTrans extends JdbcEntity 
{
    private String _id;
    private String sourceUrl;
    private String name;
    private String profilePictureUrl;
    private Date lastLoginDate;
    private Date lastUpdateDate;
    private Date registrationDate;
    private String email;    
    private String phone;
    private String location;
    private String pair;
    private String jobSearchStatus;
    private Double wantedPayment;
    private String descriptionTitle;
    private String resumeLink;    
    private String profesion;
    private int age;
    private boolean drivingLicense;
    private boolean hasVehicle;
    private boolean availableForTravel;
    private boolean availableForRelocation;
    private boolean currentlyWorking;
    private String country;
    private Date extractionDate;

    public ResumeTrans() 
    {
        this.country = "co";
    }

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProfilePictureUrl() {
		return profilePictureUrl;
	}

	public void setProfilePictureUrl(String profilePictureUrl) {
		this.profilePictureUrl = profilePictureUrl;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPair() {
		return pair;
	}

	public void setPair(String pair) {
		this.pair = pair;
	}

	public String getJobSearchStatus() {
		return jobSearchStatus;
	}

	public void setJobSearchStatus(String jobSearchStatus) {
		this.jobSearchStatus = jobSearchStatus;
	}

	public Double getWantedPayment() {
		return wantedPayment;
	}

	public void setWantedPayment(Double wantedPayment) {
		this.wantedPayment = wantedPayment;
	}

	public String getDescriptionTitle() {
		return descriptionTitle;
	}

	public void setDescriptionTitle(String descriptionTitle) {
		this.descriptionTitle = descriptionTitle;
	}

	public String getResumeLink() {
		return resumeLink;
	}

	public void setResumeLink(String resumeLink) {
		this.resumeLink = resumeLink;
	}

	public String getProfesion() {
		return profesion;
	}

	public void setProfesion(String profesion) {
		this.profesion = profesion;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public boolean isDrivingLicense() {
		return drivingLicense;
	}

	public void setDrivingLicense(boolean drivingLicense) {
		this.drivingLicense = drivingLicense;
	}

	public boolean isHasVehicle() {
		return hasVehicle;
	}

	public void setHasVehicle(boolean hasVehicle) {
		this.hasVehicle = hasVehicle;
	}

	public boolean isAvailableForTravel() {
		return availableForTravel;
	}

	public void setAvailableForTravel(boolean availableForTravel) {
		this.availableForTravel = availableForTravel;
	}

	public boolean isAvailableForRelocation() {
		return availableForRelocation;
	}

	public void setAvailableForRelocation(boolean availableForRelocation) {
		this.availableForRelocation = availableForRelocation;
	}

	public boolean isCurrentlyWorking() {
		return currentlyWorking;
	}

	public void setCurrentlyWorking(boolean currentlyWorking) {
		this.currentlyWorking = currentlyWorking;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Date getExtractionDate() {
		return extractionDate;
	}

	public void setExtractionDate(Date extractionDate) {
		this.extractionDate = extractionDate;
	}
}

