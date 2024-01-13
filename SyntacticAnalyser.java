package org.octopus.analysers;

import org.octopus.enums.TokenType;
import org.octopus.structure.Node;
import org.octopus.structure.Token;

import java.util.LinkedList;

public class SyntacticAnalyser implements Analyser<LinkedList<Token>, Node> {

    private LinkedList<Token> tokens;
    private LinkedList<Token> errorTokens = new LinkedList<>();

    @Override
    public Node analyze(LinkedList<Token> code) throws Exception {
        this.tokens = code;
        Node syntacticTree = null;
        if (!tokens.isEmpty()) {
            try {
                syntacticTree = program();
            } catch (Exception ex) {
                System.out.println(ex);
            }

        } else {
            generateSpecificErrorAndThrowException("code is empty", 0, 0);
        }
        String a = "" + 2*23/4;

        return syntacticTree;
    }

    public LinkedList<Token> getErrorTokens() {
        return errorTokens;
    }

    private Node program() throws Exception {
        Node node = null;

        // expect nodes: program ClassName {
        removeTokenIfEqualsOrThrowException(TokenType.PROGRAM);

        Token className = removeTokenIfEqualsOrThrowException(TokenType.ID);
        node = new Node(className, 1);

        //removeTokenIfEqualsOrThrowException(TokenType.BRACES_OPEN);

        // search for declarations and sentences
        node.putNode(block(), 0);
        //node.putNode(loopsDeclaration(), 0);

        // expect node: }
        //removeTokenIfEqualsOrThrowException(TokenType.BRACES_CLOSE);

        return node;
    }

    // ID
    private Node declarationVarName() throws Exception {
        Node node = null;

        node = new Node(removeTokenIfEqualsOrThrowException(TokenType.ID), 1);
        node.putNode(commaLowestPriorityElement(), 0);

        return node;
    }

    // optional ID
    private Node optionalDeclarationVarName() throws Exception {
        Node node = null;

        if(compare(TokenType.ID)) {
            node = new Node(removeFirstToken(), 1);
            node.putNode(commaLowestPriorityElement(), 0);
        }

        return node;
    }

