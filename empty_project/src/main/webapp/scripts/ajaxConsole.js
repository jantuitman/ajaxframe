if (!AjaxFrame) {
	AjaxFrame={}
}

(function (global) {

var AjaxConsole=function() {
	
	this.ajaxTemplates={};
	
}



AjaxConsole.prototype.loadTemplate=function(selection) {
	
	if (this.methodName != null) {
		// saveTemplate;
	}
	var methodName=$(selection).val();
	if (this.ajaxTemplates[name]==null) {
		this.ajaxTemplates[name]= { 'default': ''};
	}
	var s=JSON.stringify(this.ajaxTemplates[methodName]['default'],null,3);
  	$("#ajaxInput").val(s);
	this.methodName=methodName;
}

AjaxConsole.prototype.addTemplate=function(name,value){
	if (this.ajaxTemplates[name]==null) {
		this.ajaxTemplates[name]= { 'default': ''};
	}
	this.ajaxTemplates[name]['default']=JSON.parse(value);
}

AjaxConsole.prototype.sendRequest=function(functionName,inputField,outputDiv) {
	var jsonStr=inputField.val();
	outputDiv.html('');
	try {
		eval("var a = "+jsonStr);
	}
	catch(e) {
		alert("Fout in json: \n"+e.message);
		return;
	}
	
	
	AjaxFrame.Ajax[functionName].call(a,
		function (result) {
			console.debug(result);
	        outputDiv.html("call result: "+JSON.stringify(result,null,3));  	
		},
		function (x) {
			outputDiv.html("<span style='color:red;font-weight:bolder'>ERROR: "+x.status+"</span> "+x.statusText);
		}
	);
	
	
    /*	
	$.ajax({
	  url: "/ajax/"+functionName,
	  type: 'POST',
	  dataType: 'json',
	  data: jsonStr,
	  success: function(result) {
	     outputDiv.html("call result: "+JSON.stringify(result,null,3));  	
	  }
	});
	*/
}
   if (typeof global.Console == "undefined" ) global.Console=new AjaxConsole();

})(AjaxFrame)