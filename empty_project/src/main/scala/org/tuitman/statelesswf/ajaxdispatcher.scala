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
import org.tuitman.statelesswf.authorisation._;



case class AjaxHttpContext(req : HttpServletRequest, resp: HttpServletResponse ) ;


class AjaxDispatcher {

	type AjaxDescriptor = Tuple4[Class[_],Class[_],AuthRole,Function2[Reader,AjaxHttpContext,String]];
	
	//private var ajaxInstance : Any = null;
	private var functionList = new HashMap[String,AjaxDescriptor];
	
	def init(className : String) : Unit = synchronized {
		
		def retrieveMethods(ajaxClass: Class[_],ajaxInstance: Any) : Unit = {
			val list=List.fromArray(ajaxClass.getMethods());
			for (method <- list if (method.getDeclaringClass() == ajaxClass)
			) {
				println(method.getName());
				//val annotation : AjaxMethod = method.getAnnotations()(0).asInstanceOf[AjaxMethod];
				method.invoke(ajaxInstance) match {
					case x : Class[_] => {
						val ajaxInstance=x.newInstance();
						retrieveMethods(x,ajaxInstance);
					}
					case  ajaxDescriptor : AjaxDescriptor => {
						functionList	+= method.getName() -> ajaxDescriptor;
					}
					case _ => {
						println("Skipped unknown value "+method.getName()+" in class");
					} 
				}
			}
		}
		
		val ajaxClass= Class.forName(className);
		val ajaxInstance : Any = ajaxClass.newInstance();
		retrieveMethods(ajaxClass,ajaxInstance)
	}
	
	def dispatch(path : List[String],req : HttpServletRequest, resp: HttpServletResponse) : Unit = {
		
		def withAuthorisation(role : AuthRole, ctx : AjaxHttpContext)(fn : AjaxHttpContext => Unit) {
			role match {
				case AuthRole("none") => fn(ctx);
				case r : AuthRole => {
					val session=ctx.req.getSession(true);
					session.getAttribute("userRoles") match {
						case l: List[AuthRole] =>  {
							if (l.contains(role)) {
								try 
								{
									fn(ctx);
								}
								catch {
									case e : Throwable => {
										resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR," an error occured in the execution of the ajax call");
										throw new RuntimeException(e);
									}
								}
							}
							else {
								resp.sendError(HttpServletResponse.SC_FORBIDDEN," Authorisation failure");
							}
						}
						case _ => {
							resp.sendError(HttpServletResponse.SC_FORBIDDEN," Authorisation failure");
						}
					}
				} 
			}
		}
		
		
		val s=path.head;
		s match {
			case "ajaxConsole" => {
				val out=resp.getWriter();
				makeAjaxConsole(out);
			}
			case "ajaxProxy.js" => {
				resp.setContentType("application/javascript");
				val out=resp.getWriter();
				makeAjaxProxy(out);
			}
			case s : String => {
				val ctx = AjaxHttpContext(req,resp)
				functionList(s) match {
					case (_,_,role,fn : Function2[Reader,AjaxHttpContext,String]) => {
						withAuthorisation(role,ctx) { ctx : AjaxHttpContext =>
							resp.getWriter.println(fn(new InputStreamReader(ctx.req.getInputStream()),ctx))	
						}
					}
					case _ => resp.sendError(HttpServletResponse.SC_NOT_FOUND,"this ajaxcall does not exist.");
				}
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
		<script src="/scripts/jquery.templ.js"></script>
		<script src="/scripts/json.js"></script>
		<script src="/scripts/templating.js"></script>
		<script src="/scripts/ajaxConsole.js"></script>
		<script src="/scripts/ajaxLib.js"></script>
		<script src="/ajax/ajaxProxy.js"></script>
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
				case (input : Class[_],output,role,function) => {
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
	
	def makeAjaxProxy(out : PrintWriter) {
		out.println("var Ajax = {");
	    var methods = List[String]();
	    for((name,value) <- functionList) {
			value match {
				case (input : Class[_],output,role,function) => {
					val json=makeJsonExample(input);
					val s=name + ": new AjaxCallTemplate('"+name+"',"+Printer.compact(render(json))+")" 
					methods = s :: methods ;

				}
				case _ => {};
			}
		} 
		out.println(methods.mkString(","))
		out.println("}");
	}	
}