import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

public class Packet {

    private short src_port; // 2 bytes
    private short dest_port; // 2 bytes
    private int sequence_num; // 4 bytes
    private int ack_num; // 4 bytes
    private boolean sync_bit; // 4 bytes
    private boolean ack_bit; // 4 bytes
    private boolean fin_bit; // 4 bytes
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
