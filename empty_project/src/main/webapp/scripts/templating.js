
function Templ() {
	this.templates={};
	this.i18nTable={};
	return this;
}

Templ.prototype.init=function() {
	var self=this;
	this.initi18n(function () { self.inituagent(function () {}) });
}

Templ.prototype.initi18n=function(success) {
	// load internationalisation table:
	var self=this;
	$.ajax({ 
		url: '/templates/internationalisation.'+navigator.language+'.js',
		type: 'GET',
		success: function (result) {
			eval("var a="+result);
			self.i18nTable=a;
			success();
		},
		error: function () {
			$.ajax({
				url: '/templates/internationalisation.js',
				type: 'GET',
				success: function (result) {
					eval("var a="+result);
					self.i18nTable=a;
					success()
				}
			});
		}
	})
}

Templ.prototype.inituagent=function(success) {
	var self=this;
	$.ajax({ 
		url: '/templates/useragents.js',
		type: 'GET',
		success: function (result) {
			eval("var useragents="+result);
			self.ua='';
			for (var i=0; i < useragents.length;i++) {
				if ((new RegExp(useragents[i].expression)).test(navigator.userAgent)) {
					self.ua=useragents[i].tag;
					console.debug("user agent tag ="+self.ua)
					break;
				}
				else {
					console.debug(navigator.userAgent+" does not match:"+useragents[i].expression);
				}
			}
			success();
		}
	});	
}


/*
    renders template with a specified name, filling the json object vars in in the template.
    if the template wasn't loaded yet, it will be loaded first.
*/
Templ.prototype.render=function(name,vars,success) {
	var self=this;
	if (this.templates[name]) {
		// render template
		success($.tmpl( this.templates[name],vars));
	}
	else {
		console.debug("self.ua="+self.ua);
		// first try to load the template from the dir specific for this useragent.
		self.loadTemplate(name,self.ua,
			function (){
				self.render(name,vars,success)
			},
			function () {
				// if template could not be loaded from there, load it from the /templates dir itself.
				self.loadTemplate(name,null,
					function (){
						self.render(name,vars,success)
					}					
				)
			}
		)
	}
}



Templ.prototype.loadTemplate=function(name,useragent,success,failure) { 
	
	var dir='/templates/'+((useragent!=null&&useragent!='')?'/'+useragent+'/':'/');
	var self=this;
	$.ajax({ 
		url: dir+name,
		type: 'GET',
		success: function (result) {
			// i18n
			result=result.replace(/%%([a-zA-Z\_0-9]+)%%/g,function(varname){ 
				varname=varname.substr(2,varname.length-4);
				return self.i18nTable[varname]==null?'':self.i18nTable[varname]
			} )
			self.templates[name]=$.template(null,result);
			success();
		},
		error: function () {
			if (failure) failure();
		}
	})
}	

/**

  returns a json object with form values. 
  params: element - html element that contains the form from which data must be obtained.
  exampleObject: json object, the keys of this object are supposed to be id's of form elements.
*/
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
