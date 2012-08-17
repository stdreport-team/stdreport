package org.xreports.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class IfxDBConn extends GenericDBConn {
  public static final String DriverName = "com.informix.jdbc.IfxDriver";
  public static final String URLSCHEMA = "jdbc:informix-sqli://$HOST$:$PORT$:INFORMIXSERVER=$DBSERVER$;";  //user=$USER$;password=$PWD$


  @Override
  public String getDriverName() {
    return DriverName;
  }
  
  
  @Override
  protected void setConnectionDB(Connection conn) throws SQLException {
    Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    stmt.execute("database " + getDBName() + ";");
    stmt.close();
  }

}
