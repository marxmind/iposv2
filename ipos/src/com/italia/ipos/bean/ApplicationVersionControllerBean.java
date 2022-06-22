package com.italia.ipos.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import com.italia.ipos.application.ApplicationFixes;
import com.italia.ipos.application.ApplicationVersionController;
import com.italia.ipos.security.Copyright;
import com.italia.ipos.security.License;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author mark italia
 * @since 09/28/2016
 * @version 1.0
 */

@Named("versionBean")
public class ApplicationVersionControllerBean {

	private static final long serialVersionUID = 1394801825228386363L;
	
	@Setter @Getter private ApplicationVersionController versionController;
	@Setter @Getter private ApplicationFixes applicationFixes;
	@Setter @Getter private List<ApplicationFixes> fixes = new ArrayList<ApplicationFixes>();
	@Setter @Getter private Copyright copyright;
	@Setter @Getter private List<License> licenses = new ArrayList<License>();
	
	@PostConstruct
	public void init(){
		 
		String sql = "SELECT * FROM app_version_control ORDER BY timestamp DESC LIMIT 1";
		String[] params = new String[0];
		versionController = ApplicationVersionController.retrieve(sql, params).get(0);
		
		sql = "SELECT * FROM copyright ORDER BY id desc limit 1";
		params = new String[0];
		copyright = Copyright.retrieve(sql, params).get(0);
		
		try{fixes = new ArrayList<ApplicationFixes>();}catch(Exception e){}
		sql = "SELECT * FROM buildfixes WHERE buildid=?";
		params = new String[1];
		params[0] = versionController.getBuildid()+"";
		try{fixes = ApplicationFixes.retrieve(sql, params);}catch(Exception e){}
		
		sql = "SELECT * FROM license";
		licenses = new ArrayList<License>();
		licenses = License.retrieve(sql, new String[0]);
		
	}
	
}
