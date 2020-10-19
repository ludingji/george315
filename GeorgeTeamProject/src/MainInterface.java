/*
Phase 1 JDBC Interfacing, Group 5

Written by Noah Miner, Allen Yang,
Evelyn Tang, Julie Herrick, George Lan
*/

import java.sql.*;
import java.util.Scanner;
import java.util.regex.*;
import java.util.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.nio.*;
import org.jgrapht.nio.dot.*;
import org.jgrapht.traverse.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.*;

import javax.imageio.ImageIO;
import org.jgrapht.ext.JGraphXAdapter;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;



public class MainInterface {



  static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
  static final String DB_URL = "jdbc:mysql://localhost:3306/?serverTimezone=UTC#/";

  // Database credentials
  // going to be static for now, maybe we can implement a login later
  static final String USER = "root";
  static final String PASS = "password";


  static Map<String,ArrayList<String>> adj_list_by_column = new HashMap<String,ArrayList<String>>();
  static Map<String,ArrayList<String>> adj_list_by_table = new HashMap<String,ArrayList<String>>();

  static ArrayList<String> table_name = new ArrayList<String>();
  // static Graph<String, DefaultWeightedEdge> table_matrix =
  //   new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);

  static Graph<String, ColumnEdge> table_matrix =
  new SimpleWeightedGraph<String, ColumnEdge>(ColumnEdge.class);

