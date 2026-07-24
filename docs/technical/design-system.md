# GitHub Cruise - Material Design 3 System

This document outlines the comprehensive design system implemented for the GitHub Cruise Android app, following **Material Design 3** and **Google's official design guidelines**.

## Design Principles

The app follows these core Material Design 3 principles:

1. **Consistency** - Unified design language across all screens
2. **Hierarchy** - Clear visual hierarchy using typography and spacing
3. **Accessibility** - Proper content descriptions and color contrast
4. **Responsive** - Adapts to different screen sizes and orientations
5. **Themeable** - Full support for light and dark modes

## Design System Structure

### 1. **Spacing System** (`ui/theme/Spacing.kt`)

Based on the **4dp grid system**, all spacing values are multiples of 4dp:

```kotlin
Spacing.extraSmall  // 4dp  - Very tight spacing
Spacing.small       // 8dp  - Compact layouts
Spacing.medium      // 16dp - Default spacing (most common)
Spacing.large       // 24dp - Section separation
Spacing.extraLarge  // 32dp - Major section breaks
```

**Usage Example:**
```kotlin
Column(
    modifier = Modifier.padding(Spacing.medium)
) {
    Text("Title")
    Spacer(modifier = Modifier.height(Spacing.small))
    Text("Content")
}
```

### 2. **Elevation System** (`ui/theme/Elevation.kt`)

Defines 6 elevation levels for depth perception:

```kotlin
Elevation.level0  // 0dp  - Flat on surface
Elevation.level1  // 1dp  - Cards at rest
Elevation.level2  // 3dp  - Buttons, FABs
Elevation.level3  // 6dp  - Dialogs, menus
Elevation.level4  // 8dp  - Navigation drawer
Elevation.level5  // 12dp - Bottom sheets
```

**Semantic Aliases:**
```kotlin
Elevation.card              // Same as level1
Elevation.appBar            // Same as level2
Elevation.dialog            // Same as level3
Elevation.navigationDrawer  // Same as level4
```

### 3. **Shape System** (`ui/theme/Shape.kt`)

Corner radius tokens for component shapes:

```kotlin
AppShapes.none        // 0dp  - Sharp corners
AppShapes.extraSmall  // 4dp  - Minimal rounding
AppShapes.small       // 8dp  - Small components
AppShapes.medium      // 12dp - Cards, dialogs (default)
AppShapes.large       // 16dp - Large cards
AppShapes.extraLarge  // 28dp - Prominent components
AppShapes.full        // 50%  - Fully rounded (pills)
```

**Semantic Aliases:**
```kotlin
AppShapes.button    // medium
AppShapes.card      // medium
AppShapes.dialog    // extraLarge
AppShapes.searchBar // full
AppShapes.avatar    // full
```

### 4. **Color System** (`ui/theme/Color.kt`)

#### Brand Colors
```kotlin
PrimaryPurple  // #893788 - Main brand color
PrimaryPink    // #DB3B88 - Secondary brand color
```

#### Neutral Palette
```kotlin
White          // #FFFFFF
Gray50         // #FDFBFD - Very light gray
Gray100        // #F1E5EB - Light gray
Gray200        // #F5F7FE - Surface gray
Gray400        // #B0CCCC - Medium gray
Gray600        // #636366 - Dark gray
Gray900        // #1C1C1E - Near black
```

#### Semantic Colors
```kotlin
// Surface colors
SurfaceLight   // Gray200
SurfaceWhite   // White
SurfaceDark    // Gray900

// Text colors
TextPrimary    // Gray900
TextSecondary  // Gray400
TextOnPrimary  // Gray50
TextOnDark     // White

// Icon colors
IconPrimary    // Gray600
IconOnPrimary  // White

// State colors
ErrorDefault   // #F0A992 - Coral
```

#### Programming Language Colors
Consistent colors for programming language chips/tags based on GitHub's language colors:

