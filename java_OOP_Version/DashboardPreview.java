package investagtionsoffice;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardPreview extends JFrame {

    private JPanel sideMenu; 
    private JTable casesTable;

    public DashboardPreview() {
        setTitle("نظام إدارة التحقيقات المتقدم");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        Color bgColor = new Color(19, 19, 20);       
        Color surfaceColor = new Color(30, 31, 34);  
        Color textColor = new Color(227, 227, 227);  

        getContentPane().setBackground(bgColor);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(bgColor);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JButton btnToggle = new JButton("☰");
        btnToggle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btnToggle.setBackground(bgColor);
        btnToggle.setForeground(textColor);
        btnToggle.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnToggle.setFocusPainted(false);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(bgColor);
        JLabel searchIcon = new JLabel("🔍 ");
        searchIcon.setForeground(textColor);
        JTextField searchField = new JTextField(20);
        searchField.setBackground(surfaceColor);
        searchField.setForeground(textColor);
        searchField.setCaretColor(textColor);
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        searchPanel.add(searchIcon);
        searchPanel.add(searchField);

        topPanel.add(btnToggle, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

    
        // 2. جدول القضايا
        String[] columns = {"رقم القضية", "اسم القضية", "الوصف", "الحالة"};
        casesTable = new JTable(new DefaultTableModel(new Object[][]{}, columns));
        casesTable.setBackground(surfaceColor);
        casesTable.setForeground(textColor);
        casesTable.setGridColor(new Color(60, 64, 67));
        casesTable.setRowHeight(40);
        casesTable.getTableHeader().setBackground(bgColor);
        casesTable.getTableHeader().setForeground(textColor);
        casesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(casesTable);
        scrollPane.getViewport().setBackground(bgColor);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

     
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) casesTable.getModel());
        casesTable.setRowSorter(sorter);
        
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
         
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

     
        // 3. القائمة الجانبية (Drawer) وأزرار التحكم
   
        sideMenu = new JPanel(new GridLayout(6, 1, 0, 10));
        sideMenu.setBackground(surfaceColor);
        sideMenu.setPreferredSize(new Dimension(200, 0));
        sideMenu.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        sideMenu.setVisible(false); 
        
        JButton btnRefresh = createMenuButton("🔄 تحديث السجل", surfaceColor, textColor);
        JButton btnAdd = createMenuButton("➕ إضافة قضية", surfaceColor, textColor);
        JButton btnEdit = createMenuButton("✏️ إدارة القضية والأدلة", surfaceColor, textColor);
        JButton btnDelete = createMenuButton("❌ حذف قضية", surfaceColor, textColor);

        btnRefresh.addActionListener(e -> {
            refreshTableData(casesTable);
            sideMenu.setVisible(false); 
        });

        btnAdd.addActionListener(e -> {
            sideMenu.setVisible(false); 
            JTextField nameField = new JTextField();
            JTextField descField = new JTextField();
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"مفتوحة", "مغلقة", "قيد التحقيق"});
            
            Object[] inputFields = {"اسم القضية:", nameField, "وصف القضية:", descField, "حالة القضية:", statusCombo};
            int option = JOptionPane.showConfirmDialog(this, inputFields, "إضافة قضية جديدة", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            
            if (option == JOptionPane.OK_OPTION) {
                if (nameField.getText().isEmpty() || descField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "الرجاء إدخال البيانات المطلوبة!", "تنبيه", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String url = "jdbc:sqlite:investigation.db";
                String sql = "INSERT INTO cases (name, description, status) VALUES (?, ?, ?)";
                try (Connection conn = DriverManager.getConnection(url);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                     pstmt.setString(1, nameField.getText());
                     pstmt.setString(2, descField.getText());
                     pstmt.setString(3, statusCombo.getSelectedItem().toString());
                     pstmt.executeUpdate(); 
                     JOptionPane.showMessageDialog(this, "تم تسجيل القضية بنجاح!");
                     refreshTableData(casesTable); 
                } catch (SQLException ex) {
                     JOptionPane.showMessageDialog(this, "خطأ: " + ex.getMessage());
                }
            }
        });

        btnEdit.addActionListener(e -> {
            sideMenu.setVisible(false);
            int selectedRow = casesTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "يرجى تحديد قضية من الجدول أولاً!", "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // سحب البيانات من الصف المفلتر لضمان صحة التحديد حتى أثناء البحث
            int modelRow = casesTable.convertRowIndexToModel(selectedRow);
            int caseId = (int) casesTable.getModel().getValueAt(modelRow, 0);
            String currentName = (String) casesTable.getModel().getValueAt(modelRow, 1);
            String currentDesc = (String) casesTable.getModel().getValueAt(modelRow, 2);
            String currentStatus = (String) casesTable.getModel().getValueAt(modelRow, 3);

            Object[] options = {"📝 تعديل بيانات القضية", "📂 إدارة الأدلة", "إلغاء"};
            int choice = JOptionPane.showOptionDialog(this, 
                    "إدارة القضية: " + currentName, "خيارات القضية رقم " + caseId, 
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (choice == 0) {
                JTextField nameField = new JTextField(currentName);
                JTextField descField = new JTextField(currentDesc);
                JComboBox<String> statusCombo = new JComboBox<>(new String[]{"مفتوحة", "مغلقة", "قيد التحقيق"});
                statusCombo.setSelectedItem(currentStatus);

                Object[] inputFields = {"اسم القضية:", nameField, "وصف القضية:", descField, "الحالة:", statusCombo};
                int updateChoice = JOptionPane.showConfirmDialog(this, inputFields, "تحديث القضية", JOptionPane.OK_CANCEL_OPTION);
                
                if (updateChoice == JOptionPane.OK_OPTION) {
                    String url = "jdbc:sqlite:investigation.db";
                    String sql = "UPDATE cases SET name=?, description=?, status=? WHERE id=?";
                    try (Connection conn = DriverManager.getConnection(url);
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, nameField.getText());
                        pstmt.setString(2, descField.getText());
                        pstmt.setString(3, statusCombo.getSelectedItem().toString());
                        pstmt.setInt(4, caseId);
                        pstmt.executeUpdate();
                        refreshTableData(casesTable);
                        JOptionPane.showMessageDialog(this, "تم التحديث بنجاح!");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "خطأ: " + ex.getMessage());
                    }
                }
            } else if (choice == 1) {
                openEvidencesManager(caseId, currentName);
            }
        });

        btnDelete.addActionListener(e -> {
            sideMenu.setVisible(false);
            int selectedRow = casesTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "يرجى تحديد قضية من الجدول لحذفها!", "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int modelRow = casesTable.convertRowIndexToModel(selectedRow);
            int caseId = (int) casesTable.getModel().getValueAt(modelRow, 0);
            String caseName = (String) casesTable.getModel().getValueAt(modelRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "هل أنت متأكد من حذف القضية: (" + caseName + ") بالكامل مع جميع أدلتها؟\nهذا الإجراء لا يمكن التراجع عنه!",
                    "تأكيد الحذف الأمني", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                String url = "jdbc:sqlite:investigation.db";
                try (Connection conn = DriverManager.getConnection(url);
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM evidences WHERE case_id = " + caseId);
                    stmt.executeUpdate("DELETE FROM cases WHERE id = " + caseId);
                    refreshTableData(casesTable); 
                    JOptionPane.showMessageDialog(this, "تم محو القضية وسجلاتها بنجاح!");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "خطأ أثناء الحذف: " + ex.getMessage());
                }
            }
        });

        sideMenu.add(btnRefresh);
        sideMenu.add(btnAdd);
        sideMenu.add(btnEdit);
        sideMenu.add(btnDelete);

        btnToggle.addActionListener(e -> {
            sideMenu.setVisible(!sideMenu.isVisible());
            revalidate(); 
        });

        add(topPanel, BorderLayout.NORTH);
        add(sideMenu, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
        
        initializeDatabase(); 
        refreshTableData(casesTable);
        
        setVisible(true);
    }


    // الدوال المساعدة الأساسية
   

    private JButton createMenuButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btn.setHorizontalAlignment(SwingConstants.LEFT); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void initializeDatabase() {
        String url = "jdbc:sqlite:investigation.db";
        String sqlCases = "CREATE TABLE IF NOT EXISTS cases (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, description TEXT, status TEXT);";
        String sqlEvidences = "CREATE TABLE IF NOT EXISTS evidences (id INTEGER PRIMARY KEY AUTOINCREMENT, case_id INTEGER, evidence_text TEXT, FOREIGN KEY(case_id) REFERENCES cases(id));";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlCases);
            stmt.execute(sqlEvidences);
        } catch (SQLException e) { }
    }

    private void refreshTableData(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); 
        String url = "jdbc:sqlite:investigation.db"; 
        String sql = "SELECT * FROM cases"; 
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getString("status")});
            }
        } catch (SQLException e) { }
    }

    private void openEvidencesManager(int caseId, String caseName) {
        JDialog evidenceDialog = new JDialog(this, "أدلة القضية: " + caseName, true);
        evidenceDialog.setSize(500, 400);
        evidenceDialog.setLocationRelativeTo(this);
        evidenceDialog.setLayout(new BorderLayout(10, 10));
        evidenceDialog.getContentPane().setBackground(new Color(30, 31, 34));

        DefaultTableModel evModel = new DefaultTableModel(new String[]{"رقم الدليل", "تفاصيل الدليل"}, 0);
        JTable evTable = new JTable(evModel);
        evidenceDialog.add(new JScrollPane(evTable), BorderLayout.CENTER);

        // قائمة سرية لحفظ أرقام قاعدة البيانات الحقيقية
        List<Integer> realEvidenceIds = new ArrayList<>();

        Runnable loadEvidences = () -> {
            evModel.setRowCount(0);
            realEvidenceIds.clear();
            int localCounter = 1; // العداد الذي سيظهر للمستخدم ويبدأ من 1 لكل قضية

            String url = "jdbc:sqlite:investigation.db";
            String sql = "SELECT id, evidence_text FROM evidences WHERE case_id = " + caseId;
            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    realEvidenceIds.add(rs.getInt("id")); 
                    evModel.addRow(new Object[]{localCounter, rs.getString("evidence_text")});
                    localCounter++;
                }
            } catch (SQLException ex) { }
        };
        loadEvidences.run(); 

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnAddEv = new JButton("إضافة دليل");
        JButton btnDelEv = new JButton("حذف الدليل المحدد");
        btnPanel.add(btnAddEv); btnPanel.add(btnDelEv);
        evidenceDialog.add(btnPanel, BorderLayout.SOUTH);

        btnAddEv.addActionListener(e -> {
            String newEv = JOptionPane.showInputDialog(evidenceDialog, "أدخل تفاصيل الدليل:");
            if (newEv != null && !newEv.trim().isEmpty()) {
                String url = "jdbc:sqlite:investigation.db";
                try (Connection conn = DriverManager.getConnection(url);
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO evidences (case_id, evidence_text) VALUES (?, ?)")) {
                    pstmt.setInt(1, caseId);
                    pstmt.setString(2, newEv);
                    pstmt.executeUpdate();
                    loadEvidences.run(); 
                } catch (SQLException ex) { }
            }
        });

        btnDelEv.addActionListener(e -> {
            int row = evTable.getSelectedRow();
            if (row != -1) {
                int realEvId = realEvidenceIds.get(row); 
                String url = "jdbc:sqlite:investigation.db";
                try (Connection conn = DriverManager.getConnection(url);
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM evidences WHERE id = " + realEvId);
                    loadEvidences.run();
                } catch (SQLException ex) { }
            }
        });

        evidenceDialog.setVisible(true);
    }


    
    private static void showLoginScreen() {
        JDialog loginDialog = new JDialog((Frame)null, "بوابة الدخول الأمني", true);
        loginDialog.setSize(380, 230);
        loginDialog.setLocationRelativeTo(null); 
        loginDialog.setLayout(new BorderLayout());
        
       
        Color bgColor = new Color(19, 19, 20);
        Color surfaceColor = new Color(30, 31, 34);
        Color textColor = new Color(227, 227, 227);
        loginDialog.getContentPane().setBackground(bgColor);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 20));
        formPanel.setBackground(bgColor);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));

        JLabel userLabel = new JLabel("اسم المستخدم:");
        userLabel.setForeground(textColor);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextField userField = new JTextField();
        userField.setBackground(surfaceColor);
        userField.setForeground(textColor);
        userField.setCaretColor(textColor);
        userField.setBorder(BorderFactory.createLineBorder(surfaceColor, 5));

        JLabel passLabel = new JLabel("كلمة المرور:");
        passLabel.setForeground(textColor);
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JPasswordField passField = new JPasswordField(); // حقل مشفر بالنجوم
        passField.setBackground(surfaceColor);
        passField.setForeground(textColor);
        passField.setCaretColor(textColor);
        passField.setBorder(BorderFactory.createLineBorder(surfaceColor, 5));

        formPanel.add(userLabel);
        formPanel.add(userField);
        formPanel.add(passLabel);
        formPanel.add(passField);

        JButton btnLogin = new JButton("تسجيل الدخول 🔐");
        btnLogin.setBackground(new Color(59, 130, 246));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(bgColor);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        btnPanel.add(btnLogin);

        loginDialog.add(formPanel, BorderLayout.CENTER);
        loginDialog.add(btnPanel, BorderLayout.SOUTH);

       
        loginDialog.getRootPane().setDefaultButton(btnLogin);

        btnLogin.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());
            
            //  بيانات الدخول الافتراضية للنظام 
            if (user.equals("admin") && pass.equals("1234")) {
                loginDialog.dispose(); // إغلاق نافذة 
                new DashboardPreview(); // فتح نظام التحقيقات السري
            } else {
                JOptionPane.showMessageDialog(loginDialog, "بيانات الدخول غير صحيحة، يرجى المحاولة مجدداً!", "وصول مرفوض", JOptionPane.ERROR_MESSAGE);
            }
        });

        // حماية النظام: إذا قام المستخدم بإغلاق نافذة الدخول بعلامة X يغلق البرنامج بالكامل
        loginDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        loginDialog.setVisible(true);
    }

    public static void main(String[] args) {
        showLoginScreen();
    }
}