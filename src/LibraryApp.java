import crud.crud_book;
import crud.crud_library;
import crud.crud_publisher;
import crud.crud_reader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class LibraryApp extends JFrame {
    private Connection c = null;
    private Statement stmt = null;

    public LibraryApp() {
        initializeDatabaseConnection();
        if (c == null || stmt == null) {
            JOptionPane.showMessageDialog(this, "Не удалось подключиться к базе данных", "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setupUI();
    }

    private void initializeDatabaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "1234");
            stmt = c.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupUI() {
        setTitle("Библиотечная система");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Создаем панель с вкладками
        JTabbedPane tabbedPane = new JTabbedPane();

        // Добавляем вкладки для каждого раздела
        tabbedPane.addTab("Книги", createBookPanel());
        tabbedPane.addTab("Читатели", createReaderPanel());
        tabbedPane.addTab("Издательства", createPublisherPanel());
        tabbedPane.addTab("Филиалы", createLibraryPanel());

        add(tabbedPane);
    }

    private JPanel createBookPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Кнопки управления
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton readBtn = new JButton("Показать все книги");
        JButton createBtn = new JButton("Добавить книгу");
        JButton updateBtn = new JButton("Изменить книгу");
        JButton deleteBtn = new JButton("Удалить книгу");

        buttonPanel.add(readBtn);
        buttonPanel.add(createBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);

        // Область для вывода данных
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Обработчики событий
        readBtn.addActionListener(e -> {
            outputArea.setText("");
            crud_book.read(stmt);
        });

        createBtn.addActionListener(e -> {
            JPanel inputPanel = new JPanel(new GridLayout(5, 2));

            JTextField articleField = new JTextField();
            JTextField nameField = new JTextField();
            JTextField yearField = new JTextField();
            JTextField genreField = new JTextField();
            JTextField publisherField = new JTextField();

            inputPanel.add(new JLabel("Артикул:"));
            inputPanel.add(articleField);
            inputPanel.add(new JLabel("Название:"));
            inputPanel.add(nameField);
            inputPanel.add(new JLabel("Год издания:"));
            inputPanel.add(yearField);
            inputPanel.add(new JLabel("Жанр:"));
            inputPanel.add(genreField);
            inputPanel.add(new JLabel("ID издательства:"));
            inputPanel.add(publisherField);

            int result = JOptionPane.showConfirmDialog(this, inputPanel,
                    "Добавить новую книгу", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    int article = Integer.parseInt(articleField.getText());
                    String name = nameField.getText();
                    int year = Integer.parseInt(yearField.getText());
                    String genre = genreField.getText();
                    int publisherId = Integer.parseInt(publisherField.getText());

                    crud_book.create(stmt, article, name, year, genre, publisherId);
                    outputArea.append("Книга успешно добавлена!\n");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Некорректные данные", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Аналогично добавьте обработчики для updateBtn и deleteBtn

        return panel;
    }

    private JPanel createReaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // Аналогично createBookPanel()
        return panel;
    }

    private JPanel createPublisherPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // Аналогично createBookPanel()
        return panel;
    }

    private JPanel createLibraryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // Аналогично createBookPanel()
        return panel;
    }

    @Override
    public void dispose() {
        try {
            if (stmt != null) stmt.close();
            if (c != null) c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryApp app = new LibraryApp();
            app.setVisible(true);
        });
    }
}