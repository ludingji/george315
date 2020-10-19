/*
 * MainFrame.java - a frame class to accept queries from user using jtextfield and create a button to submit
 *
 */


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
// import java.awt.event.*;
import java.net.URL;
import javax.imageio.ImageIO;

// import java.net.MalformedURLException;
// import java.awt.Desktop;
// import java.net.URI;
import java.io.*;
import java.util.*;

// MainFrame class as a JPanel,
public class MainFrame extends JFrame {
	private MainMainInterface mainInterface;

	private JFrame window = new JFrame("Main Window");

	// -- PHASE 3 GUI COMPONENTS -- //
	
	private JPanel phase3Panel = new JPanel();
	// JPanel components
	private JButton submitQuery = new JButton("Submit Query");
	private JTextField console = new JTextField("Enter query here");

	// Three basic commands
	private JButton showTablesButton = new JButton("Show All Tables");
	private JButton showColumnsButton = new JButton("Show Columns of Table");
	private JButton joinTablesButton = new JButton("Join Tables");

	// Dropdown for predefined commands and button to activate command
	private String command;
	private JComboBox<String> dropdown;
	private JButton commandButton = new JButton("Execute");

	// -- END PHASE 3 COMPONENTS -- //

	// These can be changed
	private int WIDTH = 950;
	private int HEIGHT = 200;

