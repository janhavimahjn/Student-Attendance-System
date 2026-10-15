
const form = document.getElementById("loginForm");
const message = document.getElementById("message");

form.addEventListener("submit", async event => {
    event.preventDefault();

    try {
        const response = await fetch("api/login", {
            method: "POST",
            headers: {
                "Content-Type":
                    "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: new URLSearchParams(new FormData(form))
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || "Login failed");
        }

        window.location.href = "dashboard.html";

    } catch (error) {
        message.textContent = error.message;
    }
});
