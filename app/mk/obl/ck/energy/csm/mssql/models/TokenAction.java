package mk.obl.ck.energy.csm.mssql.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.Table;

import com.avaje.ebean.annotation.EnumValue;

import play.data.format.Formats;

@Entity
@Table( name = "tokens" )
/*
 * @NamedQueries( {
 * @NamedQuery( name = "FindByTokenAndType", query =
 * "select a from TokenAction a where a.token = :token and a.type = :type" ),
 * @NamedQuery( name = "FindByUserAndType", query =
 * "select a from TokenAction a where a.user_id = :user_id and a.type = :type" )
 * } )
 */
public class TokenAction extends MSSQLModel implements Serializable {
	
	public enum Type {
		@EnumValue( "EV" )
		EMAIL_VERIFICATION, @EnumValue( "PR" )
		PASSWORD_RESET
	}
	
	private static final String	FIND_TOKENS_BY_TOKEN_AND_TYPE	= "select * from tokens where token = :token and type = :type";
	
	private static final String	FIND_TOKENS_BY_USER_AND_TYPE	= "select * from tokens where user_id = :user_id and type = :type";
	
	private static final long		serialVersionUID							= 1L;
	
	/**
	 * Verification time frame (until the user clicks on the link in the email)
	 * in seconds
	 * Defaults to one week
	 */
	private final static long		VERIFICATION_TIME							= 7 * 24 * 3600;
	
	private static final String	FIELD_USER_ID									= "user_id";
	
	private static final String	FIELD_TOKEN										= "token";
	
	private static final String	FIELD_TYPE										= "type";
	
	public static TokenAction create( final Type type, final String token, final User targetUser ) {
		final TokenAction ua = new TokenAction();
		ua.user = targetUser;
		ua.token = token;
		ua.type = type;
		final Date created = new Date();
		ua.created = created;
		ua.expires = new Date( created.getTime() + VERIFICATION_TIME * 1000 );
		getEntityManager().persist( ua ); // Save to Database
		return ua;
	}
	
	public static void deleteByUser( final User u, final Type type ) {
		final Query query = getEntityManager().createNativeQuery( FIND_TOKENS_BY_USER_AND_TYPE );
		query.setParameter( FIELD_USER_ID, u.getId() );
		query.setParameter( FIELD_TYPE, type.name() );
		@SuppressWarnings( "unchecked" )
		final List< TokenAction > iterator = query.getResultList();
		// find.where().eq( "targetUser.id", u.id ).eq( "type", type
		// ).findIterate();
		while ( iterator.iterator().hasNext() )
			getEntityManager().remove( iterator.iterator().next() );
	}
	
	public static TokenAction findByToken( final String token, final Type type ) {
		try {
			final Query query = getEntityManager().createNativeQuery( FIND_TOKENS_BY_TOKEN_AND_TYPE );
			query.setParameter( FIELD_TOKEN, token );
			query.setParameter( FIELD_TYPE, type.name() );
			return ( TokenAction )query.getSingleResult();
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
	
	@Basic
	@Column( unique = true, length = 70 )
	private String	token;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "user_id", nullable = false )
	private User		user;
	
	@Basic
	@Enumerated( EnumType.STRING )
	@Column( length = 2 )
	private Type		type;
	
	@Formats.DateTime( pattern = "yyyy-MM-dd HH:mm:ss" )
	@Column( columnDefinition = "datetime not null" )
	private Date		created;
	
	@Formats.DateTime( pattern = "yyyy-MM-dd HH:mm:ss" )
	@Column( columnDefinition = "datetime not null" )
	private Date		expires;
	
	@Override
	protected String classInfo() {
		final StringBuffer sb = new StringBuffer( "\n" );
		sb.append( "TargetUser : " );
		sb.append( user );
		sb.append( "\n" );
		sb.append( "Token : " );
		sb.append( token );
		sb.append( ", Type : " );
		sb.append( type );
		sb.append( "\n" );
		sb.append( "Created : " );
		sb.append( created );
		sb.append( ", Expires : " );
		sb.append( expires );
		final String s = sb.toString();
		LOGGER.info( s );
		return s;
	}
	
	public User getTargetUser() {
		return this.user;
	}
	
	public String getToken() {
		return this.token;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public boolean isValid() {
		return this.expires.after( new Date() );
	}
}
