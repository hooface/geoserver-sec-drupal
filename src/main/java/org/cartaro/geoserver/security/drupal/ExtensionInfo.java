package org.cartaro.geoserver.security.drupal;

import java.io.InputStream;
import java.util.Scanner;

/**
 * class to access bundled resources
 * 
 * @author nico mandery
 *
 */
public class ExtensionInfo {
	
	private String getResourceContents(String resourceName) {
		InputStream rs = getClass().getResourceAsStream(resourceName);
		if (rs==null) {
			return "";
		}
		return new Scanner(rs,"UTF-8").useDelimiter("\\A").next().trim();
	}
	
	String getGitVersion() {
		String gitVersion = getResourceContents("/geoserver-sec-drupal.gitversion");
		
		if ((gitVersion==null) || (gitVersion=="")) {
			gitVersion = "<No git version information available>";
		}
		return gitVersion;
	}

}