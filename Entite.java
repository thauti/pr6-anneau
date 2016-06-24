import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class Entite {
	public final String id;

	public String adresseLocale;
	public int portLocal;

	public String adresseNext;
	public int portNext;

	public String adresseNext2;
	public int portNext2;

	public String addrDiff1;
	public int portDiff1;

	public ByteArrayOutputStream filebuffer;
	public ByteArrayOutputStream webbuffer;

	public String filetransid = "-1";
	public String webtransid = "-1";

	public long filechunk = 0;
	public int filecurrchunk = 0;
	public long webchunk = 0;
	public int webcurrchunk = 0;
	public String filename = "";
	public String webname = "";
	public int webstatus = 0;
	public String addrDiff2;
	public int portDiff2;
	public boolean doubleur = false;
	public boolean etatAnneau1 = true;
	public boolean etatAnneau2 = false;
	public int testanneau = 0;
	public EntiteMulticast em1;
	public EntiteMulticast em2 = null;



	private HashSet<String> listeMessagesVus;

	public Entite(int port, String diffip, int portdiff) throws UnknownHostException, IOException {
		id = genererID();
		adresseLocale = toformatIp(InetAddress.getLocalHost().getHostAddress());
		portLocal = port;

		this.adresseNext = adresseLocale;
		this.portNext = portLocal;

		this.addrDiff1 = toformatIp(diffip);
		this.portDiff1 = portdiff;
		this.em1 = new EntiteMulticast(this,diffip,portdiff);
		Thread em1t = new Thread(this.em1);
		em1t.start();



		debug();
		filebuffer = new ByteArrayOutputStream();
		webbuffer = new ByteArrayOutputStream();
		listeMessagesVus = new HashSet<String>();

	}
	public Entite(String addr,int port, int port2, String diffip, int portdiff) throws UnknownHostException, IOException {
		id = genererID();
		adresseLocale = toformatIp(InetAddress.getLocalHost().getHostAddress());
		portLocal = port2;

		this.adresseNext = toformatIp(addr);
		this.portNext = port;

		this.addrDiff1 = toformatIp(diffip);
		this.portDiff1 = portdiff;
		this.em1 = new EntiteMulticast(this,diffip,portdiff);
		Thread em1t = new Thread(this.em1);
		em1t.start();
		debug();



		filebuffer = new ByteArrayOutputStream();
		webbuffer = new ByteArrayOutputStream();

		listeMessagesVus = new HashSet<String>();


	}
	public void debug()
	{
		System.out.println("===================");
		System.out.println("ID : "+id);
		System.out.println("IP : "+adresseLocale);
		System.out.println("Port UDP: "+portLocal);
		System.out.println("Port TCP: "+(portLocal+1));
		System.out.println("Suivant.IP Prochain: "+adresseNext);
		System.out.println("Suivant.Port Prochain: "+portNext);
		System.out.println("Adresse Multi. : "+addrDiff1);
		System.out.println("Port Multi. : "+ portDiff1);
		if(etatAnneau2)
		{
			System.out.println("Adresse Multi 2 : " + addrDiff2);
			System.out.println("Port Multi 2 : " + portDiff2);
			System.out.println("Suivant.IP 2: "+adresseNext2);
			System.out.println("Suivant.Port 2: "+portNext2);
		}
		System.out.println("===================");
	}
	public String toformatIp(String ip)
	{
		String[] a = ip.split(Pattern.quote("."));
		for(int i=0;i<a.length;i++)
		{
			if(a[i].length() == 1)
			{
				a[i] = "00"+a[i];
			}
			if(a[i].length()== 2)
			{
				a[i] = "0"+a[i];
			}
		}
		return a[0]+"."+a[1]+"."+a[2]+"."+a[3];
	}





	public void envoiUDPSuivant(String mess)
			throws IOException {
		byte[] data = mess.getBytes();
		if(etatAnneau1) {
			System.out.println("Envoi de " + mess);
			DatagramSocket sock = new DatagramSocket();
			InetSocketAddress ia = new InetSocketAddress(adresseNext, portNext);
			DatagramPacket pck = new DatagramPacket(data, data.length, ia);
			sock.send(pck);
		}
		if(etatAnneau2)
		{
			System.out.println("Envoi au second anneau");
			DatagramSocket sock = new DatagramSocket();
			InetSocketAddress ia=new InetSocketAddress(adresseNext2,portNext2);
			DatagramPacket pck = new DatagramPacket(data, data.length,ia);
		}
	}
	public void envoiUDPSuivantBinaire(byte[] data)
			throws IOException {
		if(etatAnneau1) {
			System.out.println("Envoi de " + new String(data));
			DatagramSocket sock = new DatagramSocket();
			InetSocketAddress ia = new InetSocketAddress(adresseNext, portNext);
			DatagramPacket pck = new DatagramPacket(data, data.length, ia);
			sock.send(pck);
		}
		if(etatAnneau2)
		{
			System.out.println("Envoi au second anneau");
			DatagramSocket sock = new DatagramSocket();
			InetSocketAddress ia=new InetSocketAddress(adresseNext2,portNext2);
			DatagramPacket pck = new DatagramPacket(data, data.length,ia);
		}
	}
	public void envoiUDPSuivantSpec(String mess, int anneau)
			throws IOException {
		byte[] data = mess.getBytes();
		System.out.println(anneau);
		if(anneau==1) {
			System.out.println("Envoi de " + mess);
			DatagramSocket sock = new DatagramSocket();
			InetSocketAddress ia = new InetSocketAddress(adresseNext, portNext);
			DatagramPacket pck = new DatagramPacket(data, data.length, ia);
			sock.send(pck);
		}
		if(anneau==2)
		{
			System.out.println("Envoi au second anneau");
			DatagramSocket sock = new DatagramSocket();
			InetSocketAddress ia=new InetSocketAddress(adresseNext2,portNext2);
			DatagramPacket pck = new DatagramPacket(data, data.length,ia);
			sock.send(pck);
		}
	}

	/**
	 * Genere un ID de la forme 00078459
	 */
	private static String genererID() {
		String res = "";
		long time = System.currentTimeMillis();
		double r = Math.random();
		int tmp = (int) ((time % 100000000) * r);
		int l = String.valueOf(tmp).length();
		for (int i = l; i < 8; i++) {
			res += "0";
		}
		return res + Integer.toString(tmp);
	}



	public void traitemsg(String m, byte[] mbyte)
	{
		String[] msg = m.split(Pattern.quote(" "));

		if(m.startsWith("DEBUG"))
		{
			if(msg[0].equals("DEBUGTRANS"))
			{
				demanderFichier(msg[1]);
			}
			if(msg[0].equals("DEBUGTEST"))
			{
				String idm = genererID();
				try {
					envoiUDPSuivant("TEST "+idm+" "+addrDiff1+" "+portDiff1+"");
					this.listeMessagesVus.add(idm);

					AnneauTest at = new AnneauTest(this, addrDiff1, portDiff1);
					Thread t = new Thread(at);
					testanneau =1;
					t.start();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(msg[0].equals("DEBUGWHOS"))
			{
				String idm = genererID();
				try {
					envoiUDPSuivant("WHOS "+idm+"");
					this.listeMessagesVus.add(idm);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(msg[0].trim().equals("DEBUGQUIT"))
			{
				String idm = genererID();
				try {
					envoiUDPSuivant("GBYE "+idm+" "+adresseLocale+" "+portLocal+" "+adresseNext+" "+portNext);
					this.listeMessagesVus.add(idm);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(msg[0].trim().equals("DEBUGMSG"))
			{
				String idm = genererID();
				try {
					envoiUDPSuivant("APPL "+idm+" "+"DIFF####"+" "+"013"+" "+"Hello world !");
					this.listeMessagesVus.add(idm);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(msg[0].trim().equals("DEBUGMSGMP"))
			{
				String idm = genererID();
				try {
					envoiUDPSuivant("APPL "+idm+" "+"DIFFMP##"+" "+msg[1].trim()+" "+"013"+" "+"Hello world !");
					this.listeMessagesVus.add(idm);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else if(msg.length < 2 && !m.startsWith("DEBUG"))
		{
			System.out.println("Le message n'a pas une forme correcte");
		}
		else if(msg[1].length() != 8)
		{
			System.out.println("Le message n'a pas une forme correcte (Problème idm)");
		}
		else
		{
			if(this.listeMessagesVus.contains(msg[1]))
			{
				if(msg[0].equals("TEST"))
				{
					testanneau = 0;
				}
				if(msg[0].equals("APPL"))
				{
					if(msg[2].equals("TRANSWEB"))
					{
						webstatus = 3;
					}
				}
				System.out.println("[INFO] Le message est déjà passé");
			}
			else
			{
				if(msg[0].equals("WHOS") && msg.length==2)
				{
					try {
						this.envoiUDPSuivant(m);
						this.listeMessagesVus.add(msg[1]);
						String idm = genererID();
						this.envoiUDPSuivant("MEMB "+idm+" "+id+" "+adresseLocale+" "+portLocal+"");
						this.listeMessagesVus.add(idm);

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(msg[0].equals("TEST") && msg.length==4)
				{
					System.out.println("TEST");
					try {
						if(!doubleur) {
							this.envoiUDPSuivant(m);
							this.listeMessagesVus.add(msg[1]);
						}
						else
						{
							if(msg[2].equals(addrDiff1)&& msg[3].trim().equals(portDiff1+""))
							{
								System.out.println("TEST sur le premier anneau");
								this.envoiUDPSuivantSpec(m,1);
								this.listeMessagesVus.add(msg[1]);
							}
							if(msg[2].equals(addrDiff2)&& msg[3].trim().equals(portDiff2+""))
							{
								System.out.println("TEST sur le deuxième anneau");
								this.envoiUDPSuivantSpec(m,2);
								this.listeMessagesVus.add(msg[1]);
							}
						}


					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(msg[0].equals("GBYE") && msg.length==6)
				{
					try {
						String idm = genererID();
						if(msg[2].equals(adresseNext) && Integer.parseInt(msg[3]) ==portNext)
						{
							this.envoiUDPSuivant("EYBG "+idm);
							adresseNext = msg[4];
							portNext = Integer.parseInt(msg[5].trim());
							debug();
							this.listeMessagesVus.add(idm);
							System.out.println("Au revoir "+msg[2]);
						}
						else {
							System.out.println("ip "+msg[2]);
							System.out.println("port"+msg[3]);
							System.out.println("iplo"+adresseNext);
							System.out.println("portlo"+portNext);
							this.envoiUDPSuivant(m);
							this.listeMessagesVus.add(msg[1]);
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(msg[0].equals("EYBG") && msg.length==2)
				{

							System.out.println("Au revoir");
							System.exit(1);
				}

				if(msg[0].equals("APPL"))
				{
					traiterAppli(m, mbyte);
				}
			}
		}
	}
	public void traiterAppli(String m, byte[] mbyte)
	{
		String[] msg = m.split(Pattern.quote(" "));
		System.out.println("->"+ msg[2]);

		if(msg[2].equals("DIFF####"))
		{
			int ssize = Integer.parseInt(msg[3]);
			System.out.print("[APP] Message : ");
			for(int j=0;j<ssize;j++)
			{
				System.out.print(m.charAt(27+j));
			}
			System.out.println("");
			this.listeMessagesVus.add(msg[1]);
			try {
				envoiUDPSuivant(m);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(msg[2].equals("TRANS###"))
		{
			//System.out.println("->"+ msg[2]);

			transfichier(m, mbyte);
		}
		else if(msg[2].equals("TRANSWEB"))
		{
			transweb(m, mbyte);
		} else if (msg[2].equals("DIFFMP##")){
			
			int ssize = Integer.parseInt(msg[4]);
			if (msg[3].equals(this.id)){
			  System.out.print("[APP] Message Prive: ");
			  for(int j=0;j<ssize;j++)
			  {
				  System.out.print(m.charAt(36+j));
			  }
			  System.out.println("");
			}
			this.listeMessagesVus.add(msg[1]);
			try {
				envoiUDPSuivant(m);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
		{
			try {
				envoiUDPSuivant(m);
				this.listeMessagesVus.add(msg[1]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void transfichier(String m, byte[] mbyte) {
		String[] msg = m.split(Pattern.quote(" "));
		//System.out.println("->"+ msg[3]);

		if(msg[3].equals("REQ") && msg.length == 6)
		{
			//APPL␣idm␣TRANS###␣REQ␣size-nom␣nom-fichier
			if(Integer.parseInt(msg[4]) != msg[5].length())
			{
				System.out.println("[APP] Requete incorrecte");
			}
			else
			{
				File f = new File(msg[5].trim());
				if(f.exists() && !f.isDirectory())
				{
					//APPL␣idm␣TRANS###␣ROK␣id-trans␣size-nom␣nom-fichier␣nummess
					ByteArrayOutputStream bo = new ByteArrayOutputStream();
					String idm = genererID();
					filetransid = genererID();
					int taille = f.getName().length();
					String t;
					if(taille < 10)
					{
						t="0"+taille;
					}
					else
					{
						t=taille+"";
					}
					double trestant = 512-49;
					double nummessd = Math.ceil(f.length()/trestant);
					System.out.println("[APP] Taille fichier " + f.length());
					System.out.println("[APP] Partie : "+nummessd);
					long nummess = (long)nummessd;
					System.out.println("[APP] Partie int : "+nummess);

					ByteBuffer bb = ByteBuffer.allocate(8);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.putLong(nummess);
					byte[] bytes = bb.array();
					//System.out.println(Arrays.toString(bytes));
					//String res = "APPL "+idm+" TRANS### "+"ROK "+filetransid+" "+t+" "+msg[5]+" "+bytes[0]+bytes[1]+bytes[2]+bytes[3]+bytes[4]+bytes[5]+bytes[6]+bytes[7]+"";
					String res = "APPL "+idm+" TRANS### "+"ROK "+filetransid+" "+t+" "+msg[5]+" ";
					try {
						bo.write(res.getBytes());
						bo.write(bytes[0]);
						bo.write(bytes[1]);
						bo.write(bytes[2]);
						bo.write(bytes[3]);
						bo.write(bytes[4]);
						bo.write(bytes[5]);
						bo.write(bytes[7]);
						bo.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						envoiUDPSuivantBinaire(bo.toByteArray());
						listeMessagesVus.add(idm);
						bo.reset();
					} catch (IOException e) {
						e.printStackTrace();
					}
					// APPL␣idm␣TRANS###␣SEN␣id-trans␣no-mess␣size-content␣content
					int nomess = 0;
					Path path = Paths.get(msg[5].trim());
					byte[] buf = null;
					try {
						BufferedInputStream fiu = new BufferedInputStream(new FileInputStream(msg[5].trim()));
						buf = new byte[(int)f.length()];
						fiu.read(buf,0, buf.length);

						//buf = Files.FileInputStream(path);
					} catch (IOException e) {
						e.printStackTrace();
					}
					for(nomess=0;nomess<nummess;nomess++)
					{
						int n = 0;
						String idm2 = genererID();
						ByteBuffer bb2 = ByteBuffer.allocate(8);
						bb2.order(ByteOrder.LITTLE_ENDIAN);
						bb2.putInt(nomess);
						byte[] bytes2 = bb2.array();
						//String mf = "APPL " + idm2+" TRANS### SEN "+ filetransid+" "+bytes2[0]+bytes2[1]+bytes2[2]+bytes2[3]+bytes2[4]+bytes2[5]+bytes2[6]+bytes2[7]+" ";
						String mf = "APPL " + idm2+" TRANS### SEN "+ filetransid+" ";
						//System.out.println(Arrays.toString(buf));
						int sc = 463;
						if(nomess==nummess-1){
							sc = buf.length%463;
						}
						String scs ="";
						if(sc<10)
						{
							scs = "00"+sc;
						}
						else if(sc<100)
						{
							scs = "0"+sc;
						}
						else
						{
							scs = sc+"";
						}
						try {
							bo.write(mf.getBytes());
							bo.write(bytes2[0]);
							bo.write(bytes2[1]);
							bo.write(bytes2[2]);
							bo.write(bytes2[3]);
							bo.write(bytes2[4]);
							bo.write(bytes2[5]);
							bo.write(bytes2[6]);
							bo.write(bytes2[7]);
							bo.write(" ".getBytes());
							bo.write(scs.getBytes());
							bo.write(" ".getBytes());
						} catch (IOException e) {
							e.printStackTrace();
						}
						int offset = 0;
						String env ="";
						while(n<463)
						{

							if(n +(nomess*463)<buf.length) {
								bo.write(buf[n +(nomess*463)]);
							}
							n++;
						}
						try {

							bo.write(env.getBytes());
							bo.flush();
							//String rres = mf+scs+" "+env;
							envoiUDPSuivantBinaire(bo.toByteArray());
							listeMessagesVus.add(idm2);
							bo.reset();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				else
				{
					System.out.println("[APP] "+msg[5]+" n'existe pas");
					try {
						this.envoiUDPSuivant(m);
						this.listeMessagesVus.add(msg[1]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(msg[3].equals("ROK"))
		{
			if(msg[6].equals(filename)) // NE PAS OUBLIER DE CHANGER CA
			{

			//	String temp = msg[7];
			//	byte[] tempo = temp.getBytes();
				byte[] tempoi = new byte[8];
				int tf = Integer.parseInt(msg[5]);
				tempoi[7] = mbyte[(49-9)+tf];
				tempoi[6] = mbyte[(50-9)+tf];
				tempoi[5] = mbyte[(51-9)+tf];
				tempoi[4] = mbyte[(52-9)+tf];
				tempoi[3] = mbyte[(53-9)+tf];
				tempoi[1] = mbyte[(54-9)+tf];
				tempoi[0] = mbyte[(55-9)+tf];
				//System.out.println(Arrays.toString(mbyte));
				ByteBuffer bbff = ByteBuffer.wrap(tempoi);
				//bbff.getInt();
				//System.out.println(Arrays.toString(bbff.array()));
				filechunk = bbff.getLong();
				//filechunk = Integer.parseInt(new String(tempoi));
				filetransid = msg[4];
				System.out.println("[APP] Reception de  "+ filename+" ID "+filetransid);
				System.out.println("[APP] Nombre de chunk = "+ filechunk);
				this.listeMessagesVus.add(msg[1]);

			}
			else
			{
				try {
					this.envoiUDPSuivant(m);
					this.listeMessagesVus.add(msg[1]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if(msg[3].equals("SEN"))
		{
			//System.out.println("Hello ?");

			if(msg[4].equals(filetransid)) // NE PAS OUBLIER DE CHANGER CA
			{
				System.out.println("[APP] Ecriture");
				String temp = msg[5];
				System.out.println(msg[5]);
				byte[] tempo = temp.getBytes();
				byte[] tempoi = new byte[8];
				tempoi[7] = mbyte[36];
				tempoi[6] = mbyte[37];
				tempoi[5] = mbyte[38];
				tempoi[4] = mbyte[39];
				tempoi[3] = mbyte[40];
				tempoi[1] = mbyte[41];
				tempoi[0] = mbyte[42];
				//System.out.println(Arrays.toString(tempoi));
				ByteBuffer bbff = ByteBuffer.wrap(tempoi);

				long currc = bbff.getLong();
				System.out.println("[APP] Chunk courant "+ currc + "/"+filechunk);
				if(currc == filecurrchunk)
				{
					int size;
					try {
						size = Integer.parseInt(msg[6]);
					}catch (NumberFormatException e)
					{
						size = Integer.parseInt(msg[7]);
					}
					filebuffer.write(mbyte,49,size);
					try {
						filebuffer.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if(filecurrchunk == filechunk-1) {

						try {
							System.out.println("[APP] Enregistrement");
							BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream("reception/"+filename));
							FileOutputStream stream = new FileOutputStream("reception/"+filename);
							stream.write(filebuffer.toByteArray());
							filebuffer.close();
							filebuffer = new ByteArrayOutputStream();
							//System.out.println(Arrays.toString(filebuffer.toByteArray()));
							//filebuffer.writeTo(bw);
							bw.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					filecurrchunk++;
				}
				else
				{
					String nidm;
					nidm = genererID();
					filechunk =-1;
					filecurrchunk = -1;
					filetransid ="";
					try {
						this.envoiUDPSuivant("APPL "+nidm+" TRANS### REQ "+filename.length()+" "+filename+"");
						listeMessagesVus.add(nidm);
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
			else
			{
				try {
					this.envoiUDPSuivant(m);
					this.listeMessagesVus.add(msg[1]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/////////////////////////////
	public void transweb(String m, byte[] mbyte) {
		String[] msg = m.split(Pattern.quote(" "));
		//System.out.println("->"+ msg[3]);

		if(msg[3].equals("REQ") && msg.length == 6)
		{
			//APPL␣idm␣TRANS###␣REQ␣size-nom␣nom-fichier
			if(Integer.parseInt(msg[4]) != msg[5].length())
			{
				System.out.println("[APP] Requete incorrecte");
			}
			else
			{
				File f = new File(msg[5].trim());
				if(f.exists() && !f.isDirectory())
				{
					//APPL␣idm␣TRANSWEB␣ROK␣id-trans␣size-nom␣nom-fichier␣nummess
					ByteArrayOutputStream bo = new ByteArrayOutputStream();
					String idm = genererID();
					webtransid = genererID();
					int taille = f.getName().length();
					String t;
					if(taille < 10)
					{
						t="0"+taille;
					}
					else
					{
						t=taille+"";
					}
					double trestant = 512-49;
					double nummessd = Math.ceil(f.length()/trestant);
					System.out.println("[APP] Taille fichier " + f.length());
					System.out.println("[APP] Partie : "+nummessd);
					long nummess = (long)nummessd;
					System.out.println("[APP] Partie int : "+nummess);

					ByteBuffer bb = ByteBuffer.allocate(8);
					bb.order(ByteOrder.LITTLE_ENDIAN);
					bb.putLong(nummess);
					byte[] bytes = bb.array();
					//System.out.println(Arrays.toString(bytes));
					//String res = "APPL "+idm+" TRANSWEB "+"ROK "+filetransid+" "+t+" "+msg[5]+" "+bytes[0]+bytes[1]+bytes[2]+bytes[3]+bytes[4]+bytes[5]+bytes[6]+bytes[7]+"";
					String res = "APPL "+idm+" TRANSWEB "+"ROK "+webtransid+" "+t+" "+msg[5]+" ";
					try {
						bo.write(res.getBytes());
						bo.write(bytes[0]);
						bo.write(bytes[1]);
						bo.write(bytes[2]);
						bo.write(bytes[3]);
						bo.write(bytes[4]);
						bo.write(bytes[5]);
						bo.write(bytes[7]);
						bo.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						envoiUDPSuivantBinaire(bo.toByteArray());
						listeMessagesVus.add(idm);
						bo.reset();
					} catch (IOException e) {
						e.printStackTrace();
					}
					// APPL␣idm␣TRANSWEB␣SEN␣id-trans␣no-mess␣size-content␣content
					int nomess = 0;
					Path path = Paths.get(msg[5].trim());
					byte[] buf = null;
					try {
						BufferedInputStream fiu = new BufferedInputStream(new FileInputStream(msg[5].trim()));
						buf = new byte[(int)f.length()];
						fiu.read(buf,0, buf.length);

						//buf = Files.FileInputStream(path);
					} catch (IOException e) {
						e.printStackTrace();
					}
					for(nomess=0;nomess<nummess;nomess++)
					{
						int n = 0;
						String idm2 = genererID();
						ByteBuffer bb2 = ByteBuffer.allocate(8);
						bb2.order(ByteOrder.LITTLE_ENDIAN);
						bb2.putInt(nomess);
						byte[] bytes2 = bb2.array();
						//String mf = "APPL " + idm2+" TRANSWEB SEN "+ filetransid+" "+bytes2[0]+bytes2[1]+bytes2[2]+bytes2[3]+bytes2[4]+bytes2[5]+bytes2[6]+bytes2[7]+" ";
						String mf = "APPL " + idm2+" TRANSWEB SEN "+ webtransid+" ";
						//System.out.println(Arrays.toString(buf));
						int sc = 463;
						if(nomess==nummess-1){
							sc = buf.length%463;
						}
						String scs ="";
						if(sc<10)
						{
							scs = "00"+sc;
						}
						else if(sc<100)
						{
							scs = "0"+sc;
						}
						else
						{
							scs = sc+"";
						}
						try {
							bo.write(mf.getBytes());
							bo.write(bytes2[0]);
							bo.write(bytes2[1]);
							bo.write(bytes2[2]);
							bo.write(bytes2[3]);
							bo.write(bytes2[4]);
							bo.write(bytes2[5]);
							bo.write(bytes2[6]);
							bo.write(bytes2[7]);
							bo.write(" ".getBytes());
							bo.write(scs.getBytes());
							bo.write(" ".getBytes());
						} catch (IOException e) {
							e.printStackTrace();
						}
						int offset = 0;
						String env ="";
						while(n<463)
						{

							if(n +(nomess*463)<buf.length) {
								bo.write(buf[n +(nomess*463)]);
							}
							n++;
						}
						try {

							bo.write(env.getBytes());
							bo.flush();
							//String rres = mf+scs+" "+env;
							envoiUDPSuivantBinaire(bo.toByteArray());
							listeMessagesVus.add(idm2);
							bo.reset();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				else
				{
					System.out.println("[APP] "+msg[5]+" n'existe pas");
					try {
						this.envoiUDPSuivant(m);
						this.listeMessagesVus.add(msg[1]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(msg[3].equals("ROK"))
		{
			if(msg[6].equals(webname)) // NE PAS OUBLIER DE CHANGER CA
			{

				//	String temp = msg[7];
				//	byte[] tempo = temp.getBytes();
				byte[] tempoi = new byte[8];
				int tf = Integer.parseInt(msg[5]);
				tempoi[7] = mbyte[(49-9)+tf];
				tempoi[6] = mbyte[(50-9)+tf];
				tempoi[5] = mbyte[(51-9)+tf];
				tempoi[4] = mbyte[(52-9)+tf];
				tempoi[3] = mbyte[(53-9)+tf];
				tempoi[1] = mbyte[(54-9)+tf];
				tempoi[0] = mbyte[(55-9)+tf];
				//System.out.println(Arrays.toString(mbyte));
				ByteBuffer bbff = ByteBuffer.wrap(tempoi);
				//bbff.getInt();
				//System.out.println(Arrays.toString(bbff.array()));
				webchunk = bbff.getLong();
				//filechunk = Integer.parseInt(new String(tempoi));
				webtransid = msg[4];
				System.out.println("[WEB] Reception de  "+ webname+" ID "+webtransid);
				System.out.println("[WEB] Nombre de chunk = "+ webchunk);
				this.listeMessagesVus.add(msg[1]);

			}
			else
			{
				try {
					this.envoiUDPSuivant(m);
					this.listeMessagesVus.add(msg[1]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if(msg[3].equals("SEN"))
		{
			//System.out.println("Hello ?");

			if(msg[4].equals(webtransid)) // NE PAS OUBLIER DE CHANGER CA
			{
				System.out.println("[APP] Ecriture");
				String temp = msg[5];
				System.out.println(msg[5]);
				byte[] tempo = temp.getBytes();
				byte[] tempoi = new byte[8];
				tempoi[7] = mbyte[36];
				tempoi[6] = mbyte[37];
				tempoi[5] = mbyte[38];
				tempoi[4] = mbyte[39];
				tempoi[3] = mbyte[40];
				tempoi[1] = mbyte[41];
				tempoi[0] = mbyte[42];
				//System.out.println(Arrays.toString(tempoi));
				ByteBuffer bbff = ByteBuffer.wrap(tempoi);

				long currc = bbff.getLong();
				System.out.println("[APP] Chunk courant "+ currc + "/"+webchunk);
				if(currc == webcurrchunk)
				{
					int size;
					try {
						size = Integer.parseInt(msg[6]);
					}catch (NumberFormatException e)
					{
						size = Integer.parseInt(msg[7]);
					}
					//System.out.println(Arrays.toString(mbyte));
					webbuffer.write(mbyte,49,size);
					try {
						webbuffer.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if(webcurrchunk == webchunk-1) {
							webstatus = 2;
							System.out.println("[WEB] OK");

					}
					webcurrchunk++;
				}
				else
				{
					String nidm;
					nidm = genererID();
					webchunk =-1;
					webcurrchunk = -1;
					webtransid ="";
					try {
						this.envoiUDPSuivant("APPL "+nidm+" TRANSWEB REQ "+webname.length()+" "+webname+"");
						listeMessagesVus.add(nidm);
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
			else
			{
				try {
					this.envoiUDPSuivant(m);
					this.listeMessagesVus.add(msg[1]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	////////////////////////////
	public void demanderFichier(String m)
	{
		System.out.println("[APP] Je demande le fichier " + m );
		String nidm;
		nidm = genererID();
		filechunk = 0;
		filetransid = "";
		filecurrchunk = 0;
		filename = m;
		try {
			this.envoiUDPSuivant("APPL "+nidm+" TRANS### REQ "+m.length()+" "+m+"");
			listeMessagesVus.add(nidm);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void demanderFichierWeb(String m)
	{
		System.out.println("[APP] Je demande le fichier web" + m );
		String nidm;
		nidm = genererID();
		webchunk = 0;
		webtransid = "";
		webcurrchunk = 0;
		webname = m;
		webstatus = 0;
		try {
			this.envoiUDPSuivant("APPL "+nidm+" TRANSWEB REQ "+m.length()+" "+m+"");
			listeMessagesVus.add(nidm);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void traitemsgMulticast(String m, String ip, int port) {

		if(m.startsWith("DOWN"))
		{
			System.out.println("Problème dans l'anneau");
			if(doubleur)
			{
				if(ip.equals(addrDiff1) && port == portDiff1)
				{
					etatAnneau1 = false;
					doubleur = false;
				}
				if(ip.equals(addrDiff2) && port == portDiff2)
				{
					etatAnneau2 = false;
					doubleur = false;

				}
				if(etatAnneau1 == false && etatAnneau2==false)
				{
					System.out.println("Reception d'une directive DOWN, Fin de l'Entite");
					System.exit(1);
				}
			}else
			{
				System.out.println("Reception d'une directive DOWN, Fin de l'Entite");
				System.exit(1);
			}
		}
	}
}
