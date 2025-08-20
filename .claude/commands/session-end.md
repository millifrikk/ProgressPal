# Enhanced Session End Command

End the current development session with comprehensive analytics and actionable insights.

## Command Flow

1. **Validate Active Session**:
   - Check `.claude/sessions/.current-session`
   - Load session start metadata

2. **Calculate Session Metrics**:
   ```python
   # Time metrics
   - Total duration
   - Active coding time (based on update frequency)
   - Break time
   - Efficiency rating
   
   # Productivity metrics
   - Objectives completion rate
   - Tasks completed vs planned
   - Lines of code changed
   - Files touched
   
   # Quality metrics
   - Code quality score change
   - Test coverage change
   - Tests added/modified
   - Bugs fixed vs introduced
   ```

3. **Generate Comprehensive Summary**:

   ```markdown
   ## 🏁 SESSION END SUMMARY
   
   **Session**: [Name]  
   **Duration**: [X hours Y minutes] ([Start] → [End])  
   **Type**: [Feature|Bug Fix|Refactor|Documentation|Research]  
   **Overall Status**: ✅ Successful | ⚠️ Partial Success | ❌ Blocked
   
   ---
   
   ## 📊 Executive Summary
   
   ### Objectives Achievement
   - **Primary Goal**: [✅ Completed|⚠️ Partial|❌ Not Met] - [Description]
     - Success Criteria Met: [X of Y]
   - **Secondary Goals**: [X of Y completed]
   
   ### Key Metrics
   | Metric | Start | End | Change | Target | Status |
   |--------|-------|-----|--------|--------|--------|
   | Code Quality | 8.2 | 8.5 | +0.3 | >8.0 | ✅ |
   | Test Coverage | 67% | 74% | +7% | >70% | ✅ |
   | API Performance | 245ms | 198ms | -47ms | <200ms | ✅ |
   | Open Issues | 12 | 10 | -2 | - | ✅ |
   
   ### Efficiency Analysis
   - **Productive Time**: X hours (YY% of total)
   - **Blocked Time**: X minutes (YY% of total)
   - **Task Completion Rate**: XX% (Z of W tasks)
   - **Focus Score**: X/10 (based on context switches)
   
   ---
   
   ## 🔧 Technical Summary
   
   ### Architecture Changes
   - **Patterns Introduced**: [List any new patterns]
   - **Services Modified**: [List services with change scope]
   - **Dependencies Added**: [New packages with versions]
   - **Breaking Changes**: [Any API/interface changes]
   
   ### Code Changes Overview
   ```
   Language     Files  Added  Deleted  Modified
   ---------    -----  -----  -------  --------
   Python         12    450     120      330
   TypeScript      8    225      45      180
   CSS             3     75      10       65
   ---------    -----  -----  -------  --------
   Total          23    750     175      575
   ```
   
   ### Git Activity
   - **Commits Made**: [X commits]
   - **Branch**: [branch name]
   - **Files Changed**: [List of all files with change type]
     - Added: [files]
     - Modified: [files]
     - Deleted: [files]
   - **Final Status**: [Clean|X uncommitted changes]
   
   ### Test Suite Status
   - **Tests Added**: X new test cases
   - **Tests Modified**: Y test updates
   - **Coverage Delta**: +X.X%
   - **Test Run Time**: XXXms (YY% change)
   - **Flaky Tests**: [List any intermittent failures]
   
   ---
   
   ## 📋 Task Tracking Summary
   
   ### Completed Tasks (X total)
   - ✅ [High] Task description - [time spent]
   - ✅ [Medium] Task description - [time spent]
   
   ### In Progress Tasks (Y total)
   - 🔄 [Priority] Task - [% complete] - [blocker if any]
   
   ### Deferred Tasks (Z total)
   - ⏸️ [Priority] Task - [reason for deferral]
   
   ### Newly Discovered Tasks
   - 📌 [Priority] Task - [discovered during X]
   
   ---
   
   ## 🧠 Decisions & Learnings
   
   ### Key Decisions Made
   1. **Decision**: [Description]
      - **Rationale**: [Why]
      - **Alternative**: [What was not chosen]
      - **Impact**: [Expected outcome]
   
   ### Problems Solved
   1. **Problem**: [Description]
      - **Root Cause**: [Analysis]
      - **Solution**: [Implementation]
      - **Prevention**: [Future avoidance]
   
   ### Lessons Learned
   - **What Worked Well**: 
     - [Success patterns to repeat]
   - **What Could Improve**:
     - [Areas for optimization]
   - **Surprises**:
     - [Unexpected discoveries]
   
   ---
   
   ## 🚀 Deployment & Production Impact
   
   ### Deployment Readiness
   - [ ] All tests passing
   - [ ] Documentation updated
   - [ ] Migration scripts prepared
   - [ ] Rollback plan documented
   - [ ] Performance benchmarks met
   
   ### Expected Impact
   - **Users Affected**: [Scope]
   - **Performance Impact**: [Metrics]
   - **Risk Assessment**: [Low|Medium|High]
   
   ---
   
   ## 🔮 Next Session Planning
   
   ### Immediate Next Steps
   1. [Highest priority task]
   2. [Second priority]
   
   ### Technical Debt Created
   - [Debt item] - [Estimated effort to resolve]
   
   ### Recommended Session Type
   - **Type**: [Feature|Refactor|Testing]
   - **Focus**: [Specific area]
   - **Duration**: [Estimated hours]
   
   ### Prerequisites for Next Session
   - [ ] [Required setup/configuration]
   - [ ] [Dependencies to install]
   - [ ] [Decisions needed]
   
   ---
   
   ## 📎 Appendices
   
   ### A. Configuration Changes
   ```
   [Any .env, config files, or settings changes]
   ```
   
   ### B. Important Code Snippets
   ```python
   # Key implementations worth preserving
   ```
   
   ### C. Error Log Highlights
   ```
   [Any significant errors encountered and resolved]
   ```
   
   ### D. Performance Benchmarks
   ```
   [Before/after metrics for critical operations]
   ```
   
   ---
   
   ## Session Metadata
   - **Total Updates**: [Count]
   - **Blockers Encountered**: [Count]
   - **Decisions Made**: [Count]
   - **Milestones Reached**: [Count]
   - **Session Rating**: [Auto-calculated based on objectives met]
   
   ---
   
   *Session automatically archived to `.claude/sessions/archive/[filename]`*
   *Metrics exported to `.claude/analytics/sessions.json`*
   ```

