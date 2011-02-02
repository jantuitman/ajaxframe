package org.tuitman.ajaxframe;
import java.util.Properties;


abstract trait Configuration {
	
   def getEmailProperties : Properties;
	
	
}


object Config {
	var instance : Configuration = null; 
}