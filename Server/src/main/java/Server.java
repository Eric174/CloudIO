import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

    private static final String path = "./Server/src/main/resources/";
    private static final int buferSize = 8192;

    private Socket socket;
    private int idClient;

    public Server(Socket socket, int id) {
        this.socket = socket;
        this.idClient = id;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8189);
        int userCount = 0; // счетчик поключений
        while (true) {
            Socket socket = server.accept();
            new Server(socket, userCount++).start();
        }
    }

    public void run() {
        try {
            File clientDir = new File(path + "Client" + idClient + "/");
            clientDir.mkdir();
            DataInputStream is = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            while (true) {
                // command
                String command = is.readUTF();
                if (command.equals("./getFilesList")) {
                    String [] files = clientDir.list();
                    if (files != null) {
                        dos.writeInt(files.length);
                        for (String file : files) {
                            dos.writeUTF(file);
                        }
                    } else {
                        dos.writeInt(0);
                    }
                } else if (command.equals("./upload")) {
                    String fileName = is.readUTF();
                    System.out.println("Save file: " + fileName);
                    File file = new File(clientDir + "/" +  fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    } else {
                        System.out.println("File " + fileName + " already exists");
                    }
                    FileOutputStream os = new FileOutputStream(file);
                    // file length
                    long fileLength = is.readLong();
                    long longBufer = fileLength;
                    System.out.println("Wait: " + fileLength + " bytes");
                    // file bytes
                    byte[] buffer = new byte[buferSize];
                    for (int i = 0; i < (fileLength  / buferSize) + 1; i++) {
                        int cnt = is.read(buffer);
                        os.write(buffer, 0, cnt);
                        longBufer -= cnt;
                    }
                    System.out.println(longBufer);
                    os.close();
                    System.out.println("File successfully uploaded!");
                } else if (command.equals("./load")) {
                    String fileName = is.readUTF();
                    File file = new File(clientDir + "/" + fileName);
                    dos.writeLong(file.length());
                    FileInputStream fis = new FileInputStream(file);
                    int tmp;
                    byte [] buffer = new byte[buferSize];
                    while ((tmp = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, tmp);
                    }
                    fis.close();
                } else if (command.equals("./quit")) {
                    socket.close();
                    System.out.println("Client with id: " + idClient + " exit");
                    break;
                } else {
                    System.out.println("Unknown command: " + command);
                }
            }
            is.close();
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}