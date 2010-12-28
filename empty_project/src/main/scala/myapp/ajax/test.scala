package myapp.ajax;
import org.tuitman.statelesswf.frontend.AjaxClass;


case class UserAddress(street :String);
case class User(name : String, email : String,list : List[UserAddress]);

class Test extends AjaxClass {
		
    
    val addUser = ajaxMethod[User](classOf[User]) {
		(u : User) => 
			println("User name "+u.name);
			"hello!";
		
	}
	
}