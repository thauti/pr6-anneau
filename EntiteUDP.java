import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by thom on 25/05/16.
 */
public class EntiteUDP implements Runnable {
    public Entite e;
    public EntiteUDP(Entite e)
    {
        this.e = e;
    }
    @Override
    public void run() {
        try {
            DatagramSocket sock = new DatagramSocket(e.portLocal);
            byte [] msg = new byte[512];
            DatagramPacket dp = new DatagramPacket(msg, msg.length);
            while (true) {
                sock.receive(dp);
                System.out.println("[INFO] Réception de : "  + new String(msg, 0, dp.getLength()).trim());
                e.traitemsg(new String(msg, 0, dp.getLength()).trim(), dp.getData());
                dp.setLength(msg.length);
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
