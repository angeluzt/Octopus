package org.octopus;

import org.octopus.util.Compiler;
import org.octopus.util.Constants;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {
        //String currentPath = System.getProperty("user.dir");
        new Compiler().compile();
        // Print the current working directory
        //System.out.println("Current Path: " + currentPath);
        /*if(args != null && args.length > 0) {

            if(args.length < 2) {
                System.out.println("Error: Missing arguments");
                System.out.println("In order to compile is required:");
                System.out.println("octoc folder/folder/file.oct");
                return;
            } else {
                if (args[0].equals("octoc")) {
                    if (args[1] == "-p") {
                        new Compiler().compile(args[2]);
                    } else {
                        new Compiler().compile(Constants.SOURCE_PATH);
                    }
                } else {
                    System.out.println("Instruction not found: " + args[0]);
                    System.out.println("Possible instructions: octoc or octoe");
                    return;
                }
            }
        }*/
    }
}
