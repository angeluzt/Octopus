package org.octopus.util;

import org.octopus.structure.Node;

public class Three {
    private int ident = 0;
    private int cont = 0;
    public void convertNodeToString(StringBuilder code, Node syntacticTree) {
        cont++;
        ident+=2;
        while(syntacticTree != null) {

            for (int i = 0; i<ident-1; i++) {
                code.append(i==0?"":" ");
                System.out.print(i==0?"":" ");
            }
            code.append("|__");
            System.out.print("|__");
            code.append(syntacticTree.getToken().getTokenType().name() + ": " + syntacticTree.getToken().getLexeme() +"\n");
            System.out.println(syntacticTree.getToken().getTokenType().name() + ": " + syntacticTree.getToken().getLexeme() +"\n");
            for (int i =0; i<syntacticTree.getChildren().length; i++) {
                convertNodeToString(code, syntacticTree.getChildren()[i]);
            }
            syntacticTree = syntacticTree.getBrother();
        }
        ident-=2;
        cont--;
    }
}