    private Node sentence() throws Exception {
        Node node = null;

        switch (peekToken().getTokenType()) {
            case IF:
                Token tokenNew = removeFirstToken();
                if (compare(TokenType.ELSE)) {
                    //node = new Node(sentence(), 0);
                    node = sentence();
                    //node.putNode(booleanExpressionList(), 0);
                    //node.putNode(block(), 1);
                } else {
                    node = new Node(tokenNew, 3);
                    node.putNode(booleanExpressionList(), 0);
                    node.putNode(block(), 1);
                    node.putNode(sentence(), 2);
                }
                /*//Token tokenNew = removeFirstToken();
                //if (compare(TokenType.ELSE)) {
                    node = new Node(removeFirstToken(), 2);
                    node.putNode(booleanExpressionList(), 0);
                    node.putNode(block(), 1);
                //}

                node.putNode(booleanExpressionList(), 0);
                node.putNode(block(), 1);
                if (compare(TokenType.ELSE)) {
                    node.setBrother(sentence());
                }*/
                break;
            case ELSE:
                tokenNew = removeFirstToken();
                if (compare(TokenType.IF)) {
                    //node = new Node(removeFirstToken(), 2);
                    node = sentence();
                    //node.putNode(booleanExpressionList(), 0);
                    //node.putNode(block(), 1);
                } else {
                    tokenNew.setTokenType(TokenType.IF);
                    node = new Node(tokenNew, 1);
                    node.putNode(block(), 0);
                }
                break;
            case UNTIL:
                node = new Node(removeFirstToken(), 2);
                node.putNode(booleanExpressionList(), 0);
                node.putNode(block(), 1);
                break;
            case FOR:
                // for(declaration;
                node = new Node(removeFirstToken(), 4);
                removeTokenIfEqualsOrThrowException(TokenType.PARENTHESIS_OPEN);

                node.putNode(declareVariable(), 0);
                //if empty then use an optional assignation =
                if(node.getChildren()[0] == null) {
                    node.putNode(optionalDeclarationVarName(), 0);
                    // semicolon is not deleted automatically in optional assignation
                    removeTokenIfEqualsOrThrowException(TokenType.SEMICOLON);
                }

                // expression;
                node.putNode(logicalSecondGrade(), 1);
                removeTokenIfEqualsOrThrowException(TokenType.SEMICOLON);

                //assign
                node.putNode(arithmeticOperatorsSecondGrade(), 2);
                removeTokenIfEqualsOrThrowException(TokenType.PARENTHESIS_CLOSE);

                node.putNode(block(), 3);
                break;
            case BRACES_OPEN:
                node = block();
                break;
            case DO:
                node = new Node(removeFirstToken(), 2);

                node.putNode(block(), 0);

                Node newNode = new Node(removeTokenIfEqualsOrThrowException(TokenType.UNTIL), 1);
                newNode.putNode(booleanExpressionList(), 0);
                node.putNode(newNode, 1);
                removeTokenIfEqualsOrThrowException(TokenType.SEMICOLON);

                break;
            case READ:
                node = new Node(removeFirstToken(), 1);
                removeTokenIfEqualsOrThrowException(TokenType.PARENTHESIS_OPEN);

                int currentLine = peekToken().getLine();
                node.putNode(new Node(removeTokenIfEqualsOrThrowException(TokenType.ID), currentLine), 0);

                removeTokenIfEqualsOrThrowException(TokenType.PARENTHESIS_CLOSE);
                removeTokenIfEqualsOrThrowException(TokenType.SEMICOLON);
                break;
            case WRITE:
                node = new Node(removeFirstToken(), 1);
                removeTokenIfEqualsOrThrowException(TokenType.PARENTHESIS_OPEN);

                currentLine = peekToken().getLine();
                //node.putNode(new Node(removeTokenIfEqualsOrThrowException(TokenType.ID), currentLine), 0);
                node.putNode(logicalSecondGrade(), 0);
                removeTokenIfEqualsOrThrowException(TokenType.PARENTHESIS_CLOSE);
                removeTokenIfEqualsOrThrowException(TokenType.SEMICOLON);
                break;
            case ID:
                break;
            default:
                //node = declareVariable();
                break;
        }

        return node;
    }

    // if, until, for, {, read, write, do
    // int, bool, float, text
    private Node codeLoop() throws Exception {
        Node node = null;

        Node nodeCopy = declareVariable();
        if(nodeCopy == null) {
            nodeCopy = sentence();
        }
        if(nodeCopy == null) {
            nodeCopy = assignment();
            if (nodeCopy != null){
                removeTokenIfEqualsOrThrowException(TokenType.SEMICOLON);
            }
        }

        if (nodeCopy != null)
            nodeCopy.setBrother(codeLoop());

        return nodeCopy;
    }

    // if, until, for, {, read, write, do
    private Node loopsDeclaration() throws Exception {
        Node node = null;

        node = variableDeclaration();

        while(compare(TokenType.IF) || compare(TokenType.FOR)
                || compare(TokenType.UNTIL) || compare(TokenType.BRACES_OPEN)
                || compare(TokenType.READ) || compare(TokenType.WRITE)
                || compare(TokenType.DO)) {
            if(node == null) {
                node = sentence();
            } else {
                // since node is already created, then you can create a new declaration
                node.setBrother(codeLoop());
            }
        }

        return node;
    }

    // int, bool, float, text
    private Node variableDeclaration() throws Exception {
        Node node = null;

        while(compare(TokenType.INT) || compare(TokenType.FLOAT)
                || compare(TokenType.TEXT) || compare(TokenType.BOOL)) {
            node = new Node(removeFirstToken(), 1);
            node.putNode(declarationVarName(), 0);
            removeTokenIfEqualsOrThrowException(TokenType.SEMICOLON);
            node.setBrother(loopsDeclaration());
        }

        return node;
    }

    // int, bool, float, text
    private Node declareVariable() throws Exception {
        Node node = null;

        if(compare(TokenType.INT) || compare(TokenType.FLOAT)
                || compare(TokenType.TEXT) || compare(TokenType.BOOL)) {
            node = new Node(removeFirstToken(), 1);
            node.putNode(declarationVarName(), 0);
            removeTokenIfEqualsOrThrowException(TokenType.SEMICOLON);
            //node.setBrother(loopsDeclaration());
        }

        return node;
    }

