
const message = document.getElementById("message");

async function loadDashboard() {
    try {
        const [studentsResponse, subjectsResponse] = await Promise.all([
            fetch("api/students"),
            fetch("api/subjects")
        ]);

        if (studentsResponse.status === 401 ||
            subjectsResponse.status === 401) {
            window.location.replace("login.html");
            return;
        }

        if (!studentsResponse.ok || !subjectsResponse.ok) {
            throw new Error("Could not load dashboard information");
        }

        const students = await studentsResponse.json();
        const subjects = await subjectsResponse.json();

        document.getElementById("studentCount").textContent =
            students.length;
        document.getElementById("subjectCount").textContent =
            subjects.length;

        message.textContent = "Welcome, Admin!";

    } catch (error) {
        message.textContent = error.message;
    }
}

document.getElementById("logoutButton")
    .addEventListener("click", async () => {
        try {
            await fetch("api/logout", { method: "POST" });
        } finally {
            window.location.replace("login.html");
        }
    });

loadDashboard();