  static Double ind = 0.0;
  static ArrayList<String> edgeName = new ArrayList<String>(); // weight == index
  static Map<String,String> view_def_map = new HashMap<String,String>();
  static ArrayList<ArrayList<String>> GUI_display_table = new ArrayList<ArrayList<String>>();
  // main loop to log into and interact with database
  public static void main(String[] args) {
    Connection conn = null;
    Statement stmt = null;
    try {
      // Connection stuff
      Class.forName("com.mysql.cj.jdbc.Driver");

      System.out.println("Connecting to database...");
      conn = DriverManager.getConnection(DB_URL,USER,PASS);
      stmt = conn.createStatement();

      ResultSet rs;
      rs = stmt.executeQuery("USE adventureworks;");

      database_meta(conn,stmt,adj_list_by_column,adj_list_by_table,table_matrix,table_name);

      graphPNG(table_matrix);

      // test
      // print_shortest_path(table_matrix,"employee","vendorcontact");
      // get_view_for_user(conn,stmt,"howdy1","select employee.EmployeeID,purchaseorderheader.TotalDue from employee INNER JOIN purchaseorderheader ON (employee.EmployeeID=purchaseorderheader.EmployeeID) INNER JOIN vendorcontact ON (purchaseorderheader.VendorID=vendorcontact.VendorID) where TotalDue < 581",view_def_map);

      // print_join_table(conn,stmt,table_matrix,"employee","vendorcontact",edgeName);


      Pattern spaceSplit = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'"); // splits by space, but not inside quotes

      // for basic SQL commands and calling custom functions
      Scanner sc = new Scanner(System.in);
      boolean loop = true;
      while (loop) {
        try{
          System.out.print("jdb> ");
          String command = sc.nextLine();
          if (!validateCommand(command)) {
            System.out.println("Cannot edit database.");
            continue;
          }

          Matcher m = spaceSplit.matcher(command);

          List<String> matches = new ArrayList<String>();
          String query;
          while (m.find()) {
            if (m.group(1) != null) {
              matches.add(m.group(1)); // content inside double quotes
            } else if (m.group(2) != null) {
              matches.add(m.group(2)); // content inside single quotes
            } else {
              matches.add(m.group()); // unquoted
            }
          }
          String parsed_command[] = matches.toArray(new String[matches.size()]);

          switch (parsed_command[0]) {
            case "q;":
            case "q":{
              loop = false;
              break;
            }

            case "quit;":
            case "quit":{
              loop = false;
              break;
            }


            case "jdb-show-related-tables":{
              if (parsed_command.length != 2) {
                System.out.println("Incorrect amount of arguments. Re-enter");
                break;
              }
              String parT = parsed_command[1].replace(";","").trim().toLowerCase();
              System.out.println("**** A list of table that is connected to " + parT + "****");
              System.out.println(Graphs.neighborListOf(table_matrix, parT));
            }
              break;

              case "jdb-show-all-primary-keys":
              case "jdb-show-all-primary-keys;":
              {
                System.out.println("****************here in debugging");
                // Show all primary keys from all tables. Print the list of (table_name, column_name).
                if (parsed_command.length != 1) {
                  System.out.println("Incorrect amount of arguments.Re-enter");
                  break;
                }
                String sql = "select TABLE_NAME,COLUMN_NAME FROM  INFORMATION_SCHEMA.COLUMNS where COLUMN_KEY='PRI' AND TABLE_SCHEMA = 'adventureworks'";
                System.out.println("sql:"+sql);
                conn = DriverManager.getConnection(DB_URL,USER,PASS);
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet rs2 = statement.executeQuery();
                displayResultSet(rs2,'-',150);
              }
              break;


              case "jdb-find-column":{
                if (parsed_command.length != 2) {
                  System.out.println("Re-enter");
                  break;
                }
                // re-write
                String parC = parsed_command[1].replace(";","").trim();
                ArrayList<String> arrfindcol = adj_list_by_column.get(parC);
                System.out.println("-------- tables that connect to the column ----------");
                for(int j = 0; j < arrfindcol.size();j++){
                  System.out.println(arrfindcol.get(j));
                }
                System.out.println("-----------------------------------------------------");
              }
              break;



              case "jdb-search-path":
              System.out.println("Search path");
              command = command.trim();
              command = command.replace(";","");
              String tbarr1[] = command.split(" ");
              if(tbarr1.length!= 3 || !tbarr1[0].equals("jdb-search-path")){
                System.out.println("Something wrong in syntax. try again");
                break;
              } else {
                String tb11 = tbarr1[1].trim().toLowerCase();
                String tb12 = tbarr1[2].trim().toLowerCase();
                // if table not in graph
                if(!table_name.contains(tb11) || !table_name.contains(tb12)){
                  System.out.println("one of the table not in schema, enter table in schema");
                  break;
                } else{
                  print_shortest_path(table_matrix,tb11,tb12);
                }
              }

              break;
              case "jdb-search-and-join":
              command = command.trim();
              System.out.println("Search and join");
              command = command.replace(";","");
              String tbarr[] = command.split(" ");
              if(tbarr.length!= 3 || !tbarr[0].equals("jdb-search-and-join")){
                System.out.println("Something wrong in syntax. try again");
                break;
              } else {
                String tb1 = tbarr[1].trim().toLowerCase();
                String tb2 = tbarr[2].trim().toLowerCase();
                // if table not in graph
                if(!table_name.contains(tb1) || !table_name.contains(tb2)){
                  System.out.println("one of the table not in schema, enter table in schema");
                  break;
                } else{
                  print_join_table(conn,stmt,table_matrix,tb1,tb2,edgeName);
                  // for(int p=0;p<table_name.size();p++){
                  //   System.out.println("table_name:"+table_name.get(p));
                  // }
                }
              }
              break;
              case "jdb-get-view":
              command = command.trim();
              System.out.println("Get view");
              // System.out.println("command:"+command);
              String query11 = "";
              if(command.contains("(") && command.contains(")")){
                int indl = command.indexOf("(");
                int indr = command.indexOf(")");
                if(indl == indr -1){
                  System.out.println("empty, something wrong");
                  break;
                }
                query11 = command.substring(indl+1,indr);
                // System.out.println("query11:"+query11);
              }

              // make all view name keys lowercase
              String viewName1 = command.split(" ")[1].toLowerCase();
              get_view_for_user(conn,stmt,viewName1,query11,view_def_map);

              break;
              case "jdb-show-best-salesperson": {
                int num =Integer.parseInt( removeSemicolon( parsed_command[1].trim())) ;
                jdbShowBestSalesperson(num, conn);
                break;
              }
              case "jdb-show-reason-count": {
                jdbShowReasonCount(conn);
                break;
              }
              case "jdb-show-sales-monthly": {
                int year =Integer.parseInt( removeSemicolon( parsed_command[1].trim())) ;
                jdbShowSalesMonthly (year, conn);
                break;
              }
              case "jdb-stat": {
                if (parsed_command.length != 3) {
                  System.out.println("Incorrect amount of arguments");
                  break;
                }

                // get index of the column chosen in command
                rs = stmt.executeQuery("SELECT column_name FROM information_schema.columns where table_name='" + parsed_command[1] + "'");
                int index_of_column = 0;
                boolean name_found = false;
                while (rs.next()) {
                  String colName = rs.getString(1);
                  if (colName.equalsIgnoreCase(parsed_command[2])) {
                    name_found = true;
                    break;
                  }
                  index_of_column++;
                }
                if (!name_found) {
                  System.out.println("Table or Column name not found.");
                  break;
                }

                // make sure data type is numeric
                rs = stmt.executeQuery("SELECT data_type FROM information_schema.columns where table_name='" + parsed_command[1] + "'");
                rs.next();
                for (int i = 0; i < index_of_column; i++) {
                  rs.next();
                }
                String datatype = rs.getString(1);
                if (!(datatype.contains("int") || datatype.contains("dec") || datatype.contains("num") || datatype.contains("float") || datatype.contains("double"))) {
                  System.out.println("Column must contain a numeric data type.");
                  break;
                }

                // now that query is valid, generate a list of the values in that column
                rs = stmt.executeQuery("SELECT " + parsed_command[2] + " FROM " + parsed_command[1]);
                List<Double> list = new ArrayList<Double>();
                while (rs.next()) {
                  list.add(rs.getDouble(1));
                }

                // going through list and getting stats
                double min = list.get(0);
                double max = list.get(0);
                double sum = 0;
                double median = 0;
                if (list.size()%2 == 0)
                median = (list.get(list.size()/2) + list.get(list.size()/2 - 1))/2;
                else
                median = list.get(list.size()/2);

                for (int i = 0; i < list.size(); i++) {
                  if (list.get(i) < min)
                  min = list.get(i);
                  if (list.get(i) > max)
                  max = list.get(i);
                  sum += list.get(i);
                }
                double avg = sum/list.size();

                System.out.format("Min value: %.2f\n", min);
                System.out.format("Max value: %.2f\n", max);
                System.out.format("Average: %.2f\n", avg);
                System.out.format("Median: %.2f\n", median);

                // plotting histogram
                // first get number of bins and bin width
                int num_bins = (int)Math.ceil(Math.sqrt(list.size()));
                double bin_width = (max - min)/num_bins;
                int[] bins = new int[num_bins];

                // bins[i] = (min + i*bin_width) - (min + (i+1)*bin_width)
                // populating each bin and scaling them properly
                int max_count = 0;
                for (int index = 0; index < list.size(); index++) { // populating each bin with quantity in that bin
                  int i = (int)(Math.floor((list.get(index) - min)/bin_width));
                  i = Math.min(i, bins.length - 1);
                  bins[i]++;
                  if (bins[i] > max_count)
                  max_count = bins[i];
                }
                int y_scale = 1;
                while (max_count/y_scale > 50) // get scale for y axis
                y_scale *= 10;
                for (int i = 0; i < bins.length; i++) { // divide each quantity in bins by y_scale
                  bins[i] = (int)Math.round(bins[i]/(double)y_scale);
                }

                // displaying bins
                // printing y-axis
                int offset = String.format("%.2f", max).length() * 2 + 3; // how long the header for each bin should be when displaying
                System.out.println();
                System.out.format("%-" + (offset + 1) + "s", "");
                int max_count_scaled = max_count/y_scale;
                for (int count = 0; count <= max_count_scaled; count++) // printing y-axis labels
                System.out.print(count + "___");
                System.out.format("(Each star and each y-axis label represents %d counts)\n", y_scale);

                // printing each bin
                for (int i = 0; i < bins.length; i++) {
                  String range = String.format("%.2f - %.2f", min + i*bin_width, min + (i+1)*bin_width);
                  System.out.format("%-" + offset + "s |", range);
                  for (int j = 0; j < bins[i]; j++) {
                    System.out.print("*");
                  }
                  System.out.println();
                }

                break;
              }
              case "jdb-customer-info": // jdb-customer-info "conditions" groupby? columnName // displays individual customers line by line that match "conditions", can group by column name, e.g. how many customers in this state
              createTempAggregateSalesTables(stmt);
              query = "SELECT ";
              boolean counting = (parsed_command.length == 3 && parsed_command[1].equalsIgnoreCase("groupby"))
              || parsed_command.length == 4 && (parsed_command[2].equalsIgnoreCase("groupby"));

              if (counting) {
                query += parsed_command[parsed_command.length - 1] + ",Count(DISTINCT(customer.CustomerID)) ";
              } else {
                query += "AccountNumber AS 'Account Number', CustomerType AS 'Customer Type', "
                + "customeraggregate.NumSales AS 'Total Number of Orders', customeraggregate.NumProducts AS 'Number of distinct products', "
                + "customeraggregate.TotalProducts AS 'Total number of products', subtotals.TotalSpent AS 'Total amount spent', "
                + "AddressLine1 AS 'Address Line 1', AddressLine2 AS 'Address Line 2', City, stateprovince.Name AS State, countryregion.Name AS Country, PostalCode AS 'Postal Code' ";
              }

              // Getting location info from customers
              query += "FROM customeraggregate "
              + "INNER JOIN subtotals ON (customeraggregate.CustomerID=subtotals.CustomerID) "
              + "INNER JOIN customer ON (customeraggregate.CustomerID=customer.customerID) "
              + "INNER JOIN customeraddress ON (customer.CustomerID=customeraddress.CustomerID) "
              + "INNER JOIN address ON (customeraddress.AddressID=address.AddressID) "
              + "INNER JOIN stateprovince ON (address.StateProvinceID=stateprovince.StateProvinceID) "
              + "INNER JOIN countryregion ON (stateprovince.CountryRegionCode=countryregion.CountryRegionCode) ";

              if (parsed_command.length > 1 && !parsed_command[1].equalsIgnoreCase("groupby")) {
                query += "WHERE " + parsed_command[1] + " ";
              }

              if (counting) {
                query += "GROUP BY " + parsed_command[parsed_command.length - 1] + " ";
              }
              //query += "LIMIT 1000";

              rs = stmt.executeQuery(query);
              printResults(rs);
              break;
              case "jdb-customer-orders": //jdb-customer-orders <condition> <aggregate-by-sales|aggregate>
              query = "";
              String where_expr = "";
              String group_expr = "";
              String from_expr = "";
              if (parsed_command.length > 1 && !parsed_command[1].toLowerCase().contains("aggregate")) {
                where_expr = "WHERE " + parsed_command[1] + " ";
              }
              if ((parsed_command.length == 2 && parsed_command[1].equalsIgnoreCase("aggregate")) ||
              (parsed_command.length == 3 && parsed_command[2].equalsIgnoreCase("aggregate"))) {
                createTempAggregateSalesTables(stmt); // tables with aggregate sales data for a customer
                query = "SELECT customer.AccountNumber AS 'Account Number', "
                + "customer.CustomerType AS 'Customer Type', "
                + "customeraggregate.NumSales AS 'Total number of sales',"
                + "customeraggregate.NumProducts AS 'Distinct number of items bought',"
                + "customeraggregate.TotalProducts AS 'Total number of items bought',"
                + "subtotals.TotalSpent AS 'Total amount spent' ";
                from_expr = "FROM customer "
                + "INNER JOIN subtotals ON (customer.CustomerID=subtotals.CustomerID) "
                + "INNER JOIN customeraggregate ON (customer.CustomerID=customeraggregate.CustomerID) ";
              }
              else if ((parsed_command.length == 2 && parsed_command[1].equalsIgnoreCase("aggregate-by-sales")) ||
              (parsed_command.length == 3 && parsed_command[2].equalsIgnoreCase("aggregate-by-sales"))) {
                createTempAggregateSalesTables(stmt); // tables with aggregate sales data for a customer
                query = "SELECT customer.AccountNumber AS 'Account Number', "
                + "customer.CustomerType AS 'Customer Type', "
                + "salesorderheader.SalesOrderID AS 'Sales Order ID', "
                + "salesaggregate.NumDistinctProducts AS 'Distinct number of items bought', "
                + "salesaggregate.TotalProductCount AS 'Total number of items bought', "
                + "salesorderheader.SubTotal AS 'Total amount spent' ";
                from_expr = "FROM customer INNER JOIN salesorderheader ON (customer.CustomerID=salesorderheader.CustomerID) "
                + "INNER JOIN salesaggregate ON (salesorderheader.SalesOrderID=salesaggregate.SalesOrderID) ";
              }
              else {
                query = "SELECT customer.AccountNumber AS 'Account Number', "
                + "customer.CustomerType AS 'CustomerType', "
                + "salesorderheader.SalesOrderID AS 'Sales Order ID', "
                + "product.Name AS 'Product Name', "
                + "product.ListPrice AS 'List Price', "
                + "salesorderdetail.OrderQty AS 'Amount ordered' ";
                from_expr = "FROM customer INNER JOIN salesorderheader ON (customer.CustomerID=salesorderheader.CustomerID) "
                + "INNER JOIN salesorderdetail ON (salesorderheader.SalesOrderID=salesorderdetail.SalesOrderID) "
                + "INNER JOIN product ON (salesorderdetail.ProductID=product.ProductID) ";
              }


              query += from_expr + where_expr + group_expr + "";

              System.out.println(query);
              rs = stmt.executeQuery(query);
              printResults(rs);
              break;

              case "CREATE":
              // create view satement
              if(command.contains("CREATE VIEW") && command.contains("AS")){
                System.out.println(command);
                String cv1[] = command.split(" AS ");
                String viewName = cv1[0].replace("CREATE VIEW","").trim().toLowerCase();
                System.out.println("v:"+viewName);
                String qy = cv1[1].replace("(","").replace(")","").replace(";","").trim();
                System.out.println("qy:"+qy);

                if(qy.equals("")){
                  System.out.println("empty view def, try again");
                  break;
                }
                if(view_def_map.containsKey(viewName)){
                  System.out.println("THE view "+viewName+" has already existed. use replace view command");
                  break;
                }
                // call create view function
                else {
                  create_or_update_view(viewName,qy,0);
                  for(String ttt: view_def_map.keySet()){
                    System.out.println(ttt + "-> " + view_def_map.get(ttt));
                  }
                }
              }
              // missing as keyword
              else if (command.contains("CREATE VIEW") && (!command.contains("AS"))){
                System.out.println("something wrong, missing AS keyword");
              }
              // other create mysql statement
              else {

              }
              break;

              case "REPLACE":
              System.out.println(command);
              if(command.contains("REPLACE VIEW") && command.contains("AS")){
                System.out.println(command);
                String cv1[] = command.split(" AS ");
                String viewName = cv1[0].replace("REPLACE VIEW","").trim().toLowerCase();
                System.out.println("v:"+viewName);
                String qy = cv1[1].replace("(","").replace(")","").replace(";","").trim();
                System.out.println("qy:"+qy);

                // empty query, break
                if(qy.equals("")){
                  System.out.println("empty view def, try again");
                  break;
                }
                // call create_update view
                if(view_def_map.containsKey(viewName)){
                  create_or_update_view(viewName,qy,1);
                  for(String ttt: view_def_map.keySet()){
                    System.out.println(ttt + "-> " + view_def_map.get(ttt));
                  }
                }
                else{
                  System.out.println("the view is not created yet, cannot replace.");
                }

              }
              // error
              else {
                System.out.println("Something wrong, try again");
              }
              break;

              case "DROP":
              if (command.contains("DROP VIEW")) {
                String viewName = command.replace("DROP VIEW","").replace(";","").trim().toLowerCase();
                System.out.println("table in drop:" + viewName);
                // remove from map
                if(view_def_map.containsKey(viewName)){
                  view_def_map.remove(viewName);
                  System.out.println(viewName + " removed");
                  for(String ttt: view_def_map.keySet()){
                    System.out.println(ttt + "-> " + view_def_map.get(ttt));
                  }
                }
                else{
                  System.out.println("the view is not created yet, cannot drop.");
                }

              }
              // other mysql drop commands
              else{ }
              break;

              default: // basic sql commands

              rs = stmt.executeQuery(command);
              printResults(rs);
              break;
            }// end switch
          }// end try
          catch(SQLException e0){
            System.out.println("*******************************************");
            System.out.println("YOU GOT A SQL EXCPETION!!!!! SEE BELOW");
            System.out.println("*******************************************");
            System.out.println("*******************************************");
            e0.printStackTrace();
            System.out.println("\n\n");
          } catch(Exception e1){
            System.out.println("*******************************************");
            System.out.println("YOU GOT A normal EXCPETION!!!!! SEE BELOW");
            System.out.println("*******************************************");
            System.out.println("*******************************************");
            e1.printStackTrace();
            System.out.println("\n\n");
          }

        }// end while

        // Closing scanner
        sc.close();
      } catch (SQLException se) {
        //Handle errors for JDBC
        se.printStackTrace();
      } catch (Exception e) {
        //Handle errors for Class.forName
        e.printStackTrace();
      } finally {
        //finally block used to close resources
        try {
          if (stmt!=null)
          stmt.close();
        } catch (SQLException se2) {}

          try {
            if (conn!=null)
            conn.close();
          } catch (SQLException se) {
            se.printStackTrace();
          }
        }
      }

