package mk.obl.ck.energy.csm.security;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.feth.play.module.pa.PlayAuthenticate;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.HandlerCache;

@Singleton
public class MyHandlerCache implements HandlerCache {
	
	private final DeadboltHandler		defaultHandler;
	
	private final PlayAuthenticate	auth;
	
	@Inject
	public MyHandlerCache( final PlayAuthenticate auth, final ExecutionContextProvider execContextProvider ) {
		this.auth = auth;
		this.defaultHandler = new MyDeadboltHandler( auth, execContextProvider );
	}
	
	@Override
	public DeadboltHandler apply( final String key ) {
		return this.defaultHandler;
	}
	
	@Override
	public DeadboltHandler get() {
		return this.defaultHandler;
	}
}