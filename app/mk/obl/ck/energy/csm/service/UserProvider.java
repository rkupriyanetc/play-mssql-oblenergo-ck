package mk.obl.ck.energy.csm.service;

import javax.inject.Inject;

import org.jetbrains.annotations.Nullable;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import mk.obl.ck.energy.csm.models.User;
import play.mvc.Http.Session;

/**
 * Service layer for User DB entity
 */
public class UserProvider {
	
	private final PlayAuthenticate auth;
	
	@Inject
	public UserProvider( final PlayAuthenticate auth ) {
		this.auth = auth;
	}
	
	@Nullable
	public User getUser( final Session session ) {
		final AuthUser currentAuthUser = this.auth.getUser( session );
		final User localUser = User.findByAuthUserIdentity( currentAuthUser );
		return localUser;
	}
}
