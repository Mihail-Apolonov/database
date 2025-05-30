import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class AdminPanel extends JFrame {
    private Connection connection;

    public AdminPanel(Connection connection) {
        this.connection = connection;
        setTitle("Панель администратора");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Вкладка "Книги"
        JPanel bookPanel = createTabPanel("SELECT article, name, year, genre FROM book", "Книги");
        tabbedPane.addTab("Книги", bookPanel);

        // Вкладка "Читатели"
        JPanel readerPanel = createTabPanel("SELECT readerticket, name, address FROM reader", "Читатели");
        tabbedPane.addTab("Читатели", readerPanel);

        // Вкладка "Библиотеки"
        JPanel libraryPanel = createTabPanel("SELECT id, name, address, phonenumber FROM librarybrunch", "Библиотеки");
        tabbedPane.addTab("Библиотеки", libraryPanel);

        // Вкладка "Издатели"
        JPanel publisherPanel = createTabPanel("SELECT id, name, address FROM publisher", "Издатели");
        tabbedPane.addTab("Издатели", publisherPanel);

        add(tabbedPane);
        setVisible(true);
    }

    private JPanel createTabPanel(String query, String tableName) {
        JPanel panel = new JPanel(new BorderLayout());

        // Создаем таблицу
        JTable table = createTable(query);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Кнопка "Добавить"
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddDialog(tableName, table);
            }
        });
        buttonPanel.add(addButton);

        // Кнопка "Изменить"
        JButton editButton = new JButton("Изменить");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    showEditDialog(tableName, table, selectedRow);
                } else {
                    JOptionPane.showMessageDialog(AdminPanel.this,
                            "Пожалуйста, выберите запись для редактирования",
                            "Ошибка", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        buttonPanel.add(editButton);

        // Кнопка "Удалить"
        JButton deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int confirm = JOptionPane.showConfirmDialog(AdminPanel.this,
                            "Вы уверены, что хотите удалить выбранную запись?",
                            "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteRecord(tableName, table, selectedRow);
                    }
                } else {
                    JOptionPane.showMessageDialog(AdminPanel.this,
                            "Пожалуйста, выберите запись для удаления",
                            "Ошибка", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        buttonPanel.add(deleteButton);

        // Кнопка "Обновить"
        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable(table, query);
            }
        });
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JTable createTable(String query) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData metaData = rs.getMetaData();

            // Создаем модель таблицы
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Запрещаем редактирование ячеек напрямую
                }
            };
            int columnCount = metaData.getColumnCount();

            // Добавляем названия колонок
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }

            // Добавляем данные
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

            rs.close();
            stmt.close();

            return new JTable(model);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            return new JTable();
        }
    }

    private void showAddDialog(String tableName, JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int columnCount = model.getColumnCount();

        JPanel panel = new JPanel(new GridLayout(columnCount, 2));
        JTextField[] fields = new JTextField[columnCount];

        for (int i = 0; i < columnCount; i++) {
            panel.add(new JLabel(model.getColumnName(i) + ":"));
            fields[i] = new JTextField();
            panel.add(fields[i]);
        }

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Добавить новую запись в " + tableName, JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
                for (int i = 0; i < columnCount; i++) {
                    if (i > 0) sql.append(", ");
                    sql.append("?");
                }
                sql.append(")");

                PreparedStatement pstmt = connection.prepareStatement(sql.toString());
                for (int i = 0; i < columnCount; i++) {
                    pstmt.setObject(i + 1, fields[i].getText());
                }

                pstmt.executeUpdate();
                pstmt.close();

                refreshTable(table, getSelectQueryForTable(tableName));
                JOptionPane.showMessageDialog(this, "Запись успешно добавлена", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении записи: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog(String tableName, JTable table, int selectedRow) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int columnCount = model.getColumnCount();

        JPanel panel = new JPanel(new GridLayout(columnCount, 2));
        JTextField[] fields = new JTextField[columnCount];

        for (int i = 0; i < columnCount; i++) {
            panel.add(new JLabel(model.getColumnName(i) + ":"));
            fields[i] = new JTextField(model.getValueAt(selectedRow, i).toString());
            panel.add(fields[i]);
        }

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Редактировать запись в " + tableName, JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
                for (int i = 1; i < columnCount; i++) {
                    if (i > 1) sql.append(", ");
                    sql.append(model.getColumnName(i)).append(" = ?");
                }
                sql.append(" WHERE ").append(model.getColumnName(0)).append(" = ?");

                PreparedStatement pstmt = connection.prepareStatement(sql.toString());
                for (int i = 1; i < columnCount; i++) {
                    pstmt.setObject(i, fields[i].getText());
                }
                pstmt.setObject(columnCount, fields[0].getText());

                pstmt.executeUpdate();
                pstmt.close();

                refreshTable(table, getSelectQueryForTable(tableName));
                JOptionPane.showMessageDialog(this, "Запись успешно обновлена", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при обновлении записи: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteRecord(String tableName, JTable table, int selectedRow) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String idColumnName = model.getColumnName(0);
        Object idValue = model.getValueAt(selectedRow, 0);

        try {
            String sql = "DELETE FROM " + tableName + " WHERE " + idColumnName + " = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setObject(1, idValue);

            pstmt.executeUpdate();
            pstmt.close();

            refreshTable(table, getSelectQueryForTable(tableName));
            JOptionPane.showMessageDialog(this, "Запись успешно удалена", "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при удалении записи: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable(JTable table, String query) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Очищаем таблицу

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ошибка обновления данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSelectQueryForTable(String tableName) {
        switch (tableName) {
            case "Книги":
                return "SELECT article, name, year, genre FROM book";
            case "Читатели":
                return "SELECT readerticket, name, address FROM reader";
            case "Библиотеки":
                return "SELECT id, name, address, phonenumber FROM librarybrunch";
            case "Издатели":
                return "SELECT id, name, address FROM publisher";
            default:
                return "";
        }
    }
}