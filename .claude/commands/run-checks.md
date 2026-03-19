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

3. **Summary** — present a concise table:
   | Check | Status | Details |
   |-------|--------|---------|
   | Detekt | ✅/❌ | N issues |
   | Unit Tests | ✅/❌ | N passed, N failed |
   | Coverage | ✅/❌ | X% (threshold: 80%) |

If any check fails, show the specific error output and suggest the most likely fix based on the error message.
