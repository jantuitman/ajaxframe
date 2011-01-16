package org.tuitman.statelesswf.authorisation;
import org.tuitman.statelesswf.AjaxClass;
import org.tuitman.statelesswf.AjaxHttpContext;

import net.liftweb.mongodb._;
import net.liftweb.json.JsonDSL._;
import com.mongodb.DBObject;
import net.liftweb.json.JsonAST.JObject;


case class AuthRole(roleName : String) ;

case class AjaxAuthentication(email : String, password : String);
case class AjaxAuthenticationResponse(message : String);

case class AppUser(_id: String, email :String, password: String, roles: List[AuthRole]) extends MongoDocument[AppUser] {
	def meta = AppUser;
};

object AppUser extends MongoDocumentMeta[AppUser] {
  override def collectionName = "appUser"	
}




class DefaultAuthentication extends AjaxClass {





	def login  =ajaxContextAwareMethod[AjaxAuthentication,AjaxAuthenticationResponse] (AuthRole("none")) {
			( auth : AjaxAuthentication, ctx: AjaxHttpContext) => 
			
			
			
			AppUser.find(("email" -> auth.email ) ~ ("password" -> Digester.sha1digest(auth.password))) match {
				case Some(AppUser(_id,email,password,roles)) =>
					val session=ctx.req.getSession(true);
					session.setAttribute("userId",_id);
					session.setAttribute("userRoles",roles);
					AjaxAuthenticationResponse("Logged in!");
				case None => {
					AjaxAuthenticationResponse("Unknown user/password");
				}
			}
	}

	def registerAccount = ajaxContextAwareMethod[AjaxAuthentication,AjaxAuthenticationResponse](AuthRole("none")) {
		( auth : AjaxAuthentication,ctx: AjaxHttpContext) => AjaxAuthenticationResponse("registered now!")
		AppUser.find("email" -> auth.email) match {
			case Some(AppUser(_id,email,digestedPassword,roles)) =>
				AjaxAuthenticationResponse("User already exists!");
			case None => {
				val roles = List(AuthRole("user"));
				AppUser(null,auth.email,Digester.sha1digest(auth.password),roles).save ;
				AppUser.find(("email" -> auth.email ) ~ ("password" -> Digester.sha1digest(auth.password))) match {
					case Some(AppUser(_id,email,password,roles)) =>
						val session=ctx.req.getSession(true);
						session.setAttribute("userId",_id);
						session.setAttribute("userRoles",roles);
						AjaxAuthenticationResponse("User registered!");
					case None => {
						AjaxAuthenticationResponse("Internal errror");
					}
				}
			}
		}
	
	}

	def logout = ajaxContextAwareMethod[AjaxAuthentication,AjaxAuthenticationResponse](AuthRole("user")) {
		( v : AjaxAuthentication,ctx: AjaxHttpContext) => 
		val session=ctx.req.getSession(true);
		session.invalidate();		
		AjaxAuthenticationResponse("logged out now!")
	}
}