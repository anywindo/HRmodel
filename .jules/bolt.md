
## 2026-10-16 - [In-Memory Search Anti-Pattern]
**Learning:** Found an O(N) in-memory search across an entire table (using `findAll().stream().flatMap(...)`) in the service layer where a simple indexed database lookup (`findById`) via a Repository should be used.
**Action:** Always check if a Repository exists or can be created to offload search and filter operations to the database level rather than doing it in-memory.
## 2026-10-21 - [Database Filtering vs. In-Memory Filtering]
**Learning:** Found performance bottlenecks in the codebase where all entities (e.g., `Employee`) were fetched into memory (`findAll()`) before filtering via Java streams (e.g., `stream().filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)`). This leads to excessive database retrieval and memory overhead. Also discovered test flakiness in `LeaveServiceTest.cancelRequest_RefundsBalance` related to date calculation mapping on weekends.
**Action:** Replaced in-memory filtering with database-level queries (`findByStatus()`) using Spring Data JPA repository methods. Addressed test failures by calculating appropriate mock dates without weekends. Always ensure test mock logic avoids weekends if business logic considers them.
