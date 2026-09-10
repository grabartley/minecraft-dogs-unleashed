# ADR 0001: A linter forbids comments rather than a formatter stripping them

- **Status:** Accepted
- **Date:** 2026-09-10

## Context

[`../standards.md`](../standards.md), the engineering standard shared across this family of mods and imported into this repository alongside this record, says the source carries no comments at all, not explanatory ones, not javadoc, not "why" ones. That rule has only ever been prose. Nothing checked it, and here it was not being kept: an audit of the 365 Java files across the four source sets found roughly 1,600 comment lines spread over 161 files, javadoc included.

So this repository needed two things where a repository already keeping the rule needs only one. The comments had to come out, and something had to stop them coming back. They are deliberately separate changes, because they carry very different risks and merging them would hide the second inside the first.

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

This decision was first taken in Too Many Chests, a sibling mod in this family, and is recorded there as its ADR 0034. That repository is not public, so this record deliberately carries the whole argument rather than deferring to it, and the reasoning above stands on its own. The two configurations are byte-identical, so the family converges on one solution rather than several.

`MatchXpath` was chosen over the built-in `TodoComment` with a catch-all format, which was tried first and rejected. Checkstyle hands `TodoComment` the content `"\n"` for a bare `//`, and `"\n".split("\n")` in Java returns an empty array, so an empty comment matches nothing and passes. `MatchXpath` sees the node itself and does not care what is inside it.

## Verifying The Gate

The failure mode worth guarding against is a gate that silently stops gating: a mistyped query matches nothing, reports zero violations, and reads exactly like a clean tree. Nothing in the build detects that, so re-prove the gate by hand after any edit to the query.

Drop this into `src/main/java/com/grahambartley/dogsunleashed/` and run `./gradlew checkstyleMain`, or into another source set and run that set's task. It must report exactly six violations, on lines 3, 6, 15, 17, 19 and 21, and none on the four string and char literals:

```java
package com.grahambartley.dogsunleashed;

/** Javadoc on the type. */
public final class CommentGateFixture {

	// a plain line comment
	private static final String URL = "https://example.com/a//b";

	private static final String STAR = "/* not a comment */";

	private static final char QUOTE = '"';

	private static final String ESCAPED = "a \" // still a string";

	private static final int TRAILING = 1; // a trailing comment

	//

	/* a block comment */
	public int value() {
		return 42; /* an inline block */
	}
}
```

A run that reports five rather than six is the `TodoComment` failure above: the bare `//` on line 17 is the one that gets missed.

Note that a `grep` for lines starting with `//`, `/*` or `*` is not a substitute for this. Run against this repository it reports a file that contains no comment at all, because `DogAmbienceEffects` wraps a multiplication so that `*` begins the line. Text matching cannot tell an operator from a comment marker, which is the whole reason the rule is expressed against the AST.

## Consequences

A comment in any type declaration fails the build. It fails locally on `./gradlew check` and in CI, at `file:line:column`, tagged `[NoComments]`, with the violation message naming the standard and pointing at this directory for where reasoning belongs instead.

The drawbacks accepted:

- **The build fails instead of fixing itself.** There is no `spotlessApply` equivalent that removes the comment for you. Someone who writes one has to delete it themselves. This is deliberate: the moment a tool is allowed to rewrite source unattended, a bug in it becomes a silent code deletion, and that trade was the whole reason for choosing a linter.
- **Consumers of the published sources jar get no hover documentation.** The build publishes sources via `withSourcesJar()`, and a reader outside this repository has no javadoc and no access to these records unless they find the repository. The API surface is small and the names are long, which is the bet being made.
- **A genuinely surprising piece of code has nowhere in-file to explain itself.** That pressure is intended. It is meant to push the explanation into a record here, or push the code into a shape that does not need one, and it will occasionally be the wrong answer for a truly obscure workaround.
- **Some information really was lost when the existing comments came out.** A few call sites used inline parameter-name comments such as `/* required */ true`, and a voxel-shape table labelled each row with the part of the dog house it described. Those hints are gone rather than relocated, and a reader now has to follow the parameter list or the geometry to recover them.
- **A file holding nothing but comments is not caught.** A `.java` file with no type declaration parses to an empty compilation unit, which gives `MatchXpath` no tree to walk, so it reports nothing. A `package-info.java` carrying javadoc *is* caught, because it declares a package, so the practical exposure is a file that would have to be otherwise empty to begin with.
- **A second tool now runs on every build.** Checkstyle is configured with exactly one rule and is not a general style gate. Growing it into one is a separate decision, and it should be resisted while Spotless and `googleJavaFormat` own formatting.
- **The rule is enforced here and in Too Many Chests, not everywhere.** The standard is shared across the mods in this family, and the rest still hold it by memory alone or, in two cases, state a weaker version of it.
