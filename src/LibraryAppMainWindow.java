import crud.crud_library;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryAppMainWindow {

    private static Connection connection;
    private static Statement stmt;
    private static int selectedLibraryId;
    private static String selectedLibraryName;

    public LibraryAppMainWindow(int libraryId, String libraryName, Connection conn, Statement statement) {
        this.selectedLibraryId = libraryId;
        this.selectedLibraryName = libraryName;
        this.connection = conn;
        this.stmt = statement;

        createAndShowMainWindow();
    }

    private void createAndShowMainWindow() {
        JFrame frame = new JFrame("Библиотечная система - " + selectedLibraryName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);

        // Создаем основную панель с вкладками
        JTabbedPane tabbedPane = new JTabbedPane();

        // Вкладка "Книги"
        JPanel booksPanel = createBooksPanel();
        tabbedPane.addTab("Книги", booksPanel);

        // Вкладка "Выдача/возврат"
        JPanel lendingPanel = createLendingPanel();
        tabbedPane.addTab("Выдача/возврат", lendingPanel);

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок
        JLabel titleLabel = new JLabel("Книги в библиотеке: " + selectedLibraryName, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Таблица с книгами
        String[] columnNames = {"Артикул", "Название", "Год", "Жанр", "Издатель", "Статус"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable booksTable = new JTable(model);
        booksTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(booksTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Заполняем таблицу данными
        loadBooksData(model);

        return panel;
    }

    private void loadBooksData(DefaultTableModel model) {
        try {
            // Очищаем таблицу
            model.setRowCount(0);

            // SQL запрос для получения книг в выбранной библиотеке
            String sql = "SELECT b.article, b.name, b.year, b.genre, p.name as publisher_name, " +
                    "CASE WHEN b.article IN (SELECT bl.book_id FROM book_library bl WHERE library_id = " + selectedLibraryId + ") THEN 'В библиотеке' " +
                    "WHEN b.article IN (SELECT r.book_id FROM reader r) THEN 'На руках' ELSE 'Нет в наличии' END as status " +
                    "FROM book b " +
                    "JOIN publisher p ON b.publisher_id = p.id ";

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("article"),
                        rs.getString("name"),
                        rs.getInt("year"),
                        rs.getString("genre"),
                        rs.getString("publisher_name"),
                        rs.getString("status")
                };
                model.addRow(row);
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке данных о книгах: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createLendingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок
        JLabel titleLabel = new JLabel("Выдача и возврат книг", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Основная панель с двумя таблицами
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Таблица с книгами (левая)
        String[] booksColumns = {"Артикул", "Название", "Жанр", "Статус"};
        DefaultTableModel booksModel = new DefaultTableModel(booksColumns, 0);
        JTable booksTable = new JTable(booksModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane booksScrollPane = new JScrollPane(booksTable);
        booksScrollPane.setBorder(BorderFactory.createTitledBorder("Книги"));

        // Таблица с читателями (правая)
        String[] readersColumns = {"ID", "ФИО", "Год рождения", "Адрес", "Книга"};
        DefaultTableModel readersModel = new DefaultTableModel(readersColumns, 0);
        JTable readersTable = new JTable(readersModel);
        readersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane readersScrollPane = new JScrollPane(readersTable);
        readersScrollPane.setBorder(BorderFactory.createTitledBorder("Читатели"));

        // Загрузка данных в таблицы
        loadLendingBooksData(booksModel);
        loadReadersData(readersModel);

        // Добавляем таблицы на панель
        tablesPanel.add(booksScrollPane);
        tablesPanel.add(readersScrollPane);

        // Панель с кнопками управления
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton lendButton = new JButton("Выдать книгу");
        JButton returnButton = new JButton("Принять возврат");
        buttonsPanel.add(lendButton);
        buttonsPanel.add(returnButton);

        // Добавляем все на основную панель
        panel.add(tablesPanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadLendingBooksData(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            String sql = "SELECT b.article, b.name, b.year, b.genre, p.name as publisher_name, " +
                    "CASE WHEN b.article IN (SELECT bl.book_id FROM book_library bl WHERE library_id = " + selectedLibraryId + ") THEN 'В библиотеке' " +
                    "WHEN b.article IN (SELECT r.book_id FROM reader r) THEN 'На руках' ELSE 'Нет в наличии' END as status " +
                    "FROM book b " +
                    "JOIN publisher p ON b.publisher_id = p.id ";

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("article"),
                        rs.getString("name"),
                        rs.getInt("year"),
                        rs.getString("genre"),
                        rs.getString("publisher_name"),
                        rs.getString("status")
                });
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке списка книг: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReadersData(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            String sql = "SELECT * FROM reader ";

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("readerticket"),
                        rs.getString("name"),
                        rs.getInt("birth"),
                        rs.getString("address"),
                        rs.getInt("book_id")
                });
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке списка читателей: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}