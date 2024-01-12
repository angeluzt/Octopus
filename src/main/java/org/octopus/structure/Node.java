package org.octopus.structure;

import org.octopus.enums.TokenType;

public class Node {
    private Node brother;
    private Node[] children;
    private Token token;

    public Node() {
    }

    public Node(Token token, int childrenLength) {
        this.token = token;
        children = new Node[childrenLength];
    }

    public Node getBrother() {
        return brother;
    }

    public void setBrother(Node brother) {
        this.brother = brother;
    }

    public Node[] getChildren() {
        return children;
    }

    public Node getChildrenAt(int index) {
        if(children[index] == null) {
            return null;
        }
        return children[index];
    }

    public void setChildren(Node[] children) {
        this.children = children;
    }

    public void putNode(Node node, int index) {
        children[index] = node;
    }

    public Token getToken() {
        return token;
    }

    public String getTokenLexeme() {
        return token.getLexeme();
    }

    public int getLine() {
        return token.getLine();
    }

    public int getColum() {
        return token.getColumn();
    }

    public TokenType getTokenType() {
        return token.getTokenType();
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
