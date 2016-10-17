package org.jamwiki.mail;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import org.jamwiki.Environment;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.WikiLogger;

import java.util.*;

/**
 * This class is used to send mails from JAMWiki. It is not intended to be
 * used as a mail client.
 * 
 * The configuration of the class is done using a set of properties that are stored in
 * the database (and in jamwiki.properties). The table below shows the initial standard configuration. Notice that
 * all bluewin values are real and can be used for initial testing (only works with authentication).
 *
 * You can override some of these values using one of the methods that allow you to
 * set their value. Calling a method with an overridden property will not change the
 * configuration in the database.
 *
 * <pre>
 * smtp-authentication    : Indicates if the SMTP server requires authentication. If set to true then
 *                          the values of smtp-username and smtp-userpass must be set as well. If set
 *                          to false those properties are ignored.
 * smtp-use-ssl           : Indicates that the SMTP server requires SSL. A well known server using SSL
 *                          is gmail.com.
 * smtp-reply-to          : The FROM address of the mail.  
 * smtp-host              : The host name of the SMTP server.
 * smtp-content-type      : text/plain or text/html (default at installation: text/plain).
 * smtp-address-separator : The character used to separate TO mail addresses in a String (default: ";").
 * smtp-userpass          : The password of the user sending the mail.
 * smtp-username          : The username of the user sending the mail (often the same as smtp-reply-to).
 * smtp-port              : The port of the SMTP server (default: 25).
 * </pre>
 * @author cclavadetscher
 *
 */
public class WikiMail
{
	private static final WikiLogger logger = WikiLogger.getLogger(WikiMail.class.getName());
	
	/**
	 * The content type for sending plain text messages.
	 */
	public static final String TEXT = "text/plain";
	/**
	 * The content type for sending html documents.
	 */
	public static final String HTML = "text/html";

	private boolean smtpAuth       = false;
	private String  host           = null;
	private String  port           = null;
	private String  replyTo        = null;
	private String  user           = null;
	private String  pass           = null;
	private boolean useSSL         = false;
	private String  separator      = null;
	private String  defContentType = null;
	
	//-------------------------------------------------------------------------
	// Public interface
	//-------------------------------------------------------------------------

	public WikiMail() {
		init(Environment.getInstance());
	}
	
	public WikiMail(Properties props) {
		init(props);
	}
	
	private void init(Properties props) {
		this.smtpAuth       = "true".equals(props.getProperty(Environment.PROP_EMAIL_SMTP_REQUIRES_AUTH));
		this.host           = props.getProperty(Environment.PROP_EMAIL_SMTP_HOST);
		this.port           = props.getProperty(Environment.PROP_EMAIL_SMTP_PORT);
		this.replyTo        = props.getProperty(Environment.PROP_EMAIL_REPLY_ADDRESS);
		this.user           = props.getProperty(Environment.PROP_EMAIL_SMTP_USERNAME);
		this.pass           = Encryption.getEncryptedProperty(Environment.PROP_EMAIL_SMTP_PASSWORD, props);
		this.useSSL         = "true".equals(props.getProperty(Environment.PROP_EMAIL_SMTP_USE_SSL));
		this.separator      = props.getProperty(Environment.PROP_EMAIL_ADDRESS_SEPARATOR);
		this.defContentType = props.getProperty(Environment.PROP_EMAIL_DEFAULT_CONTENT_TYPE);
		this.useSSL         = "true".equals(props.getProperty(Environment.PROP_EMAIL_SMTP_USE_SSL));
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(this.getClass().getName());
		sb.append("\nE-Mail settings: \n");
		sb.append("Authentication: " + this.smtpAuth + "\n");
		sb.append("Host          : " + this.host + "\n");
		sb.append("Port          : " + this.port + "\n");
		sb.append("Username      : " + this.user + "\n");
		sb.append("Password      : " + this.pass + "\n");
		sb.append("Separator     : " + this.separator + "\n");
		sb.append("Content type  : " + this.defContentType);
		return sb.toString();
	}
	
