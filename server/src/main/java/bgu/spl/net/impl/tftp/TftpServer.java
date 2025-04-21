package bgu.spl.net.impl.tftp;

public class TftpServer {
    public static void main(String[] args) {

        // you can use any server... 
        I_TftpServer.threadPerClient(
                7777, //port
                () -> new TftpProtocol(), //protocol factory
                TftpEncoderDecoder::new //message encoder decoder factory
        ).serve();

    }
}