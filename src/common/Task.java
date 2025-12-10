package common;

public class Task {
    private int id;
    private int userId;
    private String text;
    private boolean done;
    private String priority;
    private String category;

    public Task(int id, int userId, String text, boolean done, String priority, String category){
        this.id = id; this.userId = userId; this.text = text; this.done = done;
        this.priority = priority; this.category = category;
    }

    // getters / setters
    public int getId(){return id;}
    public int getUserId(){return userId;}
    public String getText(){return text;}
    public boolean isDone(){return done;}
    public String getPriority(){return priority;}
    public String getCategory(){return category;}
}
