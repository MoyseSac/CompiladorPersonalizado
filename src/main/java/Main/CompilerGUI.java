package Main;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class CompilerGUI extends JFrame {
    
    private JTextArea codeArea;
    private JTextArea outputArea;
    private JButton compileButton;
    private JButton clearButton;
    private Compiler compiler;
    
    public CompilerGUI() {
        setTitle("Compilador de Lenguaje Propio");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        

        JSplitPane splitPane = createMainPanel();
        add(splitPane, BorderLayout.CENTER);
        
        
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null);
    }
    
    private JSplitPane createMainPanel() {
        // Editor de código
        codeArea = new JTextArea();
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        codeArea.setTabSize(4);
        codeArea.setText(getExampleCode());
        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setBorder(BorderFactory.createTitledBorder("Código fuente"));
        
        // Consola de salida
        outputArea = new JTextArea();
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(40, 40, 40));
        outputArea.setForeground(Color.GREEN);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Consola de salida"));
        
        // Divisor horizontal
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, codeScroll, outputScroll);
        splitPane.setDividerLocation(600);
        return splitPane;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 10, 10));
        Dimension buttonSize = new Dimension(150, 40);
        Font buttonFont = new Font("Arial", Font.BOLD, 14);

        compileButton = new JButton(" Compilar");
        compileButton.setFont(buttonFont);
        compileButton.setPreferredSize(buttonSize);
        compileButton.addActionListener(e -> compileButtonAction());
        compileButton.setBackground(new Color(60, 60, 60));
        compileButton.setForeground(Color.GREEN);

        clearButton = createStyledButton("Limpiar salida", e -> outputArea.setText(""));
        JButton lexicalButton   = createStyledButton("Ver análisis léxico", e -> showLexicalAnalysis());
        JButton syntaxButton    = createStyledButton("Ver errores sintácticos", e -> showSyntaxErrors());
        JButton semanticButton  = createStyledButton("Ver errores semánticos", e -> showSemanticErrors());
        JButton symbolTableButton = createStyledButton("Ver tabla de símbolos", e -> showSymbolTable());

        panel.add(compileButton);
        panel.add(clearButton);
        panel.add(lexicalButton);
        panel.add(syntaxButton);
        panel.add(semanticButton);
        panel.add(symbolTableButton);

        return panel;
    }

    private void compileButtonAction() {
        outputArea.setText("");
        String sourceCode = codeArea.getText();
        
        if (sourceCode.trim().isEmpty()) {
            outputArea.setText("Error: no hay código fuente.\n");
            return;
        }
        
        outputArea.append("=== INICIANDO COMPILACIÓN ===\n\n");
        
        compiler = new Compiler(sourceCode);
        boolean success = compiler.compile();
        
        // Resultados en consola
        if (success) {
            outputArea.append("✓✓✓ COMPILACIÓN EXITOSA ✓✓✓\n\n");
            outputArea.append("No se encontraron errores en ninguna fase.\n");
            outputArea.append("El código es sintáctica y semánticamente correcto.\n\n");
            outputArea.append("═".repeat(50) + "\n");
            outputArea.append("TABLA DE SÍMBOLOS:\n");
            outputArea.append("═".repeat(50) + "\n");
            outputArea.append(compiler.getSymbolTableReport());
        } else {
            outputArea.append("✗✗✗ COMPILACIÓN FALLIDA ✗✗✗\n\n");
            outputArea.append(compiler.getAllErrorsReport());
            outputArea.append("\nCorrige los errores e intenta de nuevo.\n");
        }
        
        outputArea.setCaretPosition(0); // Ir al inicio
    }
    
    // Ventana análisis léxico 
    private void showLexicalAnalysis() {
        if (compiler == null) {
            JOptionPane.showMessageDialog(
                this,
                "Por favor, compila primero.",
                "Sin datos",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        JTextArea textArea = new JTextArea(compiler.getLexicalAnalysisReport());
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 500));
        
        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Resultados del análisis léxico",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    // Ventana errores sintácticos
    private void showSyntaxErrors() {
        if (compiler == null) {
            JOptionPane.showMessageDialog(
                this,
                "Por favor, compila primero.",
                "Sin datos",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        JTextArea textArea = new JTextArea(compiler.getSyntaxErrorsReport());
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 400));
        
        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Errores sintácticos",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    // Ventana errores semánticos 
    private void showSemanticErrors() {
        if (compiler == null) {
            JOptionPane.showMessageDialog(
                this,
                "Por favor, compila primero.",
                "Sin datos",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        JTextArea textArea = new JTextArea(compiler.getSemanticErrorsReport());
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 400));
        
        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Errores semánticos",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    // Tabla de simbolos
    private void showSymbolTable() {
        if (compiler == null) {
            JOptionPane.showMessageDialog(
                this,
                "Por favor, compila primero.",
                "Sin datos",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        JTextArea textArea = new JTextArea(compiler.getSymbolTableReport());
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(900, 500));
        
        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Tabla de símbolos",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    

    private JButton createStyledButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(150, 40));
        button.setBackground(new Color(60, 60, 60));
        button.setForeground(Color.GREEN);
        button.addActionListener(action);
        return button;
    }

    //Texto ejemplo
    private String getExampleCode() {
        return """
            
            
            ajilabal count = 0;
            tzij message = "Utz awäch";
            kolik isActive = je;
            
            samaj add(ajilabal a, ajilabal b) tzolil ajilabal {
                ajilabal sum;
                sum = a + b;
                tzol sum;
            }

            """;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CompilerGUI gui = new CompilerGUI();
            gui.setVisible(true);
        });
    }
}