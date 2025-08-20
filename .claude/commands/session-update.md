# Enhanced Session Update Command

Add structured updates to the current session with automatic metrics collection.

## Command Flow

1. **Validate Active Session**:
   - Check `.claude/sessions/.current-session`
   - Error if no active session

2. **Determine Update Type**:
   - Progress (default)
   - Blocker (--blocker)
   - Decision (--decision)
   - Milestone (--milestone)
   - Pivot (--pivot)

3. **Collect Automatic Metrics**:
   ```bash
   # Git metrics
   git status --porcelain | analyze
   git diff --stat
   git log --oneline -n 5
   
   # Code quality (if applicable)
   # Python: pylint score, coverage
   # JS/TS: eslint results
   
   # Test status
   # pytest/jest results summary
   
   # Performance metrics
   # API response times if server running
   ```

4. **Format Update**:
   ```markdown
   ### Update - [Timestamp] - [Phase] - [Type]
   
   **Update Type**: [Progress|Blocker|Decision|Milestone|Pivot]
   **Phase**: [Planning|Implementation|Testing|Review|Deployment]
   
   **Summary**: [One-line summary from $ARGUMENTS or auto-generated]
   
   **Details**:
   [Detailed description of work done, decisions made, issues encountered]
   
   **Changes Made**:
   - Files Modified: [list with +/- line counts]
   - Components Affected: [list components/services touched]
   - Tests Updated: [test files modified]
   
   **Metrics Snapshot**:
   - Git Status: [X files modified, Y untracked, Z staged]
   - Tasks: [X completed, Y in progress, Z blocked] (from TodoWrite)
   - Tests: [X passing, Y failing, Z skipped]
   - Coverage: [XX.X%] ([+/-X.X%] from session start)
   - Quality Score: [X.X/10] ([+/-X.X] from session start)
   - Performance: [API avg response time if applicable]
   
   **Completed Since Last Update**:
   - ✓ [Completed task 1]
   - ✓ [Completed task 2]
   
   **Currently Working On**:
   - 🔄 [In progress task]
   
   **Blockers/Issues**:
   - ⚠️ [Any blockers or issues]
   
   **Decisions Made**:
   - 📌 [Important decisions with rationale]
   
   **Next Steps**:
   - [ ] [Immediate next action]
   - [ ] [Following action]
   
   **Time Spent**: [X minutes on this segment]
   **Session Total**: [Y hours Z minutes]
   
   ---
   ```

5. **Special Update Types**:

   **Blocker Update**:
   ```markdown
   ### 🚨 BLOCKER - [Timestamp]
   
   **Issue**: [Clear description of blocker]
   **Impact**: [What this blocks]
   **Attempted Solutions**:
   1. [What was tried]
   2. [Other attempts]
   **Help Needed**: [Specific assistance required]
   **Workaround**: [Temporary solution if any]
   ```

   **Decision Update**:
   ```markdown
   ### 📌 DECISION - [Timestamp]
   
   **Decision**: [Clear statement of decision]
   **Context**: [Why this came up]
   **Options Considered**:
   1. Option A - Pros/Cons
   2. Option B - Pros/Cons
   **Rationale**: [Why this option was chosen]
   **Impact**: [Expected effects]
   **Rollback Plan**: [How to reverse if needed]
   ```

   **Milestone Update**:
   ```markdown
   ### 🎯 MILESTONE - [Timestamp]
   
   **Achievement**: [What was completed]
   **Objective Met**: [Link to session objective]
   **Quality Checks**:
   - ✅ All tests passing
   - ✅ Code review complete
   - ✅ Documentation updated
   **Metrics at Milestone**:
   - Coverage: XX%
   - Performance: XX ms
   - Quality Score: X.X/10
   ```

6. **Auto-save Checkpoints**:
   - Create checkpoint file every 30 minutes
   - Save current state metrics
   - Allow rollback if needed

## Usage Examples

```bash
# Simple progress update
/session-update Implemented user authentication middleware

# Blocker update
/session-update "Cannot connect to Vertex AI" --blocker

# Decision update
/session-update "Chose SQLAlchemy over raw SQL for better ORM" --decision

# Milestone update
/session-update "Authentication system complete and tested" --milestone

# Update with specific phase
/session-update "Starting integration tests" --phase=testing
```

## Automatic Summaries

If no update message provided, generate automatic summary:
1. Analyze git diff for high-level changes
2. Check completed TODOs for achievements
3. Identify modified components
4. Create coherent summary

## Best Practices

1. Update at natural break points (every 30-60 min)
2. Use appropriate update types for clarity
3. Include enough detail for future reference
4. Always note decisions and their rationale
5. Track blockers immediately when encountered