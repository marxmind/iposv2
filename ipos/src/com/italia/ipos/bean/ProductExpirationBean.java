package com.italia.ipos.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.italia.ipos.controller.Product;
import com.italia.ipos.utils.DateUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author mark italia
 * @since 06/08/2017
 *@version 1.0
 */
@Named("prodExpBean")
@ViewScoped
public class ProductExpirationBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 67865897968581L;
	
	
	@Setter @Getter private String productSearch;
	@Setter @Getter private List<Product> products = new ArrayList<>();
	
	@PostConstruct
	public void init(){
		products = new ArrayList<>();
		
		String sql = " AND prod.productExpiration is not null AND prod.productExpiration>=? ORDER BY prod.productExpiration ASC LIMIT 100";
		String[] params = new String[1];
		params[0] = DateUtils.getCurrentDateYYYYMMDD();
		
		if(getProductSearch()!=null && !getProductSearch().isEmpty()){
			sql = "AND prop.productname like '%"+ getProductSearch().replace("--", "") +"%' " + sql;
		}
		
		for(Product prods : Product.retrieve(sql, params)){
			Date expDate = DateUtils.getDateFromString(prods.getProductExpiration(), "yyyy-MM-dd");
			String days = ""+ Math.abs(DateUtils.getRemainingDays(expDate, new Date(), TimeUnit.DAYS));
			prods.setRemainingProductDays(days);
			products.add(prods);
		}
		
	}
	
	
	
}
