package com.studentInfo.controller;

import com.studentInfo.dao.ContactDao;
import com.studentInfo.entity.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactController {

    @Autowired
    private ContactDao contactDAO;

    // Get contacts by student ID
    @GetMapping("/student/{studentId}")
    @ResponseBody  // Add this annotation for JSON response
    public List<Contact> getContactsByStudent(@PathVariable int studentId) {
        System.out.println("=== GET /contacts/student/" + studentId + " called ===");

        try {
            List<Contact> contacts = contactDAO.getContactsByStudentId(studentId);
            System.out.println("Found " + contacts.size() + " contacts for student ID: " + studentId);

            // Debug each contact
            for (Contact contact : contacts) {
                System.out.println("  Contact ID: " + contact.getId() +
                        ", Mobile: " + contact.getMobNo() +
                        ", City: " + contact.getCity());
            }

            return contacts;
        } catch (Exception e) {
            System.out.println("ERROR getting contacts for student " + studentId + ": " + e.getMessage());
            e.printStackTrace();
            return null; // or return empty list: return new ArrayList<>();
        }
    }

    // Add other contact methods with @ResponseBody
    @PostMapping("/add")
    @ResponseBody
    public String addContact(@RequestBody Contact contact) {
        System.out.println("=== POST /contacts/add called ===");
        System.out.println("Contact data: " + contact);

        try {
            contactDAO.addContact(contact);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Contact getContactById(@PathVariable int id) {
        System.out.println("=== GET /contacts/" + id + " called ===");
        return contactDAO.getContactById(id);
    }

    @PutMapping("/update")
    @ResponseBody
    public String updateContact(@RequestBody Contact contact) {
        System.out.println("=== PUT /contacts/update called ===");
        try {
            contactDAO.updateContact(contact);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteContact(@PathVariable int id) {
        System.out.println("=== DELETE /contacts/" + id + " called ===");
        try {
            contactDAO.deleteContact(id);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}