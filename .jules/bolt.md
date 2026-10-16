
## 2026-10-16 - [In-Memory Search Anti-Pattern]
**Learning:** Found an O(N) in-memory search across an entire table (using `findAll().stream().flatMap(...)`) in the service layer where a simple indexed database lookup (`findById`) via a Repository should be used.
**Action:** Always check if a Repository exists or can be created to offload search and filter operations to the database level rather than doing it in-memory.
