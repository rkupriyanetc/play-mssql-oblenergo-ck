package mk.obl.ck.energy.csm.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.controllers.AuthenticateBase;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import mk.obl.ck.energy.csm.mssql.models.User;
import mk.obl.ck.energy.csm.providers.MyUsernamePasswordAuthProvider;
import mk.obl.ck.energy.csm.providers.MyUsernamePasswordAuthProvider.MyLogin;
import mk.obl.ck.energy.csm.providers.MyUsernamePasswordAuthProvider.MySignup;
import mk.obl.ck.energy.csm.service.UserProvider;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.login;
import views.html.profile;
import views.html.restricted;
import views.html.signup;

public class Application extends Controller {
	
	public static final String	FLASH_MESSAGE_KEY	= "message";
	
	public static final String	FLASH_ERROR_KEY		= "error";
	
	public static String formatTimestamp( final long t ) {
		return new SimpleDateFormat( "yyyy-dd-MM HH:mm:ss" ).format( new Date( t ) );
	}
	
	private final PlayAuthenticate								auth;
	
	private final MyUsernamePasswordAuthProvider	provider;
	
	private final UserProvider										userProvider;
	
	@Inject
	public Application( final PlayAuthenticate auth, final MyUsernamePasswordAuthProvider provider,
			final UserProvider userProvider ) {
		this.auth = auth;
		this.provider = provider;
		this.userProvider = userProvider;
	}
	
	public Result doLogin() {
		AuthenticateBase.noCache( response() );
		final Form< MyLogin > filledForm = this.provider.getLoginForm().bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not fill everything properly
			return badRequest( login.render( this.auth, this.userProvider, filledForm ) );
		else
			// Everything was filled
			return this.provider.handleLogin( ctx() );
	}
	
	public Result doSignup() {
		AuthenticateBase.noCache( response() );
		final Form< MySignup > filledForm = this.provider.getSignupForm().bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not fill everything properly
			return badRequest( signup.render( this.auth, this.userProvider, filledForm ) );
		else
			// Everything was filled
			// do something with your part of the form before handling the user
			// signup
			return this.provider.handleSignup( ctx() );
	}
	
	public Result index() {
		return ok( index.render( this.userProvider ) );
	}
	
	public Result jsRoutes() {
		return ok( play.routing.JavaScriptReverseRouter.create( "jsRoutes", routes.javascript.Signup.forgotPassword() ) )
				.as( "text/javascript" );
	}
	
	public Result login() {
		return ok( login.render( this.auth, this.userProvider, this.provider.getLoginForm() ) );
	}
	
	@SubjectPresent
	public Result profile() {
		final User localUser = userProvider.getUser( session() );
		return ok( profile.render( this.auth, this.userProvider, localUser ) );
	}
	
	@SubjectPresent
	public Result restricted() {
		final User localUser = this.userProvider.getUser( session() );
		return ok( restricted.render( this.userProvider, localUser ) );
	}
	
	public Result signup() {
		return ok( signup.render( this.auth, this.userProvider, this.provider.getSignupForm() ) );
	}
}