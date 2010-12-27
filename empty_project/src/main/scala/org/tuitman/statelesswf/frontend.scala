package org.tuitman.statelesswf.frontend ;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Field;
import java.lang.Class;
import scala.collection.mutable.HashMap;
import net.liftweb.json.JsonParser._;
import net.liftweb.json.JsonAST._;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import scala.io.Source;
/*
class AjaxService extends HttpServlet {
	
	
	override def service(request: HttpServletRequest, response: HttpServletResponse) {
		val out=response.getWriter();
		out.println("<h1>Hello world</h1>");
		
	}
	
}
*/

class Dispatcher extends Filter {
    
    var ajaxDispatcher : AjaxDispatcher = null;    

    override def init(config : FilterConfig) {
  	   println("Init method of filter");
       println("Ajax class " + config.getInitParameter("ajaxClass"));
	   ajaxDispatcher = new AjaxDispatcher ;
	   ajaxDispatcher.init(config.getInitParameter("ajaxClass"));
	
	   /*
	   val ajaxClass= Class.forName(config.getInitParameter("ajaxClass"));
	   val methods=List.fromArray(ajaxClass.getMethods());
	   for (method <- methods if (method.getDeclaringClass() == ajaxClass)) {
		  println("method : "+method.getName());
		  println("returntype :"+method.getReturnType().getName());
		  for (parameter <- method.getParameterTypes()) {
		     println("parameter :"+parameter.getName())	
		  } 
		  for (genpar <- method.getGenericParameterTypes()) {
		    if (genpar.isInstanceOf[ParameterizedType])	{
				println("generic parameter");
				val x=genpar.asInstanceOf[ParameterizedType];
				for (parameterclass <- x.getActualTypeArguments ) {
					println("typeargument "+parameterclass.toString());
				}
			}
			else {
				println("no generic parameter "+genpar);
			}
			if (genpar.isInstanceOf[Class[Any]]) {
				println("is a class: "+genpar.asInstanceOf[Class[Any]].getName());
			}
		  } 
	   } 
	   */
	
		
		//val finder = ClassFinder()
		//val classes = finder.getClasses // classes is an Iterator[ClassInfo]
		//val ajaxClasses=ClassFinder.concreteSubclasses("org.tuitman.statelesswf.Ajax", classes)
		//ajaxClasses.foreach(println(_))
    }

	override def doFilter(request: ServletRequest, response: ServletResponse,chain :FilterChain) {
		val req = request.asInstanceOf[HttpServletRequest] ;
		val resp = response.asInstanceOf[HttpServletResponse] ;
		
		val arrPath=List.fromArray(req.getRequestURI().split("/")) tail;
		println(arrPath);
		if (arrPath.head == "ajax") {
			ajaxDispatcher.dispatch(arrPath.tail,req,resp)
		}
		else {
			// forward the call to the chain. 
			chain.doFilter(request,response);
		}
	}
	
	override def destroy() {
		
	}
	
}

/*
abstract class AjaxFn {
	
	def call(json : JValue) : String;
	def getTypeInfo : Class[_] ;
	
}
*/



abstract class AjaxClass {
	def ajaxMethod[T] (function : (T) => String ) (implicit mf : Manifest[T]): Function[JValue,String] = {
		(x : JValue) =>
		
		//import scala.reflect._;
		implicit val formats = net.liftweb.json.DefaultFormats;
		//def classType[T](prefix: Manifest[_], clazz: Predef.Class[_], args: Manifest[_]*):
		//val mf =scala.reflect.Manifest.classType[T](Class[T]);
		
		// dump the manifest.
		//println(" Manifest "+mf.toString());
		//println(mf.typeArguments.mkString(","));
		
		
		
		val x2=x.extract[T](formats,mf);
		function(x2) ;
	}
}

/*
abstract class AjaxClass {
	def ajaxMethod[T] (function : (T) => String ) : AjaxFn[T] = { 
		new AjaxFn() {
			
			def call (json : JValue) : String = {
				function(json.extract[T]);
			}
			
			def getTypeInfo {
				return classOf[T];
			}
			
		}
		
	}
}
*/

class AjaxDispatcher {
	var functionList : HashMap[String,Function[JValue,String]] = new HashMap[String,Function[JValue,String]];
	var ajaxInstance : Any=null;
	
	def init(className : String) {
	   val ajaxClass= Class.forName(className);
	   ajaxInstance = ajaxClass.newInstance(); 
	   val list=List.fromArray(ajaxClass.getMethods());
	   for (method <- list if (method.getDeclaringClass() == ajaxClass)) {
	        println(method.getName());
	        functionList	+= method.getName() -> method.invoke(ajaxInstance).asInstanceOf[Function[JValue,String]];
	   }
	   //functionList.keySet.foreach (println);
	}
	
	def dispatch(path : List[String],req : HttpServletRequest, resp: HttpServletResponse) {
		val s=path.head;
		if (functionList isDefinedAt(s)) {
			println("method "+s+" exists!");
		  	val fn=functionList(s).asInstanceOf[Function[JValue,String]];


		    //var line : String = Source.fromInputStream(req.getInputStream()).getLines.reduceLeft(_ + _);
		    //println(line);
		    
		    val json = parse(new InputStreamReader(req.getInputStream()));
		    
		
		    //val f : Function2[JValue,String]= field.get(ajaxInstance).asInstanceOf[Function2[JValue,String]];
		    //println("type information: "+t);
		    //val result=obj.call(json); 
		    val result = fn(json);
			
			/*
			for (parameter <- method.getParameterTypes()) {
		     	println("parameter :"+parameter.getName())	
		  	} 
		    */
			
		}
		else  {
		    resp.sendError(HttpServletResponse.SC_NOT_FOUND,"this ajaxcall does not exist.");
		}
	}
}




