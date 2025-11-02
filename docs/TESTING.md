# Testing Guide

This project uses JUnit 5 for testing with JaCoCo for code coverage and Mockito for mocking dependencies.

## JUnit 5 (Unit Tests)

Unit tests for testing logic and configuration values.

### Location
`src/test/java/`

### Running JUnit Tests
```bash
./gradlew test
```

### Running Tests with Coverage
```bash
./gradlew check
```

This runs tests and verifies code coverage meets the 80% threshold.

### Test Strategy

Since Minecraft mod code requires runtime initialization, tests focus on:
- **Configuration validation**: Testing that values are within valid ranges
- **Constant verification**: Ensuring mod IDs, paths, and identifiers are correct
- **Logic testing**: Validating calculations and utility methods with full coverage
- **Class existence**: Verifying classes can be loaded without triggering Minecraft bootstrap

### Example Tests
- `HuskyEntityTest.java` - Testing entity attribute configuration values
- `HuskyModelTest.java` - Testing resource path formats
- `HuskyRendererTest.java` - Testing renderer configuration values
- `EntityUtilsTest.java` - Testing utility methods with Mockito mocks
- `DogsUnleashedTest.java` - Testing mod constants and identifiers

### Writing Tests

**Testing configuration values:**
```java
@Test
@DisplayName("Husky health should be higher than vanilla wolf")
void testHuskyHealth() {
	double huskyHealth = 25.0;
	double wolfHealth = 20.0;
	assertTrue(huskyHealth > wolfHealth);
}
```

**Using Mockito for dependencies:**
```java
@Test
@DisplayName("Method should handle null input")
void testNullInput(@Mock SomeClass mock) {
	when(mock.getValue()).thenReturn(null);
	assertThrows(IllegalArgumentException.class, () -> {
		// test code
	});
}
```

### Important Notes
- Tests cannot instantiate Minecraft entities or use game classes that require runtime context
- Use value assertions and configuration validation instead of object instantiation
- Mock dependencies with Mockito when testing utility methods
- Avoid triggering Minecraft's registry system in tests

## Code Coverage

This project uses JaCoCo for code coverage reporting.

### Coverage Requirements
- **Minimum Overall Coverage**: 80%
- **No exclusions** - all Java code is subject to coverage requirements
- Tests validate configuration values, constants, and logical assertions
- Minecraft-dependent classes are tested via value validation rather than instantiation

### Viewing Coverage Reports
After running `./gradlew check`:
- **HTML Report**: `build/reports/jacoco/test/html/index.html`
- **XML Report**: `build/reports/jacoco/test/jacocoTestReport.xml`

### Coverage Enforcement
The build will **fail** if coverage drops below 80%. This ensures:
- All configuration values are validated
- New utility code has tests
- Refactoring doesn't remove test coverage
- Quality standards are maintained

## Test Structure

```
src/
└── test/
	└── java/
		├── com/grahambartley/
		│   ├── DogsUnleashedTest.java
		│   ├── DogsUnleashedClientTest.java
		│   ├── ModEntitiesTest.java
		│   ├── ModItemsTest.java
		│   ├── ModSpawnsTest.java
		│   ├── entity/
		│   │   └── HuskyEntityTest.java
		│   ├── model/
		│   │   └── HuskyModelTest.java
		│   ├── render/
		│   │   └── HuskyRendererTest.java
		│   └── util/
		│       └── EntityUtilsTest.java
```

## CI/CD Integration

Tests run automatically on every push and pull request via GitHub Actions.

### GitHub Actions Workflow
The workflow (`.github/workflows/gradle.yml`) runs:
1. Code formatting check (Spotless)
2. JUnit tests with coverage verification (`./gradlew check`)
3. Test result reporting (published to PR checks)
4. Coverage report publishing

### Test Reports
- Test results are published as GitHub Check annotations
- Failed tests show up directly in pull requests
- Coverage reports are added as PR comments
- Detailed test summary available in the Actions tab

### Viewing Results
- **PR Checks**: Test results appear in the "Checks" tab of pull requests
- **PR Comments**: Coverage reports show up as automated comments
- **Actions Tab**: Full logs and test reports available in the repository's Actions tab
- **Artifacts**: Coverage HTML reports available as downloadable artifacts
- **Local Reports**: `build/reports/tests/test/index.html` after running `./gradlew test`

## Current Test Coverage

- **Total Tests**: 19 passing tests
- **Coverage**: 80%+ (enforced)
- **Test Classes**: 9 test classes covering all main code
