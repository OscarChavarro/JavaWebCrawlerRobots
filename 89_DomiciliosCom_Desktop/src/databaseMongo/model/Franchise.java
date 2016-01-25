package databaseMongo.model;

import java.util.ArrayList;

public class Franchise extends JdbcEntity
{
	private String url;
	private String name;
	private String category;
	private Double minimumOrder;
	private String homeCost;
	private ArrayList<String> paymentMethod;
	private int rating;
	private int commentsNum;
	private Product product;
	
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

	public Double getMinimumOrder() {
		return minimumOrder;
	}

	public void setMinimumOrder(Double minimumOrder) {
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

	public void setPaymentMethod(ArrayList<String> paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public int getCommentsNum() {
		return commentsNum;
	}

	public void setCommentsNum(int commentsNum) {
		this.commentsNum = commentsNum;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
	
}
