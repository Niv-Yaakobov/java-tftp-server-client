package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Procces {

    private int connectionId;
    private TftpConnections<byte[]> connections;
    private byte[] response = new byte[1 << 10]; // start with 1k
    private byte[] msg;
    private byte[] textToSend;
    private boolean isLoggedIn = false;
    private byte[] textToSave;
    public byte[] bcastResponse = null;
    private String fileName;
    public boolean hasResponse = true;

    public Procces(int connectionId, TftpConnections<byte[]> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
    }

    public byte[] handleMessage(byte[] message) {
        hasResponse = true;
        // System.out.println("message: ");
        // printArrayAsInteger(message);
        // printArrayAsString(message);
        msg = Arrays.copyOf(message, message.length);
        short result = convertBytesToShort(msg[0], msg[1]);
        if (!isLoggedIn & result != 7 & result!=10){
            //check if the request is RRQ or DELRQ
            if(result == 1 || result == 8){
                String fileName = byteToString(Arrays.copyOfRange(msg, 2, msg.length - 1));
                Integer readersCounter = connections.filesLibrary.get(fileName);
                if(readersCounter == null){
                    buildErrorMsg((byte)1);
                }
                else
                    buildErrorMsg((byte) 6);
            }
            else if(result == 2){
                String fileName = byteToString(Arrays.copyOfRange(msg, 2, msg.length - 1));
                Integer readersCounter = connections.filesLibrary.get(fileName);
                if(readersCounter != null){
                    buildErrorMsg((byte)5);
                }
                else
                    buildErrorMsg((byte) 6);
            }
            else
                buildErrorMsg((byte) 6);
        }
        else {
            switch (result) {
                case 1:
                    RRQ();
                    break;
                case 2:
                    WRQ();
                    break;
                case 3:
                    DATA();
                    break;
                case 4:
                    ACK();
                    break;
                case 5:
                    ERROR();
                    break;
                case 6:
                    DIRQ();
                    break;
                case 7:
                    LOGRQ();
                    break;
                case 8:
                    DELRQ();
                    break;
                case 10:
                    DISC();
                    break;
                default:
                    System.out.println("Default case");
            }
        }
        return response;
    }

    private void RRQ() {
        String fileName = byteToString(Arrays.copyOfRange(msg, 2, msg.length - 1));
        Integer readersCounter = connections.filesLibrary.get(fileName);
        if (readersCounter != null && readersCounter != -1) {
            try {
                connections.filesLibrary.put(fileName, ++readersCounter);
                textToSend = readFileToByteArray("./Flies/" + fileName);
                connections.filesLibrary.put(fileName, --readersCounter);

            } catch (IOException e) {
            }
            buildDataPacket((byte) 0, (byte) 1);
        } else {
            buildErrorMsg((byte) 1);
        }
    }

    private void WRQ() {
        String fileName = byteToString(Arrays.copyOfRange(msg, 2, msg.length - 1));
        if (isFileInFolder("./Flies", fileName)) {
            buildErrorMsg((byte) 5);
        } else {
            buildAckMsg((byte) 0, (byte) 0);
            this.fileName = fileName;
        }
    }

    private void DATA() {
        short pacSize = convertBytesToShort(msg[2], msg[3]);
        byte[] data = Arrays.copyOfRange(msg, 6, msg.length);
        if (pacSize < 512) {
            // end of download
            appendArray(data);
            try{
                insertBytesIntoFile(fileName,textToSave);
            }catch (IOException e) {
            }
            buildAckMsg(msg[4], msg[5]);
            buildBcastRes((byte)1);
        }
        else{
            appendArray(data);
            buildAckMsg(msg[4], msg[5]);
        }

    }

    private void ACK() {
        byte byte1 = msg[2];
        byte byte2 = msg[3];
        if ((byte1 == 0 && byte2 == 0) || textToSend == null || textToSend.length == 0) {// no response is needed
            response = null;
            hasResponse = false;
        }
        else{
            short blockNum = convertBytesToShort(byte1, byte2);
            blockNum++;
            byte[] blockNumBytes = convertShortToBytes(blockNum);
            buildDataPacket(blockNumBytes[0], blockNumBytes[1]);
        }
    }

    private void DIRQ() {
        lockAllFiles();
        getFilesNamesByteArray();
        buildDataPacket((byte) 0, (byte) 1);
        unlockAllFiles();
    }
    private void printArrayAsString(byte[] byteArray){
        String result = new String(byteArray, StandardCharsets.UTF_8); // specify the character set
        System.out.println(result);
    }
    
    private void printArrayAsInteger(byte[] byteArray){
        System.out.print("[");
        for (byte b : byteArray) {
            System.out.print(b + ", ");
        }
        System.out.print("]");

        System.out.println(); // to move to the next line
    }

    private void LOGRQ() {
        if(!isLoggedIn){
            String username = byteToString(Arrays.copyOfRange(msg, 2, msg.length - 1));
            boolean exist = false;
            for (String existingUsername : connections.loggedUsernames.values()) {
                if (existingUsername.equals(username)) {
                    buildErrorMsg((byte) 7);
                    exist = true;
                }
            }
            if (!exist) {
                connections.loggedUsernames.put(connectionId, username);
                buildAckMsg((byte) 0, (byte) 0);
                isLoggedIn = true;
            }
        }else{
            buildErrorMsg((byte)7);
        }
    }

    private void DELRQ() {
        byte[] filenameBytes = Arrays.copyOfRange(msg, 2, msg.length-1);
        String filename = byteToString(filenameBytes);
        if(connections.filesLibrary.keySet().contains(filename)){
            Integer readersCounter = connections.filesLibrary.get(filename);
            while(readersCounter > 0 ){
            }
            connections.filesLibrary.replace(filename, -1);
            deleteFile(filename);
            connections.filesLibrary.remove(filename);
            buildAckMsg((byte)0, (byte)0);
            this.fileName = filename;
            buildBcastRes((byte)0);
        }
        else{
            buildErrorMsg((byte)1);
        }
    }

    private void DISC() {
        if(!isLoggedIn){
            buildErrorMsg((byte) 6);
            //buildAckMsg((byte)0, (byte)0);
        }
        else{
            isLoggedIn = false;
            connections.loggedUsernames.remove(connectionId);
            buildAckMsg((byte)0, (byte)0);
        }
    }

    private boolean deleteFile( String fileName) {
        Path filePath = Paths.get("./Flies", fileName);

        try {
            // Delete the file
            Files.delete(filePath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    private void lockAllFiles(){
        for(String name: connections.filesLibrary.keySet()){
            Integer readersCounter = connections.filesLibrary.get(name) + 1;
            connections.filesLibrary.put(name,readersCounter );
        }
    }
    private void unlockAllFiles(){
        for(String name: connections.filesLibrary.keySet()){
            Integer readersCounter = connections.filesLibrary.get(name) - 1;
            connections.filesLibrary.put(name,readersCounter );
        }
    }

    private short convertBytesToShort(byte byte1, byte byte2) {
        return (short) (((short) (byte1 & 0xFF) << 8) | ((short) (byte2 & 0xFF)));
    }

    private byte[] convertShortToBytes(short num) {
        return new byte[] { (byte) ((num >> 8) & 0xFF),(byte) (num & 0xFF)};
    }

    public String byteToString(byte[] byteArray) {
        return new String(byteArray, Charset.forName("UTF-8"));
    }

    public byte[] stringToByte(String msg) {
        return msg.getBytes();
    }

    private void buildAckMsg(byte byte1, byte byte2) {
        byte[] ackMsg = { 0, 4, byte1, byte2 };
        response = Arrays.copyOf(ackMsg, ackMsg.length);
    }

    private void buildBcastRes(byte status){
        byte[] file = stringToByte(fileName);
        byte[] res = new byte[4 + file.length];
        res[0]=0; res[1]=9; res[2]=status; res[res.length-1]='\0';
        for(int i = 3; i < res.length-1; i++){
            res[i] = file[i-3];
        }
        bcastResponse = Arrays.copyOf(res, res.length);
    }

    private void buildErrorMsg(byte errorCode) {
        String errorMsg = "";
        switch (errorCode) {
            case 0:
                errorMsg = "Not defined, see error message (if any).";
                break;
            case 1:
                errorMsg = "File not found - RRQ DELRQ of non-existing file.";
                break;
            case 2:
                errorMsg = "Access violation - File cannot be written, read or deleted.";
                break;
            case 3:
                errorMsg = "Disk full or allocation exceeded - No room in disk";
                break;
            case 4:
                errorMsg = "Illegal TFTP operation - Unknown Opcode.";
                break;
            case 5:
                errorMsg = "File already exists - File name exists on WRQ.";
                break;
            case 6:
                errorMsg = "User not logged in - Any opcode received before Login completes.";
                break;
            case 7:
                errorMsg = "User already logged in - Login username already connected";
                break;
        }
        byte[] s = stringToByte(errorMsg);
        byte[] res = new byte[s.length + 5];
        res[0] = 0;
        res[1] = 5;
        res[2] = 0;
        res[3] = errorCode;
        res[res.length - 1] = '\0';
        for (int i = 4; i < res.length - 1; i++)
            res[i] = s[i - 4];

        response = Arrays.copyOf(res, res.length);
    }

    private void buildDataPacket(byte byte1, byte byte2) {
        byte[] data;
        if (textToSend.length >= 512) {
            data = Arrays.copyOfRange(textToSend, 0, 512);
            textToSend = Arrays.copyOfRange(textToSend, 512, textToSend.length);
        } else {
            data = Arrays.copyOfRange(textToSend, 0, textToSend.length);
            textToSend = null;
        }
        byte[] res = new byte[6 + data.length];
        byte[] pacSize = convertShortToBytes((short) data.length);
        res[0] = 0;
        res[1] = 3;
        res[2] = pacSize[0];
        res[3] = pacSize[1];
        res[4] = byte1;
        res[5] = byte2;
        for (int i = 6; i < data.length + 6; i++) {
            res[i] = data[i - 6];
        }
        response = Arrays.copyOf(res, res.length);
    }

    private byte[] readFileToByteArray(String filePath) throws IOException {
        File file = new File(filePath);
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

    private void getFilesNamesByteArray() {
        File folder = new File("./Flies");
        File[] files = folder.listFiles();

        StringBuilder namesBuilder = new StringBuilder();

        for (File file : files) {
            if (file.isFile()) {
                namesBuilder.append(file.getName());
                namesBuilder.append('\0');
            }
        }

        namesBuilder.append('\0'); // Add final null byte at the end
        textToSend = Arrays.copyOf(namesBuilder.toString().getBytes(StandardCharsets.UTF_8), namesBuilder.toString().getBytes(StandardCharsets.UTF_8).length);

    }

    private boolean isFileInFolder(String folderPath, String filename) {
        File folder = new File(folderPath);

        if (!folder.isDirectory()) {
            System.err.println("Error: Not a valid folder path.");
            return false;
        }

        File fileToCheck = new File(folder, filename);
        return fileToCheck.exists() && fileToCheck.isFile();
    }

    private void insertBytesIntoFile(String filename, byte[] contentBytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream("./Flies/" + filename, false)) {
            fos.write(contentBytes);
            connections.filesLibrary.put(filename, 0);
        }
    }

    private void appendArray(byte[] array) {
        if (textToSave != null) {
            byte[] tmp = new byte[textToSave.length + array.length];
            for (int i = 0; i < textToSave.length; i++)
                tmp[i] = textToSave[i];
            for (int i = textToSave.length; i < tmp.length; i++)
                tmp[i] = array[i - textToSave.length];
            textToSave = Arrays.copyOf(tmp, tmp.length);
        } else
            textToSave = Arrays.copyOf(array, array.length);

    }

    private void ERROR() {

    }
}