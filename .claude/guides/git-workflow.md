# Git 工作流规范

## 提交消息格式

```
<type>: <description>

<optional body>
```

**类型：** feat, fix, refactor, docs, test, chore, perf, ci

## 分支命名

- 功能分支：`feature/功能名称`
- Bug 修复：`fix/问题描述`
- 重构：`refactor/模块名称`
- 文档：`docs/更新内容`

## Pull Request 流程

1. 分析完整提交历史
2. 使用 `git diff [base-branch]...HEAD` 查看所有更改
3. 起草全面的 PR 摘要
4. 包含带有 TODO 的测试计划
5. 新分支使用 `-u` 标志推送
