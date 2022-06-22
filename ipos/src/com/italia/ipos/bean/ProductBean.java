package com.italia.ipos.bean;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.italia.ipos.controller.Product;
import com.italia.ipos.controller.ProductInventory;
import com.italia.ipos.controller.ProductPricingTrans;
import com.italia.ipos.controller.ProductProperties;
import com.italia.ipos.enm.Ipos;
import com.italia.ipos.reader.ReadConfig;

/**
 * 
 * @author mark italia
 * @since 10/05/2016
 * @version 1.0
 */
@Named
@ApplicationScoped
public class ProductBean implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 54356743641L;
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
	
	public List<ProductProperties> productList(ProductProperties product){
		List<ProductProperties> products = new ArrayList<ProductProperties>();
		
		for(ProductProperties prop : ProductProperties.retrieve(product)){
			ProductInventory inv = ProductInventory.retrieve(prop.getPropid()+"");
			prop.setProductInventory(inv);
			
			ProductPricingTrans price = new ProductPricingTrans();
			price.setIsActiveprice(1);
			ProductPricingTrans prize = ProductPricingTrans.retrievePrice(prop.getPropid()+"");
			prop.setProductPricingTrans(prize);
			copyProductImg(prop.getProductcode());
			products.add(prop);
		}
		
		return products;
	}
	
	
	public List<Product> loadProduct(String sql, String[] params){
		List<Product> products = new ArrayList<Product>();
		
		for(ProductInventory inv : ProductInventory.retrieve(sql, params)){
			Product prod = new Product();
			prod = inv.getProduct();
			prod.setProductProperties(ProductProperties.properties(inv.getProductProperties().getPropid()+""));
			prod.setProductInventory(inv);
			
			ProductPricingTrans price = new ProductPricingTrans();
			price.setIsActiveprice(1);
			
			Product prd = new Product();
			prd.setIsactiveproduct(1);
			prd.setProdid(prod.getProdid());
			
			ProductPricingTrans prize = null;
			try{prize = ProductPricingTrans.retrievePrice(price,prd).get(0);}catch(IndexOutOfBoundsException e){
				prize = new ProductPricingTrans();
				prize.setPurchasedprice(new BigDecimal("0"));
				prize.setSellingprice(new BigDecimal("0"));
				prize.setNetprice(new BigDecimal("0"));
			}
			prod.setProductPricingTrans(prize);
			
			/**
			 * temporary commented. this cause slowness in the system during loading
			 * solution loading of image has been moved to menuBean
			 */
			//copyProductImg(prop.getProductProperties().getProductcode());
			products.add(prod);
		}
		
		return products;
	}
	
	@Deprecated
	public List<Product> productList(Product product,ProductProperties proper){
		List<Product> products = new ArrayList<Product>();
				
		for(Product prop : Product.retrieve(product,proper)){
			Product prd = new Product();
			prd.setProdid(prop.getProdid());
			prd.setIsactiveproduct(1);
			
			List<ProductInventory> invs = ProductInventory.retrieve(prd);
			if(invs.size()>0){
				ProductInventory inv = invs.get(0);
				if(inv.getNewqty()>0){
					prop.setProductProperties(ProductProperties.properties(prop.getProductProperties().getPropid()+""));
					prop.setProductInventory(inv);
					
					ProductPricingTrans price = new ProductPricingTrans();
					price.setIsActiveprice(1);
					ProductPricingTrans prize = ProductPricingTrans.retrievePrice(price,prd).get(0);
					prop.setProductPricingTrans(prize);
					
					/**
					 * temporary commented. this cause slowness in the system during loading
					 * solution loading of image has been moved to menuBean
					 */
					//copyProductImg(prop.getProductProperties().getProductcode());
					products.add(prop);
				}
			}
		}
		
		return products;
	}
	
	public void loadProduct(){
		
		
		String sql = " AND inv.newqty!=0 AND inv.isactive=1  AND prd.isactiveproduct=1";
		String[] params = new String[0];
		for(ProductInventory inv : ProductInventory.retrieve(sql, params)){
			Product prod = new Product();
			prod = inv.getProduct();
			prod.setProductProperties(ProductProperties.properties(inv.getProductProperties().getPropid()+""));
			prod.setProductInventory(inv);
			
			ProductPricingTrans price = new ProductPricingTrans();
			price.setIsActiveprice(1);
			
			Product prd = new Product();
			prd.setIsactiveproduct(1);
			prd.setProdid(prod.getProdid());
			
			ProductPricingTrans prize = null;
			try{prize = ProductPricingTrans.retrievePrice(price,prd).get(0);}catch(IndexOutOfBoundsException e){
				prize = new ProductPricingTrans();
				prize.setPurchasedprice(new BigDecimal("0"));
				prize.setSellingprice(new BigDecimal("0"));
				prize.setNetprice(new BigDecimal("0"));
			}
			prod.setProductPricingTrans(prize);
			
			/**
			 * temporary commented. this cause slowness in the system during loading
			 * solution loading of image has been moved to menuBean
			 */
			copyProductImg(inv.getProductProperties().getProductcode());
			
		}
		
	}
	
	public void copyProductImg(String productCode){
		String pathToSave = FacesContext.getCurrentInstance()
                .getExternalContext().getRealPath(Ipos.SEPERATOR.getName()) + Ipos.SEPERATOR.getName() +
                						Ipos.APP_RESOURCES_LOC.getName() + Ipos.SEPERATOR.getName() + 
                						Ipos.APP_RESOURCES_LOC_IMG.getName() + Ipos.SEPERATOR.getName();
		System.out.println("pathToSave " + pathToSave + productCode + ".jpg");
		File logdirectory = new File(pathToSave);
		if(logdirectory.isDirectory()){
			System.out.println("is directory");
		}
		
		
		String productFile = ReadConfig.value(Ipos.APP_IMG_FILE) + productCode + ".jpg";
		File file = new File(productFile);
		//if(!file.exists()){
			System.out.println("copying file.... " + file.getName());
			try{
			Files.copy(file.toPath(), (new File(pathToSave + file.getName())).toPath(),
			        StandardCopyOption.REPLACE_EXISTING);
			}catch(IOException e){}
		//}
	}
	
	public StreamedContent getLoadImage(String filepath, String productCode){
		
		System.out.println("Loading image " + filepath + " filename " + productCode);
		File file = new File(filepath);
		try{
		if(file.exists()){
			
	        BufferedInputStream in =  new BufferedInputStream(new FileInputStream(file),DEFAULT_BUFFER_SIZE); 
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        
	        int val = -1;      	
	            while((val = in.read()) != -1){
	                out.write(val);
	            }
	       

	        byte[] bytes = out.toByteArray();
	         //return new DefaultStreamedContent(new ByteArrayInputStream(bytes), "image/jpg", productCode+".jpg");
	        
	        return DefaultStreamedContent.builder()
					.contentType("image/jpg")
					.name(productCode+".jpg")
					.stream(()->{
						try {
							return new FileInputStream(file);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					})
					.build();
			
		}else{
			return getDefaultImg();
		}
		}catch(Exception e){
			return getDefaultImg();
		}
	}
	
	
	
	public StreamedContent getProductImage(){
		StreamedContent image = null;
		
		
		
		String productCode = "PRODUCT-GP-0000000001";
		try{
		System.out.println("Reading product image " + productCode);
		String imgLocation = ReadConfig.value(Ipos.APP_IMG_FILE) + productCode + ".jpg";
		
		File file = new File(imgLocation);
		if(file.exists()){
			
			BufferedImage img = ImageIO.read(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int w = img.getWidth(null);
			int h = img.getHeight(null);

			// image is scaled two times at run time
			int scale = 2;

			BufferedImage bi = new BufferedImage(w * scale, h * scale,
					BufferedImage.TYPE_INT_ARGB);
			Graphics g = bi.getGraphics();

			g.drawImage(img, 10, 10, w * scale, h * scale, null);

			ImageIO.write(bi, "jpg", bos);
			//return new DefaultStreamedContent(new ByteArrayInputStream(
				//	bos.toByteArray()), "image/jpg");
			
			var df = DefaultStreamedContent.builder()
					.contentType("image/jpg")
					.name(productCode + ".jpg")
					.stream(()->{
						try {
							return new FileInputStream(file);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					})
					.build();
			
			return df;
			
		}else{
			return getDefaultImg();
		}
		
		
		
		}catch(IOException e){}
		
		return getDefaultImg();
	}
	
	public StreamedContent getDefaultImg(){
		StreamedContent image = null;
		try{
		//FacesContext context = FacesContext.getCurrentInstance();		
		//if(context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE){
		
		//ByteArrayOutputStream bos = new ByteArrayOutputStream();
		//BufferedImage img = ImageIO.read(context.getExternalContext()
		//		.getResourceAsStream("/resources/img/noimageproduct.jpg"));
		//int w = img.getWidth(null);
		//int h = img.getHeight(null);

		// image is scaled two times at run time
		//int scale = 2;

		//BufferedImage bi = new BufferedImage(w * scale, h * scale,
		//		BufferedImage.TYPE_INT_ARGB);
		//Graphics g = bi.getGraphics();

		//g.drawImage(img, 10, 10, w * scale, h * scale, null);

		//ImageIO.write(bi, "jpg", bos);
		//image = new DefaultStreamedContent(new ByteArrayInputStream(
			//	bos.toByteArray()), "image/jpg");
		String imageName = System.getenv("SystemDrive") + File.separator + "ipos" + File.separator + "noimageproduct.jpg";
		File file = new File(imageName);
		image = DefaultStreamedContent.builder()
				.contentType("image/jpg")
				.name("noimageproduct.jpg")
				.stream(()->{
					try {
						return new FileInputStream(file);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				})
				.build();
		
		}catch(Exception e){}
		
		return image;
	}
}
