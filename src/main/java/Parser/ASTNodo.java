package Parser;

import Utils.Position;

//Creamos nuestro nodo con forma AST 
public abstract class ASTNodo {
    protected Position position;
    
    public ASTNodo(Position position) {
        this.position = position;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public abstract String toString();
    
    
    
}