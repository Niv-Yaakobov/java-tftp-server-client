package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private TftpConnections<byte[]> connections;
    private int connectionId;
    private boolean shouldTerminate = false;
    private Procces p;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connectionId = connectionId;
        this.connections = (TftpConnections) connections;
        this.p = new Procces(connectionId, this.connections);
    }

    @Override
    public void process(byte[] message) {
        byte[]response = p.handleMessage(message);
        if(p.hasResponse){
            connections.send(connectionId, response);
            if(p.bcastResponse != null){
                for (Integer id : connections.loggedUsernames.keySet()){
                    connections.send(id,p.bcastResponse);
                }
                p.bcastResponse = null;
            }
        }
        if(message[0] == 0 & message[1] == 10){ //the message is DISC
            shouldTerminate = true;
            connections.disconnect(connectionId);
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    } 

    public Connections getConnections(){
        return connections;
    }
}