```kotlin
// Popular languages
LanguageKotlin      // #A97BFF - Purple
LanguageJava        // #B07219 - Orange-brown
LanguagePython      // #3572A5 - Blue
LanguageJavaScript  // #F1E05A - Yellow
LanguageTypeScript  // #2B7489 - Teal
LanguageSwift       // #FFAC45 - Orange
LanguageGo          // #00ADD8 - Cyan
LanguageRust        // #DEA584 - Tan
LanguageCPlusPlus   // #F34B7D - Pink
LanguageCSharp      // #178600 - Green
LanguageRuby        // #701516 - Dark red
LanguagePHP         // #4F5D95 - Purple-blue
LanguageDart        // #00B4AB - Turquoise
LanguageDefault     // #586069 - Gray (fallback)
```

**Usage Example:**
```kotlin
// In AppChip.kt
AppChip(
    text = "Kotlin",
    backgroundColor = LanguageKotlin,  // Uses theme color
    textColor = Color.White
)
```

### 5. **Typography System** (`ui/theme/TextStyle.kt`)

Complete Material Design 3 type scale:

#### Display Styles (Large, expressive text)
```kotlin
MaterialTheme.typography.displayLarge   // 57sp, Bold
MaterialTheme.typography.displayMedium  // 45sp, Bold
MaterialTheme.typography.displaySmall   // 36sp, Bold
```

#### Headline Styles (High emphasis)
```kotlin
MaterialTheme.typography.headlineLarge  // 32sp, SemiBold
MaterialTheme.typography.headlineMedium // 28sp, SemiBold
MaterialTheme.typography.headlineSmall  // 24sp, SemiBold
```

#### Title Styles (Medium emphasis)
```kotlin
MaterialTheme.typography.titleLarge     // 22sp, SemiBold
MaterialTheme.typography.titleMedium    // 16sp, Medium
MaterialTheme.typography.titleSmall     // 14sp, Medium
```

#### Body Styles (Main content)
```kotlin
MaterialTheme.typography.bodyLarge      // 16sp, Normal
MaterialTheme.typography.bodyMedium     // 14sp, Normal
MaterialTheme.typography.bodySmall      // 12sp, Normal
```

#### Label Styles (UI components)
```kotlin
MaterialTheme.typography.labelLarge     // 14sp, Medium
MaterialTheme.typography.labelMedium    // 12sp, Medium
MaterialTheme.typography.labelSmall     // 11sp, Medium
```

### 6. **Dimensions** (`ui/theme/Dimension.kt`)

Component-specific sizing:

```kotlin
// Icons
Dimension.iconSizeSmall       // 18dp
Dimension.iconSizeMedium      // 24dp
Dimension.iconSizeLarge       // 32dp
Dimension.iconSizeExtraLarge  // 48dp

// Avatars
Dimension.avatarSizeSmall     // 32dp
Dimension.avatarSizeMedium    // 48dp
Dimension.avatarSizeLarge     // 64dp
Dimension.avatarSizeExtraLarge // 96dp

// Buttons
Dimension.buttonHeightSmall   // 32dp
Dimension.buttonHeightMedium  // 40dp
Dimension.buttonHeightLarge   // 48dp

// AppBar
Dimension.appBarHeight        // 64dp
Dimension.appBarIconSize      // 24dp

// Progress
Dimension.progressIndicatorSize   // 48dp
Dimension.progressIndicatorStroke // 4dp

// Divider
Dimension.dividerThickness    // 1dp
```

## 🧩 Reusable Components

### AppActionBarView
App bar with back navigation and title.

```kotlin
AppActionBarView(
    modifier = Modifier.fillMaxWidth(),
    headerText = "Screen Title",
    showBackButton = true,
    onBackClick = { navController.popBackStack() }
)
```

### NetworkImageView
Async image loader with circular avatar shape.

```kotlin
NetworkImageView(
    imageUrl = user.avatarUrl,
    contentDescription = "User avatar",
    modifier = Modifier.size(Dimension.avatarSizeMedium),
    shape = AppShapes.avatar
)
```

