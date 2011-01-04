package org.tuitman.statelesswf;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.io.Reader;
import java.io.InputStreamReader;
import scala.collection.mutable.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.tuitman.statelesswf.reflection.Reflection._;
import net.liftweb.json.JsonAST._;
import net.liftweb.json.Printer;

import java.io.PrintWriter;




class AjaxDispatcher {
	
	private var ajaxInstance : Any = null;
	private var functionList = new HashMap[String,Tuple3[Class[_],Class[_],Function1[Reader,String]]];
	
	def init(className : String) = synchronized {
		def dispatchingFunction(extractor : JsonExtractor[_,_], inputType : Class[_], outputType: Class[_],method : Method) : Function1[Reader,String] = {
			(is : Reader) => {
				       val returnValue : Any = method.invoke(inputType.cast(extractor.getInput(is)));
				       extractor.makeOutput(outputType.cast(returnValue));
			}
		}
		
		val ajaxClass= Class.forName(className);
		ajaxInstance = ajaxClass.newInstance(); 
		val list=List.fromArray(ajaxClass.getMethods());
		for (method <- list if (method.getDeclaringClass() == ajaxClass)
		) {
			println(method.getName());
			//val annotation : AjaxMethod = method.getAnnotations()(0).asInstanceOf[AjaxMethod];
			
			val ajaxMethod=method.invoke(ajaxInstance).asInstanceOf[Tuple3[Class[_],Class[_],Function1[Reader,String]]];
			
			//import myapp.ajax.User;
			functionList	+= method.getName() -> ajaxMethod  
			/*         
			        Tuple3(
						classOf[User],
						classOf[User],
						fn
				     )
			*/  
		}
	}
	
	def dispatch(path : List[String],req : HttpServletRequest, resp: HttpServletResponse) : Unit = {
		val s=path.head;
		if ( s =="ajaxConsole") {
			val out=resp.getWriter();
			makeAjaxConsole(out);
		}
		else {
			functionList(s) match {
				case (_,_,fn : Function1[Reader,String]) => {
					resp.getWriter.println(fn(new InputStreamReader(req.getInputStream())))	
				}
				case _ => resp.sendError(HttpServletResponse.SC_NOT_FOUND,"this ajaxcall does not exist.");
			}
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
			<script>
			     function sendReq() {
						g_ajaxConsole.sendRequest($('#functionName').val(),$('#ajaxInput'),$('#ajaxOutput'))				
			     }
			
			</script>
			<body>""");
			out.println("<div><select id=\"functionName\" onchange=\"g_ajaxConsole.loadTemplate(this)\" >")
			out.println("<option value=\"\" SELECTED>select ajax function</option>");
			for((name,value) <- functionList) {
				out.println("<option value=\""+name+"\">"+name+"</option>");
				value match {
					case (input : Class[_],output,function) => {
						// hmmmm...
						val json=makeJsonExample(input);
						out.println("<script>g_ajaxConsole.addTemplate('"+name+"','"+Printer.compact(render(json))+"')</script>")

					}
					case _ => {}
				}
			}
			out.println("</select></div>");
			out.println("""
			<textarea id="ajaxInput" style="width:500px;height:250px">&nbsp;</textarea><br/>
			<input type="button" onclick="sendReq()" value="Send" /><br/>
			<input type="button" onclick="alert('there!')" value="test" />
			<hr/>
			<div id="ajaxOutput" ></div>
			""")

			out.println("</body>");

		}	
	
}