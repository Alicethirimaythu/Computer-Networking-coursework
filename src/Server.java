import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class Server {

    private static DatagramSocket socket;
    private int port = 3003;
    private String Servermessage = "Message is sent from the server";

    public static void main(String[] args){
        var server = new Server();
        server.start();
    }
    public void start(){

        if(socket != null){
            return;
        }

        try {
            socket = new DatagramSocket(port);

            System.out.println("Server is listening in port " + port + "!");


            byte[] buffer = new byte[256];
            while(!socket.isClosed()){

                try {
                    var incomingPacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(incomingPacket);


                    var clientAddress = incomingPacket.getAddress();
                    var clientPort = incomingPacket.getPort();

                    var message = new String(
                            incomingPacket.getData(),
                            0,
                            incomingPacket.getLength(),
                            StandardCharsets.UTF_8
                    );

                    // if the message is "exit", stop the server.
                    if(message.equalsIgnoreCase("exit")){
                        socket.close();
                        socket = null;
                        break;
                    }

                    System.out.println("Client>> " + message);

                    var serverMsg = Servermessage.getBytes(StandardCharsets.UTF_8);
                    var outgoingPacket = new DatagramPacket(serverMsg, serverMsg.length,
                            clientAddress, clientPort);

                    socket.send(outgoingPacket);
                } catch (IOException e) {
                    System.err.println(
                            "Communication error. " +
                                    "Is there a problem with the client?"
                    );
                }

            }
        } catch (SocketException ex) {
            System.err.println(
                    "Failed to start the server. " +
                            "Is the port already taken?"
            );
            ex.printStackTrace();
        }

    }
}
