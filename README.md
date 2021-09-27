# Lifecycle issue with Compose and Navigation

## Context

Following the sample code, we insert a lifecycle observer :

```kotlin
@Composable
fun LogLifecycle() {
    val owner = LocalLifecycleOwner.current
    val lifecycle = owner.lifecycle
    logcat("MainActivity") { "owner $owner $lifecycle ${lifecycle.currentState}" }

    DisposableEffect(lifecycle) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            logcat("MainActivity") { "event $event" }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}
```

into a BottomNavigation destination.

## Issue

Pressing the same destination twice, or switching rapidly between two destinations will cause the following logs:

```
2021-09-27 16:16:52.809 20448-20448/com.nitro.lifecyclestopped D/MainActivity: owner androidx.navigation.NavBackStackEntry@6f0ea592 androidx.lifecycle.LifecycleRegistry@9ce9bd6 STARTED
2021-09-27 16:16:52.814 20448-20448/com.nitro.lifecyclestopped D/MainActivity: event ON_CREATE
2021-09-27 16:16:52.814 20448-20448/com.nitro.lifecyclestopped D/MainActivity: event ON_START
2021-09-27 16:16:53.147 20448-20448/com.nitro.lifecyclestopped D/MainActivity: event ON_RESUME
2021-09-27 16:16:54.807 20448-20448/com.nitro.lifecyclestopped D/MainActivity: event ON_PAUSE
2021-09-27 16:16:54.807 20448-20448/com.nitro.lifecyclestopped D/MainActivity: event ON_STOP
```

meaning the lifecycle is stopped. The issue is we are currently on the screen using the lifecycle, the lifecycle should come back to
ON_START then ON_RESUME, but it is never triggered again.

## Real use case

A real use case with this issue is using a mapView (
from [Crane](https://github.com/android/compose-samples/blob/main/Crane/app/src/main/java/androidx/compose/samples/crane/details/MapViewUtils.kt) ) :

```kotlin
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        // Make MapView follow the current lifecycle
        val lifecycleObserver = getMapLifecycleObserver(mapView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

private fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
            Lifecycle.Event.ON_START -> mapView.onStart()
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            Lifecycle.Event.ON_STOP -> mapView.onStop()
            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
            else -> throw IllegalStateException()
        }
    }
```

It means that with the same behavior, a MapView on a Bottom destination will stop working after pressing the destination twice or
going/coming quickly between destinations, as `mapView.onStop()` will be called but `mapView.onStart()` won't (hence the mapview is frozen
and never "wake up"). The only solution is for the user to go to another destination, wait for a second, and come back to the previous
destination.

## Gif

![Gif](https://raw.githubusercontent.com/NitroG42/LifecycleStopped/master/lifecycle.gif)