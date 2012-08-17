package org.xreports.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xreports.util.Text;

public abstract class GenericDBConn {
	private String																							c_dbName;
	private String																							c_user;
	private String																							c_passwd;
	private String																							c_jdbcUrl;
	private Connection																					c_dbConn;

	private static Map<String, Class<? extends GenericDBConn>>	c_availDrivers;

	static {
		c_availDrivers = new HashMap<String, Class<? extends GenericDBConn>>();
		c_availDrivers.put(JtdsDBConn.DriverName, JtdsDBConn.class);
		c_availDrivers.put(MssqlsDBConn.DriverName, MssqlsDBConn.class);
		c_availDrivers.put(OracleDBConn.DriverName, OracleDBConn.class);
		c_availDrivers.put(IfxDBConn.DriverName, IfxDBConn.class);
		c_availDrivers.put(MysqlDBConn.DriverName, MysqlDBConn.class);
		c_availDrivers.put(HsqldbDBConn.DriverName, HsqldbDBConn.class);
		c_availDrivers.put(PostgresqlDBConn.DriverName, PostgresqlDBConn.class);
	}

	public abstract String getDriverName();

	protected abstract void setConnectionDB(Connection conn) throws SQLException;

	public static GenericDBConn getInstance(String driverClass)
			throws InstantiationException, IllegalAccessException {
		Class<? extends GenericDBConn> driver = c_availDrivers.get(driverClass);
		if (driver != null)
			return driver.newInstance();

		throw new IllegalArgumentException("Driver " + driverClass
				+ " not available");
	}

	public static void registerDriver(String driverClass,
			Class<? extends GenericDBConn> connector) {
		if (driverClass != null && connector != null)
			c_availDrivers.put(driverClass, connector);

		throw new NullPointerException("driver or connector is null");
	}

	public void open() throws SQLException, ClassNotFoundException {
		c_dbConn = connect(getDBName(), getUser(), getPasswd());
	}

	public void close() throws SQLException {
		if (c_dbConn == null)
			return;
		if (!c_dbConn.isClosed()) {
			c_dbConn.close();
			c_dbConn = null;
		}

	}

	private Connection connect(String dbName, String name, String pwd)
			throws SQLException, ClassNotFoundException {
		Connection conn = null;
		@SuppressWarnings("unused")
		Class<?> driver = Class.forName(getDriverName());
		if (name != null)
			conn = DriverManager.getConnection(getJdbcUrl(), name, pwd);
		else
			conn = DriverManager.getConnection(getJdbcUrl());
		try {
			setConnectionDB(conn);
		} catch (SQLException e) {
			// se va male la setconnectiondb, chiudo la connessione!
			if (conn != null) {
				conn.close();
			}
			throw e;
		}
		return conn;
	}

	/**
	 * Setta il nome del database per la connessione corrente.
	 * 
	 * @param dbname
	 *          il nome del DB
	 */
	public void setDBName(String dbname) {
		c_dbName = (null != dbname) ? dbname.trim() : dbname;
	}

	/**
	 * Restituisce il nome del database.
	 * 
	 * @return String il nome del DB
	 */
	public String getDBName() {
		return c_dbName;
	}

	public String getUser() {
		return c_user;
	}

	public void setUser(String user) {
		c_user = user;
	}

	public String getPasswd() {
		return c_passwd;
	}

	public void setPasswd(String passwd) {
		c_passwd = passwd;
	}

	public String getJdbcUrl() {
		return c_jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		c_jdbcUrl = jdbcUrl;
	}

	public List<HashMap<String, Object>> getRows(String sql, int maxRecords)
			throws SQLException {
		if (!Text.isValue(sql)) {
			throw new IllegalArgumentException("The query text is null or empty!");
		}
		ResultSet rs = null;
		List<HashMap<String, Object>> rows = new LinkedList<HashMap<String, Object>>();
		try {
			rs = createRecordSet(c_dbConn, sql);
			int nCols = rs.getMetaData().getColumnCount();
			int count = 0;
			while (rs.next()) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				for (int i = 0; i < nCols; i++) {
					//forced lower case on column names
					map.put(rs.getMetaData().getColumnName(i + 1).toLowerCase(),
							rs.getObject(i + 1));
				}
				rows.add(map);
				count++;
				if (maxRecords > 0 && count >= maxRecords) {
					break;
				}
			}
			return rows;
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
				// ignoro
			}
		}
	}

	private ResultSet createRecordSet(Connection conn, String query) throws SQLException {
		if ((query == null) || (query.length() == 0)) {
			return null;
		}
		PreparedStatement ps = conn.prepareStatement(query,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = ps.executeQuery();
		return rs;
	}

}
