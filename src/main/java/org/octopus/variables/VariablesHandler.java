package org.octopus.variables;

import org.octopus.enums.TokenType;

import java.util.HashMap;

/**
 * This class is in charge of the code context, that means that can help us to determine if a variable being created
 * already exists in previous context.
 *
 * The context key is an Integer, this number is linked to a HashMap that can contain multiple variables and a value.
 * Example:
 * After "{" a context can be created as 1  and all the variables are added to it
 * --------------------------------
 * {//context:1 =  main context
 *     { //context:2
 *     int a = 9;
 *        { //context 3
 *            int a = 0;
 *        }
 *     }
 * int a = 234;
 * }
 * --------------------------------
 * - In the context:2 "a" is and is not an error, because variable "a", do not exist before
 * - The "a" variable declared context:3 is an error, because "a" already exists in context:2,
 *   and the same variable cannot exist in the same context or in nested contexts
 * - BUT the last "a" = 124; is not an error because variables from context:2 and context:3 do not exist outside
 *   its own {} context.
 */
public class VariablesHandler {
    HashMap<Integer, HashMap<String, Variable>> variables = new HashMap<>();

    /**
     * Create a new context inside the variables HashMap
     *
     * @param contextIndex id of the new context that should be created in the context handler
     */
    public void addNewContext(Integer contextIndex) {
        HashMap<String, Variable> context = new HashMap<>();
        if(!variables.containsKey(contextIndex)) {
            variables.put(contextIndex, context);
        }
    }

    /**
     * Delete an existing context
     * @param contextIndex id of the context that should be created
     */
    public void deleteContext(Integer contextIndex) {
        variables.remove(contextIndex);
    }

    /**
     * When given a variable name and a context id, get the variable if exists inside the contextIndex
     * @param name variable name
     * @param contextIndex index of the required context
     * @return the variable found, or UNKNOWN if variable do not exist
     */
    public Variable getVariableFromContext(String name, Integer contextIndex) {
        HashMap<String, Variable> currentContext = variables.get(contextIndex);
        if(currentContext != null && !currentContext.isEmpty() && currentContext.containsKey(name)) {
            return currentContext.get(name);
        }
        return new Variable(null, TokenType.UNKNOWN);
    }

    /**
     * Get the variable if exists in current, or previous contexts, this means we want to know if the current variable
     * already exist in any of th nested contexts, because that's an error.
     * @param name variable name
     * @param contextIndex index of the most internal context index,
     * @return the variable if exists in current or previous contexts
     */
    public Variable getVariableIfExistsInSelectedContextAndNestedContexts(String name, Integer contextIndex) {
        while(contextIndex > 0) {
            if(variables.containsKey(contextIndex)) {
                if(variables.get(contextIndex).containsKey(name)) {
                    return variables.get(contextIndex).get(name);
                }
            }
            contextIndex--;
        }
        return null;
    }

    public boolean insertVariableInContextIfDoNotExists(String name, Object value, TokenType type, Integer contextIndex) {
        HashMap<String, Variable> currentContext = variables.get(contextIndex);

        // if variable is null, means not found and can be created
        if(getVariableIfExistsInSelectedContextAndNestedContexts(name, contextIndex) == null) {
            currentContext.put(name, new Variable(value, type));
            return false;
        } else {
            return true;
        }
    }
}
