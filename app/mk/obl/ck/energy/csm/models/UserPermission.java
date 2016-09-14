package mk.obl.ck.energy.csm.models;

import javax.persistence.Entity;
import javax.persistence.Id;

import be.objectify.deadbolt.java.models.Permission;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
public class UserPermission extends AppModel implements Permission {

	/**
	 *
	 */
	private static final long														serialVersionUID	= 1L;

	@Id
	public Long																					id;

	public String																				value;

	public static final Finder< Long, UserPermission >	find							= new Finder< Long, UserPermission >( Long.class,
			UserPermission.class );

	@Override
	public String getValue() {
		return value;
	}

	public static UserPermission findByValue( final String value ) {
		return find.where().eq( "value", value ).findUnique();
	}
}
