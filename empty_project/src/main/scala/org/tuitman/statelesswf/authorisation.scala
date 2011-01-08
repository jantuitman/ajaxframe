package org.tuitman.statelesswf.authorisation;
import org.tuitman.statelesswf.AjaxClass;
import org.tuitman.statelesswf.AjaxHttpContext;

import net.liftweb.mongodb._;
import net.liftweb.json.JsonDSL._;
import com.mongodb.DBObject;
import net.liftweb.json.JsonAST.JObject;




abstract class AppRole;

/* for unauthenticated users */
case class Unauth extends AppRole; 
case class AuthRole(roleName : String) extends AppRole;
case class AdminRole extends AppRole;

case class AjaxAuthentication(email : String, password : String);
case class AjaxAuthenticationResponse(message : String);

case class AppUser(_id: String, email :String, password: String, roles: List[AuthRole]) extends MongoDocument[AppUser] {
	def meta = AppUser;
};

object AppUser extends MongoDocumentMeta[AppUser] {
  override def collectionName = "appUser"	
}




class DefaultAuthentication extends AjaxClass {





	def login  =ajaxContextAwareMethod[AjaxAuthentication,AjaxAuthenticationResponse](Unauth()) {
			( auth : AjaxAuthentication, ctx: AjaxHttpContext) => 
			
			
			
			AppUser.find(("email" -> auth.email ) ~ ("password" -> Digester.sha1digest(auth.password))) match {
				case Some(AppUser(_id,email,password,roles)) =>
					AjaxAuthenticationResponse("Logged in!");
				case None => {
					AjaxAuthenticationResponse("Unknown user/password");
				}
			}
	}

	def register = ajaxContextAwareMethod[AjaxAuthentication,AjaxAuthenticationResponse](Unauth()) {
		( auth : AjaxAuthentication,ctx: AjaxHttpContext) => AjaxAuthenticationResponse("registered now!")
		AppUser.find("email" -> auth.email) match {
			case Some(AppUser(_id,email,digestedPassword,roles)) =>
				AjaxAuthenticationResponse("User already exists!");
			case None => {
				AppUser(null,auth.email,Digester.sha1digest(auth.password),List(AuthRole("user"))).save ;
				AjaxAuthenticationResponse("User registered");
			}
		}
	
	}

	def logout = ajaxContextAwareMethod[AjaxAuthentication,AjaxAuthenticationResponse](AuthRole("*")) {
		( v : AjaxAuthentication,ctx: AjaxHttpContext) => AjaxAuthenticationResponse("logged out now!")
	}
}