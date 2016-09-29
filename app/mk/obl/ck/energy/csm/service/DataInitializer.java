package mk.obl.ck.energy.csm.service;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mk.obl.ck.energy.csm.mssql.models.MSSQLModel;
import mk.obl.ck.energy.csm.mssql.models.UserRole;

/**
 * Data initializer class.
 */
public class DataInitializer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( DataInitializer.class );
	
	public DataInitializer() {
		// DBInitializer.dbInitializer();
		/*
		 * if ( SecurityRole.find.findRowCount() == 0 )
		 * for ( final String roleName : Arrays.asList(
		 * mk.obl.ck.energy.csm.controllers.Application.USER_ROLE ) ) {
		 * final SecurityRole role = new SecurityRole();
		 * role.roleName = roleName;
		 * role.save();
		 * }
		 */
		final EntityManager em = MSSQLModel.getEntityManager();
		if ( UserRole.findByRoleName( UserRole.ADMIN_ROLE_NAME ) == null ) {
			em.persist( UserRole.ADMIN );
			LOGGER.warn( "UserRole ADMIN is {}", UserRole.ADMIN );
		}
		if ( UserRole.findByRoleName( UserRole.OPER_ROLE_NAME ) == null ) {
			em.persist( UserRole.OPER );
			LOGGER.warn( "UserRole OPER is {}", UserRole.OPER );
		}
	}
}
