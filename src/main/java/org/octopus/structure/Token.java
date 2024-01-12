package org.octopus.structure;

import org.octopus.enums.TokenType;

public class Token {
    private TokenType tokenType;
    private int line;
    private int column;
    private String lexeme;

    public Token() {
    }

    public Token(TokenType tokenType, String lexeme, int line, int column) {
        this.tokenType = tokenType;
        this.lexeme = lexeme;
        this.line = line;
        this.column =  column;
    }

    public String getLexemeType() {
        return tokenType.getTokenType();
    }

    public String getLexeme() {
        return this.lexeme;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }
}
