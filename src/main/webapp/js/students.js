
const API_URL = "api/students";

const form = document.getElementById("studentForm");
const message = document.getElementById("message");
const tableBody = document.getElementById("studentTableBody");
const searchInput = document.getElementById("searchInput");

let editingId = null;

function showMessage(text) {
    message.textContent = text;
}

async function request(url, options = {}) {
    const response = await fetch(url, options);
    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.error || "Request failed");
    }
    return data;
}

function formBody(extra = {}) {
    const body = new URLSearchParams(new FormData(form));
    Object.entries(extra).forEach(([key, value]) => body.set(key, value));
    return body;
}

async function loadStudents() {
    try {
        const query = searchInput.value.trim();
        const url = query
            ? API_URL + "?search=" + encodeURIComponent(query)
            : API_URL;

        const students = await request(url);
        tableBody.replaceChildren();

        students.forEach(student => {
            const row = document.createElement("tr");

            [
                student.studentId,
                student.rollNumber,
                student.fullName,
                student.email,
                student.className
            ].forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value ?? "";
                row.appendChild(cell);
            });

            const actions = document.createElement("td");

            const editButton = document.createElement("button");
            editButton.type = "button";
            editButton.textContent = "Edit";
            editButton.addEventListener("click", () => editStudent(student));

            const deleteButton = document.createElement("button");
            deleteButton.type = "button";
            deleteButton.textContent = "Delete";
            deleteButton.addEventListener("click", () => deleteStudent(student.studentId));

            actions.append(editButton, deleteButton);
            row.appendChild(actions);
            tableBody.appendChild(row);
        });

        showMessage("Loaded " + students.length + " student(s).");
    } catch (error) {
        showMessage(error.message);
    }
}

function editStudent(student) {
    editingId = student.studentId;
    form.elements.rollNumber.value = student.rollNumber;
    form.elements.fullName.value = student.fullName;
    form.elements.email.value = student.email ?? "";
    form.elements.className.value = student.className;
    form.querySelector("button[type='submit']").textContent = "Update Student";
    showMessage("Editing " + student.fullName);
    form.scrollIntoView({ behavior: "smooth" });
}

async function deleteStudent(studentId) {
    if (!confirm("Delete this student? This cannot be undone.")) return;

    try {
        const body = new URLSearchParams({
            action: "delete",
            studentId: String(studentId)
        });

        const result = await request(API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body
        });

        showMessage(result.message);
        await loadStudents();
    } catch (error) {
        showMessage(error.message);
    }
}

form.addEventListener("submit", async event => {
    event.preventDefault();

    try {
        const extra = editingId === null
            ? {}
            : { action: "update", studentId: String(editingId) };

        const result = await request(API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: formBody(extra)
        });

        showMessage(result.message);
        form.reset();
        editingId = null;
        form.querySelector("button[type='submit']").textContent = "Add Student";
        await loadStudents();
    } catch (error) {
        showMessage(error.message);
    }
});

document.getElementById("refreshButton")
    .addEventListener("click", loadStudents);

searchInput.addEventListener("input", loadStudents);

loadStudents();
