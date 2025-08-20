# Changelog Help Command

Display help and usage information for the changelog management system.

## Command Output

```
📝 CHANGELOG MANAGEMENT HELP
═══════════════════════════════════════

The changelog system automatically tracks project changes and maintains
a comprehensive history following Keep a Changelog standards.

## 🚀 Quick Start

Automatic tracking (via session end):
  /session-end              # Auto-adds completed features to changelog

Manual commands:
  /changelog-add "text"     # Add entry to changelog
  /changelog-view           # View changelog
  /changelog-release X.Y.Z  # Create release version

## 📋 Available Commands

### /changelog-add [description] [options]
Add a new entry to the changelog.

Options:
  --category=[added|changed|fixed|security|deprecated|removed]
  --issue=#123              # Link to issue
  --pr=#456                 # Link to pull request
  --auto-detect             # Detect from git changes

Examples:
  /changelog-add "Added user profile export"
  /changelog-add "Fixed memory leak" --category=fixed
  /changelog-add --auto-detect

### /changelog-view [options]
Display the changelog with filtering.

Options:
  --version=X.Y.Z           # Show specific version
  --unreleased              # Show only unreleased
  --category=[type]         # Filter by category
  --summary                 # Show statistics
  --since=X.Y.Z            # Changes since version

Examples:
  /changelog-view
  /changelog-view --unreleased
  /changelog-view --category=fixed
  /changelog-view 0.3.0

### /changelog-release [version] [options]
Create a new release from unreleased changes.

Options:
  --auto                    # Auto-suggest version
  --date=YYYY-MM-DD        # Custom release date
  --with-notes             # Generate release notes

Examples:
  /changelog-release 0.4.0
  /changelog-release --auto
  /changelog-release 1.0.0 --with-notes

## 📚 Categories

- Added: New features or functionality
- Changed: Changes to existing features
- Fixed: Bug fixes
- Security: Security updates
- Deprecated: Features marked for removal
- Removed: Deleted features

## 🔢 Versioning (Semantic)

MAJOR.MINOR.PATCH (e.g., 1.2.3)

- MAJOR: Breaking changes
- MINOR: New features (backwards compatible)
- PATCH: Bug fixes (backwards compatible)

## 🔄 Session Integration

The changelog automatically updates when you end a session:
1. Completed objectives → Added entries
2. Bug fixes → Fixed entries
3. Improvements → Changed entries
4. Security fixes → Security entries

## ✍️ Writing Good Entries

DO:
✓ Write from user perspective
✓ Be specific but concise
✓ Include impact
✓ Reference issues/PRs

DON'T:
✗ Use technical jargon
✗ Write vague descriptions
✗ Include implementation details
✗ Forget user impact

## 📊 Examples

Good entries:
- "Added PDF export with custom formatting for stories"
- "Fixed audio playback issues on iOS Safari"
- "Improved dashboard loading time by 50%"

Bad entries:
- "Fixed bug"
- "Updated code"
- "New feature added"

## 🔧 Configuration

Settings in .claude/config/changelog-settings.json:
- autoUpdate: Enable automatic updates
- linkSessions: Include session references
- categories: Enabled categories
- versioningScheme: Version format

## 💡 Pro Tips

1. Update as you work, not after
2. Group related changes together
3. Review before releases
4. Keep entries user-focused
5. Use consistent terminology

## 📖 Full Documentation

See CHANGELOG_GUIDE.md for comprehensive guide

## 🆘 Common Issues

"How do I fix a wrong entry?"
→ Edit CHANGELOG.md directly or use /changelog-add to override

"What version should I use?"
→ Use /changelog-release --auto for suggestions

"How do I see what changed?"
→ Use /changelog-view --unreleased

---

For more help: See CHANGELOG_GUIDE.md
```