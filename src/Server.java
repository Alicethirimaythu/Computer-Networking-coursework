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
                   Packet receivedPacket = new Packet(buff);
                   var clientAddress = packet.getAddress();
                   var clientPort = packet.getPort();
                   System.out.println("\n<<Received from Client>> " + receivedPacket.toString());

                   //checking each packet with the checksum
                   int checksum = receivedPacket.calculateChecksum(receivedPacket.toByteArray());
                   System.out.println("checksum: " + checksum);

                   if(checksum == receivedPacket.getChecksum()){
                       if(state == State.NONE){

                           // second handshake where server acknowledge and send sync+ack packet to client
                           if(receivedPacket.isSync_bit()){
                               System.out.println("\nThree way handshake 1/3!");

                               //send ACK+SYN packet
                               Packet hs2 = new Packet();
                               hs2.setAck_bit(true);
                               ack_num = receivedPacket.getSequence_num()+1;
                               hs2.setAck_num(ack_num);

                               seq_num = ThreadLocalRandom.current().nextInt(0, 2147483647);
                               hs2.setSync_bit(true);
                               hs2.setSequence_num(seq_num);

                               hs2.setSrc_port((short) port);
                               hs2.setDest_port((short) clientPort);

                               hs2.setChecksum(hs2.calculateChecksum(hs2.toByteArray()));
                               state = State.SYN_RECV;
                               send(clientAddress, clientPort, hs2.toByteArray());
                               System.out.println("\nThree way handshake 2/3!");
                           }
                       }else if(state == State.SYN_RECV){
                           //received the ack packet and the three handshake is done and connection established.
                           if(receivedPacket.isAck_bit() && receivedPacket.getAck_num() == (++seq_num)){
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

                               imageP.setSequence_num(receivedPacket.getAck_num());
                               imageP.setSync_bit(false);

                               imageP.setAck_num(receivedPacket.getSequence_num());
                               imageP.setAck_bit(true);

                               imageP.setSrc_port((short) port);
                               imageP.setDest_port((short) clientPort);

//                               var checksum1 = imageP.calculateChecksum(imageP.toByteArray());
//                               System.out.println("First image packet sent to client's checksum: " + checksum1);
                               imageP.setChecksum(imageP.calculateChecksum(imageP.toByteArray()));
                               send(clientAddress, clientPort, imageP.toByteArray());
                               counter++;
                           }
                       }else if(state == State.ESTABLISHED){
                           if(receivedPacket.isAck_bit()){
                               ImageHandler image = new ImageHandler("src/inside.jpg");
                               var imageBA = image.toImageByteArray();
                               var imglist = image.getListOfImgPacket(MAX_BUFFERSIZE, imageBA);
                               //System.out.println("image list size: " + imglist.size());
                               // if the image has more than one packet
                               if (imglist.size() != 1 && counter < imglist.size()) {
                                   Packet imgP = new Packet();

                                   imgP.setData(imglist.get(counter));

                                   imgP.setSequence_num(receivedPacket.getAck_num());
                                   imgP.setSync_bit(false);

                                   imgP.setAck_num(receivedPacket.getSequence_num());
                                   imgP.setAck_bit(true);

                                   imgP.setSrc_port((short) port);
                                   imgP.setDest_port((short) clientPort);

                                   imgP.setChecksum(imgP.calculateChecksum(imgP.toByteArray()));
                                   send(clientAddress, clientPort, imgP.toByteArray());
                                   counter++;
                               }
                               else{
                                   System.out.println("Sent all the packets!");
                                   Packet finP = new Packet();
                                   finP.setSequence_num(receivedPacket.getAck_num());
                                   finP.setFin_bit(true);
                                   finP.setAck_num(receivedPacket.getSequence_num() + 1);
                                   finP.setAck_bit(true);
                                   finP.setSrc_port((short) port);
                                   finP.setDest_port((short) clientPort);
                                   finP.setChecksum(finP.calculateChecksum(finP.toByteArray()));
                                   send(clientAddress, clientPort, finP.toByteArray());
                                   state = State.FIN_SEND;
                               }

                           }
                       }else if(state == State.FIN_SEND){
                           System.out.println("Four way handshake 1/4");
                           if(receivedPacket.isAck_bit() && !receivedPacket.isFin_bit()){
                               System.out.println("Four way handshake 2/4");
                               state = State.FIN_RECV;
                           }
                       }
                       else if(state == State.FIN_RECV){
                           if(receivedPacket.isAck_bit() && receivedPacket.isFin_bit()){
                               System.out.println("Four way handshake 3/4");

                               //sending the last packet which is ack_seq
                               Packet ack_seq = new Packet();
                               ack_seq.setAck_bit(true);
                               ack_seq.setFin_bit(true);
                               ack_seq.setAck_num(receivedPacket.getSequence_num() + 1);
                               ack_seq.setSequence_num(receivedPacket.getAck_num());

                               ack_seq.setSrc_port((short) port);
                               ack_seq.setDest_port((short) clientPort);
                               ack_seq.setChecksum(ack_seq.calculateChecksum(ack_seq.toByteArray()));
                               send(clientAddress, clientPort,ack_seq.toByteArray());
                               state = State.FIN_ACKD;
                               System.out.println("Four way handshake 4/4");
                               socket.close();
                               System.out.println("Disconnected!");
                           }
                       }

                   }else{
                       System.out.println("The packet is corrupted as the checksum is not the same!");
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


