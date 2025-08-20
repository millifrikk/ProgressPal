# Changelog View Command

Display the changelog with filtering and formatting options.

## Command Flow

1. **Load Changelog**:
   - Read CHANGELOG.md
   - Parse sections and versions

2. **Apply Filters**:
   - Version range
   - Category filter
   - Date range
   - Search terms

3. **Display Options**:
   - Full changelog
   - Specific version
   - Unreleased only
   - Summary view

## Display Formats

### Default View
```
📝 ECHOES CHANGELOG
═══════════════════════════════════════

## [Unreleased]
• Added: Enhanced session management system
• Changed: Improved orchestrator prompt
• Fixed: Session file formatting issues

## [0.3.0] - 2025-08-02 (3 days ago)
### Major Changes
• 🎨 Complete UI/UX modernization
• 🤖 Natural Vertex AI conversations
• 🐛 Fixed critical API endpoints

[More versions...]
```

### Summary View
```
📊 CHANGELOG SUMMARY
═══════════════════════════════════════

Total Releases: 3
Latest Version: 0.3.0 (2025-08-02)
Next Version: 0.4.0 (suggested)

Recent Activity (Last 30 days):
• Added: 12 features
• Changed: 8 improvements  
• Fixed: 15 bugs
• Security: 2 updates

Top Contributors:
• Session: feature-ui-enhancement (5 changes)
• Session: bugfix-api-endpoints (4 changes)
```

### Version Details
```
## Version 0.3.0 - Released 2025-08-02

Duration: 14 hours development time
Sessions: 3 sessions contributing
Impact: High (UI overhaul)

### Statistics
• Files Changed: 236
• Lines Added: 5,420
• Lines Removed: 3,180
• Test Coverage: +7%

### Changes by Category
Added (4):
  ✓ Professional landing page
  ✓ Marketing components
  ✓ Design system (144 properties)
  ✓ UI/UX enhancements

Changed (3):
  ✓ UI theme modernization
  ✓ Vertex AI responses
  ✓ Voice recorder cleanup

Fixed (3):
  ✓ Life story endpoints
  ✓ Database field mismatches
  ✓ Dashboard loading failures
```

## Usage Examples

```bash
# View full changelog
/changelog-view

# View specific version
/changelog-view 0.3.0

# View unreleased changes only
/changelog-view --unreleased

# Filter by category
/changelog-view --category=added
/changelog-view --category=fixed

# Search for specific terms
/changelog-view --search="authentication"

# View summary statistics
/changelog-view --summary

# Compare versions
/changelog-view --compare=0.2.0..0.3.0

# Export formatted
/changelog-view --format=markdown > RELEASE_NOTES.md
```

## Smart Features

### Change Impact Analysis
- Categorize changes by impact level
- Highlight breaking changes
- Show dependency updates

### Session Attribution
- Link changes to development sessions
- Show contributor patterns
- Track feature velocity

### Release Predictions
- Suggest next version based on changes
- Estimate release readiness
- Identify missing documentation

## Filtering Options

### By Version
- `--version=X.Y.Z` - Specific version
- `--since=X.Y.Z` - All changes since version
- `--range=X.Y.Z..A.B.C` - Version range

### By Date
- `--since-date=YYYY-MM-DD`
- `--until-date=YYYY-MM-DD`
- `--last-30-days`

### By Category
- `--added` - New features only
- `--fixed` - Bug fixes only
- `--security` - Security updates only

### By Impact
- `--breaking` - Breaking changes only
- `--major` - Major features only
- `--minor` - Minor updates only