    // ,
    private Node commaLowestPriorityElement() throws Exception {
        Node node = null;

        node = assignment();

        if(compare(TokenType.COMMA)) {
            // no assignment for this declaration
            removeFirstToken();
            if(node == null) {
                node = declarationVarName();
            } else {
                node.setBrother(declarationVarName());
            }
        }

        return node;
    }

    // =, +=, -+, *=, /=, %=
    private Node assignment() throws Exception {
        Node node = null;

        node = logicalSecondGrade();

        switch (peekToken().getTokenType()) {
            case ASSIGN:
                //TODO: include missing equalities
                node = new Node(removeFirstToken(), 1);
                node.putNode(logicalSecondGrade(), 0);
                /*Node nodeCopy = new Node(removeFirstToken(), 2);
                nodeCopy.putNode(node, 0);

                node = nodeCopy;
                node.putNode(logicalSecondGrade(), 1);*/
                break;
            default:
                //TODO:error??
                break;

        }

        return node;
    }

    // Logical ||
    private Node logicalSecondGrade() throws Exception {
        Node node = null;

        node = logicalFirstGrade();

        while(compare(TokenType.LOGICAL_OR)) {
            Node nodeCopy = new Node(removeFirstToken(), 2);
            nodeCopy.putNode(node, 0);

            node = nodeCopy;
            node.putNode(logicalFirstGrade(), 1);
        }

        return node;
    }

    // Logical &&
    private Node logicalFirstGrade() throws Exception {
        Node node = null;

        node = booleanOperatorsFourthGrade();

        while(compare(TokenType.LOGICAL_AND)) {
            Node nodeCopy = new Node(removeFirstToken(), 2);
            nodeCopy.putNode(node, 0);

            node = nodeCopy;
            node.putNode(logicalFirstGrade(), 1);
        }

        return node;
    }

    // OR
    private Node booleanOperatorsFourthGrade() throws Exception {
        Node node = null;

        node = booleanOperatorsThirdGrade();

        while(compare(TokenType.OR)) {
            Node nodeCopy = new Node(removeFirstToken(), 2);
            nodeCopy.putNode(node, 0);

            node = nodeCopy;
            int previousTokens = getTokensSize();
            node.putNode(booleanOperatorsThirdGrade(), 1);
            if(previousTokens ==  getTokensSize()) {
                generateSpecificErrorAndThrowException("<expression> expected", node.getToken().getLine(), peekToken().getColumn());
            }
        }

        return node;
    }

    // AND
    private Node booleanOperatorsThirdGrade() throws Exception {
        Node node = null;

        node = booleanOperatorsSecondGrade();

        while(compare(TokenType.AND)) {
            Node nodeCopy = new Node(removeFirstToken(), 2);
            nodeCopy.putNode(node, 0);

            node = nodeCopy;
            int previousTokens = getTokensSize();
            node.putNode(booleanOperatorsSecondGrade(), 1);
            if(previousTokens ==  getTokensSize()) {
                generateSpecificErrorAndThrowException("<expression> expected", node.getToken().getLine(), peekToken().getColumn());
            }
        }

        return node;
    }

    // ==, !=: Equality
    private Node booleanOperatorsSecondGrade() throws Exception {
        Node node = null;

        node = booleanOperatorsFirstGrade();

        if(compare(TokenType.EQUALS) || compare(TokenType.DIFFERENT)) {
            Node nodeCopy = new Node(removeFirstToken(), 2);
            nodeCopy.putNode(node, 0);

            node = nodeCopy;
            node.putNode(booleanOperatorsFirstGrade(), 1);
        }

        return node;
    }

    //<, <=, >, >=: Relational
    private Node booleanOperatorsFirstGrade() throws Exception {
        Node node = null;

        node = arithmeticOperatorsSecondGrade();

        switch (peekToken().getTokenType()) {
            case LOWER:
            case LOWER_OR_EQUALS:
            case HIGHER:
            case HIGHER_OR_EQUALS:
            case DIFFERENT:
                Node nodeCopy = new Node(removeFirstToken(), 2);
                nodeCopy.putNode(node, 0);

                node = nodeCopy;
                node.putNode(arithmeticOperatorsSecondGrade(), 1);
                break;
            default:
                //TODO:error??
                break;

        }

        return node;
    }

