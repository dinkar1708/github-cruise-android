# Android Technical Knowledge Base & Code Samples

Comprehensive technical documentation and architecture reference for Android development with Jetpack Compose, Kotlin, and modern Android architecture.

---

## 1. Beginner Topics

### Kotlin Fundamentals
- [Nullable Types and Null Safety](beginner/null_safety.md)
- [Data Classes vs Regular Classes](beginner/data_classes.md)
- [Sealed Classes and When Expressions](beginner/sealed_classes.md)
- [Coroutines Basics and Dispatchers](beginner/coroutines_basics.md)

### Jetpack Compose Basics
- [State and Recomposition](beginner/state_recomposition.md)
- [Modifier Order and Layout Behavior](beginner/modifier_order.md)

### Android & Architecture Lifecycles
- [Activity Lifecycle & OS Transitions](beginner/lifecycle_activity.md)
- [Composable Lifecycle & Phases](beginner/lifecycle_compose.md)
- [ViewModel Lifecycle & Scopes](beginner/lifecycle_viewmodel.md)
- [Lifecycle Observer & Sensor Safety (Composable Lifecycle)](beginner/lifecycle_observer_component.md)

---

## 2. Intermediate Topics

### State Management & Architecture
- [ViewModel and State Flow](intermediate/viewmodel.md)
- [When to Use Coroutine Scopes (Usage Guide)](intermediate/when_to_use_coroutine_scopes.md)
- [Coroutines Execution Order Quiz & Interview Puzzles](intermediate/coroutines_execution_order_quiz.md)
- [Compose Side Effects Master Guide (8 Handlers)](intermediate/compose_side_effects_guide.md)
- [StateFlow vs SharedFlow vs Flow](intermediate/flow_types.md)
- [Hilt Dependency Injection](intermediate/hilt_di.md)
- [LaunchedEffect and Side Effects](intermediate/launched_effect.md)

### Navigation & UI Lists
- [Passing Data Between Screens](intermediate/passing_data_between_screens.md)
- [Type-Safe Navigation](intermediate/type_safe_navigation.md)
- [LazyColumn Performance and Keys](intermediate/lazy_column_performance.md)

---

## 3. Advanced Topics

### Performance & Memory
- [ANR (Application Not Responding): Causes & Fixes](advanced/anr_causes_and_fixes.md)
- [Memory Leak Detection and LeakCanary](advanced/memory_leak_detection.md)
- [Performance Monitoring and Recomposition Tracing](advanced/performance_monitoring.md)

### Background Processing & System Services
- [AlarmManager Timing Guarantees, Doze Mode & Exact Alarms](advanced/alarm_manager_guarantees.md)

### Video & Live Streaming Systems
- [RTMP/S Live Video Broadcasting and Adaptive Bitrate (Creator Uplink)](advanced/rtmp_live_broadcasting.md)
- [Live Stream Audience Room and Interactive Playback (Fan Downlink)](advanced/live_stream_audience_interactions.md)

---

## 4. Codebase Navigation Reference

Interactive demo screens are implemented in the main repository:

- **Main Navigation Host**: [`NavGraph.kt`](../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/NavGraph.kt#L40)
- **Samples Sub-Graph**: [`SamplesNavGraph.kt`](../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/SamplesNavGraph.kt#L25)
- **Samples Hub Screen**: [`SamplesListScreen.kt`](../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/SamplesListScreen.kt)
