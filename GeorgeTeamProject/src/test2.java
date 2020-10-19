import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class test2 {

	public static void main(String[] args) {
		
		var frame = new JFrame();
		frame = new JFrame("dashboard for adventureworks");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(2000, 2000);
		frame.setVisible(false);
		
		var finalPanel = new JPanel();
		var but1 = new JButton("button 1");
		var but2 = new JButton("button 2");
		var but3 = new JButton("button 3");
		var but4 = new JButton("button 4");
		var but5 = new JButton("button 5");
		var but6 = new JButton("button 6");
		
		finalPanel.add(but1, BorderLayout.SOUTH);
		finalPanel.add(but2, BorderLayout.SOUTH);
		finalPanel.add(but3, BorderLayout.SOUTH);
		finalPanel.add(but4, BorderLayout.NORTH);
		finalPanel.add(but5, BorderLayout.NORTH);
		finalPanel.add(but5, BorderLayout.EAST);
		
		frame.add(finalPanel);
		frame.setVisible(true);
	}

}
