import java.util.ArrayList;
import java.util.List;

public class UserServiceImpl implements UserService {
    private final List<User> users = new ArrayList<>();

    @Override
    public List<User> getAllUsers() {
        return users;
    }

    @Override
    public void addUser(String name, String email) {
        users.add(new User(name, email));
    }

    @Override
    public boolean deleteUser(int id) {
        return users.removeIf(u -> u.getId() == id);
    }
}
