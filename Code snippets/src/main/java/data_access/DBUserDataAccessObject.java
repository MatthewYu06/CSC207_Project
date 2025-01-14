package main.java.data_access;

import main.java.entity.DefaultUserFactory;
import main.java.entity.UserFactory;
import main.java.entity.User;
import main.java.use_case.login.LoginUserDataAccessInterface;
import main.java.use_case.signup.SignupUserDataAccessInterface;
import main.java.use_case.clear_users.ClearUserDataAccessInterface;
import main.java.use_case.update_users.UpdateUserDataAccessInterface;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUserDataAccessObject implements SignupUserDataAccessInterface, LoginUserDataAccessInterface, ClearUserDataAccessInterface,
        UpdateUserDataAccessInterface {

    private Connection conn = null;
    private final Map<String, User> accounts = new HashMap<>();
    private UserFactory userFactory;

    public DBUserDataAccessObject(UserFactory userFactory) {
        this.userFactory = userFactory;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/user",
                    "remoteUser",
                    "thisismysql*"
            );

            String sqlOrder = "SELECT userid, password, groupid1, groupid2, groupid3, groupid4, groupid5, groupid6," +
                    " groupid7, groupid8, courseid1, courseid2, courseid3, courseid4, courseid5, courseid6, courseid7, courseid8 FROM users";

            PreparedStatement statement = conn.prepareStatement(sqlOrder);

            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                String userId = rs.getString("userid");
                String userPw = rs.getString("password");
                User user = this.userFactory.create(userId, userPw);
                for(int i = 1; i <= 8; i++) {
                    user.getGroupId().add(rs.getString("groupid" + i));
                }
                for(int i = 1; i <= 8; i++) {
                    user.getCourseId().add(rs.getString("courseid" + i));
                }
                accounts.put(userId, user);
            }
            rs.close();
            statement.close();

        } catch (ClassNotFoundException e) {
            System.out.println("Class Not Found");
        } catch (SQLException e) {
            System.out.println("Check the database");
        } finally {
            if(conn != null) {
                try {
                    conn.close();
                    System.out.println("Connection closed");
                } catch (SQLException e) {}
            }
        }
    }

    // @Override
    public void saveUser(User user) {
        accounts.put(user.getId(), user);
        this.save();
    }

    // @Override
    public User get(String username) {
        return accounts.get(username);
    }

    public void save() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/user",
                    "remoteUser",
                    "thisismysql*"
            );

            for(User user : accounts.values()) {
                String sqlOrder = "INSERT INTO users (userid, password, groupid1, groupid2, groupid3, groupid4," +
                        " groupid5, groupid6, groupid7, groupid8, courseid1, courseid2, courseid3, courseid4, courseid5, " +
                        "courseid6, courseid7, courseid8) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                PreparedStatement statement = conn.prepareStatement(sqlOrder);
                statement.setString(1, user.getId());
                statement.setString(2, user.getPassword());
                for(int i = 3; i <= 10; i++) {
                    if((i - 2) > user.getGroupId().size()) {
                        statement.setString(i, "NONE");
                        continue;
                    }
                    statement.setString(i, user.getGroupId().get(i - 3));
                }
                for(int i = 11; i <= 18; i++) {
                    if((i - 10) > user.getCourseId().size()) {
                        statement.setString(i, "NONE");
                        continue;
                    }
                    statement.setString(i, user.getCourseId().get(i - 11));
                }
                statement.executeUpdate();
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Class Not Found");
        } catch (SQLException e) {
            System.out.println("Check the database");
        } finally {
            if(conn != null) {
                try {
                    conn.close();
                    System.out.println("Connection closed");
                } catch (SQLException e) {}
            }
        }
    }

    //@Override
    public boolean existsByName(String identifier) {
        return accounts.containsKey(identifier);
    }

    // @Override
    public void clear() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/user",
                    "remoteUser",
                    "thisismysql*"
            );
            String sqlOrder = "DELETE FROM user.users";

            PreparedStatement statement = conn.prepareStatement(sqlOrder);
            statement.executeUpdate();
            statement.close();

        } catch (ClassNotFoundException e) {
            System.out.println("Class Not Found");
        } catch (SQLException e) {
            System.out.println("Check the database");
        } finally {
            if(conn != null) {
                try {
                    conn.close();
                    System.out.println("Connection closed");
                } catch (SQLException e) {}
            }
        }
    }

    // @Override
    public void update(User user) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/user",
                    "remoteUser",
                    "thisismysql*"
            );
            StringBuilder sqlOrder = new StringBuilder()
                    .append("UPDATE user.users SET ")
                    .append("userid=?, ")
                    .append("password=?, ");

            for(int i = 1; i <= user.getGroupId().size(); i++) {
                sqlOrder.append("groupid").append(i).append("=?, ");
            }

            for(int i = 1; i <= user.getCourseId().size(); i++) {
                if (i == user.getCourseId().size()) {
                    sqlOrder.append("courseid").append(i).append("=?");
                }
                else {
                    sqlOrder.append("courseid").append(i).append("=?, ");
                }
            }
            PreparedStatement statement = conn.prepareStatement(sqlOrder.toString());

            statement.setString(1, user.getId());
            statement.setString(2, user.getPassword());

            for(int i = 0; i < user.getGroupId().size(); i++) {
                statement.setString(i + 3, user.getGroupId().get(i));
            }
            for(int i = 0; i < user.getCourseId().size(); i++) {
                statement.setString(i + 3 + user.getGroupId().size(), user.getCourseId().get(i));
            }
            statement.executeUpdate();
            statement.close();
        } catch (ClassNotFoundException e) {
            System.out.println("Class Not Found");
        } catch (SQLException e) {
            System.out.println("Check the database");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Connection closed");
                } catch (SQLException e) { }
            }
        }
    }

//    public static void main(String[] args) {
//        User user = new User("matthew","su36571536");
//        user.getCourseId().add("csc236");
//        user.getCourseId().add("CSC258");
//        user.getGroupId().add("group1");
//        user.getGroupId().add("group2");
//
//        Connection conn = null;
//        saveUser(user);
        // update(user);
        // clear();
//    }

}
