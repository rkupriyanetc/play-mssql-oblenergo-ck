package mk.obl.ck.energy.csm.controllers;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.controllers.AuthenticateBase;

import mk.obl.ck.energy.csm.mssql.models.TokenAction;
import mk.obl.ck.energy.csm.mssql.models.TokenAction.Type;
import mk.obl.ck.energy.csm.mssql.models.User;
import mk.obl.ck.energy.csm.providers.MyLoginUsernamePasswordAuthUser;
import mk.obl.ck.energy.csm.providers.MyUsernamePasswordAuthProvider;
import mk.obl.ck.energy.csm.providers.MyUsernamePasswordAuthProvider.MyIdentity;
import mk.obl.ck.energy.csm.providers.MyUsernamePasswordAuthUser;
import mk.obl.ck.energy.csm.service.UserProvider;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.account.signup.exists;
import views.html.account.signup.no_token_or_invalid;
import views.html.account.signup.oAuthDenied;
import views.html.account.signup.password_forgot;
import views.html.account.signup.password_reset;
import views.html.account.signup.unverified;

public class Signup extends Controller {
	
	public static class PasswordReset extends Account.PasswordChange {
		
		private String token;
		
		public PasswordReset() {}
		
		public PasswordReset( final String token ) {
			this.token = token;
		}
		
		public String getToken() {
			return token;
		}
		
		public void setToken( final String token ) {
			this.token = token;
		}
	}
	
	private final Form< PasswordReset >						PASSWORD_RESET_FORM;
	
	private final Form< MyIdentity >							FORGOT_PASSWORD_FORM;
	
	private final PlayAuthenticate								auth;
	
	private final UserProvider										userProvider;
	
	private final MyUsernamePasswordAuthProvider	userPaswAuthProvider;
	
	private final MessagesApi											msg;
	
	@Inject
	public Signup( final PlayAuthenticate auth, final UserProvider userProvider,
			final MyUsernamePasswordAuthProvider userPaswAuthProvider, final FormFactory formFactory, final MessagesApi msg ) {
		this.auth = auth;
		this.userProvider = userProvider;
		this.userPaswAuthProvider = userPaswAuthProvider;
		this.PASSWORD_RESET_FORM = formFactory.form( PasswordReset.class );
		this.FORGOT_PASSWORD_FORM = formFactory.form( MyIdentity.class );
		this.msg = msg;
	}
	
	public Result doForgotPassword() {
		AuthenticateBase.noCache( response() );
		final Form< MyIdentity > filledForm = FORGOT_PASSWORD_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not fill in his/her email
			return badRequest( password_forgot.render( this.userProvider, filledForm ) );
		else {
			// The email address given *BY AN UNKNWON PERSON* to the form - we
			// should find out if we actually have a user with this email
			// address and whether password login is enabled for him/her. Also
			// only send if the email address of the user has been verified.
			final String email = filledForm.get().getEmail();
			// We don't want to expose whether a given email address is signed
			// up, so just say an email has been sent, even though it might not
			// be true - that's protecting our user privacy.
			flash( Application.FLASH_MESSAGE_KEY,
					this.msg.preferred( request() ).at( "playauthenticate.reset_password.message.instructions_sent", email ) );
			final User user = User.findByEmail( email );
			if ( user != null ) {
				// yep, we have a user with this email that is active - we do
				// not know if the user owning that account has requested this
				// reset, though.
				final MyUsernamePasswordAuthProvider provider = this.userPaswAuthProvider;
				// User exists
				if ( user.isEmailValidated() )
					provider.sendPasswordResetMailing( user, ctx() );
				// In case you actually want to let (the unknown person)
				// know whether a user was found/an email was sent, use,
				// change the flash message
				else {
					// We need to change the message here, otherwise the user
					// does not understand whats going on - we should not verify
					// with the password reset, as a "bad" user could then sign
					// up with a fake email via OAuth and get it verified by an
					// a unsuspecting user that clicks the link.
					flash( Application.FLASH_MESSAGE_KEY,
							this.msg.preferred( request() ).at( "playauthenticate.reset_password.message.email_not_verified" ) );
					// You might want to re-send the verification email here...
					provider.sendVerifyEmailMailingAfterSignup( user, ctx() );
				}
			}
			return redirect( routes.Application.index() );
		}
	}
	
