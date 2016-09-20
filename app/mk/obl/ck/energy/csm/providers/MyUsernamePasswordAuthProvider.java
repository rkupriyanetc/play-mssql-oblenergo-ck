package mk.obl.ck.energy.csm.providers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.mail.Mailer.MailerFactory;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;

import mk.obl.ck.energy.csm.controllers.routes;
import mk.obl.ck.energy.csm.mssql.models.LinkedAccount;
import mk.obl.ck.energy.csm.mssql.models.TokenAction;
import mk.obl.ck.energy.csm.mssql.models.TokenAction.Type;
import mk.obl.ck.energy.csm.mssql.models.User;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Lang;
import play.i18n.Messages;
import play.inject.ApplicationLifecycle;
import play.mvc.Call;
import play.mvc.Http.Context;

@Singleton
public class MyUsernamePasswordAuthProvider extends
		UsernamePasswordAuthProvider< String, MyLoginUsernamePasswordAuthUser, MyUsernamePasswordAuthUser, MyUsernamePasswordAuthProvider.MyLogin, MyUsernamePasswordAuthProvider.MySignup > {
	
	public static class MyIdentity {
		
		@Required
		@Email
		protected String email;
		
		public MyIdentity() {}
		
		public MyIdentity( final String email ) {
			this.email = email;
		}
	}
	
	public static class MyLogin extends MyIdentity
			implements com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.UsernamePassword {
		
		@Required
		@MinLength( 3 )
		protected String password;
		
		@Override
		public String getEmail() {
			return email;
		}
		
		@Override
		public String getPassword() {
			return password;
		}
		
		public void setEmail( final String email ) {
			this.email = email;
		}
		
		public void setPassword( final String password ) {
			this.password = password;
		}
	}
	
	public static class MySignup extends MyLogin {
		
		@Required
		@MinLength( 3 )
		private String	repeatPassword;
		
		@Required
		private String	name;
		
		public String getName() {
			return name;
		}
		
		public String getRepeatPassword() {
			return repeatPassword;
		}
		
		public void setName( final String name ) {
			this.name = name;
		}
		
		public void setRepeatPassword( final String repeatPassword ) {
			this.repeatPassword = repeatPassword;
		}
		
		public String validate() {
			if ( password == null || !password.equals( repeatPassword ) )
				return Messages.get( "playauthenticate.password.signup.error.passwords_not_same" );
			return null;
		}
	}
	
	private static final String	SETTING_KEY_VERIFICATION_LINK_SECURE				= SETTING_KEY_MAIL + "." + "verificationLink.secure";
	
	private static final String	SETTING_KEY_PASSWORD_RESET_LINK_SECURE			= SETTING_KEY_MAIL + "." + "passwordResetLink.secure";
	
	private static final String	SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET	= "loginAfterPasswordReset";
	
	private static final String	EMAIL_TEMPLATE_FALLBACK_LANGUAGE						= "en";
	
	private static String generateToken() {
		return UUID.randomUUID().toString();
	}
	
	private final Form< MySignup >	SIGNUP_FORM;
	
	private final Form< MyLogin >		LOGIN_FORM;
	
	@Inject
	public MyUsernamePasswordAuthProvider( final PlayAuthenticate auth, final FormFactory formFactory,
			final ApplicationLifecycle lifecycle, final MailerFactory mailerFactory ) {
		super( auth, lifecycle, mailerFactory );
		this.SIGNUP_FORM = formFactory.form( MySignup.class );
		this.LOGIN_FORM = formFactory.form( MyLogin.class );
	}
	
	@Override
	protected MyLoginUsernamePasswordAuthUser buildLoginAuthUser( final MyLogin login, final Context ctx ) {
		return new MyLoginUsernamePasswordAuthUser( login.getPassword(), login.getEmail() );
	}
	
	@Override
	protected MyUsernamePasswordAuthUser buildSignupAuthUser( final MySignup signup, final Context ctx ) {
		return new MyUsernamePasswordAuthUser( signup );
	}
	
	protected String generatePasswordResetRecord( final User u ) {
		final String token = generateToken();
		TokenAction.create( Type.PASSWORD_RESET, token, u );
		return token;
	}
	
	@Override
	protected String generateVerificationRecord( final MyUsernamePasswordAuthUser user ) {
		return generateVerificationRecord( User.findByAuthUserIdentity( user ) );
	}
	
	protected String generateVerificationRecord( final User user ) {
		final String token = generateToken();
		// Do database actions, etc.
		TokenAction.create( Type.EMAIL_VERIFICATION, token, user );
		return token;
	}
	
	private String getEmailName( final User user ) {
		return getEmailName( user.getEmail(), user.getName() );
	}
	
	protected String getEmailTemplate( final String template, final String langCode, final String url, final String token,
			final String name, final String email ) {
		Class< ? > cls = null;
		String ret = null;
		try {
			cls = Class.forName( template + "_" + langCode );
		}
		catch ( final ClassNotFoundException e ) {
			Logger.warn(
					"Template: '" + template + "_" + langCode + "' was not found! Trying to use English fallback template instead." );
		}
		if ( cls == null )
			try {
				cls = Class.forName( template + "_" + EMAIL_TEMPLATE_FALLBACK_LANGUAGE );
			}
			catch ( final ClassNotFoundException e ) {
				Logger.error( "Fallback template: '" + template + "_" + EMAIL_TEMPLATE_FALLBACK_LANGUAGE + "' was not found either!" );
			}
		if ( cls != null ) {
			Method htmlRender = null;
			try {
				htmlRender = cls.getMethod( "render", String.class, String.class, String.class, String.class );
				ret = htmlRender.invoke( null, url, token, name, email ).toString();
			}
			catch ( final NoSuchMethodException e ) {
				e.printStackTrace();
			}
			catch ( final IllegalAccessException e ) {
				e.printStackTrace();
			}
			catch ( final InvocationTargetException e ) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	@Override
	public Form< MyLogin > getLoginForm() {
		return LOGIN_FORM;
	}
	
	protected Body getPasswordResetMailingBody( final String token, final User user, final Context ctx ) {
		final boolean isSecure = getConfiguration().getBoolean( SETTING_KEY_PASSWORD_RESET_LINK_SECURE );
		final String url = routes.Signup.resetPassword( token ).absoluteURL( ctx.request(), isSecure );
		final Lang lang = Lang.preferred( ctx.request().acceptLanguages() );
		final String langCode = lang.code();
		final String html = getEmailTemplate( "views.html.account.email.password_reset", langCode, url, token, user.getName(),
				user.getEmail() );
		final String text = getEmailTemplate( "views.txt.account.email.password_reset", langCode, url, token, user.getName(),
				user.getEmail() );
		return new Body( text, html );
	}
	
	protected String getPasswordResetMailingSubject( final User user, final Context ctx ) {
		return Messages.get( "playauthenticate.password.reset_email.subject" );
	}
	
	@Override
	public Form< MySignup > getSignupForm() {
		return SIGNUP_FORM;
	}
	
	@Override
	protected Body getVerifyEmailMailingBody( final String token, final MyUsernamePasswordAuthUser user, final Context ctx ) {
		final boolean isSecure = getConfiguration().getBoolean( SETTING_KEY_VERIFICATION_LINK_SECURE );
		final String url = routes.Signup.verify( token ).absoluteURL( ctx.request(), isSecure );
		final Lang lang = Lang.preferred( ctx.request().acceptLanguages() );
		final String langCode = lang.code();
		final String html = getEmailTemplate( "views.html.account.signup.email.verify_email", langCode, url, token, user.getName(),
				user.getEmail() );
		final String text = getEmailTemplate( "views.txt.account.signup.email.verify_email", langCode, url, token, user.getName(),
				user.getEmail() );
		return new Body( text, html );
	}
	
	protected Body getVerifyEmailMailingBodyAfterSignup( final String token, final User user, final Context ctx ) {
		final boolean isSecure = getConfiguration().getBoolean( SETTING_KEY_VERIFICATION_LINK_SECURE );
		final String url = routes.Signup.verify( token ).absoluteURL( ctx.request(), isSecure );
		final Lang lang = Lang.preferred( ctx.request().acceptLanguages() );
		final String langCode = lang.code();
		final String html = getEmailTemplate( "views.html.account.email.verify_email", langCode, url, token, user.getName(),
				user.getEmail() );
		final String text = getEmailTemplate( "views.txt.account.email.verify_email", langCode, url, token, user.getName(),
				user.getEmail() );
		return new Body( text, html );
	}
	
	@Override
	protected String getVerifyEmailMailingSubject( final MyUsernamePasswordAuthUser user, final Context ctx ) {
		return Messages.get( "playauthenticate.password.verify_signup.subject" );
	}
	
	protected String getVerifyEmailMailingSubjectAfterSignup( final User user, final Context ctx ) {
		return Messages.get( "playauthenticate.password.verify_email.subject" );
	}
	
	public boolean isLoginAfterPasswordReset() {
		return getConfiguration().getBoolean( SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET );
	}
	
	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.LoginResult loginUser(
			final MyLoginUsernamePasswordAuthUser authUser ) {
		final User u = User.findByUsernamePasswordIdentity( authUser );
		if ( u == null )
			return LoginResult.NOT_FOUND;
		else
			if ( !u.isEmailValidated() )
				return LoginResult.USER_UNVERIFIED;
			else {
				for ( final LinkedAccount acc : u.getLinkedAccounts() )
					if ( getKey().equals( acc.getProviderKey() ) )
						if ( authUser.checkPassword( acc.getProviderUserId(), authUser.getPassword() ) )
							// Password was correct
							return LoginResult.USER_LOGGED_IN;
						else
							// if you don't return here,
							// you would allow the user to have
							// multiple passwords defined
							// usually we don't want this
							return LoginResult.WRONG_PASSWORD;
				return LoginResult.WRONG_PASSWORD;
			}
	}
	
	@Override
	protected List< String > neededSettingKeys() {
		final List< String > needed = new ArrayList< String >( super.neededSettingKeys() );
		needed.add( SETTING_KEY_VERIFICATION_LINK_SECURE );
		needed.add( SETTING_KEY_PASSWORD_RESET_LINK_SECURE );
		needed.add( SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET );
		return needed;
	}
	
	@Override
	protected String onLoginUserNotFound( final Context context ) {
		context.flash().put( mk.obl.ck.energy.csm.controllers.Application.FLASH_ERROR_KEY,
				Messages.get( "playauthenticate.password.login.unknown_user_or_pw" ) );
		return super.onLoginUserNotFound( context );
	}
	
	public void sendPasswordResetMailing( final User user, final Context ctx ) {
		final String token = generatePasswordResetRecord( user );
		final String subject = getPasswordResetMailingSubject( user, ctx );
		final Body body = getPasswordResetMailingBody( token, user, ctx );
		sendMail( subject, body, getEmailName( user ) );
	}
	
	public void sendVerifyEmailMailingAfterSignup( final User user, final Context ctx ) {
		final String subject = getVerifyEmailMailingSubjectAfterSignup( user, ctx );
		final String token = generateVerificationRecord( user );
		final Body body = getVerifyEmailMailingBodyAfterSignup( token, user, ctx );
		sendMail( subject, body, getEmailName( user ) );
	}
	
	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.SignupResult signupUser(
			final MyUsernamePasswordAuthUser user ) {
		final User u = User.findByUsernamePasswordIdentity( user );
		if ( u != null )
			if ( u.isEmailValidated() )
				// This user exists, has its email validated and is active
				return SignupResult.USER_EXISTS;
			else
				// this user exists, is active but has not yet validated its
				// email
				return SignupResult.USER_EXISTS_UNVERIFIED;
		// The user either does not exist or is inactive - create a new one
		@SuppressWarnings( "unused" )
		final User newUser = User.create( user );
		// Usually the email should be verified before allowing login, however
		// if you return
		// return SignupResult.USER_CREATED;
		// then the user gets logged in directly
		return SignupResult.USER_CREATED_UNVERIFIED;
	}
	
	@Override
	protected MyLoginUsernamePasswordAuthUser transformAuthUser( final MyUsernamePasswordAuthUser authUser,
			final Context context ) {
		return new MyLoginUsernamePasswordAuthUser( authUser.getEmail() );
	}
	
	@Override
	protected Call userExists( final UsernamePasswordAuthUser authUser ) {
		return routes.Signup.exists();
	}
	
	@Override
	protected Call userUnverified( final UsernamePasswordAuthUser authUser ) {
		return routes.Signup.unverified();
	}
}
