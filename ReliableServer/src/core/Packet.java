package core;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Packet {

    public static final int MIN_BYTES = 11;
    public static final int MAX_PAYLOAD = 1013;
    public static final int MAX_BYTES = MIN_BYTES + MAX_PAYLOAD;

    private byte type;
    private int seqNumber;
    private InetAddress peerAddress;
    private int peerPort;
    private byte[] payload;

    public Packet(byte type, int seqNumber, InetAddress peerAddress, int peerPort, byte[] payload) {
        this.type = type;
        this.seqNumber = seqNumber;
        this.peerAddress = peerAddress;
        this.peerPort = peerPort;
        this.payload = payload;
    }

    public byte getType() {
        return type;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public InetAddress getPeerAddress() {
        return peerAddress;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
    }

    public void setPeerAddress(InetAddress peerAddress) {
        this.peerAddress = peerAddress;
    }

    public void setPeerPort(short peerPort) {
        this.peerPort = peerPort;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public static Packet fromBytes(ByteBuffer buffer) throws IOException {

        PacketBuilder builder = new PacketBuilder()
                .setType(buffer.get())
                .setSeqNumber(buffer.getInt());

        //Get address from buffer
        byte[] host = {buffer.get(), buffer.get(), buffer.get(), buffer.get()};
        builder.setPeerAddress(InetAddress.getByAddress(host));
        builder.setPeerPort(buffer.getShort());

        byte[] payload = new byte[buffer.remaining()];
        buffer.get(payload);
        builder.setPayload(payload);

        return builder.build();
    }

    public byte[] toBytes() {

        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_BYTES).order(ByteOrder.BIG_ENDIAN);

        byteBuffer.put(type);
        byteBuffer.putInt(seqNumber);
        byteBuffer.put(peerAddress.getAddress());
        byteBuffer.putShort((short)peerPort);
        byteBuffer.put(payload);
        byteBuffer.flip();

        return byteBuffer.array();
    }

    public String toString() {

        return  "type: " + type +"\t" +
                "seq number: " + seqNumber +"\t" +
                "port: " + peerPort +"\t" +
                "address: " + peerAddress.getHostAddress() +"\t" +
                "payload-len: " + payload.length + "";

    }


    //Builder pattern due to large constructor
    public static class PacketBuilder {

        private byte type;
        private int seqNumber;
        private InetAddress peerAddress;
        private int peerPort;
        private byte[] payload;

        public PacketBuilder() {
            this.type = 0;
            this.seqNumber = 0;
            this.peerAddress = null;
            this.peerPort = 0;
            this.payload = new byte[MAX_PAYLOAD];
        }

        public PacketBuilder setType(byte type) {
            this.type = type;
            return this;
        }

        public PacketBuilder setSeqNumber(int seqNumber) {

            this.seqNumber = seqNumber;
            return this;
        }

        public PacketBuilder setPeerAddress(InetAddress peerAddress) {

            this.peerAddress = peerAddress;
            return this;
        }

        public PacketBuilder setPeerPort(int peerPort) {

            this.peerPort = peerPort;
            return this;
        }

        public PacketBuilder setPayload(byte[] payload) {

            this.payload = payload;
            return this;
        }

        public Packet build() {
            return new Packet(type, seqNumber, peerAddress, peerPort, payload);
        }
    }

}
