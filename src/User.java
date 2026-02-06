public abstract class User {
    protected String id;
    protected String password;
    protected String name;

    public User(String id, String password, String name) {
        this.id = id;
        this.password = password;
        this.name = name;
    }

    public String getId() { return id; }
    public String getPassword() { return password; }
    public String getName() { return name; }
}