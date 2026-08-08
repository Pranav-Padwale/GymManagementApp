# Workspace Rules

## Git & Branching Strategy
- **Never commit or push directly to the `main` branch.**
- Always create a dedicated feature branch (e.g. `feature/<feature-name>`) BEFORE implementing changes.
- Push changes strictly to the feature branch on remote (`git push -u origin feature/<feature-name>`).
- Open a Pull Request on GitHub to merge feature branches into `main`.
