import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class LibraryAppMainWindow {
    private Connection connection;
    private Statement stmt;
    private int selectedLibraryId;
    private String selectedLibraryName;

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

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Книги", createBooksPanel());
        tabbedPane.addTab("Выдача/возврат", createLendingPanel());

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Книги в библиотеке: " + selectedLibraryName, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"Артикул", "Название", "Год", "Жанр", "Издатель", "Статус"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable booksTable = new JTable(model);
        booksTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(booksTable), BorderLayout.CENTER);

        loadBooksData(model);
        return panel;
    }

    private JPanel createLendingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Выдача и возврат книг", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Панель с таблицами
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Таблица книг
        String[] booksColumns = {"Артикул", "Название", "Год", "Жанр", "Статус"};
        DefaultTableModel booksModel = new DefaultTableModel(booksColumns, 0);
        JTable booksTable = new JTable(booksModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane booksScrollPane = new JScrollPane(booksTable);
        booksScrollPane.setBorder(BorderFactory.createTitledBorder("Книги"));

        // Таблица читателей
        String[] readersColumns = {"ID", "ФИО", "Год рождения", "Адрес", "ID книги"};
        DefaultTableModel readersModel = new DefaultTableModel(readersColumns, 0);
        JTable readersTable = new JTable(readersModel);
        readersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane readersScrollPane = new JScrollPane(readersTable);
        readersScrollPane.setBorder(BorderFactory.createTitledBorder("Читатели"));

        tablesPanel.add(booksScrollPane);
        tablesPanel.add(readersScrollPane);

        // Панель кнопок
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton lendButton = new JButton("Выдать книгу");
        JButton returnButton = new JButton("Принять возврат");

        // Обработчик кнопки "Выдать книгу"
        lendButton.addActionListener(e -> {
            int selectedBookRow = booksTable.getSelectedRow();
            int selectedReaderRow = readersTable.getSelectedRow();

            // Проверка выбора
            if (selectedBookRow == -1 || selectedReaderRow == -1) {
                showError("Пожалуйста, выберите книгу и читателя");
                return;
            }

            // Получаем данные
            int bookArticle = (int) booksModel.getValueAt(selectedBookRow, 0);
            String bookStatus = (String) booksModel.getValueAt(selectedBookRow, 4);
            int readerId = (int) readersModel.getValueAt(selectedReaderRow, 0);
            Object readerBookId = readersModel.getValueAt(selectedReaderRow, 4);

            // Проверка доступности книги
            if (!"В библиотеке".equals(bookStatus)) {
                showError("Выбранная книга не доступна для выдачи");
                return;
            }

            // Проверка, что у читателя нет книги
            if (readerBookId != null && !readerBookId.toString().isEmpty()) {
                showError("У выбранного читателя уже есть книга");
                return;
            }

            // Выполнение операции выдачи
            try {
                connection.setAutoCommit(false);

                // 1. Обновляем запись читателя
                String updateReaderSQL = "UPDATE reader SET book_id = ? WHERE readerticket = ?";
                try (PreparedStatement ps = connection.prepareStatement(updateReaderSQL)) {
                    ps.setInt(1, bookArticle);
                    ps.setInt(2, readerId);
                    ps.executeUpdate();
                }

                // 2. Удаляем запись из book_library
                String deleteSQL = "DELETE FROM book_library WHERE book_id = ? AND library_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(deleteSQL)) {
                    ps.setInt(1, bookArticle);
                    ps.setInt(2, selectedLibraryId);
                    int rows = ps.executeUpdate();

                    if (rows == 0) {
                        connection.rollback();
                        showError("Книга не найдена в текущей библиотеке");
                        return;
                    }
                }

                connection.commit();
                JOptionPane.showMessageDialog(panel, "Книга успешно выдана", "Успех", JOptionPane.INFORMATION_MESSAGE);

                // Обновляем таблицы
                loadLendingBooksData(booksModel);
                loadReadersData(readersModel);
                booksTable.clearSelection();
                readersTable.clearSelection();

            } catch (SQLException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rbEx) {
                    rbEx.printStackTrace();
                }
                showError("Ошибка при выдаче книги: " + ex.getMessage());
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException acEx) {
                    acEx.printStackTrace();
                }
            }
        });

        returnButton.addActionListener(e -> {
            int selectedBookRow = booksTable.getSelectedRow();
            int selectedReaderRow = readersTable.getSelectedRow();

            // Проверка выбора
            if (selectedBookRow == -1 || selectedReaderRow == -1) {
                showError("Пожалуйста, выберите книгу и читателя");
                return;
            }

            // Получаем данные с учетом возможных Long значений
            long bookArticle = ((Number)booksModel.getValueAt(selectedBookRow, 0)).longValue();
            String bookStatus = (String) booksModel.getValueAt(selectedBookRow, 4);
            long readerId = ((Number)readersModel.getValueAt(selectedReaderRow, 0)).longValue();
            Object readerBookIdObj = readersModel.getValueAt(selectedReaderRow, 4);
            long readerBookId = (readerBookIdObj != null) ? ((Number)readerBookIdObj).longValue() : 0L;

            // Проверки
            if (readerBookId == 0L) {
                showError("У выбранного читателя нет книги");
                return;
            }

            if (!"На руках".equals(bookStatus)) {
                showError("Выбранная книга не находится на руках");
                return;
            }

            if (bookArticle != readerBookId) {
                showError("Выбранная книга не совпадает с книгой у читателя");
                return;
            }

            // Подтверждение операции
            int confirm = JOptionPane.showConfirmDialog(
                    panel,
                    "Подтвердите возврат книги: " + booksModel.getValueAt(selectedBookRow, 1) +
                            " от читателя: " + readersModel.getValueAt(selectedReaderRow, 1),
                    "Подтверждение возврата",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Выполнение операции возврата
            try {
                connection.setAutoCommit(false);

                // 1. Обнуляем book_id у читателя
                String updateReaderSQL = "UPDATE reader SET book_id = NULL WHERE readerticket = ?";
                try (PreparedStatement ps = connection.prepareStatement(updateReaderSQL)) {
                    ps.setLong(1, readerId);
                    ps.executeUpdate();
                }

                // 2. Добавляем запись в book_library
                String insertSQL = "INSERT INTO book_library (book_id, library_id) VALUES (?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(insertSQL)) {
                    ps.setLong(1, bookArticle);
                    ps.setLong(2, selectedLibraryId);
                    ps.executeUpdate();
                }

                connection.commit();
                JOptionPane.showMessageDialog(panel, "Книга успешно возвращена в библиотеку", "Успех", JOptionPane.INFORMATION_MESSAGE);

                // Обновляем таблицы
                loadLendingBooksData(booksModel);
                loadReadersData(readersModel);
                booksTable.clearSelection();
                readersTable.clearSelection();

            } catch (SQLException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rbEx) {
                    rbEx.printStackTrace();
                }
                showError("Ошибка при возврате книги: " + ex.getMessage());
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException acEx) {
                    acEx.printStackTrace();
                }
            }
        });

        buttonsPanel.add(lendButton);
        buttonsPanel.add(returnButton);

        // Загрузка данных
        loadLendingBooksData(booksModel);
        loadReadersData(readersModel);

        panel.add(tablesPanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadBooksData(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            String sql = "SELECT b.article, b.name, b.year, b.genre, p.name as publisher_name, " +
                    "CASE WHEN b.article IN (SELECT bl.book_id FROM book_library bl WHERE library_id = ?) THEN 'В библиотеке' " +
                    "WHEN b.article IN (SELECT r.book_id FROM reader r) THEN 'На руках' ELSE 'Нет в наличии' END as status " +
                    "FROM book b JOIN publisher p ON b.publisher_id = p.id";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, selectedLibraryId);
                ResultSet rs = ps.executeQuery();
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
            }
        } catch (SQLException e) {
            showError("Ошибка при загрузке данных о книгах: " + e.getMessage());
        }
    }

    private void loadLendingBooksData(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            String sql = "SELECT b.article, b.name, b.year, b.genre, " +
                    "CASE WHEN b.article IN (SELECT bl.book_id FROM book_library bl WHERE library_id = ?) THEN 'В библиотеке' " +
                    "WHEN b.article IN (SELECT r.book_id FROM reader r) THEN 'На руках' ELSE 'Нет в наличии' END as status " +
                    "FROM book b";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, selectedLibraryId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("article"),
                            rs.getString("name"),
                            rs.getInt("year"),
                            rs.getString("genre"),
                            rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            showError("Ошибка при загрузке списка книг: " + e.getMessage());
        }
    }

    private void loadReadersData(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            String sql = "SELECT readerticket, name, birth, address, book_id FROM reader";

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("readerticket"),
                            rs.getString("name"),
                            rs.getInt("birth"),
                            rs.getString("address"),
                            rs.getObject("book_id")
                    });
                }
            }
        } catch (SQLException e) {
            showError("Ошибка при загрузке списка читателей: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
}