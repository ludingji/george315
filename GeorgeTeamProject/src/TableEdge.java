import java.sql.*;
import java.util.*;
import java.lang.*;

public class TableEdge{

  public String s1;
  public String s2;
  public Integer edgelabel = 0;
  public String edgevalue = "";


  public TableEdge(){
    s1 = "";
    s2 = "";
    edgevalue = "";
  }

  public TableEdge(String t1, String t2){
    s1 = t1;
    s2 = t2;
  }

  // commenting this out because there is no working Edge class right now
  /*public TableEdge(Edge e){
    this.s1 = e.s1;
    this.s2 = e.s2;
    this.edgelabel = e.edgelabel;
    this.edgevalue = e.edgevalue;
  }

  public boolean equaledge(Edge e){
    if((e.s1).equals(this.s1) && (e.s2).equals(this.s2)){
      return true;
    }
    return false;
  }*/

  public void printEdge(){
    System.out.println("s1:"+s1+" s2:"+s2+" "+"edgelabel:"+edgelabel);
  }

};
