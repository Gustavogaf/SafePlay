package com.example.safeplay.features.desafio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safeplay.data.model.quiz.Desafio

// Estrutura dinâmica para cores e ícones pedagógicos
data class ZonaTheme(
    val titulo: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val colorBase: Color,
    val colorBg: Color,
    val colorHover: Color
)

// 1. Função para formatar qualquer string do JSONB ("barra_seguranca" -> "Barra Segurança")
fun formatarNomeZona(zonaId: String): String {
    return zonaId.split("_")
        .joinToString(" ") { palavra ->
            palavra.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        .replace("Seguranca", "Segurança") // Ajuste fino de acentuação
}

// 2. Função de tema atualizada para usar o nome dinâmico
fun obterTemaDaZona(zonaId: String): ZonaTheme {
    val tituloDinamico = formatarNomeZona(zonaId)

    return when {
        // Se a zona contiver essas palavras, ganha o tema VERDE (Seguro)
        zonaId.contains("seguranca") || zonaId.contains("nuvem") || zonaId.contains("entrada") || zonaId.contains("porta") ->
            ZonaTheme(tituloDinamico, Icons.Default.Shield, Color(0xFF2E7D32), Color(0xFFE8F5E9), Color(0xFFC8E6C9))

        // Se contiver essas palavras, ganha o tema VERMELHO (Perigo/Lixeira)
        zonaId.contains("lixeira") || zonaId.contains("corrompido") || zonaId.contains("ameaca") || zonaId.contains("brecha") ->
            ZonaTheme(tituloDinamico, Icons.Default.DeleteForever, Color(0xFFD32F2F), Color(0xFFFFEBEE), Color(0xFFFFCDD2))

        // Tema padrão AZUL para qualquer outra zona genérica do JSONB
        else ->
            ZonaTheme(tituloDinamico, Icons.Default.Extension, Color(0xFF0056D2), Color(0xFFE3F2FD), Color(0xFFBBDEFB))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizDragAndDropScreen(
    desafio: Desafio,
    pontuacaoAtual: Int,
    progresso: Float,
    onCloseClick: () -> Unit,
    onConfirmarResposta: (mapaAlocacao: Map<String, String?>) -> Unit
) {
    val regras = desafio.regras_validacao ?: return Text("Erro: Configuração ausente.")

    var alocacaoItens by remember(desafio.id_desafio) {
        mutableStateOf(regras.itens_interativos.associate { it.id to (null as String?) })
    }

    DragDropScreen(
        onDrop = { idItem, idZona ->
            // Se o item for largado de volta no inventário, a alocação é limpa (retorna ao estado original)
            if (idZona == "inventario") {
                alocacaoItens = alocacaoItens + (idItem to null)
            } else {
                alocacaoItens = alocacaoItens + (idItem to idZona)
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                Box(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = { onConfirmarResposta(alocacaoItens) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Confirmar", fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                }
            },
            containerColor = Color(0xFFF7F9FC)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()) // Habilita o scroll completo do container
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onCloseClick,
                        modifier = Modifier.size(40.dp).background(Color(0xFFE0E6ED), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.DarkGray)
                    }

                    LinearProgressIndicator(
                        progress = progresso,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp).height(12.dp).clip(RoundedCornerShape(50)),
                        color = Color(0xFF0056D2),
                        trackColor = Color(0xFFE0E6ED),
                        strokeCap = StrokeCap.Round
                    )

                    Row(
                        modifier = Modifier.background(Color.White, RoundedCornerShape(50)).border(1.dp, Color(0xFFE0E6ED), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(pontuacaoAtual.toString(), fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(desafio.contexto, textAlign = TextAlign.Center, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(desafio.pergunta, textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    regras.zonas_alvo.forEach { zonaTecnica ->
                        val tema = obterTemaDaZona(zonaTecnica)

                        DropZone(zonaId = zonaTecnica) { isHovering ->
                            val currentBgColor = if (isHovering) tema.colorHover else tema.colorBg
                            val currentBorderColor = if (isHovering) tema.colorBase else tema.colorBase.copy(alpha = 0.5f)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(currentBgColor, RoundedCornerShape(16.dp))
                                    .border(2.dp, currentBorderColor, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(tema.icon, contentDescription = null, tint = tema.colorBase, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(tema.titulo, fontSize = 14.sp, fontWeight = FontWeight.Black, color = tema.colorBase)
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                                ) {
                                    // CORREÇÃO AQUI: Mudado de "rules" para "regras"
                                    val itensNestaZona = regras.itens_interativos.filter { alocacaoItens[it.id] == zonaTecnica }

                                    if (itensNestaZona.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                                            Text("Solte itens aqui...", color = tema.colorBase.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        itensNestaZona.forEach { item ->
                                            DragTarget(itemParaArrastar = item.id, itemTexto = item.texto) {
                                                DraggableItem(text = item.texto, color = tema.colorBase, isWhite = false, icon = tema.icon)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // O container do inventário agora age como uma DropZone com ID "inventario"
                DropZone(zonaId = "inventario") { isHovering ->
                    val inventarioBg = if (isHovering) Color(0xFFECEFF1) else Color.White
                    val inventarioBorderColor = if (isHovering) Color(0xFF78909C) else Color(0xFFE0E6ED)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(inventarioBg, RoundedCornerShape(16.dp))
                            .border(1.dp, inventarioBorderColor, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("PEÇAS DISPONÍVEIS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val itensNoInventario = regras.itens_interativos.filter { alocacaoItens[it.id] == null }

                                if (itensNoInventario.isEmpty() && alocacaoItens.values.any { it != null }) {
                                    Text("Todas as peças foram alocadas! Arraste de volta para remover.", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                                } else {
                                    itensNoInventario.forEach { item ->
                                        DragTarget(itemParaArrastar = item.id, itemTexto = item.texto) {
                                            DraggableItem(text = item.texto, color = Color(0xFF0D1B2A), isWhite = true, icon = Icons.Default.PanTool)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// CORREÇÃO AQUI: A função DraggableItem foi recolocada no arquivo
@Composable
fun DraggableItem(
    text: String,
    color: Color,
    isWhite: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier
            .background(if (isWhite) Color.White else color, RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = if (isWhite) Color(0xFFCFD8DC) else color.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isWhite) Color.Gray else Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = if (isWhite) Color(0xFF0D1B2A) else Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp
        )
    }
}

@Composable
fun QuizMultiplaEscolhaScreen(
    desafio: Desafio,
    alternativas: List<com.example.safeplay.data.model.quiz.Alternativa>,
    onConfirmar: (idAlternativa: String) -> Unit
) {
    var alternativaSelecionada by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(desafio.pergunta, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))

        alternativas.forEach { alt ->
            val isSelected = alternativaSelecionada == alt.id_alternativa

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { alternativaSelecionada = alt.id_alternativa },
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) BorderStroke(2.dp, Color(0xFF0056D2)) else null,
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = alt.texto_opcao,
                    modifier = Modifier.padding(20.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { alternativaSelecionada?.let { onConfirmar(it) } },
            enabled = alternativaSelecionada != null,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Verificar Resposta")
        }
    }
}