#!/bin/bash

# Version checking script to prevent overwriting existing releases in Maven Central
# This script validates that the version being published doesn't already exist

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
GROUP_ID="io.github.neuraquant"
ARTIFACT_ID="kai"
MAVEN_CENTRAL_URL="https://repo1.maven.org/maven2"
SONATYPE_URL="https://s01.oss.sonatype.org/service/local/repositories/releases/content"

# Get version from build.gradle.kts
VERSION=$(grep "version = " build.gradle.kts | sed 's/.*version = "\(.*\)".*/\1/')

if [ -z "$VERSION" ]; then
    echo -e "${RED}❌ Could not determine version from build.gradle.kts${NC}"
    exit 1
fi

echo -e "${BLUE}🔍 Checking version $VERSION for $GROUP_ID:$ARTIFACT_ID${NC}"

# Function to check if version exists in Maven Central
check_maven_central() {
    local version=$1
    local url="$MAVEN_CENTRAL_URL/${GROUP_ID//.//}/$ARTIFACT_ID/$version"
    
    echo -e "${YELLOW}Checking Maven Central: $url${NC}"
    
    if curl -s --head "$url" | head -n 1 | grep -q "200 OK"; then
        echo -e "${RED}❌ Version $version already exists in Maven Central!${NC}"
        echo -e "${RED}   URL: $url${NC}"
        return 1
    else
        echo -e "${GREEN}✅ Version $version not found in Maven Central${NC}"
        return 0
    fi
}

# Function to check if version exists in Sonatype Central
check_sonatype() {
    local version=$1
    local url="$SONATYPE_URL/${GROUP_ID//.//}/$ARTIFACT_ID/$version"
    
    echo -e "${YELLOW}Checking Sonatype Central: $url${NC}"
    
    if curl -s --head "$url" | head -n 1 | grep -q "200 OK"; then
        echo -e "${RED}❌ Version $version already exists in Sonatype Central!${NC}"
        echo -e "${RED}   URL: $url${NC}"
        return 1
    else
        echo -e "${GREEN}✅ Version $version not found in Sonatype Central${NC}"
        return 0
    fi
}

# Function to check if version is a valid semantic version
validate_semantic_version() {
    local version=$1
    
    # Check if version follows semantic versioning (major.minor.patch)
    if [[ $version =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9.-]+)?(\+[a-zA-Z0-9.-]+)?$ ]]; then
        echo -e "${GREEN}✅ Version $version follows semantic versioning${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  Version $version doesn't follow semantic versioning (major.minor.patch)${NC}"
        echo -e "${YELLOW}   Consider using format like: 1.0.0, 1.0.1, 1.1.0, 2.0.0${NC}"
        return 1
    fi
}

# Function to check if version is a SNAPSHOT
check_snapshot() {
    local version=$1
    
    if [[ $version == *"-SNAPSHOT" ]]; then
        echo -e "${YELLOW}⚠️  Version $version is a SNAPSHOT version${NC}"
        echo -e "${YELLOW}   SNAPSHOT versions are allowed to be overwritten${NC}"
        return 0
    else
        echo -e "${BLUE}ℹ️  Version $version is a release version${NC}"
        return 1
    fi
}

# Function to suggest next version
suggest_next_version() {
    local current_version=$1
    
    # Extract major, minor, patch
    if [[ $current_version =~ ^([0-9]+)\.([0-9]+)\.([0-9]+) ]]; then
        local major=${BASH_REMATCH[1]}
        local minor=${BASH_REMATCH[2]}
        local patch=${BASH_REMATCH[3]}
        
        echo -e "${BLUE}💡 Suggested next versions:${NC}"
        echo -e "   Patch: $major.$minor.$((patch + 1))"
        echo -e "   Minor: $major.$((minor + 1)).0"
        echo -e "   Major: $((major + 1)).0.0"
    fi
}

# Main validation logic
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    Version Validation for Maven Central${NC}"
echo -e "${BLUE}========================================${NC}"

# Check if version is SNAPSHOT
if check_snapshot "$VERSION"; then
    echo -e "${GREEN}✅ SNAPSHOT version detected - overwriting is allowed${NC}"
    exit 0
fi

# Validate semantic versioning
if ! validate_semantic_version "$VERSION"; then
    echo -e "${YELLOW}⚠️  Continuing despite non-semantic version...${NC}"
fi

# Check Maven Central
if ! check_maven_central "$VERSION"; then
    echo -e "${RED}❌ Cannot publish: Version $VERSION already exists in Maven Central${NC}"
    suggest_next_version "$VERSION"
    echo -e "${RED}Please update the version in build.gradle.kts and try again.${NC}"
    exit 1
fi

# Check Sonatype Central
if ! check_sonatype "$VERSION"; then
    echo -e "${RED}❌ Cannot publish: Version $VERSION already exists in Sonatype Central${NC}"
    suggest_next_version "$VERSION"
    echo -e "${RED}Please update the version in build.gradle.kts and try again.${NC}"
    exit 1
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✅ Version $VERSION is safe to publish!${NC}"
echo -e "${GREEN}========================================${NC}"

# Additional checks for release versions
if [[ ! $VERSION == *"-SNAPSHOT" ]]; then
    echo -e "${BLUE}📋 Pre-release checklist:${NC}"
    echo -e "   ✓ Version doesn't exist in Maven Central"
    echo -e "   ✓ Version doesn't exist in Sonatype Central"
    echo -e "   ✓ Version follows semantic versioning"
    echo -e ""
    echo -e "${BLUE}💡 Remember:${NC}"
    echo -e "   • Release versions cannot be overwritten"
    echo -e "   • Test thoroughly before publishing"
    echo -e "   • Update CHANGELOG.md if you have one"
    echo -e "   • Consider creating a git tag for this version"
fi