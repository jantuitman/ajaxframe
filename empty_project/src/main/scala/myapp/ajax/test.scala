package myapp.ajax;
import org.tuitman.statelesswf.AjaxClass;
import org.tuitman.statelesswf.AjaxMethod;


case class UserAddress(street :String);
case class User(name : String, email : String,list : List[UserAddress]);

class Test extends AjaxClass {
		
  
val addUser = ajaxMethod[User,User] { 
	( u : User) =>
	println("User name "+u.name);
	u;
}  




}