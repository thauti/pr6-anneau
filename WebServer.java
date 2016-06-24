import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WebServer implements Runnable {
    int port;
    Entite e;
    public WebServer(int port, Entite e)
    {
        this.port = port;
        this.e = e;
    }
    public void run()
    {
        try {
            ServerSocket serverSock = new ServerSocket(port);
            System.out.println("Serveur web lancé");
            int c = 0;
            while (true) {
                System.out.println("[WEB] En attente d'une connexion sur le port "+port);
                System.out.println(++c);
                Socket s = serverSock.accept();
                Thread wc = new Thread(new WebServerClient(s,e));
                wc.start();
            }
        }catch(Exception e)
            {
                e.printStackTrace();
            }
    }

}
