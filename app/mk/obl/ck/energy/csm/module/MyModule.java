package mk.obl.ck.energy.csm.module;

import com.feth.play.module.mail.IMailer;
import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.MailerFactory;
import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.providers.openid.OpenIdAuthProvider;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import mk.obl.ck.energy.csm.providers.MyStupidBasicAuthProvider;
import mk.obl.ck.energy.csm.providers.MyUsernamePasswordAuthProvider;
import mk.obl.ck.energy.csm.service.MyResolver;
import mk.obl.ck.energy.csm.service.MyUserService;

/**
 * Initial DI module.
 */
public class MyModule extends AbstractModule {
	
	@Override
	protected void configure() {
		install( new FactoryModuleBuilder().implement( IMailer.class, Mailer.class ).build( MailerFactory.class ) );
		bind( Resolver.class ).to( MyResolver.class );
		// bind( DataInitializer.class ).asEagerSingleton();
		bind( MyUserService.class ).asEagerSingleton();
		// bind(GoogleAuthProvider.class).asEagerSingleton();
		// bind(FacebookAuthProvider.class).asEagerSingleton();
		// bind(FoursquareAuthProvider.class).asEagerSingleton();
		bind( MyUsernamePasswordAuthProvider.class ).asEagerSingleton();
		bind( OpenIdAuthProvider.class ).asEagerSingleton();
		// bind(TwitterAuthProvider.class).asEagerSingleton();
		// bind(LinkedinAuthProvider.class).asEagerSingleton();
		// bind(VkAuthProvider.class).asEagerSingleton();
		// bind(XingAuthProvider.class).asEagerSingleton();
		// bind(UntappdAuthProvider.class).asEagerSingleton();
		// bind(PocketAuthProvider.class).asEagerSingleton();
		// bind(GithubAuthProvider.class).asEagerSingleton();
		bind( MyStupidBasicAuthProvider.class ).asEagerSingleton();
		// bind(SpnegoAuthProvider.class).asEagerSingleton();
		// bind(EventBriteAuthProvider.class).asEagerSingleton();
	}
}
