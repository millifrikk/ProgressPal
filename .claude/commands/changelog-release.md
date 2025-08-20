# Changelog Release Command

Create a new release version in the changelog, moving unreleased changes to a versioned section.

## Command Flow

1. **Determine Version**:
   - Parse version from $ARGUMENTS or auto-suggest
   - Follow semantic versioning (MAJOR.MINOR.PATCH)
   - Validate version format

2. **Analyze Changes**:
   - Review [Unreleased] section
   - Suggest version bump based on changes:
     - MAJOR: Breaking changes
     - MINOR: New features (Added)
     - PATCH: Bug fixes (Fixed)

3. **Create Release Section**:
   ```markdown
   ## [X.Y.Z] - YYYY-MM-DD
   
   [Move all unreleased entries here]
   
   ## [Unreleased]
   [Empty sections ready for new changes]
   ```

4. **Generate Release Notes**:
   - Summary of major changes
   - Migration instructions if needed
   - Credits and acknowledgments

## Version Determination

### Semantic Versioning Rules
- **MAJOR** (X.0.0): Incompatible API changes
  - Breaking changes in API
  - Major architectural changes
  - Backward compatibility broken

- **MINOR** (0.X.0): Backwards-compatible functionality
  - New features added
  - New API endpoints
  - Enhancements to existing features

- **PATCH** (0.0.X): Backwards-compatible bug fixes
  - Bug fixes only
  - Security patches
  - Performance improvements

### Auto-Suggestion Logic
Analyze unreleased changes:
```
If any "BREAKING CHANGE" → Major bump
Else if any "Added" entries → Minor bump
Else if only "Fixed" entries → Patch bump
```

## Usage Examples

```bash
# Create release with specific version
/changelog-release 0.4.0

# Auto-suggest version based on changes
/changelog-release --auto

# Create release with custom date
/changelog-release 1.0.0 --date=2025-08-15

# Pre-release version
/changelog-release 0.5.0-beta.1

# Generate release notes
/changelog-release 0.4.0 --with-notes
```

## Release Notes Template

```markdown
# Release Notes - v[X.Y.Z]

## 🎉 Highlights
- [Major feature or improvement]
- [Significant bug fix]
- [Performance enhancement]

## 🚀 What's New
[Detailed list of added features]

## 🔧 Improvements
[List of changes and enhancements]

## 🐛 Bug Fixes
[List of fixed issues]

## ⚠️ Breaking Changes
[Any backward compatibility issues]

## 📦 Dependencies
[Updated dependencies]

## 🙏 Credits
[Contributors and acknowledgments]

## 📋 Full Changelog
[Link to detailed changelog]
```

## Integration Features

### Git Tag Creation
```bash
# Suggest git commands
git tag -a v0.4.0 -m "Release version 0.4.0"
git push origin v0.4.0
```

### PR Description
Generate pull request description from changelog entries

### Documentation Update
Update version references in:
- README.md
- package.json
- setup.py
- Documentation

## Best Practices

1. **Regular Releases**: Don't accumulate too many changes
2. **Clear Communication**: Highlight breaking changes
3. **User Impact**: Focus on what users care about
4. **Testing**: Ensure all tests pass before release
5. **Documentation**: Update docs with new features