package org.tuitman.ajaxframe;
import java.io.Reader;
import java.lang.reflect.Method;
import org.tuitman.ajaxframe.authorisation.AuthRole;


abstract class AjaxClass {
	
	
	def ajaxMethod[Output <: AnyRef] (role: AuthRole) ( func : () => Output) 
	    : AjaxDescriptor = {
			val extractor=new JsonExtractor[Unit,Output];
			
		    val applyMethods : Array[Method] = 
		  		for (method <- func.getClass.getMethods() if method.getName() == "apply" && method.getDeclaringClass == func.getClass 
		    		) yield method;
		
		    AjaxDescriptor0(classOf[Unit],applyMethods(0).getReturnType(),role,{
				 (ctx : AjaxHttpContext) =>
				    extractor.makeOutput(func())
			})
		
	}
	def ajaxMethod[Input,Output <: AnyRef] (role: AuthRole) ( func : (Input) => Output) (implicit mf : Manifest[Input])
	    : AjaxDescriptor = {
			val extractor=new JsonExtractor[Input,Output];
			
		    val applyMethods : Array[Method] = for (method <- func.getClass.getMethods() if method.getName() == "apply" && method.getDeclaringClass == func.getClass
		         && method.getParameterTypes()(0) != classOf[java.lang.Object]
		    ) yield method;
		
			AjaxDescriptor1(applyMethods(0).getParameterTypes()(0),
		    applyMethods(0).getReturnType(),
		    role,{
				(r : Reader, ctx : AjaxHttpContext) =>
			    	extractor.makeOutput(func(extractor.getInput(r)))
			})
	}

	def ajaxContextAwareMethod[Output <: AnyRef] (role: AuthRole) ( func : (AjaxHttpContext) => Output) 
	    : AjaxDescriptor = {

			val extractor=new JsonExtractor[Unit,Output];

		    val applyMethods : Array[Method] = for (method <- func.getClass.getMethods() if method.getName() == "apply" && method.getDeclaringClass == func.getClass
		         && method.getParameterTypes()(0) != classOf[java.lang.Object]
		    ) yield method;
		
			AjaxDescriptor0(applyMethods(0).getParameterTypes()(0),
		    applyMethods(0).getReturnType(),
		    role,{
				(ctx : AjaxHttpContext) =>
			    	extractor.makeOutput(func(ctx))
			})
	}

	
	def ajaxContextAwareMethod[Input,Output <: AnyRef] (role: AuthRole) ( func : (Input,AjaxHttpContext) => Output) (implicit mf : Manifest[Input])
	    : AjaxDescriptor = {

			val extractor=new JsonExtractor[Input,Output];

		    val applyMethods : Array[Method] = for (method <- func.getClass.getMethods() if method.getName() == "apply" && method.getDeclaringClass == func.getClass
		         && method.getParameterTypes()(0) != classOf[java.lang.Object]
		    ) yield method;
		
			AjaxDescriptor1(applyMethods(0).getParameterTypes()(0),
		    applyMethods(0).getReturnType(),
		    role,{
				(r : Reader, ctx : AjaxHttpContext) =>
			    	extractor.makeOutput(func(extractor.getInput(r),ctx))
			})
	}
	
	
	
}