4. **Generate Actionable Insights**:
   - Pattern recognition from multiple sessions
   - Productivity trends
   - Common blockers
   - Time estimates accuracy

5. **Update Changelog**:
   - Analyze completed objectives and features
   - Determine appropriate changelog entries
   - Auto-add to CHANGELOG.md under [Unreleased]
   - Example:
     ```markdown
     ## [Unreleased]
     
     ### Added
     - [Feature from primary objective] (Session: YYYY-MM-DD-HHMM)
     
     ### Fixed
     - [Bug fixes completed] (Session: YYYY-MM-DD-HHMM)
     ```

6. **Archive and Clean**:
   - Move session to archive with full metrics
   - Clear `.current-session`
   - Update session analytics database
   - Generate session report card
   - Update changelog with session achievements

7. **Provide Summary Output**:
   ```
   ✅ Session Ended: [session-name]
   ⏱️  Duration: X hours Y minutes
   🎯 Objectives Met: X of Y (XX%)
   📈 Quality Improved: +X.X points
   📊 Coverage Increased: +X%
   
   Key Achievements:
   • [Top achievement 1]
   • [Top achievement 2]
   
   Next Recommended Action:
   → [Specific next step based on session outcome]
   
   Full summary saved to: [filepath]
   Analytics updated in: .claude/analytics/
   ```

## Special Features

### Continuous Learning
- Compare estimates vs actual time
- Track decision outcomes
- Build knowledge base from learnings

### Team Integration
- Export summary for team sharing
- Generate PR description
- Create ticket updates

### Quality Gates
- Warn if quality metrics degraded
- Highlight if tests were skipped
- Flag if documentation wasn't updated

## Usage Examples

```bash
# Standard end
/session-end

# End with custom summary
/session-end "Phase 1 complete, ready for review"

# End with specific status
/session-end --status=blocked --reason="Waiting for API access"

# Generate report only (keep session active)
/session-end --report-only
```