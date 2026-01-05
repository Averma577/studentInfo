<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    if (contextPath.equals("/")) {
        contextPath = "";
    }
%>
<html>
<head>
    <title>Student Management System</title>
    <!-- FIXED: Use contextPath variable -->
    <link rel="stylesheet" href="<%=contextPath%>/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>
    <div class="container">
        <h1><i class="fas fa-user-graduate"></i> Student Management System</h1>

        <!-- Add Search Bar -->
        <div style="display: flex; gap: 10px; margin-bottom: 20px;">
            <button class="btn btn-primary" onclick="showStudentModal('add')">
                <i class="fas fa-plus"></i> Add Student
            </button>
            <input type="text" id="searchInput" placeholder="Search students..."
                   style="flex: 1; padding: 8px; border: 1px solid #ddd; border-radius: 5px;">
            <button class="btn btn-secondary" onclick="searchStudents()">
                <i class="fas fa-search"></i> Search
            </button>
            <button class="btn btn-secondary" onclick="clearSearch()">
                <i class="fas fa-times"></i> Clear
            </button>
        </div>

        <div class="table-container">
            <table id="studentTable">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Father Name</th>
                        <th>Aadhar Number</th>
                        <th>Profile Photo</th>
                        <th>Aadhar</th>
                        <th>Contacts</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody id="studentTableBody">
                    <!-- Students will be loaded here dynamically -->
                </tbody>
            </table>
        </div>
    </div>

    <!-- Student Modal -->
    <div id="studentModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeStudentModal()">&times;</span>
            <h2 id="modalTitle">Add Student</h2>
            <form id="studentForm" enctype="multipart/form-data">
                <input type="hidden" id="studentId">

                <div class="form-group">
                    <label for="name">Name:</label>
                    <input type="text" id="name" name="name" required>
                </div>

                <div class="form-group">
                    <label for="fatherName">Father Name:</label>
                    <input type="text" id="fatherName" name="fatherName" required>
                </div>

                <div class="form-group">
                    <label for="aadharNumber">Aadhar Number:</label>
                    <input type="text" id="aadharNumber" name="aadharNumber" required
                           pattern="\d{12}" title="12 digit Aadhar number">
                </div>

                <div class="form-group">
                    <label for="profilePhoto">Profile Photo:</label>
                    <input type="file" id="profilePhoto" name="profilePhotoFile" accept="image/*">
                    <small>Max size: 5MB</small>
                </div>

                <div class="form-group">
                    <label for="aadharFile">Upload Aadhar:</label>
                    <input type="file" id="aadharFile" name="aadharFile" accept="image/*,.pdf">
                    <small>Max size: 5MB</small>
                </div>

                <div class="form-buttons">
                    <button type="button" class="btn btn-secondary" onclick="closeStudentModal()">
                        Cancel
                    </button>
                    <button type="submit" class="btn btn-primary">
                        Save
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- Contact Modal -->
  <!-- Contact Modal -->
  <div id="contactModal" class="modal">
      <div class="modal-content">
          <span class="close" onclick="closeContactModal()">&times;</span>
          <h2><i class="fas fa-address-book"></i> Manage Contacts</h2>
          <p style="color: #666; margin-bottom: 20px;">Student ID: <span id="currentStudentId"></span></p>

          <div id="contactFormContainer">
              <!-- Contact form will be loaded here -->
          </div>

          <div id="contactsList" style="margin: 20px 0; max-height: 300px; overflow-y: auto;">
              <!-- Contacts will be listed here -->
          </div>

          <div style="text-align: center; margin-top: 20px;">
              <button class="btn btn-primary" onclick="showAddContactForm()">
                  <i class="fas fa-plus"></i> Add New Contact
              </button>
              <button class="btn btn-secondary" onclick="closeContactModal()" style="margin-left: 10px;">
                  <i class="fas fa-times"></i> Close
              </button>
          </div>
      </div>
  </div>
    <!-- FIXED: Define CONTEXT_PATH for JavaScript -->
    <script>
        // Define CONTEXT_PATH for JavaScript
        const CONTEXT_PATH = '<%=contextPath%>';
        console.log('Application Context Path:', CONTEXT_PATH);

        // Debug information
        console.log('=== DEBUG INFORMATION ===');
        console.log('JSP Context Path: <%=contextPath%>');
        console.log('Window location:', window.location.href);
        console.log('Pathname:', window.location.pathname);
    </script>

    <!-- FIXED: Use contextPath for script reference -->
    <script src="<%=contextPath%>/script.js"></script>

    <!-- Initialize when page loads -->
    <script>
        window.addEventListener('load', function() {
            console.log('Page fully loaded');
            console.log('CONTEXT_PATH value:', CONTEXT_PATH);
            console.log('loadStudents function exists?', typeof loadStudents === 'function');

            // Test: Click Add Student button to see if it works
            const addBtn = document.querySelector('.btn-primary');
            if (addBtn && typeof showStudentModal === 'function') {
                console.log('JavaScript functions are available');
            }
        });
    </script>
</body>
</html>