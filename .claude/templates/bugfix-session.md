# Bug Fix Session Template

## Session Metadata
- **Session ID**: [YYYY-MM-DD-HHMM-bugfix-identifier]
- **Start Time**: [ISO 8601 timestamp]
- **Type**: Bug Fix
- **Priority**: [Critical|High|Medium|Low]
- **Status**: Active
- **Bug ID**: [Issue tracker ID]
- **Severity**: [Blocker|Major|Minor|Trivial]
- **Affected Users**: [Scope/percentage]

## Bug Details
### Summary
[One-line description of the bug]

### Expected Behavior
[What should happen]

### Actual Behavior
[What is happening]

### Reproduction Steps
1. [Step 1]
2. [Step 2]
3. [Step 3]
4. Expected: [result]
5. Actual: [result]

### Environment
- **Found In**: [Version/commit]
- **Browser/OS**: [If applicable]
- **API Version**: [If applicable]
- **Database State**: [Relevant conditions]

## Initial Investigation
### Error Analysis
- **Error Messages**: 
  ```
  [Stack traces, console errors]
  ```
- **Logs**: [Relevant log entries]
- **Related Code**: [Files/functions suspected]

### Root Cause Hypothesis
- **Primary Suspect**: [Most likely cause]
- **Alternative Causes**: [Other possibilities]
- **Evidence**: [Supporting data]

## Fix Strategy
### Approach
- **Fix Type**: [Code change|Configuration|Data correction]
- **Affected Components**: [List of components]
- **Testing Strategy**: [How to verify fix]

### Implementation Plan
1. [ ] Reproduce bug locally
2. [ ] Identify exact failure point
3. [ ] Implement fix
4. [ ] Write regression test
5. [ ] Test edge cases
6. [ ] Verify no side effects

### Rollback Plan
[How to revert if fix causes issues]

## Quality Checklist
- [ ] Bug reproduced before fix
- [ ] Root cause identified
- [ ] Fix implemented
- [ ] Regression test added
- [ ] All existing tests pass
- [ ] Manual testing complete
- [ ] Performance impact assessed
- [ ] Documentation updated

## Progress Tracking

### Debugging Timeline
- **Bug Reported**: [When]
- **First Reproduced**: [When]
- **Root Cause Found**: [When]
- **Fix Implemented**: [When]
- **Fix Verified**: [When]

---

## Session Log

### [Start Time] - Investigation Phase
- Attempting to reproduce issue
- Checking recent changes that might relate

### Debug Notes
[This section will be filled during debugging]