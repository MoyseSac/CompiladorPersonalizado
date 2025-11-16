package Parser;
import Utils.Position; 
/**
 *
 * @author Moise
 */
public class VarDeclarationNode extends ASTNodo {
    public String type;        // "integer", "string", "bool"
    public String name;        // variable name
    public ASTNodo initializer; // optional: = expression
    
    public VarDeclarationNode(String type, String name, ASTNodo initializer, Position position) {
        super(position);
        this.type = type;
        this.name = name;
        this.initializer = initializer;
    }
    
    @Override
    public String toString() {
        return String.format("VarDecl(%s %s)", type, name);
    }
}
