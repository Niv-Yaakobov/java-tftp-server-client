package bgu.spl.net.impl.tftp;

import java.io.IOException;

import bgu.spl.net.srv.ConnectionHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

public class TftpConnectionHandler implements Runnable, ConnectionHandler<byte[]> {

    private final TftpProtocol protocol;
    private final TftpEncoderDecoder encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;

    public TftpConnectionHandler(Socket sock, TftpEncoderDecoder reader, TftpProtocol protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { 
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                byte[] nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(byte[] msg) {
        if (msg != null) {
            try {
                out.write(encdec.encode(msg));
                out.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public TftpProtocol getProtocol() {
        return protocol;
    }
}

