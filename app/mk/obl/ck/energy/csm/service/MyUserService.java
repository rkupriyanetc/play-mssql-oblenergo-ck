package mk.obl.ck.energy.csm.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.service.AbstractUserService;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;

import mk.obl.ck.energy.csm.mssql.models.User;

@Singleton
public class MyUserService extends AbstractUserService {
	
	@Inject
	public MyUserService( final PlayAuthenticate auth ) {
		super( auth );
	}
	
	@Override
	public Object getLocalIdentity( final AuthUserIdentity identity ) {
		// For production: Caching might be a good idea here...
		// ...and dont forget to sync the cache when users get deactivated/deleted
		final User u = User.findByAuthUserIdentity( identity );
		if ( u != null )
			return u.getId();
		else
			return null;
	}
	
	@Override
	public AuthUser link( final AuthUser oldUser, final AuthUser newUser ) {
		User.addLinkedAccount( oldUser, newUser );
		return newUser;
	}
	
	@Override
	public AuthUser merge( final AuthUser newUser, final AuthUser oldUser ) {
		if ( !oldUser.equals( newUser ) )
			User.merge( oldUser, newUser );
		return oldUser;
	}
	
	@Override
	public Object save( final AuthUser authUser ) {
		final boolean isLinked = User.existsByAuthUserIdentity( authUser );
		if ( !isLinked )
			return User.create( authUser ).getId();
		else
			// we have this user already, so return null
			return null;
	}
	
	@Override
	public AuthUser update( final AuthUser knownUser ) {
		// User logged in again, bump last login date
		User.setLastLoginDate( knownUser );
		return knownUser;
	}
}