      public static boolean validateCommand(String command) {
        command = command.toUpperCase();
        return !(command.contains("CREATE") || command.contains("DROP") ||
        command.contains("ALTER") || command.contains("DELETE") ||
        command.contains("INSERT")) || command.contains("CREATE VIEW")||
        command.contains("UPDATE") ||
        command.contains("REPLACE VIEW") || command.contains("DROP VIEW");
      }

      public static void printResults(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();

        // Printing column names
        for (int i = 1; i <= cols; i++) {
          if (i > 1)
          System.out.print(", ");
          System.out.print(rsmd.getColumnLabel(i));
        }
        System.out.println("");

        // Printing query contents
        while (rs.next()) {
          for (int i = 1; i <= cols; i++) {
            if (i > 1)
            System.out.print(", ");
            String colVal = rs.getString(i);
            if (colVal != null && colVal.contains(","))
            System.out.print('"' + colVal + '"');
            else
            System.out.print(colVal);
          }
          System.out.println("");
        }
      }

      public static void createTempAggregateSalesTables(Statement stmt) throws SQLException {
        stmt.executeUpdate("CREATE TEMPORARY TABLE IF NOT EXISTS subtotals "
        + "SELECT salesorderheader.CustomerID, SUM(salesorderheader.SubTotal) AS TotalSpent "
        + "FROM salesorderheader "
        + "GROUP BY salesorderheader.CustomerID");
        stmt.executeUpdate("CREATE TEMPORARY TABLE IF NOT EXISTS customeraggregate "
        + "SELECT salesorderheader.CustomerID,"
        + "COUNT(DISTINCT(salesorderheader.SalesOrderID)) AS NumSales, "
        + "COUNT(DISTINCT(salesorderdetail.ProductID)) AS NumProducts,"
        + "SUM(salesorderdetail.OrderQty) AS TotalProducts "
        + "FROM salesorderheader INNER JOIN salesorderdetail ON (salesorderheader.SalesOrderID=salesorderdetail.SalesOrderID) "
        + "GROUP BY salesorderheader.CustomerID"); // a list of customer IDs and their aggregate sales info
        stmt.executeUpdate("CREATE TEMPORARY TABLE IF NOT EXISTS salesaggregate "
        + "SELECT salesorderheader.SalesOrderID, "
        + "COUNT(DISTINCT(salesorderdetail.ProductID)) AS NumDistinctProducts, "
        + "SUM(salesorderdetail.OrderQty) AS TotalProductCount "
        + "FROM salesorderheader INNER JOIN salesorderdetail ON (salesorderheader.SalesOrderID=salesorderdetail.SalesOrderID) "
        + "GROUP BY salesorderheader.SalesOrderID");
      }

