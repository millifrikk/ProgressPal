# Session Analytics Command

Analyze session data to provide insights and improve development efficiency.

## Command Flow

1. **Load Analytics Data**:
   - Read from `.claude/analytics/sessions.json`
   - Parse all archived sessions
   - Build metrics database

2. **Generate Analytics Report**:

   ```markdown
   # 📊 Session Analytics Report
   
   **Analysis Period**: [First Session] to [Latest Session]  
   **Total Sessions**: [Count]  
   **Total Development Time**: [Hours]
   
   ---
   
   ## 🎯 Productivity Metrics
   
   ### Session Efficiency
   | Metric | Average | Best | Worst | Trend |
   |--------|---------|------|-------|-------|
   | Duration | 3.5h | 2h | 8h | →️ |
   | Objectives Met | 78% | 100% | 25% | ↗️ |
   | Task Completion | 82% | 100% | 40% | ↗️ |
   | Focus Score | 7.2/10 | 9/10 | 4/10 | ↗️ |
   
   ### Time Analysis
   - **Most Productive Hours**: 10 AM - 12 PM (based on completion rate)
   - **Average Session Length**: 3.5 hours
   - **Optimal Session Length**: 2-4 hours (highest efficiency)
   
   ### Task Estimation Accuracy
   - **Overestimated**: 45% of tasks (by avg 32%)
   - **Underestimated**: 35% of tasks (by avg 48%)
   - **Accurate (±20%)**: 20% of tasks
   - **Improvement Trend**: +12% accuracy over last 10 sessions
   
   ---
   
   ## 📈 Quality Metrics Trends
   
   ### Code Quality Evolution
   ```
   Score |     ╱╲    ╱╲
    8.5  |    ╱  ╲__╱  ╲___
    8.0  |___╱            
    7.5  |
         └─────────────────────→ Sessions
   ```
   
   ### Test Coverage Progression
   ```
   Coverage |        ____╱▔▔▔
       75% |    ___╱
       70% |___╱
       65% |
           └─────────────────────→ Sessions
   ```
   
   ---
   
   ## 🔍 Pattern Analysis
   
   ### Common Blockers (by frequency)
   1. **API Integration Issues** - 23% of sessions
      - Average resolution time: 45 minutes
      - Common cause: Authentication/credentials
   
   2. **Type Errors** - 18% of sessions
      - Average resolution time: 20 minutes
      - Common cause: Interface mismatches
   
   3. **Database Schema Conflicts** - 15% of sessions
      - Average resolution time: 35 minutes
      - Common cause: Migration issues
   
   ### Success Patterns
   - Sessions with clear objectives: 85% completion rate
   - Sessions with time estimates: 72% on-time delivery
   - Sessions after break (>12h): 91% objective completion
   
   ### Technology Performance
   | Technology | Sessions | Avg Efficiency | Issues Rate |
   |------------|----------|----------------|-------------|
   | Python/FastAPI | 45 | 84% | 12% |
   | React/TypeScript | 38 | 78% | 18% |
   | Database/SQL | 22 | 71% | 24% |
   | AI Integration | 15 | 65% | 31% |
   
   ---
   
   ## 🎓 Learning Insights
   
   ### Skill Development
   - **Fastest Improving**: API Development (+24% efficiency)
   - **Needs Focus**: AI Integration (31% issue rate)
   - **Most Consistent**: Frontend Development (σ=0.12)
   
   ### Decision Outcomes
   - **Total Decisions Tracked**: 67
   - **Successful Decisions**: 52 (78%)
   - **Decisions Reversed**: 8 (12%)
   - **Pending Evaluation**: 7 (10%)
   
   ### Knowledge Base Growth
   - **Patterns Documented**: 34
   - **Solutions Reused**: 19 times
   - **Time Saved by Reuse**: ~12 hours
   
   ---
   
   ## 💡 Recommendations
   
   ### For Productivity
   1. **Optimal Session Planning**:
      - Schedule 3-4 hour blocks
      - Start with clear objectives
      - Include 20% buffer time
   
   2. **Reduce Common Blockers**:
      - Create API integration checklist
      - Maintain type definition library
      - Use database migration tools
   
   3. **Improve Estimation**:
      - Break tasks < 30 minutes
      - Add 40% buffer for AI tasks
      - Review similar past tasks
   
   ### For Quality
   1. **Maintain Standards**:
      - Run quality checks every hour
      - Keep sessions < 5 hours
      - Take breaks every 90 minutes
   
   2. **Testing Focus**:
      - Write tests during implementation
      - Maintain > 75% coverage
      - Fix failing tests immediately
   
   ---
   
   ## 📅 Historical Comparison
   
   ### This Month vs Last Month
   | Metric | Last Month | This Month | Change |
   |--------|------------|------------|--------|
   | Sessions | 22 | 28 | +27% |
   | Avg Duration | 4.1h | 3.5h | -15% |
   | Completion Rate | 71% | 78% | +7% |
   | Quality Score | 7.8 | 8.3 | +0.5 |
   
   ### Session Type Distribution
   ```
   Feature      : ████████████ 40%
   Bug Fix      : ████████ 25%
   Refactor     : ██████ 20%
   Research     : ███ 10%
   Documentation: ██ 5%
   ```
   
   ---
   
   ## 🔮 Predictive Analytics
   
   ### Next Session Suggestions
   Based on patterns and current state:
   1. **Type**: Refactor (technical debt accumulation)
   2. **Duration**: 3-4 hours (optimal for refactoring)
   3. **Focus**: Service layer optimization
   4. **Risk**: Medium (dependent system changes)
   
   ### Upcoming Challenges
   - Test coverage dropping below 75% threshold
   - Performance metrics need attention
   - Documentation lag detected
   
   ---
   
   *Report generated: [timestamp]*
   *Next analysis scheduled: After 5 more sessions*
   ```

3. **Export Options**:
   - JSON format for external tools
   - CSV for spreadsheet analysis
   - Markdown for documentation
   - HTML for presentation

## Usage Examples

```bash
# Generate full analytics
/session-analytics

# Analyze specific period
/session-analytics --from="2025-01-01" --to="2025-01-31"

# Focus on specific metrics
/session-analytics --metrics=productivity,quality

# Compare periods
/session-analytics --compare="last-month,this-month"

# Export format
/session-analytics --export=csv --output=./reports/
```

## Analytics Database Schema

```json
{
  "sessions": [{
    "id": "session-id",
    "type": "feature",
    "duration_minutes": 210,
    "objectives_met": 0.8,
    "tasks_completed": 12,
    "quality_delta": 0.3,
    "coverage_delta": 5.2,
    "blockers": ["api-auth"],
    "decisions": ["chose-pattern-x"],
    "timestamp": "ISO-8601"
  }],
  "patterns": {
    "productivity": {},
    "quality": {},
    "blockers": {}
  }
}
```