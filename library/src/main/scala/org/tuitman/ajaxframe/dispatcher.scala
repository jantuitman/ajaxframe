package org.tuitman.ajaxframe;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;

class Dispatcher extends Filter {
    
    var ajaxDispatcher : AjaxDispatcher = null;    

    override def init(config : FilterConfig) {
	   ajaxDispatcher = new AjaxDispatcher ;
	   ajaxDispatcher.init(config.getInitParameter("ajaxClass"));
	   // add configuration here.
	   println("configuration class"+config.getInitParameter("configurationClass"));
	   val c = Class.forName(config.getInitParameter("configurationClass"));
	   Config.instance = c.newInstance().asInstanceOf[Configuration];
    }

	override def doFilter(request: ServletRequest, response: ServletResponse,chain :FilterChain) {
		val req = request.asInstanceOf[HttpServletRequest] ;
		val resp = response.asInstanceOf[HttpServletResponse] ;
		
		val path=req.getRequestURI().split("/").toList;
		val path2 = if (path.length == 0) {
			List("index.html")
		} else {
		 path tail;
		}
		if (path2.head == "ajax") {
			ajaxDispatcher.dispatch(path2.tail,req,resp)
		}
		else {
			// forward the call to the chain. 
			chain.doFilter(request,response);
		}
	}
	
	override def destroy() {
		
	}
	
}
