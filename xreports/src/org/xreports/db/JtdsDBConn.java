package org.xreports.db;

public class JtdsDBConn extends SqlsDBConn {
  public static final String DriverName = "net.sourceforge.jtds.jdbc.Driver";
  public static final String URLSCHEMA = "jdbc:jtds:sqlserver://$HOST$:$PORT$/$DBNAME$;AppName=$APP$;ProgName=$PROG$";


  @Override
  public String getDriverName() {
    return DriverName;
  }

}
