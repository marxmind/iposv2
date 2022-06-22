package com.italia.ipos.reports;
/**
 * 
 * @author Mark Italia
 * @version 1.0
 * @since 10/03/2018
 *
 */

import java.io.InputStream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class VCardRpt {

	private String f1;
	private InputStream f2;
	private String f3;
	private InputStream f4;
	private String f5;
	private String f6;
}
