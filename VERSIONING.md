# Versioning Guide for Kai Library

This document explains the versioning strategy and protections in place to prevent overwriting existing packages in Maven Central.

## 🛡️ Protection Mechanisms

### 1. **Pre-Publish Validation**
- **Shell Script**: `./scripts/check-version.sh` - Comprehensive version checking
- **Gradle Task**: `validateVersion` - Built-in validation in the build process
- **GitHub Actions**: Automatic validation before publishing

### 2. **Version Checking**
The system checks:
- ✅ Maven Central for existing versions
- ✅ Sonatype Central for existing versions  
- ✅ Semantic versioning compliance
- ✅ SNAPSHOT vs Release version handling

### 3. **Automatic Failures**
Publishing will **automatically fail** if:
- The version already exists in Maven Central
- The version already exists in Sonatype Central
- The version doesn't follow semantic versioning (with warnings)

## 📋 Versioning Strategy

### Semantic Versioning (SemVer)
We follow [Semantic Versioning](https://semver.org/) format: `MAJOR.MINOR.PATCH`

- **MAJOR**: Breaking changes (incompatible API changes)
- **MINOR**: New features (backward-compatible functionality)
- **PATCH**: Bug fixes (backward-compatible bug fixes)

### Version Types

#### Release Versions
- Format: `1.0.0`, `1.0.1`, `1.1.0`, `2.0.0`
- **Cannot be overwritten** once published
- Must be unique and follow semantic versioning
- Automatically validated before publishing

#### SNAPSHOT Versions
- Format: `1.0.1-SNAPSHOT`, `1.1.0-SNAPSHOT`
- **Can be overwritten** (for development)
- Used for testing and development builds
- Not recommended for production use

## 🚀 Version Management

### Using the Version Bump Script

The `./scripts/bump-version.sh` script provides easy version management:

```bash
# Increment patch version (1.0.0 -> 1.0.1)
./scripts/bump-version.sh patch

# Increment minor version (1.0.0 -> 1.1.0)  
./scripts/bump-version.sh minor

# Increment major version (1.0.0 -> 2.0.0)
./scripts/bump-version.sh major

# Set specific version
./scripts/bump-version.sh set 1.2.3

# Create snapshot version (1.0.0 -> 1.0.1-SNAPSHOT)
./scripts/bump-version.sh snapshot

# Release snapshot (1.0.1-SNAPSHOT -> 1.0.1)
./scripts/bump-version.sh release
```

### Manual Version Updates

Edit `build.gradle.kts`:
```kotlin
version = "1.0.1"  // Update this line
```

## 🔍 Validation Commands

### Check Version Compatibility
```bash
# Comprehensive version checking
./scripts/check-version.sh

# Gradle validation task
./gradlew validateVersion

# Both (recommended)
./scripts/check-version.sh && ./gradlew validateVersion
```

### Test Publishing Locally
```bash
# Test without publishing to Maven Central
./gradlew publishToMavenLocal

# Test with validation
./gradlew validateVersion publishToMavenLocal
```

## 📦 Publishing Workflow

### Automatic Publishing (GitHub Actions)
1. **Push to main branch** triggers the workflow
2. **Version validation** runs automatically
3. **Publishing proceeds** only if version is safe
4. **Failure stops** the process if version exists

### Manual Publishing
```bash
# Full validation and publishing
./gradlew validateVersion publishToSonatype closeAndReleaseSonatypeStagingRepository

# With credentials
./gradlew validateVersion publishToSonatype closeAndReleaseSonatypeStagingRepository \
  -PsonatypeUsername=your-username \
  -PsonatypePassword=your-password
```

## ⚠️ Common Issues and Solutions

### Issue: "Version already exists in Maven Central"
**Solution:**
1. Check what versions exist: Visit [Maven Central](https://search.maven.org/artifact/io.github.neuraquant/kai)
2. Use the bump script: `./scripts/bump-version.sh patch`
3. Commit and push the version change

### Issue: "Version doesn't follow semantic versioning"
**Solution:**
1. Use proper format: `MAJOR.MINOR.PATCH`
2. Examples: `1.0.0`, `1.0.1`, `1.1.0`, `2.0.0`
3. Avoid: `1.0`, `v1.0.0`, `1.0.0-beta`

### Issue: "SNAPSHOT version detected"
**Solution:**
- For development: Keep SNAPSHOT versions
- For release: Use `./scripts/bump-version.sh release`

## 🎯 Best Practices

### 1. **Version Planning**
- Plan version numbers before development
- Use SNAPSHOT versions for development
- Reserve release versions for stable code

### 2. **Pre-Release Testing**
```bash
# Test with SNAPSHOT version
./scripts/bump-version.sh snapshot
./gradlew publishToMavenLocal
# Test your application with the SNAPSHOT version

# When ready, release
./scripts/bump-version.sh release
git add build.gradle.kts
git commit -m "Release version 1.0.1"
git push origin main
```

### 3. **Git Tagging**
The bump script can create git tags:
```bash
./scripts/bump-version.sh patch
# Answer 'y' when prompted to create git tag
```

### 4. **Changelog Management**
Consider maintaining a `CHANGELOG.md`:
```markdown
## [1.0.1] - 2024-01-15
### Fixed
- Bug fix description

## [1.0.0] - 2024-01-01
### Added
- Initial release
```

## 🔧 Configuration

### Environment Variables
```bash
# For manual publishing
export SONATYPE_USERNAME="your-username"
export SONATYPE_PASSWORD="your-password"
```

### Gradle Properties
Create `~/.gradle/gradle.properties`:
```properties
sonatypeUsername=your-username
sonatypePassword=your-password
```

## 📊 Version History

Track your version history:
```bash
# View version changes
git log --oneline --grep="version\|release" --all

# View tags
git tag -l

# Compare versions
git diff v1.0.0..v1.0.1
```

## 🚨 Emergency Procedures

### If You Accidentally Try to Overwrite
1. **Stop the process** immediately (Ctrl+C)
2. **Check what happened**: Visit Maven Central
3. **Update version**: Use bump script
4. **Test locally**: `./gradlew validateVersion`
5. **Retry publishing**: With new version

### If Version Validation Fails
1. **Check network**: Ensure internet connection
2. **Verify version**: Run `./scripts/check-version.sh`
3. **Check Maven Central**: Manually verify version doesn't exist
4. **Contact maintainers**: If issue persists

## 📚 Additional Resources

- [Semantic Versioning](https://semver.org/)
- [Maven Central](https://search.maven.org/)
- [Sonatype Central](https://central.sonatype.com/)
- [Gradle Publishing Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)

---

**Remember**: Once a version is published to Maven Central, it **cannot be overwritten**. Always validate versions before publishing!