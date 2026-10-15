
const api = "api/subjects";
const form = document.getElementById("subjectForm");
const rows = document.getElementById("subjectRows");
const message = document.getElementById("message");

async function callApi(url, options = {}) {
    const response = await fetch(url, options);
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || "Request failed");
    return data;
}

async function loadSubjects() {
    try {
        const subjects = await callApi(api);
        rows.replaceChildren();

        subjects.forEach(s => {
            const tr = document.createElement("tr");

            [s.subjectId, s.subjectCode, s.subjectName].forEach(value => {
                const td = document.createElement("td");
                td.textContent = value;
                tr.appendChild(td);
            });

            const td = document.createElement("td");
            const button = document.createElement("button");
            button.textContent = "Delete";
            button.addEventListener("click", async () => {
                if (!confirm("Delete this subject?")) return;
                try {
                    const body = new URLSearchParams({
                        action: "delete", subjectId: String(s.subjectId)
                    });
                    const result = await callApi(api, {
                        method: "POST",
                        headers: {"Content-Type":
                            "application/x-www-form-urlencoded;charset=UTF-8"},
                        body
                    });
                    message.textContent = result.message;
                    await loadSubjects();
                } catch (e) {
                    message.textContent = e.message;
                }
            });
            td.appendChild(button);
            tr.appendChild(td);
            rows.appendChild(tr);
        });
    } catch (e) {
        message.textContent = e.message;
    }
}

form.addEventListener("submit", async event => {
    event.preventDefault();
    try {
        const result = await callApi(api, {
            method: "POST",
            headers: {"Content-Type":
                "application/x-www-form-urlencoded;charset=UTF-8"},
            body: new URLSearchParams(new FormData(form))
        });
        message.textContent = result.message;
        form.reset();
        await loadSubjects();
    } catch (e) {
        message.textContent = e.message;
    }
});

loadSubjects();
