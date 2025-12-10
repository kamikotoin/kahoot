package server;

import server.dao.UserDAO;
import server.dao.TaskDAO;
import common.User;
import common.Task;
import common.Utils;

import java.util.*;
import java.sql.SQLException;

public class CommandParser {

    // returns string reply: "OK|..." or "ERR|..."
    public static String handle(String raw, ClientContext ctx) {
        try {
            if (raw == null || raw.isEmpty()) return "ERR|Empty";
            int pipe = raw.indexOf('|');
            String cmd = (pipe>=0) ? raw.substring(0,pipe) : raw;
            String params = (pipe>=0)? raw.substring(pipe+1):"";
            Map<String,String> map = Utils.parseParams(params);

            switch (cmd) {
                case "LOGIN" -> {
                    String user = map.get("username");
                    String pass = map.get("password");
                    User u = UserDAO.login(user, pass);
                    if (u == null) return "ERR|Invalid credentials";
                    ctx.setUser(u);
                    return "OK|userid="+u.getId()+";username="+u.getUsername()+";role="+u.getRole();
                }
                case "REGISTER" -> {
                    String user = map.get("username");
                    String pass = map.get("password");
                    User u = UserDAO.register(user, pass);
                    if (u == null) return "ERR|Register failed";
                    return "OK|userid="+u.getId();
                }
                case "GET_TASKS" -> {
                    if (!ctx.checkLogged()) return "ERR|Not logged in";
                    List<Task> tasks = TaskDAO.getTasksByUser(ctx.getUser().getId());
                    StringBuilder sb = new StringBuilder();
                    // build: OK|tasks=id:1,text:Buy;id:2,...
                    sb.append("OK|tasks=");
                    boolean first=true;
                    for (Task t : tasks) {
                        if (!first) sb.append(",");
                        sb.append("id=").append(t.getId())
                          .append("&text=").append(escape(t.getText()))
                          .append("&done=").append(t.isDone() ? 1:0)
                          .append("&priority=").append(t.getPriority())
                          .append("&category=").append(t.getCategory());
                        first=false;
                    }
                    return sb.toString();
                }
                case "ADD_TASK" -> {
                    if (!ctx.checkLogged()) return "ERR|Not logged in";
                    String text = map.getOrDefault("text","");
                    String pr = map.getOrDefault("priority","MEDIUM");
                    String cat = map.getOrDefault("category","GENERAL");
                    int id = TaskDAO.addTask(ctx.getUser().getId(), text, pr, cat);
                    if (id>0) {
                        // notify all clients that a task added (clients will filter by user)
                        NotificationService.getInstance().broadcast("EVENT|NEW_TASK;user="+ctx.getUser().getId()+";id="+id+";text="+escape(text));
                        return "OK|task_id="+id;
                    } else return "ERR|Insert failed";
                }
                case "DELETE_TASK" -> {
                    if (!ctx.checkLogged()) return "ERR|Not logged in";
                    int id = Integer.parseInt(map.get("id"));
                    boolean ok = TaskDAO.deleteTask(id);
                    if (ok) {
                        NotificationService.getInstance().broadcast("EVENT|DELETE_TASK;id="+id);
                        return "OK|deleted="+id;
                    } else return "ERR|Delete failed";
                }
                case "MARK_DONE" -> {
                    if (!ctx.checkLogged()) return "ERR|Not logged in";
                    int id = Integer.parseInt(map.get("id"));
                    boolean done = "1".equals(map.getOrDefault("done","1"));
                    boolean ok = TaskDAO.markDone(id, done);
                    if (ok) {
                        NotificationService.getInstance().broadcast("EVENT|UPDATE_TASK;id="+id+";done="+(done?1:0));
                        return "OK|updated="+id;
                    } else return "ERR|Update failed";
                }
                default -> { return "ERR|Unknown command"; }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                return "ERR|Username already exists";
            }
            e.printStackTrace();
            return "ERR|Database error";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERR|"+e.getMessage();
        }
    }

    private static String escape(String s) {
        if (s==null) return "";
        return s.replace("|"," ").replace(";"," ").replace(","," ");
    }
}
