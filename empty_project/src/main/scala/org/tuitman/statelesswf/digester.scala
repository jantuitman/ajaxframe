package org.tuitman.statelesswf.authorisation;
import java.security.MessageDigest;


object Digester {
	
	
	
	def sha1digest(input : String) : String = {

		def bytes2Hex( bytes: Array[Byte] ): String = {
		    def cvtByte( b: Byte ): String = {
		        (if (( b & 0xff ) < 0x10 ) "0" else "" ) + java.lang.Long.toString( b & 0xff, 16 )
		    }

		    bytes.map( cvtByte( _ )).mkString.toUpperCase
		}
		
		bytes2Hex(MessageDigest.getInstance("SHA1").digest(input.getBytes("UTF-8")));
		
	}
	
	
}