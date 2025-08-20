# Enhanced Session List Command

Display all development sessions with filtering, sorting, and analytics summary.

## Command Flow

1. **Scan Sessions Directory**:
   - Check `.claude/sessions/` exists
   - List all `.md` files (exclude .current-session)
   - Parse metadata from each file

2. **Categorize Sessions**:
   - Group by type (Feature, Bug Fix, Refactor, etc.)
   - Sort by date (newest first by default)
   - Mark current active session

3. **Display Enhanced List**:

   ```
   📚 DEVELOPMENT SESSIONS
   ═══════════════════════════════════════
   
   Active Session: [current-session-name] ⚡
   Total Sessions: [count] | Total Time: [X hours]
   
   🗂️  RECENT SESSIONS (Last 10)
   
   ┌─────────────────────┬───────────┬──────────┬────────────┬─────────┐
   │ Session             │ Type      │ Duration │ Completion │ Quality │
   ├─────────────────────┼───────────┼──────────┼────────────┼─────────┤
   │ ⚡ auth-refactor    │ Feature   │ 2h 15m   │ 75%        │ +0.3    │
   │ fix-memory-leak     │ Bug Fix   │ 3h 45m   │ 100% ✓     │ +0.1    │
   │ api-optimization    │ Refactor  │ 4h 20m   │ 100% ✓     │ +0.5    │
   │ vertex-ai-research  │ Research  │ 1h 30m   │ N/A        │ -       │
   └─────────────────────┴───────────┴──────────┴────────────┴─────────┘
   
   📊 SESSION ANALYTICS
   
   By Type:
   • Feature:  ████████████ 12 sessions (45%)
   • Bug Fix:  ████████ 8 sessions (30%)
   • Refactor: ████ 4 sessions (15%)
   • Research: ██ 2 sessions (10%)
   
   By Success Rate:
   • Completed: 18 (75%)
   • Partial: 4 (17%)
   • Blocked: 2 (8%)
   
   📈 TRENDS
   • Average Duration: 3h 15m
   • Completion Rate: 75% (↑ from last month)
   • Quality Impact: +0.3 average
   • Most Productive Day: Tuesday
   
   🔍 FILTER OPTIONS
   Use flags to filter results:
   --type=feature      Show only feature sessions
   --completed         Show only completed sessions
   --this-week        Show this week's sessions
   --search="auth"    Search in session names
   
   📄 VIEW SESSION
   To view a specific session:
   /session-view [session-filename]
   ```

4. **Provide Session Summary**:
   - Total development time
   - Success rate metrics
   - Common patterns
   - Productivity insights

## Display Options

### Sorting
- `--sort=date` (default) - Newest first
- `--sort=duration` - Longest first
- `--sort=completion` - By completion rate
- `--sort=quality` - By quality impact

### Filtering
- `--type=[type]` - Filter by session type
- `--status=[completed|partial|blocked]`
- `--date-range=[this-week|this-month|custom]`
- `--min-duration=[minutes]`

### Output Formats
- Default: Interactive table
- `--format=simple` - Basic list
- `--format=detailed` - Full metadata
- `--format=json` - Machine readable

## Usage Examples

```bash
# List all sessions (default view)
/session-list

# Show only completed features
/session-list --type=feature --completed

# Sessions from this week
/session-list --this-week

# Search for specific topic
/session-list --search="authentication"

# Export as JSON
/session-list --format=json > sessions.json
```

## Special Features

### Quick Actions
After listing, provide shortcuts:
- `1-9`: Quick open session by number
- `a`: View analytics dashboard
- `n`: Start new session

### Pattern Recognition
- Identify most productive times
- Show success patterns
- Highlight problem areas