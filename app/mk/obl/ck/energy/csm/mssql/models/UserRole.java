package mk.obl.ck.energy.csm.mssql.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NamedQuery;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.Table;

import be.objectify.deadbolt.java.models.Role;

@Entity
@Table( name = "roles" )
@NamedQuery( name = "UserRole.findByRolename", query = "SELECT r FROM UserRole r where r.roleName = :rolename" )
public class UserRole extends MSSQLModel implements Role {
	
	private static final String	FIELD_ROLENAME	= "rolename";
	
	@Column( name = "role_name", length = 5 )
	private final String				roleName;
	
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
	
	public UserRole findByRoleName( final String roleName ) {
		try {
			final Query query = getEntityManager().createNamedQuery( "UserRole.findByRolename" );
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
	
	@Override
	public String getName() {
		return roleName;
	}
}