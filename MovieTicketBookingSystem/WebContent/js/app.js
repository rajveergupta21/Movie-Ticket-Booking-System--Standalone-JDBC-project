const CineVerse = {
    sessionUser: null,

    async initNavbar(activePage) {
        try {
            const res = await fetch("/api/session");
            const data = await res.json();
            if (data.authenticated) {
                this.sessionUser = data.user;
            } else {
                this.sessionUser = null;
            }
        } catch (err) {
            this.sessionUser = null;
        }

        const navHost = document.getElementById("navbar-host");
        if (!navHost) {
            return;
        }

        let authLinks = "";
        if (this.sessionUser) {
            const adminLink = this.sessionUser.role === "ADMIN"
                ? `<a href="/admin.html" class="nav-link ${activePage === "admin" ? "active" : ""}">Admin Dashboard</a>`
                : "";
            authLinks = `
                <a href="/my-bookings.html" class="nav-link ${activePage === "bookings" ? "active" : ""}">My Bookings</a>
                ${adminLink}
                <span class="badge badge-gold">${this.escapeHtml(this.sessionUser.name)}</span>
                <button type="button" class="btn btn-outline btn-sm" id="logout-btn">Logout</button>
            `;
        } else {
            authLinks = `
                <a href="/login.html" class="nav-link ${activePage === "login" ? "active" : ""}">Login</a>
                <a href="/register.html" class="btn btn-primary btn-sm">Register</a>
            `;
        }

        navHost.innerHTML = `
            <header class="navbar">
                <div class="nav-container">
                    <a href="/index.html" class="brand-logo">
                        <span class="brand-badge">CV</span>
                        <span>CineVerse</span>
                    </a>
                    <nav class="nav-links">
                        <a href="/index.html" class="nav-link ${activePage === "home" ? "active" : ""}">Home</a>
                        <a href="/movies.html" class="nav-link ${activePage === "movies" ? "active" : ""}">Movies &amp; Showtimes</a>
                        ${authLinks}
                    </nav>
                </div>
            </header>
        `;

        const logoutBtn = document.getElementById("logout-btn");
        if (logoutBtn) {
            logoutBtn.addEventListener("click", async () => {
                await fetch("/api/logout", { method: "POST" });
                window.location.href = "/index.html";
            });
        }
    },

    escapeHtml(str) {
        if (str === null || str === undefined) {
            return "";
        }
        return String(str)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    },

    encodeForm(dataObj) {
        const params = new URLSearchParams();
        Object.keys(dataObj).forEach((k) => {
            params.append(k, dataObj[k]);
        });
        return params.toString();
    },

    async postForm(url, dataObj) {
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
            },
            body: this.encodeForm(dataObj)
        });
        const json = await response.json();
        return { ok: response.ok, status: response.status, data: json };
    }
};
