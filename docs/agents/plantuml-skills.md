# PlantUML agent skills

This repo vendors PlantUML-capable agent skills under `.agents/skills` so local
agents can discover them from the workspace.

Installed skills:

| Skill | Source | Purpose |
| --- | --- | --- |
| `plantuml-skill` | `Agents365-ai/365-skills` | General PlantUML generation and Kroki-based rendering guidance. |
| `plantuml-renderer` | `a5c-ai/babysitter` | PlantUML rendering workflow for software architecture diagrams. |
| `plantuml-ascii` | `github/awesome-copilot` | Terminal-friendly ASCII output from PlantUML diagrams. |

Not vendored as skills:

| Item | Reason |
| --- | --- |
| `@brainstack/plantuml-mcp` | MCP server package, not a filesystem skill. Install/configure in the agent MCP runtime. |
| `iflow-mcp_2niuhe-mcp_plantuml` | PyPI MCP server package, not a filesystem skill. Install/configure in the agent MCP runtime. |
| `@masayannuu/plantuml` | No public GitHub `SKILL.md` source was found during setup. |
| Andre Lindenberg PlantUML bundle | No public GitHub `SKILL.md` source was found during setup. |
