package mk.obl.ck.energy.csm.mssql.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
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
public class User extends MSSQLModel implements Subject {
	
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
	
	public static User findByAuthUserIdentity( final AuthUserIdentity identity ) {
		if ( identity == null )
			return null;
		if ( identity instanceof UsernamePasswordAuthUser )
			return findByUsernamePasswordIdentity( ( UsernamePasswordAuthUser )identity );
		else
			return getAuthUserFind( identity ).findUnique();
	}
	
	public static void merge( final AuthUser oldUser, final AuthUser newUser ) {
		User.findByAuthUserIdentity( oldUser ).merge( User.findByAuthUserIdentity( newUser ) );
	}
	
	public static void setLastLoginDate( final AuthUser knownUser ) {
		final User u = User.findByAuthUserIdentity( knownUser );
		u.lastLogin = new java.sql.Date( java.lang.System.currentTimeMillis() );
		u.save();
	}
	
	public static void verify( final User unverified ) {
		// You might want to wrap this into a transaction
		unverified.emailValidated = true;
		unverified.save();
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
		LinkedAccount a = this.getAccountByProvider( authUser.getProvider() );
		if ( a == null )
			if ( create ) {
				a = LinkedAccount.create( authUser );
				a.user = this;
				getEntityManager().persist( a );
			} else
				throw new RuntimeException( "Account not enabled for password usage" );
		a.providerUserId = authUser.getHashedPassword();
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
	
	@Override
	public String getIdentifier() {
		return Long.toString( this.getId() );
	}
	
	@Override
	public List< ? extends Permission > getPermissions() {
		return this.permissions;
	}
	
	@Override
	public List< ? extends Role > getRoles() {
		return this.roles;
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
