package org.xreports.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlDBConn extends GenericDBConn {
  public static final String DriverName = "com.mysql.jdbc.Driver";
  public static final String URLSCHEMA  = "jdbc:mysql://$HOST$:$PORT$/$DBNAME$?user=$USER$&password=$PWD$&allowMultiQueries=true";  
  
  @Override
  public String getDriverName() {
    return DriverName;
  }

  @Override
  protected void setConnectionDB(Connection conn) throws SQLException {
    Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    stmt.execute("USE " + getDBName() + ";");
    stmt.close();
  }

}
