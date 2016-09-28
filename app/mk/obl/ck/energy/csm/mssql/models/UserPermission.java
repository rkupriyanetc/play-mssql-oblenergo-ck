package mk.obl.ck.energy.csm.mssql.models;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.Table;

import be.objectify.deadbolt.java.models.Permission;

@Entity
@Table( name = "permissions" )
/*
 * @NamedQueries( {
 * @NamedQuery( name = "FindByPermissionValue", query =
 * "select p from UserPermission p where p.value = :value" ) } )
 */
public class UserPermission extends MSSQLModel implements Permission, Serializable {
	
	private static final String	FIND_PERMISSION_BY_VALUE	= "select * from permissions where value = :value";
	
	private static final long		serialVersionUID					= 1L;
	
	private static final String	FIELD_PERMISSION_VALUE		= "value";
	
	public static UserPermission findByValue( final String value ) {
		try {
			final Query query = getEntityManager().createNativeQuery( FIND_PERMISSION_BY_VALUE );
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
	
	@Basic
	@Column( length = 50 )
	private String value;
	
	@Override
	protected String classInfo() {
		final StringBuffer sb = new StringBuffer( "\n" );
		sb.append( "UserPermission : " );
		sb.append( value );
		return sb.toString();
	}
	
	@Override
	public String getValue() {
		return value;
	}
}