	// Constructor for MainFrame
	MainFrame() {
		try {
			mainInterface = new MainMainInterface();
		} catch (Exception e) {
			e.printStackTrace();
		}

		phase3Panel.setLayout(null);

		// JButtons, add actionlistener here
		submitQuery.setBounds(10, 120, 120, 30);
		submitQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = getConsoleInput();
				// send to main interface
				sendToMainInterface(text);
			}
		});
		phase3Panel.add(submitQuery);

		// Three basic command buttons
		showTablesButton.setBounds(10, 10, 140, 30);
		showTablesButton.addActionListener(new BasicListener());
		showTablesButton.setActionCommand("show-tables");
		phase3Panel.add(showTablesButton);

		showColumnsButton.setBounds(160, 10, 180, 30);
		showColumnsButton.addActionListener(new BasicListener());
		showColumnsButton.setActionCommand("show-columns");
		phase3Panel.add(showColumnsButton);

		joinTablesButton.setBounds(350, 10, 120, 30);
		joinTablesButton.addActionListener(new BasicListener());
		joinTablesButton.setActionCommand("join-tables");
		phase3Panel.add(joinTablesButton);

		// Dropdown
		String[] choices = {"Show related tables", "Show all primary keys", "Find column", "Search path", 
				"Search and join", "Get view", "Get stats", "Show monthly sales", "Show best salesperson",
				"Get customer info", "Get customer orders", "Plot schema", "Show reason count"};
		dropdown = new JComboBox<String>(choices);
		dropdown.setBounds(10, 50, 180, 20);
		command = "Show related tables";
		dropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command = (String)dropdown.getItemAt(dropdown.getSelectedIndex());
			}
		});
		phase3Panel.add(dropdown);

		commandButton.setBounds(200, 50, 100, 20);
		commandButton.addActionListener(new CommandListener());
		phase3Panel.add(commandButton);

		console.setBounds(10, 90, WIDTH - 2*10, 20);
		phase3Panel.add(console);

		// adding JPanel to MainFrame
		this.add(phase3Panel);
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.pack();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	// listener for basic function buttons
	private class BasicListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();
			String query = "";
			switch(action) {
				case "join-tables": {
					String[] args = {"Table name 1", "Join condition 1-2", "Table name 2", "Join condition 2-3 (optional)", "Table name 3 (optional)", "Join condition 3-4 (optional)", "Table name 4 (optional)"};
					query = promptInput("join-tables", args, 1);
					break;
				}
				case "show-tables":
					query = "show-tables";
					break;
				case "show-columns": {
					String[] args = {"Table name", "Column names separated by space"};
					query = promptInput("show-specific-columns", args);
					break;
				}
			}
			if (!query.equals(""))
				sendToMainInterface(query);
		}
	}

	// ButtonListener that handles logic for dropdown
	private class CommandListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String query = "";
			
			// prompt different input depending on command
			switch(command) {
				case "Show related tables": {
					String[] args = {"Table name"};
					query = promptInput("jdb-show-related-tables", args);
					break;
				}
				case "Show all primary keys":
					// No command String[] args needed
					query = "jdb-show-all-primary-keys";
					break;
				case "Find column": {
					String[] args = {"Column Name"};
					query = promptInput("jdb-find-column", args);
					break;
				}
				case "Search path": {
					String[] args = {"Table 1", "Table 2"};
					query = promptInput("jdb-search-path", args);
					break;
				}
				case "Search and join": {
					String[] args = {"Table 1", "Table 2"};
					query = promptInput("jdb-search-and-join", args);
					break;
				}
				case "Get view": {
					String[] args = {"Table or view name", "Query"};
					query = promptInput("jdb-get-view", args, 2);
					break;
				}
				case "Get stats": {
					String[] args = {"Table or view name", "Column name"};
					query = promptInput("jdb-stat", args);
					break;
				}
				case "Show best salesperson": {
					String[] args = {"Number to show"};
					query = promptInput("jdb-show-best-salesperson", args, 3);
					break;
				}
				case "Show monthly sales": {
					String[] args = {"Year"};
					query = promptInput("jdb-show-sales-monthly", args, 3);
					break;
				}
				case "Get customer orders": {
					String[] args = {"SQL Conditions (in MySQL syntax) (optional)", "aggregate or aggregate-by-sales (optional)"};
					query = promptInput("jdb-customer-orders", args, 4);
					break;
				}
				case "Plot schema": 
					query = "jdb-plot-schema";
					break;
				case "Get customer info": {
					String[] args = {"SQL Conditions (in MySQL syntax) (optional)", "column (for grouping by, optional)"};
					query = promptInput("jdb-customer-info", args, 5);
					break;
				}
				case "Show reason count": {
					query = "jdb-show-reason-count";
					break;
				}
			}
			if (!query.equals(""))
				sendToMainInterface(query);
		}
	}

	// sends command or query to main interface
	public void sendToMainInterface(String query) {
		mainInterface.setQueryString(query);
		try {
			mainInterface.switchOnFirstWord();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String promptInput(String function, String[] fieldStrings) {
		return promptInput(function, fieldStrings, 0);
	}

	// creates JInputDialog to gather parameters for MainMainInterface functions
	// fieldStrings specifies parameter names
	// special is 0 for default, other numbers are for certain commands that have sql syntax or optional parameters
	public String promptInput(String function, String[] fieldStrings, int special) {
		int numFields = fieldStrings.length;
		JPanel pane = new JPanel();
		System.out.println(numFields);
		pane.setLayout(new GridLayout(numFields, 2, 2, 2));

		JTextField[] textFields = new JTextField[numFields];

		// Initialize text fields and add to pane with label
		for (int i = 0; i < numFields; i++) {
			textFields[i] = new JTextField();
			pane.add(new JLabel("Enter " + fieldStrings[i] + ": "));
			pane.add(textFields[i]);
		}

		int option = JOptionPane.showConfirmDialog(window, pane, "Enter " + function + " parameters", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (option == JOptionPane.CANCEL_OPTION) {
			return "";
		}

		String[] params = new String[numFields];
		for (int i = 0; i < numFields; i++) {
			params[i] = textFields[i].getText();
		}
		
		String query = generateQuery(params, special);
		if (query.equals(""))
			return "";
		return function + " " + query;
	}
	
	/* 
	 * Depending on case, checks whether all required arguments are there
	 * Sometimes, arguments are optional
	 * Other times, arguments may be sql syntax and will need to be surrounded by quotes
	 */
	private String generateQuery(String[] command, int special) {
		String query = "";
		if (special == 0) {
			for (String s : command) {
				if (s.equals("")) {
					JOptionPane.showMessageDialog(null, "Not enough arguments.");
					return "";
				}
			}
		}
		
		else if (special == 1) { // join-tables
			if (command[0].equals("") || command[1].equals("") || command[2].equals("")) {
				JOptionPane.showMessageDialog(null, "Not enough arguments.");
				return "";
			}
			boolean a = (command[3].equals("") && command[4].equals("")) || (!command[3].equals("") && !command[4].equals(""));
			boolean b = (command[5].equals("") && command[6].equals("")) || (!command[5].equals("") && !command[6].equals(""));
			if (!a || !b) {
				JOptionPane.showMessageDialog(null, "Cannot have a join column with no table, or a table with no join column.");
				return "";
			}
			command[1] = "\"" + command[1] + "\""; // sql condition
			if (!command[3].equals(""))
				command[3] = "\"" + command[3] + "\""; // sql condition
			if (!command[5].equals(""))
				command[5] = "\"" + command[5] + "\""; // sql condition
		} 
		
		else if (special == 2) { // jdb-get-view
			if (command[0].equals("") || command[1].equals("")) {
				JOptionPane.showMessageDialog(null, "Missing one or more arguments.");
				return "";
			}
			command[1] = "\"" + command[1] + "\""; // is a query
		}
		
		else if (special == 3) { // require a number as the only parameter
			try {
		        Integer.parseInt(command[0]);
		    } catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(null, "Argument must be a number.");
		        return "";
		    }
		}
		
		else if (special == 4 || special == 5) { // first argument is sql condition, both arguments optional
			if (!command[0].equals("")) {
				command[0] = "\"" + command[0] + "\"";
			}
			if (special == 5) {
				command[1] = "groupby " + command[1];
			}
			else if (special == 4) {
				if (!command[1].equals("") && !command[1].equalsIgnoreCase("aggregate") && !command[1].equalsIgnoreCase("aggregate-by-sales")) {
					JOptionPane.showMessageDialog(null, "Second argument must be either blank, aggregate, or aggregate-by-sales.");
					return "";
				}
			}
		}

		for (String s : command) {
			query += s + " ";
		}
		return query;
	}

	// for getting textfield contents
	public String getConsoleInput() {
		return console.getText();
	}

	// Uncomment this and run this file alone to test
	public static void main (String[] args) {
		MainFrame testFrame = new MainFrame();
	}
}

