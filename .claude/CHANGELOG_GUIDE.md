# Changelog Management Guide

## Overview

The changelog system provides automatic tracking of all development changes, creating a comprehensive history of the project's evolution. It integrates with the session management system to capture changes as they happen.

## Quick Start

### Automatic Changelog Updates
When you end a session, completed features and fixes are automatically added:
```bash
/session-end
# ✅ Automatically updates CHANGELOG.md with session achievements
```

### Manual Changelog Commands
```bash
/changelog-add "Added new dashboard widget"
/changelog-view                             # View full changelog
/changelog-release 0.4.0                    # Create new release
```

## Changelog Structure

### Standard Format (Keep a Changelog)
```markdown
## [Unreleased]

### Added
- New features or functionality

### Changed
- Changes to existing functionality

### Fixed
- Bug fixes

### Deprecated
- Features marked for removal

### Removed
- Removed features

### Security
- Security fixes or updates
```

### Version Format (Semantic Versioning)
- **MAJOR.MINOR.PATCH** (e.g., 1.2.3)
- **MAJOR**: Breaking changes
- **MINOR**: New features (backwards compatible)
- **PATCH**: Bug fixes (backwards compatible)

## Integration with Sessions

### Automatic Tracking
When a session ends, the system:
1. Analyzes completed objectives
2. Identifies feature additions, changes, and fixes
3. Automatically creates appropriate changelog entries
4. Links entries to session for traceability

### Session Metadata in Changelog
```markdown
### Added
- User authentication system (Session: 2025-08-03-1430-auth-feature)
- Password reset functionality (Session: 2025-08-03-1430-auth-feature)

### Fixed
- Memory leak in WebSocket handler (Session: 2025-08-04-0900-bugfix-websocket)
```

## Best Practices

### 1. Writing Good Changelog Entries

**DO:**
- Write from the user's perspective
- Be specific but concise
- Group related changes
- Include issue/PR references

**DON'T:**
- Use technical jargon
- Write vague descriptions
- Include implementation details
- Forget the impact

### Examples

**Good:**
```markdown
### Added
- Export user stories as PDF with custom formatting options
- Email notifications for story completion with family sharing links
- Bulk operations for managing multiple stories at once

### Fixed
- Dashboard no longer crashes when viewing stories with special characters
- Audio playback now works correctly on iOS devices
- Fixed timezone display issues in conversation history
```

**Bad:**
```markdown
### Added
- Added new stuff to dashboard
- Some PDF thing
- Did email feature

### Fixed
- Fixed bug
- Audio works now
- Time bug
```

### 2. Categorization Guidelines

#### Added
- New features
- New API endpoints
- New UI components
- New configuration options
- New documentation

#### Changed
- Feature enhancements
- Performance improvements
- UI/UX updates
- API modifications (non-breaking)
- Dependency updates

#### Fixed
- Bug fixes
- Issue resolutions
- Performance problems
- UI glitches
- Data inconsistencies

#### Security
- Vulnerability patches
- Authentication improvements
- Authorization fixes
- Encryption updates
- Security policy changes

#### Deprecated
- Features scheduled for removal
- Old API versions
- Legacy configuration options
- Outdated dependencies

#### Removed
- Deleted features
- Removed endpoints
- Cleaned up code
- Removed dependencies

## Release Process

### 1. Pre-Release Checklist
- [ ] All tests passing
- [ ] Documentation updated
- [ ] Changelog entries reviewed
- [ ] Version number determined
- [ ] Migration guide prepared (if needed)

### 2. Creating a Release
```bash
# Review unreleased changes
/changelog-view --unreleased

# Auto-suggest version based on changes
/changelog-release --auto

# Or specify version
/changelog-release 0.4.0
```

### 3. Post-Release Tasks
- [ ] Tag git repository
- [ ] Update version in package files
- [ ] Generate release notes
- [ ] Notify stakeholders
- [ ] Deploy to production

## Advanced Features

### 1. Changelog Analytics
Track development patterns:
- Feature velocity over time
- Bug fix frequency
- Breaking change patterns
- Release cadence

### 2. Auto-Generated Release Notes
The system can generate:
- Executive summaries
- Technical details
- Migration guides
- API change documentation

### 3. Integration Points
- Git hooks for commit messages
- PR templates with changelog sections
- CI/CD changelog validation
- Documentation generation

## Troubleshooting

### Common Issues

**Duplicate Entries**
- System prevents automatic duplicates
- Manual entries should be reviewed
- Use search before adding

**Version Conflicts**
- Follow semantic versioning strictly
- Don't skip versions
- Use pre-release versions for testing

**Missing Changes**
- Check session was ended properly
- Verify objectives were marked complete
- Can manually add with `/changelog-add`

## Examples by Scenario

### Feature Development
```bash
/session-start "add-export-feature" --type=feature
# ... development work ...
/session-end
# ✅ Automatically adds to changelog:
# ### Added
# - PDF export functionality for user stories (Session: 2025-08-03-1430-feature)
```

### Bug Fix
```bash
/session-start "fix-audio-playback" --type=bugfix
# ... fix implementation ...
/session-end
# ✅ Automatically adds to changelog:
# ### Fixed
# - Audio playback issues on mobile devices (Session: 2025-08-03-1600-bugfix)
```

### Manual Addition
```bash
/changelog-add "Added Redis caching for improved performance" --category=changed
```

### Creating Release
```bash
# Review changes
/changelog-view --unreleased

# Create release
/changelog-release 0.4.0

# Git tag (suggested command)
git tag -a v0.4.0 -m "Release version 0.4.0"
git push origin v0.4.0
```

## Configuration

### Changelog Settings
`.claude/config/changelog-settings.json`:
```json
{
  "autoUpdate": true,           // Auto-update on session end
  "linkSessions": true,         // Include session references
  "categories": [               // Enabled categories
    "Added",
    "Changed",
    "Fixed",
    "Security",
    "Deprecated",
    "Removed"
  ],
  "versioningScheme": "semver", // Versioning standard
  "changelogPath": "./CHANGELOG.md"
}
```

## Tips for Success

1. **Regular Updates**: Don't wait to update changelog
2. **User Focus**: Write for your users, not developers
3. **Consistency**: Use same terminology throughout
4. **Traceability**: Link to sessions, issues, PRs
5. **Review**: Check changelog before releases

The changelog is a living document that tells the story of your project's evolution. Keep it updated, clear, and useful!