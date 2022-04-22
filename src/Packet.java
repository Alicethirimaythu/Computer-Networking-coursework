public class Packet {

    private short src_port;
    private short dest_port;
    private int sequence_num;
    private int ack_num;
    private boolean sync_bit;
    private boolean ack_bit;
    private boolean fin_bit;
    private byte[] data;

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

}
