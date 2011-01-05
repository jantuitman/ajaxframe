package myapp.ajax;
import org.tuitman.statelesswf.AjaxClass;
import net.liftweb.mongodb._;


class Test extends AjaxClass {

	MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost("localhost", 27017), "test"))

	val user = classOf[UserCalls];
	
	

}