### SharedProgressIndicator
Centered loading spinner.

```kotlin
SharedProgressIndicator()
```

### SharedErrorView
Error state with icon and message.

```kotlin
SharedErrorView(errorMessage = "Failed to load data")
```

### HorizontalLineView
Material divider line.

```kotlin
HorizontalLineView()
```

### StateContentBox
Smart container that handles loading, error, and content states.

```kotlin
StateContentBox(
    isLoading = viewState.isLoading,
    errorMessage = viewState.errorMessage
) {
    // Content goes here
}
```

## 📱 Screen Examples

### Splash Screen
- Brand gradient background (PrimaryPurple → PrimaryPink)
- Smooth scale animation
- Display typography for title

### Users List Screen
- AppBar with no back button
- Search bar with pill shape
- List with pagination
- Proper spacing using tokens

### User Repository Screen
- Profile header with avatar
- Repository list with filtering
- Cards with medium shape

## Best Practices

### 1. Always Use Design Tokens
**Don't:**
```kotlin
Modifier.padding(16.dp)
```

**Do:**
```kotlin
Modifier.padding(Spacing.medium)
```

### 2. Use Semantic Colors
**Don't:**
```kotlin
Color(0xFF893788)
```

**Do:**
```kotlin
MaterialTheme.colorScheme.primary
// or
PrimaryPurple  // when you need the exact color
```

### 3. Use Typography Scale
**Don't:**
```kotlin
Text(
    text = "Title",
    fontSize = 22.sp,
    fontWeight = FontWeight.SemiBold
)
```

**Do:**
```kotlin
Text(
    text = "Title",
    style = MaterialTheme.typography.titleLarge
)
```

### 4. Use Shape Tokens
**Don't:**
```kotlin
Modifier.clip(RoundedCornerShape(12.dp))
```

**Do:**
```kotlin
Modifier.clip(AppShapes.card)
```

### 5. Use Elevation Tokens
**Don't:**
```kotlin
Modifier.shadow(3.dp)
```

**Do:**
```kotlin
Modifier.shadow(Elevation.appBar)
```

## 🌗 Dark Mode Support

The app automatically adapts to system dark mode preference. All components use `MaterialTheme.colorScheme` which switches between light and dark palettes automatically.

**Light Theme:**
- Background: White
- Primary: PrimaryPurple
- Text: Gray900

**Dark Theme:**
- Background: Gray900
- Primary: Gray50
- Text: White

## ♿ Accessibility

All components follow accessibility best practices:

1. **Content Descriptions** - All icons and images have proper descriptions
2. **Color Contrast** - Meets WCAG AA standards
3. **Touch Targets** - Minimum 48dp for clickable elements
4. **Text Scaling** - Supports user font size preferences

## Migration Guide

If you're updating old code to use the new design system:

1. **Replace hardcoded dp values** with `Spacing` tokens
2. **Replace hardcoded colors** with `MaterialTheme.colorScheme` or semantic color names
3. **Replace RoundedCornerShape** with `AppShapes` tokens
4. **Replace hardcoded elevations** with `Elevation` tokens
5. **Replace hardcoded sizes** with `Dimension` tokens

The old color names are marked with `@Deprecated` for backward compatibility but should be migrated.

## 📚 References

- [Material Design 3](https://m3.material.io/)
- [Material Design Color System](https://m3.material.io/styles/color/roles)
- [Material Design Typography](https://m3.material.io/styles/typography/type-scale-tokens)
- [Material Design Shape](https://m3.material.io/styles/shape/shape-scale-tokens)
- [Material Design Elevation](https://m3.material.io/styles/elevation/tokens)
- [Material Design Spacing](https://m3.material.io/foundations/layout/applying-layout/spacing)

---

**Last Updated:** 2024-06-10
**Design System Version:** 1.0.0
