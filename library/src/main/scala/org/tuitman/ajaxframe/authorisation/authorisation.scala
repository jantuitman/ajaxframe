package org.tuitman.ajaxframe.authorisation;
import org.tuitman.ajaxframe.AjaxClass;
import org.tuitman.ajaxframe.AjaxHttpContext;
import org.tuitman.ajaxframe.mongo._;
import org.tuitman.ajaxframe.Emailer;
import org.tuitman.ajaxframe.Config;

import net.liftweb.json.JsonDSL._;
import com.mongodb.DBObject;
import net.liftweb.json.JsonAST.JObject;

import java.util.UUID;
import scala.util.Random;

case class AuthRole(roleName : String) ;

case class AjaxResetPassword(email :String);
case class AjaxAuthentication(email : String, password : String);
case class AjaxAuthenticationResponse(message : String);

case class AppUser(_id: String, email :String, password: String, roles: List[AuthRole],validationGuid: String) extends MongoDocument[AppUser] {
	def meta = AppUser;
};

object AppUser extends MongoDocumentCustomMeta[AppUser] {
  override def collectionName = "appUser"
  override def clzz = classOf[AppUser];	
}




class DefaultAuthentication extends AjaxClass {


	def login  =ajaxContextAwareMethod[AjaxAuthentication,AjaxAuthenticationResponse] (AuthRole("none")) {
			( auth : AjaxAuthentication, ctx: AjaxHttpContext) => 
			
			
			
			AppUser.find(("email" -> auth.email ) ~ ("password" -> Digester.sha1digest(auth.password))) match {
				case Some(AppUser(_id,email,password,roles,_)) =>
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
		( auth : AjaxAuthentication,ctx: AjaxHttpContext) => 
		AppUser.find("email" -> auth.email) match {
			case Some(AppUser(_id,email,digestedPassword,roles,_)) =>
				AjaxAuthenticationResponse("User already exists!");
			case None => {
				val roles = List(AuthRole("user"));
				val uuid=UUID.randomUUID();
				AppUser(null,auth.email,Digester.sha1digest(auth.password),roles,uuid.toString()).save ;
				Emailer.sendMail(auth.email,"Thank you for registering!",
				    "Thank you for registering at "+Config.instance.getEmailProperties.getProperty("sitename")+"""|
	                |Please confirm your email adress by visiting the following link 
	                |""".stripMargin
	                + "http://localhost:8080/ajax/confirmEmail?"+uuid.toString(),
	                None
				);
				AppUser.find(("email" -> auth.email ) ~ ("password" -> Digester.sha1digest(auth.password))) match {
					case Some(AppUser(_id,email,password,roles,_)) =>
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
	
	def confirmEmail = ajaxContextAwareMethod[AjaxAuthenticationResponse](AuthRole("none")) {
		(ctx: AjaxHttpContext) => 
		val uuid=ctx.req.getQueryString();
		// TODO test if uuid == "confirmed"
		if (uuid != "confirmed") { 
			AppUser.find("validationid" -> uuid) match {
				case Some(x) => {
					x.copy(validationGuid = "confirmed").save
					ctx.resp.sendRedirect("/"); 
					AjaxAuthenticationResponse("redirected");
				}
				case None => {
					AjaxAuthenticationResponse("failure");
				}
			}
		}
		else {
			ctx.resp.sendRedirect("/"); 
			AjaxAuthenticationResponse("redirected");
		}
	}
	
	def resetPassword=ajaxContextAwareMethod[AjaxResetPassword,AjaxAuthenticationResponse](AuthRole("none")) {
		(a : AjaxResetPassword,ctx: AjaxHttpContext) => 
		
		def generateChar : String = {
			val s="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
			val i=Random.nextInt(s.length);
			s.substring(i,i+1);
		}
		
		def generatePassword : String = {
			val l=for ( i <- 0 until 10) yield if (i % 3 ==0 ) ""+Random.nextInt(10) else generateChar; 
			l.mkString ;
		}
		
		val newPassword=generatePassword;
		AppUser.find("email" -> a.email) match {
			case Some(x) => {
				x.copy(password = Digester.sha1digest(newPassword)).save ;
				Emailer.sendMail(a.email,"Your password was reset",("""|
					Your password at """+Config.instance.getEmailProperties.getProperty("sitename")+""" was reset. The new password is 
					|
					|""").stripMargin+newPassword,
					None
				)	
			}
			case None => ;
		}
		ctx.resp.sendRedirect("/"); 
		AjaxAuthenticationResponse("redirected");
	}

	def logout = ajaxContextAwareMethod[AjaxAuthenticationResponse](AuthRole("user")) {
		(ctx: AjaxHttpContext) => 
		val session=ctx.req.getSession(true);
		session.invalidate();		
		AjaxAuthenticationResponse("logged out now!")
	}
}