

import javax.swing.*;
import java.awt.*;
// import java.awt.event.*;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import java.io.*;
import java.util.*;
import java.util.List;
import java.sql.*;



// this class could be used to show tables--- first 3 GUI tasks in phase 3

// https://stackoverflow.com/questions/19373186/copying-a-2d-string-arraylist


public class TableGUI extends JFrame{


	// must use 2D primitive String array here
	private String [][] table;
	private ImageIcon image;
	private final int width_base = 800;
	private final int height_base = 600;
	private JTable jtable;
	private JLabel jlabel;
	private String [] cols;
	private JScrollPane scroll;


	// constructor of the TableGUI class. The constructor will pop up the image
	public TableGUI(ResultSet rs){
		try{
			this.setSize(width_base,height_base);
			this.setTitle("Present table in GUI");

			ResultSetMetaData rsmd = rs.getMetaData();
			int numCols = rsmd.getColumnCount();

			ArrayList<String[]> table_al = new ArrayList<String[]>();
			// System.out.println("hi");
			while (rs.next()) {
				// System.out.println("howdy");
				String[] row = new String[numCols];
				for (int i = 1; i <= numCols; i++){
						row[i-1] = rs.getString(i);
						// System.out.println("row:"+row[i-1]);
				}
				table_al.add(row);
			}

			// System.out.println("hi2");
			this.table = table_al.toArray(new String[table_al.size()][]);

			// convert array list of column names to string array to pass into JTable
			this.cols = new String[numCols];
			for(int i = 1; i <= numCols; i++){
				this.cols[i-1] = rsmd.getColumnLabel(i);
			}

			// JTable element requires Object [][] for the table you want to show and Object[] for column row
			this.jtable = new JTable(this.table, this.cols);
			jtable.setShowGrid(true);
			// use the JTable to instantiate ScrollPane, required
			// add JScrollPane to JPanel
			add(new JScrollPane(this.jtable));
			// set the panel to be visible
			setVisible(true);


		} catch (Exception e){
			System.out.println("Something wrong in constructor");
			e.printStackTrace();
		}
	}// end constructor

	// in case input is not a ResultSet
	public TableGUI(List<String> rows) {
		try {
			this.setSize(width_base,height_base);
			this.setTitle("Present output in GUI");
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			String allText = "<html><pre>";
			for (String row : rows) {
				allText += row + "<br>";
			}
			allText += "</pre></html>";

			jlabel = new JLabel(allText);
			add(new JScrollPane(jlabel));
			setVisible(true);
		} catch (Exception e) {
			System.out.println("Something wrong in constructor");
			e.printStackTrace();
		}
	}


}
