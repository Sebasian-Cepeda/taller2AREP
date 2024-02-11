package juan.taller2;

import java.io.IOException;

import juan.taller2.apiMovie.WebServer;

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
            WebServer.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}