package com.example.safeplay.features.desafio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
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
import com.example.safeplay.data.model.quiz.ItemInterativo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizDragAndDropScreen(
    desafio: Desafio,
    pontuacaoAtual: Int,
    progresso: Float,
    onCloseClick: () -> Unit,
    onConfirmarResposta: (mapaAlocacao: Map<String, String?>) -> Unit
) {
    // Pegamos a configuração JSONB que veio do Supabase
    val regras = desafio.regras_validacao
        ?: return Text("Erro: Configuração de validação ausente.")

    // ESTADO DINÂMICO: Controla em qual zona cada ID de item foi solto
    // Estrutura: Map<IdDoItem, NomeDaZonaAlvo?>
    var alocacaoItens by remember(desafio.id) {
        mutableStateOf(regras.itens_interativos.associate { it.id to (null as String?) })
    }

    DragDropScreen {
        Scaffold(
            bottomBar = {
                Box(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = { onConfirmarResposta(alocacaoItens) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB33A00)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Confirmar", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                }
            },
            containerColor = Color(0xFFF4F7FB)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Top Bar Dinâmica
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
                        Box(modifier = Modifier.size(12.dp).background(Color(0xFF6200EA), CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(pontuacaoAtual.toString(), fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Textos Narrativos do Banco de Dados
                Text(desafio.contexto, textAlign = TextAlign.Center, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(desafio.pergunta, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A))

                Spacer(modifier = Modifier.height(24.dp))

                // 2. RENDERIZADOR DE ZONAS ALVO (Gera quantas caixas o banco mandar)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    regras.zonas_alvo.forEach { zonaTecnica ->
                        val tituloExibicao = traduzirZonaAlvo(zonaTecnica)

                        DropZone(
                            onItemDropped = { idItem ->
                                // Atualiza o mapa vinculando o item a esta zona específica
                                alocacaoItens = alocacaoItens + (idItem to zonaTecnica)
                            }
                        ) { isHovering ->
                            val borderColor = if (isHovering) Color(0xFF6200EA) else Color(0xFF0056D2).copy(alpha = 0.3f)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isHovering) Color(0xFFF0E6FF) else Color.White, RoundedCornerShape(12.dp))
                                    .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(tituloExibicao, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                ) {
                                    // Filtra e desenha os itens que foram jogados aqui dentro
                                    val itensNestaZona = regras.itens_interativos.filter { alocacaoItens[it.id] == zonaTecnica }

                                    itensNestaZona.forEach { item ->
                                        DraggableItem(text = item.texto, color = Color(0xFF0056D2), isWhite = false)
                                    }
                                    if (itensNestaZona.isEmpty()) {
                                        Text("Arraste elementos para aqui...", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. INVENTÁRIO (Exibe apenas os itens que ainda NÃO foram arrastados)
                Box(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFE8EEF4), RoundedCornerShape(16.dp)).padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("ELEMENTOS DISPONÍVEIS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val itensNoInventario = regras.itens_interativos.filter { alocacaoItens[it.id] == null }

                            itensNoInventario.forEach { item ->
                                DragTarget(itemParaArrastar = item.id) {
                                    DraggableItem(text = item.texto, color = Color.White, isWhite = true, icon = Icons.Default.Extension)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper pedagógico para traduzir os identificadores técnicos do JSONB para a interface do aluno.
 */
fun traduzirZonaAlvo(zona: String): String {
    return when (zona) {
        "barra_seguranca" -> "🛡️ Elementos Fortes (Barra de Segurança)"
        "lixeira" -> "🗑️ Elementos Fracos (Descartar na Lixeira)"
        "porta_1" -> "🚪 Porta de Entrada 1 (Primeira Defesa)"
        "porta_2" -> "🚪 Porta de Entrada 2 (Segunda Defesa)"
        "servidor_nuvem" -> "☁️ Servidor em Nuvem / Armazenamento Seguro"
        "disco_local_corrompido" -> "💻 Disco Local (Em Falha Crítica)"
        "brecha_sistema_1", "brecha_sistema_2" -> "🔧 Vulnerabilidade Aberta no Sistema"
        "scanner_ameacas" -> "🔍 Scanner de Ameaças (Sinais de Golpe)"
        "caixa_entrada" -> "📥 Caixa de Entrada Segura"
        else -> zona.replace("_", " ").capitalize()
    }

}

// Componente visual da peça interativa
@Composable
fun DraggableItem(
    text: String,
    color: Color,
    isWhite: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier
            .background(color, RoundedCornerShape(8.dp))
            .border(
                width = if (isWhite) 1.dp else 0.dp,
                color = if (isWhite) Color.LightGray else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isWhite) Color(0xFF0056D2) else Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            color = if (isWhite) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}