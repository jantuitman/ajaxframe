package org.tuitman.ajaxframe;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;


object Emailer {
	
	def sendMail(to : String, subject : String, textBody : String, htmlBody : Option[String])
  	{
    	val session = Session.getDefaultInstance( Config.instance.getEmailProperties, null );
        val message = new MimeMessage( session );
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      	message.setSubject( subject );
      	  
		val content = new MimeMultipart("alternative");
		val text = new MimeBodyPart();
		val html = new MimeBodyPart();

		text.setText(textBody);
		text.setHeader("MIME-Version" , "1.0" );
		text.setHeader("Content-Type" , text.getContentType() );
        
        htmlBody match {
			case Some(x) => html.setContent(htmlBody, "text/html");
			case None => html.setContent(textBody,"text/html"); // TODO: fancy <P> wrapping...
        } 
		html.setHeader("MIME-Version" , "1.0" );

		content.addBodyPart(text);
		content.addBodyPart(html);

		message.setContent( content );
		message.setHeader("MIME-Version" , "1.0" );
		message.setHeader("Content-Type" , content.getContentType() );
      	message.setSentDate(new Date());
      	Transport.send( message );
  	}
  
}