	public Result doResetPassword() {
		AuthenticateBase.noCache( response() );
		final Form< PasswordReset > filledForm = PASSWORD_RESET_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			return badRequest( password_reset.render( this.userProvider, filledForm ) );
		else {
			final String token = filledForm.get().token;
			final String newPassword = filledForm.get().getPassword();
			final TokenAction ta = tokenIsValid( token, Type.PASSWORD_RESET );
			if ( ta == null )
				return badRequest( no_token_or_invalid.render( this.userProvider ) );
			final User u = ta.getTargetUser();
			try {
				// Pass true for the second parameter if you want to
				// automatically create a password and the exception never to
				// happen
				u.resetPassword( new MyUsernamePasswordAuthUser( newPassword ), false );
			}
			catch ( final RuntimeException re ) {
				flash( Application.FLASH_MESSAGE_KEY,
						this.msg.preferred( request() ).at( "playauthenticate.reset_password.message.no_password_account" ) );
			}
			final boolean login = this.userPaswAuthProvider.isLoginAfterPasswordReset();
			if ( login ) {
				// automatically log in
				flash( Application.FLASH_MESSAGE_KEY,
						this.msg.preferred( request() ).at( "playauthenticate.reset_password.message.success.auto_login" ) );
				return this.auth.loginAndRedirect( ctx(), new MyLoginUsernamePasswordAuthUser( u.getEmail() ) );
			} else
				// send the user to the login page
				flash( Application.FLASH_MESSAGE_KEY,
						this.msg.preferred( request() ).at( "playauthenticate.reset_password.message.success.manual_login" ) );
			return redirect( routes.Application.login() );
		}
	}
	
	public Result exists() {
		AuthenticateBase.noCache( response() );
		return ok( exists.render( this.userProvider ) );
	}
	
	public Result forgotPassword( final String email ) {
		AuthenticateBase.noCache( response() );
		Form< MyIdentity > form = FORGOT_PASSWORD_FORM;
		if ( email != null && !email.trim().isEmpty() )
			form = FORGOT_PASSWORD_FORM.fill( new MyIdentity( email ) );
		return ok( password_forgot.render( this.userProvider, form ) );
	}
	
	public Result oAuthDenied( final String getProviderKey ) {
		AuthenticateBase.noCache( response() );
		return ok( oAuthDenied.render( this.userProvider, getProviderKey ) );
	}
	
	public Result resetPassword( final String token ) {
		AuthenticateBase.noCache( response() );
		final TokenAction ta = tokenIsValid( token, Type.PASSWORD_RESET );
		if ( ta == null )
			return badRequest( no_token_or_invalid.render( this.userProvider ) );
		return ok( password_reset.render( this.userProvider, PASSWORD_RESET_FORM.fill( new PasswordReset( token ) ) ) );
	}
	
	/**
	 * Returns a token object if valid, null if not
	 *
	 * @param token
	 * @param type
	 * @return
	 */
	private TokenAction tokenIsValid( final String token, final Type type ) {
		TokenAction ret = null;
		if ( token != null && !token.trim().isEmpty() ) {
			final TokenAction ta = TokenAction.findByToken( token, type );
			if ( ta != null && ta.isValid() )
				ret = ta;
		}
		return ret;
	}
	
	public Result unverified() {
		AuthenticateBase.noCache( response() );
		return ok( unverified.render( this.userProvider ) );
	}
	
	public Result verify( final String token ) {
		AuthenticateBase.noCache( response() );
		final TokenAction ta = tokenIsValid( token, Type.EMAIL_VERIFICATION );
		if ( ta == null )
			return badRequest( no_token_or_invalid.render( this.userProvider ) );
		final String email = ta.getTargetUser().getEmail();
		User.verify( ta.getTargetUser() );
		flash( Application.FLASH_MESSAGE_KEY, this.msg.preferred( request() ).at( "playauthenticate.verify_email.success", email ) );
		if ( this.userProvider.getUser( session() ) != null )
			return redirect( routes.Application.index() );
		else
			return redirect( routes.Application.login() );
	}
}
