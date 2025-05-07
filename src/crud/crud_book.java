package crud;

import java.sql.ResultSet;
import java.sql.Statement;

public class crud_book {

    public static void read(Statement stmt) {
        String sql = "select article, name, year, genre, reader_id from book;";
        try {
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("article\t\tname\t\t\t\tyear\t\tgenre\t\tstatus");
            while (rs.next()) {
                System.out.printf("%-12s", rs.getInt("article"));
                System.out.printf("%-20s", rs.getString("name"));
                System.out.printf("%-12s", rs.getInt("year"));
                System.out.printf("%-12s", rs.getString("genre"));
                int status = rs.getInt("reader_id");
                if (status != 0) System.out.println("На руках");
                else System.out.println();
            }
            rs.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public static void create(Statement stmt, int article, String name, int year, String genre, int publisher_id) {
        String sql = "insert into book(article, name, year, genre, publisher_id) " +
                "values('" + article + "', '" + name + "', '" + year + "', '" + genre + "', '"+publisher_id+"');";
        try {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Inserted successfully !");
    }

    public static void delete(Statement stmt, int article){
        String sql = "delete from book where article = "+article+";";
        try {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Row deleted successfully !");
    }

    public static void update(Statement stmt, int article, String name, int year, String genre){
        String sql = "update book " +
                "set name = '"+name+"', year = "+year+", genre = '"+genre+"' " +
                "where article = "+article+";";
        try{
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Row updated Successfully!");
    }
}
