package bgu.spl.net.impl.tftp;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class TftpConnections<T> implements Connections<T> {

    public ConcurrentHashMap<Integer, ConnectionHandler<T>> loginIds = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer, String> loggedUsernames = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, Integer> filesLibrary = new ConcurrentHashMap<>();

    public TftpConnections() {
        File folder = new File("./Flies");
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                filesLibrary.put(file.getName(), 0);
            }
        }
    }

    @Override
    public void connect(int connectionId, ConnectionHandler<T> handler) {
        loginIds.put(connectionId, handler);
        ((TftpConnectionHandler) (handler)).getProtocol().start(connectionId, (Connections<byte[]>)this);
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if (!loginIds.containsKey(connectionId) || msg == null)
            return false;
        loginIds.get(connectionId).send(msg);
        return true;
    }

    @Override
    public void disconnect(int connectionId) {
        if (!loginIds.containsKey(connectionId))
            return;
        try {
            Thread.sleep(10);  
            loginIds.get(connectionId).close();
        } catch (Exception ignore) {
        }
        loginIds.remove(connectionId);
    }

}