      // // helper function build a map
      public static void database_meta(Connection conn, Statement stmt,Map<String,ArrayList<String>> adj_list_by_column, Map<String,ArrayList<String>> adj_list_by_table,Graph<String, ColumnEdge> table_matrix,ArrayList<String>table_name){
        // public static void database_meta(Connection conn, Statement stmt,Map<String,ArrayList<String>> adj_list_by_column, Map<String,ArrayList<String>> adj_list_by_table,Graph<String, DefaultWeightedEdge> table_matrix,ArrayList<String>table_name){
        try{

          ArrayList<ArrayList<String>> tbl_col = new ArrayList<ArrayList<String>>();

          String tablename;

          DatabaseMetaData metaData = conn.getMetaData();
          String[] types = {"TABLE"};
          ResultSet tables = metaData.getTables(null, null, "%", types);

          // O(n)
          while (tables.next()) {
            tablename = tables.getString("TABLE_NAME");
            // System.out.println("table:"+tablename);
            table_name.add(tablename);
            table_matrix.addVertex(tablename); // add table node to graph
          }

          // checking
          // for(int i = 0; i < table_name.size();i++){
          //   System.out.println(table_name.get(i));
          // }

          // System.out.println("***************************************");
          // System.out.println("***************************************");

          String query1 = "select table_name,column_name from information_schema.columns where table_schema = 'adventureworks' order by table_name,column_name";

          stmt = conn.createStatement();
          ResultSet rs1;
          ResultSetMetaData rsmd1;
          rs1 = stmt.executeQuery(query1);
          rsmd1 = rs1.getMetaData();
          while(rs1.next()){
            // System.out.println("---------");
            ArrayList<String> temp1 = new ArrayList<String>();
            for(int j=1;j <= rsmd1.getColumnCount();j++){
              temp1.add(rs1.getString(j));
            }
            tbl_col.add(temp1);
          }

          // // prinout to check built table
          // for(int i = 0; i < tbl_col.size();i++){
          //   for(int k=0;k < tbl_col.get(i).size();k++){
          //     System.out.print(tbl_col.get(i).get(k) + " ");
          //   }
          //   System.out.println();
          // }

          // building adj list
          for(int i=0;i<tbl_col.size();i++){
            // ith item's 2nd column
            String colstr = tbl_col.get(i).get(1);
            String curr_tbl = tbl_col.get(i).get(0);
            // not being added
            if(adj_list_by_column.get(colstr)==null){
              // ith item's 1st column, make a new array
              ArrayList<String> temp2 = new ArrayList<String>();
              temp2.add(curr_tbl);
              adj_list_by_column.put(colstr,temp2);
              temp2 = null;
            } else {
              // System.out.println(adj_list.get(colstr).getClass
              // System.out.println(curr_tbl);
              ArrayList<String> temp2 = adj_list_by_column.get(colstr);
              temp2.add(curr_tbl); // append
              adj_list_by_column.replace(colstr,temp2); // update
              // System.out.println(colstr + temp2.size());
            }
          }

          // print for checking
          // for(String k: adj_list_by_column.keySet()){
          //   System.out.println("******************* "+ k +" ******************");
          //   for(int j=0;j<adj_list_by_column.get(k).size();j++){
          //     System.out.println(adj_list_by_column.get(k).get(j));
          //   }
          // }

          // building map for table: adj_list_by_table
          for(int i=0;i<tbl_col.size();i++){
            // ith item's 2nd column
            String colstr = tbl_col.get(i).get(1);
            String curr_tbl = tbl_col.get(i).get(0);

            if(!colstr.endsWith("ID")){
              // System.out.println("Does not contain");
              continue;
            }

            // not being added
            if(adj_list_by_table.get(curr_tbl)==null){
              // ith item's 1st column, make a new array
              ArrayList<String> temp2 = new ArrayList<String>();
              temp2.add(colstr);
              adj_list_by_table.put(curr_tbl,temp2);
              temp2 = null;
            } else {
              // System.out.println(adj_list.get(colstr).getClass
              // System.out.println(curr_tbl);
              ArrayList<String> temp2 = adj_list_by_table.get(curr_tbl);
              temp2.add(colstr); // append
              adj_list_by_table.replace(curr_tbl,temp2); // update
              // System.out.println(curr_tbl +temp2.size());
              temp2 = null;
            }
          }

          // System.out.println("-------------------------------------------------------------");
          // System.out.println("-----------------------------then table--------------------------------");
          //
          // for(String k: adj_list_by_table.keySet()){
          //   System.out.println("******************* "+ k +" ******************");
          //   for(int j=0;j<adj_list_by_table.get(k).size();j++){
          //     System.out.println(adj_list_by_table.get(k).get(j));
          //   }
          // }

          for(String k: adj_list_by_column.keySet()){
            // currently , id only
            if(!k.contains("ID")){
              continue;
            }
            ArrayList<String> temp3 = adj_list_by_column.get(k);
            for(int j =0;j< temp3.size();j++){
              for(int q=0;q<temp3.size();q++){
                if(q != j){
                  table_matrix.addEdge(temp3.get(j),temp3.get(q),new ColumnEdge(k));
                  table_matrix.setEdgeWeight(table_matrix.getEdge(temp3.get(j),temp3.get(q)), ind); // nullptrException if ee to set
                  // System.out.println("index at: " + ind.toString() + " col:" + k + " j:" + temp3.get(j) + " q:" + temp3.get(q));
                  // edgeName add the string, this could be either map or arraylist
                  edgeName.add(k);
                  ind += 1.0;
                }
              }
            }
          }

          // // test:
          // int stopping = 0;
          // for(DefaultWeightedEdge e : table_matrix.edgeSet()){
          //   System.out.println(table_matrix.getEdgeSource(e) + " --> " + table_matrix.getEdgeTarget(e));
          //   System.out.println(edgeName.get((int)table_matrix.getEdgeWeight(e))); // Note: double cannot be dereferebced error --  canot use intValue()
          //   if(stopping == 10){break;}
          // }



        } catch(Exception e){
          System.out.println(e);
          e.printStackTrace();
        }
      }



