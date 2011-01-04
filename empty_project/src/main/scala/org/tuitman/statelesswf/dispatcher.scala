package org.tuitman.statelesswf;

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
    }

	override def doFilter(request: ServletRequest, response: ServletResponse,chain :FilterChain) {
		val req = request.asInstanceOf[HttpServletRequest] ;
		val resp = response.asInstanceOf[HttpServletResponse] ;
		
		val arrPath=List.fromArray(req.getRequestURI().split("/")) tail;
        // TODO: protect against nullpointerexception when no slashes in uri. 
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
