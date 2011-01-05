package myapp.ajax;
import org.tuitman.statelesswf.AjaxClass;
import net.liftweb.mongodb._;
import net.liftweb.json.JsonDSL._;
import com.mongodb.DBObject;
import net.liftweb.json.JsonAST.JObject;

case class UserAddress(street :String);
case class User(_id: String, name : String, email : String,list : List[UserAddress]) extends MongoDocument[User] {
	def meta = User;
};

object User extends MongoDocumentMeta[User] {
  override def collectionName = "myusers"	
}

class UserCalls extends AjaxClass {
		
	val addUser = ajaxMethod[User,User] { 
		( u : User) => u match {
			case User(id,name,email,list) => {
				User(null,name,email,list).save ;
			}
		}
		u;
	}  


	val findUserByName = ajaxMethod[User,List[User]] {
		( u : User) =>

		User.findAll("name" -> u.name);
	}

	val allUsers = ajaxMethod[User,List[User]] {
		( u : User) =>
    
	    User.findAll(JObject(Nil));	
	}


	val updateUser = ajaxMethod[User,User] {
		( u : User) =>
    
	    User.update("name" -> u.name,u );
	    u;
	
	}


	val deleteUser= ajaxMethod[User,User] {
		( u : User) =>
    
	    User.find("name" -> u.name) match {
		   case Some(user) => user.delete;
		   case _ => ; 
	    }
	    u;	
	}

}