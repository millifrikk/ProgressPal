# Starting Context Guide for Claude Code

This document provides a systematic approach for Claude Code to quickly gain context when starting a fresh session on the Echoes prototype project.

## Essential Context Reading Order

### 1. Core Project Context
- **CLAUDE.md** - Primary project instructions and development guide

### 2. Current Session Context
- Check `.claude/sessions/.current-session` for active session file
- If active session exists, read that specific file
- If empty/no active session, read the newest session file by date

### 3. Current Project Status (Root Directory)
- Read the **newest** `.md` file in root directory for most current project state

### 4. Structured Documentation (docs/ Directory)
Read key documentation files in order:
- `docs/01-project-overview.md` - Platform vision and architecture
- `docs/02-development-guide.md` - Setup and development workflow
- `docs/03-phase3-intelligent-features.md` - Advanced AI implementation
- `docs/04-implementation-history.md` - Development timeline
- `docs/05-api-reference.md` - API and service documentation
- `docs/06-deployment-operations.md` - Production deployment guide

### 5. Architecture Understanding
- `backend/app/main.py` - FastAPI entry point and routing
- `backend/app/models.py` - Enhanced database schema with Vertex AI integration
- `backend/app/websocket_assistant_manager.py` - WebSocket conversation orchestration
- `backend/app/services/implementations/vertex_ai_assistant_adapter.py` - Vertex AI integration
- `frontend/src/App.tsx` - React frontend entry point

## Context Initialization Command

When starting a fresh session, execute this command sequence:

```bash
# 1. Read core project instructions
Read CLAUDE.md

# 2. Determine and read current session context
Read .claude/sessions/.current-session
# If the file contains a session name, read that specific session file
# If empty, use bash to find and read the newest session file:
# Bash find .claude/sessions/ -name "*.md" -not -name ".current-session" -exec stat -c '%Y %n' {} \; | sort -nr | head -1 | cut -d' ' -f2

# 3. Read newest project status document
# Use bash to find the newest .md file in root directory:
# Bash find . -maxdepth 1 -name "*.md" -not -name "README.md" -not -name "CLAUDE.md" -exec stat -c '%Y %n' {} \; | sort -nr | head -1 | cut -d' ' -f2

# 4. Read structured documentation (in order)
Read docs/01-project-overview.md
Read docs/02-development-guide.md  
Read docs/03-phase3-intelligent-features.md
Read docs/04-implementation-history.md
Read docs/05-api-reference.md
Read docs/06-deployment-operations.md

# 5. Check project structure and current state
LS backend/app/services/
LS frontend/src/components/
Bash git status
```

## Key Context Points to Establish

### Project Status
- Current development phase (MVP, dashboard integration, etc.)
- Recent changes and ongoing work
- Known issues or blockers

### Technical Stack
- Backend: Python 3.9+ with FastAPI
- Frontend: React with TypeScript  
- Database: SQLite with SQLAlchemy
- AI: OpenAI API (Whisper + GPT-4) + Google Vertex AI with Memory Bank
- Phone: Twilio Voice API
- Export: PDF generation (ReportLab + @react-pdf/renderer)

### Development Environment
- Backend runs on port 8000
- Frontend runs on port 3000
- Uses ngrok for Twilio webhook development
- Virtual environment for Python dependencies
- Google Cloud credentials for Vertex AI integration

### Current Architecture
- **Dual AI Provider System**: OpenAI + Vertex AI with circuit breaker failover
- **Phone Interface**: Twilio → FastAPI webhooks → WebSocket conversations
- **Intelligent Conversation**: Real-time theme detection, cross-session continuity
- **Story Workflow**: Conversations → AI drafts → User review/edit → Published stories
- **Family Sharing**: PDF exports, secure invitations, engagement analytics
- **Memory Management**: Vertex AI Memory Bank for semantic story storage

## Quick Status Check Commands

```bash
# Check if development servers are configured  
ls backend/.env
ls frontend/package.json

# Check database state and migrations
ls backend/echoes.db
ls backend/scripts/*.py

# Check Vertex AI configuration
ls backend/service-account-key.json

# Check recent logs and test results
ls -la backend/logs/ | tail -5
ls backend/test_*.py

# Check current git state
git status
git log --oneline -5

# Check service structure
ls backend/app/services/implementations/
ls frontend/src/components/ | head -10
```

## Session Documentation Pattern

When working on tasks, update the current session file in `.claude/sessions/` with:
- **Task objectives** - What needs to be accomplished
- **Changes made** - Files modified, services updated, database migrations
- **Current status** - Progress on each task with completion tracking
- **Technical details** - Implementation approaches, service integrations  
- **Next steps** - Priorities for continued development
- **Blockers/Issues** - Any problems encountered and their solutions

Update `.claude/sessions/.current-session` to reference the active session file name.

## Recommended Workflow

1. **Execute this starting guide** - Gather comprehensive project context
2. **Identify current priorities** - From session files and project status
3. **Understand recent work** - Changes, implementations, ongoing development
4. **Assess system state** - Development environment, database, services
5. **Plan next steps** - Based on project roadmap and immediate needs
6. **Begin development** - With full understanding of system architecture

## Smart Context Features

This command file now dynamically:
- **Finds newest root .md files** - Always reads most current project status
- **Detects active sessions** - Uses `.current-session` or finds newest by date  
- **Reads structured docs** - Comprehensive project knowledge from `docs/`
- **Checks modern architecture** - Vertex AI integration, WebSocket management
- **Validates environment** - Database, services, configuration files

This approach ensures consistent, up-to-date context establishment across all Claude Code sessions without manual file list maintenance.