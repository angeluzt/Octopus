package org.octopus.analysers;

import org.octopus.util.Constants;
import org.octopus.enums.LexicalState;
import org.octopus.enums.TokenType;
import org.octopus.structure.Token;

import java.util.LinkedList;

import static org.octopus.enums.LexicalState.BLOCK_COMMENT;
import static org.octopus.enums.LexicalState.END_BLOCK_COMMENT;
import static org.octopus.enums.LexicalState.FRACTION;
import static org.octopus.enums.LexicalState.HAS_EQUALS_IN_ADD;
import static org.octopus.enums.LexicalState.HAS_EQUALS_IN_DIFFERENT;
import static org.octopus.enums.LexicalState.HAS_EQUALS_IN_DIVIDE;
import static org.octopus.enums.LexicalState.HAS_EQUALS_IN_EQUALS;
import static org.octopus.enums.LexicalState.HAS_EQUALS_IN_HIGHER_THAN;
import static org.octopus.enums.LexicalState.HAS_EQUALS_IN_LOWER_THAN;
import static org.octopus.enums.LexicalState.HAS_EQUALS_IN_MULTIPLY;
import static org.octopus.enums.LexicalState.HAS_EQUALS_IN_SUBTRACT;
import static org.octopus.enums.LexicalState.INITIAL;
import static org.octopus.enums.LexicalState.LOGIC_AND;
import static org.octopus.enums.LexicalState.LOGIC_OR;
import static org.octopus.enums.LexicalState.NUMBER;
import static org.octopus.enums.LexicalState.REGULAR_COMMENT;
import static org.octopus.enums.LexicalState.STRING;
import static org.octopus.enums.LexicalState.VAR;
import static org.octopus.enums.LexicalState.VERIFY_COMMENT;

public class LexicalAnalyser implements Analyser<String, LinkedList<Token>> {

