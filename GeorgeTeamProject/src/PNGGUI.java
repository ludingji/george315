/*
* PNGGUI.java
*/


import javax.swing.*;
import java.awt.*;
// import java.awt.event.*;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import java.io.File;
// import java.net.MalformedURLException;
import java.awt.Desktop;
// import java.net.URI;
import java.io.*;
import java.util.*;


public class PNGGUI extends JFrame{

  // private ImageIcon img_icon1;
  // // private JPanel panel1;
  // private JLabel img_label;
  // private BufferedImage img_buf;
  // private final int width_base = 1200;
  // private final int height_base = 1000;

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L; // this line is needed or else other classes will not recognize this

public PNGGUI(String img){
    try{

      // this.setSize(width_base,height_base);
      // this.setTitle("Graph Schema - jdb-get-schema");
      // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      // img_icon1 = new ImageIcon(img);
      // img_label = new JLabel(img_icon1);
      // add(img_label);

      // this.setVisible(true);

      File f = new File(img);
      Desktop desk = Desktop.getDesktop(); // get the desktop element
      desk.open(f); // use the default app to open image generated
      JOptionPane.showMessageDialog(null,"Default image viewer opened. Please close the message Dialog window and the default image viewer app before entering queries");

    }
    catch(Exception e){
      JOptionPane.showMessageDialog(null,"your default image viewer in your system might not be able to open the image");
      JOptionPane.showMessageDialog(null,"See more details in terminals");
      System.out.println("************* error occur when trying to open image *************");
      System.out.println("******** printing stack trace ***************");
      e.printStackTrace();
    }
  }
}
