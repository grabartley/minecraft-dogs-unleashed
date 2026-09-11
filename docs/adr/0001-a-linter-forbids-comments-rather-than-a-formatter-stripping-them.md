# ADR 0001: A linter forbids comments rather than a formatter stripping them

- **Status:** Accepted
- **Date:** 2026-09-11

## Context

[`../standards.md`](../standards.md), the engineering standard shared across this family of mods and imported into this repository alongside this record, says the source carries no comments at all, not explanatory ones, not javadoc, not "why" ones. That rule has only ever been prose. Nothing checked it, and here it was not being kept: an audit of the 365 Java files across the four source sets found roughly 1,600 comment lines spread over 161 files, javadoc included.

So this repository needed two things where a repository already keeping the rule needs only one. The comments had to come out, and something had to stop them coming back. They were deliberately separate changes, because they carry very different risks and merging them would have hidden the second inside the first.

Removing them was the dangerous half. Comment removal cannot be done with a regular expression: `"https://example.com/a//b"`, `'"'` and `"/* not a comment */"` all defeat naive matching, and the corruption is silent. It needs a character-level state machine that knows when it is inside a string, a char literal, or a text block. When such a lexer is right it rewrites the file invisibly, which is pleasant. When it is wrong it eats a line of code inside a branch nobody tests, and the result still compiles, still passes the suite, and reaches review as one deletion among sixteen hundred. That strip was therefore done once, under a mechanical proof that every removed line was a comment, a blank line a comment vacated, or a code line whose content survives verbatim in the diff.

That risk is exactly why the *enforcement* must not work the same way.

Spotless already runs on every build, so it was the obvious place to put the gate. It is the wrong one. Spotless has no assertion primitive: every step is a rewriter, and `spotlessCheck` means nothing more than "run the rewriter and diff against disk". Its only comment-aware steps point the other way, `licenseHeader` inserting a comment and `googleJavaFormat().formatJavadoc()` preserving and reflowing javadoc. Enforcing there would mean shipping that same literal-aware lexer and running it unattended on every build, forever, turning a one-off audited risk into a permanent one.

## Decision

Checkstyle fails the build on any comment. It never edits source.

[`../../config/checkstyle/checkstyle.xml`](../../config/checkstyle/checkstyle.xml) carries one rule, a `MatchXpath` query over the parsed comment nodes:

```xml
<property name="query" value="//SINGLE_LINE_COMMENT | //BLOCK_COMMENT_BEGIN"/>
```

The query runs against Checkstyle's real Java AST, so a string literal is never mistaken for a comment and no regular expression is involved anywhere. `SINGLE_LINE_COMMENT` covers `//`, and `BLOCK_COMMENT_BEGIN` covers both `/* */` and `/** */`, so javadoc is not a special case and gets no exemption.

The Gradle `checkstyle` plugin attaches a task per source set, and `check` picks all four up on its own. CI already runs `./gradlew check`, so no workflow change was needed.

`MatchXpath` was chosen over the built-in `TodoComment` with a catch-all format, which was tried first and rejected. Checkstyle hands `TodoComment` the content `"\n"` for a bare `//`, and `"\n".split("\n")` in Java returns an empty array, so an empty comment matches nothing and passes. `MatchXpath` sees the node itself and does not care what is inside it.

This decision was first taken in Too Many Chests, a sibling mod in this family, and is recorded there as its ADR 0034. That repository is not public, so this record deliberately carries the whole argument rather than deferring to it, and the reasoning above stands on its own. The configuration, the wiring and the self-test below are the same in both, so the family converges on one solution rather than several.

## The Gate Tests Itself

The failure mode worth guarding against is a gate that silently stops gating: a mistyped query matches nothing, reports zero violations, and reads exactly like a clean tree. A verification procedure that lives in a document and relies on someone remembering to run it is the same class of failure this record exists to remove.

`NoCommentsRuleTest` runs Checkstyle against the real `config/checkstyle/checkstyle.xml`, over a fixture at `src/test/resources/comment-gate/CommentGateFixture.java.txt`. The fixture is a resource rather than a source file so the gate cannot audit its own test data.

