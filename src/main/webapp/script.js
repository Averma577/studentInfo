// Global variables
let currentStudentId = null;
let currentContactId = null;

// Check if CONTEXT_PATH is defined
if (typeof CONTEXT_PATH === 'undefined') {
    window.CONTEXT_PATH = '';
    console.warn('CONTEXT_PATH not defined in HTML, using empty string');
} else {
    console.log('CONTEXT_PATH found:', CONTEXT_PATH);
}

// Get base URL dynamically
function getBaseUrl() {
    return CONTEXT_PATH || '';
}

// DOM Ready - Enhanced with more debugging
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM Content Loaded');
    console.log('Base URL for API calls:', getBaseUrl());

    // Test all buttons exist
    const addBtn = document.getElementById('searchInput');
    console.log('Search input exists?', !!addBtn);

    // Initialize the page
    initializePage();
});

function initializePage() {
    console.log('Initializing page...');

    // Load students initially
    loadStudents();

    // Student form submit
    const studentForm = document.getElementById('studentForm');
    if (studentForm) {
        console.log('Found student form, adding submit listener');
        studentForm.addEventListener('submit', function(e) {
            e.preventDefault();
            console.log('Student form submitted');
            saveStudent();
        });
    } else {
        console.error('Student form NOT FOUND!');
    }

    // Search input event
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keyup', function(e) {
            if (e.key === 'Enter') {
                console.log('Enter pressed in search');
                searchStudents();
            }
        });
    }

    // Test button clicks
    document.addEventListener('click', function(e) {
        if (e.target.closest('.btn-primary') && e.target.closest('.btn-primary').textContent.includes('Add Student')) {
            console.log('Add Student button clicked manually');
        }
    });
}

