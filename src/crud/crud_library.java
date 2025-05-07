package crud;

import java.sql.ResultSet;
import java.sql.Statement;

public class crud_library {

    public static void read(Statement stmt) {
        String sql = "select * from librarybrunch;";
        try {
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("id\tname\t\t\taddress\t\t\tphone");
            while (rs.next()) {
                System.out.printf("%-4s", rs.getInt("id"));
                System.out.printf("%-16s", rs.getString("name"));
                System.out.printf("%-16s", rs.getString("address"));
                System.out.println(rs.getInt("phonenumber"));
            }
            rs.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public static void create(Statement stmt, String name, String address, int phonenumber) {
        String sql = "insert into librarybrunch(name, address, phonenumber) " +
                "values('" + name + "', '" + address + "', '" + phonenumber + "');";
        try {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Inserted successfully !");
    }

    public static void delete(Statement stmt, int id){
        String sql = "delete from librarybrunch where id = "+id+";";
        try {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Row deleted successfully !");
    }

    public static void update(Statement stmt, int id, String name, String address, int number){
        String sql = "update librarybrunch " +
                "set name = '"+name+"', address = '"+address+"', phonenumber = "+number+" " +
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
