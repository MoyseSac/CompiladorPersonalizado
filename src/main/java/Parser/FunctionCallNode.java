package Parser;
import Utils.Position;
import java.util.List;

public class FunctionCallNode extends ASTNodo {
    public String functionName;
    public List<ASTNodo> arguments;
    
    public FunctionCallNode(String functionName, List<ASTNodo> arguments, Position position) {
        super(position);
        this.functionName = functionName;
        this.arguments = arguments;
    }
    
    @Override
    public String toString() {
        return String.format("FunctionCall(%s)", functionName);
    }
}