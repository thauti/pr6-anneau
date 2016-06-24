import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class EntiteMulticast implements Runnable {
    public String ip;
    public int port;
    public Entite e;
    public EntiteMulticast(Entite e, String ip, int port)
    {
        this.e = e;
        this.ip = ip;
        this.port = port;
    }

    public void run()
    {
        try {
            MulticastSocket sock = new MulticastSocket(port);
            sock.joinGroup(InetAddress.getByName(ip));
            byte[]data=new byte[512];
            DatagramPacket paquet=new DatagramPacket(data,data.length);
            while(true)
            {
                sock.receive(paquet);
                System.out.println("[INFO] Reception de " + new String(data, 0, paquet.getLength()).trim());
                String st=new String(paquet.getData(),0,paquet.getLength());
                e.traitemsgMulticast(st, ip, port);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
