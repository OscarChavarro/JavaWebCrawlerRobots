package databaseMongo.model;

import java.util.ArrayList;

public class Franchise extends JdbcEntity
{
	private String url;
	private String name;
	private String category;
	private String minimumOrder;
	private String homeCost;
	private ArrayList<String> paymentMethod;
	private String rating;
	private int commentsNum;
	private boolean isFranchise;
	private boolean orderOnline;
	private ArrayList<Product> product;
	
	public Franchise() 
	{
		super();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getMinimumOrder() {
		return minimumOrder;
	}

	public void setMinimumOrder(String minimumOrder) {
		this.minimumOrder = minimumOrder;
	}

	public String getHomeCost() {
		return homeCost;
	}

	public void setHomeCost(String homeCost) {
		this.homeCost = homeCost;
	}

	public ArrayList<String> getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(ArrayList<String> paymentMethod) 
	{
		this.paymentMethod = paymentMethod;
	}
	
	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public int getCommentsNum() {
		return commentsNum;
	}

	public void setCommentsNum(int commentsNum) {
		this.commentsNum = commentsNum;
	}

	public boolean getIsFranchise() {
		return isFranchise;
	}

	public void setIsFranchise(String isFranchise) {
		if(isFranchise.contains("false"))
			this.isFranchise = false;
		else
			this.isFranchise = true;
	}

	public boolean getOrderOnline() {
		return orderOnline;
	}

	public void setOrderOnline(String orderOnline) {
		if(orderOnline.contains("false"))
			this.orderOnline = false;
		else
			this.orderOnline = true;
	}

	public ArrayList<Product> getProduct() {
		return product;
	}

	public void setProduct(ArrayList<Product> product) {
		this.product = product;
	}
	
}
