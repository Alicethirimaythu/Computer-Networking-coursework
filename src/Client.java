import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Client {

    private static final int MAX_BUFFERSIZE = 1024;
    private static InetAddress SERVER_HOSTNAME;
    private int serverPort = 8080;
    private int selfPort = 12500;
    private State state = State.NONE;
    private int seq_num = 0;
    private int ack_num = 0;
    private List<byte[]> img_list = new ArrayList<>();

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

        while(!clientSock.isClosed()){
            if(state == State.NONE){
                // Initial SYNC packet from the client send to the server to start the first handshake
                Packet hs1 = new Packet();
                seq_num = ThreadLocalRandom.current().nextInt(0, 2147483647);

                hs1.setSequence_num(seq_num);
                hs1.setSync_bit(true);
                hs1.setDest_port((short)serverPort);
                hs1.setSrc_port((short)selfPort);
                hs1.setData(null);
                hs1.setChecksum(hs1.calculateChecksum(hs1.toByteArray()));
                state = State.SYN_SEND;
                //System.out.println("hs1 packet: " + hs1.toString());

                send(hs1.toByteArray());
                System.out.println("\nThree way handshake 1/3!");
            }

            // second handshake where client received sync+ack packet from the server
            byte[] buff = new byte[MAX_BUFFERSIZE];
            DatagramPacket packet = new DatagramPacket(buff, buff.length);


            try {
                clientSock.receive(packet);
                Packet receivedPacket = new Packet(buff);
                System.out.println("\n<<Received from server>> " + receivedPacket.toString());

                int checksum = receivedPacket.calculateChecksum(receivedPacket.toByteArray());
                System.out.println("Checksum: " + checksum);

                if(checksum == receivedPacket.getChecksum()){
                    if(state == State.SYN_SEND){

                        //third handshake where client send ack packet to server
                        if(receivedPacket.getAck_num() == seq_num+1){
                            System.out.println("\nThree way handshake 2/3");

                            //send ACK packet to server
                            Packet hs3 = new Packet();
                            seq_num = receivedPacket.getAck_num();
                            hs3.setSequence_num(seq_num);
                            hs3.setSync_bit(false);

                            ack_num = receivedPacket.getSequence_num() + 1;
                            hs3.setAck_num(ack_num);
                            hs3.setAck_bit(true);

                            hs3.setDest_port((short)serverPort);
                            hs3.setSrc_port((short)selfPort);

                            hs3.setChecksum(hs3.calculateChecksum(hs3.toByteArray()));
                            send(hs3.toByteArray());
                            System.out.println("\nThree way handshake 3/3");
                            state = State.ESTABLISHED;
                            seq_num = ack_num;
                            seq_num--;
                        }
                    }else if(state == State.ESTABLISHED){
                        // to store all the image packets send from the server.


                        // receive the image data
                        Packet ack = new Packet();
                        if(receivedPacket.getSequence_num() > seq_num && !receivedPacket.isFin_bit()){
                            img_list.add(receivedPacket.getData());
                            seq_num = receivedPacket.getSequence_num();
                            ack.setSequence_num(receivedPacket.getAck_num());
                            ack.setAck_bit(true);
                            ack.setAck_num(receivedPacket.getSequence_num()+ receivedPacket.getData().length);

                            ack.setDest_port((short)serverPort);
                            ack.setSrc_port((short)selfPort);
                            ack.setChecksum(ack.calculateChecksum(ack.toByteArray()));
                            send(ack.toByteArray());

                        }else if(receivedPacket.isFin_bit()){
                            ImageHandler imgH = new ImageHandler(img_list);
                            imgH.Convert_toImage();

                            System.out.println("Four way handshake 1/4");

                            // send ack packet
                            Packet ackPack = new Packet();

                            ackPack.setSequence_num(receivedPacket.getAck_num());
                            ackPack.setAck_bit(true);
                            ackPack.setAck_num(receivedPacket.getSequence_num());
                            ackPack.setFin_bit(false);

                            ackPack.setDest_port((short)serverPort);
                            ackPack.setSrc_port((short)selfPort);

                            ackPack.setChecksum(ackPack.calculateChecksum(ackPack.toByteArray()));
                            send(ackPack.toByteArray());
                            System.out.println("Four way handshake 2/4");

                            // send fin_ack packet
                            Packet finP = new Packet();

                            finP.setAck_bit(true);
                            finP.setAck_num(receivedPacket.getAck_num() + 1);

                            finP.setDest_port((short)serverPort);
                            finP.setSrc_port((short)selfPort);

                            finP.setFin_bit(true);
                            finP.setSequence_num(receivedPacket.getSequence_num());
                            finP.setChecksum(finP.calculateChecksum(finP.toByteArray()));

                            state = State.FIN_SEND;
                            send(finP.toByteArray());
                            System.out.println("Four way handshake 3/4");
                        }
                    }else if(state == State.FIN_SEND){

                        if(receivedPacket.isAck_bit() && receivedPacket.isFin_bit()){
                            System.out.println("Four way handshake 4/4");
                            clientSock.close();
                            System.out.println("Disconnected!");
                        }
                    }
                }else{
                    System.out.println("The packet is corrupted as the checksum is not the same!");
                }

            } catch (IOException e) {
                System.err.println("Failed to received packet!");
                throw new RuntimeException(e);
            }

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

}