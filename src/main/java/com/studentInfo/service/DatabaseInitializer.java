package com.studentInfo.service;

import com.studentInfo.dao.ContactDao;
import com.studentInfo.dao.StudentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseInitializer {

    @Autowired
    private StudentDao studentDAO;

    @Autowired
    private ContactDao contactDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        try {
            // Test database connection
            try (Connection conn = dataSource.getConnection()) {
                System.out.println("✅ Database connection successful!");
                System.out.println("Database: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("Version: " + conn.getMetaData().getDatabaseProductVersion());
            }

            // Create database if not exists
            jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS studentdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            jdbcTemplate.execute("USE studentdb");

            // Create tables
            studentDAO.createTable();
            contactDAO.createTable();

            // Insert sample data (optional)
            insertSampleData();

            System.out.println("✅ Database tables created successfully!");

        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertSampleData() {
        try {
            // Check if students table is empty
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Integer.class);

            if (count == 0) {
                // Insert sample students
                String insertStudentSQL = "INSERT INTO students (name, father_name, aadhar_number) VALUES (?, ?, ?)";
                jdbcTemplate.update(insertStudentSQL, "John Doe", "Robert Doe", "123456789012");
                jdbcTemplate.update(insertStudentSQL, "Jane Smith", "William Smith", "987654321098");

                System.out.println("✅ Sample student data inserted!");
            }
        } catch (Exception e) {
            System.out.println("Note: Sample data not inserted (table might already have data)");
        }
    }
}