var g_ajaxConsole=new AjaxConsole();


function AjaxConsole() {
	
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


