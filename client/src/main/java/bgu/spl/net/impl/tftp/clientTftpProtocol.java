package bgu.spl.net.impl.tftp;

import java.nio.charset.Charset;
import java.util.Arrays;

public abstract class clientTftpProtocol{

    protected byte[] response; 
    protected byte[] msg;
        
    public static void printByteArray(byte[] byteArray) {
        // Print as integers
        System.out.print("Array as integers: ");
        for (byte b : byteArray) {
            System.out.print((int) b + " ");
        }
        System.out.println();

        // Print as string
        String stringRepresentation = new String(byteArray);
        System.out.println("Array as string: " + stringRepresentation);
    }
    

    public byte[] process(byte[] message) {
        response = null;
        this.msg = Arrays.copyOf(message, message.length);
        short opcode = convertBytesToShort(msg[0], msg[1]);
        switch (opcode) {    
        case 1:
            RRQ();
            break;
        case 2:
            WRQ();
            break;
        case 3:
            DATA();
            break;
        case 4:
            ACK();
            break;
        case 5:
            ERROR();
            break;
        case 6:
            DIRQ();
            break;
        case 7:
            LOGRQ();
            break;
        case 8:
            DELRQ();
            break;
        case 9:
            BCAST();
            break;
        case 10:
            DISC();
            break;
        default:
            System.out.println("invalid command: " + opcode);
        }
        return response;
    }

    public abstract void RRQ();
    public abstract void WRQ();
    public abstract void DATA();
    public abstract void ACK();
    public abstract void ERROR();
    public abstract void DIRQ();
    public abstract void LOGRQ();
    public abstract void DELRQ();
    public abstract void BCAST();
    public abstract void DISC();


    // ......................................................................
    public short convertBytesToShort(byte byte1, byte byte2) {
        return (short) (((short) (byte1 & 0xFF) << 8) | ((short) (byte2 & 0xFF)));
    }

    public byte[] convertShortToBytes(short num) {
        return new byte[] { (byte) ((num >> 8) & 0xFF),(byte) (num & 0xFF)};
    }

    public String convertByteToString(byte[] byteArray) {
        return new String(byteArray, Charset.forName("UTF-8"));
    }
}

