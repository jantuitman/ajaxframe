package org.tuitman.ajaxframe;
import java.io.Reader;
import org.tuitman.ajaxframe.authorisation._;


trait AjaxDescriptor;
case class AjaxDescriptor0(inputType : Class[_],outputType : Class[_],role: AuthRole,function: Function1[AjaxHttpContext,String]) extends AjaxDescriptor;
case class AjaxDescriptor1(inputType : Class[_],outputType : Class[_],role: AuthRole,function: Function2[Reader,AjaxHttpContext,String]) extends AjaxDescriptor;