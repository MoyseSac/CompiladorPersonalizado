
package Parser;
import Utils.Position;
/**
 *
 * @author Moise
 */
public class LiteralNode extends ASTNodo {
    public Object value;
    public String type;  // "integer", "string", "bool"
    
    public LiteralNode(Object value, String type, Position position) {
        super(position);
        this.value = value;
        this.type = type;
    }
    
    @Override
    public String toString() {
        return String.format("Literal(%s: %s)", type, value);
    }
}
