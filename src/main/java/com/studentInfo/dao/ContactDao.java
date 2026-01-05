package com.studentInfo.dao;

import com.studentInfo.entity.Contact;
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
public class ContactDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Create contact table
    @Transactional
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS contacts (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "mob_no VARCHAR(10) NOT NULL, " +
                "city VARCHAR(50) NOT NULL, " +
                "address VARCHAR(255) NOT NULL, " +
                "student_id INT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE, " +
                "INDEX idx_student_id (student_id)) " +
                "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        jdbcTemplate.execute(sql);
        System.out.println("Contacts table created successfully!");
    }

    // Add contact with auto-generated key
    @Transactional
    public int addContact(Contact contact) {
        String sql = "INSERT INTO contacts (mob_no, city, address, student_id) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, contact.getMobNo());
            ps.setString(2, contact.getCity());
            ps.setString(3, contact.getAddress());
            ps.setInt(4, contact.getStudentId());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    // Get contacts by student ID
    public List<Contact> getContactsByStudentId(int studentId) {
        String sql = "SELECT * FROM contacts WHERE student_id = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, new ContactRowMapper(), studentId);
    }

    // Get contact by ID
    public Contact getContactById(int id) {
        String sql = "SELECT * FROM contacts WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new ContactRowMapper(), id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Update contact
    @Transactional
    public int updateContact(Contact contact) {
        String sql = "UPDATE contacts SET mob_no = ?, city = ?, address = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                contact.getMobNo(),
                contact.getCity(),
                contact.getAddress(),
                contact.getId());
    }

    // Delete contact
    @Transactional
    public int deleteContact(int id) {
        String sql = "DELETE FROM contacts WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // Delete all contacts for a student
    @Transactional
    public int deleteContactsByStudentId(int studentId) {
        String sql = "DELETE FROM contacts WHERE student_id = ?";
        return jdbcTemplate.update(sql, studentId);
    }

    // Check if mobile number exists for student
    public boolean isMobileExistsForStudent(String mobNo, int studentId, Integer excludeId) {
        String sql;
        if (excludeId != null) {
            sql = "SELECT COUNT(*) FROM contacts WHERE mob_no = ? AND student_id = ? AND id != ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, mobNo, studentId, excludeId);
            return count != null && count > 0;
        } else {
            sql = "SELECT COUNT(*) FROM contacts WHERE mob_no = ? AND student_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, mobNo, studentId);
            return count != null && count > 0;
        }
    }

    // RowMapper for Contact
    private static class ContactRowMapper implements RowMapper<Contact> {
        @Override
        public Contact mapRow(ResultSet rs, int rowNum) throws SQLException {
            Contact contact = new Contact();
            contact.setId(rs.getInt("id"));
            contact.setMobNo(rs.getString("mob_no"));
            contact.setCity(rs.getString("city"));
            contact.setAddress(rs.getString("address"));
            contact.setStudentId(rs.getInt("student_id"));
            contact.setCreatedAt(rs.getTimestamp("created_at")); // MISSING FIELD
            return contact;
        }
    }
}