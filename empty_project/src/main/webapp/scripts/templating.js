var Templating=new Templ();


function Templ() {
	this.templates={};
}

Templ.prototype.render=function(name,vars,success) {
	
	if (this.templates[name]) {
		success($.tmpl( this.templates[name],vars));
	}
	else {
		var self=this;
		$.ajax({ 
			url: '/templates/'+name,
			type: 'GET',
			success: function (result) {
				self.templates[name]=$.template(null,result);
				self.render(name,vars,success);
			}
		})
	}
}	
	
Templ.prototype.extractJson=function(element, exampleObject) {
	
	var result={};
	for (var v in exampleObject) {
		var val=$('#'+v,element).val();
		if ((val==null || val=='' ) && exampleObject[v]!=null && exampleObject[v]!='') {
			val=exampleObject[v];
		}
		result[v]=val;
	}
	return result;
}