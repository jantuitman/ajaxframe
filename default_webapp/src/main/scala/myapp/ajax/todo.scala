package myapp.ajax;
import org.tuitman.ajaxframe.AjaxClass;
import org.tuitman.ajaxframe.authorisation._;

import net.liftweb.mongodb._;
import net.liftweb.json.JsonDSL._;
import com.mongodb.DBObject;
import net.liftweb.json.JsonAST.JObject;

case class ToDo(_id : String,description :String, done : Boolean) extends MongoDocument[ToDo] {
	def meta = ToDo;
}

object ToDo extends MongoDocumentMeta[ToDo] {
  override def collectionName = "todo"	
}

class ToDoCalls extends AjaxClass {
		
	val add = ajaxMethod[ToDo,ToDo](AuthRole("user")) { 
		( t : ToDo) => 
		t match {
			case ToDo(id,description,done) => {
				ToDo(null,description,done).save ;
			}
		}
		t;
	}  


	val allOpenItems = ajaxMethod[ToDo,List[ToDo]] (AuthRole("user")) {
		( t : ToDo) =>
		ToDo.findAll("done" -> false);
	}


}