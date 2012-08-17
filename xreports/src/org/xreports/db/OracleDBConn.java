package org.xreports.db;

import java.sql.Connection;
import java.sql.SQLException;

public class OracleDBConn extends GenericDBConn {
  public static final String DriverName = "oracle.jdbc.OracleDriver";
  public static final String URLSCHEMA  = "jdbc:oracle:thin:@$HOST$:$PORT$:$DBNAME$";

  @Override
  public String getDriverName() {
    return DriverName;
  }

  @Override
  protected void setConnectionDB(Connection conn) throws SQLException {
    //e' gia' nel jdbc url

  }

}
