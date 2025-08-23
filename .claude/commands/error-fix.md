# Analyze and Fix Android Error

## Error Details
```
$ARGUMENTS
```

## Instructions for Error Resolution

Please follow this systematic approach to analyze and resolve the Android error above:

### 1. ERROR ANALYSIS PHASE

**Thoroughly analyze the error:**
- Parse the complete error stack trace
- Identify the root cause (not just symptoms)
- Determine error type (compile-time, runtime, build configuration, dependency, etc.)
- Identify all affected components and files
- Note the specific line numbers and methods involved
- Check if this is a known Android/Kotlin/Java issue pattern

**Context gathering:**
- Identify which Android component is affected (Activity, Fragment, ViewModel, Service, etc.)
- Determine Android API level implications
- Check if this relates to lifecycle, threading, or UI thread issues
- Identify any third-party library involvement

### 2. DOCUMENTATION VERIFICATION PHASE

**Use Context7 MCP server to verify the solution approach:**
- Query Context7 for the specific Android SDK documentation related to this error
- Look up Kotlin/Java documentation for any language-specific issues
- Search for official Android best practices for this error pattern
- Verify any deprecated API usage or migration requirements
- Check Material Design guidelines if UI-related
- Confirm proper Jetpack/AndroidX library usage

**Specifically search Context7 for:**
- The exact class/method mentioned in the error
- Android version compatibility notes
- Known issues and recommended workarounds
- Performance implications of potential fixes

### 3. SOLUTION DESIGN PHASE

**Before implementing any code changes, create a comprehensive fix plan:**

```markdown
## 🔍 Error Analysis Summary
- **Error Type**: [Classify the error]
- **Root Cause**: [Specific cause, not symptom]
- **Affected Components**: [List all affected files/components]
- **Android Version Impact**: [Min SDK implications]
- **Severity**: [Critical/High/Medium/Low]

## 📚 Documentation References (from Context7)
- [Relevant Android SDK documentation links/snippets]
- [Best practice guidelines that apply]
- [Any deprecation notices or migration guides]

## 🎯 Proposed Solution

### Approach [A] - Recommended
**Description**: [Clear description of the fix]
**Implementation Strategy**:
1. [Step 1 with specific file and line changes]
2. [Step 2 with rationale]
3. [Step 3 with testing approach]

**Pros**:
- [Advantage 1]
- [Advantage 2]

**Cons**:
- [Potential drawback if any]

**Best Practices Alignment**:
- ✅ [How this follows Android best practice 1]
- ✅ [How this follows Android best practice 2]

### Approach [B] - Alternative (if applicable)
[Similar structure as above]

## 🔧 Implementation Plan

### Files to Modify:
1. `[file path]` - [what changes and why]
2. `[file path]` - [what changes and why]

### Code Changes Required:
- [Specific change 1 with justification]
- [Specific change 2 with justification]

### Testing Strategy:
- [ ] Unit test for [specific test]
- [ ] UI test for [if applicable]
- [ ] Manual testing steps:
  1. [Step 1]
  2. [Step 2]

### Potential Side Effects:
- [Any ripple effects to watch for]
- [Performance implications]
- [Backward compatibility concerns]

## ⚠️ Risk Assessment
- **Risk Level**: [Low/Medium/High]
- **Rollback Strategy**: [How to revert if needed]
- **Monitoring**: [What to watch after fix]

## 📋 Pre-Implementation Checklist
- [ ] Error fully understood and documented
- [ ] Context7 documentation verified
- [ ] Solution follows Android best practices
- [ ] No deprecated APIs being introduced
- [ ] Thread safety considered
- [ ] Memory leak potential evaluated
- [ ] Configuration changes handled properly
- [ ] Edge cases identified

---

**⏸️ STOPPING HERE - Awaiting your approval before implementing any code changes**

Please review the analysis and proposed solution above. Reply with:
- "Proceed with Approach A" to implement the recommended solution
- "Proceed with Approach B" to implement the alternative
- "Modify approach" if you want changes to the plan
- Any specific concerns or requirements
```

### 4. IMPORTANT INSTRUCTIONS

**DO NOT:**
- Start coding immediately
- Make assumptions without checking documentation
- Apply quick fixes without understanding root cause
- Ignore Android lifecycle implications
- Skip Context7 verification for APIs and best practices

**ALWAYS:**
- Present the complete analysis before any code changes
- Use Context7 to verify every Android API usage
- Consider multiple Android versions (especially minSdk)
- Think about configuration changes (rotation, etc.)
- Consider memory and performance implications
- Check for deprecated APIs or better alternatives
- Ensure thread safety for concurrent operations
- Follow Material Design and Android patterns

**After approval is given:**
- Implement the approved approach exactly as planned
- Add appropriate error handling
- Include meaningful comments explaining the fix
- Update any related documentation
- Suggest appropriate tests

Wait for explicit approval before making any code modifications.