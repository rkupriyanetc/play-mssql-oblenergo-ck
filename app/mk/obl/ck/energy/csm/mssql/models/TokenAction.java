package mk.obl.ck.energy.csm.mssql.models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.Table;

import com.avaje.ebean.annotation.EnumValue;

import play.data.format.Formats;

@Entity
@Table( name = "actions" )
@NamedQueries( {
		@NamedQuery( name = "FindByTokenAndType", query = "select a from TokenAction a where a.token = :token and a.type = :type" ),
		@NamedQuery( name = "FindByUserAndType", query = "select a from TokenAction a where a.user = :user_id and a.type = :type" ) } )
public class TokenAction extends MSSQLModel {
	
	public enum Type {
		@EnumValue( "EV" )
		EMAIL_VERIFICATION, @EnumValue( "PR" )
		PASSWORD_RESET
	}
	
	/**
	 * Verification time frame (until the user clicks on the link in the email)
	 * in seconds
	 * Defaults to one week
	 */
	private final static long		VERIFICATION_TIME	= 7 * 24 * 3600;
	
	private static final String	FIELD_USER_ID			= "user_id";
	
	private static final String	FIELD_TOKEN				= "token";
	
	private static final String	FIELD_TYPE				= "type";
	
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
		final Query query = getEntityManager().createNamedQuery( "FindByUserAndType" );
		query.setParameter( FIELD_USER_ID, u.getId() );
		query.setParameter( FIELD_TYPE, type.name() );
		final List< TokenAction > iterator = query.getResultList();
		// find.where().eq( "targetUser.id", u.id ).eq( "type", type
		// ).findIterate();
		while ( iterator.iterator().hasNext() )
			getEntityManager().remove( iterator.iterator().next() );
	}
	
	public static TokenAction findByToken( final String token, final Type type ) {
		try {
			final Query query = getEntityManager().createNamedQuery( "FindByTokenAndType" );
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
	
	@Column( unique = true )
	private String	token;
	
	@ManyToOne
	private User		user;
	
	private Type		type;
	
	@Formats.DateTime( pattern = "yyyy-MM-dd HH:mm:ss" )
	private Date		created;
	
	@Formats.DateTime( pattern = "yyyy-MM-dd HH:mm:ss" )
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
	
	public boolean isValid() {
		return this.expires.after( new Date() );
	}
}
