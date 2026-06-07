package com.example.safeplay.features.desafio

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex

// 1. O ESTADO GLOBAL DO ARRASTO
// Guarda a informação da peça atual, onde ela está e se está a ser arrastada
internal class DragDropState {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggedItem: String? by mutableStateOf(null) // Para já, arrastamos o texto da peça
}

// Criamos um canal de comunicação invisível para toda a tela
internal val LocalDragDropState = compositionLocalOf { DragDropState() }

// 2. A TELA PRINCIPAL QUE OBSERVA O ARRASTO
// Este componente envolve a sua tela inteira e desenha a peça "fantasma" que segue o dedo
@Composable
fun DragDropScreen(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = remember { DragDropState() }

    CompositionLocalProvider(LocalDragDropState provides state) {
        Box(modifier = modifier.fillMaxSize()) {
            content()

            // Se algo estiver a ser arrastado, desenhamos a peça exatamente onde o dedo está
            if (state.isDragging && state.draggedItem != null) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val offset = (state.dragPosition + state.dragOffset)
                            translationX = offset.x
                            translationY = offset.y
                        }
                        .zIndex(10f) // Garante que a peça flutua acima de tudo
                ) {
                    // Aqui renderizamos visualmente a peça que está presa ao dedo
                    DraggableItem(
                        text = state.draggedItem!!,
                        color = androidx.compose.ui.graphics.Color(0xFF6200EA),
                        isWhite = false
                    )
                }
            }
        }
    }
}

// 3. O MODIFICADOR DA PEÇA (DRAG TARGET)
// Colocamos isto nas peças do inventário para que elas "sintam" o dedo
@Composable
fun DragTarget(
    itemParaArrastar: String,
    content: @Composable () -> Unit
) {
    val state = LocalDragDropState.current
    var currentPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .onGloballyPositioned { currentPosition = it.localToWindow(Offset.Zero) }
            .pointerInput(itemParaArrastar) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        state.isDragging = true
                        state.draggedItem = itemParaArrastar
                        state.dragPosition = currentPosition + offset
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        state.dragOffset += dragAmount
                    },
                    onDragEnd = {
                        state.isDragging = false
                        state.dragOffset = Offset.Zero
                        // Aqui a peça foi solta! O DropZone vai detetar isto.
                    },
                    onDragCancel = {
                        state.isDragging = false
                        state.dragOffset = Offset.Zero
                    }
                )
            }
    ) {
        // Se a peça estiver a ser arrastada, escondemos a original deixando o espaço vazio
        if (state.isDragging && state.draggedItem == itemParaArrastar) {
            Box(modifier = Modifier.matchParentSize()) // Espaço fantasma
        } else {
            content()
        }
    }
}

// 4. A ZONA DE SOLTURA (DROP ZONE)
// Colocamos isto na caixa tracejada para saber se o dedo abriu lá dentro
@Composable
fun DropZone(
    onItemDropped: (String) -> Unit,
    content: @Composable BoxScope.(isHovering: Boolean) -> Unit
) {
    val state = LocalDragDropState.current
    var isHovering by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.onGloballyPositioned { coordinates ->
            if (state.isDragging) {
                // Calcula se o dedo do utilizador está dentro das coordenadas desta caixa
                val bounds = coordinates.boundsInWindow()
                val pointerPos = state.dragPosition + state.dragOffset
                isHovering = bounds.contains(pointerPos)
            } else if (isHovering) {
                // Se o dedo foi solto (isDragging = false) E estava a pairar por cima, SUCESSO!
                isHovering = false
                state.draggedItem?.let { item ->
                    onItemDropped(item)
                    state.draggedItem = null // Limpa o item do dedo
                }
            }
        }
    ) {
        content(isHovering)
    }
}