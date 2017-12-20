package pullup.ctd.com.myapplication;

/**
 * Created by steph on 12/16/2017.
 */
//STEP 1. Import required packages
import java.sql.*;

public class JDBC {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://pulluptest.cf2qkbf5sehb.us-east-1.rds.amazonaws.com/PullUpTest";

    //  Database credentials
    static final String USER = "bigsteve4288";
    static final String PASS = "Raven0209";

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try{
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected database successfully...");

            //STEP 4: Execute a query
            System.out.println("Inserting records into the table...");
            stmt = conn.createStatement();

            String sql = "INSERT INTO pullup (eventtype, eventname, eventAddress, eventstarttime, eventhost, eventstatus, eventimage) " +
                    "VALUES ('Radio', 'LitStation', '1207 Elzy Avenue Greenwood,MS 38930', '900','Gip','343','im')";
            stmt.executeUpdate(sql);
       /*     sql = "INSERT INTO Registration " +
                    "VALUES (101, 'Mahnaz', 'Fatma', 25)";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO Registration " +
                    "VALUES (102, 'Zaid', 'Khan', 30)";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO Registration " +
                    "VALUES(103, 'Sumit', 'Mittal', 28)";
            stmt.executeUpdate(sql);*/
            System.out.println("Inserted records into the table...");

        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    conn.close();
            }catch(SQLException se){
            }// do nothing
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");
    }//end main
}//end JDBCExample