// Student Functions
function loadStudents() {
    console.log('Loading students from:', getBaseUrl() + '/students/list');

    // Show loading message
    const tbody = document.getElementById('studentTableBody');
    if (tbody) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center"><i class="fas fa-spinner fa-spin"></i> Loading students...</td></tr>';
    }

    fetch(getBaseUrl() + '/students/list')
        .then(response => {
            console.log('Response status:', response.status, 'OK?', response.ok);
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(students => {
            console.log('Students loaded:', students);
            const tbody = document.getElementById('studentTableBody');
            if (!tbody) {
                console.error('studentTableBody not found!');
                return;
            }

            tbody.innerHTML = '';

            if (!students || students.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="8" class="text-center">No students found. Click "Add Student" to create one.</td>
                    </tr>
                `;
                return;
            }

            students.forEach(student => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${student.id || 'N/A'}</td>
                    <td>${student.name || 'N/A'}</td>
                    <td>${student.fatherName || 'N/A'}</td>
                    <td>${student.aadharNumber || 'N/A'}</td>
                    <td>
                        ${student.profilePhoto ?
                            `<a href="${getBaseUrl()}/uploads/${student.profilePhoto}" target="_blank">
                                <img src="${getBaseUrl()}/uploads/${student.profilePhoto}" class="file-preview" alt="Profile">
                             </a>` : 'No Photo'}
                    </td>
                    <td>
                        ${student.aadharPath ?
                            `<a href="${getBaseUrl()}/uploads/${student.aadharPath}" target="_blank">
                                <i class="fas fa-file"></i> View Aadhar
                             </a>` : 'No File'}
                    </td>
                    <td>
                        <button class="btn btn-sm btn-warning" onclick="manageContacts(${student.id})">
                            <i class="fas fa-address-book"></i> Contacts
                        </button>
                    </td>
                    <td class="action-buttons">
                        <button class="btn btn-sm btn-warning" onclick="editStudent(${student.id})">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="deleteStudent(${student.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                `;
                tbody.appendChild(row);
            });

            console.log(`Loaded ${students.length} students`);
        })
        .catch(error => {
            console.error('Error loading students:', error);
            showNotification('Error loading students. Check console for details.', 'error');

            const tbody = document.getElementById('studentTableBody');
            if (tbody) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="8" class="text-center error">
                            <i class="fas fa-exclamation-triangle"></i>
                            Error loading students: ${error.message}
                            <br>
                            <small>Make sure your backend server is running</small>
                        </td>
                    </tr>
                `;
            }
        });
}

function showStudentModal(mode) {
    console.log('showStudentModal called with mode:', mode);

    const modal = document.getElementById('studentModal');
    const title = document.getElementById('modalTitle');
    const form = document.getElementById('studentForm');

    if (!modal || !title || !form) {
        console.error('Modal elements not found!');
        showNotification('Error: Modal not found', 'error');
        return;
    }

    if (mode === 'add') {
        title.textContent = 'Add Student';
        form.reset();
        document.getElementById('studentId').value = '';

        // Clear file inputs
        document.getElementById('profilePhoto').value = '';
        document.getElementById('aadharFile').value = '';
    }

    modal.style.display = 'block';
    console.log('Modal displayed');
}

function closeStudentModal() {
    const modal = document.getElementById('studentModal');
    const form = document.getElementById('studentForm');
    if (modal) modal.style.display = 'none';
    if (form) form.reset();
    console.log('Modal closed');
}

function saveStudent() {
    console.log('saveStudent called');

    const form = document.getElementById('studentForm');
    if (!form) {
        console.error('Student form not found!');
        return;
    }

    // Create FormData from the form
    const formData = new FormData(form);
    const studentId = document.getElementById('studentId').value;

    let url = getBaseUrl() + '/students/add';
    let method = 'POST';

    if (studentId) {
        url = getBaseUrl() + '/students/update';
        method = 'POST';
        formData.append('id', studentId);
    }

    console.log('Saving student to:', url, 'Method:', method);
    console.log('FormData entries:');
    for (let pair of formData.entries()) {
        console.log(pair[0] + ': ' + (pair[0].includes('File') ? '[FILE]' : pair[1]));
    }

    // Show loading
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
    submitBtn.disabled = true;

    fetch(url, {
        method: method,
        body: formData
    })
    .then(response => {
        console.log('Save response status:', response.status);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
    })
    .then(result => {
        console.log('Save result:', result);
        if (result && result.success) {
            showNotification(result.message || 'Student saved successfully!', 'success');
            closeStudentModal();
            loadStudents();
        } else {
            showNotification(result?.message || 'Error saving student!', 'error');
        }
    })
    .catch(error => {
        console.error('Error saving student:', error);
        showNotification('Error saving student! Check console for details.', 'error');
    })
    .finally(() => {
        // Restore button
        if (submitBtn) {
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }
    });
}

function editStudent(id) {
    console.log('Editing student ID:', id);
    fetch(getBaseUrl() + `/students/${id}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(student => {
            console.log('Student data loaded:', student);
            document.getElementById('studentId').value = student.id;
            document.getElementById('name').value = student.name || '';
            document.getElementById('fatherName').value = student.fatherName || '';
            document.getElementById('aadharNumber').value = student.aadharNumber || '';

            document.getElementById('modalTitle').textContent = 'Edit Student';

            const modal = document.getElementById('studentModal');
            if (modal) modal.style.display = 'block';
        })
        .catch(error => {
            console.error('Error loading student:', error);
            showNotification('Error loading student details', 'error');
        });
}

function deleteStudent(id) {
    if (confirm('Are you sure you want to delete this student and all associated contacts?')) {
        console.log('Deleting student ID:', id);
        fetch(getBaseUrl() + `/students/${id}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(result => {
            console.log('Delete result:', result);
            if (result && result.success) {
                showNotification(result.message || 'Student deleted successfully!', 'success');
                loadStudents();
            } else {
                showNotification(result?.message || 'Error deleting student!', 'error');
            }
        })
        .catch(error => {
            console.error('Error deleting student:', error);
            showNotification('Error deleting student!', 'error');
        });
    }
}

// Search Functions
function searchStudents() {
    const searchInput = document.getElementById('searchInput');
    if (!searchInput) return;

    const keyword = searchInput.value.trim();
    console.log('Searching for:', keyword);

    if (keyword === '') {
        loadStudents();
        return;
    }

    // Show loading
    const tbody = document.getElementById('studentTableBody');
    if (tbody) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center"><i class="fas fa-spinner fa-spin"></i> Searching...</td></tr>';
    }

    fetch(getBaseUrl() + `/students/search?keyword=${encodeURIComponent(keyword)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(students => {
            console.log('Search results:', students);
            const tbody = document.getElementById('studentTableBody');
            if (!tbody) return;

            tbody.innerHTML = '';

            if (!students || students.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="8" class="text-center">
                            No students found matching "${keyword}"
                        </td>
                    </tr>
                `;
                return;
            }

            students.forEach(student => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${student.id}</td>
                    <td>${student.name}</td>
                    <td>${student.fatherName}</td>
                    <td>${student.aadharNumber || 'N/A'}</td>
                    <td>
                        ${student.profilePhoto ?
                            `<a href="${getBaseUrl()}/uploads/${student.profilePhoto}" target="_blank">
                                <img src="${getBaseUrl()}/uploads/${student.profilePhoto}" class="file-preview">
                             </a>` : 'No Photo'}
                    </td>
                    <td>
                        ${student.aadharPath ?
                            `<a href="${getBaseUrl()}/uploads/${student.aadharPath}" target="_blank">
                                View Aadhar
                             </a>` : 'No File'}
                    </td>
                    <td>
                        <button class="btn btn-sm btn-warning" onclick="manageContacts(${student.id})">
                            <i class="fas fa-address-book"></i> Contacts
                        </button>
                    </td>
                    <td class="action-buttons">
                        <button class="btn btn-sm btn-warning" onclick="editStudent(${student.id})">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="deleteStudent(${student.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        })
        .catch(error => {
            console.error('Error searching:', error);
            showNotification('Error searching students', 'error');
        });
}

function clearSearch() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.value = '';
        loadStudents();
    }
}

// Contact Functions
function manageContacts(studentId) {
    console.log('Managing contacts for student:', studentId);
    currentStudentId = studentId;
    const modal = document.getElementById('contactModal');
    if (modal) {
        modal.style.display = 'block';
        loadContacts(studentId);
    }
}

function closeContactModal() {
    const modal = document.getElementById('contactModal');
    if (modal) modal.style.display = 'none';
    currentStudentId = null;
    currentContactId = null;
}

function loadContacts(studentId) {
    console.log('DEBUG: Loading contacts for student ID:', studentId);
    console.log('DEBUG: Fetch URL:', getBaseUrl() + `/contacts/student/${studentId}`);

    fetch(getBaseUrl() + `/contacts/student/${studentId}`)
        .then(response => {
            console.log('DEBUG: Response status:', response.status);

            if (!response.ok) {
                console.error('DEBUG: HTTP error:', response.statusText);
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            return response.json();
        })
        .then(contacts => {
            console.log('DEBUG: Contacts received:', contacts);

            const container = document.getElementById('contactsList');
            if (!container) {
                console.error('DEBUG: contactsList element not found!');
                return;
            }

            container.innerHTML = '';

            if (!contacts || contacts.length === 0) {
                container.innerHTML = `
                    <div style="text-align: center; padding: 20px; color: #6c757d;">
                        <i class="fas fa-address-book fa-2x" style="margin-bottom: 10px;"></i><br>
                        No contacts found for this student.
                    </div>
                `;
                return;
            }

            contacts.forEach(contact => {
                const contactDiv = document.createElement('div');
                contactDiv.className = 'contact-item';
                contactDiv.innerHTML = `
                    <div class="contact-item-header">
                        <h4 style="margin: 0; display: flex; align-items: center; gap: 10px;">
                            <i class="fas fa-phone" style="color: #28a745;"></i>
                            ${contact.mobNo || 'No Mobile'}
                        </h4>
                        <div class="contact-item-actions">
                            <button class="btn btn-sm btn-warning" onclick="editContact(${contact.id})" title="Edit Contact">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteContact(${contact.id})" title="Delete Contact">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>
                    <div style="margin-top: 10px;">
                        <p><strong><i class="fas fa-city"></i> City:</strong> ${contact.city || 'N/A'}</p>
                        <p><strong><i class="fas fa-map-marker-alt"></i> Address:</strong> ${contact.address || 'N/A'}</p>
                    </div>
                `;
                container.appendChild(contactDiv);
            });

            // Hide add contact form if showing
            const formContainer = document.getElementById('contactFormContainer');
            if (formContainer) {
                formContainer.innerHTML = '';
            }

        })
        .catch(error => {

            const container = document.getElementById('contactsList');
            if (container) {
                container.innerHTML = `
                    <div style="text-align: center; padding: 20px; color: #dc3545;">
                        <i class="fas fa-exclamation-triangle fa-2x" style="margin-bottom: 10px;"></i><br>
                        Error loading contacts.<br>
                        <small>${error.message}</small>
                    </div>
                `;
            }
        });
}

function manageContacts(studentId) {
    currentStudentId = studentId;

    // Update the student ID display
    const studentIdSpan = document.getElementById('currentStudentId');
    if (studentIdSpan) {
        studentIdSpan.textContent = studentId;
    }

    const modal = document.getElementById('contactModal');
    if (modal) {
        modal.style.display = 'block';
        loadContacts(studentId);
    }
}

function showAddContactForm() {
    currentContactId = null;
    const container = document.getElementById('contactFormContainer');
    if (!container) return;

    container.innerHTML = `
        <h3><i class="fas fa-plus"></i> Add Contact</h3>
        <form id="contactForm">
            <input type="hidden" id="contactId">
            <div class="form-group">
                <label for="mobile">Mobile Number:</label>
                <input type="text" id="mobile" name="mobNo" required pattern="[0-9]{10}" placeholder="10 digit mobile number">
            </div>
            <div class="form-group">
                <label for="city">City:</label>
                <input type="text" id="city" name="city" required placeholder="Enter city">
            </div>
            <div class="form-group">
                <label for="address">Address:</label>
                <textarea id="address" name="address" rows="3" required placeholder="Enter full address"></textarea>
            </div>
            <div class="form-buttons">
                <button type="button" class="btn btn-secondary" onclick="cancelContactForm()">
                    Cancel
                </button>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save"></i> Save Contact
                </button>
            </div>
        </form>
    `;

    const contactForm = document.getElementById('contactForm');
    if (contactForm) {
        contactForm.addEventListener('submit', function(e) {
            e.preventDefault();
            saveContact();
        });
    }
}

function cancelContactForm() {
    const container = document.getElementById('contactFormContainer');
    if (container) container.innerHTML = '';
}

function saveContact() {
    const mobile = document.getElementById('mobile');
    const city = document.getElementById('city');
    const address = document.getElementById('address');

    if (!mobile || !city || !address) {
        console.error('Contact form elements not found!');
        return;
    }

    const contact = {
        id: currentContactId,
        mobNo: mobile.value,
        city: city.value,
        address: address.value,
        studentId: currentStudentId
    };

    console.log('Saving contact:', contact);

    let url = getBaseUrl() + '/contacts/add';
    let method = 'POST';

    if (currentContactId) {
        url = getBaseUrl() + '/contacts/update';
        method = 'PUT';
    }

    console.log('Saving contact to:', url, 'Method:', method);

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(contact)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.text();
    })
    .then(result => {
        console.log('Contact save result:', result);
        if (result === 'success') {
            showNotification('Contact saved successfully!', 'success');
            cancelContactForm();
            loadContacts(currentStudentId);
        } else {
            showNotification('Error saving contact!', 'error');
        }
    })
    .catch(error => {
        console.error('Error saving contact:', error);
        showNotification('Error saving contact!', 'error');
    });
}

function editContact(contactId) {
    currentContactId = contactId;
    console.log('Editing contact ID:', contactId);

    fetch(getBaseUrl() + `/contacts/${contactId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(contact => {
            console.log('Contact data:', contact);
            const container = document.getElementById('contactFormContainer');
            if (!container) return;

            container.innerHTML = `
                <h3><i class="fas fa-edit"></i> Edit Contact</h3>
                <form id="contactForm">
                    <input type="hidden" id="contactId" value="${contact.id}">
                    <div class="form-group">
                        <label for="mobile">Mobile Number:</label>
                        <input type="text" id="mobile" name="mobNo" value="${contact.mobNo}" required pattern="[0-9]{10}">
                    </div>
                    <div class="form-group">
                        <label for="city">City:</label>
                        <input type="text" id="city" name="city" value="${contact.city}" required>
                    </div>
                    <div class="form-group">
                        <label for="address">Address:</label>
                        <textarea id="address" name="address" rows="3" required>${contact.address}</textarea>
                    </div>
                    <div class="form-buttons">
                        <button type="button" class="btn btn-secondary" onclick="cancelContactForm()">
                            Cancel
                        </button>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-save"></i> Update Contact
                        </button>
                    </div>
                </form>
            `;

            const contactForm = document.getElementById('contactForm');
            if (contactForm) {
                contactForm.addEventListener('submit', function(e) {
                    e.preventDefault();
                    saveContact();
                });
            }
        })
        .catch(error => {
            console.error('Error loading contact:', error);
            showNotification('Error loading contact details', 'error');
        });
}

function deleteContact(contactId) {
    if (confirm('Are you sure you want to delete this contact?')) {
        console.log('Deleting contact ID:', contactId);
        fetch(getBaseUrl() + `/contacts/${contactId}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.text();
        })
        .then(result => {
            console.log('Delete contact result:', result);
            if (result === 'success') {
                showNotification('Contact deleted successfully!', 'success');
                loadContacts(currentStudentId);
            } else {
                showNotification('Error deleting contact!', 'error');
            }
        })
        .catch(error => {
            console.error('Error deleting contact:', error);
            showNotification('Error deleting contact!', 'error');
        });
    }
}

// Notification function
function showNotification(message, type) {
    console.log(`Notification [${type}]: ${message}`);

    // Remove existing notifications
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }

    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <span><i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i> ${message}</span>
        <button onclick="this.parentElement.remove()" style="background:none; border:none; color:white; cursor:pointer; margin-left:10px;">&times;</button>
    `;

    document.body.appendChild(notification);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentElement) {
            notification.remove();
        }
    }, 5000);
}

// Close modal when clicking outside
window.onclick = function(event) {
    const studentModal = document.getElementById('studentModal');
    const contactModal = document.getElementById('contactModal');

    if (event.target === studentModal) {
        closeStudentModal();
    }
    if (event.target === contactModal) {
        closeContactModal();
    }
};

// Make functions globally accessible
window.showStudentModal = showStudentModal;
window.closeStudentModal = closeStudentModal;
window.searchStudents = searchStudents;
window.clearSearch = clearSearch;
window.loadStudents = loadStudents;
window.saveStudent = saveStudent;
window.editStudent = editStudent;
window.deleteStudent = deleteStudent;
window.manageContacts = manageContacts;
window.closeContactModal = closeContactModal;
window.showAddContactForm = showAddContactForm;
window.cancelContactForm = cancelContactForm;
window.saveContact = saveContact;
window.editContact = editContact;
window.deleteContact = deleteContact;
window.showNotification = showNotification;

console.log('All JavaScript functions loaded and ready');