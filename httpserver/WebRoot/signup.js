document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector("form");

    form.addEventListener("submit", async (e) => {
        e.preventDefault(); // stop default form POST

        const input = document.querySelector("input");
        const username = input.value;

        try {
            const response = await fetch("/sign", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    "Content-Length": username.length.toString()
                },
                body: `username=${encodeURIComponent(username)}`
            });

            const text = await response.text();

            // show response
            alert(text);

        } catch (err) {
            console.error("Error:", err);
        }
    });
});