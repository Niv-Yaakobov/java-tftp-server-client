package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ListeningProtocol extends clientTftpProtocol {

    public byte[] textToSend;
    public byte[] textToSave;

    @Override
    public void RRQ() {
        System.out.println("can't get this packet");
    }

    @Override
    public void WRQ() {
        System.out.println("can't get this packet");
    }

    @Override
    public void DATA() {
        short packetSize = convertBytesToShort(msg[2], msg[3]);
        short blockNumber = convertBytesToShort(msg[4], msg[5]);
        msg = Arrays.copyOfRange(msg, 6, msg.length);
        addNewData();
        sendAck(blockNumber);
        if (packetSize < 512){
            if (TftpClient.commandCode == 6){
                printFilesList();
            }
            else{ //RRQ - download from server
                String temp = TftpClient.command;
                String fileName = temp.substring(4);
                writeToEmptyFile(fileName);
                System.out.println(TftpClient.command + " complete");
            }
            textToSave = null;
            TftpClient.finishCurrentCommand();
        }
    }

    @Override
    public void ACK() {
        short blockNumber = convertBytesToShort(msg[2], msg[3]);
        if (blockNumber == 0){
            if (TftpClient.commandCode == 2){ // WRQ -> upload file
                String fileName = TftpClient.command;
                fileName = fileName.substring(4);
                try{
                    textToSend = writeFileToArray(fileName);
                }catch (IOException e) {
                }
                response = createPackageToSend(blockNumber);
            }
            else if (TftpClient.commandCode == 7){ // LOGRQ -> login
                TftpClient.isLoggedIn = true;
                TftpClient.finishCurrentCommand();
            }
            else if (TftpClient.commandCode == 8){ // DELRQ -> delete the file I asked 
                TftpClient.finishCurrentCommand();
            }
            else if(TftpClient.commandCode == 10){ // DISC -> disconnect
                System.out.print("");
                TftpClient.isLoggedIn = false;
                TftpClient.finishCurrentCommand();
                TftpClient.shouldTerminate = true;
            }
        }
        else{ // WRQ -> send 'blocknumber+1' packet
            if (textToSend != null && textToSend.length > 0){
                response = createPackageToSend(blockNumber);
            }
            else{
                textToSend = null;
                TftpClient.finishCurrentCommand();
            }
        }
    }

    @Override
    public void ERROR() {
        short errorNum = convertBytesToShort(msg[2], msg[3]);
        byte[] _errorMsg = Arrays.copyOfRange(msg, 4, msg.length-1);
        String errorMsg = convertByteToString(_errorMsg);
        short commandCode = TftpClient.commandCode;

        if (commandCode == 1){ // Try to download unexisted file
            String fileName = TftpClient.command;
            fileName = fileName.substring(4);
            deleteFileFromFolder(fileName);
            textToSave = null;
        }
        else if (commandCode == 2 || commandCode == 6){
            textToSend = null;
        }
        else if(commandCode == 10){
            TftpClient.shouldTerminate = true;
        }
        
        
        TftpClient.finishCurrentCommand();
        System.out.println("ERROR " + errorNum + " " + errorMsg);
    }

    @Override
    public void DIRQ() {
        System.out.println("can't get this packet");
    }

    @Override
    public void LOGRQ() {
        System.out.println("can't get this packet");
    }

    @Override
    public void DELRQ() {
        System.out.println("can't get this packet");
    }

    @Override
    public void BCAST() {
        String del_OR_add = (msg[2] == (byte)0) ? "del " : "add ";
        byte[] _fileName = Arrays.copyOfRange(msg, 3, msg.length-1);
        String fileName = convertByteToString(_fileName);
        System.out.println("BCAST " + del_OR_add + fileName);
    }

    @Override
    public void DISC() {
        System.out.println("can't get this packet");
    }

    private void addNewData(){
        if (textToSave != null && textToSave.length != 0){
            byte[] temp = new byte[textToSave.length+msg.length];
            for (int i = 0; i < textToSave.length; i++){
                temp[i] = textToSave[i];
            }
            for (int i = textToSave.length; i < temp.length; i++){
                temp[i] = msg[i - textToSave.length];
            }
            textToSave = Arrays.copyOf(temp, temp.length);
        }
        else{
            textToSave = Arrays.copyOf(msg, msg.length);
        }
    }

    private byte[] createPackageToSend(short blockNumber){
        if (textToSend == null || textToSend.length == 0){
            return null;
        }
        short packetSize = (short) ((textToSend.length < 512) ? textToSend.length : 512);
        byte[] _response = new byte[packetSize+6];
        _response[0] = (byte)0; _response[1] = (byte)3;
        byte[] _packetSize = convertShortToBytes(packetSize);
        _response[2] = _packetSize[0]; _response[3] = _packetSize[1];
        byte[] _blockNum = convertShortToBytes(++blockNumber);
        _response[4] = _blockNum[0]; _response[5] = _blockNum[1];

        for (int i = 6; i < _response.length; i++){
            _response[i] = textToSend[i - 6];
        }
        textToSend = Arrays.copyOfRange(textToSend, packetSize, textToSend.length);
        return _response;
    }

    private void sendAck(short _blockNumber){
        byte[] block = convertShortToBytes(_blockNumber);
        byte[] res = {0,4,block[0],block[1]};
        response = Arrays.copyOf(res, res.length);
    }

    private void printFilesList(){
        int start = 0;
        for (int i = 0; i < textToSave.length; i++) {
            if (textToSave[i] == '\0') {
                String fileName = new String(textToSave, start, i - start);
                System.out.println(fileName);
                start = i + 1; 
            }
        }
        if (start < textToSave.length) {
            String fileName = new String(textToSave, start, textToSave.length - start);
            System.out.println(fileName);
        }
        textToSave = null;
    }

    private void writeToEmptyFile(String _fileName){
        try(FileOutputStream fos = new FileOutputStream(TftpClient.clientFiles_path + _fileName, false)){
            fos.write(textToSave);
            textToSave = null;
        }catch (IOException e) {}
    }

    private void deleteFileFromFolder(String _fileName){
        File directory = new File(TftpClient.clientFiles_path);
        File fileToDelete = new File(directory, _fileName);
        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }

    private byte[] writeFileToArray(String _fileName)throws IOException{
        File file = new File(TftpClient.clientFiles_path + _fileName);
        long fileSize = file.length();

        if (fileSize > Integer.MAX_VALUE) {
            throw new IOException("File is too large to read into a byte array.");
        }
        byte[] fileBytes = new byte[(int) fileSize];

        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead = fis.read(fileBytes);
            if (bytesRead < fileSize) {
                throw new IOException("Not all bytes could be read from the file.");
            }
        }
        return fileBytes;
    }
}
