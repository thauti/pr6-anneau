import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Created by thom on 25/05/16.
 */
public class AnneauTest implements Runnable{
    public Entite e;
    public String addr;
    public int port;
    public AnneauTest(Entite e, String addr, int port)
    {
        this.e = e;
        this.addr = addr;
        this.port = port;
    }
    public void run()
    {
        try {
            System.out.println("Démarrage chrono");
            Thread.sleep(3000);
            if(e.testanneau == 1) {
                System.out.println("Fin chrono");
                String mess = "DOWN";
                byte[] data;
                data = mess.getBytes();
                System.out.println("[INFO] Envoi Multicast de " + mess);
                DatagramSocket sock = new DatagramSocket();
                InetSocketAddress ia = new InetSocketAddress(e.addrDiff1, e.portDiff1);
                DatagramPacket pck = new DatagramPacket(data, data.length, ia);
                sock.send(pck);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
