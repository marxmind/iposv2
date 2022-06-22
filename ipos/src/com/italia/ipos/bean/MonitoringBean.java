package com.italia.ipos.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.italia.ipos.controller.AddOnStore;
import com.italia.ipos.controller.Customer;
import com.italia.ipos.controller.CustomerPayment;
import com.italia.ipos.controller.CustomerPaymentTrans;
import com.italia.ipos.controller.DeliveryItemTrans;
import com.italia.ipos.controller.Incomes;
import com.italia.ipos.controller.MoneyIO;
import com.italia.ipos.controller.Payable;
import com.italia.ipos.controller.ProductInventory;
import com.italia.ipos.controller.ProductPricingTrans;
import com.italia.ipos.controller.ProductProperties;
import com.italia.ipos.controller.PurchasedItem;
import com.italia.ipos.controller.Receivable;
import com.italia.ipos.controller.StoreProduct;
import com.italia.ipos.controller.Summary;
import com.italia.ipos.controller.Supplier;
import com.italia.ipos.controller.SupplierPayment;
import com.italia.ipos.controller.SupplierTrans;
import com.italia.ipos.controller.Xtras;
import com.italia.ipos.enm.DateFormatter;
import com.italia.ipos.enm.DeliveryStatus;
import com.italia.ipos.enm.MoneyStatus;
import com.italia.ipos.enm.PaymentTransactionType;
import com.italia.ipos.enm.Status;
import com.italia.ipos.utils.Currency;
import com.italia.ipos.utils.DateUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 12/25/2018
 *
 */
