import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

public class Server {

    private static final int MAX_BUFFERSIZE = 1024;
    private static DatagramSocket socket;
    private final int port = 8080;
    private State state = State.NONE;
    private int seq_num = 0;
    private int ack_num = 0;
    private int counter = 0;

    public static void main(String[] args){
        var server = new Server();
        server.start();
    }

   public void start(){

        if(socket != null){return;}

       try {
           socket = new DatagramSocket(port);
           System.out.println("Server is listening at port: " + port + "!");

           byte[] buff = new byte[MAX_BUFFERSIZE];
           while(!socket.isClosed()){
               DatagramPacket packet = new DatagramPacket(buff, buff.length);

               try {
                   // received the first SYNC packet from the server to start the first handshake
                   socket.receive(packet);
                   Packet hs1 = new Packet(buff);
                   var clientAddress = packet.getAddress();
                   var clientPort = packet.getPort();
                   System.out.println("\n<<Received from Client>> " + hs1.toString());

                   if(state == State.NONE){

                       // second handshake where server acknowledge and send sync+ack packet to client
                       if(hs1.isSync_bit()){
                           System.out.println("\nThree way handshake 1/3!");

                           //send ACK+SYN packet
                           Packet hs2 = new Packet();
                           hs2.setAck_bit(true);
                           ack_num = hs1.getSequence_num()+1;
                           hs2.setAck_num(ack_num);

                           seq_num = ThreadLocalRandom.current().nextInt(0, 2147483647);
                           hs2.setSync_bit(true);
                           hs2.setSequence_num(seq_num);

                           hs2.setSrc_port((short) port);
                           hs2.setDest_port((short) clientPort);
                           state = State.SYN_RECV;
                           send(clientAddress, clientPort, hs2.toByteArray());
                           System.out.println("\nThree way handshake 2/3!");
                       }
                   }else if(state == State.SYN_RECV){
                       //received the ack packet and the three handshake is done and connection established.
                       if(hs1.isAck_bit() && hs1.getAck_num() == (++seq_num)){
                           state = State.ESTABLISHED;
                           System.out.println("\nThree way handshake 3/3! "
                                                + "Connection Established");

                           //sending the data which is an image
                           ImageHandler image = new ImageHandler("src/inside.jpg");
                           var imageBA = image.toImageByteArray();
                           var imglist = image.getListOfImgPacket(MAX_BUFFERSIZE, imageBA);

                           // send the first packet of the data.
                           Packet imageP = new Packet();
                           imageP.setData(imglist.get(counter));

                           imageP.setSequence_num(seq_num);
                           imageP.setSync_bit(false);

                           imageP.setAck_num(0);
                           imageP.setAck_bit(true);

                           imageP.setSrc_port((short) port);
                           imageP.setDest_port((short) clientPort);
                           send(clientAddress, clientPort, imageP.toByteArray());
                           counter++;
                       }
                   }else if(state == State.ESTABLISHED){
                       if(hs1.isAck_bit()){
                           ImageHandler image = new ImageHandler("src/inside.jpg");
                           var imageBA = image.toImageByteArray();
                           var imglist = image.getListOfImgPacket(MAX_BUFFERSIZE, imageBA);
                           //System.out.println("image list size: " + imglist.size());
                           // if the image has more than one packet
                           if (imglist.size() != 1 && counter < imglist.size()) {
                               Packet imgP = new Packet();

                                   imgP.setData(imglist.get(counter));

                                   imgP.setSequence_num(hs1.getAck_num());
                                   imgP.setSync_bit(false);

                                   imgP.setAck_num(0);
                                   imgP.setAck_bit(true);

                                   imgP.setSrc_port((short) port);
                                   imgP.setDest_port((short) clientPort);
                                   send(clientAddress, clientPort, imgP.toByteArray());
                                   counter++;
                           }
                           else{
                               System.out.println("Sent all the packets!");
                               Packet finP = new Packet();
                               finP.setSequence_num(hs1.getAck_num());
                               finP.setFin_bit(true);
                               finP.setAck_bit(true);
                               finP.setSrc_port((short) port);
                               finP.setDest_port((short) clientPort);
                               send(clientAddress, clientPort, finP.toByteArray());
                               state = State.FIN_SEND;
                           }

                       }
                   }else if(state == State.FIN_SEND){
                       System.out.println("Four way handshake 1/4");
                       if(hs1.isAck_bit() && hs1.isFin_bit()){
                           System.out.println("Four way handshake 2/4");
                           System.out.println("Four way handshake 3/4");

                           //sending the last packet which is ack_seq
                           Packet ack_seq = new Packet();
                           ack_seq.setAck_bit(true);
                           ack_seq.setSequence_num(hs1.getSequence_num() + 1);

                           ack_seq.setSrc_port((short) port);
                           ack_seq.setDest_port((short) clientPort);
                           send(clientAddress, clientPort,ack_seq.toByteArray());
                           state = State.FIN_ACKD;
                           System.out.println("Four way handshake 4/4");
                       }
                   }
                   else if(state == State.FIN_ACKD){
                       socket.close();
                   }


               } catch (IOException e) {
                   System.err.println("Failed to received the packet!");
                   throw new RuntimeException(e);
               }

           }


       } catch (SocketException e) {
           System.err.println("Cannot connect to the server!");
           throw new RuntimeException(e);
       }

   }

    private void send(InetAddress address, int port, byte[] p){
        var outgoingPacket = new DatagramPacket(p, p.length, address, port);
        try {
            socket.send(outgoingPacket);
        } catch (IOException e) {
            System.out.println("Unable to send the packet!");
            throw new RuntimeException(e);
        }
    }

}


