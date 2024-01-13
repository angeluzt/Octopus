package org.octopus.analysers;

import org.octopus.enums.TokenType;
import org.octopus.structure.Node;
import org.octopus.structure.Token;
import org.octopus.variables.VariablesHandler;

import java.util.LinkedList;


public class SemanticAnalyser implements Analyser<Node, LinkedList> {

    //private TokenType currentType;
    private VariablesHandler variables = new VariablesHandler();
    private LinkedList<Token> errorTokens = new LinkedList<>();
    @Override
    public LinkedList<Token> analyze(Node code) throws Exception {
        Node nodeCopy = code;

        iterateContextThree(nodeCopy.getChildrenAt(0), 0);

        return errorTokens;
    }

    private void contextManager(Node node, Integer ctx) {
        int localCtx = ctx;
        while (node != null) {

            for (int i = 0; i < node.getChildren().length; i++) {

                if (i==0 && node != null && node.getTokenType().equals(TokenType.CONTEXT)) {
                    contextManager(node.getChildrenAt(i), localCtx+1);
                } else {
                    System.out.println(node.getToken().getLexeme() + ", ctx: " + localCtx + ", line: " + node.getToken().getLine());
                    contextManager(node.getChildrenAt(i), localCtx);
                }
            }
            node = node.getBrother();
        }
    }

    private void iterateContextThree(Node node, Integer ctx) {
        int localCtx = ctx;

        while (node != null) {

            for (int i = 0; i < node.getChildren().length; i++) {

                if (i==0 && node != null && node.getTokenType().equals(TokenType.CONTEXT)) {
                    variables.addNewContext(localCtx+1);
                    iterateContextThree(node.getChildrenAt(0), localCtx + 1);
                    variables.deleteContext(localCtx+1);
                } else if (i==0 && node != null &&
                            (node.getTokenType().equals(TokenType.INT) ||
                                    node.getTokenType().equals(TokenType.FLOAT) ||
                                    node.getTokenType().equals(TokenType.TEXT) ||
                                    node.getTokenType().equals(TokenType.ASSIGN) ||
                                    node.getTokenType().equals(TokenType.BOOL))) {
                        analyseContextDeclarations(node.getChildrenAt(i), node.getTokenType(), localCtx);
                    } else if(node.getTokenType().equals(TokenType.FOR)) {
                        //Evaluate children from node
                        switch (i) {
                            case 0://for(<here>;;){}
                                // create future context
                                variables.addNewContext(localCtx+1);
                                // Next token will be a context: {}, then we decrease ctx because will be increased there
                                iterateContextThree(node.getChildrenAt(i), localCtx+1);
                                break;
                            case 1:
                                //System.out.println("For:1"+ ", ctx: " + localCtx);
                                break;
                            case 2:
                                //System.out.println("For:2"+ ", ctx: " + localCtx);
                                break;
                            case 3://for(;;){<here>}
                                iterateContextThree(node.getChildrenAt(i), localCtx);
                                break;
                        }
                    } else if(node.getTokenType().equals(TokenType.UNTIL)) {
                        switch (i){
                            case 0://until(<here>){}
                                System.out.println("until:0"+ ", ctx: " + localCtx);
                                break;
                            case 1://until(){<here>}
                                //System.out.println("until:1"+ ", ctx: " + localCtx);
                                // analyse declarations
                                iterateContextThree(node.getChildrenAt(i), localCtx);
                                break;
                        }
                    } else if(node.getTokenType().equals(TokenType.DO)) {
                        switch (i){
                            case 0://do{<here>}until();
                                // analyse declarations
                                iterateContextThree(node.getChildrenAt(i), localCtx);
                                break;
                            case 1://do{}until(<here>);
                                System.out.println("do-until:0");
                                break;
                        }
                    } else if(node.getTokenType().equals(TokenType.ID)) {

                        postOrderInExpressions(node, localCtx);
                        node = node.getChildrenAt(0);// TODO: Possible BUG
                    } else if(node.getTokenType().equals(TokenType.UNARY_ADD) ||
                            node.getTokenType().equals(TokenType.UNARY_SUBTRACT)) {

                        postOrderInExpressions(node, localCtx);

                    } else if(node.getTokenType().equals(TokenType.IF)) {
                        switch (i){
                            case 0://do{<here>}until();
                                postOrderInExpressions(node.getChildrenAt(0), localCtx);
                                //System.out.println("if:0 -> " + node.getChildrenAt(0).getTokenLexeme());
                                // analyse declarations
                                //iterateContextThree(node.getChildrenAt(i), localCtx);
                                break;
                            case 1://do{}until(<here>);
                                //System.out.println("if:1 -> " + node.getChildrenAt(1).getTokenLexeme());
                                iterateContextThree(node.getChildrenAt(1), localCtx);
                                break;
                        }
                        //postOrderInExpressions(node, localCtx);
                        //node = node.getChildrenAt(0);

                    } else if(node.getTokenType().equals(TokenType.ASSIGN)) {
                        //postOrderInExpressions(node, localCtx);
                    } else {
                        //if(node != null)
                        iterateContextThree(node.getChildrenAt(i), localCtx);
                    }
                //}
            }
            node = node.getBrother();
        }
    }

