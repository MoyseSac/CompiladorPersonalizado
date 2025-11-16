package Parser;
import Utils.Position;

public class ParameterNode extends ASTNodo {
    public String type;
    public String name;
    
    public ParameterNode(String type, String name, Position position) {
        super(position);
        this.type = type;
        this.name = name;
    }
    
    @Override
    public String toString() {
        return String.format("Param(%s %s)", type, name);
    }
}
