package bgu.spl.net.impl.tftp;

import java.net.Socket;
import java.io.IOException;


public class TftpClient {

    public static String command = "";
    public static short commandCode = -1;
    public static boolean isDone = true;
    public static boolean isLoggedIn = false;
    public static boolean shouldTerminate = false;
    public static String clientFiles_path = "./";

    public static void finishCurrentCommand(){
        command = "";
        commandCode = -1;
        isDone = true;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        KeyboardThread keyboard;
        ListeningThread listening;
        if (args.length == 0) {
            args = new String[]{"192.168.1.71"};
        }
        try (Socket sock = new Socket(args[0], 7777)) {
            keyboard = new KeyboardThread(sock);
            listening = new ListeningThread(sock);
            Thread keyboardThread = new Thread(keyboard);
            Thread listeningThread = new Thread(listening);
            keyboardThread.start();
            listeningThread.start();

            keyboardThread.join();
            listeningThread.join();
        }
    }
}
