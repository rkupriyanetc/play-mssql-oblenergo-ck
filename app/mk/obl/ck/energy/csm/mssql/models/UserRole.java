package mk.obl.ck.energy.csm.mssql.models;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.Table;

import be.objectify.deadbolt.java.models.Role;

@Entity
@Table( name = "roles" )
public class UserRole extends MSSQLModel implements Role, Serializable {
	
	private static final String	FIND_ROLE_BY_ROLENAME	= "select * from roles where rolename = :rolename";
	
	private static final long		serialVersionUID			= 1L;
	
	public static final String	OPER_ROLE_NAME				= "OPER";
	
	public static final String	ADMIN_ROLE_NAME				= "ADMIN";
	// public static final Role OPER = new UserRole( OPER_ROLE_NAME );
	
	// public static final Role ADMIN = new UserRole( ADMIN_ROLE_NAME );
	private static final String	FIELD_ROLENAME				= "rolename";
	
	public static UserRole findByRoleName( final String roleName ) {
		try {
			final Query query = getEntityManager().createNativeQuery( FIND_ROLE_BY_ROLENAME, UserRole.class );
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
	
	@Basic
	@Column( name = "rolename", length = 5 )
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
