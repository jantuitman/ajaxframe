function AjaxCallTemplate(name,example) {
	this.name=name;
	this.example=example;
}

AjaxCallTemplate.prototype.call=function(param,success,error) {
	var self=this;
	var d=JSON.stringify(param,function(k,v) { if (v==null) return null ; else return v;},3);
	alert(d)
	$.ajax({
	  url: "/ajax/"+this.name,
	  type: 'POST',
	  dataType: 'json',
	  data: d,
	  success: function(result) {
	     if (success) {
		     success(result);
	     }
	     else alert("unhandled call result in ajaxcall '"+name+"': "+JSON.stringify(result,null,3)); 
	  },
	  error: function(result) {
		 alert("error in AjaxCall "+result);
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
	alert("error in ajaxCall::: "+result);
}