    private void analyseContextDeclarations(Node node, TokenType type, Integer ctx) {
        while (node != null) {
            if(node.getTokenType().compare(TokenType.ID)) {
                boolean isError = variables.insertVariableInContext(node.getToken().getLexeme(), null, type, ctx);
                if(isError) {
                    System.out.println( node.getToken().getLexeme() + " already declared");
                    addErrorVariable(node.getTokenLexeme(), node.getLine(), node.getColum());
                }
                // TODO: handle error when variable already exists in this or previous contexts
                if(node.getChildrenAt(0) != null) {
                    analyseContextDeclarations(node.getChildrenAt(0), type, ctx);
                }
            } else if (node.getTokenType().compare(TokenType.ASSIGN)) {
                TokenType currentType = postOrderInExpressions(node, ctx);

                if(currentType.compare(TokenType.UNKNOWN)) {
                    // variable not found
                } else if(!type.compare(postOrderInExpressions(node, ctx))){
                    addGenericError("expected value: " + type, node.getLine(), node.getColum());
                }
            }

            node = node.getBrother();
        }
    }

    //TokenType status = TokenType.UNKNOWN;
    private TokenType postOrderInExpressions(Node node, Integer context) {
        /*if(status == TokenType.ERROR) {
            return null;
        }*/

        TokenType right;
        TokenType left;

        if (node.getTokenType().compare(TokenType.UNARY_ADD) ||
                node.getTokenType().compare(TokenType.UNARY_SUBTRACT)) {

            // unary plus: +2, minus: -sa,
            left = postOrderInExpressions(node.getChildrenAt(0), context);
            switch (left) {
                case INT:
                    return TokenType.INT;
                case FLOAT:
                    return TokenType.FLOAT;
                default:
                    //System.out.println("ERROR 1: unary error, only float or int allowed after -, +");
                    addGenericError("unary error, only ids (float or int) allowed after: " + node.getTokenLexeme(), node.getLine(), node.getColum());
                    return TokenType.UNKNOWN;//TODO: ????
            }
            // handle arithmetic operators
        } else if (node.getTokenType() == TokenType.MULTIPLY ||
                node.getTokenType() == TokenType.DIVIDE ||
                node.getTokenType() == TokenType.SUBTRACT) {

            right = postOrderInExpressions(node.getChildrenAt(0), context);
            left =  postOrderInExpressions(node.getChildrenAt(1), context);
            if(right.compare(left)) {
                // return the token type when we already found final values and both are same type
                switch (right) {
                    case FLOAT:
                        return TokenType.FLOAT;
                    case INT:
                        return TokenType.INT;
                    default:
                        System.out.println("ERROR 1: only int, float allowed");
                        return TokenType.UNKNOWN;//TODO: ????
                }
            } else {
                // int +, - int changes to float
                if (left.compare(TokenType.FLOAT) && right.compare(TokenType.INT)
                        || left.compare(TokenType.INT) && right.compare(TokenType.FLOAT)) {
                    return TokenType.FLOAT;
                } else {
                    // ERROR: only numeric expression allowed TODO: handle error
                    System.out.println("ERROR 2: only primitives allowed");
                    return TokenType.UNKNOWN;
                }
            }
            // handle arithmetic operators, where text is allowed
        } else if (node.getTokenType() == TokenType.ADD){
            right = postOrderInExpressions(node.getChildrenAt(0), context);
            left =  postOrderInExpressions(node.getChildrenAt(1), context);
            if(right.compare(left)) {
                // return the token type when we already found final values and both are same type
                switch (left) {
                    case FLOAT:
                        return TokenType.FLOAT;
                    case INT:
                        return TokenType.INT;
                    case TEXT:
                        return TokenType.TEXT;
                    default:
                        System.out.println("ERROR 1: only int, float allowed");
                        return TokenType.UNKNOWN;//TODO: ????
                }
            } else {
                // int + float changes to float
                if (left.compare(TokenType.FLOAT) && right.compare(TokenType.INT)
                        || left.compare(TokenType.INT) && right.compare(TokenType.FLOAT)) {
                    return TokenType.FLOAT;
                    // int, float + text changes to text
                } else if (left.compare(TokenType.TEXT) && right.compare(TokenType.INT)
                            || left.compare(TokenType.INT) && right.compare(TokenType.TEXT)
                            || left.compare(TokenType.TEXT) && right.compare(TokenType.FLOAT)
                            || left.compare(TokenType.FLOAT) && right.compare(TokenType.TEXT)) {
                    return TokenType.TEXT;
                }  else {
                    // ERROR: only numeric expression allowed TODO: handle error
                    System.out.println("ERROR 2: only int, float, text allowed when using +");
                    return TokenType.UNKNOWN;
                }
            }
            // handle comparators where only int, float is allowed
        } else if (node.getTokenType() == TokenType.HIGHER ||
                node.getTokenType() == TokenType.HIGHER_OR_EQUALS ||
                node.getTokenType() == TokenType.LOWER ||
                node.getTokenType() == TokenType.LOWER_OR_EQUALS) {

            right = postOrderInExpressions(node.getChildrenAt(0), context);
            left =  postOrderInExpressions(node.getChildrenAt(1), context);
            if(right.compare(left)) {
                // return the token type when we already found final values and both are same type
                switch (left) {
                    case INT:
                    case FLOAT:
                        return TokenType.BOOL;
                    default:
                        System.out.println("ERROR 1: only boolean expression allowed");
                        return TokenType.UNKNOWN;//TODO: ????
                }
            } else {
                if (left.compare(TokenType.INT) && right.compare(TokenType.FLOAT)
                    || left.compare(TokenType.FLOAT) && right.compare(TokenType.INT)) {
                    return TokenType.BOOL;
                } else {
                    // TODO: ERROR: only boolean expression allowed
                    System.out.println("ERROR 2: only boolean expression allowed");
                    return TokenType.UNKNOWN;
                }
            }
        } else if (node.getTokenType() == TokenType.EQUALS
                || node.getTokenType() == TokenType.DIFFERENT) {

            right = postOrderInExpressions(node.getChildrenAt(0), context);
            left =  postOrderInExpressions(node.getChildrenAt(1), context);
            if(right.compare(left)) {
                // return the token type when we already found final values and both are same type
                switch (left) {
                    case FLOAT:
                    case INT:
                    case TEXT:
                    case BOOL:
                        return TokenType.BOOL;
                    default:
                        System.out.println("ERROR 1: only boolean expression allowed");
                        return TokenType.UNKNOWN;//TODO: ????
                }
            } else {
                if (left.compare(TokenType.INT) && right.compare(TokenType.FLOAT)
                        || left.compare(TokenType.FLOAT) && right.compare(TokenType.INT)
                        //|| left.compare(TokenType.TRUE) && right.compare(TokenType.FALSE)
                        //|| left.compare(TokenType.FALSE) && right.compare(TokenType.TRUE)
                ) {
                    return TokenType.BOOL;
                } else {
                    // TODO: ERROR: only boolean expression allowed
                    System.out.println("ERROR 2: only boolean expression allowed");
                    return TokenType.UNKNOWN;
                }
            }
        }
        else if (node.getTokenType() == TokenType.AND ||
                node.getTokenType() == TokenType.OR ||
                node.getTokenType() == TokenType.LOGICAL_AND ||
                node.getTokenType() == TokenType.LOGICAL_OR) {

            right = postOrderInExpressions(node.getChildrenAt(0), context);
            left =  postOrderInExpressions(node.getChildrenAt(1), context);
            // &, |, &&, || only allows booleans
            if(right.compare(left) && right.compare(TokenType.BOOL)) {
                return TokenType.BOOL;
            } else {
                // TODO: ERROR: only boolean expression allowed
                System.out.println("ERROR 1: only boolean expression allowed");
                return TokenType.UNKNOWN;
            }
        } else if (node.getTokenType() == TokenType.NOT) {

            left = postOrderInExpressions(node.getChildrenAt(0), context);
            if(left.compare(TokenType.BOOL)) {
                return TokenType.BOOL;
            } else {
                // TODO: ERROR: only boolean expression allowed
                System.out.println("ERROR 2: only boolean expression allowed");
                return TokenType.UNKNOWN;
            }
        } else if (node.getTokenType() == TokenType.ADD_ADD ||
             node.getTokenType() == TokenType.SUBTRACT_SUBTRACT) {

            left = postOrderInExpressions(node.getChildrenAt(0), context);
            if(left.compare(TokenType.INT)) {
                return TokenType.INT;
            } else if (left.compare(TokenType.FLOAT)) {
                return TokenType.FLOAT;
            } else {
                // TODO: ERROR: only boolean expression allowed
                System.out.println("ERROR 2: only id, int, float allowed for prefix ++, --");
                return TokenType.UNKNOWN;
            }
        } else if (node.getTokenType() == TokenType.ASSIGN) {
            return postOrderInExpressions(node.getChildrenAt(0), context);
        }  else if (node.getTokenType() == TokenType.INT_NUMBER) {
            return TokenType.INT;
        }  else if (node.getTokenType() == TokenType.FLOAT_NUMBER) {
            return TokenType.FLOAT;
        }  else if (node.getTokenType() == TokenType.STRING) {
            return TokenType.TEXT;
        } else if (node.getTokenType() == TokenType.TRUE || node.getTokenType() == TokenType.FALSE) {
            return TokenType.BOOL;
        } else if (node.getTokenType().compare(TokenType.ID)) {
            if(!variables.isInsidePreviousContext(node.getTokenLexeme(), context)) {
                addGenericError("variable: " + node.getTokenLexeme() + " is not declared", node.getLine(), node.getColum());
                return TokenType.UNKNOWN;
            } else {
                TokenType idType = variables.getVariableFromContext(node.getTokenLexeme(), context).getType();

                // id can have preorder --, ++, but only allowed for int and float.
                switch (idType) {
                    case INT:
                    case FLOAT:
                        return idType;
                    case BOOL:
                    case TEXT:
                        if(node.getChildrenAt(0) != null) {
                            addGenericError("preorder: " + node.getChildrenAt(0).getTokenLexeme() + " not allowed in " + idType.getTokenName() + " variables", node.getLine(), node.getColum());
                            return TokenType.UNKNOWN;
                        } else {
                            return idType;
                        }
                       // break;
                }
            }
            /*variables.getVariableFromContext(node.getToken().getLexeme(), context);

            if(right.compare(left)) {
                // return the token type when we already found final values and both are same type
                switch (node.getTokenType()) {
                    case BOOL:
                        return TokenType.BOOL;

                    default:
                        System.out.println("ERROR 1: only boolean expression allowed");
                        return status = TokenType.ERROR;//TODO: ????
                }
            } else {
                // TODO: ERROR: only boolean expression allowed
                System.out.println("ERROR 2: only boolean expression allowed");
                return status = TokenType.ERROR;
            }*/
        } else {
            return TokenType.UNKNOWN;
        }
        /*else if (node.getTokenType() == TokenType.HIGHER ||
                node.getTokenType() == TokenType.HIGHER_OR_EQUALS ||
                node.getTokenType() == TokenType.LOWER ||
                node.getTokenType() == TokenType.LOWER_OR_EQUALS ||
                node.getTokenType() == TokenType.AND ||
                node.getTokenType() == TokenType.OR ||
                node.getTokenType() == TokenType.LOGICAL_AND ||
                node.getTokenType() == TokenType.LOGICAL_OR ||
                node.getTokenType() == TokenType.EQUALS ||
                node.getTokenType() == TokenType.DIFFERENT) {

            right = postOrderAnalysis(node.getChildren()[0]);
            left =  postOrderAnalysis(node.getChildren()[1]);
            if(right.compare(left)) {
                // return the token type when we already found final values and both are same type
                switch (node.getTokenType()) {
                    case BOOL:
                        return TokenType.BOOL;

                    default:
                        System.out.println("ERROR 1: only boolean expression allowed");
                        return status = TokenType.ERROR;//TODO: ????
                }
            } else {
                // TODO: ERROR: only boolean expression allowed
                System.out.println("ERROR 2: only boolean expression allowed");
                return status = TokenType.ERROR;
            }
        }*/

        return TokenType.UNKNOWN;
    }

    private void addErrorVariable(String varName, int line, int column) {
        String errorMessage = "variable: <" + varName + "> is already declared";
        System.out.println(errorMessage);
        errorTokens.add(new Token(TokenType.ERROR, errorMessage, line, column));
    }

    private void addGenericError(String errorMessage, int line, int column) {
        System.out.println(errorMessage);
        errorTokens.add(new Token(TokenType.ERROR, errorMessage, line, column));
    }
}
