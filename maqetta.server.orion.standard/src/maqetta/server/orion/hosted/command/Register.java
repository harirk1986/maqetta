package maqetta.server.orion.hosted.command;

import java.io.IOException;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.davinci.server.user.IUser;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.orion.server.core.users.OrionScope;
import org.maqetta.server.Command;
import org.maqetta.server.ServerManager;

public class Register  extends Command {

    private static Random generator = new Random();
	private static String EMAIL_FIELD = "SIGNUP_EMAIL";
	private static String EMAIL_TEMPLATE = "Please click this link to activate your maqetta.org user account:<br><br>";
	
	class EmailRunnable implements Runnable {
		String emailAdd = null;
		String message = null;
		EmailRunnable(String emailAdd, String message){
			this.emailAdd = emailAdd;
			this.message = message;
		}
	    public void run() {
	    	ServerManager.getServerManger().sendEmail("admin@maqetta.org", emailAdd, "Maqetta.org user activation", message);
	    	System.out.println("---------------\nSending email:\n"+message );
	    }
	}
	
    public void handleCommand(HttpServletRequest req, HttpServletResponse resp, IUser user) throws IOException {
    	
    	String emailAdd = req.getParameter("login");
    	String randomToken = System.currentTimeMillis() + "_" + generator.nextInt();
    	
    	String requestUrl = req.getRequestURL().toString();
    	String host = requestUrl.substring(0, requestUrl.indexOf('/', "http://".length()));
    	String authLink = host + "/mixloginstatic/LoginWindow.html?login=" + emailAdd + "&loginTolken=" + randomToken + "&redirect=../maqetta/static/migrate.html";
    	
    	
    	IEclipsePreferences signupTokens = new OrionScope().getNode("signup"); //$NON-NLS-1$
    	
    	/* index by the token for easy retrival */
		IEclipsePreferences result = (IEclipsePreferences) signupTokens.node(randomToken);
    	/* store the email address with the token */
		result.put(Register.EMAIL_FIELD, emailAdd);
		
		sendEmail(emailAdd, EMAIL_TEMPLATE + authLink);
	
        
    	this.responseString = "OK";
      }
    
    private void sendEmail(String emailAdd, String htmlContent){
    	(new Thread(new EmailRunnable(emailAdd, htmlContent))).start();
    }
}