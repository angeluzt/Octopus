package org.octopus.analysers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.octopus.enums.TokenType;
import org.octopus.structure.Token;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LexicalAnalyserTest {

    //@InjectMocks
    //class under test

    @Mock
    LexicalAnalyser lexicalAnalyser;

    @Test
    public void testSpecificCode() throws Exception {
        LinkedList<Token> mockResponse = new LexicalAnalyser().analyze("int i=12,j=3;");

        assertEquals(mockResponse.size(), 9);
        assertEquals(mockResponse.peek().getTokenType().getTokenType(), "keyword");
        assertEquals(mockResponse.pop().getLexeme(), "int");

        assertEquals(mockResponse.peek().getLexemeType(), "literal");
        assertEquals(mockResponse.pop().getLexeme(), "i");

        assertEquals(mockResponse.peek().getLexemeType(), "operator");
        assertEquals(mockResponse.pop().getLexeme(), "=");

        assertEquals(mockResponse.peek().getLexemeType(), "literal");
        assertEquals(mockResponse.pop().getLexeme(), "12");

        assertEquals(mockResponse.peek().getLexemeType(), "separator");
        assertEquals(mockResponse.pop().getLexeme(), ",");

        assertEquals(mockResponse.peek().getLexemeType(), "literal");
        assertEquals(mockResponse.pop().getLexeme(), "j");

        assertEquals(mockResponse.peek().getLexemeType(), "operator");
        assertEquals(mockResponse.pop().getLexeme(), "=");

        assertEquals(mockResponse.peek().getLexemeType(), "literal");
        assertEquals(mockResponse.pop().getLexeme(), "3");

        assertEquals(mockResponse.peek().getLexemeType(), "separator");
        assertEquals(mockResponse.pop().getLexeme(), ";");
    }

    @Test
    void testMocks() throws Exception {
        LinkedList<Token> mockToken = new LinkedList<>();
        mockToken.add(new Token(TokenType.ID, "abcd", 12));
        when(lexicalAnalyser.analyze(anyString())).thenReturn(mockToken);


        LinkedList<Token> mockResponse = lexicalAnalyser.analyze("");

        mockResponse.peek();
    }
}
