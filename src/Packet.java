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

    //for viewing the packet is doing right and testing some code
//    public static void main(String[] args){
//
//        byte[] data = new byte[128];
//        Packet p = new Packet((short) 3001, (short) 3002, 1, 0, true, false, false, data);
//        System.out.println(Arrays.toString(p.toByteArray()));
//        System.out.println(p.toByteArray().length -24);
//        var a = Arrays.copyOfRange(p.toByteArray(), 24, p.toByteArray().length);
//        System.out.println("data length: " + a.length);
//
//        System.out.println("data: " + Arrays.toString(a));
//    }

    public Packet(){
        this((short) 0, (short)0, 0, 0, false, false, false, null);
    }

    public Packet(short src_port, short dest_port, int sequence_num, int ack_num, boolean sync_bit, boolean ack_bit, boolean fin_bit, byte[] data){
        this.src_port = src_port;
        this.dest_port = dest_port;
        this.sequence_num = sequence_num;
        this.ack_num = ack_num;
        this.sync_bit = sync_bit;
        this.ack_bit = ack_bit;
        this.fin_bit = fin_bit;
        this.data = data;
    }

    //if the packet it in byte array, it will convert into the type that each variable/field have
    public Packet(byte[] byteArray){
        this.src_port = ByteBuffer.wrap(byteArray, 0, 2).getShort();
        this.dest_port = ByteBuffer.wrap(byteArray, 2, 2).getShort();
        this.sequence_num = ByteBuffer.wrap(byteArray, 4, 4).getInt();
        this.ack_num = ByteBuffer.wrap(byteArray, 8, 4).getInt();
        this.sync_bit = ByteBuffer.wrap(byteArray, 12, 4).getInt() == 1;
        this.ack_bit = ByteBuffer.wrap(byteArray, 16, 4).getInt() == 1;
        this.fin_bit = ByteBuffer.wrap(byteArray, 20, 4).getInt() == 1;
        this.data = Arrays.copyOfRange(byteArray, 24, byteArray.length);
    }

    // convert header into byte array and put both header byte array and data into one byte array
    public byte[] toByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(24 + this.data.length);
        buffer.putShort(this.src_port);
        buffer.putShort(this.dest_port);
        buffer.putInt(this.sequence_num);
        buffer.putInt(this.sequence_num);
        buffer.putInt(this.sync_bit?1:0);
        buffer.putInt(this.ack_bit?1:0);
        buffer.putInt(this.fin_bit?1:0);
        buffer.put(this.data);

        return buffer.array();
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

}
