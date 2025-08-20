# Changelog Add Command

Add a new entry to the project changelog with proper categorization and formatting.

## Command Flow

1. **Parse Entry Type**:
   - Determine category: Added|Changed|Fixed|Deprecated|Removed|Security
   - Extract description from $ARGUMENTS
   - Auto-detect from git changes if not specified

2. **Format Entry**:
   - Add to appropriate section under [Unreleased]
   - Include timestamp and session reference
   - Link to related commits/PRs if available

3. **Update Changelog**:
   ```markdown
   ## [Unreleased]
   
   ### [Category]
   - [Description] ([Session: YYYY-MM-DD-HHMM])
   ```

## Entry Categories

### Added
- New features or functionality
- New API endpoints
- New components or services
- New documentation

### Changed
- Updates to existing functionality
- Refactoring or improvements
- Performance optimizations
- UI/UX enhancements

### Fixed
- Bug fixes
- Issue resolutions
- Error corrections
- Performance problems

### Deprecated
- Features marked for removal
- Old API versions
- Legacy code paths

### Removed
- Deleted features
- Removed dependencies
- Cleaned up code

### Security
- Security fixes
- Authentication updates
- Vulnerability patches
- Access control changes

## Usage Examples

```bash
# Simple addition
/changelog-add "Added user profile export feature"

# With category specified
/changelog-add "Fixed memory leak in WebSocket handler" --category=fixed

# With metadata
/changelog-add "Added Redis caching layer" --issue=#123 --pr=#456

# Auto-detect from current changes
/changelog-add --auto-detect
```

## Smart Features

### Auto-Detection
If no description provided, analyze:
- Recent git commits
- Changed files
- Current session objectives

### Duplicate Prevention
- Check if similar entry exists
- Warn about potential duplicates
- Merge related changes

### Session Integration
- Link to current session
- Include relevant metrics
- Reference decision logs

## Best Practices

1. **Be Specific**: Clear, actionable descriptions
2. **User-Focused**: Describe impact, not implementation
3. **Consistent Style**: Follow existing changelog format
4. **Link Context**: Reference issues, PRs, sessions
5. **Regular Updates**: Add entries as you complete work