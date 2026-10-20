
## 2026-10-16 - [In-Memory Search Anti-Pattern]
**Learning:** Found an O(N) in-memory search across an entire table (using `findAll().stream().flatMap(...)`) in the service layer where a simple indexed database lookup (`findById`) via a Repository should be used.
**Action:** Always check if a Repository exists or can be created to offload search and filter operations to the database level rather than doing it in-memory.
## 2026-10-20 - [Full Table Scan in Service Layer]
**Learning:** Found a full table scan (`findAll()`) followed by an in-memory application-layer filter for hierarchical data in `EmployeeService.getMyTeam()`. This causes large memory overhead and slow response times. Furthermore, passing the `findAll` results to the constructor `new EmployeeResponse` causes an N+1 issue because the constructor loops over `allEmployees` to find the supervisor of each team member.
**Action:** Always fetch only the necessary data from the database using tailored repository methods (e.g. `findByStatusAndPosition_ReportsTo_PositionId_Value`) instead of fetching everything and filtering in memory. For `EmployeeResponse`, pass only the necessary context (e.g. the supervisor object directly) instead of the full list of all employees.
