import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Client {

    private static final int MAX_BUFFERSIZE = 512;
    private static InetAddress SERVER_HOSTNAME;
    private int serverPort = 1234;
    private int selfPort = 5678;
    private State state = State.NONE;
    private int seq_num = 0;
    private int ack_num = 0;

    DatagramSocket clientSock;

    public static void main(String[] args){
        var client = new Client();
        client.start();
    }

    public void start(){
        try {
            SERVER_HOSTNAME = InetAddress.getByName("localhost");
            clientSock = new DatagramSocket(selfPort);
            System.out.println("Client is connected at address: " +
                    SERVER_HOSTNAME + " and port: " + selfPort);
        } catch (SocketException e) {
            System.err.println("Cannot bind the client at port " + selfPort + "!");
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            System.err.println("Server address is unknown!");
            throw new RuntimeException(e);
        }

        // Initial SYNC packet from the client send to the server to start the first handshake
        Packet hs1 = new Packet();
        seq_num = ThreadLocalRandom.current().nextInt(0, 2147483647);

        hs1.setSequence_num(seq_num);
        hs1.setSync_bit(true);
        hs1.setDest_port((short)serverPort);
        hs1.setSrc_port((short)selfPort);
        hs1.setData(null);
        state = State.SYN_SEND;

        send(hs1.toByteArray());
        System.out.println("Three way handshake 1/3!");

        // second handshake where client received sync+ack packet from the server
        byte[] buff = new byte[MAX_BUFFERSIZE];
        DatagramPacket packet = new DatagramPacket(buff, buff.length);

        try {
            clientSock.receive(packet);
            Packet hs2 = new Packet(buff);
            System.out.println("Received from server>> " + hs2.toString());

            if(state == State.SYN_SEND){

                //third handshake where client send ack packet to server
                if(hs2.getAck_num() == seq_num+1){
                    System.out.println("Three way handshake 2/3");

                    //send ACK packet to server
                    Packet hs3 = new Packet();
                    seq_num = hs2.getAck_num();
                    hs3.setSequence_num(seq_num);
                    hs3.setSync_bit(true);

                    ack_num = hs2.getSequence_num() + 1;
                    hs3.setAck_num(ack_num);
                    hs3.setAck_bit(true);

                    hs3.setDest_port((short)serverPort);
                    hs3.setSrc_port((short)selfPort);
                    send(hs3.toByteArray());
                    System.out.println("Three way handshake 3/3");
                    //...
                }
            }


        } catch (IOException e) {
            System.err.println("Failed to received packet!");
            throw new RuntimeException(e);
        }


    }

    private void send(byte[] packet){
        send(packet, SERVER_HOSTNAME, serverPort);
    }
    private void send(byte[] p, InetAddress address, int port){
        var outgoingPacket = new DatagramPacket(p, p.length, address, port);
        try {
            clientSock.send(outgoingPacket);

        } catch (IOException e) {
            System.err.println("Unable to send the packet!");
            throw new RuntimeException(e);
        }
    }




//    public void start(){
//        var scanner = new Scanner(System.in);
//
//        try {
//            clientSock = new DatagramSocket(clientPort);
//            System.out.println("Client is port is: " + clientPort);
//
//        } catch (SocketException ex) {
//            System.err.println(
//                    "Failed to initialize the client socket. " +
//                            "Is there a free port?"
//            );
//            ex.printStackTrace();
//        }
//
//        final InetAddress serverAddress;
//        try {
//            serverAddress = InetAddress.getByName(SERVER_HOSTNAME);
//        } catch (UnknownHostException ex) {
//            System.err.println("Unknown host: " + SERVER_HOSTNAME);
//            ex.printStackTrace();
//            return;
//        }
//
//        System.out.print("> ");
//
//        byte[] buffer = new byte[256];
//
//        while (!clientSock.isClosed()){
//
//            try {
//                if(System.in.available() > 0){
//                    String message = scanner.nextLine();
//
//                    if(message.equalsIgnoreCase("exit")){
//                        var exitBuffer = message.getBytes(StandardCharsets.UTF_8);
//                        clientSock.send(new DatagramPacket(
//                                exitBuffer,
//                                exitBuffer.length,
//                                serverAddress,
//                                serverPort
//                        ));
//
//                        clientSock.close();
//                        break;
//                    }
//
//                    var messageBuffer = message.getBytes(StandardCharsets.UTF_8);
//                    clientSock.send(new DatagramPacket(
//                            messageBuffer,
//                            messageBuffer.length,
//                            serverAddress,
//                            serverPort
//                    ));
//
//                    var incomingPacket = new DatagramPacket(
//                            buffer,
//                            buffer.length,
//                            serverAddress,
//                            serverPort
//                    );
//                    clientSock.receive(incomingPacket);
//
//                    var messageResponse = new String(
//                            incomingPacket.getData(), 0, incomingPacket.getLength(),
//                            StandardCharsets.UTF_8
//                    );
//
//                    System.out.println("Server: " + messageResponse);
//
//                    System.out.print("> ");
//
//
//                }
//            }  catch (IOException ex) {
//                // If we encounter an IOException, it means there was a
//                // problem communicating (IO = Input/Output) so we'll log
//                // the error.
//                System.err.println(
//                        "A communication error occurred with the server."
//                );
//                ex.printStackTrace();
//                break;
//            }
//        }
//    }

}
