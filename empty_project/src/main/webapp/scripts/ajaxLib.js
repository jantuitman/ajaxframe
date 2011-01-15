if (typeof Ajax == "undefined") Ajax={};

function AjaxCallTemplate(name,example) {
	this.name=name;
	this.example=example;
	this.authhandler=new AuthorisationHandler();
}

AjaxCallTemplate.prototype.call=function(param,success,error,authhandler) {
	var self=this;
	if (authhandler==null) authhandler=this.authhandler;
	var d=JSON.stringify(param,function(k,v) { if (v==null) return null ; else return v;},3);
	$.ajax({
	  url: "/ajax/"+this.name,
	  type: 'POST',
	  dataType: 'json',
	  data: d,
	  success: function(result) {
	     // check if callback is defined.
	     if (success) {
		     success(result);
	     }
	     else alert("unhandled call result in ajaxcall '"+name+"': "+JSON.stringify(result,null,3)); 
	  },
	  error: function(result) {
		 if (result.status == 403) {
		  	if (authhandler.handle403) {
				 /* handle the 403 (which will present a login screen ) and when done, 
				    reexecute the call.
				 */ 
			     authhandler.handle403(function (){
					self.call(param,success,error,authhandler)
			     },error);
		    }
		 } 
		 if (error) error(result) ;
		 else {
			self.defaultErrorHandler(result);
		 } 
	  }
	});
}

/*
   creates a param from a raw json object:
   all properties in the example object are filled in with
       String, Number -> null or the value present in raw.
       Array -> [] or the value present in raw.
       Object -> recursive fill in. 
*/
AjaxCallTemplate.prototype.param=function(raw) {

   function enrichRawParam(raw,example) {
		if ($.isArray(example)) {
			if (raw==null) return [] ;
			else {
				if (! $.isArray(raw)) throw new Error("array expected");
				var arr=[];
				for (var i=0; i<raw.length;i++) {
					arr.push(enrichRawParam(raw[i],example[0]));
				}
				return arr;
			}
		}
		if (typeof(example)=="object") {
			if (raw==null) raw=new Object(); // object without properties.
			var o={};
			for (var v in example) {
				console.log(v);
				o[v]=enrichRawParam(raw[v],example[v]);
			}
			console.log("JSON stringify "+JSON.stringify(o,function (k,v) { if (v==null) return null ; else return v;},3));
			return o;
		}
		if (typeof(example)=="string" || typeof(example)=="number") {
			return raw;
		}
		else throw new Error("Unknown type "+typeof(example));
   }
    
   var result = enrichRawParam(raw,this.example);
   //alert(JSON.stringify(result,function (k,v) { if (v==null) return null ; else return v;},3));
   return result;
}


AjaxCallTemplate.prototype.defaultErrorHandler=function(result) {
	alert("error ajaxCall: "+result.status+" "+result.statusText);
}


/****** authorisation stuff *****/

function AuthorisationHandler() {
	
}

AuthorisationHandler.prototype.handle403=function(success, error) {


	Templating.render('login.html',{},function (h) {
			h.appendTo('body');
			$('#loginform',h).submit(function () {
				Ajax.login.call(Templating.extractJson(h,{ 'email': null, 'password': null}),
						function (result) {
							
							if (result.message != 'Logged in!') {
								$("#msg",h).html(result.message);
							}
							else {
								h.remove();
								success();
							}
						}
				)
				return false;
			})
	})
}


