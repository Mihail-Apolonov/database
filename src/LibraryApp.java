import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryApp {

    private static Connection connection;
    private static Statement stmt;

    public static void main(String[] args) {
        // Подключение к базе данных
        connectToDatabase();

        // Получаем список библиотек из БД
        List<Library> libraries = getLibraries();

        // Если нет библиотек в БД
        if (libraries.isEmpty()) {
            JOptionPane.showMessageDialog(null, "В базе данных нет библиотек", "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        // Создаем диалог выбора библиотеки
        Library selectedLibrary = (Library) JOptionPane.showInputDialog(
                null,
                "Выберите библиотеку:",
                "Выбор библиотеки",
                JOptionPane.QUESTION_MESSAGE,
                null,
                libraries.toArray(),
                libraries.get(0));

        // Если пользователь нажал "Отмена"
        if (selectedLibrary == null) {
            System.exit(0);
        }

        // Создаем и показываем главное окно приложения
        new LibraryAppMainWindow(selectedLibrary.getId(), selectedLibrary.getName(), connection, stmt);
    }

    private static void connectToDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "1234");
            stmt = connection.createStatement();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ошибка подключения к базе данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private static List<Library> getLibraries() {
        List<Library> libraries = new ArrayList<>();
        try {
            String sql = "SELECT id, name FROM librarybrunch;";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                libraries.add(new Library(rs.getInt("id"), rs.getString("name")));
            }
            rs.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ошибка при получении списка библиотек: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        return libraries;
    }
}

class Library {
    private int id;
    private String name;

    public Library(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}