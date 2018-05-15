package disabledefaultlogin.helpers;

import java.util.Map;

import com.mendix.core.Core;
import com.mendix.core.action.user.LoginAction;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.AuthenticationRuntimeException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.ISession;
import com.mendix.systemwideinterfaces.core.IUser;

import system.proxies.User;

public class HandleLoginAction extends LoginAction {
	private String userName;
	private String password;
	public final static String USERNAME_PARAM = "userName";
	public final static String PASSWORD_PARAM = "password";
	
	// Get and store log node
	private final static ILogNode logNode = Core.getLogger(disabledefaultlogin.proxies.constants.Constants.getLogNode());

	public HandleLoginAction(Map<String, ? extends Object> params) {
		super(Core.createSystemContext(), params);

		this.userName = (String) params.get(USERNAME_PARAM);
		this.password = (String) params.get(PASSWORD_PARAM);
	}

	@Override
	public ISession execute() throws Exception {
		// If you have existing java code which overwrite LoginAction copy the code from that Action to here or vice versa.
		IContext sysContext = Core.createSystemContext();
		
		// Try to get user on basis of username
		IUser user = Core.getUser(sysContext, this.userName);
		
		// Perform standard user checks
		if (user == null)
			throw new AuthenticationRuntimeException("Login FAILED: unknown user '" + this.userName + "'.");
		else if (user.isWebserviceUser())
			throw new AuthenticationRuntimeException("Login FAILED: client login attempt for web service user '" + this.userName + "'.");
		else if (user.isAnonymous())
			throw new AuthenticationRuntimeException("Login FAILED: client login attempt for guest user '" + this.userName + "'.");
		else if (user.isActive() == false)
			throw new AuthenticationRuntimeException("Login FAILED: user '" + this.userName + "' is not active.");
		else if (user.isBlocked() == true)
			throw new AuthenticationRuntimeException("Login FAILED: user '" + this.userName + "' is blocked.");
		else if (user.getUserRoleNames().isEmpty())
			throw new AuthenticationRuntimeException("Login FAILED: user '" + this.userName + "' does not have any user roles.");
		else if (!Core.authenticate(sysContext, user, this.password))
			throw new AuthenticationRuntimeException("Login FAILED: invalid password for user '" + user.getName() + "'.");
		
		// Execute microflow to determine whether login is allowed
		User userParam = User.load(sysContext, user.getMendixObject().getId());
		logNode.debug("Executing microflow to determine whether user '" + this.userName + "' is allowed to login");
		boolean allowLogin = disabledefaultlogin.proxies.microflows.Microflows.allowUserToLogin(sysContext, userParam);
		
		if (allowLogin == false)
			throw new AuthenticationRuntimeException("Login FAILED: user '" + this.userName + "' tried to login, but default login has been disabled.");
		
		// Perform login action
		ISession newSession = null;
		newSession = super.execute();
		
		return newSession;
		
	}
}