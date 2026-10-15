/**
 * 
 */
const subjectSelect = document.getElementById("subjectId");
const dateInput = document.getElementById("attendanceDate");
const rows = document.getElementById("attendanceRows");
const message = document.getElementById("message");

dateInput.value = new Date().toLocaleDateString("en-CA");

async function api(url, options = {}) {
    const response = await fetch(url, options);
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || "Request failed");
    return data;
}

async function loadSubjects() {
    const subjects = await api("api/subjects");
    subjectSelect.replaceChildren();

    subjects.forEach(s => {
        const option = document.createElement("option");
        option.value = s.subjectId;
        option.textContent = s.subjectCode + " - " + s.subjectName;
        subjectSelect.appendChild(option);
    });
}

async function loadAttendance() {
    try {
        const url = "api/attendance?subjectId="
            + encodeURIComponent(subjectSelect.value)
            + "&date=" + encodeURIComponent(dateInput.value);

        const students = await api(url);
        rows.replaceChildren();

        students.forEach(s => {
            const tr = document.createElement("tr");

            [s.rollNumber, s.fullName].forEach(value => {
                const td = document.createElement("td");
                td.textContent = value;
                tr.appendChild(td);
            });

            const statusCell = document.createElement("td");
            const select = document.createElement("select");

            ["PRESENT", "ABSENT", "LATE"].forEach(status => {
                const option = document.createElement("option");
                option.value = status;
                option.textContent = status;
                select.appendChild(option);
            });

            if (s.status) select.value = s.status;
            statusCell.appendChild(select);
            tr.appendChild(statusCell);

            const remarksCell = document.createElement("td");
            const remarks = document.createElement("input");
            remarks.value = s.remarks || "";
            remarks.maxLength = 255;
            remarksCell.appendChild(remarks);
            tr.appendChild(remarksCell);

            const actionCell = document.createElement("td");
            const button = document.createElement("button");
            button.textContent = "Save";

            button.addEventListener("click", async () => {
                try {
                    const body = new URLSearchParams({
                        studentId: String(s.studentId),
                        subjectId: subjectSelect.value,
                        date: dateInput.value,
                        status: select.value,
                        remarks: remarks.value
                    });

                    const result = await api("api/attendance", {
                        method: "POST",
                        headers: {"Content-Type":
                            "application/x-www-form-urlencoded;charset=UTF-8"},
                        body
                    });
                    message.textContent = result.message;
                } catch (e) {
                    message.textContent = e.message;
                }
            });

            actionCell.appendChild(button);
            tr.appendChild(actionCell);
            rows.appendChild(tr);
        });

        message.textContent = students.length + " student(s) loaded.";
    } catch (e) {
        message.textContent = e.message;
    }
}

document.getElementById("loadButton")
    .addEventListener("click", loadAttendance);

subjectSelect.addEventListener("change", loadAttendance);
dateInput.addEventListener("change", loadAttendance);

(async () => {
    try {
        await loadSubjects();
        if (subjectSelect.value) await loadAttendance();
        else message.textContent = "Add a subject before marking attendance.";
    } catch (e) {
        message.textContent = e.message;
    }
})();
