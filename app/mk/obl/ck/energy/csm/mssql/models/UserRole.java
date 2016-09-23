package mk.obl.ck.energy.csm.mssql.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.Table;

import be.objectify.deadbolt.java.models.Role;

@Entity
@Table( name = "roles" )
@NamedQueries( { @NamedQuery( name = "FindByUserRolename", query = "select r from UserRole r where r.roleName = :rolename" ) } )
public class UserRole extends MSSQLModel implements Role {
	
	public static final String	OPER_ROLE_NAME	= "OPER";
	
	public static final String	ADMIN_ROLE_NAME	= "ADMIN";
	
	public static final Role		OPER						= new UserRole( OPER_ROLE_NAME );
	
	public static final Role		ADMIN						= new UserRole( ADMIN_ROLE_NAME );
	
	private static final String	FIELD_ROLENAME	= "rolename";
	
	public static UserRole findByRoleName( final String roleName ) {
		try {
			final Query query = getEntityManager().createNamedQuery( "FindByUserRolename" );
			query.setParameter( FIELD_ROLENAME, roleName );
			return ( UserRole )query.getSingleResult();
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
	
	@Column( name = "role_name", length = 5 )
	private final String roleName;
	
	// public UserRole() {}
	private UserRole( final String roleName ) {
		this.roleName = roleName;
	}
	
	@Override
	protected String classInfo() {
		final StringBuffer sb = new StringBuffer( "\n" );
		sb.append( "RoleName : " );
		sb.append( roleName );
		return sb.toString();
	}
	
	@Override
	public boolean equals( final Object o ) {
		if ( o == null )
			return false;
		if ( o instanceof UserRole )
			return ( ( UserRole )o ).getName().equals( this.getName() );
		return false;
	}
	
	@Override
	public String getName() {
		return roleName;
	}
}
