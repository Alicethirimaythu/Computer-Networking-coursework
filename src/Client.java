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
    private int serverPort = 80;
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
                Packet hs2 = new Packet(buff);
                System.out.println("\n<<Received from server>> " + hs2.toString());

                if(state == State.SYN_SEND){

                    //third handshake where client send ack packet to server
                    if(hs2.getAck_num() == seq_num+1){
                        System.out.println("\nThree way handshake 2/3");

                        //send ACK packet to server
                        Packet hs3 = new Packet();
                        seq_num = hs2.getAck_num();
                        hs3.setSequence_num(seq_num);
                        hs3.setSync_bit(false);

                        ack_num = hs2.getSequence_num() + 1;
                        hs3.setAck_num(ack_num);
                        hs3.setAck_bit(true);

                        hs3.setDest_port((short)serverPort);
                        hs3.setSrc_port((short)selfPort);
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
                    if(hs2.getSequence_num() > seq_num && !hs2.isFin_bit()){
                        img_list.add(hs2.getData());
                        seq_num = hs2.getSequence_num();
                        ack.setAck_bit(true);
                        ack.setAck_num(hs2.getSequence_num() + hs2.getData().length);

                        ack.setDest_port((short)serverPort);
                        ack.setSrc_port((short)selfPort);
                        send(ack.toByteArray());

                    }else if(hs2.isFin_bit()){
                        ImageHandler imgH = new ImageHandler(img_list);
                        imgH.Convert_toImage();

                        state = State.FIN_RECV;
                        System.out.println("Four way handshake 1/4");

                        // send fin_ack packet
                        Packet finP = new Packet();

                        finP.setAck_bit(true);
                        finP.setAck_num(hs2.getSequence_num() + 1);

                        finP.setDest_port((short)serverPort);
                        finP.setSrc_port((short)selfPort);

                        finP.setFin_bit(true);
                        seq_num = ack_num + 1;
                        finP.setSequence_num(seq_num);

                        send(finP.toByteArray());
                        state = State.FIN_SEND;
                        send(finP.toByteArray());
                        System.out.println("Four way handshake 2/4");
                        System.out.println("Four way handshake 3/4");
                    }
                }else if(state == State.FIN_SEND){

                    if(hs2.isAck_bit() && hs2.getSequence_num() == (++seq_num)){
                        System.out.println("Four way handshake 4/4");
                        clientSock.close();
                    }
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