    // +, -: additive
    private Node arithmeticOperatorsSecondGrade() throws Exception {
        Node node = null;

        node = arithmeticOperatorsFirstGrade();

        // iterate until all the -, + are finished
        while(compare(TokenType.ADD) || compare(TokenType.SUBTRACT)) {
            Node nodeCopy = new Node(removeFirstToken(), 2);
            nodeCopy.putNode(node, 0);

            node = nodeCopy;
            int previousTokens = getTokensSize();
            node.putNode(arithmeticOperatorsFirstGrade(), 1);
            if(previousTokens ==  getTokensSize()) {
                generateSpecificErrorAndThrowException("<expression> expected", node.getToken().getLine(), peekToken().getColumn());
            }
        }

        return node;
    }

    // +, /, %: multiplicative
    private Node arithmeticOperatorsFirstGrade() throws Exception {
        Node node = null;

        node = unaryOperators();

        // iterate until all the *, / are finished
        while(compare(TokenType.MULTIPLY) || compare(TokenType.DIVIDE)) {
            Node nodeCopy = new Node(removeFirstToken(), 2);
            nodeCopy.putNode(node, 0);

            node = nodeCopy;
            int previousTokens = getTokensSize();
            node.putNode(unaryOperators(), 1);
            if(previousTokens ==  getTokensSize()) {
                generateSpecificErrorAndThrowException("<expression> expected", node.getToken().getLine(), peekToken().getColumn());
            }
        }

        return node;
    }

    // !, -, +: unary operators
    private Node unaryOperators() throws Exception {
        Node node = null;

        node = postFixIncrementDecrement();

        if(compare(TokenType.NOT)) {
            if(node == null) {
                node = new Node(removeFirstToken(), 1);
                node.putNode(unaryOperators(), 0);
            }
        }

        return node;
    }

    // --, ++: after variable
    private Node postFixIncrementDecrement() throws Exception {
        Node node = null;

        node = preFixIncrementDecrement();
        if(compare(TokenType.SUBTRACT_SUBTRACT) || compare(TokenType.ADD_ADD)) {
            if(node != null) {
                if(node.getToken().getTokenType() != TokenType.ID) {
                    generateSpecificErrorAndThrowException("<identifier> expected", peekToken().getLine(), peekToken().getColumn());
                }
                Token token = removeFirstToken();
                if(token.getTokenType().compare(TokenType.SUBTRACT_SUBTRACT)) {
                    token.setTokenType(TokenType.SUBTRACT_SUBTRACT_POST);// TODO: review
                } else {
                    token.setTokenType(TokenType.ADD_ADD_POST);// TODO: review
                }
                node.setChildren(new Node[2]);
                node.putNode(new Node(token, 0), 0);
            } else {
                System.out.println("ERROR?");
            }
        }

        return node;
    }

    // --, ==: before variable
    private Node preFixIncrementDecrement() throws Exception {
        Node node = null;

        node = parenthesis();

        if(compare(TokenType.SUBTRACT_SUBTRACT) || compare(TokenType.ADD_ADD)) {
            // if node exists means something was found in section parenthesis(): and in postfix nothing should be found
            if(node != null)
                return node;

            node = new Node(removeFirstToken(), 1);

            Token token = removeTokenIfEqualsOrThrowException(TokenType.ID);
            node.putNode(new Node(token, 0), 0);

            if(compare(TokenType.ADD_ADD) || compare(TokenType.SUBTRACT_SUBTRACT)) {
                generateSpecificErrorAndThrowException("a variable cannot have postfix <--,++> and prefix<--,++> at the same time", peekToken().getLine(), peekToken().getColumn());
            }
        } else if (compare(TokenType.SUBTRACT) || compare(TokenType.ADD)) {
            if(node != null)
                return node;

            Token currentToken = removeFirstToken();
            // this - and + represents a unary +exp, -(exp)
            if (currentToken.getTokenType().compare(TokenType.SUBTRACT)) {
                currentToken.setTokenType(TokenType.UNARY_SUBTRACT);
            } else {
                currentToken.setTokenType(TokenType.UNARY_ADD);
            }

            node = new Node(currentToken, 1);
            int previousTokens =  getTokensSize();
            node.putNode(parenthesis(), 0);
            if(previousTokens ==  getTokensSize()) {
                generateSpecificErrorAndThrowException("<expression> expected", node.getToken().getLine(), peekToken().getColumn());
            }
        }

        return node;
    }