@Named("monBean")
@ViewScoped
public class MonitoringBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 65679758641L;
	
	@Setter @Getter private String cashToday;
	@Setter @Getter private String expensesToday;
	@Setter @Getter private String collectedCashToday;
	@Setter @Getter private String monthlyExpenses;
	@Setter @Getter private String salesToday;
	@Setter @Getter private String monthlyGrossSales;
	@Setter @Getter private String monthYearTodayLabel;
	@Setter @Getter private String overTheCounterAmount;
	@Setter @Getter private String overTheCounterAmountMonthly;
	@Setter @Getter private String payable;
	@Setter @Getter private String receivable;
	@Setter @Getter private String storeItems;
	@Setter @Getter private String worthStoreItems;
	@Setter @Getter private String warehouseStokcs;
	
	@PostConstruct
	public void init() {
		
		String dateFrom = DateUtils.getCurrentDateYYYYMMDD();
		String dateTo = DateUtils.getCurrentDateYYYYMMDD();
		
		setMonthYearTodayLabel(DateUtils.getMonthName(DateUtils.getCurrentMonth()) + " " + DateUtils.getCurrentYear());
		
		loadCashInOut(dateFrom, dateTo, false);
		loadCashCollectedToday(dateFrom, dateTo);
		loadProductSale(dateFrom, dateTo, false);
		
		//monthly search
		dateFrom = DateUtils.getCurrentYear() + "-" + (DateUtils.getCurrentMonth()<9? "0" +DateUtils.getCurrentMonth() : DateUtils.getCurrentMonth()) + "-01";
		dateTo = DateUtils.getLastDayOfTheMonth("yyyy-MM-dd", dateTo, Locale.TAIWAN);
		loadProductSale(dateFrom, dateTo, true);
		loadCashInOut(dateFrom, dateTo, true);
		
		loadReceivable();
		loadPayable();
		
		loadStoreItems();
		loadWareHouseStocks();
	}
	
	private void loadPayable() {
		SupplierTrans tran = new SupplierTrans();
		tran.setIsActive(1);
		tran.setStatus(1);
		
		Supplier sup = new Supplier();
		sup.setIsactive(1);
		
		double payTotal = 0d;
		int id=1;
		for(SupplierTrans tn : SupplierTrans.retrieve(tran,sup)){
			
			Payable payable = new Payable();
			payable.setId(id++);
			payable.setDateTrans(tn.getTransDate());
			payable.setDescription(tn.getSupplier().getSuppliername());
			payable.setTransactionType("Payment");
			double amount = 0d, paidAmnt = 0d;;
			SupplierTrans tnx = new SupplierTrans();
			tnx.setIsActive(1);
			tnx.setId(tn.getId());
			
			SupplierPayment pay = new SupplierPayment();
			pay.setIsActive(1);
			
			for(SupplierPayment py : SupplierPayment.retrieve(pay,tnx)){
				paidAmnt += py.getAmount();
			}
			
			amount = tn.getPurchasedPrice() - paidAmnt;
			payable.setAmount(Currency.formatAmount(amount));
			payable.setRemarks("");
			payTotal += amount;
		}
		setPayable(Currency.formatAmount(payTotal));
	}
	
	
	private void loadReceivable() {
		CustomerPayment cus = new CustomerPayment();
		cus.setPayisactive(1);
		
		Customer cz = new Customer();
		cz.setIsactive(1);
		
		double recTotal=0d;
		for(CustomerPayment py : CustomerPayment.retrieve(cus,cz)){
			if(py.getAmountbalance().doubleValue()>0){
				recTotal += py.getAmountbalance().doubleValue();
			}
		}
		setReceivable(Currency.formatAmount(recTotal));
	}
	
	private void loadCashInOut(String dateFrom, String dateTo, boolean isForMonth) {
		String sql = "SELECT * FROM iomoney WHERE iodatetrans>=? AND iodatetrans<=? ";
    	String[] params = new String[2];
    	params[0] = dateFrom;
    	params[1] = dateTo;
    	
    	double in=0d, out=0d, netCash=0d, otc=0d;
    	for(MoneyIO io : MoneyIO.retrieve(sql, params)){
    		if(io.getInAmount()==0 && io.getOutAmount()==0){
    			
    		}else{
	    		in += io.getInAmount();
	    		out += io.getOutAmount();
    		}
    		
    		if(MoneyStatus.OTC.getId()==io.getTransType()) {
    			otc += io.getInAmount();
    		}
    		
    	}
    	netCash = in - out;
    	if(isForMonth) {
    		setMonthlyExpenses(Currency.formatAmount(out));
    		setOverTheCounterAmountMonthly(Currency.formatAmount(otc));
    	}else {
    		netCash = netCash<0? 0.00 : netCash;
	    	setCashToday(Currency.formatAmount(netCash));
	    	setExpensesToday(Currency.formatAmount(out));
	    	setOverTheCounterAmount(Currency.formatAmount(otc));
    	}
	}
	
	private void loadCashCollectedToday(String dateFrom, String dateTo) {
		
		CustomerPaymentTrans pay = new CustomerPaymentTrans();
		pay.setIspaid(1);
		pay.setPaytransisactive(1);
		pay.setBetween(true);
		pay.setDateFrom(dateFrom);
		pay.setDateTo(dateTo);
		double cashTotal = 0d;
		for(CustomerPaymentTrans caz : CustomerPaymentTrans.retrieve(pay)){
			if(caz.getAmountpay().doubleValue()>0){
				cashTotal += caz.getAmountpay().doubleValue();
			}
		}
		
		Xtras x = new Xtras(); 
		x.setIsActive(1);
		x.setStatus(Status.POSTED.getId());
		x.setBetween(true);
		x.setDateFrom(dateFrom);
		x.setDateTo(dateTo);
		
		for(Xtras xt : Xtras.retrieve(x)){
			if(MoneyStatus.OTHER_INCOME.getId()==xt.getTransType()){
				cashTotal += xt.getAmount();
			}else if(MoneyStatus.INCOME.getId()==xt.getTransType()){
				cashTotal += xt.getAmount();
			}
		}
		setCollectedCashToday(Currency.formatAmount(cashTotal));
	}
	
	private void loadProductSale(String dateFrom, String dateTo, boolean isForMonth){
			
		PurchasedItem item = new PurchasedItem();
		item.setIsactiveitem(1);
		item.setIsBetweenDate(true);
		item.setDateFrom(dateFrom);
		item.setDateTo(dateTo);
		double totalSale = 0d;
		List<PurchasedItem> plist = new ArrayList<PurchasedItem>();
		Map<String, List<PurchasedItem>> mapSold = new HashMap<String, List<PurchasedItem>>();
		
		for(PurchasedItem items : PurchasedItem.retrieve(item)){
			String date = items.getDatesold();
			
			if(mapSold!=null && mapSold.size()>0) {
				if(mapSold.containsKey(date)) {
					
					plist = mapSold.get(date);
					plist.add(items);
					mapSold.put(date, plist);
					
				}else {
					
					plist = new ArrayList<PurchasedItem>();
					plist.add(items);
					mapSold.put(date, plist);
				}
			}else {
				plist.add(items);
				mapSold.put(date, plist);
			}
			
		}
		
		for(String date : mapSold.keySet()) {
			for(PurchasedItem prod : mapSold.get(date)) {
				double saleTmp = prod.getSellingPrice().doubleValue() * prod.getQty();
				totalSale += saleTmp;
			}
		}
		
		//additional product in store
		AddOnStore on = new AddOnStore();
		on.setIsActive(1);
		on.setBetween(true);
		on.setDateFrom(dateFrom);
		on.setDateTo(dateTo);
		on.setStatus(Status.POSTED.getId());
		for(AddOnStore st : AddOnStore.retrieve(on)){
			totalSale += st.getAmount();
		}		
		
		if(isForMonth) {
			setMonthlyGrossSales(Currency.formatAmount(totalSale));
		}else {
			setSalesToday(Currency.formatAmount(totalSale));
		}
			
		}	
	
	private void loadStoreItems() {
		
		String sql = "SELECT * FROM storeproduct WHERE qty!=0 AND prodIsActive=1";
		String[] params = new String[0];
		
		double grandPrice=0d, qtyTotal = 0d, grandTotal=0d;
		for(StoreProduct store : StoreProduct.retrieveProduct(sql, params)){
			grandPrice += store.getSellingPrice();
			double total = store.getQuantity() * store.getSellingPrice();
			store.setTotal(total);
			grandTotal += total;
			qtyTotal += store.getQuantity();
		}
		setStoreItems(Currency.formatAmount(qtyTotal));
		setWorthStoreItems(Currency.formatAmount(grandTotal));
		
	}
	
	private void loadWareHouseStocks() {
		String sql = " AND inv.newqty!=0 AND prop.isactive=1";
		String[] params = new String[0]; 
		double warehouseTotalQty = 0d;
		for(ProductInventory inv : ProductInventory.retrieve(sql, params)){
			
			ProductProperties prop = ProductProperties.properties(inv.getProductProperties().getPropid()+"");
			inv.setProductProperties(prop);
			warehouseTotalQty += inv.getNewqty();
		}
		setWarehouseStokcs(Currency.formatAmount(warehouseTotalQty));
	}
	
	

	
}
