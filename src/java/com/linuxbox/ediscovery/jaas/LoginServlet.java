package com.linuxbox.ediscovery.jaas;

/*
 * @(#)SampleLoginModule.java   1.18 00/01/11
 *
 * Copyright 2000-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * -Redistributions of source code must retain the above copyright
 * notice, this  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduct the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF  OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */


import java.util.*;
import java.io.IOException;
import java.io.PrintWriter;

import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.mozilla.javascript.Context;


/**
 * <p> This sample LoginModule authenticates users with a password.
 *
 * <p> This LoginModule only recognizes one user:   testUser
 * <p> testUser's password is:  testPassword
 *
 * <p> If testUser successfully authenticates itself,
 * a <code>SamplePrincipal</code> with the testUser's user name
 * is added to the Subject.
 *
 * <p> This LoginModule recognizes the debug option.
 * If set to true in the login Configuration,
 * debug messages will be output to the output stream, System.out.
 *
 * 
 */
public class LoginServlet extends HttpServlet implements LoginModule  {

    // initial state
    private Subject subject;
   
    //not sure if we need this callbackHandler in the future for processing input and output text
    private CallbackHandler callbackHandler;  
    private Map sharedState;
    private Map options;

    // configurable option
    private boolean debug = false;

    // the authentication status
    private boolean succeeded = false;
    private boolean commitSucceeded = false;

    // username and password
    private String username;
    private char[] password;
    HttpSession session;

    // testUser's SamplePrincipal
    private SamplePrincipal userPrincipal;

    /**
     * Initialize this <code>LoginModule</code>.
     *
     * <p>
     *
     * @param subject the <code>Subject</code> to be authenticated. <p>
     *
     * @param callbackHandler a <code>CallbackHandler</code> for communicating
     *          with the end user (prompting for user names and
     *          passwords, for example). <p>
     *
     * @param sharedState shared <code>LoginModule</code> state. <p>
     *
     * @param options options specified in the login
     *          <code>Configuration</code> for this particular
     *          <code>LoginModule</code>.
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map sharedState, Map options) {

    this.subject = subject;
    this.callbackHandler = callbackHandler;
    this.sharedState = sharedState;
    this.options = options;

    // initialize any configured options
    debug = "true".equalsIgnoreCase((String)options.get("debug"));
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {
    	
    	session = req.getSession(true);
    	PrintWriter out = resp.getWriter();
		
    	// change path of these to whereever the configs are located at
    	System.setProperty("java.security.auth.login.config", "/home/natalie/git/enkive-ediscovery/src/java/com/linuxbox/ediscovery/jaas/sample_jaas.config");
		System.setProperty("LDAP_PROPERTIES", "/home/natalie/git/enkive-ediscovery/src/java/com/linuxbox/ediscovery/jaas/ldap.properties");

	  	
		if (session.getAttribute("userName") == null) {
		    session.setAttribute("userName", "Anonymous user!");
		}else{
  	
		   try {
		
				String userEntered = (String) session.getAttribute("userName");
	        	String passwordEntered = (String) session.getAttribute("userPassword");
	        	out.println("Welcome " + (String) session.getAttribute("userName") + "!<br>");
	        
	        	this.username = userEntered;
                this.password = passwordEntered.toCharArray();	   
                
	            if(login()){
	            	out.println("Authentication succeeded!");
 	
	            }else{
	            	out.println("Authentication Failed!");
	            }
		    } catch (SecurityException se) {
		    	System.err.println("Cannot create LoginContext. "
		            + se.getMessage());
		        System.exit(-1);
		    } catch (LoginException e) {
				out.println("log in error " + e.getMessage());
			}
		    
		
		}

		out.println("<html>");
		out.println("<head>");
		out.println("<title> Test</title>");
		out.println("</head>");
		out.println("<body>");

		out.println("<h1> Test</h1>");

		out.println("<P>");
		out.print("<form action=\"");
		out.print(resp.encodeURL("sessionServlet"));  //temporary link back to current page for now
		out.print("\" ");
		out.println("method=POST>");
		out.println("Enter User name");
		out.println("<br>");
		out.println("<input type=text autocomplete=off  size=20 name=myname>");
		out.println("<br>");
		out.println("Enter password");
		out.println("<br>");
		out.println("<input type=text autocomplete=off size=20 name=mypassword>");
		out.println("<input type=submit>");
		out.println("</form>");

		out.println("</body>");
		out.println("</html>");

		
    }
    public void doPost(HttpServletRequest req,
            HttpServletResponse resp)
	throws IOException, ServletException
	{
		session = req.getSession(true);
		session.setAttribute("userName", req.getParameter("myname"));
		session.setAttribute("userPassword", req.getParameter("mypassword"));
		doGet(req, resp);
	}
    
   
    /**
     * Authenticate the user by prompting for a user name and password.
     *
     * <p>
     *
     * @return true in all cases since this <code>LoginModule</code>
     *      should not be ignored.
     *
     * @exception FailedLoginException if the authentication fails. <p>
     *
     * @exception LoginException if this <code>LoginModule</code>
     *      is unable to perform the authentication.
     */
    public boolean login() throws LoginException {
	
    // LDAP authentication
    System.out.println("check user name is " + username);
    try{
	    if (LDAPAuthenticator.getInstance().authenticate(username,password)) {
	
	        // authentication succeeded!!!
	        //passwordCorrect = true;
	        if (debug)
	        System.out.println("\t\t[SampleLoginModule] " +
	                "authentication succeeded");
	        succeeded = true;
	        return true;
	    } else {
	
	        // authentication failed -- clean out state
	        if (debug)
	        System.out.println("\t\t[SampleLoginModule] " +
	                "authentication failed");
	        succeeded = false;
	        username = null;
	        for (int i = 0; i < password.length; i++)
	        password[i] = ' ';
	        password = null;
	        return false;
	        
	    }
    }catch (Exception e){
    	System.out.println("exception " + e.getMessage());
    	return false;
    }
	   
    }