    /**
     * Receive code in String format and tokenize the code.
     *
     * @param code code in string format
     * @return list of tokens that represents the code
     * @throws Exception
     */
    public LinkedList<Token> analyze(String code) throws Exception {
        code += " ";
        char []letters = code.toCharArray();

        int lineNumber = 1;
        int column = 0;
        boolean isFraction = false;
        LexicalState state = LexicalState.INITIAL;
        StringBuilder currentToken = new StringBuilder();
        LinkedList<Token> tokensResponse = new LinkedList<>();

        for (int index = 0;index < letters.length; index++) {
            column++;
            ////System.out.println("Current: >" + letters[index] + "<");
            switch (state) {
                case INITIAL:
                    // ignore blank characters
                    if(letters[index] == '\r') {

                    } else if (isSpace(letters[index])) {
                            if(letters[index] == '\n') {
                                lineNumber++;
                                column = 0;
                                //System.out.println("Current line: " + lineNumber);
                            }
                        } else if (Character.isLetter(letters[index]) || letters[index] == '_') {
                            //System.out.println("Start: text search");
                            currentToken.append(letters[index]);
                            state = VAR;
                        } else if (Character.isDigit(letters[index])) {
                            //System.out.println("Start: digit search");
                            currentToken.append(letters[index]);
                            state = NUMBER;
                        } else if (letters[index] == '/') {
                            //System.out.println("Start: verify comment");
                            state = VERIFY_COMMENT;
                        } else if (letters[index] == '+') {
                            //System.out.println("Start: add logic");
                            state = HAS_EQUALS_IN_ADD;
                        } else if (letters[index] == '-') {
                            ////System.out.println("Start: verify comment");
                            //state = VERIFY_COMMENT;
                            state = HAS_EQUALS_IN_SUBTRACT;
                        } else if (letters[index] == '*') {
                            ////System.out.println("Start: verify comment");
                            state = HAS_EQUALS_IN_MULTIPLY;
                        } else if (letters[index] == '>') {
                            ////System.out.println("Start: verify comment");
                            state = HAS_EQUALS_IN_HIGHER_THAN;
                        } else if (letters[index] == '<') {
                            ////System.out.println("Start: verify comment");
                            state = HAS_EQUALS_IN_LOWER_THAN;
                        } else if (letters[index] == '=') {
                            ////System.out.println("Start: verify comment");
                            state = HAS_EQUALS_IN_EQUALS;
                        } else if (letters[index] == '!') {
                            ////System.out.println("Start: verify comment");
                            state = HAS_EQUALS_IN_DIFFERENT;
                        } else if (letters[index] == '"') {
                            state = STRING;
                        }  else if (letters[index] == ';') {
                            //System.out.println("; found");
                            tokensResponse.addLast(getTokenInstance(TokenType.SEMICOLON, lineNumber, column));
                        } else if (letters[index] == ',') {
                            //System.out.println(", found");
                            tokensResponse.addLast(getTokenInstance(TokenType.COMMA, lineNumber, column));
                        }  else if (letters[index] == '{') {
                            //System.out.println("{ found");
                            tokensResponse.addLast(getTokenInstance(TokenType.BRACES_OPEN, lineNumber, column));
                        } else if (letters[index] == '}') {
                            //System.out.println("} found");
                            tokensResponse.addLast(getTokenInstance(TokenType.BRACES_CLOSE, lineNumber, column));
                        } else if (letters[index] == '(') {
                            //System.out.println("( found");
                            tokensResponse.addLast(getTokenInstance(TokenType.PARENTHESIS_OPEN, lineNumber, column));
                        } else if (letters[index] == ')') {
                            //System.out.println(") found");
                            tokensResponse.addLast(getTokenInstance(TokenType.PARENTHESIS_CLOSE, lineNumber, column));
                        } else if (letters[index] == '[') {
                            //System.out.println("[ found");
                            tokensResponse.addLast(getTokenInstance(TokenType.BRACKET_OPEN, lineNumber, column));
                        } else if (letters[index] == ']') {
                            //System.out.println("] found");
                            tokensResponse.addLast(getTokenInstance(TokenType.BRACKET_CLOSE, lineNumber, column));
                        } else if (letters[index] == '&') {
                            state = LOGIC_AND;
                            //System.out.println("& and found");
                            //tokensResponse.addLast(getTokenInstance(TokenType.AND, lineNumber, column));
                        } else if (letters[index] == '|') {
                            state = LOGIC_OR;
                            //System.out.println("| or found");
                            //tokensResponse.addLast(getTokenInstance(TokenType.OR, lineNumber, column));
                        } else if (letters[index] == '?') {
                            //System.out.println("? or found");
                            tokensResponse.addLast(getTokenInstance(TokenType.TERNARY, lineNumber, column));
                        } else {
                            //System.out.println(letters[index] + " found???");
                            tokensResponse.addLast(getTokenInstanceLiterals(TokenType.UNKNOWN, letters[index] + "", lineNumber, column));
                            throw new Exception("Unknown character: >" + letters[index] + "<");
                        }

                    break;
                case NUMBER:
                    if (Character.isDigit(letters[index])) {
                        currentToken.append(letters[index]);
                        //System.out.println(">digit search: " + currentToken);
                    } else if (letters[index] == '.' && !isFraction) {
                        //System.out.println(">possible fraction found");
                        state = FRACTION;
                    } else {

                        if(isFraction) {
                            //System.out.println(">final fraction found: " + currentToken);
                            tokensResponse.addLast(getTokenInstanceLiterals(TokenType.FLOAT_NUMBER, currentToken.toString(), lineNumber, column));
                        } else {
                            //System.out.println(">regular number found: " + currentToken);
                            tokensResponse.addLast(getTokenInstanceLiterals(TokenType.INT_NUMBER, currentToken.toString(), lineNumber, column));
                        }
                        isFraction = false;
                        state = INITIAL;
                        currentToken.setLength(0);
                        // move to previous character in order to tokenize the character used to define the end of the number
                        // because it can be /, +, -, etc
                        index--;
                        column--;
                    }
                    break;
                case FRACTION:
                    if (Character.isDigit(letters[index])) {
                        isFraction = true;
                        state = NUMBER;
                        currentToken.append('.').append(letters[index]);
                        //System.out.println(">fraction found: " + currentToken);
                    } else {
                        throw new Exception("float number >" + currentToken + "< cannot contain this character: '" + letters[index] + "' after the: .");
                    }
                    break;
                case VAR:
                    if (Character.isLetter(letters[index]) || Character.isDigit(letters[index]) || letters[index] == '_') {
                        currentToken.append(letters[index]);
                        //System.out.println(">text search: " + currentToken);
                    } else {
                        //System.out.println(">final var found: " + currentToken);
                        tokensResponse.addLast(getTokenInstanceId(TokenType.ID, currentToken.toString(), lineNumber, column));

                        state = INITIAL;
                        currentToken.setLength(0);
                        // move to previous character, must not be ignored.
                        index--;
                        column--;
                    }
                    break;
            case STRING:
                    if (letters[index] != '"') {
                        currentToken.append(letters[index]);
                        //System.out.println(">text search: " + currentToken);
                    } else {
                        //System.out.println(">final text found: " + currentToken);
                        //tokensResponse.addLast(getTokenInstanceLiteralsText(TokenType.STRING, currentToken.toString(), lineNumber, column));
                        tokensResponse.addLast(getTokenInstanceLiterals(TokenType.STRING, currentToken.toString(), lineNumber, column));

                        state = INITIAL;
                        currentToken.setLength(0);
                        // move to previous character, must not be ignored.
                        //index--;
                    }
                    break;
                case VERIFY_COMMENT:
                    if (letters[index] == '/') {
                        //System.out.println(">regular comment found");
                        state = REGULAR_COMMENT;
                    } else if (letters[index] == '*') {
                        //System.out.println(">block comment found");
                        state = BLOCK_COMMENT;
                    } else {
                        state = HAS_EQUALS_IN_DIVIDE;
                        // move to previous character,  we already know we found a: / but current char must be reevaluated
                        index--;
                    }
                    break;
                case REGULAR_COMMENT:
                    if (letters[index] != '\n') {
                        //System.out.println(">>ignoring regular character: " + letters[index]);
                    } else {
                        //System.out.println(">>end regular comment");
                        lineNumber++;
                        column = 0;
                        //System.out.println("Current line: " + lineNumber);
                        state = INITIAL;
                    }
                    break;
                case BLOCK_COMMENT:
                    if (letters[index] == '*') {
                        state = END_BLOCK_COMMENT;
                    } else {
                        if(letters[index] == '\n') {
                            lineNumber++;
                            column = 0;
                        }
                        //System.out.println(">>ignoring block character: " + letters[index]);
                    }
                    break;
                case END_BLOCK_COMMENT:
                    if (letters[index] == '/') {
                        //System.out.println(">>end block comment");
                        state = INITIAL;
                    } else {
                        // return to block comment and keep ignoring
                        state = BLOCK_COMMENT;
                        if(letters[index] == '\n') {
                            lineNumber++;
                            column = 0;
                        }
                    }
                    break;
                case HAS_EQUALS_IN_ADD:
                    if (letters[index] == '=') {
                        //System.out.println("+= found");
                        tokensResponse.addLast(getTokenInstance(TokenType.ADD_EQUALS, lineNumber, column));
                    } else if (letters[index] == '+') {
                        //System.out.println("++ found");
                        tokensResponse.addLast(getTokenInstance(TokenType.ADD_ADD, lineNumber, column));
                    } else {
                        //System.out.println("+ found");
                        tokensResponse.addLast(getTokenInstance(TokenType.ADD, lineNumber, column));
                        // move to previous character, must not be ignored. But we already know we found a: +
                        index--;
                        column--;
                    }
                    state = INITIAL;
                    break;
                case HAS_EQUALS_IN_SUBTRACT:
                    if (letters[index] == '=') {
                        //System.out.println("-= found");
                        tokensResponse.addLast(getTokenInstance(TokenType.SUBTRACT_EQUALS, lineNumber, column));
                    } else if (letters[index] == '-') {
                        //System.out.println("-- found");
                        tokensResponse.addLast(getTokenInstance(TokenType.SUBTRACT_SUBTRACT, lineNumber, column));
                    } else {
                        //System.out.println("- found");
                        tokensResponse.addLast(getTokenInstance(TokenType.SUBTRACT, lineNumber, column));
                        // move to previous character, must not be ignored. But we already know we found a: -
                        index--;
                        column--;
                    }
                    state = INITIAL;
                    break;
                case HAS_EQUALS_IN_LOWER_THAN:
                    if (letters[index] == '=') {
                        //System.out.println("<= found");
                        tokensResponse.addLast(getTokenInstance(TokenType.LOWER_OR_EQUALS, lineNumber, column));
                    } else {
                        //System.out.println("< found");
                        tokensResponse.addLast(getTokenInstance(TokenType.LOWER, lineNumber, column));
                        // move to previous character, must not be ignored. But we already know we found a: <
                        index--;
                        column--;
                    }
                    state = INITIAL;
                    break;
                case HAS_EQUALS_IN_HIGHER_THAN:
                    if (letters[index] == '=') {
                        //System.out.println(">= found");
                        tokensResponse.addLast(getTokenInstance(TokenType.HIGHER_OR_EQUALS, lineNumber, column));
                    } else {
                        //System.out.println("> found");
                        tokensResponse.addLast(getTokenInstance(TokenType.HIGHER, lineNumber, column));
                        // move to previous character, must not be ignored. But we already know we found a: >
                        index--;
                        column--;
                    }
                    state = INITIAL;
                    break;
                case HAS_EQUALS_IN_EQUALS:
                    if (letters[index] == '=') {
                        //System.out.println("== found");
                        tokensResponse.addLast(getTokenInstance(TokenType.EQUALS, lineNumber, column));
                    } else {
                        //System.out.println("= found");
                        tokensResponse.addLast(getTokenInstance(TokenType.ASSIGN, lineNumber, column));
                        // move to previous character, must not be ignored. But we already know we found a: =
                        index--;
                        column--;
                    }
                    state = INITIAL;
                    break;
                case HAS_EQUALS_IN_DIFFERENT:
                    if (letters[index] == '=') {
                        //System.out.println("!= found");
                        tokensResponse.addLast(getTokenInstance(TokenType.DIFFERENT, lineNumber, column));
                    } else {
                        //System.out.println("! found");
                        tokensResponse.addLast(getTokenInstance(TokenType.NOT, lineNumber, column));
                        // move to previous character, must not be ignored. But we already know we found a: !
                        index--;
                        column--;
                    }
                    state = INITIAL;
                    break;
                case HAS_EQUALS_IN_MULTIPLY:
                    if (letters[index] == '=') {
                        //System.out.println("*= found");
                        tokensResponse.addLast(getTokenInstance(TokenType.MULTIPLY_EQUALS, lineNumber, column));
                    } else {
                        //System.out.println("* found");
                        tokensResponse.addLast(getTokenInstance(TokenType.MULTIPLY, lineNumber, column));
                        // move to previous character, must not be ignored. But we already know we found a: *
                        index--;
                        column--;
                    }
                    state = INITIAL;
                    break;
                case HAS_EQUALS_IN_DIVIDE:
                    if (letters[index] == '=') {
                        //System.out.println("/= found");
                        tokensResponse.addLast(getTokenInstance(TokenType.DIVIDE_EQUALS, lineNumber, column));
                    } else {
                        //System.out.println("/ found");
                        tokensResponse.addLast(getTokenInstance(TokenType.DIVIDE, lineNumber, column));
                        // move to previous character, must not be ignored. But we already know we found a: /
                        index--;
                        column--;
                    }
                    state = INITIAL;
                    break;
                case LOGIC_AND:
                    if (letters[index] == '&') {
                        //System.out.println("&& found");
                        tokensResponse.addLast(getTokenInstance(TokenType.LOGICAL_AND, lineNumber, column));
                    } else {
                        //System.out.println("& found");
                        tokensResponse.addLast(getTokenInstance(TokenType.AND, lineNumber, column));
                        // move to previous character, must not be ignored. But we already know we found a: /
                        index--;
                        column--;
                    }
                    state = INITIAL;
                    break;
                case LOGIC_OR:
                    if (letters[index] == '|') {
                        //System.out.println("|| found");
                        tokensResponse.addLast(getTokenInstance(TokenType.LOGICAL_OR, lineNumber, column));
                    } else {
                        //System.out.println("| found");
                        tokensResponse.addLast(getTokenInstance(TokenType.OR, lineNumber, column));
                        // move to previous character, must not be ignored. But we already know we found a: /
                        index--;
                        column--;
                    }
                    state = INITIAL;
                    break;
            }
        }

        return tokensResponse;
    }

    private Token getTokenInstance(TokenType tokenType, int lineNumber, int column) {
        return new Token(tokenType, tokenType.getTokenName(), lineNumber, column);
    }

    private Token getTokenInstanceLiterals(TokenType tokenType, String lexeme, int lineNumber, int column) {
        return new Token(tokenType, lexeme, lineNumber, column);
    }

    private Token getTokenInstanceId(TokenType tokenType, String lexeme, int lineNumber, int column) {
        if(Constants.reservedWords.containsKey(lexeme)) {
            return new Token(Constants.reservedWords.get(lexeme), lexeme, lineNumber, column);
        } else {
            return new Token(tokenType, lexeme, lineNumber, column);
        }
    }

    private boolean isSpace(char currentChar) {
        if(currentChar == ' ' || currentChar == '\t' || currentChar == '\n') {
            return true;
        } else {
            return false;
        }
    }
}