      // public static void print_shortest_path(Graph<String, DefaultWeightedEdge> table_matrix,String tb1, String tb2){
      //   System.out.println("Shortest path from tb1 to tb2:");
      //   DijkstraShortestPath<String, DefaultWeightedEdge> dijkstraAlg =
      //       new DijkstraShortestPath<>(table_matrix);
      //   SingleSourcePaths<String, DefaultWeightedEdge> iPaths = dijkstraAlg.getPaths(tb1);
      //
      //   System.out.println("shortest path from table \""+tb1+"\" to table \"" + tb2 + "\":");
      //   System.out.println(iPaths.getPath(tb2).getVertexList() + "\n");
      // }


      public static void print_shortest_path(Graph<String, ColumnEdge> table_matrix,String tb1, String tb2){
        System.out.println("Shortest path from tb1 to tb2:");
        DijkstraShortestPath<String, ColumnEdge> dijkstraAlg =
        new DijkstraShortestPath<>(table_matrix);
        SingleSourcePaths<String, ColumnEdge> iPaths = dijkstraAlg.getPaths(tb1);

        System.out.println("shortest path from table \""+tb1+"\" to table \"" + tb2 + "\":");
        System.out.println(iPaths.getPath(tb2).getVertexList() + "\n");
      }


      //
      // public static void print_join_table(Connection conn, Statement stmt,Graph<String, DefaultWeightedEdge> table_matrix,String tb1, String tb2, ArrayList<String>edgeName){
      //   try{
      //   System.out.println("Shortest path from tb1 to tb2:");
      //   DijkstraShortestPath<String, DefaultWeightedEdge> dijkstraAlg =
      //       new DijkstraShortestPath<>(table_matrix);
      //   SingleSourcePaths<String, DefaultWeightedEdge> iPaths = dijkstraAlg.getPaths(tb1);
      //
      //   System.out.println("shortest path from table \""+tb1+"\" to table \"" + tb2 + "\":");
      //   System.out.println(iPaths.getPath(tb2).getVertexList() + "\n");
      //   List<String> tablelist = iPaths.getPath(tb2).getVertexList();
      //
      //
      //   // retrieve on what columns into arraylist
      //   ArrayList<String> onclauseEdge = new ArrayList<String>();
      //   // retrieve edge string by weight of edge
      //   for(int j = 0; j < tablelist.size()-1;j++){
      //     // get edge name weight in graph
      //     double wt = table_matrix.getEdgeWeight(table_matrix.getEdge(tablelist.get(j),tablelist.get(j+1)));
      //     // get the string by weight/index in edgename
      //     int index = (int)wt;
      //     String columnName1 = edgeName.get(index);
      //     System.out.println("index:"+index);
      //     System.out.println("columnName1:"+columnName1);
      //     // append it to oncluaseEdge
      //     onclauseEdge.add(columnName1);
      //   }
      //
      //
      //   String query = "select * from ";
      //   System.out.println("tblistsize:"+tablelist.size());
      //   System.out.println("oncluasesize:"+onclauseEdge.size());
      //   // contruct tables names and inner join strings
      //   String middle = "";
      //   for(int j=0;j<tablelist.size();j++){
      //
      //     if(j==0){
      //       query = query + tablelist.get(j);
      //     } else{
      //       String currT = tablelist.get(j);
      //       String prevT = tablelist.get(j-1);
      //       String col = onclauseEdge.get(j-1);
      //       query = query+" INNER JOIN "+currT+" ON ("+prevT+"."+col+"="+currT+"."+col+")";
      //     }
      //   }
      //
      //
      //   System.out.println(query);
      //   stmt = conn.createStatement();
      //   ResultSet rs2;
      //   ResultSetMetaData rsmd2;
      //   rs2 = stmt.executeQuery(query);
      //   rsmd2 = rs2.getMetaData();
      //   System.out.println("hellow");
      //   int countloop = 0;
      //   while(rs2.next()){
      //
      //     for(int j=1;j <= rsmd2.getColumnCount();j++){
      //       String type = rsmd2.getColumnTypeName(j);
      //
      //       // System.out.println("type:"+type);
      //       if(type.toLowerCase().contains("binary")){
      //         System.out.println(rsmd2.getColumnName(j)+": "+"some binary, print out make a noise");
      //       }else {
      //         System.out.println(rsmd2.getColumnName(j)+": "+rs2.getString(j));
      //       }
      //
      //     }// end forloop
      //     System.out.println("*******************************************************");
      //
      //   }
      //
      //
      // }catch(Exception e){
      //   System.out.println("Something wrong in join table.");
      //   e.printStackTrace();
      // }
      //
      // }// print join table


