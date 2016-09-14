package mk.obl.ck.energy.csm.service;

import java.util.Arrays;

import mk.obl.ck.energy.csm.models.SecurityRole;

/**
 * Data initializer class.
 */
public class DataInitializer {
	
	// private static final Logger LOGGER = LoggerFactory.getLogger(
		// DataInitializer.class );
	public DataInitializer() {
		// DBInitializer.dbInitializer();
		if ( SecurityRole.find.findRowCount() == 0 )
			for ( final String roleName : Arrays.asList( mk.obl.ck.energy.csm.controllers.Application.USER_ROLE ) ) {
				final SecurityRole role = new SecurityRole();
				role.roleName = roleName;
				role.save();
			}
	}
}
