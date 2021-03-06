package org.tuitman.ajaxframe;
import java.io.Reader;
import net.liftweb.json.DefaultFormats;
import net.liftweb.json.JsonParser._;
import net.liftweb.json.Printer;
import net.liftweb.json.Serialization.write;
import net.liftweb.json.JsonAST.render;




class JsonExtractor[Input,Output <: AnyRef](inputClass: Class[_])(implicit mf: Manifest[Input]) {
	
	
	def getInput(is : Reader) : Input = {
		
		//implicit val formats = net.liftweb.json.DefaultFormats;
		//parse(is).extract[Input](formats,mf);
		JsonExtract.extract(parse(is),inputClass).asInstanceOf[Input];
	}
	
	def makeOutput(ret : Any) : String = {
		implicit val formats = net.liftweb.json.DefaultFormats;
		//Printer.compact(render(write[Output](ret.asInstanceOf[Output])));
		write[Output](ret.asInstanceOf[Output]);
	}
	
}