	/**
	 * @param recipients The list of recipients as a String[].
	 * @param subject The subject line.
	 * @param message The message itself.
	 * @param contentType The type of content. Use one of the constants if you
	 * want to explicitly use this parameter. The default value is defined in
	 * the property smtp_default_content_type in the database.
	 * @throws MessagingException If there is any problem sending the mail.
	 */
	public void postMail(String[] recipients,
			             String subject,
			             String message,
			             String contentType)
	throws MessagingException
	{
		postMail(recipients,null,null,subject,message,contentType,null);
	}

	/**
	 * @param recipients The list of recipients as a String[].
	 * @param subject The subject line.
	 * @param message The message itself.
	 * @throws MessagingException If there is any problem sending the mail.
	 */
	public void postMail(String[] recipients,
			             String subject,
			             String message)
	throws MessagingException
	{
		postMail(recipients,null,null,subject,message,null,null);
	}

	/**
	 * @param recipients The list of recipients as a String[].
	 * @param message The message itself.
	 * @throws MessagingException If there is any problem sending the mail.
	 */
	public void postMail(String[] recipients,
                         String message)
	throws MessagingException
	{
		postMail(recipients,null,null,null,message,null,null);
	}

	/**
	 * @param toRecipients The list of recipients as a single String. The mail
	 * addresses must be separated using the separator defined in the property
	 * smtp_addr_separator in the database.
	 * @param subject The subject line.
	 * @param message The message itself.
	 * @param contentType The type of content. Use one of the constants if you
	 * want to explicitly use this parameter. The default value is defined in
	 * the property smtp_default_content_type in the database.
	 * @param attachments A list of names of files that must be attached to the
	 * mail. You must use the absolute name, i.e. including the directory where
	 * the file is located
	 * @throws MessagingException If there is any problem sending the mail.
	 */
    public void postMail(String toRecipients,
    		             String ccRecipients,
    		             String bccRecipients,
                         String subject,
                         String message,
                         String contentType,
                         String[] attachments)
	throws MessagingException
	{
		String[] toList = toRecipients.split(this.separator);
		String[] ccList = null;
		if(ccRecipients != null) ccList = ccRecipients.split(this.separator);
		String[] bccList = null;
		if(bccRecipients != null) bccList = bccRecipients.split(this.separator);
		postMail(toList,ccList,bccList,subject,message,contentType,attachments);
	}
	
	/**
	 * @param recipients The list of recipients as a single String. The mail
	 * addresses must be separated using the separator defined in the property
	 * smtp_addr_separator in the database.
	 * @param subject The subject line.
	 * @param message The message itself.
	 * @param contentType The type of content. Use one of the constants if you
	 * want to explicitly use this parameter. The default value is defined in
	 * the property smtp_default_content_type in the database.
	 * @throws MessagingException If there is any problem sending the mail.
	 */
	public void postMail(String recipients,
	                     String subject,
	                     String message,
	                     String contentType)
	throws MessagingException
	{
		postMail(recipients,null,null,subject,message,contentType,null);
	}
	
	/**
	 * @param recipients The list of recipients as a single String. The mail
	 * addresses must be separated using the separator defined in the property
	 * smtp_addr_separator in the database.
	 * @param subject The subject line.
	 * @param message The message itself.
	 * @throws MessagingException If there is any problem sending the mail.
	 */
	public void postMail(String recipients,
                         String subject,
                         String message)
	throws MessagingException
	{
		postMail(recipients,null,null,subject,message,null,null);
	}
	
	/**
	 * @param recipients The list of recipients as a single String. The mail
	 * addresses must be separated using the separator defined in the property
	 * smtp_addr_separator in the database.
	 * @param message The message itself.
	 * @throws MessagingException If there is any problem sending the mail.
	 */
	public void postMail(String recipients,
                         String message)
	throws MessagingException
	{
		postMail(recipients,null,null,null,message,null,null);
	}

