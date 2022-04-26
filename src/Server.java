import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

public class Server {

    private static final int MAX_BUFFERSIZE = 512;
    private static DatagramSocket socket;
    private final int port = 8080;
    private State state = State.NONE;
    private int seq_num = 0;
    private int ack_num = 0;
    private String Servermessage = "Message is sent from the server";

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
                       if(hs1.isAck_bit() && hs1.getAck_num() == (++seq_num)){
                           state = State.ESTABLISHED;
                           System.out.println("\nThree way handshake 3/3! "
                                                + "Connection Established");


                       }
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







//    public void start(){
//
//        if(socket != null){
//            return;
//        }
//
//        try {
//            socket = new DatagramSocket(port);
//
//            System.out.println("Server is listening in port " + port + "!");
//
//
//            byte[] buffer = new byte[256];
//            while(!socket.isClosed()){
//
//                try {
//                    var incomingPacket = new DatagramPacket(buffer, buffer.length);
//                    socket.receive(incomingPacket);
//
//
//                    var clientAddress = incomingPacket.getAddress();
//                    var clientPort = incomingPacket.getPort();
//
//                    var message = new String(
//                            incomingPacket.getData(),
//                            0,
//                            incomingPacket.getLength(),
//                            StandardCharsets.UTF_8
//                    );
//
//                    // if the message is "exit", stop the server.
//                    if(message.equalsIgnoreCase("exit")){
//                        socket.close();
//                        socket = null;
//                        break;
//                    }
//
//                    System.out.println("Client>> " + message);
//
//                    var serverMsg = Servermessage.getBytes(StandardCharsets.UTF_8);
//                    var outgoingPacket = new DatagramPacket(serverMsg, serverMsg.length,
//                            clientAddress, clientPort);
//
//                    socket.send(outgoingPacket);
//                } catch (IOException e) {
//                    System.err.println(
//                            "Communication error. " +
//                                    "Is there a problem with the client?"
//                    );
//                }
//
//            }
//        } catch (SocketException ex) {
//            System.err.println(
//                    "Failed to start the server. " +
//                            "Is the port already taken?"
//            );
//            ex.printStackTrace();
//        }
//
//    }
//}
