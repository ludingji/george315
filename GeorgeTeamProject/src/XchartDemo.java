
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.demo.charts.area.AreaChart01;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.*;

public class XchartDemo {

  public static void main(String[] args) throws Exception {

    double[] xData = new double[] { 0.0, 1.0, 2.0 };
    double[] yData = new double[] { 2.0, 1.0, 0.0 };
    List<String> name = new ArrayList<String>(Arrays.asList("TX","LA","CA","MN","UT","ML","FL","AZ","WY"));
    List<Integer> pop = new ArrayList<Integer>(Arrays.asList(31111,23314,21455,21235,47786,14432,34564,11231,75234));


    JFrame frame = new JFrame("XChart Swing Demo");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setSize(800,800);
    // Create Chart
    XYChart chart1 = new XYChart(400,400);
    chart1.addSeries("data1",xData,yData);
    CategoryChart chart2 = new CategoryChartBuilder().width(400).height(400).title("Stick").build();
    // chart2.getStyler().setLegendPosition(LegendPosition.InsideNW);
    // chart2.getStyler().setDefaultSeriesRenderStyle(CategorySeriesRenderStyle.Stick);
    chart2.addSeries("data", name, pop);


    JPanel panel1 = new XChartPanel(chart1);
    JPanel panel2 = new XChartPanel(chart2);
    JPanel panel3 = new JPanel();
    panel3.add(panel1);
    panel3.add(panel2);
    frame.add(panel3);
    frame.pack();
    frame.setVisible(true);

    // Show it
    // new SwingWrapper(chart).displayChart();

  }
}
