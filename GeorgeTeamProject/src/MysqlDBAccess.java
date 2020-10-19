import java.sql.*;

public class MysqlDBAccess {
	
	public static java.sql.Connection createConnection(String db_url, String user, String password) throws SQLException {		
		java.sql.Connection conn;
		conn = DriverManager.getConnection(db_url,user,password);		
		return conn;
	}
	
	
	public static void getResult(String sql, Connection conn) throws SQLException {
		Statement statement=conn.createStatement();
		ResultSet resultSet=statement.executeQuery(sql);
		displayResultSet(resultSet,'-',150);
		System.out.println();
		resultSet.close();
		statement.close();
	}
	
	public static void jdbShowRelatedTables(String tableName, Connection conn) throws SQLException {		
		
		String sql="select TABLE_NAME from INFORMATION_SCHEMA.COLUMNS where table_schema='adventureworks' and TABLE_NAME != ? and" +
		 " COLUMN_NAME in (	select column_name from information_schema.statistics where table_schema='adventureworks' and" +
		 " table_name = ?  and index_name = 'primary');";		
		
		PreparedStatement statement =conn.prepareStatement(sql);
		statement.setString(1, tableName );
		statement.setString(2, tableName);
		ResultSet resultSet=statement.executeQuery();
		displayResultSet(resultSet,'-',150);
		System.out.println();
		resultSet.close();
		statement.close();		
	}
	
	public static void jdbShowAllPrimaryKeys(Connection conn) throws SQLException {
		String sql="select TABLE_NAME, column_name from information_schema.statistics " +
				"where table_schema='adventureworks'  and index_name = 'primary' order by table_name;";
		Statement statement=conn.createStatement();
		ResultSet resultSet=statement.executeQuery(sql);
		displayResultSet(resultSet,'-',150);
		System.out.println();
		resultSet.close();
		statement.close();
		
	}
	
	public static void jdbFindColumn (String columnName, Connection conn) throws SQLException {
		
		String sql="select TABLE_NAME from information_schema.columns where column_name like ?";
		PreparedStatement statement =conn.prepareStatement(sql);
		statement.setString(1, columnName );		
		ResultSet resultSet=statement.executeQuery();
		displayResultSet(resultSet,'-',150);
		System.out.println();
		resultSet.close();
		statement.close();			
	}
	
	public static void jdbSearchPath (String fromTable, String toTable, Connection conn) throws SQLException {
		
	}
	
	public static void jdbSearchAndJoin(String fromTable, String toTable, Connection conn) throws SQLException {
		
	}
	
	public static void jdbGetView(String viewName, String query, Connection conn) throws SQLException {
		String sql="Create OR REPLACE view  "+viewName +" as " + query;
		Statement statement =conn.createStatement();
		statement.execute(sql);		
		statement.close();
	}
	
	public static void jdbStat(String tableViewName, String columnName, Connection conn) throws SQLException {
		
//		Statement statement =conn.createStatement();
//		String sql="select max(" + columnName + ") from " + tableViewName;
//		ResultSet resultSet=statement.executeQuery(sql);
//		resultSet.next();
//		int upperLimit= resultSet.getInt(1);
		int upperLimit=100;
		System.out.printf("%10s","0");
		for (int i = 0; i < 5; i++) {
			System.out.print(String.format("%10s", upperLimit*(i+1)/5).replace(' ', '_'));
		}
		System.out.println();
		for (int i = 0; i < 5; i++) {
			System.out.printf("%10s","|");			
			System.out.println();
		}
		
		System.out.println();
	}
	
	public static void jdbShowBestSalesperson(int num, Connection conn) throws SQLException {
		String sql="SELECT c.FirstName, c.LastName, bestEmployeeYTD.bestYTD " + 
				"from (select SalesPersonID, SalesYTD as bestYTD from salesperson order by SalesYTD desc limit ?) " + 
				"as bestEmployeeYTD inner join " + 
				"employee e on bestEmployeeYTD.SalesPersonID = e.EmployeeID inner join " + 
				"Contact c on e.ContactID = c.ContactID;" ;		
				
				PreparedStatement statement =conn.prepareStatement(sql);
				statement.setInt(1, num);
				
				ResultSet resultSet=statement.executeQuery();
				displayResultSet(resultSet,'-',150);
				System.out.println();
				resultSet.close();
				statement.close();		
	}
	
	public static void jdbShowReasonCount (Connection conn) throws SQLException {
		
		String sql="select sr.Name as reason, count(*) as orderCount  from " + 
				"salesorderheader sh inner join\r\n" + 
				"salesorderheadersalesreason shr using(SalesOrderID) inner join " + 
				"salesreason sr using(SalesReasonID) " + 
				"group by sr.Name " + 
				"order by count(*) desc;";
		Statement statement =conn.createStatement();				
		ResultSet resultSet=statement.executeQuery(sql);
		displayResultSet(resultSet,'-',150);
		System.out.println();
		resultSet.close();
		statement.close();			
	}
	
	public static void jdbShowSalesMonthly(int year, Connection conn) throws SQLException {
		String sql="select  month(OrderDate) as month, sum(SubTotal) as sales  from " + 
				"salesorderheader " + 
				"where year(OrderDate)= ? " + 
				"group by year(OrderDate) ,month(OrderDate) " + 
				"order by month(OrderDate)";		
				
				PreparedStatement statement =conn.prepareStatement(sql);
				statement.setInt(1, year);
				
				ResultSet resultSet=statement.executeQuery();
				displayResultSet(resultSet,'-',150);
				System.out.println();
				resultSet.close();
				statement.close();		
	}
	
	private static void displayResultSet(ResultSet resultSet, char symbol, int width) throws SQLException 
	{
	    ResultSetMetaData rsmd = resultSet.getMetaData();
	    int columnsNumber = rsmd.getColumnCount();	    

	    for(int i = 1; i <= columnsNumber; i++)
	    {
	    	System.out.printf("| %-20.20s",rsmd.getColumnLabel(i));
	    }	    	
	    System.out.println();
	    for(int i = 0; i < width; ++i)
	        System.out.printf("%c", symbol);
	    
	    System.out.println();
	    while (resultSet.next()) {
			// Print one row
	    	
			for (int i = 1; i <= columnsNumber; i++) {
				System.out.printf("| %-20.20s",resultSet.getString(i));				
			}
			
			System.out.println();// Move to the next line to print the next row.
		}
	}
	
	
	public static void  closeConnection(Connection conn) throws SQLException {		
		if(conn!=null)
        conn.close();	
	}
}
