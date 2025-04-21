package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class KeyboardProtocol extends clientTftpProtocol {

    @Override
    public void RRQ() {
        byte[] _msg = Arrays.copyOfRange(msg, 2,   msg.length);
        String fileName = convertByteToString(_msg);
        if (isFileAlreadyExists(fileName)){
            msg = null;
            System.out.println("file already exists");
        }
        else{
            TftpClient.commandCode = 1;
            TftpClient.command = "RRQ " + convertByteToString(_msg);
            TftpClient.isDone = false;
            createEmptyFile(fileName);
            buildResponse(true);
        }
    }

    @Override
    public void WRQ() {
        byte[] _msg = Arrays.copyOfRange(msg, 2,   msg.length);
        String fileName = convertByteToString(_msg);
        if (!isFileAlreadyExists(fileName)){
            msg = null;
            System.out.println("file does not exist");
        }
        else{
            TftpClient.commandCode = 2;
            TftpClient.command = "WRQ " + convertByteToString(_msg);
            TftpClient.isDone = false;
            buildResponse(true);
        }
    }

    @Override
    public void DATA() {
        msg = null;
        System.out.println("DATA is invalid command");
    }

    @Override
    public void ACK() {
        msg = null;
        System.out.println("DATA is invalid command");
    }

    @Override
    public void ERROR() {
        msg = null;
        System.out.println("ERROR is invalid command");
    }

    @Override
    public void DIRQ() {
        TftpClient.commandCode = 6;
        TftpClient.command = "DIRQ";
        TftpClient.isDone = false;
        buildResponse(false);
    }

    @Override
    public void LOGRQ() {
        byte[] _msg = Arrays.copyOfRange(msg, 2,   msg.length);
        TftpClient.commandCode = 7;
        TftpClient.command = "LOGRQ " + convertByteToString(_msg);
        TftpClient.isDone = false;
        buildResponse(true);
    }

    @Override
    public void DELRQ() {
        byte[] _msg = Arrays.copyOfRange(msg, 2,   msg.length);
        TftpClient.commandCode = 8;
        TftpClient.command = "DELRQ " + convertByteToString(_msg);
        TftpClient.isDone = false;
        buildResponse(true);
    }

    @Override
    public void BCAST() {
        msg = null;
        System.out.println("BCAST is invalid command");
    }

    @Override
    public void DISC() {
        TftpClient.commandCode = 10;
        TftpClient.command = "DISC";
        TftpClient.isDone = false;
        buildResponse(false);
    }

    public boolean isFileAlreadyExists(String _fileName){
        File directory = new File(TftpClient.clientFiles_path);
        File file = new File(directory, _fileName);
        return file.exists();
    }

    public void createEmptyFile(String _fileName){
        File directory = new File(TftpClient.clientFiles_path);
        File file = new File(directory, _fileName);
        try{
            file.createNewFile();
        }catch (IOException e) {}
    }

    public void buildResponse(boolean addZero){
        if (addZero){
            response = new byte[msg.length + 1];
            for (int i = 0; i < msg.length; i++){
                response[i] = msg[i];
            }
            response[msg.length] = '\0';
        }
        else 
            response = Arrays.copyOf(msg, msg.length);
    }

}
