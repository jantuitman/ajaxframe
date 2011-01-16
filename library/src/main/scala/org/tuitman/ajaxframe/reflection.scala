package org.tuitman.ajaxframe.reflection;
import java.lang.reflect.{Constructor => JConstructor, Field, Type, ParameterizedType}

object Reflection { 
    
    import com.thoughtworks.paranamer._
    
    private val paranamer = new CachingParanamer(new BytecodeReadingParanamer)    

	private class Memo[A, R] {
	  private var cache = Map[A, R]()

	  def memoize(x: A, f: A => R): R = synchronized {
	    if (cache contains x) cache(x) else {
	      val ret = f(x)
	      cache += (x -> ret)
	      ret
	    }
	  }
	}

    	
	private val cachedConstructorArgs = new Memo[JConstructor[_], List[(String, Class[_], Type)]]

	def constructorArgs(constructor: JConstructor[_]): List[(String, Class[_], Type)] = {
	      def argsInfo(c: JConstructor[_]) = {
	        val Name = """^((?:[^$]|[$][^0-9]+)+)([$][0-9]+)?$"""r
	        def clean(name: String) = name match {
	          case Name(text, junk) => text
	        }
	        try {
	          val names = paranamer.lookupParameterNames(c).map(clean)
	          val types = c.getParameterTypes
	          val ptypes = c.getGenericParameterTypes
	          zip3(names.toList, types.toList, ptypes.toList)
	        } catch {
	          case e: ParameterNamesNotFoundException => Nil
	        }
	      }

	      cachedConstructorArgs.memoize(constructor, argsInfo(_))
	}

	def primaryConstructorArgs(c: Class[_]) = constructorArgs(c.getDeclaredConstructors()(0))


	// FIXME Replace this with Tuple3.zipped when moving to 2.8
	private def zip3[A, B, C](l1: List[A], l2: List[B], l3: List[C]): List[(A, B, C)] = {
	  def zip(x1: List[A], x2: List[B], x3: List[C], acc: List[(A, B, C)]): List[(A, B, C)] =
	    x1 match {
	      case Nil => acc.reverse
	      case x :: xs => zip(xs, x2.tail, x3.tail, (x, x2.head, x3.head) :: acc)
	    }

	  zip(l1, l2, l3, Nil)
	}


}