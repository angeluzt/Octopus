package org.octopus.util;

import org.octopus.enums.TokenType;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String OPERATOR = "operator";
    public static final String KEYWORD = "keyword";
    public static final String LITERAL = "literal";
    public static final String LOGIC_OPERATOR = "logic_operator";
    public static final String SEPARATOR = "separator";
    public static final String UNKNOWN_TKN = "unknown";

    public static final String OCT_EXTENSION = "oct";
    public static final String CODE_LOCATION_PATH = "/oct";
    public static final String FOLDER_BUILD_NAME = "/build2";
    public static final String ERROR_PATH = FOLDER_BUILD_NAME + "/error/errors.octe";
    public static final String THREE_PATH = FOLDER_BUILD_NAME + "/three";
    public static final String BINARY_PATH = FOLDER_BUILD_NAME + "/binary";

    public static final String COMPILED_FILE_CODE = "c";
    public static final String THREE_FILE_CODE = "t";

    public static final Map<String, TokenType> reservedWords;

    static {
        reservedWords = new HashMap<>();
        reservedWords.put(TokenType.PROGRAM.getTokenName(), TokenType.PROGRAM);
        reservedWords.put(TokenType.ERROR.getTokenName(), TokenType.ERROR);
        reservedWords.put(TokenType.IF.getTokenName(), TokenType.IF);
        reservedWords.put(TokenType.THEN.getTokenName(), TokenType.THEN);
        reservedWords.put(TokenType.ELSE.getTokenName(), TokenType.ELSE);
        reservedWords.put(TokenType.FI.getTokenName(), TokenType.FI);
        reservedWords.put(TokenType.DO.getTokenName(), TokenType.DO);
        reservedWords.put(TokenType.UNTIL.getTokenName(), TokenType.UNTIL);
        reservedWords.put(TokenType.FOR.getTokenName(), TokenType.FOR);
        reservedWords.put(TokenType.WRITE.getTokenName(), TokenType.WRITE);
        reservedWords.put(TokenType.READ.getTokenName(), TokenType.READ);
        reservedWords.put(TokenType.INT.getTokenName(), TokenType.INT);
        reservedWords.put(TokenType.FLOAT.getTokenName(), TokenType.FLOAT);
        reservedWords.put(TokenType.BOOL.getTokenName(), TokenType.BOOL);
        reservedWords.put(TokenType.TRUE.getTokenName(), TokenType.TRUE);
        reservedWords.put(TokenType.FALSE.getTokenName(), TokenType.FALSE);
        reservedWords.put(TokenType.NULL.getTokenName(), TokenType.NULL);
        reservedWords.put(TokenType.TEXT.getTokenName(), TokenType.TEXT);
    }
}