    //(), [] or variables
    private Node parenthesis() throws Exception {
        Node node = null;

        switch (peekToken().getTokenType()) {
            case PARENTHESIS_OPEN:
                removeFirstToken();
                node = booleanOperatorsFourthGrade();
                removeTokenIfEqualsOrThrowException(TokenType.PARENTHESIS_CLOSE);
                break;
            case STRING:
            case INT_NUMBER:
            case FLOAT_NUMBER:
            case ID:
            case TRUE:
            case FALSE:
                node = new Node(removeFirstToken(), 0);
                break;
            case LOGICAL_OR:
            case LOGICAL_AND:
            case OR:
            case AND:
                //System.out.println("ERROR?: " + peekToken().getTokenType());// TODO
                break;
            default:
                //System.out.println("Allowed?: " + peekToken().getLexeme());// TODO
                break;
        }

        return node;
    }

    private Node booleanExpressionList() throws Exception {
        Node node = null;

        removeTokenIfEqualsOrThrowException(TokenType.PARENTHESIS_OPEN);
        int tokensSize =  getTokensSize();
        node = logicalSecondGrade();
        if (tokensSize == getTokensSize()) {
            generateSpecificErrorAndThrowException("<expression> expected after", peekToken().getLine(), peekToken().getColumn());
        }
        removeTokenIfEqualsOrThrowException(TokenType.PARENTHESIS_CLOSE);

        return node;
    }

    private Node block() throws Exception {
        Node node = null;

        removeTokenIfEqualsOrThrowException(TokenType.BRACES_OPEN);
        node = new Node(new Token(TokenType.CONTEXT, TokenType.CONTEXT.getTokenName(), peekToken().getLine(), peekToken().getColumn()),1);
        node.putNode(codeLoop(), 0);
        removeTokenIfEqualsOrThrowException(TokenType.BRACES_CLOSE);

        return node;
    }

    private boolean compare(TokenType tokenType) {
        boolean isSameType = false;
        if (!tokens.isEmpty()) {
            if(tokens.getFirst().getTokenType() == tokenType) {
                isSameType = true;
            }
        }

        return isSameType;
    }

    private void generateSpecificErrorAndThrowException(String message, int line, int column) throws Exception {
        
        Token emptyToken = new Token(TokenType.ERROR, message, line, column);
        errorTokens.add(emptyToken);

        throw new Exception();
    }

    private Token removeTokenIfEqualsOrThrowException(TokenType tokenType) throws Exception  {
        Token token = null;
        if (!tokens.isEmpty()) {
            if(tokens.getFirst().getTokenType() == tokenType) {
                token = tokens.getFirst();
                nextToken();
            }
        }

        if(token == null && !tokens.isEmpty()) {
            
            errorTokens.add(new Token(TokenType.ERROR, "<" + tokenType.getTokenName() + "> expected", peekToken().getLine(), peekToken().getColumn()));
            throw new Exception("<" + tokenType.getTokenName() + "> expected");
        }

        return token;
    }

    private void validatePreviousTokenSize(int previousSize, String errorMessage) throws Exception {
        if(getTokensSize() == previousSize) {
            
            errorTokens.add(new Token(TokenType.ERROR, errorMessage, peekToken().getLine(), peekToken().getColumn()));
            throw new Exception();
        }
    }

    private int getTokensSize() {
        return tokens.size();
    }

    private Token peekToken() {
        return tokens.peek();
    }

    private Token removeFirstToken() {
        return tokens.removeFirst();
    }

    private Token nextToken() {
        return tokens.removeFirst();
    }

}
