package com.italia.ipos.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.italia.ipos.application.Application;
import com.italia.ipos.controller.Customer;
import com.italia.ipos.controller.CustomerPoints;
import com.italia.ipos.controller.PointsHistory;

import lombok.Getter;
import lombok.Setter;

@Named("pointBean")
@ViewScoped
public class PointsBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 979769807861L;
	
	@Setter @Getter private List<CustomerPoints> points = new ArrayList<CustomerPoints>();
	@Setter @Getter private List<CustomerPoints> selectedPoints;
	@Setter @Getter private CustomerPoints pointsData;
	@Setter @Getter private String searchName;
	@Setter @Getter private Customer customerData;
	@Setter @Getter private List<PointsHistory> poHis = new ArrayList<PointsHistory>();
	
	@PostConstruct
	public void init() {
		
		String sql = " AND cus.cusisactive=1 ";
		String[] params = new String[0];
		
		if(getSearchName()!=null && !getSearchName().isEmpty()) {
			sql += " AND ( cus.fullname like '%"+ getSearchName().replace("--", "") +"%' OR cus.cuscardno like '%"+ getSearchName().replace("--", "") +"%')";
		}else {
			sql += " LIMIT 10";
		}
		
		List<Customer> customers = Customer.retrieve(sql, params);
		
		
		if(customers!=null && customers.size()>0) {
			loadPoints(customers);
		}
		
	}
	
	public void loadPoints(List<Customer> customers) {
		points = new ArrayList<CustomerPoints>();
		String sql = " AND pts.isactivepo=1 ";
		String[] params = new String[0];
		for(Customer cus : customers) {
				sql = " AND pts.isactivepo=1 ";
				sql += "  AND cuz.customerid=" + cus.getCustomerid()+"";
				List<CustomerPoints> cpts = CustomerPoints.retrieve(sql, params);
				
				CustomerPoints po = new CustomerPoints();
				if(cpts==null || cpts.size()==0) {
					po.setIsActive(1);
					po.setCurrentPoints(0);
					po.setLatestAddedPoints(0);
					po.setLatestDeductedPoints(0);
					po.setCustomer(cus);
					points.add(po);
				}else if(cpts.size()==1) {
					points.add(cpts.get(0));
					clickCustomerPoints(cpts.get(0));
				}
		}
		
	}
	
	public void clickCustomerPoints(CustomerPoints pt) {
		poHis = new ArrayList<PointsHistory>();
			String sql = " AND cuz.customerid=? AND pts.isactivepoh=1 ORDER BY pts.pohid DESC";
			String[] params = new String[1];
			params[0] = pt.getCustomer().getCustomerid()+"";
			poHis = PointsHistory.retrieve(sql, params);
	}
	
	public void activatePoints(CustomerPoints pt) {
		boolean isactivate = pt.isChecked();
		
		if(isactivate) {
			pt.setIsActivated(1);
			pt.save();	
			init();
			Application.addMessage(1, "Success", "Successfully activated");
		}else {
			Application.addMessage(1, "Success", "Successfully deactivated");
		}
	}
	
	

}
