# LazyColumn Performance & Optimization

## Purpose

`LazyColumn` is the Jetpack Compose equivalent of `RecyclerView`. It composes and renders only items currently visible on screen. Following core performance best practices ensures smooth 60fps / 120fps scrolling without dropped frames or jank.

---

## The 4 Core Optimization Rules

### 1. Always Provide Stable & Unique Keys
Without keys, Compose matches items by position. Adding or deleting an item forces every single item below it to recompose. With keys, Compose tracks items by identity and reuses composition.

```kotlin
// BAD - Uses position index as identity
items(users) { user -> UserCard(user) }

// GOOD - Uses unique, immutable ID
items(
    items = users,
    key = { user -> user.id }
) { user ->
    UserCard(user)
}
```

### 2. Specify `contentType` for Heterogeneous Lists
`contentType` allows Compose to reuse compositions only between items of the same layout type (e.g. headers vs. cards vs. ads), drastically reducing composition time.

```kotlin
LazyColumn {
    item(contentType = "HEADER") { HeaderItem() }
    items(
        items = repos,
        key = { it.id },
        contentType = { "REPO_CARD" }
    ) { repo ->
        RepoCard(repo)
    }
}
```

### 3. Use `derivedStateOf` for Scroll State Observations
Reading `lazyListState.firstVisibleItemIndex` directly in a Composable triggers recomposition on **every single pixel scrolled**. `derivedStateOf` updates only when the resulting boolean changes.

```kotlin
val listState = rememberLazyListState()

// Only triggers recomposition when switching between true and false
val showScrollToTop by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 5 }
}

if (showScrollToTop) {
    ScrollToTopButton(onClick = { /* scroll */ })
}
```

### 4. Avoid Allocations & Heavy Calculations in Item Scope
Never format dates, sort collections, or perform IO calculations inside the Composable item scope. Do it in the ViewModel or domain use cases before emitting the UI state.

---

## Handling Pagination & Infinite Scrolling

In our codebase, pagination is managed via [`PaginationManager.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/domain/pagination/PaginationManager.kt) and triggered as the list approaches the end:

```kotlin
val listState = rememberLazyListState()

LazyColumn(state = listState) {
    items(repositories, key = { it.id }) { repo ->
        RepositoryItem(repo)
    }
}

// Trigger next page when user scrolls near the end
val shouldLoadMore by remember {
    derivedStateOf {
        val total = listState.layoutInfo.totalItemsCount
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        lastVisible >= total - 5 && total > 0
    }
}

LaunchedEffect(shouldLoadMore) {
    if (shouldLoadMore) viewModel.loadNextPage()
}
```

See real pagination logic in [`RepositorySearchViewModel.kt:L59`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/repositorysearch/RepositorySearchViewModel.kt#L59).

---

## Quick Performance Checklist

```
✓ Stable, unique keys provided for every dynamic item
✓ contentType specified when rendering multiple item designs
✓ derivedStateOf used for scroll-position driven UI
✓ Images sized and cached with Coil
✓ Heavy data parsing done in ViewModel / Use Case (not in item body)
✓ Tested on Release builds (Debug builds run unoptimized without R8)
```

---

## Code Reference & Project Examples

- **LazyColumn List Implementation**: [`SamplesListScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/SamplesListScreen.kt)
- **Pagination Manager**: [`PaginationManager.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/domain/pagination/PaginationManager.kt)
- **Production Paginated Search**: [`RepositorySearchViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/repositorysearch/RepositorySearchViewModel.kt)
