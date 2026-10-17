## 2026-10-16 - Hardcoded JWT Secret Remediation
**Vulnerability:** A hardcoded production JWT secret (`SECRET_KEY_STRING`) was present in `src/main/java/security/JwtUtil.java`, allowing token forgery by anyone with access to the source code.
**Learning:** The hardcoded secret was implemented under the premise that sessions need to remain valid across restarts without requiring external configuration during development. This is a common anti-pattern where developer convenience overrides production security.
**Prevention:** Externalize secrets using Spring's `@Value` property injection. Provide a strong `SecureRandom` fallback with an explicit log warning for development environments to maintain developer convenience without compromising production security.

## 2026-10-17 - Log Forging and Information Leakage in Authentication
**Vulnerability:** The authentication controller logged plain-text user passwords to standard output and leaked internal stack traces/exception messages (via `e.getMessage()`) to the client on login failure.
**Learning:** Developers sometimes leave debugging statements like `System.out.println` and `e.printStackTrace()` in critical paths, such as login controllers, which can expose sensitive user data in logs and provide attackers with internal system information.
**Prevention:** Implement strict code review policies to remove debugging output before production. Ensure all authentication failure paths return generic error messages (e.g., "Invalid credentials") to prevent username enumeration and internal state leakage.
