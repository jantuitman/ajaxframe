if (typeof AjaxFrame == "undefined") AjaxFrame={};
if (typeof AjaxFrame.Ajax == "undefined") AjaxFrame.Ajax={};

(function (global) {

	var AjaxCallTemplate=function(name,example) {
		this.name=name;
		this.example=example;
	}
	
	
	AjaxCallTemplate.prototype.call=function(param,success,error,authhandler) {
		var self=this;
		if (authhandler==null) authhandler=AjaxFrame.authorisationHandler;
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
    
    

    // register class. 
    if (typeof global.AjaxCallTemplate == "undefined") global.AjaxCallTemplate=AjaxCallTemplate;

// end of definition.
})(AjaxFrame)




/****** authorisation stuff *****/

var x=(function (global) {

	var AuthorisationHandler=function() {
	
	}

	AuthorisationHandler.prototype.handle403=function(success, error) {

	    var self=this;
		AjaxFrame.Templating.render('login.html',{},function (h) {
				h.appendTo('body');
				$('#loginform',h).submit(function () {
					AjaxFrame.Ajax.login.call(AjaxFrame.Templating.extractJson(h,{ 'email': null, 'password': null}),
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
				});
				$("#registerAccount",h).click(function(){
					self.registerAccount(success,error);
				})
				$("#forgotPassword",h).click(function(){
					self.resetPassword();
				})
		})
	}

	AuthorisationHandler.prototype.registerAccount=function(success,error) {
	   $("#loginDiv").remove();
	   $("#mainDiv").hide();
	   var self=this;
	   AjaxFrame.Templating.render('register_account.html',{},function (h) {
			h.appendTo('body');
			// to do. add event handling here.
			$('#registerform',h).submit(function () { 
				if ($('#password').val() != $('#password2').val()) {
					$("#msg",h).html("passwords don't match");
					return false;
				}  
			
				AjaxFrame.Ajax.registerAccount.call(AjaxFrame.Templating.extractJson(h,{ 'email': null, 'password': null}),
					function (result) {
						alert("register account:"+result.message);
						h.remove();
					   	$("#mainDiv").show();
					}
				);
				return false;
			});	
		});	
	}
    
	if (typeof global.AuthorisationHandler == "undefined") global.AuthorisationHandler=AuthorisationHandler;
	if (typeof global.authorisationHandler == "undefined") global.authorisationHandler=new AuthorisationHandler();

// end of definition.
})

x(AjaxFrame)
