import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> listView;
    public ListView<String> listView2;
    private final String path = "client/src/main/resources/client_dir/";
    private final int buferSize = 8192;
    private DataInputStream is;
    private DataOutputStream os;

    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            refreshList();
            serverList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upload(ActionEvent actionEvent) {
        String fileName = listView.getSelectionModel().getSelectedItem();
        if (fileName == null) return;
        System.out.println(fileName);
        try {
            File file = new File(path + fileName);
            if (!file.exists()) return;
            os.writeUTF("./upload");
            os.writeUTF(fileName);
            os.writeLong(file.length());
            FileInputStream fis = new FileInputStream(file);
            int tmp;
            byte [] buffer = new byte[buferSize];
            long longSize = 0;
            while ((tmp = fis.read(buffer)) != -1) {
                os.write(buffer, 0, tmp);
                longSize += tmp;
            }
            System.out.println("Bytes: " + longSize);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshList() {
        File file = new File(path);
        String[] files = file.list();
        listView.getItems().clear();
        if (files != null) {
            for (String name : files) {
                listView.getItems().add(name);
            }
        }
    }

    public void refreshList(ActionEvent actionEvent) {
        refreshList();
    }

    public void serverList(ActionEvent actionEvent) {
        serverList();
    }

    private void serverList() {
        try {
            listView2.getItems().clear();
            listView2.getItems().addAll(getServerFiles());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getServerFiles() throws IOException {
        List<String> files = new ArrayList<String>();
        os.writeUTF("./getFilesList");
        int listSize = is.readInt();
        for (int i = 0; i < listSize; i++) {
            files.add(is.readUTF());
        }
        return files;
    }

    public void load(ActionEvent actionEvent) throws IOException {
        String fileName = listView2.getSelectionModel().getSelectedItem();
        if (fileName == null) return;
        File file = new File(path + fileName);
        if (!file.exists()) {
            file.createNewFile();
        } else {
            System.out.println("File " + fileName + " already exists");
        }
        os.writeUTF("./load");
        os.writeUTF(fileName);
        long fileLength  = is.readLong();
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[buferSize];
        for (int i = 0; i < (fileLength + buferSize-1) / buferSize; i++) {
            int cnt = is.read(buffer);
            fos.write(buffer, 0, cnt);
        }
        fos.close();
        System.out.println("File successfully loaded!");
    }

    public void exit(ActionEvent actionEvent) throws IOException {
        os.writeUTF("./quit");
        os.close();
        is.close();
        System.exit(0);
    }
}