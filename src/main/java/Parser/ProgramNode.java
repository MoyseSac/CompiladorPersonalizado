// Parser/ProgramNode.java
package Parser;

import Utils.Position;
import java.util.List;

public class ProgramNode extends ASTNodo {
    public List<ASTNodo> statements;
    
    public ProgramNode(List<ASTNodo> statements, Position position) {
        super(position);
        this.statements = statements;
    }
    
    @Override
    public String toString() {
        return "Program(" + statements.size() + " statements)";
    }
}