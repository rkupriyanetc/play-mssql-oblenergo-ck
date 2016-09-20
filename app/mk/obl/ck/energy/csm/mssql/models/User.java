package mk.obl.ck.energy.csm.mssql.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;
import com.feth.play.module.pa.user.NameIdentity;

import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Role;
import be.objectify.deadbolt.java.models.Subject;
import mk.obl.ck.energy.csm.mssql.models.TokenAction.Type;
import play.data.format.Formats;
import play.data.validation.Constraints;

@Entity
@Table( name = "users" )
@NamedQueries( {
		@NamedQuery( name = "FindAuthUser", query = "select u from User u left join LinkedAccount a on a.user = u where u.active = true and a.providerKey = :linked_account_provider_key and a.providerUserId = :linked_account_provider_user_id" ),
		@NamedQuery( name = "FindUsernamePasswordAuthUser", query = "select u from User u left join LinkedAccount a on a.user = u where u.active = true and u.email = :email and a.providerKey = :linked_account_provider_key" ),
		@NamedQuery( name = "FindByEmail", query = "select u from User u where u.active = true and u.email = :email" ) } )
public class User extends MSSQLModel implements Subject {
	
	private static final String	FIELD_EMAIL														= "email";
	
	private static final String	FIELD_LINKED_ACCOUNT_PROVIDER_KEY			= "linked_account_provider_key";
	
	private static final String	FIELD_LINKED_ACCOUNT_PROVIDER_USER_ID	= "linked_account_provider_user_id";
	
	public static void addLinkedAccount( final AuthUser oldUser, final AuthUser newUser ) {
		final User u = User.findByAuthUserIdentity( oldUser );
		u.linkedAccounts.add( LinkedAccount.create( newUser ) );
		getEntityManager().merge( u ); // Save to Database
	}
	
	public static User create( final AuthUser authUser ) {
		final User user = new User();
		user.roles = Collections.singletonList( UserRole.findByRoleName( UserRole.OPER_ROLE_NAME ) );
		// user.permissions = new ArrayList<UserPermission>();
		// user.permissions.add(UserPermission.findByValue("printers.edit"));
		user.active = true;
		user.lastLogin = new Date();
		user.linkedAccounts = Collections.singletonList( LinkedAccount.create( authUser ) );
		if ( authUser instanceof EmailIdentity ) {
			final EmailIdentity identity = ( EmailIdentity )authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
			user.email = identity.getEmail();
			user.emailValidated = false;
		}
		if ( authUser instanceof NameIdentity ) {
			final NameIdentity identity = ( NameIdentity )authUser;
			final String name = identity.getName();
			if ( name != null )
				user.name = name;
		}
		if ( authUser instanceof FirstLastNameIdentity ) {
			final FirstLastNameIdentity identity = ( FirstLastNameIdentity )authUser;
			final String firstName = identity.getFirstName();
			final String lastName = identity.getLastName();
			if ( firstName != null )
				user.firstName = firstName;
			if ( lastName != null )
				user.lastName = lastName;
		}
		getEntityManager().persist( user ); // Save to Database
		// Ebean.saveManyToManyAssociations(user, "roles");
		// Ebean.saveManyToManyAssociations(user, "permissions");
		return user;
	}
	
	public static boolean existsByAuthUserIdentity( final AuthUserIdentity identity ) {
		final List< User > exp;
		if ( identity instanceof UsernamePasswordAuthUser )
			exp = getUsernamePasswordAuthUserFind( ( UsernamePasswordAuthUser )identity );
		else
			exp = getAuthUserFind( identity );
		return exp != null && !exp.isEmpty();
	}
	
	public static User findByAuthUserIdentity( final AuthUserIdentity identity ) {
		if ( identity == null )
			return null;
		if ( identity instanceof UsernamePasswordAuthUser )
			return findByUsernamePasswordIdentity( ( UsernamePasswordAuthUser )identity );
		else
			return getAuthUserFind( identity ).get( 0 );
	}
	
	public static User findByEmail( final String email ) {
		return getEmailUserFind( email ).get( 0 );
	}
	
	public static User findByUsernamePasswordIdentity( final UsernamePasswordAuthUser identity ) {
		return getUsernamePasswordAuthUserFind( identity ).get( 0 );
	}
	
