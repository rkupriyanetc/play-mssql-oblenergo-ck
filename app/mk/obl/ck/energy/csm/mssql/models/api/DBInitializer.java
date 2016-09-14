package mk.obl.ck.energy.csm.mssql.models.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Model;

public class DBInitializer {
	
	private static final Logger	LOGGER			= LoggerFactory.getLogger( DBInitializer.class );
	
	private static Connection		connection	= null;
	
	private static Statement		statement		= null;
	
	protected static void dbInitializer() {
		// final File fsql = null;
		// final String sql = null;
		// final FileInputStream fis = null;
		try {
			connection = Model.db().getPluginApi().getServerConfig().getDataSource().getConnection();
			/*
			 * fsql = play.Play.application().getFile(
			 * "/conf/evolutions/csm/create_tables.sql" );
			 * LOGGER.warn( "SQL file is ready {}", fsql.getName() );
			 * final int size = ( int )fsql.length();
			 * int s = 0;
			 * final byte b[] = new byte[ size + 1 ];
			 * fis = null;
			 * fis = new FileInputStream( fsql );
			 * s = fis.read( b );
			 * if ( s != size )
			 * LOGGER.error( "Error read the file {}", fsql );
			 * else
			 * sql = new String( b );
			 */
			/*
			 * if ( st.execute( sql ) )
			 * LOGGER.warn( "Evolution is executed good !!!" );
			 * else
			 * LOGGER.warn( "Evolution is not executed !!!" );
			 */
		}
		catch ( final SQLException sqle ) {
			LOGGER.error( "Get Connection in DataInitializer failed. {}", sqle );
		}
		try {
			statement = connection.createStatement();
		}
		catch ( final SQLException sqle ) {
			LOGGER.error( "Create Statement in DataInitializer failed. {}", sqle );
		}
		/*
		 * catch ( final FileNotFoundException fnfe ) {
		 * LOGGER.error( "File not found {}.", fsql.getName() );
		 * }
		 * catch ( final IOException ioe ) {
		 * LOGGER.error( "Error read of file {}.", fsql.getName() );
		 * }
		 * finally {
		 * try {
		 * if ( st != null )
		 * st.close();
		 * if ( fis != null )
		 * fis.close();
		 * if ( con != null )
		 * con.close();
		 * }
		 * catch ( final SQLException sqle ) {
		 * LOGGER.error( "Statement is not clised. {}", sqle );
		 * }
		 * catch ( final IOException ioe ) {
		 * LOGGER.error( "FileInputStream is not clised. {}", ioe );
		 * }
		 * }
		 */
	}
	
	public static Connection getConnection() {
		if ( connection == null )
			dbInitializer();
		return connection;
	}
	
	public static Statement getStatement() {
		if ( statement == null )
			dbInitializer();
		return statement;
	}
}
