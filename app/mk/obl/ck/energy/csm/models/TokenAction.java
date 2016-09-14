package mk.obl.ck.energy.csm.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.annotation.EnumValue;

import play.data.format.Formats;

@Entity
public class TokenAction extends AppModel {
	
	public enum Type {
		@EnumValue( "EV" )
		EMAIL_VERIFICATION, @EnumValue( "PR" )
		PASSWORD_RESET
	}
	
	/**
	 *
	 */
	private static final long												serialVersionUID	= 1L;
	
	/**
	 * Verification time frame (until the user clicks on the link in the email)
	 * in seconds
	 * Defaults to one week
	 */
	private final static long												VERIFICATION_TIME	= 7 * 24 * 3600;
	
	public static final Finder< Long, TokenAction >	find							= new Finder< Long, TokenAction >( Long.class,
			TokenAction.class );
	
	public static TokenAction create( final Type type, final String token, final User targetUser ) {
		final TokenAction ua = new TokenAction();
		ua.targetUser = targetUser;
		ua.token = token;
		ua.type = type;
		final Date created = new Date();
		ua.created = created;
		ua.expires = new Date( created.getTime() + VERIFICATION_TIME * 1000 );
		ua.save();
		return ua;
	}
	
	public static void deleteByUser( final User u, final Type type ) {
		final QueryIterator< TokenAction > iterator = find.where().eq( "targetUser.id", u.id ).eq( "type", type ).findIterate();
		while ( iterator.hasNext() )
			Ebean.delete( iterator.next() );
		iterator.close();
	}
	
	public static TokenAction findByToken( final String token, final Type type ) {
		return find.where().eq( "token", token ).eq( "type", type ).findUnique();
	}
	
	@Id
	public Long		id;
	
	@Column( unique = true )
	public String	token;
	
	@ManyToOne
	public User		targetUser;
	
	public Type		type;
	
	@Formats.DateTime( pattern = "yyyy-MM-dd HH:mm:ss" )
	public Date		created;
	
	@Formats.DateTime( pattern = "yyyy-MM-dd HH:mm:ss" )
	public Date		expires;
	
	public boolean isValid() {
		return this.expires.after( new Date() );
	}
}
