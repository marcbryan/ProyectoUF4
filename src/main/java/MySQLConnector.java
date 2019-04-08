import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Componente para conectarse a una base de datos MySQL y realizar consultas
 * @author Marc
 */
public class MySQLConnector implements Serializable {
	// Atributos privados
	private Connection conn = null;
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	private MySQListener listener = new MySQListener();
	
	private String usuario = "";
	private String database = "";
	
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	private final String timezone = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	
	/**
	 * Constructor sin parámetros de MySQLConnector
	 */
	public MySQLConnector() {
		// Línea importante para que la clase listener reciba los datos
		changes.addPropertyChangeListener(listener);
	}
	
	/**
	 * Crea una conexión a una base de datos MySQL con los parámetros que se especifiquen
	 * @param url - La URL a la que se intentará conecta (ej. "localhost/NombreBBDD")
	 * @param user - El nombre de usuario
	 * @param password - La contraseña del usuario
	 * @return Devuelve una conexión
	 */
	public Connection createConnection(String url, String user, String password) {
		conn = null;
		try {
			String [] sp = url.split("/");
			String db_name = sp[sp.length-1];
			conn = DriverManager.getConnection("jdbc:mysql://"+url+timezone, user, password);
			database = db_name;
			usuario = user;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * Crea una conexión a una base de datos MySQL con el usuario por defecto (root)
	 * @param url - La URL a la que se intentará conecta (ej. "localhost/NombreBBDD")
	 * @return Devuelve una conexión
	 */
	public Connection createConnection(String url) {
		conn = null;
		try {
			String [] sp = url.split("/");
			String db_name = sp[sp.length-1];
			String user = "root";
			conn = DriverManager.getConnection("jdbc:mysql://"+url+timezone, user, "");
			database = db_name;
			usuario = user;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * Ejecuta una consulta del tipo SELECT, ya que devolverá un ResultSet
	 * @param sql - La consulta a ejecutar
	 * @return Devuelve las filas de la consulta ejecutada
	 */
	public ResultSet executeQuery(String sql) {
		ResultSet rs = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			String date = sdf.format(Calendar.getInstance().getTime());
			// Pongo los datos de la consulta en el objeto
			QueryData dq = new QueryData(usuario, database, sql, date, getRowCount(rs), getQueryType(sql));
			// Lo paso al listener para que los guarde
			changes.firePropertyChange("data_query", null, dq);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// No cierro ResultSet ni Statement porque si no, no se pueden consultar los datos que tenga el ResultSet
		return rs;
	}
	
	/**
	 * Ejecuta una consulta que modifique la base de datos (INSERT, UPDATE, DELETE)
	 * @param sql - La consulta a ejecutar
	 * @return Devuelve el número de filas modificadas. En caso de error devolverá -1
	 */
	public int executeUpdate(String sql) {
		int row_count = 0;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			row_count = stmt.executeUpdate(sql);
			String date = sdf.format(Calendar.getInstance().getTime());
			// Pongo los datos de la consulta en el objeto
			QueryData dq = new QueryData(usuario, database, sql, date, row_count, getQueryType(sql));
			// Lo paso al listener para que los guarde
			changes.firePropertyChange("data_query", null, dq);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return row_count;
	}
	
	/**
	 * Llama a un Stored Procedure que modifique la base de datos (que el Stored Procedure sea INSERT, UPDATE o DELETE)
	 * @param sql - La consulta a ejecutar
	 * @return Devuelve el número de filas modificadas. En caso de error devolverá -1
	 */
	public int callProcedure(String sql) {
		int rows = -1;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute(sql);
			String date = sdf.format(Calendar.getInstance().getTime());
			rows = stmt.getUpdateCount();
			QueryData dq = new QueryData(usuario, database, sql, date, rows, getQueryType(sql));
			changes.firePropertyChange("data_query", null, dq);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return rows;
	}
	
	/**
	 * Llama a un Stored Procedure que haga SELECT 
	 * @param sql - La consulta a ejecutar
	 * @return Devuelve las filas de la consulta ejecutada
	 */
	public ResultSet executeProcedure(String sql) {
		ResultSet rs = null;
		CallableStatement cs = null;
		try {
			cs = conn.prepareCall(sql);
			rs = cs.executeQuery(sql);
			String date = sdf.format(Calendar.getInstance().getTime());
			int rows = getRowCount(rs);
			QueryData dq = new QueryData(usuario, database, sql, date, rows, getQueryType(sql));
			changes.firePropertyChange("data_query", null, dq);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// No cierro ResultSet ni CallableStatement porque si no, no se pueden consultar los datos que tenga el ResultSet
		return rs;
	}
	
	/**
	 * Busca los datos de las consultas que haya realizado un usuario en una base de datos concreta
	 * @param db_name - El nombre de la base de datos 
	 * @param user - El nombre de usuario
	 */
	public void query1(String db_name, String user) {
		boolean no_results = true;
		ArrayList<QueryData> info = listener.getInfo();
		for (QueryData data : info) {
			if (data.getDb_name().equals(db_name) && data.getUser().equals(user)) {
				System.out.println("[INFO] "+data.dataFromQuery1());
				no_results = false;
			}
		}
		if (no_results) {
			System.out.println("[INFO] El usuario '"+user+"' no ha realizado consultas en la base de datos '"+db_name+"'");
		}
	}
	
	/**
	 * Busca los datos de las consultas de cierto tipo (ej. SELECT) que haya realizado un usuario en una base de datos concreta
	 * @param db_name - El nombre de la base de datos
	 * @param user - El nombre de usuario
	 * @param type - El tipo de consulta (en mayúsculas: INSERT, SELECT, UPDATE, DELETE, etc.)
	 */
	public void query2(String db_name, String user, String type) {
		boolean no_results = true;
		ArrayList<QueryData> info = listener.getInfo();
		for (QueryData data : info) {
			if (data.getDb_name().equals(db_name) && data.getUser().equals(user) && data.getType().equals(type)) {
				System.out.println("[INFO] "+data.dataFromQuery2());
				no_results = false;
			}
		}
		if (no_results) {
			System.out.println("[INFO] El usuario '"+user+"' no ha realizado consultas del tipo '"+type+"' en la base de datos '"+db_name+"'");
		}
	}
	
	/**
	 * Busca los datos de las consultas de cierto tipo (ej. SELECT) que se hayan realizado en una base de datos concreta
	 * @param db_name - El nombre de la base de datos
	 * @param type - El tipo de consulta (en mayúsculas: INSERT, SELECT, UPDATE, DELETE, etc.)
	 */
	public void query3(String db_name, String type) {
		boolean no_results = true;
		ArrayList<QueryData> info = listener.getInfo();
		for (QueryData data : info) {
			if (data.getDb_name().equals(db_name) && data.getType().equals(type)) {
				System.out.println("[INFO] "+data.dataFromQuery3());
				no_results = false;
			}
		}
		if (no_results) {
			System.out.println("[INFO] No se han realizado consultas del tipo '" + type + "' en la base de datos '"+db_name+"'");
		}
	}
	
	// Métodos privados (solo los utilizo en esta clase)
	/**
	 * Este método sirve para saber de que tipo es una consulta
	 * @param query - La consulta de la que queremos saber el tipo
	 * @return Devuelve el tipo de consulta (SELECT, INSERT, UPDATE, etc)
	 */
	private String getQueryType(String query) {
		String type = null;
		if (query.toUpperCase().contains("{CALL")) {
			type = "STORED PROCEDURE";
		}
		else if (query.toUpperCase().contains("SELECT")) {
			type = "SELECT";
		}
		else if (query.toUpperCase().contains("INSERT INTO")) {
			type = "INSERT";
		}
		else if (query.toUpperCase().contains("UPDATE")) {
			type = "UPDATE";
		}
		else if (query.toUpperCase().contains("DELETE FROM")) {
			type = "DELETE";
		}
		return type;
	}
	
	/**
	 * Este método sirve para saber cuantos registros devuelve un SELECT
	 * @param resultSet - El ResultSet del que se quieran saber los registros que devuelve
	 * @return Devuelve el número de filas de un ResultSet
	 */
	private int getRowCount(ResultSet resultSet) {
	    if (resultSet == null) {
	        return 0;
	    }
	    try {
	        if (resultSet.last()) {
	        	return resultSet.getRow();
	        }
	    } catch (SQLException exp) {
	        exp.printStackTrace();
	    } finally {
	        try {      	
	        	if (resultSet.last()) {
	        		resultSet.beforeFirst();
	        	}
	        } catch (SQLException exp) {
	            exp.printStackTrace();
	        }
	    }
	    return 0;
	}
	
	// Métodos componente
	public void addPropertyChangeListener(PropertyChangeListener l) {
		changes.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}
}
