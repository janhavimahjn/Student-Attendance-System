
const subjectSelect = document.getElementById("subjectId");
const modeSelect = document.getElementById("mode");
const periodInput = document.getElementById("period");
const rows = document.getElementById("reportRows");
const message = document.getElementById("message");

const today = new Date();
const localDate = [
    today.getFullYear(),
    String(today.getMonth() + 1).padStart(2, "0"),
    String(today.getDate()).padStart(2, "0")
].join("-");

periodInput.value = localDate;

async function api(url) {
    const response = await fetch(url);
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || "Request failed");
    return data;
}

async function loadSubjects() {
    const subjects = await api("api/subjects");
    subjects.forEach(s => {
        const option = document.createElement("option");
        option.value = s.subjectId;
        option.textContent = s.subjectCode + " - " + s.subjectName;
        subjectSelect.appendChild(option);
    });
}

modeSelect.addEventListener("change", () => {
    periodInput.type = modeSelect.value === "month" ? "month" : "date";
    periodInput.value = modeSelect.value === "month"
        ? localDate.slice(0, 7) : localDate;
});

document.getElementById("generateButton")
    .addEventListener("click", async () => {
        try {
            const url = "api/reports?subjectId="
                + encodeURIComponent(subjectSelect.value)
                + "&mode=" + encodeURIComponent(modeSelect.value)
                + "&period=" + encodeURIComponent(periodInput.value);

            const data = await api(url);
            rows.replaceChildren();

            data.forEach(r => {
                const tr = document.createElement("tr");
                [
                    r.rollNumber, r.fullName, r.present, r.absent,
                    r.late, r.total,
                    r.percentage == null ? "N/A" : r.percentage + "%"
                ].forEach(value => {
                    const td = document.createElement("td");
                    td.textContent = value;
                    tr.appendChild(td);
                });
                rows.appendChild(tr);
            });

            message.textContent = data.length
                ? data.length + " student report(s) generated."
                : "No attendance records found for this period.";
        } catch (e) {
            message.textContent = e.message;
        }
    });

(async () => {
    try {
        await loadSubjects();
    } catch (e) {
        message.textContent = e.message;
    }
})();