It asserts that each of the six comment forms is reported, that none of the seven literal lines is, and that the total is exactly six, so a query matching too much fails as loudly as one matching too little. The fixture covers a text block holding `//`, `/*` and `*/`, which is the case a regular expression or a hand-written lexer gets wrong.

`config/checkstyle/checkstyle.xml` is declared an input of the `test` task. Without that, editing the query leaves the test up to date and Gradle skips it, which is precisely the silent failure being guarded against.

The test proves the **query**, not the **wiring**. Repointing `checkstyle.configFile` at another file, setting `ignoreFailures`, or adding a source set nobody attaches a task to all leave the suite green while the gate stops covering the code. The query is the part that fails silently and subtly; the wiring fails visibly, in the build file, where review catches it.

Text matching is not an alternative, and this repository is its own counter-example: a `grep` for lines beginning `//`, `/*` or `*` reports `DogAmbienceEffects`, which contains no comment at all and simply wraps a multiplication so `*` starts the line. A grep cannot tell an operator from a comment marker, which is why the rule is expressed against the AST.

## Consequences

A comment in any type declaration fails the build. It fails locally on `./gradlew check` and in CI, at `file:line:column`, tagged `[NoComments]`, with the violation message naming the standard and pointing at this directory for where reasoning belongs instead.

The drawbacks accepted:

- **The build fails instead of fixing itself.** There is no `spotlessApply` equivalent that removes the comment for you. Someone who writes one has to delete it themselves. This is deliberate: the moment a tool is allowed to rewrite source unattended, a bug in it becomes a silent code deletion, and that trade was the whole reason for choosing a linter.
- **Consumers of the published sources jar get no hover documentation.** The build publishes sources via `withSourcesJar()`, and a reader outside this repository has no javadoc and no access to these records unless they find the repository. The API surface is small and the names are long, which is the bet being made.
- **A genuinely surprising piece of code has nowhere in-file to explain itself.** That pressure is intended. It is meant to push the explanation into a record here, or push the code into a shape that does not need one, and it will occasionally be the wrong answer for a truly obscure workaround.
- **Some information really was lost when the existing comments came out.** A few call sites used inline parameter-name comments such as `/* required */ true`, and a voxel-shape table labelled each row with the part of the dog house it described. Those hints are gone rather than relocated, and a reader now has to follow the parameter list or the geometry to recover them.
- **The unit tests resolve a newer Guava than the mod runs against.** Checkstyle on the test classpath pulls Guava 33.2.0-jre, lifting Minecraft's 32.1.2-jre for `testRuntimeClasspath` only. It is test-scoped and never published, and the logic under test is pure functions over plain data by the standard in [`../standards.md`](../standards.md), so the exposure is small. It is a real difference between what the tests exercise and what ships, and the cost of a self-test that runs the same Checkstyle the gate does.
- **A second tool now runs on every build.** Checkstyle is configured with exactly one rule and is not a general style gate. Growing it into one is a separate decision, and it should be resisted while Spotless and `googleJavaFormat` own formatting.
- **A comment-only file with no type declaration is not caught.** A `.java` file whose entire content is `// orphan`, with no `package` statement and no type, audits clean, because an empty compilation unit gives `MatchXpath` no tree to walk. Anything carrying a `package` line is caught, `package-info.java` javadoc included, so this is unreachable in a real source tree, but it is a hole rather than a guarantee.
- **The wider family is still inconsistent.** [`../standards.md`](../standards.md) names five mods at its head, and this repository was the only one of them carrying no copy of it until now. More Arrows and Teleport Effects hold byte-identical copies stating the opposite rule, that comments explain why when the reason is genuinely surprising. More Doors states a no-comments rule and keeps it by memory. Loot Lock is a sixth repository, named in that document as a canonical implementation but absent from its family list, and carries no copy at all. Enforcement now exists in Too Many Chests and here. Reconciling the document with itself, and the remaining repositories with the document, is separate work and is not attempted here.
