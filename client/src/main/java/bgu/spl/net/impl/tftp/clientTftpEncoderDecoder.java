package bgu.spl.net.impl.tftp;

import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;

public class clientTftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {

    private byte[] bytes = new byte[1 << 10]; // start with 1k
    private int len = 0;
    private int lengthOfPacket = -1;
    private int startSearch = 2;

    @Override
    public byte[] decodeNextByte(byte nextByte) {
        pushByte(nextByte);
        if(len == 2){
            short opcode = convertBytesToShort(bytes[0],bytes[1]);
            switch (opcode) {
                case 4:
                    lengthOfPacket = 4;
                    startSearch = Integer.MAX_VALUE;
                    break;
                case 5:
                    startSearch = 4;
                    break;
                case 3:
                    startSearch = 4;
                    break;
                case 9:
                    startSearch = 3;
                    break;
            }
        }
        else if (len == 4){
            short opcode = convertBytesToShort(bytes[0],bytes[1]);
            if(opcode == 3){
                lengthOfPacket = 6 + convertBytesToShort(bytes[2],bytes[3]);
                startSearch = Integer.MAX_VALUE;
            }
        }
        if ((nextByte == '\0' && len > startSearch) || len == lengthOfPacket) {
            byte[] message = Arrays.copyOf(bytes, len);
            len = 0;
            lengthOfPacket = -1;
            startSearch = 2;
            return message;
        }
        return null; 
    }

    @Override
    public byte[] encode(byte[] message) {
        return message;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private short convertBytesToShort(byte byte1, byte byte2) {
        return (short) (((short) byte1) << 8 | ((short) (byte2)));
    }
}