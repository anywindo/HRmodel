## 2026-10-16 - Hardcoded JWT Secret Remediation
**Vulnerability:** A hardcoded production JWT secret (`SECRET_KEY_STRING`) was present in `src/main/java/security/JwtUtil.java`, allowing token forgery by anyone with access to the source code.
**Learning:** The hardcoded secret was implemented under the premise that sessions need to remain valid across restarts without requiring external configuration during development. This is a common anti-pattern where developer convenience overrides production security.
**Prevention:** Externalize secrets using Spring's `@Value` property injection. Provide a strong `SecureRandom` fallback with an explicit log warning for development environments to maintain developer convenience without compromising production security.
