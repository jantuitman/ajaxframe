package myapp.ajax;
import org.tuitman.ajaxframe.AjaxClass;
import net.liftweb.mongodb._;
import org.tuitman.ajaxframe.authorisation._;

class Test extends AjaxClass {

	MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost("localhost", 27017), "test"))

	val user = classOf[UserCalls];
	val defaultAuthentication=classOf[DefaultAuthentication];
	

}