package com.studentInfo.dao;

import com.studentInfo.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.List;

@Repository
public class StudentDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Create student table
    @Transactional
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS students (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(100) NOT NULL, " +
                "father_name VARCHAR(100) NOT NULL, " +
                "profile_photo VARCHAR(255), " +
                "aadhar_path VARCHAR(255), " +
                "aadhar_number VARCHAR(12) UNIQUE NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP) " +
                "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        jdbcTemplate.execute(sql);
        System.out.println("Students table created successfully!");
    }

    // Add student with auto-generated key
    @Transactional
    public int addStudent(Student student) {
        String sql = "INSERT INTO students (name, father_name, profile_photo, aadhar_path, aadhar_number) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, student.getName());
            ps.setString(2, student.getFatherName());
            ps.setString(3, student.getProfilePhoto());
            ps.setString(4, student.getAadharPath());
            ps.setString(5, student.getAadharNumber());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    // Get all students
    public List<Student> getAllStudents() {
        String sql = "SELECT * FROM students ORDER BY id DESC";
        return jdbcTemplate.query(sql, new StudentRowMapper());
    }

    // Get student by ID
    public Student getStudentById(int id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new StudentRowMapper(), id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Check if Aadhar number exists
    public boolean isAadharExists(String aadharNumber, Integer excludeId) {
        String sql;
        if (excludeId != null) {
            sql = "SELECT COUNT(*) FROM students WHERE aadhar_number = ? AND id != ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, aadharNumber, excludeId);
            return count != null && count > 0;
        } else {
            sql = "SELECT COUNT(*) FROM students WHERE aadhar_number = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, aadharNumber);
            return count != null && count > 0;
        }
    }

    // Update student
    @Transactional
    public int updateStudent(Student student) {
        String sql = "UPDATE students SET name = ?, father_name = ?, " +
                "profile_photo = COALESCE(?, profile_photo), " +
                "aadhar_path = COALESCE(?, aadhar_path), " +
                "aadhar_number = ? WHERE id = ?";

        return jdbcTemplate.update(sql,
                student.getName(),
                student.getFatherName(),
                student.getProfilePhoto(),
                student.getAadharPath(),
                student.getAadharNumber(),
                student.getId());
    }

    // Update student without changing files
    @Transactional
    public int updateStudentWithoutFiles(Student student) {
        String sql = "UPDATE students SET name = ?, father_name = ?, " +
                "aadhar_number = ? WHERE id = ?";

        return jdbcTemplate.update(sql,
                student.getName(),
                student.getFatherName(),
                student.getAadharNumber(),
                student.getId());
    }

    // Delete student
    @Transactional
    public int deleteStudent(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // Search students
    public List<Student> searchStudents(String keyword) {
        String sql = "SELECT * FROM students WHERE " +
                "name LIKE ? OR " +
                "father_name LIKE ? OR " +
                "aadhar_number LIKE ? " +
                "ORDER BY id DESC";
        String searchPattern = "%" + keyword + "%";
        return jdbcTemplate.query(sql, new StudentRowMapper(),
                searchPattern, searchPattern, searchPattern);
    }

    // RowMapper for Student
    private static class StudentRowMapper implements RowMapper<Student> {
        @Override
        public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
            Student student = new Student();
            student.setId(rs.getInt("id"));
            student.setName(rs.getString("name"));
            student.setFatherName(rs.getString("father_name"));
            student.setProfilePhoto(rs.getString("profile_photo"));
            student.setAadharPath(rs.getString("aadhar_path"));
            student.setAadharNumber(rs.getString("aadhar_number"));
            return student;
        }
    }
}