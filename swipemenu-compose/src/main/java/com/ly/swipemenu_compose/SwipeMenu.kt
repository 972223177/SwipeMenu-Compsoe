package com.ly.swipemenu_compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch


@Composable
fun rememberSwipeMenuState(immediatelyShowRate: Float = 0.1f): DefaultSwipeMenuState = remember {
    DefaultSwipeMenuState(immediatelyShowRate = immediatelyShowRate)
}

@Composable
fun SwipeMenu(
    state: DefaultSwipeMenuState = rememberSwipeMenuState(),
    leftMenu: (@Composable () -> Unit)? = null,
    rightMenu: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val contentWidth = LocalContext.current.resources.displayMetrics.widthPixels
    val offsetX = remember {
        Animatable(0f)
    }
    val scope = rememberCoroutineScope()
    Layout(content = {
        content()
        leftMenu?.invoke()
        rightMenu?.invoke()
    }, modifier = modifier.swipeMenu(state = state, offsetX)) { measurables, constraints ->
        val contentPlaceable = measurables[0].measure(
            constraints.copy(
                minWidth = contentWidth,
                maxWidth = contentWidth
            )
        )
        var leftMenuPlaceable: Placeable? = null
        var rightMenuPlaceable: Placeable? = null
        when (measurables.size) {
            3 -> {
                leftMenuPlaceable = measurables[1].measure(constraints)
                rightMenuPlaceable = measurables[2].measure(constraints)
            }
            2 -> {
                if (rightMenu != null) {
                    rightMenuPlaceable = measurables[1].measure(constraints)
                } else if (leftMenu != null) {
                    leftMenuPlaceable = measurables[1].measure(constraints)
                }
            }
            else -> {
                leftMenuPlaceable = null
                rightMenuPlaceable = null
            }
        }
        val leftMenuWidth = leftMenuPlaceable?.width ?: 0
        val rightMenuWidth = rightMenuPlaceable?.width ?: 0
        state.leftMenuWidth = leftMenuWidth.toFloat()
        state.rightMenuWidth = rightMenuWidth.toFloat()
        val layoutWidth = leftMenuWidth + rightMenuWidth + contentPlaceable.width
        val layoutHeight = contentPlaceable.height.coerceAtLeast(
            (leftMenuPlaceable?.height ?: 0).coerceAtLeast(rightMenuPlaceable?.height ?: 0)
        )
        val halfBlankArea = ((state.leftMenuWidth + state.rightMenuWidth) / 2).toInt()
        val leftX = -halfBlankArea - leftMenuWidth
        val contentX = -halfBlankArea
        val rightX = contentX + contentPlaceable.width
        scope.launch {
            offsetX.snapTo(state.initialOffsetX)
        }
        layout(layoutWidth, layoutHeight) {
            leftMenuPlaceable?.place(leftX, 0)
            contentPlaceable.place(contentX, 0)
            rightMenuPlaceable?.place(rightX, 0)
        }
    }
}

private fun Modifier.swipeMenu(
    state: DefaultSwipeMenuState,
    offsetX: Animatable<Float, AnimationVector1D>
) = composed {
    val scope = rememberCoroutineScope()
    DisposableEffect(state, offsetX) {
        state.scrollAction = { value, anim ->
            scope.launch {
                offsetX.stop()
                if (anim) {
                    offsetX.animateTo(value)
                } else {
                    offsetX.snapTo(value)
                }
            }
        }
        onDispose {
            state.scrollAction = null
        }
    }
    var isDragLeft: Boolean? = null
    val draggableState = rememberDraggableState(onDelta = {
        isDragLeft = when {
            it < 0 -> true
            it > 0 -> false
            else -> null
        }
        scope.launch {
            offsetX.stop()
            val nextX = it + offsetX.value
            if (state.inDragRange(nextX)) {
                offsetX.snapTo(nextX)
            }
        }
    })

    draggable(state = draggableState, orientation = Orientation.Horizontal, onDragStopped = {
        scope.launch {
            isDragLeft?.let {
                val currX = offsetX.value
                with(state) {
                    if (currX < initialOffsetX && currX > dragMinValue) {
                        offsetX.animateTo(if (it && (currX < (initialOffsetX - rightVisibleBaseline))) dragMinValue else initialOffsetX)
                    }
                    if (currX > initialOffsetX && currX <= dragMaxValue) {
                        offsetX.animateTo(if (!it && currX > (initialOffsetX + leftVisibleBaseline)) dragMaxValue else initialOffsetX)
                    }
                }
            }
        }

    }).offset {
        IntOffset(offsetX.value.toInt(), 0)
    }
}

class DefaultSwipeMenuState(var immediatelyShowRate: Float = 0.1f) {
    internal var leftMenuWidth: Float = 0f
    internal var rightMenuWidth: Float = 0f

    internal var scrollAction: ((Float, Boolean) -> Unit)? = null

    internal val dragMinValue: Float get() = leftMenuWidth

    internal val dragMaxValue: Float get() = initialOffsetX + dragMinValue

    internal val leftVisibleBaseline: Float get() = leftMenuWidth * immediatelyShowRate
    internal val rightVisibleBaseline: Float get() = rightMenuWidth * immediatelyShowRate

    internal val initialOffsetX: Float get() = leftMenuWidth + rightMenuWidth

    internal fun inDragRange(value: Float): Boolean = value in dragMinValue..dragMaxValue

    fun showLeftMenu(anim: Boolean = true) {
        scrollAction?.invoke(dragMaxValue, anim)
    }

    fun showRightMenu(anim: Boolean = true) {
        scrollAction?.invoke(dragMinValue, anim)
    }

    fun reset(anim: Boolean = true) {
        scrollAction?.invoke(initialOffsetX, anim)
    }
}

