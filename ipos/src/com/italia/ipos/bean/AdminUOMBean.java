package com.italia.ipos.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.italia.ipos.controller.Login;
import com.italia.ipos.controller.UOM;
import com.italia.ipos.controller.UserDtls;
import com.italia.ipos.utils.LogU;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author mark italia
 * @since 10/01/2016
 *@version 1.0
 */
@Named("auomBean")
@ViewScoped
public class AdminUOMBean implements Serializable {

	private static final long serialVersionUID = 1094801425228384363L;
	
	@Setter @Getter private List<UOM> uoms = new ArrayList<UOM>();
	@Setter @Getter private String uomname = "Input Description";
	@Setter @Getter private String symbol = "Input Symbol";
	@Setter @Getter private UOM uomdata;
	
	public void save(){
		UOM uom = new UOM();
		
		if(Login.checkUserStatus()){
			
			if(getUomdata()!=null){
				uom = getUomdata();
			}
			uom.setUomname(getUomname());
			uom.setSymbol(getSymbol());
			uom.setUserDtls(Login.getUserLogin().getUserDtls());
			uom.save();
			clearFields();
			init();
			
		}
		
	}
	
	private void clearFields(){
		setUomdata(null);
		setUomname("Input Description");
		setSymbol("Input Symbol");
	}
	
	@PostConstruct
	public void init(){
		if(Login.checkUserStatus()){
			
			uoms = new ArrayList<UOM>();
			uoms = UOM.retrieve("SELECT * FROM uom ORDER BY uomid desc", new String[0]);
			
			
		}
		
	}
	
	public void print(){
		System.out.println("Print");
	}
	
	public void printAll(){
		System.out.println("Print All");
	}
	
	public void close(){
		System.out.println("close");
		clearFields();
	}
	
	public void clickItem(UOM uom){
		System.out.println("clickItem");
		setUomdata(uom);
		setUomname(uom.getUomname());
		setSymbol(uom.getSymbol());
	}
	
	public void deleteRow(UOM uom, boolean isValid){
		System.out.println("deleteRow");
		if(isValid){
			if(Login.checkUserStatus()){
				LogU.add("Delete UOM id " + uom.getUomid());
				uom.delete();
				init();
			}
		}
		
	}

	
	
}
