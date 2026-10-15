
const form = document.getElementById("studentForm");
const message = document.getElementById("message");
const rollField = document.getElementById("rollField");
const rollInput = document.getElementById("rollNumber");
const passwordInput = document.getElementById("password");
const submitButton = document.getElementById("submitButton");

let mode = "login";

function setMode(nextMode) {
    mode = nextMode;
    const registering = mode === "register";

    rollField.hidden = !registering;
    rollInput.required = registering;
    passwordInput.autocomplete = registering
        ? "new-password" : "current-password";
    passwordInput.value = "";

    submitButton.textContent = registering
        ? "Create Account" : "Sign In";

    message.textContent = "";
}

document.getElementById("loginTab").addEventListener("click", event => {
    event.preventDefault();
    setMode("login");
});

document.getElementById("registerTab").addEventListener("click", event => {
    event.preventDefault();
    setMode("register");
});


form.addEventListener("submit", async event => {
    event.preventDefault();
    message.textContent = "Signing in...";

    const endpoint = mode === "register"
        ? "api/student-auth/register"
        : "api/student-auth/login";

    try {
        const response = await fetch(endpoint, {
            method: "POST",
            headers: {
                "Content-Type":
                    "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: new URLSearchParams(new FormData(form))
        });

        const text = await response.text();
        let data;

        try {
            data = JSON.parse(text);
        } catch {
            throw new Error(
                "Server returned a non-JSON response. HTTP " +
                response.status + ". Check the Eclipse Console."
            );
        }

        if (!response.ok) {
            throw new Error(data.error || "Login failed: HTTP " + response.status);
        }

        if (mode === "register") {
            setMode("login");
            message.textContent = "Account created. Sign in now.";
            return;
        }

        message.textContent = "Login successful. Opening dashboard...";
        window.location.replace("student-dashboard.html");

    } catch (error) {
        message.textContent = error.message;
        console.error("Student login error:", error);
    }
});

