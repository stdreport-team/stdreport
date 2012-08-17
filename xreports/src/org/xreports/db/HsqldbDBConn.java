package org.xreports.db;

import java.sql.Connection;
import java.sql.SQLException;

public class HsqldbDBConn extends GenericDBConn {
  public static final String DriverName = "org.hsqldb.jdbcDriver";
  public static final String URLSCHEMA_EMBEDDED = "jdbc:hsqldb:$DBNAME$";  
  public static final String URLSCHEMA_SERVER = "jdbc:hsqldb:hsql://$HOST$:$PORT$";  
  public static final String URLSCHEMA_INMEMORY = "jdbc:hsqldb:.";  
  public static final String URLSCHEMA_WEBSERVER = "jdbc:hsqldb:http://$HOST$:$PORT$";

  @Override
  public String getDriverName() {
    return DriverName;
  }

  @Override
  protected void setConnectionDB(Connection conn) throws SQLException {
    //non serve

  }

}
