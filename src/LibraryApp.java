import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryApp {
    private static Connection connection;
    private static Statement stmt;

    public static void main(String[] args) {
        connectToDatabase();
        List<Library> libraries = getLibraries();

        if (libraries.isEmpty()) {
            JOptionPane.showMessageDialog(null, "В базе данных нет библиотек", "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        JFrame selectionFrame = new JFrame("Выбор режима работы");
        selectionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        selectionFrame.setSize(400, 200);
        selectionFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel libraryPanel = new JPanel(new BorderLayout(5, 5));
        libraryPanel.add(new JLabel("Выберите библиотеку:"), BorderLayout.NORTH);
        JComboBox<Library> libraryComboBox = new JComboBox<>(libraries.toArray(new Library[0]));
        libraryPanel.add(libraryComboBox, BorderLayout.CENTER);

        panel.add(libraryPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        JButton userButton = new JButton("Войти как пользователь");
        userButton.addActionListener(e -> {
            Library selectedLibrary = (Library) libraryComboBox.getSelectedItem();
            if (selectedLibrary != null) {
                new LibraryAppMainWindow(selectedLibrary.getId(), selectedLibrary.getName(), connection, stmt);
                selectionFrame.dispose();
            }
        });

        JButton adminButton = new JButton("Войти как администратор");
        adminButton.addActionListener(e -> {
            selectionFrame.dispose();
            new AdminPanel(connection); // Передаем соединение в AdminPanel
        });

        buttonPanel.add(userButton);
        buttonPanel.add(adminButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        selectionFrame.add(panel);
        selectionFrame.setVisible(true);
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