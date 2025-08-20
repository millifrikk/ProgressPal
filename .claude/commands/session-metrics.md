# Session Metrics Command

Display detailed metrics for the current active session.

## Command Flow

1. **Validate Active Session**:
   - Check `.claude/sessions/.current-session`
   - Load session data and metadata

2. **Collect Current Metrics**:
   - Git statistics
   - Code quality scores
   - Test coverage
   - Performance metrics
   - Task progress

3. **Display Comprehensive Metrics**:

   ```
   📊 SESSION METRICS DASHBOARD
   ═══════════════════════════════════════
   
   Session: [Name] | Type: [Type] | Duration: [X]h [Y]m
   
   📈 CODE METRICS
   ├─ Files Changed........: [X] files
   ├─ Lines Added..........: +[XXX] lines
   ├─ Lines Deleted........: -[XXX] lines
   ├─ Net Change...........: [+/-XXX] lines
   └─ Commits..............: [X] commits
   
   🎯 QUALITY METRICS
   ├─ Code Quality Score...: [X.X]/10 ([+/-X.X] from start)
   ├─ Test Coverage........: [XX.X]% ([+/-X.X]% from start)
   ├─ Lint Warnings........: [X] ([+/-X] from start)
   ├─ Type Errors..........: [X] ([+/-X] from start)
   └─ Complexity...........: [X.X] (avg cyclomatic)
   
   ⚡ PERFORMANCE METRICS
   ├─ Build Time...........: [XXX]ms ([+/-X]% from start)
   ├─ Test Suite Time......: [X.X]s ([+/-X]% from start)
   ├─ Bundle Size..........: [XXX]KB ([+/-X]KB from start)
   └─ API Response Time....: [XXX]ms (avg)
   
   ✅ TASK METRICS
   ├─ Total Tasks..........: [X]
   ├─ Completed............: [X] ([XX]%)
   ├─ In Progress..........: [X] ([XX]%)
   ├─ Blocked..............: [X] ([XX]%)
   └─ Completion Rate......: [XX]% of planned
   
   ⏱️ TIME METRICS
   ├─ Total Duration.......: [X]h [Y]m
   ├─ Active Time..........: [X]h [Y]m ([XX]%)
   ├─ Break Time...........: [X]m ([XX]%)
   ├─ Updates Made.........: [X] (every [Y]m avg)
   └─ Efficiency Score.....: [X.X]/10
   
   📊 PROGRESS VISUALIZATION
   
   Primary Objective:
   [████████████░░░░░░░] 65% Complete
   
   Secondary Objectives:
   Obj 1: [██████████████████] 100% ✓
   Obj 2: [████████░░░░░░░░░░] 45%
   Obj 3: [░░░░░░░░░░░░░░░░░░] 0% (not started)
   
   🔄 RECENT CHANGES (Last 5)
   1. [timestamp] - [file] - [+X/-Y lines]
   2. [timestamp] - [file] - [+X/-Y lines]
   3. [timestamp] - [file] - [+X/-Y lines]
   4. [timestamp] - [file] - [+X/-Y lines]
   5. [timestamp] - [file] - [+X/-Y lines]
   
   💡 INSIGHTS
   • Most productive hour: [time range]
   • Average update interval: [X] minutes
   • Code quality trend: [improving/declining/stable]
   • Estimated completion: [X] hours remaining
   ```

4. **Provide Actionable Insights**:
   - Productivity patterns
   - Quality trends
   - Bottleneck identification
   - Completion estimates

## Metric Collection Methods

### Git Metrics
```bash
git diff --stat
git log --oneline --since="session-start-time"
git status --porcelain | wc -l
```

### Quality Metrics
- Python: `pylint`, `mypy`, `coverage`
- JavaScript: `eslint`, `tsc`, `jest --coverage`

### Performance Metrics
- Build times from tool output
- Test execution times
- Bundle analysis results

## Usage Examples

```bash
# View all metrics
/session-metrics

# Focus on specific category
/session-metrics --category=quality
/session-metrics --category=tasks
/session-metrics --category=performance

# Export metrics
/session-metrics --export=json > metrics.json
/session-metrics --export=csv > metrics.csv

# Compare with session start
/session-metrics --compare=start

# Real-time monitoring mode
/session-metrics --watch
```

## Special Features

### Trend Analysis
- Show metric changes over time
- Identify improvement/degradation patterns
- Predict completion times

### Alerts
- Warn if quality metrics declining
- Alert on efficiency drops
- Suggest breaks based on patterns

### Integration
- Export to monitoring tools
- Generate status reports
- Update project dashboards