      public static void print_join_table(Connection conn, Statement stmt,Graph<String, ColumnEdge> table_matrix,String tb1, String tb2, ArrayList<String>edgeName){
        try{
          System.out.println("Shortest path from tb1 to tb2:");
          DijkstraShortestPath<String, ColumnEdge> dijkstraAlg =
          new DijkstraShortestPath<>(table_matrix);
          SingleSourcePaths<String, ColumnEdge> iPaths = dijkstraAlg.getPaths(tb1);

          System.out.println("shortest path from table \""+tb1+"\" to table \"" + tb2 + "\":");
          System.out.println(iPaths.getPath(tb2).getVertexList() + "\n");
          List<String> tablelist = iPaths.getPath(tb2).getVertexList();


          // retrieve on what columns into arraylist
          ArrayList<String> onclauseEdge = new ArrayList<String>();
          // retrieve edge string by weight of edge
          for(int j = 0; j < tablelist.size()-1;j++){
            // get edge name weight in graph
            double wt = table_matrix.getEdgeWeight(table_matrix.getEdge(tablelist.get(j),tablelist.get(j+1)));
            // get the string by weight/index in edgename
            int index = (int)wt;
            String columnName1 = edgeName.get(index);
            System.out.println("index:"+index);
            System.out.println("columnName1:"+columnName1);
            // append it to oncluaseEdge
            onclauseEdge.add(columnName1);
          }


          String query = "select * from ";
          System.out.println("tblistsize:"+tablelist.size());
          System.out.println("oncluasesize:"+onclauseEdge.size());
          // contruct tables names and inner join strings
          String middle = "";
          for(int j=0;j<tablelist.size();j++){

            if(j==0){
              query = query + tablelist.get(j);
            } else{
              String currT = tablelist.get(j);
              String prevT = tablelist.get(j-1);
              String col = onclauseEdge.get(j-1);
              query = query+" INNER JOIN "+currT+" ON ("+prevT+"."+col+"="+currT+"."+col+")";
            }
          }


          System.out.println(query);
          stmt = conn.createStatement();
          ResultSet rs2;
          ResultSetMetaData rsmd2;
          rs2 = stmt.executeQuery(query);
          rsmd2 = rs2.getMetaData();
          System.out.println("hellow");
          int countloop = 0;
          while(rs2.next()){

            for(int j=1;j <= rsmd2.getColumnCount();j++){
              String type = rsmd2.getColumnTypeName(j);

              // System.out.println("type:"+type);
              if(type.toLowerCase().contains("binary")){
                System.out.println(rsmd2.getColumnName(j)+": "+"some binary, print out make a noise");
              }else {
                System.out.println(rsmd2.getColumnName(j)+": "+rs2.getString(j));
              }

            }// end forloop
            System.out.println("*******************************************************");

          }


        }catch(Exception e){
          System.out.println("Something wrong in join table.");
          e.printStackTrace();
        }

      }// print join table




