package org.tuitman.statelesswf;
import java.io.Reader;
import java.lang.reflect.Method;


abstract class AjaxClass {
	
	
	def ajaxMethod[Input,Output <: AnyRef] ( func : (Input) => Output) (implicit mf : Manifest[Input])
	    : Tuple3[Class[_],Class[_],(Reader) => String] = {
			val extractor=new JsonExtractor[Input,Output];
			
		    val applyMethods : Array[Method] = for (method <- func.getClass.getMethods() if method.getName() == "apply" && method.getDeclaringClass == func.getClass
		         && method.getParameterTypes()(0) != classOf[java.lang.Object]
		    ) yield method;
			
			return ( 
			    applyMethods(0).getParameterTypes()(0),
			    applyMethods(0).getReturnType(),
			{
				(r : Reader) =>
				    extractor.makeOutput(func(extractor.getInput(r)))
			})
	}
	
}