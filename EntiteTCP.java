import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by thom on 25/05/16.
 */
public class EntiteTCP implements Runnable {
    public Entite e;
    public EntiteTCP(Entite e)
    {
        this.e = e;
    }
    public void run()
    {
        try {

            ServerSocket soserver = new ServerSocket(e.portLocal + 1);
            Socket s;
            while(true)
            {
                s = soserver.accept();
                System.out.println("Nouvelle connexion");
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                pw.print("WELC "+e.adresseNext+" "+e.portNext+" "+e.addrDiff1+" "+e.portDiff1+"\n");
                pw.flush();
                String msg = br.readLine();
                String infos[] = msg.split(" ");
                if(!e.doubleur)
                {
                    if(infos[0].equals("DUPL"))
                    {
                        System.out.println("Création du doublage");
                        e.adresseNext2 = infos[1];
                        e.portNext2 = Integer.parseInt(infos[2]);
                        e.addrDiff2 = infos[3];
                        e.portDiff2 = Integer.parseInt(infos[4]);
                        e.doubleur = true;
                        e.etatAnneau2 = true;
                        e.em2 = new EntiteMulticast(e,e.addrDiff2,e.portDiff2);
                        pw.print("ACKD "+e.portLocal+"\n");
                        pw.flush();
                    }else {
                        e.adresseNext = infos[1];
                        e.portNext = Integer.parseInt(infos[2]);
                        pw.print("ACKC\n");
                        pw.flush();
                    }
                }
                else
                {
                    pw.print("NOTC\n");
                    pw.flush();
                }
                s.close();
                System.out.println("Fin connexion TCP");
                e.debug();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
