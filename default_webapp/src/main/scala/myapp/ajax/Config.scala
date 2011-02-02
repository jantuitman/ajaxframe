package myapp.ajax;
import org.tuitman.ajaxframe.Configuration;
import java.util.Properties;
import java.io.File;
import java.io.FileReader;


class Config extends Configuration {
	
	private var p : Properties = null;
	
	def getEmailProperties = {
		this.synchronized {
			if (p==null) p=new Properties();
			p.load(new FileReader(new File("email.properties")));
			p;
		}
	}
	
}
