package com.studentInfo.controller;

import com.studentInfo.dao.StudentDao;
import com.studentInfo.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class StudentController {

    @Autowired
    private StudentDao studentDAO;

    @Autowired
    private HttpServletRequest request;

    // Use absolute path instead of relative
    private String getUploadDir() {
        // Use the EXACT SAME PATH as configured in AppConfig
        String uploadPath = "C:/Users/hp/.SmartTomcat/StudentInfo/StudentInfo/uploads/";

        System.out.println("Controller upload directory: " + uploadPath);

        // Create directory if it doesn't exist
        File uploadFolder = new File(uploadPath);
        if (!uploadFolder.exists()) {
            boolean created = uploadFolder.mkdirs();
            System.out.println("Controller created upload directory: " + created);
        }

        // List existing files for debugging
        File[] files = uploadFolder.listFiles();
        System.out.println("Controller found " + (files != null ? files.length : 0) + " files");

        return uploadPath;
    }

    // 1. Handle root path
    @GetMapping("/")
    public String showHomePage(Model model) {
        System.out.println("=== Root URL '/' accessed ===");
        try {
            List<Student> students = studentDAO.getAllStudents();
            model.addAttribute("students", students);
            System.out.println("Loaded " + students.size() + " students");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading students");
            System.out.println("Error loading students: " + e.getMessage());
        }
        return "index";
    }

    // 2. For /students URL
    @GetMapping("/students")
    public String showStudentsPage(Model model) {
        System.out.println("=== /students URL accessed ===");
        return showHomePage(model);
    }

    // 3. Add Student - FIXED: Use @RequestParam instead of @ModelAttribute for file uploads
    @PostMapping("/students/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addStudent(
            @RequestParam("name") String name,
            @RequestParam("fatherName") String fatherName,
            @RequestParam("aadharNumber") String aadharNumber,
            @RequestParam(value = "profilePhotoFile", required = false) MultipartFile profilePhotoFile,
            @RequestParam(value = "aadharFile", required = false) MultipartFile aadharFile) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate Aadhar number
            if (studentDAO.isAadharExists(aadharNumber, null)) {
                response.put("success", false);
                response.put("message", "Aadhar number already exists!");
                System.out.println("Aadhar already exists: " + aadharNumber);
                return ResponseEntity.badRequest().body(response);
            }

            Student student = new Student();
            student.setName(name);
            student.setFatherName(fatherName);
            student.setAadharNumber(aadharNumber);

            // Handle profile photo upload
            if (profilePhotoFile != null && !profilePhotoFile.isEmpty()) {
                String profilePhotoName = saveFile(profilePhotoFile);
                student.setProfilePhoto(profilePhotoName);
                System.out.println("Profile photo saved as: " + profilePhotoName);
            }

            // Handle Aadhar upload
            if (aadharFile != null && !aadharFile.isEmpty()) {
                String aadharFileName = saveFile(aadharFile);
                student.setAadharPath(aadharFileName);
                System.out.println("Aadhar file saved as: " + aadharFileName);
            }

            int studentId = studentDAO.addStudent(student);
            System.out.println("Student added with ID: " + studentId);

            response.put("success", true);
            response.put("message", "Student added successfully!");
            response.put("studentId", studentId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error adding student: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error adding student: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 4. Get all students (for AJAX)
    @GetMapping("/students/list")
    @ResponseBody
    public ResponseEntity<List<Student>> getAllStudents() {
        System.out.println("=== Getting all students (AJAX) ===");
        try {
            List<Student> students = studentDAO.getAllStudents();
            System.out.println("Returning " + students.size() + " students");
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error getting students: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 5. Get student by ID
    @GetMapping("/students/{id}")
    @ResponseBody
    public ResponseEntity<Student> getStudentById(@PathVariable int id) {
        System.out.println("=== Getting student by ID: " + id + " ===");
        try {
            Student student = studentDAO.getStudentById(id);
            if (student != null) {
                System.out.println("Found student: " + student.getName());
                return ResponseEntity.ok(student);
            } else {
                System.out.println("Student not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error getting student: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 6. Update student - FIXED: Use @RequestParam
    @PostMapping("/students/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStudent(
            @RequestParam("id") int id,
            @RequestParam("name") String name,
            @RequestParam("fatherName") String fatherName,
            @RequestParam("aadharNumber") String aadharNumber,
            @RequestParam(value = "profilePhotoFile", required = false) MultipartFile profilePhotoFile,
            @RequestParam(value = "aadharFile", required = false) MultipartFile aadharFile) {

        Map<String, Object> response = new HashMap<>();

        System.out.println("=== Updating student ID: " + id + " ===");

        try {
            // Validate Aadhar number (exclude current student)
            if (studentDAO.isAadharExists(aadharNumber, id)) {
                response.put("success", false);
                response.put("message", "Aadhar number already exists!");
                System.out.println("Aadhar already exists for another student: " + aadharNumber);
                return ResponseEntity.badRequest().body(response);
            }

            Student student = new Student();
            student.setId(id);
            student.setName(name);
            student.setFatherName(fatherName);
            student.setAadharNumber(aadharNumber);

            boolean filesUpdated = false;

            // Handle profile photo upload
            if (profilePhotoFile != null && !profilePhotoFile.isEmpty()) {
                String profilePhotoName = saveFile(profilePhotoFile);
                student.setProfilePhoto(profilePhotoName);
                filesUpdated = true;
                System.out.println("Updated profile photo: " + profilePhotoName);
            }

            // Handle Aadhar upload
            if (aadharFile != null && !aadharFile.isEmpty()) {
                String aadharFileName = saveFile(aadharFile);
                student.setAadharPath(aadharFileName);
                filesUpdated = true;
                System.out.println("Updated Aadhar file: " + aadharFileName);
            }

            int result;
            if (filesUpdated) {
                result = studentDAO.updateStudent(student);
            } else {
                result = studentDAO.updateStudentWithoutFiles(student);
            }

            System.out.println("Update result: " + result + " rows affected");

            if (result > 0) {
                response.put("success", true);
                response.put("message", "Student updated successfully!");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Student not found!");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error updating student: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error updating student: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 7. Delete student
    @DeleteMapping("/students/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteStudent(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Delete associated files
            Student student = studentDAO.getStudentById(id);
            if (student != null) {
                deleteFile(student.getProfilePhoto());
                deleteFile(student.getAadharPath());
                System.out.println("Deleted files for student ID: " + id);
            }

            int result = studentDAO.deleteStudent(id);
            if (result > 0) {
                response.put("success", true);
                response.put("message", "Student deleted successfully!");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Student not found!");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error deleting student: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error deleting student: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 8. Search students
    @GetMapping("/students/search")
    @ResponseBody
    public ResponseEntity<List<Student>> searchStudents(@RequestParam String keyword) {
        System.out.println("=== Searching students with keyword: " + keyword + " ===");
        try {
            List<Student> students = studentDAO.searchStudents(keyword);
            System.out.println("Found " + students.size() + " students matching '" + keyword + "'");
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error searching students: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 9. Debug endpoint to check uploads
    @GetMapping("/debug/uploads")
    @ResponseBody
    public String debugUploads() {
        String uploadDir = getUploadDir();
        File dir = new File(uploadDir);

        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Upload Directory Debug</h2>");
        sb.append("<p>Upload Directory: ").append(uploadDir).append("</p>");
        sb.append("<p>Directory exists: ").append(dir.exists()).append("</p>");
        sb.append("<p>Is directory: ").append(dir.isDirectory()).append("</p>");

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            sb.append("<p>Number of files: ").append(files != null ? files.length : 0).append("</p>");
            sb.append("<ul>");
            if (files != null) {
                for (File file : files) {
                    sb.append("<li>")
                            .append(file.getName())
                            .append(" (").append(file.length()).append(" bytes)")
                            .append(" - <a href='/StudentInfo/uploads/").append(file.getName())
                            .append("' target='_blank'>View</a>")
                            .append("</li>");
                }
            }
            sb.append("</ul>");
        }

        return sb.toString();
    }
    @GetMapping("/check/file/{filename}")
    @ResponseBody
    public String checkFile(@PathVariable String filename) {
        String uploadDir = getUploadDir();
        File file = new File(uploadDir + filename);

        StringBuilder response = new StringBuilder();
        response.append("<h2>File Check for: ").append(filename).append("</h2>");
        response.append("<p>Looking in: ").append(uploadDir).append("</p>");
        response.append("<p>Full path: ").append(file.getAbsolutePath()).append("</p>");
        response.append("<p>File exists: ").append(file.exists()).append("</p>");

        if (file.exists()) {
            response.append("<p>File size: ").append(file.length()).append(" bytes</p>");
            response.append("<p>Can read: ").append(file.canRead()).append("</p>");
            response.append("<p>Can write: ").append(file.canWrite()).append("</p>");
            response.append("<a href='/StudentInfo/uploads/").append(filename).append("' target='_blank'>Try to access via Spring</a><br>");
            response.append("<a href='/test/image/").append(filename).append("' target='_blank'>Try to access via Controller</a>");
        } else {
            response.append("<p>ERROR: File not found!</p>");
            response.append("<p>Checking alternative locations:</p>");

            // Check other possible locations
            String[] locations = {
                    System.getProperty("user.dir") + "/uploads/",
                    System.getProperty("user.dir") + "/StudentInfo/uploads/",
                    "uploads/",
                    "./uploads/"
            };

            for (String location : locations) {
                File altFile = new File(location + filename);
                response.append("<p>").append(location).append(": ").append(altFile.exists()).append("</p>");
            }
        }

        return response.toString();
    }

    // Helper methods
    private String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            System.out.println("File is null or empty, skipping save");
            return null;
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        String uploadDir = getUploadDir();

        File destination = new File(uploadDir + uniqueFileName);

        // Debug info
        System.out.println("Saving file:");
        System.out.println("  Original: " + originalFileName);
        System.out.println("  Saved as: " + uniqueFileName);
        System.out.println("  Path: " + destination.getAbsolutePath());
        System.out.println("  Size: " + file.getSize() + " bytes");

        // Transfer file
        file.transferTo(destination);

        // Verify
        if (destination.exists()) {
            long savedSize = destination.length();
            System.out.println("  File saved successfully!");
            System.out.println("  Saved size: " + savedSize + " bytes");

            if (savedSize != file.getSize()) {
                System.out.println("  WARNING: Size mismatch! Original: " + file.getSize() + ", Saved: " + savedSize);
            }
        } else {
            System.out.println("  ERROR: File was not saved!");
        }

        return uniqueFileName;
    }

    private void deleteFile(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            String uploadDir = getUploadDir();
            File file = new File(uploadDir + fileName);
            if (file.exists()) {
                boolean deleted = file.delete();
                System.out.println("Deleted file " + fileName + ": " + (deleted ? "success" : "failed"));
            } else {
                System.out.println("File not found for deletion: " + fileName);
            }
        }
    }
}