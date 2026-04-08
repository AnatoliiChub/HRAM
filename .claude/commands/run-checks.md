# Run Quality Checks

Run the project's full quality gate and report results.

Execute the following commands in sequence and report the outcome of each:

1. **Detekt static analysis:**
   ```
   ./gradlew clean detekt
   ```
   Report: pass/fail, number of issues found, and any rule violations.

2. **Unit tests with coverage:**
   ```
   ./gradlew testDebugUnitTest koverHtmlReport
   ```
   Report: pass/fail, number of tests run, any failures with their test names and error messages.

