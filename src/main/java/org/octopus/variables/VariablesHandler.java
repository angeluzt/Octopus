package org.octopus.variables;

import org.octopus.enums.TokenType;

import java.util.HashMap;

public class VariablesHandler {
    HashMap<Integer, HashMap<String, Variable>> variables = new HashMap<>();

    public void addNewContext(Integer contextIndex) {
        HashMap<String, Variable> context = new HashMap<>();
        if(!variables.containsKey(contextIndex)) {
            variables.put(contextIndex, context);
        }
    }

    public void deleteContext(Integer contextIndex) {
        variables.remove(contextIndex);
    }

    public Variable getVariableFromContext(String name, Integer contextIndex) {
        HashMap<String, Variable> currentContext = variables.get(contextIndex);
        if(!currentContext.isEmpty()) {
            return currentContext.get(name);
        }
        return null;
    }

    public boolean isInsidePreviousContext(String name, Integer contextIndex) {
        //HashMap<String, Variable> currentContext = variables.get(contextIndex);
        while(contextIndex > 0) {
            if(variables.containsKey(contextIndex)) {
                if(variables.get(contextIndex).containsKey(name)) {
                    return true;
                }
            }
            contextIndex--;
        }
        /*if(!currentContext.isEmpty()) {
            return currentContext.get(name);
        }*/
        return false;
    }

    public boolean insertVariableInContext(String name, Object value, TokenType type, Integer contextIndex) {
        HashMap<String, Variable> currentContext = variables.get(contextIndex);
        //if(currentContext != null) {
        if(!isInsidePreviousContext(name, contextIndex)) {
            currentContext.put(name, new Variable(value, type));
            return false;
        } else {
            return true;
        }
    }
}
