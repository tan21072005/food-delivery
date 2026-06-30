$commands = @(
  "npx --yes skills@latest add mattpocock/skills --skill setup,grill-with-docs,to-prd,to-issues,diagnose,improve-codebase-architecture,tdd -y",
  "npx --yes skills@latest add mattpocock/skills --skill setup,grill-with-docs,to-prd,to-issues,diagnose,improve-codebase-architecture,tdd -g -y",
  "npx --yes skills@latest add supabase/agent-skills --skill supabase,supabase-postgres-best-practices -y",
  "npx --yes skills@latest add supabase/agent-skills --skill supabase,supabase-postgres-best-practices -g -y",
  "npx --yes skills@latest add obra/superpowers --skill using-superpowers,brainstorming,writing-plans,executing-plans,systematic-debugging,verification-before-completion -y",
  "npx --yes skills@latest add obra/superpowers --skill using-superpowers,brainstorming,writing-plans,executing-plans,systematic-debugging,verification-before-completion -g -y",
  "npx --yes skills@latest add leonxlnx/taste-skill --skill full-output-enforcement,image-to-code -y",
  "npx --yes skills@latest add leonxlnx/taste-skill --skill full-output-enforcement,image-to-code -g -y",
  "npx --yes skills@latest add sleekdotdesign/agent-skills --skill sleek-design-mobile-apps -y",
  "npx --yes skills@latest add sleekdotdesign/agent-skills --skill sleek-design-mobile-apps -g -y",
  "npx --yes skills@latest add firebase/agent-skills --skill firebase-auth-basics,firebase-basics -y",
  "npx --yes skills@latest add firebase/agent-skills --skill firebase-auth-basics,firebase-basics -g -y",
  "npx --yes skills@latest add anthropics/skills --skill pptx,docx,xlsx,skill-creator -y",
  "npx --yes skills@latest add anthropics/skills --skill pptx,docx,xlsx,skill-creator -g -y",
  "npx --yes skills@latest add Agents365-ai/365-skills --skill plantuml-skill -y",
  "npx --yes skills@latest add Agents365-ai/365-skills --skill plantuml-skill -g -y",
  "npx --yes skills@latest add a5c-ai/babysitter --skill plantuml-renderer -y",
  "npx --yes skills@latest add a5c-ai/babysitter --skill plantuml-renderer -g -y",
  "npx --yes skills@latest add github/awesome-copilot --skill plantuml-ascii -y",
  "npx --yes skills@latest add github/awesome-copilot --skill plantuml-ascii -g -y",
  "npx --yes skills@latest add sanyuan0704/code-review-expert -y",
  "npx --yes skills@latest add sanyuan0704/code-review-expert -g -y",
  "npx --yes skills@latest add yoanbernabeu/supabase-pentest-skills --skill supabase-detect -y",
  "npx --yes skills@latest add yoanbernabeu/supabase-pentest-skills --skill supabase-detect -g -y"
)

foreach ($cmd in $commands) {
    Write-Host "Running: $cmd"
    Invoke-Expression $cmd
}
