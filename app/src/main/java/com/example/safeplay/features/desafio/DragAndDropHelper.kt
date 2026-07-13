package com.example.safeplay.features.desafio

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex

data class DragData(val id: String, val text: String)

internal class DragDropState(val onDrop: (String, String) -> Unit) {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggedItem: DragData? by mutableStateOf(null)
    val dropZones = mutableMapOf<String, Rect>()
}

internal val LocalDragDropState = compositionLocalOf<DragDropState> { error("State não inicializado") }

@Composable
fun DragDropScreen(
    modifier: Modifier = Modifier,
    onDrop: (String, String) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val state = remember { DragDropState(onDrop) }

    CompositionLocalProvider(LocalDragDropState provides state) {
        Box(modifier = modifier.fillMaxSize()) {
            content()

            if (state.isDragging && state.draggedItem != null) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val offset = (state.dragPosition + state.dragOffset)
                            translationX = offset.x
                            translationY = offset.y
                        }
                        .zIndex(10f)
                ) {
                    DraggableItem(
                        text = state.draggedItem!!.text,
                        color = androidx.compose.ui.graphics.Color(0xFF6200EA),
                        isWhite = false
                    )
                }
            }
        }
    }
}

@Composable
fun DragTarget(
    itemParaArrastar: String,
    itemTexto: String,
    content: @Composable () -> Unit
) {
    val state = LocalDragDropState.current
    var currentPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .onGloballyPositioned { currentPosition = it.localToWindow(Offset.Zero) }
            .pointerInput(itemParaArrastar) {
                // Ajustado para AfterLongPress para liberar o scroll vertical nativo da tela
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        state.isDragging = true
                        state.draggedItem = DragData(itemParaArrastar, itemTexto)
                        state.dragPosition = currentPosition + offset
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        state.dragOffset += dragAmount
                    },
                    onDragEnd = {
                        val pointer = state.dragPosition + state.dragOffset
                        val droppedZone = state.dropZones.entries.firstOrNull { it.value.contains(pointer) }?.key

                        if (droppedZone != null && state.draggedItem != null) {
                            state.onDrop(state.draggedItem!!.id, droppedZone)
                        }

                        state.isDragging = false
                        state.dragOffset = Offset.Zero
                        state.draggedItem = null
                    },
                    onDragCancel = {
                        state.isDragging = false
                        state.dragOffset = Offset.Zero
                        state.draggedItem = null
                    }
                )
            }
    ) {
        if (state.isDragging && state.draggedItem?.id == itemParaArrastar) {
            Box(modifier = Modifier.matchParentSize())
        } else {
            content()
        }
    }
}

@Composable
fun DropZone(
    zonaId: String,
    content: @Composable BoxScope.(isHovering: Boolean) -> Unit
) {
    val state = LocalDragDropState.current

    val isHovering = if (state.isDragging) {
        val pointerPos = state.dragPosition + state.dragOffset
        state.dropZones[zonaId]?.contains(pointerPos) == true
    } else false

    Box(
        modifier = Modifier.onGloballyPositioned { coordinates ->
            state.dropZones[zonaId] = coordinates.boundsInWindow()
        }
    ) {
        content(isHovering)
    }
}