package edu.escuelaing.arep.taller1;

import java.io.IOException;

import edu.escuelaing.arep.taller1.apiMovie.WebServer;

/**
 * Main class to start the application
 * 
 * @author Juan cepeda
 * 
 * 
 */
public class Main {
    public static void main(String[] args) {
        try {
            WebServer.startSever();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}