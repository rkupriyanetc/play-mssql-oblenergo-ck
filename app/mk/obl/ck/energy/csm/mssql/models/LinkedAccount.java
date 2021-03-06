package mk.obl.ck.energy.csm.mssql.models;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.Table;

import com.feth.play.module.pa.user.AuthUser;

@Entity
@Table( name = "linkeds" )
/*
 * @NamedQueries( {
 * @NamedQuery( name = "FindByProviderKey", query =
 * "select l from LinkedAccount l where l.providerKey = :providerKey and l.user_id = :user_id"
 * ) } )
 */
public class LinkedAccount extends MSSQLModel implements Serializable {
	
	private static final String	FIND_LINKEDS_BY_PROVIDER_KEY	= "select * from linkeds where provider_key = :provider_key and user_id = :user_id";
	
	private static final long		serialVersionUID							= 1L;
	
	private static final String	FIELD_USER_ID									= "user_id";
	
	private static final String	FIELD_USER_PROVIDER						= "user_provider";
	
	private static final String	FIELD_PROVIDER_KEY						= "provider_key";
	
	public static LinkedAccount create( final AuthUser authUser ) {
		final LinkedAccount ret = new LinkedAccount();
		ret.update( authUser );
		return ret;
	}
	
	public static LinkedAccount create( final LinkedAccount la ) {
		final LinkedAccount ret = new LinkedAccount();
		ret.providerKey = la.providerKey;
		ret.providerUserId = la.providerUserId;
		return ret;
	}
	
	public static LinkedAccount findByProviderKey( final User user, final String key ) {
		// return find.where().eq( "user", user ).eq( "providerKey", key
		// ).findUnique();
		try {
			final Query query = getEntityManager().createNativeQuery( FIND_LINKEDS_BY_PROVIDER_KEY );
			query.setParameter( FIELD_USER_ID, user.getId() );
			query.setParameter( FIELD_PROVIDER_KEY, key );
			return ( LinkedAccount )query.getSingleResult();
		}
		catch ( final IllegalStateException ise ) {
			LOGGER.error( "{}", ise );
			return null;
		}
		catch ( final IllegalArgumentException iae ) {
			LOGGER.error( "{}", iae );
			return null;
		}
		catch ( final EntityNotFoundException enfe ) {
			LOGGER.error( "{}", enfe );
			return null;
		}
		catch ( final NonUniqueResultException nure ) {
			LOGGER.error( "{}", nure );
			return null;
		}
	}
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "user_id", nullable = false )
	private User		user;
	
	@Basic
	@Column( name = "user_provider", length = 30 )
	private String	providerUserId;
	
	@Basic
	@Column( name = "provider_key", length = 30 )
	private String	providerKey;
	
	@Override
	protected String classInfo() {
		final StringBuffer sb = new StringBuffer( "\n" );
		sb.append( "TargetUser : " );
		sb.append( user );
		sb.append( "\n" );
		sb.append( "User Provider : " );
		sb.append( this.providerUserId );
		sb.append( ". Provider Key : " );
		sb.append( this.providerKey );
		final String s = sb.toString();
		LOGGER.info( s );
		return s;
	}
	
	public String getProviderKey() {
		return this.providerKey;
	}
	
	public String getProviderUserId() {
		return this.providerUserId;
	}
	
	public User getTargetUser() {
		return this.user;
	}
	
	public void setProviderKey( final String providerKey ) {
		this.providerKey = providerKey;
	}
	
	public void setProviderUserId( final String providerUserId ) {
		this.providerUserId = providerUserId;
	}
	
	public void setTargetUser( final User user ) {
		this.user = user;
	}
	
	public void update( final AuthUser authUser ) {
		this.providerKey = authUser.getProvider();
		this.providerUserId = authUser.getId();
	}
}
