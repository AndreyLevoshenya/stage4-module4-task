export function decodeJwtPayload(token) {
    if (!token || typeof token !== "string") {
        throw new Error("Token is required");
    }

    const parts = token.split(".");
    if (parts.length < 2) {
        throw new Error("Invalid JWT format");
    }

    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64 + "=".repeat((4 - (base64.length % 4)) % 4);

    const json = decodeURIComponent(
        atob(padded)
            .split("")
            .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
            .join("")
    );

    return JSON.parse(json);
}



