
package Parser;
import Utils.Position; 
import java.util.List;

public class FunctionDeclarationNode extends ASTNodo {
    public String name;
    public String returnType;  // "void", "integer", "string", "bool"
    public List<ParameterNode> parameters;
    public List<ASTNodo> body;
    
    public FunctionDeclarationNode(String name, String returnType, 
                                   List<ParameterNode> parameters, 
                                   List<ASTNodo> body, Position position) {
        super(position);
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.body = body;
    }
    
    @Override
    public String toString() {
        return String.format("FunctionDecl(%s, returns %s)", name, returnType);
    }
}
