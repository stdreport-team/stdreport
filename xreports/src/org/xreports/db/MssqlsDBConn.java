package org.xreports.db;

public class MssqlsDBConn extends SqlsDBConn {
  public static final String DriverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
  public static final String URLSCHEMA  = "jdbc:sqlserver://$HOST$:$PORT$;DatabaseName=$DBNAME$";

  @Override
  public String getDriverName() {
    return DriverName;
  }

}
