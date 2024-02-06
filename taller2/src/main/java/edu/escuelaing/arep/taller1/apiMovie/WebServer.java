package edu.escuelaing.arep.taller1.apiMovie;

import java.net.*;

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

            outputLine = (petition.startsWith("/film")) ? movieInfo(petition.replace("/film?name=", "")) : mainPage();

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
     * @return a html structure with some movie information, with some headers
     */
    private static String movieInfo(String name) {
        try {

            JsonObject post = apiMovie.searchMovie(name);
            return "HTTP/1.1 200 OK\r\n" +
                    "Content-Type:text/html; charset=utf-8\\r\\n" + //
                    "\r\n"
                    + "<!DOCTYPE html>\r\n" + //
                    "<html lang=\"en\">\r\n" + //
                    "\r\n" + //
                    "<head>\r\n" + //
                    "    <meta charset=\"UTF-8\">\r\n" + //
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + //
                    "    <title>Movies</title>\r\n" + //
                    "</head>\r\n" + //
                    "\r\n" + //
                    "<body>\r\n" + //
                    "    <h1>Title:" + post.get("Title") + "</h1>\r\n" + //
                    "    <img src=" + post.get("Poster") + ">\r\n" + //
                    "    <h3>Director(s):" + post.get("Director") + "</h3>\r\n" + //
                    "    <h2>Plot:" + post.get("Plot") + "</h2>\r\n" + //
                    "</body>\r\n" + //
                    "\r\n" + //
                    "</html>";
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
    private static String mainPage() {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type:text/html; charset=utf-8\\r\\n" + //
                "\r\n"
                + "<!DOCTYPE html>\r\n" + //
                "<html lang=\"en\">\r\n" + //
                "\r\n" + //
                "<head>\r\n" + //
                "    <meta charset=\"UTF-8\">\r\n" + //
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" + //
                "    <title>Consultar Peliculas</title>\r\n" + //
                "    <style>\r\n" + //
                "        * {\r\n" + //
                "            margin: 0;\r\n" + //
                "            padding: 0;\r\n" + //
                "            background: url(\"../../img/fondo.jpg\") no-repeat;\r\n" + //
                "            align-items: center;\r\n" + //
                "            justify-content: center;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "\r\n" + //
                "        .container {\r\n" + //
                "            margin-top: 5%;\r\n" + //
                "            display: flex;\r\n" + //
                "            flex-direction: column;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "\r\n" + //
                "        #movie {\r\n" + //
                "            width: 100%;\r\n" + //
                "            height: 30%;\r\n" + //
                "            border-radius: 10%;\r\n" + //
                "            color: blue;\r\n" + //
                "        }\r\n" + //
                "\r\n" + //
                "        #getMovie {\r\n" + //
                "            height: 30px;\r\n" + //
                "            width: 150px;\r\n" + //
                "            border-radius: 40%;\r\n" + //
                "            outline: none;\r\n" + //
                "            cursor: pointer;\r\n" + //
                "            color: blue;\r\n" + //
                "            background-color: red;\r\n" + //
                "            ;\r\n" + //
                "    </style>\r\n" + //
                "</head>\r\n" + //
                "\r\n" + //
                "\r\n" + //
                "<body>\r\n" + //
                "    <div class=\"container\">\r\n" + //
                "        <div class=\"consulta\">\r\n" + //
                "            <form action=\"/film\">\r\n" + //
                "                <label>Escriba el nombre de la pelicula a consultar</label>\r\n" + //
                "                <input type=\"text\" id=\"movie\" placeholder=\"Ingrese el nombre de la pelicula\" name=\"name\">\r\n"
                + //
                "                <input type=\"button\" id=\"getMovie\" value=\"consultar\" onclick=\"consultMovie()\">\r\n"
                + //
                "            </form>\r\n" + //
                "\r\n" + //
                "        </div>\r\n" + //
                "\r\n" + //
                "        <div id=\"pelicula\">\r\n" + //
                "\r\n" + //
                "        </div>\r\n" + //
                "\r\n" + //
                "    </div>\r\n" + //
                "\r\n" + //
                "    <script>\r\n" + //
                "        function consultMovie() {\r\n" + //
                "            let nameMovie = document.getElementById(\"movie\").value;\r\n" + //
                "            console.log(nameMovie);\r\n" + //
                "            const xhttp = new XMLHttpRequest();\r\n" + //
                "            xhttp.onload = function () {\r\n" + //
                "                document.getElementById(\"pelicula\").innerHTML =\r\n" + //
                "                    this.responseText;\r\n" + //
                "            }\r\n" + //
                "            xhttp.open(\"GET\", \"/film?name=\" + nameMovie);\r\n" + //
                "            xhttp.send();\r\n" + //
                "        }\r\n" + //
                "    </script>\r\n" + //
                "</body>\r\n" + //
                "\r\n" + //
                "</html>";
    }

}