      public static void get_view_for_user(Connection conn, Statement stmt,String view_name,String view_def,Map<String,String> view_def_map){
        try{

          view_name = view_name.trim();
          String qry = "";

          // if esist in map
          if(view_def_map.containsKey(view_name) && view_def.equals("")){
            System.out.println("view exist");
            qry = view_def_map.get(view_name);
          }
          // if does not exist in map, a new qry comes in
          else if(!view_def_map.containsKey(view_name)) {
            System.out.println("view created");
            qry = view_def;
            create_or_update_view(view_name,view_def,0);
          }
          // if exist and also new query, update
          else if(view_def_map.containsKey(view_name) && (!view_def.equals(""))) {
            System.out.println("view update");
            qry = view_def;
            create_or_update_view(view_name,view_def,1);
          }
          else {
            System.out.println("something wrong");
            return;
          }

          stmt = conn.createStatement();
          ResultSet rs;
          ResultSetMetaData rsmd;
          rs = stmt.executeQuery(qry);
          rsmd = rs.getMetaData();

          while(rs.next()){

            for(int j=1;j <= rsmd.getColumnCount();j++){
              String type = rsmd.getColumnTypeName(j);

              if(type.toLowerCase().contains("binary")){
                System.out.println(rsmd.getColumnName(j)+": "+"some binary, print out make a noise");
              }else {
                System.out.println(rsmd.getColumnName(j)+": "+rs.getString(j));
              }
            }
            System.out.println("*******************************************************");
          }// end while




        }catch(Exception e){
          System.out.println("get-view-for-user");
          e.printStackTrace();
        }
      }


