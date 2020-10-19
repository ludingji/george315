
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.nio.*;
import org.jgrapht.nio.dot.*;
import org.jgrapht.traverse.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

// follow ReglationshipEdge example at https://jgrapht.org/guide/LabeledEdges

public class ColumnEdge extends DefaultWeightedEdge{
  public String label = "";

  public ColumnEdge(String lab){
    this.label = lab;
  }

  @Override
  public String toString() {
    return label;
  }
}
