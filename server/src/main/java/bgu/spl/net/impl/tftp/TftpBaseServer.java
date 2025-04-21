package bgu.spl.net.impl.tftp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class TftpBaseServer implements I_TftpServer {

    private final int port;
    private final Supplier<TftpProtocol> protocolFactory;
    private final Supplier<TftpEncoderDecoder> encdecFactory;
    private ServerSocket sock;
    private TftpConnections<byte[]> connections;
    private int connectionsCounter = 0;

    public TftpBaseServer(
            int port,
            Supplier<TftpProtocol> protocolFactory,
            Supplier<TftpEncoderDecoder> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
        this.connections = new TftpConnections();
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock; //just to be able to close
            
            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();

                TftpConnectionHandler handler = new TftpConnectionHandler(
                        clientSock,
                        encdecFactory.get(),
                        protocolFactory.get());

                connections.connect(connectionsCounter++, handler);
                execute(handler);
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(TftpConnectionHandler handler);

}