      // update or create view locally

      public static void create_or_update_view(String view_name,String view_def,int choice){
        try{

          if(choice == 0){
            view_def_map.put(view_name,view_def);
            System.out.println("create locally");
          } else {
            view_def_map.replace(view_name,view_def);
            System.out.println("update locally");
          }

          System.out.println("create/update view successfully");

        }catch(Exception e){
          System.out.println("in create_update_view");
          e.printStackTrace();
        }
      }

      public static void graphPNG(Graph<String, ColumnEdge> table_matrix){
        JGraphXAdapter<String, ColumnEdge> graphAdapter = new JGraphXAdapter<>(table_matrix);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());
        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        File imgFile = new File("DB.png");
        try
        {
          ImageIO.write(image, "PNG", imgFile);
          System.out.println("Image created successfully!");
          Desktop.getDesktop().open(imgFile);
        } catch (IOException e)
        {
          System.out.println(e.toString());
        }
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

      public static void jdbShowBestSalesperson(int num, Connection conn) throws SQLException {
        String sql="SELECT c.FirstName, c.LastName, bestEmployeeYTD.bestYTD " +
        "from (select SalesPersonID, SalesYTD as bestYTD from salesperson order by SalesYTD desc limit ?) " +
        "as bestEmployeeYTD inner join " +
        "employee e on bestEmployeeYTD.SalesPersonID = e.EmployeeID inner join " +
        "contact c on e.ContactID = c.ContactID;" ;

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

      private static String removeSemicolon(String str) {
        String result = str;
        if (str.charAt(str.length() - 1) == ';') {
          result = str.substring(0, str.length() - 1);
        }

        return result;
      }

    }
