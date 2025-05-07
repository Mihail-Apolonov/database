import crud.crud_book;
import crud.crud_library;
import crud.crud_publisher;
import crud.crud_reader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public static void main(String args[]) {
    Connection c = null;
    Statement stmt = null;
    try {
        Class.forName("org.postgresql.Driver");
        c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "1234");
        stmt = c.createStatement();
    } catch (Exception e) {
        System.err.println(e.getClass().getName() + ": " + e.getMessage());
        System.exit(0);
    }
}