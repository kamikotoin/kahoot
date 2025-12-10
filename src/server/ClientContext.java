package server;

import common.User;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientContext {
    private User user;
    private final PrintWriter out;
    private final Socket socket;

    public ClientContext(PrintWriter out, Socket socket) {
        this.out = out; this.socket = socket;
    }
    public void setUser(User u){ this.user = u; }
    public User getUser(){ return user; }
    public boolean checkLogged(){ return user != null; }
    public PrintWriter getOut(){ return out; }
    public Socket getSocket(){ return socket; }
}
