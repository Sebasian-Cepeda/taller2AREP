package edu.escuelaing.arep.taller2.apiMovie;

import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.*;

import java.io.*;

/**
 * Web server class to use the web application
 * 
 * @author juan cepeda
 */
public class WebServer {

    private static final int PORT = 35000;
    private static final APIMovies apiMovie = new APIMovies();

    /**
     * Method that start the web server
     * 
     * @throws IOException
     */
    public static void startSever() throws IOException {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;
            boolean readingFirst = true;
            String petition = "";

            while ((inputLine = in.readLine()) != null) {

                if (readingFirst) {
                    petition = inputLine.split(" ")[1];
                    readingFirst = false;
                }

                if (!in.ready()) {
                    break;
                }
            }

            outputLine = (petition.startsWith("/film")) ? movieInfo(petition.replace("/film?name=", ""))
                    : mainPage(petition);

            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }

        serverSocket.close();
    }

    /**
     * return a html structure with some movie information
     * 
     * @param name the name of the movie
     * @return a html structure with movie information
     */
    private static String movieInfo(String name) {
        try {
            JsonObject resp = apiMovie.searchMovie(name);
            JsonElement title = resp.get("Title"), poster = resp.get("Poster"), director = resp.get("Director"),
                    plot = resp.get("Plot");

            String outputLine = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type:text/html\r\n"
                    + "\r\n"
                    + getStaticFile("/movieInfo.html").replace("{Title}", title.toString())
                            .replace("\"{Poster}\"", poster.toString()).replace("{Directors}", director.toString())
                            .replace("{Plot}", plot.toString());

            return outputLine;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * method that returns the principal html page
     * 
     * @return the principal page of the application
     */
    private static String mainPage(String file) {

        String mimeType = getMimeType(file);
        String resp = getStaticFile(file);
        String outputLine = "HTTP/1.1 200 OK\r\n"
                + "Content-Type:" + mimeType + "\r\n"
                + "\r\n"
                + resp;

        return outputLine;
    }

    private static String getMimeType(String file) {
        return (file.endsWith(".html") || file.endsWith("/")) ? "text/html"
                : ((file.endsWith(".css")) ? "text/css"
                        : (file.endsWith(".js")) ? "application/javascript"
                                : (file.endsWith(".jpg")) ? "image  " : "text/plain");
    }

    /**
     * returns the static file related with the request
     * 
     * @return string with all information insite the file
     */
    private static String getStaticFile(String file) {
        Path path = (file.equals("/")) ? Paths.get("target/classes/public/static/movie.html")
                : Paths.get("target/classes/public/static" + file);

        Charset charset = Charset.forName("UTF-8");
        StringBuilder outputLine = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                outputLine.append(line).append("\n");
            }
        } catch (Exception e) {
            System.err.format("IOException: " + e.getMessage(), e);
        }

        return outputLine.toString();
    }

}
