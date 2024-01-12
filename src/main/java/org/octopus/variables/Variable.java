package org.octopus.variables;

import org.octopus.enums.TokenType;

public class Variable {
    private Object value;
    private TokenType type;

    public Variable() {}

    public Variable(Object variable, TokenType type) {
        this.value = variable;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }
}
