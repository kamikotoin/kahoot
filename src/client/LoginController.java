package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML public TextField username;
    @FXML public PasswordField password;
    @FXML public Label status;

    private ClientConnection conn;

    @FXML
    public void initialize() {
        // connect on background thread to avoid UI blocking
        new Thread(() -> {
            try {
                conn = new ClientConnection();
                conn.connect("127.0.0.1", 5555);
                // install a simple event logger (optional)
                conn.startListener(msg -> System.out.println("EVENT: " + msg));
                Platform.runLater(() -> status.setText("Connected"));
            } catch (Exception e) {
                Platform.runLater(() -> status.setText("Server not available"));
            }
        }).start();
    }

    @FXML
    public void onLogin() {
        String user = username.getText() == null ? "" : username.getText().trim();
        String pass = password.getText() == null ? "" : password.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            status.setText("Enter username and password");
            return;
        }

        status.setText("Logging in...");

        new Thread(() -> {
            try {
                String req = "LOGIN|username=" + user + ";password=" + pass;
                String reply = conn.sendRequest(req);

                if (reply == null) {
                    Platform.runLater(() -> status.setText("No reply from server"));
                    return;
                }

                if (reply.startsWith("OK|")) {
                    int uid = parseUserId(reply);
                    Platform.runLater(() -> loadMainWindow(uid));
                } else {
                    Platform.runLater(() -> status.setText(reply));
                }
            } catch (Exception e) {
                Platform.runLater(() -> status.setText("Login failed: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void onRegister() {
        String user = username.getText() == null ? "" : username.getText().trim();
        String pass = password.getText() == null ? "" : password.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            status.setText("Enter username and password");
            return;
        }

        status.setText("Registering...");

        new Thread(() -> {
            try {
                String req = "REGISTER|username=" + user + ";password=" + pass;
                String reply = conn.sendRequest(req);
                if (reply == null) {
                    Platform.runLater(() -> status.setText("No reply from server"));
                } else {
                    Platform.runLater(() -> status.setText(reply));
                }
            } catch (Exception e) {
                Platform.runLater(() -> status.setText("Register failed: " + e.getMessage()));
            }
        }).start();
    }

    private int parseUserId(String okReply) {
        String[] parts = okReply.split("\\|");
        if (parts.length < 2) return -1;
        String[] kv = parts[1].split(";");
        for (String s : kv) {
            if (s.startsWith("userid=")) {
                try {
                    return Integer.parseInt(s.substring("userid=".length()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return -1;
    }

    private void loadMainWindow(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/main.fxml"));
            Scene scene = new Scene(loader.load());
            MainController mc = loader.getController();
            mc.init(conn, userId);
            ClientApp.switchScene(scene);
        } catch (Exception e) {
            status.setText("Failed to load main window");
            e.printStackTrace();
        }
    }
}
