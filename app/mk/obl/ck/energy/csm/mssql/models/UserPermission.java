package mk.obl.ck.energy.csm.mssql.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NamedQuery;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.Table;

import be.objectify.deadbolt.java.models.Permission;

@Entity
@Table( name = "permissions" )
@NamedQuery( name = "FindByPermissionValue", query = "SELECT r FROM UserRole r where r.roleName = :rolename" )
public class UserPermission extends MSSQLModel implements Permission {
	
	private static final String	FIELD_PERMISSION_VALUE	= "value";
	
	@Column( length = 50 )
	public String								value;

	@Override
	public String getValue() {
		return value;
	}

	public static UserPermission findByValue( final String value ) {
		try {
			final Query query = getEntityManager().createNamedQuery( "FindByPermissionValue" );
			query.setParameter( FIELD_PERMISSION_VALUE, value );
			return ( UserPermission )query.getSingleResult();
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
	protected String classInfo() {
		final StringBuffer sb = new StringBuffer( "\n" );
		sb.append( "Permission value : " );
		sb.append( value );
		return sb.toString();
	}
}
