package bgu.spl.net.impl.tftp;

import java.io.IOException;
import java.net.Socket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

class ListeningThread implements Runnable {
    private Socket socket;
    private ListeningProtocol protocol;
    private clientTftpEncoderDecoder encdec;

    public ListeningThread(Socket socket) {
        this.socket = socket;
        this.protocol = new ListeningProtocol();
        this.encdec = new clientTftpEncoderDecoder();
    }

    @Override
    public void run() {
        
        try (BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());){
            int read;

            while (!TftpClient.shouldTerminate && (read = in.read()) >= 0) {
                byte[] nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    byte[] response = protocol.process(nextMessage);
                    if (response != null && response.length > 0) {
                        try {
                            out.write(encdec.encode(response));
                            out.flush();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }               
            }
        }
        catch (Exception ignore) {}        
    }
 }
