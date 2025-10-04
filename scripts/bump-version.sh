#!/bin/bash

# Version bumping script for Kai library
# This script helps increment version numbers following semantic versioning

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get current version from build.gradle.kts
CURRENT_VERSION=$(grep "version = " build.gradle.kts | sed 's/.*version = "\(.*\)".*/\1/')

if [ -z "$CURRENT_VERSION" ]; then
    echo -e "${RED}❌ Could not determine current version from build.gradle.kts${NC}"
    exit 1
fi

echo -e "${BLUE}Current version: $CURRENT_VERSION${NC}"

# Function to extract version parts
extract_version_parts() {
    local version=$1
    if [[ $version =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)(-.*)?$ ]]; then
        MAJOR=${BASH_REMATCH[1]}
        MINOR=${BASH_REMATCH[2]}
        PATCH=${BASH_REMATCH[3]}
        SUFFIX=${BASH_REMATCH[4]:-""}
    else
        echo -e "${RED}❌ Invalid version format: $version${NC}"
        exit 1
    fi
}

# Function to update version in build.gradle.kts
update_version() {
    local new_version=$1
    local file="build.gradle.kts"
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s/version = \".*\"/version = \"$new_version\"/" "$file"
    else
        # Linux
        sed -i "s/version = \".*\"/version = \"$new_version\"/" "$file"
    fi
    
    echo -e "${GREEN}✅ Updated version to $new_version in $file${NC}"
}

# Function to create git tag
create_git_tag() {
    local version=$1
    local tag="v$version"
    
    if git tag -l | grep -q "^$tag$"; then
        echo -e "${YELLOW}⚠️  Tag $tag already exists${NC}"
        return 1
    else
        git tag -a "$tag" -m "Release version $version"
        echo -e "${GREEN}✅ Created git tag: $tag${NC}"
        return 0
    fi
}

# Function to show help
show_help() {
    echo -e "${BLUE}Usage: $0 [OPTION]${NC}"
    echo ""
    echo "Options:"
    echo "  patch    Increment patch version (1.0.0 -> 1.0.1)"
    echo "  minor    Increment minor version (1.0.0 -> 1.1.0)"
    echo "  major    Increment major version (1.0.0 -> 2.0.0)"
    echo "  set X.Y.Z Set specific version (e.g., set 1.2.3)"
    echo "  snapshot Create snapshot version (1.0.0 -> 1.0.1-SNAPSHOT)"
    echo "  release  Remove snapshot suffix (1.0.1-SNAPSHOT -> 1.0.1)"
    echo "  help     Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 patch"
    echo "  $0 minor"
    echo "  $0 set 2.0.0"
    echo "  $0 snapshot"
    echo "  $0 release"
}

# Main logic
case "${1:-help}" in
    "patch")
        extract_version_parts "$CURRENT_VERSION"
        NEW_VERSION="$MAJOR.$MINOR.$((PATCH + 1))$SUFFIX"
        update_version "$NEW_VERSION"
        echo -e "${GREEN}🎉 Bumped to patch version: $NEW_VERSION${NC}"
        ;;
    
    "minor")
        extract_version_parts "$CURRENT_VERSION"
        NEW_VERSION="$MAJOR.$((MINOR + 1)).0$SUFFIX"
        update_version "$NEW_VERSION"
        echo -e "${GREEN}🎉 Bumped to minor version: $NEW_VERSION${NC}"
        ;;
    
    "major")
        extract_version_parts "$CURRENT_VERSION"
        NEW_VERSION="$((MAJOR + 1)).0.0$SUFFIX"
        update_version "$NEW_VERSION"
        echo -e "${GREEN}🎉 Bumped to major version: $NEW_VERSION${NC}"
        ;;
    
    "set")
        if [ -z "$2" ]; then
            echo -e "${RED}❌ Please provide a version number${NC}"
            echo "Usage: $0 set X.Y.Z"
            exit 1
        fi
        NEW_VERSION="$2"
        update_version "$NEW_VERSION"
        echo -e "${GREEN}🎉 Set version to: $NEW_VERSION${NC}"
        ;;
    
    "snapshot")
        if [[ $CURRENT_VERSION == *"-SNAPSHOT" ]]; then
            echo -e "${YELLOW}⚠️  Version is already a snapshot: $CURRENT_VERSION${NC}"
        else
            extract_version_parts "$CURRENT_VERSION"
            NEW_VERSION="$MAJOR.$MINOR.$((PATCH + 1))-SNAPSHOT"
            update_version "$NEW_VERSION"
            echo -e "${GREEN}🎉 Created snapshot version: $NEW_VERSION${NC}"
        fi
        ;;
    
    "release")
        if [[ $CURRENT_VERSION == *"-SNAPSHOT" ]]; then
            NEW_VERSION=${CURRENT_VERSION%-SNAPSHOT}
            update_version "$NEW_VERSION"
            echo -e "${GREEN}🎉 Released version: $NEW_VERSION${NC}"
        else
            echo -e "${YELLOW}⚠️  Version is not a snapshot: $CURRENT_VERSION${NC}"
        fi
        ;;
    
    "help"|"-h"|"--help")
        show_help
        ;;
    
    *)
        echo -e "${RED}❌ Unknown option: $1${NC}"
        show_help
        exit 1
        ;;
esac

# Ask if user wants to create a git tag for release versions
if [[ "${1:-help}" == "patch" || "${1:-help}" == "minor" || "${1:-help}" == "major" || "${1:-help}" == "set" ]]; then
    if [[ ! $NEW_VERSION == *"-SNAPSHOT" ]]; then
        echo ""
        read -p "Create git tag for version $NEW_VERSION? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            if create_git_tag "$NEW_VERSION"; then
                echo -e "${GREEN}✅ Git tag created successfully${NC}"
                echo -e "${BLUE}💡 To push the tag: git push origin v$NEW_VERSION${NC}"
            fi
        fi
    fi
fi

echo ""
echo -e "${BLUE}📋 Next steps:${NC}"
echo -e "  1. Review the changes: git diff"
echo -e "  2. Commit the version change: git add build.gradle.kts && git commit -m \"Bump version to $NEW_VERSION\""
echo -e "  3. Push to trigger publishing: git push origin main"
echo -e "  4. Or test locally: ./gradlew publishToMavenLocal"