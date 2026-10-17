## 2024-05-18 - N+1 Saves in Scheduled Tasks
**Learning:** Found an anti-pattern in the Spring Boot backend where JPA `save()` was being called inside loops during batch processing tasks like `runMonthlyAccruals`. This causes N individual database queries.
**Action:** Always look for iterative database writes and replace them with batch `saveAll()` calls outside the loop for bulk operations.
