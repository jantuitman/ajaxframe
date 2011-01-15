
function Templ() {
	this.templates={};
	this.i18nTable={};
	return this;
}


Templ.prototype.init=function() {
	// load internationalisation table:
	var self=this;
	$.ajax({ 
		url: '/templates/internationalisation.'+navigator.language+'.js',
		type: 'GET',
		success: function (result) {
			eval("var a="+result);
			self.i18nTable=a;
		},
		error: function () {
			$.ajax({
				url: '/templates/internationalisation.js',
				type: 'GET',
				success: function (result) {
					eval("var a="+result);
					self.i18nTable=a;
				}
			});
		}
	})
}


Templ.prototype.render=function(name,vars,success) {

	if (this.templates[name]) {
		// render template
		success($.tmpl( this.templates[name],vars));
	}
	else {
		var self=this;
		$.ajax({ 
			url: '/templates/'+name,
			type: 'GET',
			success: function (result) {
				// i18n
				result=result.replace(/%%([a-zA-Z\_0-9]+)%%/g,function(varname){ 
					varname=varname.substr(2,varname.length-4);
					return self.i18nTable[varname]==null?'':self.i18nTable[varname]
				} )
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

var Templating=new Templ();
Templating.init();
