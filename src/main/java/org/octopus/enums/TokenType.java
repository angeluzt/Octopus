package org.octopus.enums;

import static org.octopus.util.Constants.KEYWORD;
import static org.octopus.util.Constants.UNKNOWN_TKN;
import static org.octopus.util.Constants.LITERAL;
import static org.octopus.util.Constants.OPERATOR;
import static org.octopus.util.Constants.SEPARATOR;
import static org.octopus.util.Constants.LOGIC_OPERATOR;

public enum TokenType {
    // keywords
    PROGRAM("program", KEYWORD),
    ERROR("error", KEYWORD),
    IF("if", KEYWORD),
    THEN("then", KEYWORD),
    ELSE("else", KEYWORD),
    FI("fi", KEYWORD),
    DO("do", KEYWORD),
    UNTIL("until", KEYWORD),
    FOR("for", KEYWORD),
    WRITE("write", KEYWORD),
    READ("read", KEYWORD),
    INT("int", KEYWORD),
    FLOAT("float", KEYWORD),
    TEXT("text", KEYWORD),
    BOOL("bool", KEYWORD),
    TRUE("true", KEYWORD),
    FALSE("false", KEYWORD),
    NULL("null", KEYWORD),
    // literals
    INT_NUMBER("number", LITERAL),
    FLOAT_NUMBER("float_number", LITERAL),
    ID("identifier", LITERAL),
    STRING("text", LITERAL),
    // operators
    ADD("+", OPERATOR),
    UNARY_ADD("+", OPERATOR),
    SUBTRACT("-", OPERATOR),
    UNARY_SUBTRACT("-", OPERATOR),
    ADD_ADD("++", OPERATOR),
    SUBTRACT_SUBTRACT("--", OPERATOR),
    ADD_ADD_POST("++", OPERATOR),
    SUBTRACT_SUBTRACT_POST("--", OPERATOR),
    ADD_EQUALS("+=", OPERATOR),
    SUBTRACT_EQUALS("-=", OPERATOR),
    MULTIPLY("*", OPERATOR),
    MULTIPLY_EQUALS("*=", OPERATOR),
    DIVIDE("/", OPERATOR),
    DIVIDE_EQUALS("/=", OPERATOR),
    ASSIGN("=", OPERATOR),
    // logical operators
    LOGICAL_AND("&&", LOGIC_OPERATOR),
    LOGICAL_OR("||", LOGIC_OPERATOR),
    AND("&", LOGIC_OPERATOR),
    OR("|", LOGIC_OPERATOR),
    NOT("!", LOGIC_OPERATOR),
    LOWER("<", LOGIC_OPERATOR),
    LOWER_OR_EQUALS("<=", LOGIC_OPERATOR),
    HIGHER(">", LOGIC_OPERATOR),
    HIGHER_OR_EQUALS(">=", LOGIC_OPERATOR),
    EQUALS("==", LOGIC_OPERATOR),
    DIFFERENT("!=", LOGIC_OPERATOR),
    TERNARY("?", LOGIC_OPERATOR),// TODO: pending
    // separators
    QUOT("\"", SEPARATOR),
    COMMA(",", SEPARATOR),
    SEMICOLON(";", SEPARATOR),
    PARENTHESIS_OPEN("(", SEPARATOR),
    PARENTHESIS_CLOSE(")", SEPARATOR),
    BRACES_OPEN("{", SEPARATOR),
    BRACES_CLOSE("}", SEPARATOR),
    BRACKET_OPEN("[", SEPARATOR),
    BRACKET_CLOSE("]", SEPARATOR),
    CONTEXT("{}", SEPARATOR),
    UNKNOWN("unknown", UNKNOWN_TKN);

    private String tokenName;
    private String tokenType;

    private TokenType(String tokenName, String tokenType) {
        this.tokenName = tokenName;
        this.tokenType = tokenType;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getTokenName() {
        return tokenName;
    }

    public boolean compare(TokenType token) {
        return this.equals(token);
    }
}
