import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {

    public static final String SERVER_HOSTNAME = "localhost";
    private int serverPort = 3000;

    DatagramSocket clientSock;

    public static void main(String[] args){
        var client = new Client();
        client.start();
    }

    public void start(){
        var scanner = new Scanner(System.in);

        try {
            clientSock = new DatagramSocket();

        } catch (SocketException ex) {
            System.err.println(
                    "Failed to initialize the client socket. " +
                            "Is there a free port?"
            );
            ex.printStackTrace();
        }

        final InetAddress serverAddress;
        try {
            serverAddress = InetAddress.getByName(SERVER_HOSTNAME);
        } catch (UnknownHostException ex) {
            System.err.println("Unknown host: " + SERVER_HOSTNAME);
            ex.printStackTrace();
            return;
        }

        System.out.println("> ");

        byte[] buffer = new byte[256];

        while (!clientSock.isClosed()){

            try {
                if(System.in.available() > 0){
                    String message = scanner.nextLine();

                    if(message.equalsIgnoreCase("exit")){
                        var exitBuffer = message.getBytes(StandardCharsets.UTF_8);
                        clientSock.send(new DatagramPacket(
                                exitBuffer,
                                exitBuffer.length,
                                serverAddress,
                                serverPort
                        ));

                        clientSock.close();
                        break;
                    }

                    var messageBuffer = message.getBytes(StandardCharsets.UTF_8);
                    clientSock.send(new DatagramPacket(
                            messageBuffer,
                            messageBuffer.length,
                            serverAddress,
                            serverPort
                    ));

                    var incomingPacket = new DatagramPacket(
                            buffer,
                            buffer.length,
                            serverAddress,
                            serverPort
                    );
                    clientSock.receive(incomingPacket);

                    var messageResponse = new String(
                            incomingPacket.getData(), 0, incomingPacket.getLength(),
                            StandardCharsets.UTF_8
                    );

                    System.out.println("Server: " + messageResponse);

                    System.out.print("> ");


                }
            }  catch (IOException ex) {
                // If we encounter an IOException, it means there was a
                // problem communicating (IO = Input/Output) so we'll log
                // the error.
                System.err.println(
                        "A communication error occurred with the server."
                );
                ex.printStackTrace();
                break;
            }
        }
    }

}
