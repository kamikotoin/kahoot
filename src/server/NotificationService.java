package server;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationService {
    private static final NotificationService inst = new NotificationService();
    private final List<PrintWriter> clients = new CopyOnWriteArrayList<>();

    private NotificationService(){}

    public static NotificationService getInstance(){ return inst; }

    public void register(PrintWriter out) {
        clients.add(out);
    }

    public void unregister(PrintWriter out) {
        clients.remove(out);
    }

    public void broadcast(String msg) {
        for (PrintWriter pw : clients) {
            try {
                pw.println(msg);
                pw.flush();
            } catch (Exception ignored) {}
        }
    }
}
