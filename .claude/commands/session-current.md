# Enhanced Session Current Command

Show comprehensive status of the current development session with real-time metrics.

## Command Flow

1. **Check Active Session**:
   - Read `.claude/sessions/.current-session`
   - If empty/missing, inform user no active session

2. **Load Session Data**:
   - Parse session file for metadata
   - Calculate time elapsed
   - Count updates made
   - Check current git status

3. **Display Comprehensive Status**:

   ```
   📊 CURRENT SESSION STATUS
   ═══════════════════════════════════════
   
   Session: [Name]
   Type: [Feature|Bug Fix|Refactor|Research] | Priority: [Level]
   File: .claude/sessions/[filename]
   
   ⏱️  TIMING
   • Started: [time] ([X hours Y minutes ago])
   • Last Update: [time] ([Z minutes ago])
   • Estimated Duration: [X hours] ([% complete])
   
   🎯 OBJECTIVES
   Primary Goal: [objective]
   • Status: [In Progress|Blocked|Near Complete]
   • Progress: [|||||||---] 70%
   
   Secondary Goals: [X of Y completed]
   ✓ [Completed goal]
   ○ [Pending goal]
   
   📈 METRICS
   • Updates Made: [count]
   • Code Changes: [X files modified, Y added, Z deleted]
   • Task Progress: [X completed, Y in progress, Z pending]
   • Quality Score: [current] ([+/-X] from start)
   • Test Coverage: [current]% ([+/-X%] from start)
   
   🔄 RECENT ACTIVITY
   [Last 3 updates with timestamps]
   • [time]: [update summary]
   • [time]: [update summary]
   • [time]: [update summary]
   
   ⚠️  ACTIVE ISSUES
   • [Any current blockers]
   • [Pending decisions]
   
   💡 SUGGESTED ACTIONS
   Based on current progress:
   • [Suggested next step]
   • [Time for a break if >90 min]
   • [Update session if >30 min since last]
   
   Available Commands:
   • /session-update "message" - Add progress update
   • /session-metrics - Detailed metrics view
   • /session-end - End with summary
   ```

4. **Smart Suggestions**:
   - If no update for >30 min: Suggest update
   - If session >3 hours: Suggest break
   - If objectives met: Suggest session end
   - If blocked: Suggest pivot or help

## Special Features

### Real-time Git Integration
```bash
# Show uncommitted changes summary
git status --porcelain | wc -l
git diff --stat | tail -1
```

### Task Progress Calculation
- Parse TODO items from session
- Check TodoWrite tool status
- Calculate completion percentage

### Time Management
- Warn if approaching estimated duration
- Track productive vs blocked time
- Suggest optimal break times

## Usage Examples

```bash
# Basic status check
/session-current

# With verbose metrics
/session-current --verbose

# Quick summary only
/session-current --brief
```