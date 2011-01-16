package org.tuitman.ajaxframe.frontend ;
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
import net.liftweb.json._;
import net.liftweb.json.Printer;

import org.tuitman.ajaxframe.reflection.Reflection._;

import java.io.PrintWriter;
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
		//val ajaxClasses=ClassFinder.concreteSubclasses("org.tuitman.ajaxframe.Ajax", classes)
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
	def ajaxMethod[T] (clazz: Class[T])(function : (T) => String ) (implicit mf : Manifest[T]): (Function[JValue,String],Class[T]) =( {
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
	},clazz);
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
	var functionList : HashMap[String,Tuple2[Function[JValue,String],Class[_]]] = new HashMap[String,Tuple2[Function[JValue,String],Class[_]]];
	var ajaxInstance : Any=null;
	
	def init(className : String) {
	   val ajaxClass= Class.forName(className);
	   ajaxInstance = ajaxClass.newInstance(); 
	   val list=List.fromArray(ajaxClass.getMethods());
	   for (method <- list if (method.getDeclaringClass() == ajaxClass)) {
	        println(method.getName());
	        functionList	+= method.getName() -> method.invoke(ajaxInstance).asInstanceOf[Tuple2[Function[JValue,String],Class[_]]];
	   }
	   //functionList.keySet.foreach (println);
	}
	
	def dispatch(path : List[String],req : HttpServletRequest, resp: HttpServletResponse) : Unit = {
		val s=path.head;
		if ( s =="ajaxConsole") {
			val out=resp.getWriter();
			makeAjaxConsole(out);
		}
		else if (functionList isDefinedAt(s)) {
			println("method "+s+" exists!");
		  	val (fn,clazz)=functionList(s).asInstanceOf[Tuple2[Function[JValue,String],Class[_]]];


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
	
	
	/*
	   string -> string field
	   int -> number field
	   list<object> -> makeJsonExample(object) : and then wrapped in an JArray.
	   object -> makeJsonExample(classOf(object)) : and then as JObject field.
	*/
	def makeJsonExample(clazz : Class[_]) : JValue = {
        
        def classFromListType(gentype : ParameterizedType) : Class[_] = {
			if (gentype.toString.endsWith("List")) {
				throw new RuntimeException("expecting a list class, cant handle "+gentype);
			}
			else {
				gentype.getActualTypeArguments()(0).asInstanceOf[Class[_]];
			}
        }

        //val methods = for(m <- clazz.getMethods() if (m.getDeclaringClass() == clazz ) yield m;
		val list = primaryConstructorArgs(clazz).map( { 
			case (name,clss,genericType) => 
				if (genericType.isInstanceOf[ParameterizedType]) {
					val arrayElement=makeJsonExample(classFromListType(genericType.asInstanceOf[ParameterizedType]));
					JField(name,JArray(
							List(arrayElement,arrayElement)
						)
					);
				}
				else if (clss.isInstanceOf[Class[String]]) {
					JField(name,JString("String"))
				}
				else if (clss.isInstanceOf[Class[Int]]) {
					JField(name,JInt(1))
				}
				else {
					// object type.
					JField(name,makeJsonExample(clss));
				}	
		}) ;
		JObject(list);
	}

/*
*/	
	def makeAjaxConsole(out : PrintWriter) {
		out.println("<html><head><title>ajaxConsole</title>");
		out.println("""
		<script src="/scripts/jquery-1.4.2.js"></script>
		<script src="/scripts/json.js"></script>
		<script src="/scripts/ajaxConsole.js"></script>
		<body>""");
		out.println("<div><select id=\"functionName\" onchange=\"g_ajaxConsole.loadTemplate(this)\" >")
		out.println("<option value=\"\" SELECTED>select ajax function</option>");
		for((name,value) <- functionList) {
			out.println("<option value=\""+name+"\">"+name+"</option>");
			value match {
				case Tuple2(function,clazz) => {
					// hmmmm...
					val json=makeJsonExample(clazz);
					out.println("<script>g_ajaxConsole.addTemplate('"+name+"','"+Printer.compact(render(json))+"')</script>")
					
					/*
					val constructor=clazz.getConstructors()(0);
					println("For method "+name+" found a constructorwith the arguments:");
					for(param <- constructor.getGenericParameterTypes()) {
						if (param.isInstanceOf[ParameterizedType]) {
		                    val x=param.asInstanceOf[ParameterizedType];
							println("generic param of type:" +x.getRawType().asInstanceOf[Class[Any]].getName());
		                    for (parameterclass <- x.getActualTypeArguments ) {
		                         println("typeargument "+parameterclass.toString());
		                         // lets look if this thing has a constructor.
		                         val nested=parameterclass.asInstanceOf[Class[Any]].getConstructors()(0);
		                         for (n2 <- nested.getParameterTypes()) {
			                        println("...."+n2.toString());
		                         }
		                    }
						}
						else {
							println(" param of type: "+param.asInstanceOf[Class[Any]].getName())
						}
					}
					*/
				}
				case _ => {}
			}
		}
		out.println("</select></div>");
		out.println("""<textarea id="ajaxInput" style="width:500px;height:250px"></textarea>""")
		
		out.println("</body>");
		
	}
}




