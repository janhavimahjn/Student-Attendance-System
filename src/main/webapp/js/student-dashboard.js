
const $ = id => document.getElementById(id);

function localMonth() {
    const now = new Date();
    return now.getFullYear() + "-"
        + String(now.getMonth() + 1).padStart(2, "0");
}

$("reportMonth").value = localMonth();

async function api(url, options = {}) {
    const response = await fetch(url, options);

    // The session may have expired.
    if (response.status === 401) {
        window.location.replace("student-login.html");
        throw new Error("Please sign in again.");
    }

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.error || "Request failed");
    }

    return data;
}

function cell(row, value) {
    const td = document.createElement("td");
    td.textContent = value ?? "—";
    row.appendChild(td);
    return td;
}

async function loadProfile() {
    const profile = await api("api/student/profile");

    $("welcome").textContent = "Welcome, " + profile.fullName;
    $("profileDetails").textContent =
        "Roll Number: " + profile.rollNumber
        + " | Class: " + profile.className
        + " | Email: " + profile.email;
}

async function loadHistory() {
    const rows = $("historyRows");
    rows.replaceChildren();

    const records = await api("api/student/attendance");

    let present = 0;
    let absent = 0;
    let late = 0;

    records.forEach(record => {
        if (record.status === "PRESENT") present++;
        if (record.status === "ABSENT") absent++;
        if (record.status === "LATE") late++;

        const tr = document.createElement("tr");

        cell(tr, record.date);
        cell(tr, record.subjectCode + " - " + record.subjectName);

        const statusCell = cell(tr, record.status);
        statusCell.className = "status-" + record.status.toLowerCase();

        cell(tr, record.remarks);
        rows.appendChild(tr);
    });

    const attended = present + late;
    const total = present + absent + late;
    const percentage = total ? attended / total * 100 : null;

    $("overallPercentage").textContent =
        percentage === null ? "N/A" : percentage.toFixed(1) + "%";

    $("overallProgress").style.width =
        percentage === null ? "0%" : percentage + "%";

    $("presentCount").textContent = present + " + " + late + " late";
    $("absentCount").textContent = absent;

    $("historyMessage").textContent = records.length
        ? records.length + " attendance records"
        : "No attendance records have been published yet.";
}

async function loadReport() {
    const month = $("reportMonth").value;
    if (!month) {
        $("reportMessage").textContent = "Select a month first.";
        return;
    }

    try {
        const records = await api(
            "api/student/reports?month=" + encodeURIComponent(month)
        );

        const rows = $("reportRows");
        rows.replaceChildren();

        records.forEach(record => {
            const tr = document.createElement("tr");

            cell(tr, record.subjectCode + " - " + record.subjectName);
            cell(tr, record.present);
            cell(tr, record.absent);
            cell(tr, record.late);
            cell(tr, record.total);
            cell(tr, Number(record.percentage).toFixed(2) + "%");

            rows.appendChild(tr);
        });

        $("reportMessage").textContent = records.length
            ? "Report generated for " + month
            : "No attendance records for this month.";

    } catch (error) {
        $("reportMessage").textContent = error.message;
    }
}

$("loadReportButton").addEventListener("click", loadReport);

$("logoutButton").addEventListener("click", async () => {
    try {
        await fetch("api/student-auth/logout", { method: "POST" });
    } finally {
        window.location.replace("student-login.html");
    }
});

(async function init() {
    try {
        await loadProfile();
        await loadHistory();
        await loadReport();
    } catch (error) {
        $("welcome").textContent = error.message;
    }
})();
