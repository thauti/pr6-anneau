import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class WebServerClient implements Runnable{
    Socket sock;
    Entite e;
    public WebServerClient(Socket s, Entite e)
    {
        this.sock = s;
        this.e = e;
    }

    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(this.sock.getOutputStream()));
            OutputStream os = this.sock.getOutputStream();
            String[] header = new String[15];
            String tmp = br.readLine();
            int i = 0;
            while (!tmp.isEmpty()) {
                //System.out.println(tmp);
                header[i] = tmp;
                tmp = br.readLine();
                i++;

            }
           // System.out.println("Fin Reception");

            String[] head_info = header[0].split(" ");
            String req = head_info[0];
            String req_url = head_info[1];
            String req_prot = head_info[2];
            if (req_url.endsWith("/")) {
                Path path;
                if(req_url.length() == 1)
                    e.demanderFichierWeb("index.html");
                else
                    e.demanderFichierWeb(req_url.substring(1)+"index.html");
                int continuer = 1;
                while(continuer==1)
                {
                    if(e.webstatus == 3)
                    {
                        send404(pw);
                    }
                    if(e.webstatus == 2)
                    {
                        sendPage(os,e.webbuffer.toByteArray());
                        e.webbuffer = new ByteArrayOutputStream();
                        e.webstatus = 0;
                        continuer = 0;
                    }
                    Thread.sleep(200);
                }
            } else {
                String url = req_url.substring(1);
                Path path = Paths.get(url);
                System.out.println(req_url);

                e.demanderFichierWeb(url);
                int continuer = 1;
                while(continuer==1)
                {
                    if(e.webstatus == 3)
                    {
                        send404(pw);
                    }
                    if(e.webstatus == 2)
                    {
                        sendPage(os,e.webbuffer.toByteArray());
                        e.webbuffer = new ByteArrayOutputStream();
                        e.webstatus = 0;
                        continuer = 0;
                    }
                    Thread.sleep(200);
                }

            }
            pw.close();
            os.close();
            sock.close();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void sendPage(OutputStream os, byte[] msg) throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
        pw.print("HTTP/1.1 200 OK \r\n");
        pw.flush();
        pw.print("Content-Length: "+msg.length+" \r\n");
        pw.flush();
        pw.print("Server: RingoServer/1.0 \r\n");
        pw.flush();
        pw.print("Connection: Closed\r\n");
        pw.flush();
        pw.print("\r\n");
        pw.flush();
        os.write(msg);
        os.flush();
        //pw.flush();
    }
    public  void send404(PrintWriter pw)
    {
        String erreur = "<h1>Erreur 404</h1>\r\n";
        pw.print("HTTP/1.1 404 Not Found\r\n");
        pw.flush();
        pw.print("Content-Length: "+erreur.length()+" \r\n");
        pw.flush();
        pw.print("Server: RingoServer/1.0 \r\n");
        pw.flush();
        pw.print("Connection: Closed\r\n");
        pw.flush();
        pw.print("\r\n");
        pw.flush();
        pw.print(erreur);
        pw.flush();
    }
}
