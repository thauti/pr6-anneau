import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;

public class test {

	public static void main(String[] args) {
		int taillearg = args.length;
		if(args.length > 2)
		{
			if(args[args.length-2].equals("-web"));
			{
				taillearg = args.length-2;
			}
		}
		if(args.length == 0)
		{
			System.out.println("Usage :");
			System.out.println("java test -a PORTUDP IPDIFF PORTDIFF [-web portweb]");
			System.out.println("java test IP PORTTCP PORTUDP [-web portweb]");
			System.out.println("java test ip PORTUDP PORTUDP IPDIFF PORTDIFF [-web portweb]");
			System.exit(1);
		}
		try {
			Entite e;
			Thread eudp;
			Thread etcp;
			if(args[0].equals("-a"))
			{
				e = new Entite(Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
				eudp = new Thread(new EntiteUDP(e));
				etcp = new Thread(new EntiteTCP(e));

			}
			else
			{
				System.out.println("Connexion à "+args[0]+" port "+args[1]+1);
				Socket so = new Socket(args[0], Integer.parseInt(args[1]));
				//Entite e = new Entite(Integer.parseInt(args[2]));
				PrintWriter pw = new PrintWriter(new DataOutputStream(so.getOutputStream()));
				BufferedReader br = new BufferedReader(new InputStreamReader(so.getInputStream()));
				e=null;
				int continuer = 1;
				while(continuer == 1)
				{
					String msg = br.readLine();
					System.out.println(msg);
					String[] infos = msg.split(Pattern.quote(" "));
					System.out.println(msg);
					if(taillearg == 5 )
					{
						System.out.println("DEMANDE DUPL");
						e = new Entite(args[0],Integer.parseInt(args[1]),Integer.parseInt(args[2]), args[3],Integer.parseInt(args[4]));
						pw.print("DUPL "+e.adresseLocale+" "+e.portLocal+" "+e.addrDiff1+" "+e.portDiff1+"\n");
						pw.flush();
					}else
					{
						e = new Entite(infos[1], Integer.parseInt(infos[2]),Integer.parseInt(args[2]),infos[3], Integer.parseInt(infos[4]));
						pw.print("NEWC "+e.adresseLocale+" "+e.portLocal+"\n");
						pw.flush();
					}
					msg = br.readLine();
					if(msg.equals("NOTC\n"))
					{
						System.out.println("Erreur :  L'entité cible a refusé la connexion");
						System.exit(1);
					}
					if(msg.startsWith("ACKD"))
					{
						String[] a = msg.split(" ");
						int port = Integer.parseInt(a[1].trim());
						e.portNext = port;
						e.debug();
					}
					System.out.println(msg);
					continuer = 0;
				}

				System.out.println("Fin connexion");
				so.close();
			}
			eudp = new Thread(new EntiteUDP(e));
			etcp = new Thread(new EntiteTCP(e));
			if(args.length > 2) {
				if (args[args.length - 2].equals("-web")) {
					WebServer ws = new WebServer(Integer.parseInt(args[args.length-1]), e);
					Thread wt = new Thread(ws);
					wt.start();
				}
			}
			eudp.start();
			etcp.start();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