	/**
	 * The central method for sending mails. The other methods basically
	 * call this one, setting the missing parameters to null.
	 * @param toRecipients The list of recipients as a String[].
	 * @param ccRecipients The list of recipients on CC as a String[].
	 * @param bccRecipients The list of recipients on BCC as a String[].
	 * @param subject The subject line.
	 * @param message The message itself.
	 * @param contentType The type of content. Use one of the constants if you
	 * want to explicitly use this parameter. The default value is defined in
	 * the property smtp_default_content_type in the database.
	 * @param attachments A list of names of files that must be attached to the
	 * mail. You must use the absolute name, i.e. including the directory where
	 * the file is located
	 * @throws MessagingException If there is any problem sending the mail.
	 */
    public void postMail(String[] toRecipients,
    		             String[] ccRecipients,
    		             String[] bccRecipients,
    		             String subject,
    		             String message,
    		             String contentType,
    		             String[] attachments)
    throws MessagingException
    {
    	if(contentType == null || !contentType.equals(WikiMail.TEXT) || !contentType.equals(WikiMail.HTML))
    	{
    		contentType = defContentType;
    	}
    	try
    	{
    		logger.info(toString());
	        //Set the host smtp address
	        Properties props = new Properties();
	        props.put("mail.transport.protocol","smtp");
	        props.put("mail.smtp.host",this.host);
	        props.put("mail.smtp.port",this.port);
	        props.put("mail.from",this.replyTo);
	        if(this.smtpAuth)
	        {
		        props.put("mail.smtp.auth","true");
	        }
	        else
	        {
		        props.put("mail.smtp.auth","false");
	        }
	        
	        if (this.useSSL) {
		        props.put("mail.smtp.socketFactory.port", this.port);
		        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	        }
	        
	        Authenticator auth = new SMTPAuthenticator(this.user,this.pass);
	        Session session = Session.getInstance(props,auth);

	        // create a message
	        Message msg = new MimeMessage(session);
	
	        // set the from and to address
	        InternetAddress addressFrom = new InternetAddress(this.replyTo);
	        msg.setFrom(addressFrom);
	
	        InternetAddress[] addressTo = new InternetAddress[toRecipients.length];
	        for (int i = 0; i < toRecipients.length; i++)
	        {
	            addressTo[i] = new InternetAddress(toRecipients[i]);
	        }
	        msg.setRecipients(Message.RecipientType.TO, addressTo);

	        if(ccRecipients != null)
	        {
		        InternetAddress[] addressCC = new InternetAddress[ccRecipients.length];
		        for (int i = 0; i < ccRecipients.length; i++)
		        {
		            addressCC[i] = new InternetAddress(ccRecipients[i]);
		        }
		        msg.setRecipients(Message.RecipientType.CC, addressCC);
	        }

	        if(bccRecipients != null)
	        {
		        InternetAddress[] addressBCC = new InternetAddress[bccRecipients.length];
		        for (int i = 0; i < bccRecipients.length; i++)
		        {
		            addressBCC[i] = new InternetAddress(bccRecipients[i]);
		        }
		        msg.setRecipients(Message.RecipientType.BCC, addressBCC);
	        }
	        
	        // handle attachments
	        Multipart multipart = new MimeMultipart();
	        
	        // create the message part 
	        MimeBodyPart messageBodyPart = new MimeBodyPart();
	        //messageBodyPart.setText(message);
	        messageBodyPart.setContent(message,contentType);
	        multipart.addBodyPart(messageBodyPart);

	        // additional parts are attachment
	        if(attachments != null && attachments.length > 0)
	        {
		        for(int i=0;i<attachments.length;i++)
		        {
			        messageBodyPart = new MimeBodyPart();
			        DataSource source = new FileDataSource(attachments[i]);
			        messageBodyPart.setDataHandler(new DataHandler(source));
			        messageBodyPart.setFileName(attachments[i]);
			        multipart.addBodyPart(messageBodyPart);
		        }
	        }

            // Setting the Subject and Content Type
	        if(subject == null) subject = "No subject";
	        msg.setSubject(subject);
	        msg.setContent(multipart);
	        Transport.send(msg);
    	}
    	catch(MessagingException me)
    	{
    		logger.error("Exception", me);
    		throw me;
    	}
    }

    /**
    * SimpleAuthenticator is used to do simple authentication
    * when the SMTP server requires it.
    */
    private class SMTPAuthenticator extends javax.mail.Authenticator
    {
    	String user = null;
    	String pass = null;
    	
    	public SMTPAuthenticator(String username,String password)
    	{
    		user = username;
    		pass = password;
    	}
    	
        public PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication(user,pass);
        }
    }

    /**
     * This method is a shortcut to retrieve the address separator delimiter
     * defined in the database.
     * @return The address separator to use in a String containing many mail
     * addresses.
     */
    public String getAddressSeparator()
    {
    	return this.separator;
    }
}
