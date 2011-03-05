package org.tuitman.ajaxframe;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Constructor;
import org.tuitman.ajaxframe.reflection.Reflection._;
import net.liftweb.json.JsonAST._;
import scala.reflect.ClassManifest;

object JsonExtract {
	
	
	
	def extract (json :JValue,returnType : Class[_]) : AnyRef = {
		
		def fieldValue(fields : List[JField], name : String) :Option[JValue] = {
			val l=for(JField(n,v) <- fields if n==name) yield JField(n,v);
			l match {
				case Nil => None ;
				case List(JField(n,v)) => Some(v);
			}
		}
		
		/**
		   converts a json object to a simple value (String, Int, Object) but not Array.
		   name is just needed to throw the right error message if value isn't of type requestedType.
		*/
		def convertSimpleValue(requestedType : Class[_],v : Option[JValue],name : String) : Object = {
			if (requestedType == classOf[String]) {
			    v match {
					case Some(JString(s)) => s;
					case Some(JNull) => null;
					case Some(x) => throw new RuntimeException("String expected for field '"+name+"', found "+x+" is null?"+(x==null));
					case None => null;
				}
			}
			else if (requestedType == classOf[Int]) {
			    v match {
					case Some(JInt(i)) => i.asInstanceOf[Object];
					case Some(JNull) => 0.asInstanceOf[AnyRef]
					case Some(_) => throw new RuntimeException("Int expected for field '"+name+"'");
					case None => 0.asInstanceOf[AnyRef];
				}
			}
			else if (requestedType == classOf[Boolean]) {
			    v match {
					case Some(JBool(b)) => b.asInstanceOf[Object];
					case Some(JNull) => false.asInstanceOf[AnyRef];
					case Some(_) => throw new RuntimeException("Bool expected for field '"+name+"'");
					case None => false.asInstanceOf[AnyRef];
				}
			}
			else {
				// class is another object, so recursively invoke extract.
				v match {
					case Some(x: JObject) => extract(x,requestedType);
					case Some(JNull) => null;
					case None => null;
					case Some(_) => throw new RuntimeException("Json object expected for field '"+name+"'");
				}
			}
			
		}
		
		
	    json match {
			case JObject(fields: List[_]) => {
				val params : List[AnyRef]=for ( (name,clss,genericType) <- primaryConstructorArgs(returnType)) yield {
					
					//println(" parameter "+name);
					val v=fieldValue(fields,name);
					if (genericType.isInstanceOf[ParameterizedType]) {
						 //println("Parameterized type found type found in constructor args: ownertype "+genericType.asInstanceOf[ParameterizedType].getOwnerType().toString())
						 if (genericType.asInstanceOf[ParameterizedType].getRawType().toString().endsWith("List")) {
							// maybe check if we are dealing with an array.
							v match {
								case Some(JArray(l)) => {
									// all jvalues need to be converted.
									l.map( (json) => convertSimpleValue(
										genericType.asInstanceOf[ParameterizedType]
											.getActualTypeArguments()(0).asInstanceOf[Class[_]]
									    ,Some(json)
									    ,name
										)
									)
									
								}
								case None => {
									List[Object]()
								}
								case _ => throw new RuntimeException("Array expected for field '"+name+"'");
							}
						}
						else {
							null;
						}
					}
					else {
						// no generic class, so the parameter is an object or a primitive value.
						convertSimpleValue(clss,v,name);
					} 
				};
				//val x=params.asInstanceOf[List[Object]];
				//var y : Array[AnyRef]=new Array[AnyRef](params.length);
				//for (i <- 0 to (y.length)) y(i)=params(i);
				val cc : Constructor[_]=returnType.getDeclaredConstructors()(0);
				//println(" we are going to construct with params "+params);
				cc.newInstance(params.toArray[Object] : _*).asInstanceOf[AnyRef];
			}
			case _ => throw new RuntimeException("expected json object, found array or primitive.");
		
		} 
	}
	
	
	
}