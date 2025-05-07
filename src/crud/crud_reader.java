package crud;

import java.sql.ResultSet;
import java.sql.Statement;

public class crud_reader {


    public static void read(Statement stmt) {
        String sql = "select * from reader;";
        try {
        ResultSet rs = stmt.executeQuery(sql);

            System.out.println("ticket\t\tname\t\t\tbirth year\t\taddress");
            while (rs.next()) {
                System.out.printf("%-12s", rs.getInt("readerticket"));
                System.out.printf("%-16s", rs.getString("name"));
                System.out.printf("%-16s", rs.getInt("birth"));
                System.out.println(rs.getString("address"));
            }

            rs.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public static void create(Statement stmt, int id, String name, int birth, String address) {
        String sql = "insert into reader(readerticket, name, birth, address) " +
                "values('" + id + "', '" + name + "', '" + birth + "', '" + address + "');";
        try {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Inserted successfully !");
    }

    public static void delete(Statement stmt, int id){
        String sql = "delete from reader where readerticket = "+id+";";
        try {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Row deleted successfully !");
    }

    public static void update(Statement stmt, int id, String name, int birth, String address){
        String sql = "update reader " +
                "set name = '"+name+"', birth = "+birth+", address = '"+address+"' " +
                "where readerticket = "+id+";";
        try{
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Row updated Successfully!");
    }
}
