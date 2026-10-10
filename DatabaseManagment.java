
package investagtionsoffice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class DatabaseManagment {
    private static final String URL="jdbc:sqlite:investigation.db";
    public static Connection connect(){
        Connection conn=null;
        try{
          conn=DriverManager.getConnection(URL);  
        }catch(SQLException e){
            System.out.println("the connction faild"+ e.getMessage());
        }
        return conn;
    }
    public static void createNewTables() {
        
        // كود SQL لإنشاء جدول القضايا
        String sqlCases = "CREATE TABLE IF NOT EXISTS cases (\n"
                + " case_number INTEGER PRIMARY KEY,\n"
                + " case_name TEXT NOT NULL,\n"
                + " case_description TEXT\n"
                + ");";

        // كود SQL لإنشاء جدول الأدلة (مع ربطه برقم القضية)
        String sqlEvidences = "CREATE TABLE IF NOT EXISTS evidences (\n"
                + " id INTEGER PRIMARY KEY,\n"
                + " type TEXT NOT NULL,\n"
                + " details TEXT,\n"
                + " status TEXT,\n"
                + " appearance_date TEXT,\n"
                + " case_number INTEGER,\n"
                + " FOREIGN KEY (case_number) REFERENCES cases (case_number) ON DELETE CASCADE\n"
                + ");";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()){
            stmt.execute(sqlCases);
            stmt.execute(sqlEvidences);
            System.out.println("data base table done");     }
        catch(SQLException e){
            System.out.println("error creating table"+ e.getMessage());
        }
                
}
    public static void insertCase(String case_name,int case_number,String case_description){
        String sql= "insert into cases(case_name,case_number,case_description) values(?,?,?)";
        try(Connection conn= connect();
               java.sql.PreparedStatement pstmt=conn.prepareStatement(sql);){
                pstmt.setString(1, case_name);
                pstmt.setInt(2, case_number);
                pstmt.setString(3, case_description);
                pstmt.executeUpdate();}
                catch(SQLException e){
                        System.out.println("error insert case >"+e.getMessage());
                        }
                
    }
    // دالة لإضافة دليل جديد وربطه بقضية موجودة
    public static void insertEvidence(int id, String type, String details, String status, String appearance_date, int case_number) {
        
        // كود SQL لإدخال 6 قيم
        String sql = "INSERT INTO evidences (id, type, details, status, appearance_date, case_number) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
             // تعبئة البيانات بالترتيب
             pstmt.setInt(1, id);
             pstmt.setString(2, type);
             pstmt.setString(3, details);
             pstmt.setString(4, status);
             pstmt.setString(5, appearance_date); // استخدمنا String لتسهيل حفظ التاريخ
             pstmt.setInt(6, case_number);        // هذا هو المفتاح الأجنبي الذي يربط الدليل بالقضية
             
             pstmt.executeUpdate();
             System.out.println("=> Evidence [" + type + "] linked to Case [" + case_number + "] successfully!");
             
        } catch (SQLException e) {
             System.out.println("=> Error inserting evidence > " + e.getMessage());
        }
    }
    public static void printAllCases(){
        String sql="select* from cases";
        try(Connection conn=connect();
                Statement stmt=conn.createStatement();
                java.sql.ResultSet rs=stmt.executeQuery(sql)){
            System.out.println("stored cases");
            while(rs.next()){
                int number=rs.getInt("case_number");
                String name=rs.getString("case_name");
                String description=rs.getString("case_description");
                
                System.out.println("the case number:"+number+"the name of case:"+name);
                System.out.println("description of the case:"+description);
                System.out.println("---------------------------------------------");
            }
            
        }
        catch(SQLException e){
                    System.out.println("error fetching case"+ e.getMessage());
                    }        
    }
    // دالة لجلب الأدلة المرتبطة بقضية محددة
    public static void printEvidencesForCase(int caseNumber) {
        
        // كود SQL لجلب الأدلة بشرط أن يتطابق رقم القضية
        String sql = "SELECT * FROM evidences WHERE case_number = ?";

        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
             // حقن رقم القضية في علامة الاستفهام
             pstmt.setInt(1, caseNumber);
             
             // تنفيذ الاستعلام وحفظ النتائج في ResultSet
             try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                 
                 System.out.println("\n=== 🔍 أدلـة الـقـضـيـة رقـم [" + caseNumber + "] ===");
                 boolean isFound = false; // متغير لنتأكد هل وجدنا أدلة أم لا
                 
                 while (rs.next()) {
                     isFound = true;
                     int evidenceId = rs.getInt("id");
                     String type = rs.getString("type");
                     String details = rs.getString("details");
                     String status = rs.getString("status");
                     String date = rs.getString("appearance_date");
                     
                     // طباعة بيانات الدليل
                     System.out.println("🔹 دليل رقم: " + evidenceId + " | النوع: " + type);
                     System.out.println("   التفاصيل: " + details);
                     System.out.println("   الحالة: " + status + " | التاريخ: " + date);
                     System.out.println("------------------------------------------------");
                 }
                 
                 // إذا انتهى البحث ولم نجد أي دليل
                 if (!isFound) {
                     System.out.println("⚠️ لا توجد أدلة مسجلة لهذه القضية حتى الآن.");
                 }
             }
             
        } catch (SQLException e) {
            System.out.println("=> Error fetching evidences > " + e.getMessage());
        }
    }
}
