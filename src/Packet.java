import jdk.jfr.Unsigned;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Packet {

    private short src_port; // 2 bytes
    private short dest_port; // 2 bytes
    private int sequence_num; // 4 bytes
    private int ack_num; // 4 bytes
    private boolean sync_bit; // 4 bytes
    private boolean ack_bit; // 4 bytes
    private boolean fin_bit; // 4 bytes
    private byte[] data;
    private int checksum; // 4 bytes

//    for viewing the packet is doing right and testing some code
    public static void main(String[] args){

        ImageHandler img = new ImageHandler("src/pic1.jpg");
        var imgBA = img.toImageByteArray();
        Packet p = new Packet((short) 3001, (short) 3002, 1, 0, true, false, false, 0, null);
        p.setData(imgBA);
        int total = 0;
        for(byte b: p.toByteArray()){
            total += Byte.toUnsignedInt(b);
        }
        System.out.println("Total sum of bytes with checksum included before: " + total);
        System.out.println("Before check sum" + p.toString());
        int check = p.calculateChecksum(p.toByteArray());
        p.setChecksum(check);
        int totalA = 0;
        for(byte b: p.toByteArray()){
            totalA += Byte.toUnsignedInt(b);
        }
        System.out.println("Total sum of bytes with checksum included before: " + totalA);
        System.out.println("After check sum" + p.toString());


    }

    public Packet(){
        this((short) 0, (short)0, 0, 0, false, false, false, 0, new byte[0]);
    }

    public Packet(short src_port, short dest_port, int sequence_num, int ack_num, boolean ack_bit, boolean sync_bit, boolean fin_bit, int checksum, byte[] data){
        this.src_port = src_port;
        this.dest_port = dest_port;
        this.sequence_num = sequence_num;
        this.ack_num = ack_num;
        this.sync_bit = sync_bit;
        this.ack_bit = ack_bit;
        this.fin_bit = fin_bit;
        this.checksum = checksum;
        this.data = data;
    }

    //if the packet it in byte array, it will convert into the type that each variable/field have
    public Packet(byte[] byteArray){
        byte[] trimmed = trimming(byteArray);
        this.src_port = ByteBuffer.wrap(trimmed, 0, 2).getShort();
        this.dest_port = ByteBuffer.wrap(trimmed, 2, 2).getShort();
        this.sequence_num = ByteBuffer.wrap(trimmed, 4, 4).getInt();
        this.ack_num = ByteBuffer.wrap(trimmed, 8, 4).getInt();
        this.ack_bit = ByteBuffer.wrap(trimmed, 12, 4).getInt() == 1;
        this.sync_bit = ByteBuffer.wrap(trimmed, 16, 4).getInt() == 1;
        this.fin_bit = ByteBuffer.wrap(trimmed, 20, 4).getInt() == 1;
        this.checksum = ByteBuffer.wrap(trimmed, 24, 4).getInt();
        if(trimmed.length > 28){
            this.data = Arrays.copyOfRange(byteArray, 28, byteArray.length);
        }
    }

    // convert header into byte array and put both header byte array and data into one byte array
    public byte[] toByteArray(){
        ByteBuffer buffer;
        if(this.data == null){
            buffer = ByteBuffer.allocate(28);
            buffer.putShort(this.src_port);
            buffer.putShort(this.dest_port);
            buffer.putInt(this.sequence_num);
            buffer.putInt(this.ack_num);
            buffer.putInt(this.ack_bit?1:0);
            buffer.putInt(this.sync_bit?1:0);
            buffer.putInt(this.fin_bit?1:0);
            buffer.putInt(this.checksum);
        }
        else {
            buffer = ByteBuffer.allocate(28 + this.data.length);
            buffer.putShort(this.src_port);
            buffer.putShort(this.dest_port);
            buffer.putInt(this.sequence_num);
            buffer.putInt(this.ack_num);
            buffer.putInt(this.ack_bit ? 1 : 0);
            buffer.putInt(this.sync_bit ? 1 : 0);
            buffer.putInt(this.fin_bit ? 1 : 0);
            buffer.putInt(this.checksum);
            buffer.put(this.data);
        }

        return buffer.array();
    }

    //calculating checksum by summing up all the bytes (converted into unsigned integers) in the byte array
    //without the checksum
    public int calculateChecksum(byte[] array){
        int checksum = 0;
        for (int i = 0; i < array.length; i++) {
            if(i < 24 || i > 27){
                checksum += Byte.toUnsignedInt(array[i]);
            }
        }
        return checksum;
    }

    public int getChecksum(){
        return this.checksum;
    }
    public void setChecksum(int checksum){
        this.checksum = checksum;
    }

    private byte[] trimming(byte[] byteArray){
        int i = byteArray.length - 1;
        while(i>=28 && byteArray[i] == 0){
            --i;
        }
        return Arrays.copyOf(byteArray, i + 1);
    }

    public short getSrc_port() {
        return src_port;
    }

    public void setSrc_port(short src_port) {
        this.src_port = src_port;
    }

    public short getDest_port() {
        return dest_port;
    }

    public void setDest_port(short dest_port) {
        this.dest_port = dest_port;
    }

    public int getSequence_num() {
        return sequence_num;
    }

    public void setSequence_num(int sequence_num) {
        this.sequence_num = sequence_num;
    }

    public int getAck_num() {
        return ack_num;
    }

    public void setAck_num(int ack_num) {
        this.ack_num = ack_num;
    }

    public boolean isSync_bit() {
        return sync_bit;
    }

    public void setSync_bit(boolean sync_bit) {
        this.sync_bit = sync_bit;
    }

    public boolean isAck_bit() {
        return ack_bit;
    }

    public void setAck_bit(boolean ack_bit) {
        this.ack_bit = ack_bit;
    }

    public boolean isFin_bit() {
        return fin_bit;
    }

    public void setFin_bit(boolean fin_bit) {
        this.fin_bit = fin_bit;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if(this.data == null){
            builder.append("\nSource Port= ").append(src_port).append("; \n").
                    append("Destination Port= ").append(dest_port).append("; \n").
                    append("SYNC Bit= ").append(sync_bit).append("; \n").
                    append("Sequence Number= ").append(sequence_num).append("; \n").
                    append("ACK Bit= ").append(ack_bit).append("; \n").
                    append("ACK Number= ").append(ack_num).append("; \n").
                    append("FIN Bit= ").append(fin_bit).append("; \n").
                    append("Checksum= ").append(checksum).append("; \n").
                    append("DATA= ").append("null;").append("\n");
        }
        else{
            builder.append("\nSource Port= ").append(src_port).append("; \n").
                    append("Destination Port= ").append(dest_port).append("; \n").
                    append("SYNC Bit= ").append(sync_bit).append("; \n").
                    append("Sequence Number= ").append(sequence_num).append("; \n").
                    append("ACK Bit= ").append(ack_bit).append("; \n").
                    append("ACK Number= ").append(ack_num).append("; \n").
                    append("FIN Bit= ").append(fin_bit).append("; \n").
                    append("Checksum= ").append(checksum).append("; \n").
                    append("DATA= ").append(Arrays.toString(data)).append("\n");
        }
        return builder.toString();
    }
}
