package org.xreports.db;

import java.sql.Connection;
import java.sql.SQLException;

public class PostgresqlDBConn extends GenericDBConn {
  public static final String DriverName = "org.postgresql.Driver";
  public static final String URLSCHEMA  = "jdbc:postgresql://$host$:$port$/$database$";

  @Override
  public String getDriverName() {
    return DriverName;
  }

  @Override
  protected void setConnectionDB(Connection conn) throws SQLException {
    //non serve
  }

}
