package juan.taller2.apiMovie;

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
    public static void startServer() throws IOException {
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

            outputLine = (petition.startsWith("/film"))
                    ? movieInfo(petition.replace("/film?name=", ""), clientSocket.getOutputStream())
                    : mainPage(petition, clientSocket.getOutputStream());

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
    private static String movieInfo(String name, OutputStream ops) {
        try {
            JsonObject resp = apiMovie.searchMovie(name);
            JsonElement title = resp.get("Title");
            JsonElement poster = resp.get("Poster");
            JsonElement director = resp.get("Director");
            JsonElement plot = resp.get("Plot");

            String outputLine = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type:text/html\r\n"
                    + "\r\n"
                    + getStaticFile("/movieInfo.html", ops)
                            .replace("{Title}", title.toString())
                            .replace("\"{Poster}\"", poster.toString())
                            .replace("{Directors}", director.toString())
                            .replace("{Plot}", plot.toString());

            return outputLine;
        } catch (Exception e) {
            e.printStackTrace();
            return "HTTP/1.1 500 Internal Server Error\r\n\r\n";
        }
    }

    /**
     * method that returns the principal html page
     * 
     * @return the principal page of the application
     */
    private static String mainPage(String file, OutputStream ops) {
        String contentType = getMimeType(file);
        String content = getStaticFile(file, ops);

        String outputLine = "HTTP/1.1 200 OK\r\n"
                + "Content-Type:" + contentType + "\r\n"
                + "\r\n"
                + content;

        return outputLine;
    }

    private static String getMimeType(String file) {
        if (file.endsWith(".html") || file.endsWith("/")) {
            return "text/html";
        } else if (file.endsWith(".css")) {
            return "text/css";
        } else if (file.endsWith(".js")) {
            return "application/javascript";
        } else if (file.endsWith(".jpg")) {
            return "image/jpeg";
        } else {
            return "text/plain";
        }
    }

    /**
     * returns the static file related with the request
     * 
     * @return string with all information insite the file
     */
    private static String getStaticFile(String file, OutputStream ops) {
        Path path = (file.equals("/"))
                ? Paths.get("target/classes/public/static/movie.html")
                : Paths.get("target/classes/public/static" + file);

        try {
            Charset charset = Charset.forName("UTF-8");
            StringBuilder outputLine = new StringBuilder();
            byte[] bytes;

            if (file.endsWith(".jpg")) {
                bytes = getAnImage(file);
                String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: image/jpeg\r\n" +
                        "Content-Length: " + bytes.length + "\r\n" +
                        "\r\n";
                ops.write(response.getBytes());
                ops.write(bytes);
            } else {
                try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputLine.append(line).append("\n");
                    }
                }
            }

            return outputLine.toString();
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            return "HTTP/1.1 404 Not Found\r\n\r\n";
        }
    }

    /**
     * return the bytes of an image
     * 
     * @param file the route of the file to return to the browser
     * @return an array of bytes
     */
    private static byte[] getAnImage(String file) {

        Path image = Paths.get("target/classes/public/static" + file);

        try {
            return Files.readAllBytes(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