	private static List< User > getAuthUserFind( final AuthUserIdentity identity ) {
		// return find.where().eq( "active", true ).eq(
		// "linkedAccounts.providerUserId", identity.getId() ).eq(
		// "linkedAccounts.providerKey", identity.getProvider() );
		try {
			final Query query = getEntityManager().createNamedQuery( "FindAuthUser" );
			query.setParameter( FIELD_LINKED_ACCOUNT_PROVIDER_USER_ID, identity.getId() );
			query.setParameter( FIELD_LINKED_ACCOUNT_PROVIDER_KEY, identity.getProvider() );
			return query.getResultList();
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
	
	private static List< User > getEmailUserFind( final String email ) {
		// return find.where().eq( "active", true ).eq( "email", email );
		try {
			final Query query = getEntityManager().createNamedQuery( "FindByEmail" );
			query.setParameter( FIELD_EMAIL, email );
			return query.getResultList();
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
	
	private static List< User > getUsernamePasswordAuthUserFind( final UsernamePasswordAuthUser identity ) {
		// return getEmailUserFind( identity.getEmail() ).eq(
		// "linkedAccounts.providerKey", identity.getProvider() );
		try {
			final Query query = getEntityManager().createNamedQuery( "FindUsernamePasswordAuthUser" );
			query.setParameter( FIELD_EMAIL, identity.getEmail() );
			query.setParameter( FIELD_LINKED_ACCOUNT_PROVIDER_KEY, identity.getProvider() );
			return query.getResultList();
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
	
	public static void merge( final AuthUser oldUser, final AuthUser newUser ) {
		User.findByAuthUserIdentity( oldUser ).merge( User.findByAuthUserIdentity( newUser ) );
	}
	
	public static void setLastLoginDate( final AuthUser knownUser ) {
		final User u = User.findByAuthUserIdentity( knownUser );
		u.lastLogin = new Date( java.lang.System.currentTimeMillis() );
		getEntityManager().merge( u );
	}
	
	public static void verify( final User unverified ) {
		// You might want to wrap this into a transaction
		unverified.emailValidated = true;
		getEntityManager().merge( unverified );
		TokenAction.deleteByUser( unverified, Type.EMAIL_VERIFICATION );
	}
	
	@Constraints.Email
	// if you make this unique, keep in mind that users *must* merge/link their
	// accounts then on signup with additional providers
	// @Column(unique = true)
	private String									email;
	
	private String									name;
	
	private String									firstName;
	
	private String									lastName;
	
	@Formats.DateTime( pattern = "yyyy-MM-dd HH:mm:ss" )
	private Date										lastLogin;
	
	private boolean									active;
	
	private boolean									emailValidated;
	
	@ManyToMany
	private List< UserRole >				roles;
	
	@OneToMany( cascade = CascadeType.ALL )
	private List< LinkedAccount >		linkedAccounts;
	
	@ManyToMany
	private List< UserPermission >	permissions;
	
	public void changePassword( final UsernamePasswordAuthUser authUser, final boolean create ) {
		LinkedAccount a = LinkedAccount.findByProviderKey( this, authUser.getProvider() );
		if ( a == null )
			if ( create ) {
				a = LinkedAccount.create( authUser );
				a.setTargetUser( this );
				getEntityManager().persist( a );
			} else
				throw new RuntimeException( "Account not enabled for password usage" );
		a.setProviderUserId( authUser.getHashedPassword() );
		getEntityManager().merge( a );
	}
	
	@Override
	protected String classInfo() {
		final StringBuffer sb = new StringBuffer( "\n" );
		sb.append( "User " );
		sb.append( this.email );
		sb.append( ". Id : " );
		sb.append( this.getId() );
		sb.append( "\n" );
		sb.append( this.firstName );
		if ( this.firstName != null && !this.firstName.isEmpty() )
			sb.append( " " );
		sb.append( this.name );
		if ( this.lastName != null && !this.lastName.isEmpty() )
			sb.append( " " );
		sb.append( this.lastName );
		sb.append( "\n" );
		sb.append( "Last login : " );
		sb.append( this.lastLogin );
		sb.append( ", is email validated : " );
		sb.append( this.emailValidated );
		sb.append( ", is active : " );
		sb.append( this.active );
		final String s = sb.toString();
		LOGGER.info( s );
		return s;
	}
	
	public String getEmail() {
		return this.email;
	}
	
	@Override
	public String getIdentifier() {
		return Long.toString( this.getId() );
	}
	
	public List< LinkedAccount > getLinkedAccounts() {
		return this.linkedAccounts;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public List< ? extends Permission > getPermissions() {
		return this.permissions;
	}
	
	public Set< String > getProviders() {
		final Set< String > providerKeys = new HashSet< String >( this.linkedAccounts.size() );
		for ( final LinkedAccount acc : this.linkedAccounts )
			providerKeys.add( acc.getProviderKey() );
		return providerKeys;
	}
	
	@Override
	public List< ? extends Role > getRoles() {
		return this.roles;
	}
	
	public boolean isEmailValidated() {
		return this.emailValidated;
	}
	
	public void merge( final User otherUser ) {
		for ( final LinkedAccount acc : otherUser.linkedAccounts )
			this.linkedAccounts.add( LinkedAccount.create( acc ) );
		// deactivate the merged user that got added to this one
		otherUser.active = false;
		Arrays.asList( new User[] { otherUser, this } ).forEach( u -> getEntityManager().merge( u ) );
	}
	
	public void resetPassword( final UsernamePasswordAuthUser authUser, final boolean create ) {
		// You might want to wrap this into a transaction
		this.changePassword( authUser, create );
		TokenAction.deleteByUser( this, Type.PASSWORD_RESET );
	}
}