    /**
     * <p> This method is called if the LoginContext's
     * overall authentication succeeded
     * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules
     * succeeded).
     *
     * <p> If this LoginModule's own authentication attempt
     * succeeded (checked by retrieving the private state saved by the
     * <code>login</code> method), then this method associates a
     * <code>SamplePrincipal</code>
     * with the <code>Subject</code> located in the
     * <code>LoginModule</code>.  If this LoginModule's own
     * authentication attempted failed, then this method removes
     * any state that was originally saved.
     *
     * <p>
     *
     * @exception LoginException if the commit fails.
     *
     * @return true if this LoginModule's own login and commit
     *      attempts succeeded, or false otherwise.
     */
    public boolean commit() throws LoginException {
    if (succeeded == false) {
        return false;
    } else {
        // add a Principal (authenticated identity)
        // to the Subject

        // assume the user we authenticated is the SamplePrincipal
        userPrincipal = new SamplePrincipal(username);
        if (!subject.getPrincipals().contains(userPrincipal))
        subject.getPrincipals().add(userPrincipal);

        if (debug) {
        System.out.println("\t\t[SampleLoginModule] " +
                "added SamplePrincipal to Subject");
        }

        // in any case, clean out state
        username = null;
        for (int i = 0; i < password.length; i++)
        password[i] = ' ';
        password = null;

        commitSucceeded = true;
        return true;
    }
    }

    /**
     * <p> This method is called if the LoginContext's
     * overall authentication failed.
     * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules
     * did not succeed).
     *
     * <p> If this LoginModule's own authentication attempt
     * succeeded (checked by retrieving the private state saved by the
     * <code>login</code> and <code>commit</code> methods),
     * then this method cleans up any state that was originally saved.
     *
     * <p>
     *
     * @exception LoginException if the abort fails.
     *
     * @return false if this LoginModule's own login and/or commit attempts
     *      failed, and true otherwise.
     */
    public boolean abort() throws LoginException {
    if (succeeded == false) {
        return false;
    } else if (succeeded == true && commitSucceeded == false) {
        // login succeeded but overall authentication failed
        succeeded = false;
        username = null;
        if (password != null) {
        for (int i = 0; i < password.length; i++)
            password[i] = ' ';
        password = null;
        }
        userPrincipal = null;
    } else {
        // overall authentication succeeded and commit succeeded,
        // but someone else's commit failed
        logout();
    }
    return true;
    }

    /**
     * Logout the user.
     *
     * <p> This method removes the <code>SamplePrincipal</code>
     * that was added by the <code>commit</code> method.
     *
     * <p>
     *
     * @exception LoginException if the logout fails.
     *
     * @return true in all cases since this <code>LoginModule</code>
     *          should not be ignored.
     */
    public boolean logout() throws LoginException {

    subject.getPrincipals().remove(userPrincipal);
    succeeded = false;
    succeeded = commitSucceeded;
    username = null;
    if (password != null) {
        for (int i = 0; i < password.length; i++)
        password[i] = ' ';
        password = null;
    }
    userPrincipal = null;
    return true;
    }
	

    public void setUserName(String newUserName){
    	
    	username = newUserName;
    }
    
    
}