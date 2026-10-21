## 2026-10-16 - Hardcoded JWT Secret Remediation
**Vulnerability:** A hardcoded production JWT secret (`SECRET_KEY_STRING`) was present in `src/main/java/security/JwtUtil.java`, allowing token forgery by anyone with access to the source code.
**Learning:** The hardcoded secret was implemented under the premise that sessions need to remain valid across restarts without requiring external configuration during development. This is a common anti-pattern where developer convenience overrides production security.
**Prevention:** Externalize secrets using Spring's `@Value` property injection. Provide a strong `SecureRandom` fallback with an explicit log warning for development environments to maintain developer convenience without compromising production security.

## 2026-10-17 - Log Forging and Information Leakage in Authentication
**Vulnerability:** The authentication controller logged plain-text user passwords to standard output and leaked internal stack traces/exception messages (via `e.getMessage()`) to the client on login failure.
**Learning:** Developers sometimes leave debugging statements like `System.out.println` and `e.printStackTrace()` in critical paths, such as login controllers, which can expose sensitive user data in logs and provide attackers with internal system information.
**Prevention:** Implement strict code review policies to remove debugging output before production. Ensure all authentication failure paths return generic error messages (e.g., "Invalid credentials") to prevent username enumeration and internal state leakage.

## 2024-05-15 - Path Traversal in File Uploads
**Vulnerability:** A path traversal vulnerability existed in the `/api/leave/upload` endpoint of `LeaveController`. The file extension was extracted using `substring` after the last dot. If an attacker provided a filename like `foo.bar/../../etc/passwd`, the extracted "extension" would be `.bar/../../etc/passwd`. Appending this to a UUID and resolving it allowed writing files outside the intended `uploads/` directory.
**Learning:** Extracting an extension using only string operations (e.g., `lastIndexOf(".")`) without sanitization can inadvertently capture path separators (`/` or `\`). Even if the filename starts with a UUID, appending an unsanitized extension allows path traversal.
**Prevention:**
1. Always sanitize file extensions (e.g., `replaceAll("[^a-zA-Z0-9.]", "")`).
2. Always normalize the resolved path (`path.normalize()`).
3. Always verify that the normalized target path is a child of the intended directory (`filePath.startsWith(uploadDir)`).
