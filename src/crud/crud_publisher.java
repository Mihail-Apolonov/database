package crud;

import java.sql.ResultSet;
import java.sql.Statement;

public class crud_publisher {

    public static void read(Statement stmt) {
        String sql = "select * from publisher;";
        try {
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("id\tname\t\t\taddress");
            while (rs.next()) {
                System.out.printf("%-4s", rs.getInt("id"));
                System.out.printf("%-16s", rs.getString("name"));
                System.out.println(rs.getString("address"));
            }
            rs.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public static void create(Statement stmt, String name, String address) {
        String sql = "insert into publisher(name, address) " +
                "values('" + name + "', '" + address + "');";
        try {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Inserted successfully !");
    }

    public static void delete(Statement stmt, int id){
        String sql = "delete from publisher where id = "+id+";";
        try {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Row deleted successfully !");
    }

    public static void update(Statement stmt, int id, String name, String address){
        String sql = "update publisher " +
                "set name = '"+name+"', address = '"+address+"' " +
                "where id = "+id+";";
        try{
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Row updated Successfully!");
    }
}
