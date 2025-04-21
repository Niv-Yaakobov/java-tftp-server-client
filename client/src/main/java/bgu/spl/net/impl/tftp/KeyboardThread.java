package bgu.spl.net.impl.tftp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class KeyboardThread implements Runnable {
    private Socket socket;
    public String input;
    public byte[] response;
    private clientTftpEncoderDecoder encdec;
    private KeyboardProtocol keyboardProtocol;

    public KeyboardThread(Socket socket) {
        this.socket = socket;
        this.keyboardProtocol = new KeyboardProtocol();
        this.encdec = new clientTftpEncoderDecoder();
    }

    @Override
    public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());) {

            while (!TftpClient.shouldTerminate) {
                while (!TftpClient.isDone){
                    // wait until done with prev command
                    System.out.print("");
                }
                if(TftpClient.shouldTerminate)
                    break;
                input = in.readLine();
                if (!TftpClient.shouldTerminate && input != null && input.length() > 0) {
                    String cmd = extractFirstWord(input);
                    if(input.length() > cmd.length())
                        input = input.substring(cmd.length() + 1);
                    else input="";
                    byte[] _cmd = cmdToOPCODE(cmd);
                    byte[] _input = input.getBytes();
                    byte[] msg = union(_cmd, _input);
                    response = keyboardProtocol.process(msg);
                    if(response != null && response.length > 0){
                        try {
                            out.write(encdec.encode(response));
                            out.flush();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractFirstWord(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        return input.trim().split("\\s+")[0];
    }

    private byte[] cmdToOPCODE(String cmd){
        short opcode = -1;
        switch (cmd){
            case "RRQ":
            opcode = 1;
            break;
            case "WRQ":
            opcode = 2;
            break;
            case "DATA":
            opcode = 3;
            break;
            case "ACK":
            opcode = 4;
            break;
            case "DIRQ":
            opcode = 6;
            break;
            case "LOGRQ":
            opcode = 7;
            break;
            case "DELRQ":
            opcode = 8;
            break;
            case "DISC":
            opcode = 10;
            break;

        }
        return convertShortToBytes(opcode);
    }

    private byte[] union(byte[] b1, byte[] b2){
        byte[] b = new byte[b1.length+b2.length];
        for (int i = 0; i<b1.length; i++){
            b[i] = b1[i];
        }
        for (int i = 0; i<b2.length; i++){
            b[i+b1.length] = b2[i];
        }
        return b;
    }

    private byte[] convertShortToBytes(short num) {
        return new byte[] { (byte) (num >> 8), (byte) (num & 0xff) };
    }
}
