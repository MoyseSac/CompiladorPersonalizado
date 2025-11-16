// Utils/ErrorMessages.java
package Utils;

public class ErrorMessages {

    private static Language currentLanguage = Language.SPANISH;

    public static void setLanguage(Language lang) {
        currentLanguage = lang;
    }

    public static String get(ErrorCode code, Object... args) {
        switch (currentLanguage) {
            case KICHE:
                return getKiche(code, args);
            case SPANISH:
            default:
                return getSpanish(code, args);
        }
    }

    private static String getSpanish(ErrorCode code, Object... args) {
        switch (code) {
            case VAR_NOT_DECLARED:
                return String.format("La variable '%s' no ha sido declarada", args);
            case VAR_MAYBE_UNINITIALIZED:
                return String.format("La variable '%s' podría usarse sin haberse inicializado", args);
            case VAR_DECLARED_NEVER_USED:
                return String.format("La variable '%s' fue declarada pero nunca se utilizó", args);

            case TYPE_MISMATCH_DECLARATION:
                return String.format(
                    "Incompatibilidad de tipos en la declaración de '%s': el parser tiene '%s' y el análisis semántico ve '%s'",
                    args
                );
            case TYPE_MISMATCH_ASSIGNMENT:
                return String.format(
                    "Incompatibilidad de tipos: no se puede asignar '%s' a '%s' de tipo '%s'",
                    args
                );
            case TYPE_MISMATCH_RETURN:
                return String.format(
                    "Incompatibilidad de tipos en return: se esperaba '%s' pero se obtuvo '%s'",
                    args
                );
            case ARITHMETIC_REQUIRES_INT:
                return String.format(
                    "La operación aritmética requiere tipos integer, se obtuvo '%s' y '%s'",
                    args
                );
            case COMPARISON_REQUIRES_INT:
                return String.format(
                    "La comparación requiere tipos integer, se obtuvo '%s' y '%s'",
                    args
                );
            case LOGICAL_REQUIRES_BOOL:
                return String.format(
                    "La operación lógica requiere tipos bool, se obtuvo '%s' y '%s'",
                    args
                );
            case UNARY_MINUS_REQUIRES_INT:
                return String.format(
                    "El operador unario '-' requiere tipo integer, se obtuvo '%s'",
                    args
                );
            case NOT_REQUIRES_BOOL:
                return String.format(
                    "El operador lógico '!' requiere tipo bool, se obtuvo '%s'",
                    args
                );
            case PLUS_INVALID_TYPES:
                return String.format(
                    "El operador '+' no acepta '%s' y '%s'",
                    args
                );

            case IF_CONDITION_NOT_BOOL:
                return "La condición del 'if' debe ser de tipo bool";
            case WHILE_CONDITION_NOT_BOOL:
                return "La condición del 'while' debe ser de tipo bool";
            case DO_WHILE_CONDITION_NOT_BOOL:
                return "La condición del 'do-while' debe ser de tipo bool";
            case FOR_CONDITION_NOT_BOOL:
                return "La condición del 'for' debe ser de tipo bool";

            case FUNCTION_NOT_DECLARED:
                return String.format("La función '%s' no ha sido declarada", args);
            case NOT_A_FUNCTION:
                return String.format("'%s' no es una función", args);
            case FUNCTION_ARG_COUNT_MISMATCH:
                return String.format(
                    "La función '%s' espera %d argumentos pero se recibieron %d",
                    args
                );
            case FUNCTION_MUST_RETURN_VALUE:
                return String.format(
                    "La función debe retornar un valor de tipo '%s'",
                    args
                );
            case FUNCTION_VOID_CANNOT_RETURN_VALUE:
                return "Una función 'void' no puede retornar un valor";
            case RETURN_OUTSIDE_FUNCTION:
                return "Sentencia 'return' fuera de una función";
            case CANNOT_COMPARE_TYPES: 
                return String.format("No se puede comparar '%s' con '%s'", args);
            default:
                return "Error semántico";
        }
    }

    private static String getKiche(ErrorCode code, Object... args) {
        // Por ahora versión súper básica / mezclada, solo para la demo.
        switch (code) {
            case VAR_NOT_DECLARED:
                return String.format("K'olik: ri variable '%s' man q'atal taj (no está declarada)", args);
            case IF_CONDITION_NOT_BOOL:
                return "K'olik: ri condición if k'o chi sea bool";
            default:
                return "K'olik: xojtik error semántico";
        }
    }
}
