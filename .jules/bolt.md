
## 2026-10-16 - [In-Memory Search Anti-Pattern]
**Learning:** Found an O(N) in-memory search across an entire table (using `findAll().stream().flatMap(...)`) in the service layer where a simple indexed database lookup (`findById`) via a Repository should be used.
**Action:** Always check if a Repository exists or can be created to offload search and filter operations to the database level rather than doing it in-memory.
## 2026-10-18 - Replacing O(N²) List Search with O(N) Hash Map in DTO mapping
**Learning:** Found an anti-pattern where a DTO constructor was performing an O(N) linear search on a list passed as an argument (`allEmployees.stream().filter(...).findFirst()`) for EVERY item mapped from a stream. This turns an intended O(N) collection mapping operation into an O(N²) nested loop.
**Action:** When performing O(1) lookups during stream mapping, pre-compute a Hash Map (using `Collectors.toMap`) before mapping and pass the map into the constructor or mapping method instead of the raw list.
