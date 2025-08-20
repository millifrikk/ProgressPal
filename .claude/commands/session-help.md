# Enhanced Session Management Help

## 🚀 Quick Start

```bash
/session-start "feature-name"     # Start new session
/session-update "progress"        # Add update
/session-current                  # Check status
/session-end                      # End with summary
```

## 📚 Complete Command Reference

### Core Commands

#### `/session-start [name] [options]`
Start a new development session with professional tracking.

**Options:**
- `--type=[feature|bugfix|refactor|research|deployment]` - Session type
- `--priority=[critical|high|medium|low]` - Priority level
- `--relates-to=[issue-id]` - Link to issue/ticket
- `--continues=[session-id]` - Continue previous session

**Examples:**
```bash
/session-start "add-authentication"
/session-start "fix-memory-leak" --type=bugfix --priority=critical
/session-start "refactor-api" --continues=2025-01-15-api-design
```

#### `/session-update [message] [options]`
Add structured update to current session.

**Options:**
- `--type=[progress|blocker|decision|milestone|pivot]` - Update type
- `--phase=[planning|implementation|testing|review|deployment]`

**Examples:**
```bash
/session-update "Implemented JWT authentication"
/session-update "Database connection failing" --type=blocker
/session-update "Chose PostgreSQL over MySQL" --type=decision
/session-update "All tests passing" --type=milestone
```

#### `/session-current [options]`
Display current session status with metrics.

**Options:**
- `--verbose` - Show detailed metrics
- `--brief` - Quick summary only

#### `/session-end [summary] [options]`
End session with comprehensive summary and analytics.

**Options:**
- `--status=[completed|partial|blocked]` - Final status
- `--report-only` - Generate report without ending

#### `/session-list [options]`
List all sessions with filtering and analytics.

**Options:**
- `--type=[type]` - Filter by session type
- `--completed` - Only completed sessions
- `--this-week` - Recent sessions only
- `--search=[term]` - Search in session names

### Analytics Commands

#### `/session-analytics [options]`
View comprehensive analytics across all sessions.

**Options:**
- `--period=[week|month|quarter|all]`
- `--metrics=[productivity|quality|patterns]`
- `--export=[json|csv|html]`

#### `/session-metrics`
View current session metrics in detail.

### Utility Commands

#### `/session-template [type]`
Create session from template.

**Available Templates:**
- `feature` - New feature development
- `bugfix` - Bug fixing session
- `refactor` - Code refactoring
- `research` - Technology research
- `deployment` - Production deployment

## 📊 Understanding Session Metrics

### Productivity Metrics
- **Efficiency Rate**: Active time / Total time
- **Focus Score**: Based on context switches
- **Completion Rate**: Tasks completed / Total tasks

### Quality Metrics
- **Code Quality**: Linting and complexity scores
- **Test Coverage**: Percentage and trends
- **Performance**: API response times

### Time Metrics
- **Duration**: Total session time
- **Active Time**: Time between updates
- **Break Time**: Inactive periods

## 🎯 Best Practices

### 1. Session Planning
- Start with clear, measurable objectives
- Use appropriate session type and priority
- Link to related issues or previous sessions
- Set realistic time estimates

### 2. Regular Updates
- Update every 30-60 minutes
- Use specific update types (blocker, decision, etc.)
- Include enough detail for future reference
- Track decisions with rationale

### 3. Effective Endings
- Always end sessions properly
- Review objectives achievement
- Document lessons learned
- Plan next steps

## 🔧 Configuration

### Session Settings
Located in `.claude/config/session-settings.json`:

```json
{
  "updateInterval": 30,        // Minutes between update reminders
  "maxSessionDuration": 240,   // Maximum session length (minutes)
  "autoSaveMetrics": true,     // Auto-collect metrics
  "templates": {
    "useDefault": true,        // Use templates by default
    "customPath": "./templates" // Custom template location
  }
}
```

### Analytics Settings
Control what metrics are tracked:

```json
{
  "trackGitMetrics": true,
  "trackTestMetrics": true,
  "trackPerformance": true,
  "anonymizeData": false
}
```

## 📈 Session Templates

### Feature Template Structure
```markdown
- Metadata (type, priority, relations)
- Objectives (primary, secondary, scope)
- Technical approach
- Task breakdown
- Risk assessment
- Quality standards
```

### Bug Fix Template Structure
```markdown
- Bug details (summary, reproduction)
- Investigation notes
- Root cause analysis
- Fix strategy
- Testing plan
- Regression prevention
```

## 🔍 Troubleshooting

### "No active session"
- Check `.claude/sessions/.current-session`
- Start new session with `/session-start`

### "Metrics not updating"
- Ensure git is initialized
- Check file permissions
- Verify tool installations

### "Session already exists"
- End current session first
- Or use `--force` flag (not recommended)

## 💡 Pro Tips

1. **Use Templates**: Start with templates for consistency
2. **Tag Decisions**: Always use `--type=decision` for important choices
3. **Track Blockers**: Document immediately with `--type=blocker`
4. **Review Analytics**: Weekly analytics review improves estimates
5. **Link Sessions**: Use `--continues` for multi-session features

## 📱 Integration

### Git Integration
- Commits can reference session ID
- PR descriptions auto-generated
- Branch names from session names

### IDE Integration
- VS Code extension available
- Auto-session on project open
- Inline metric displays

## 🎓 Examples by Scenario

### Starting a New Feature
```bash
/session-start "user-dashboard" --type=feature --priority=high
# Work on feature...
/session-update "Created dashboard component structure"
/session-update "Implemented data fetching" 
/session-update "Added responsive design" --type=milestone
/session-end "Dashboard MVP complete"
```

### Debugging Production Issue
```bash
/session-start "prod-memory-leak" --type=bugfix --priority=critical
/session-update "Reproduced issue locally"
/session-update "Found leak in WebSocket handler" --type=decision
/session-update "Fix deployed to staging" --type=milestone
/session-end "Memory leak resolved"
```

### Research Session
```bash
/session-start "evaluate-redis" --type=research
/session-update "Benchmarked Redis vs current solution"
/session-update "Redis 3x faster for our use case" --type=decision
/session-end "Recommendation: Adopt Redis for caching"
```

---

For more information, see the full SESSION_MANAGEMENT_GUIDE.md