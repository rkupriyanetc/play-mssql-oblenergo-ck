package mk.obl.ck.energy.csm.mssql.models;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mk.obl.ck.energy.csm.mssql.models.api.MSSQLIdentifier;

/**
 * @author RVK
 */
@MappedSuperclass
public abstract class MSSQLModel implements MSSQLIdentifier, Serializable {
	
	private static final long			serialVersionUID					= 1L;
	
	protected static final Logger	LOGGER										= LoggerFactory.getLogger( MSSQLModel.class );
	
	public static final String		DEFAULT_PERSISTENCE_UNIT	= "defaultPersistenceUnit";
	
	public static final String		DEFAULT_DATASOURCE				= "DefaultDS";
	
	protected static final String	FIELD_ID									= "id";
	
	@PersistenceContext( unitName = DEFAULT_PERSISTENCE_UNIT )
	private static EntityManager	em;
	
	public static EntityManager getEntityManager() {
		if ( em == null ) {
			final EntityManagerFactory emf = Persistence.createEntityManagerFactory( DEFAULT_PERSISTENCE_UNIT );
			em = emf.createEntityManager();
		}
		LOGGER.warn( "EntityManager is {}", em );
		LOGGER.info( "EntityManager is {}", em );
		return em;
	}
	
	@Id
	@GeneratedValue
	@Column( updatable = false, columnDefinition = "bigint" )
	protected Long id;
	
	protected MSSQLModel() {}
	
	@Inject
	public MSSQLModel( final EntityManager manager ) {
		MSSQLModel.em = manager;
	}
	
	protected abstract String classInfo();
	
	@Override
	public Long getId() {
		return id;
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer( getClass().getName() );
		sb.append( " - " );
		sb.append( getClass().getSimpleName() );
		sb.append( classInfo() );
		sb.append( "ID - " );
		sb.append( id );
		final String s = sb.toString();
		LOGGER.info( s );
		return s;
	}
}