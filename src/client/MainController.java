package client;

import common.Task;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.*;

public class MainController {
    @FXML public TableView<TaskRow> table;
    @FXML public TableColumn<TaskRow, String> colText;
    @FXML public TableColumn<TaskRow, String> colPriority;
    @FXML public TableColumn<TaskRow, String> colCategory;
    @FXML public TableColumn<TaskRow, Boolean> colDone;

    @FXML public TextField txtNew;
    @FXML public ChoiceBox<String> prio;
    @FXML public TextField cat;
    @FXML public ChoiceBox<String> priorityFilter;
    @FXML public Label status;

    private ClientConnection conn;
    private int userId;
    private final ObservableList<TaskRow> data = FXCollections.observableArrayList();

    public void init(ClientConnection connection, int userId) {
        this.conn = connection;
        this.userId = userId;

        // Setup table columns (simple mapping)
        colText.setCellValueFactory(c -> c.getValue().textProperty());
        colPriority.setCellValueFactory(c -> c.getValue().priorityProperty());
        colCategory.setCellValueFactory(c -> c.getValue().categoryProperty());
        colDone.setCellValueFactory(c -> c.getValue().doneProperty());

        table.setItems(data);

        // Start background listener for server events
        conn.startListener(this::handleServerMessage);

        // start auto-sync
        startAutoSync();
        // initial load
        syncTasks();
    }

    private void handleServerMessage(String msg) {
        // server may push: EVENT|NEW_TASK;user=2;id=7;...
        System.out.println("SRV: " + msg);
        if (msg.startsWith("EVENT|")) {
            // simple strategy: on any event, refresh tasks
            Platform.runLater(this::syncTasks);
        }
    }

    public void onAdd() {
        String text = txtNew.getText();
        if (text == null || text.isBlank()) { status.setText("Enter text"); return; }
        String p = prio.getValue() == null ? "MEDIUM" : prio.getValue();
        String c = cat.getText() == null ? "GENERAL" : cat.getText();
        try {
            String req = "ADD_TASK|text=" + text + ";priority=" + p + ";category=" + c;
            String reply = conn.sendRequest(req);
            status.setText(reply);
            syncTasks();
            txtNew.clear();
        } catch (IOException e) {
            status.setText("Failed to add");
        }
    }

    public void onDelete() {
        TaskRow sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { status.setText("Select"); return; }
        try {
            String req = "DELETE_TASK|id=" + sel.getId();
            String reply = conn.sendRequest(req);
            status.setText(reply);
            syncTasks();
        } catch (IOException e) { status.setText("Delete failed"); }
    }

    public void onMarkDone() {
        TaskRow sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { status.setText("Select"); return; }
        try {
            String req = "MARK_DONE|id=" + sel.getId() + ";done=1";
            String reply = conn.sendRequest(req);
            status.setText(reply);
            syncTasks();
        } catch (IOException e) { status.setText("Update failed"); }
    }

    public void onSync() { syncTasks(); }

    private void syncTasks() {
        try {
            String req = "GET_TASKS|";
            String reply = conn.sendRequest(req);
            if (reply.startsWith("OK|tasks=")) {
                String payload = reply.substring("OK|tasks=".length());
                List<TaskRow> rows = parseTasks(payload);
                Platform.runLater(() -> {
                    data.setAll(rows);
                });
            } else {
                Platform.runLater(() -> status.setText(reply));
            }
        } catch (IOException e) {
            Platform.runLater(() -> status.setText("Sync error"));
        }
    }

    private List<TaskRow> parseTasks(String payload) {
        List<TaskRow> rows = new ArrayList<>();
        if (payload == null || payload.isEmpty()) return rows;
        String[] items = payload.split(",");
        for (String it : items) {
            Map<String,String> map = new HashMap<>();
            String[] pairs = it.split("&");
            for (String p : pairs) {
                int idx = p.indexOf('=');
                if (idx>0) map.put(p.substring(0,idx), p.substring(idx+1));
            }
            int id = Integer.parseInt(map.get("id"));
            String text = map.get("text");
            boolean done = "1".equals(map.getOrDefault("done","0"));
            String pr = map.get("priority");
            String cat = map.get("category");
            rows.add(new TaskRow(id, text, pr, cat, done));
        }
        return rows;
    }

    private void startAutoSync() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(10000);
                    syncTasks();
                }
            } catch (InterruptedException ignored) {}
        });
        t.setDaemon(true);
        t.start();
    }
}
