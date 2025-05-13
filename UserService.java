import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    void addUser(String name, String email);
    boolean deleteUser(int id);
}
