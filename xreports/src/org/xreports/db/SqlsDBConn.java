package org.xreports.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class SqlsDBConn extends GenericDBConn {

  
  @Override
  protected void setConnectionDB(Connection conn) throws SQLException {
    Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    stmt.execute("USE " + getDBName() + ";");
    stmt.close();
  }
  
}
