public class User {
    private static int counter = 1;
    private final Integer id;
    private final String name;
    private final String email;

    public User(String name, String email) {
        this.id = counter++;
        this.name = name;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
