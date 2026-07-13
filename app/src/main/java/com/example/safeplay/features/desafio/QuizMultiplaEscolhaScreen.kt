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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
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
import com.example.safeplay.data.model.quiz.Alternativa
import com.example.safeplay.data.model.quiz.Desafio

@Composable
fun QuizMultiplaEscolhaScreen(
    desafio: Desafio,
    alternativas: List<Alternativa>,
    pontuacaoAtual: Int,
    progresso: Float,
    onCloseClick: () -> Unit,
    onConfirmarResposta: (idAlternativa: String) -> Unit
) {
    var alternativaSelecionada by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                Button(
                    onClick = { alternativaSelecionada?.let { onConfirmarResposta(it) } },
                    enabled = alternativaSelecionada != null,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD84315),
                        disabledContainerColor = Color.LightGray
                    ),
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Cabeçalho igual ao Drag and Drop
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

            Spacer(modifier = Modifier.height(32.dp))

            // Contexto e Pergunta
            Text(desafio.contexto, textAlign = TextAlign.Center, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(desafio.pergunta, textAlign = TextAlign.Center, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))

            Spacer(modifier = Modifier.height(32.dp))

            // Lista de Alternativas
            alternativas.forEach { alternativa ->
                val isSelected = alternativaSelecionada == alternativa.id_alternativa
                val cardBgColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
                val borderColor = if (isSelected) Color(0xFF0056D2) else Color(0xFFE0E6ED)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { alternativaSelecionada = alternativa.id_alternativa }
                        .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Radio button customizado
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(if (isSelected) Color(0xFF0056D2) else Color.Transparent, CircleShape)
                                .border(2.dp, if (isSelected) Color(0xFF0056D2) else Color.Gray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(modifier = Modifier.size(10.dp).background(Color.White, CircleShape))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = alternativa.texto_opcao,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = Color(0xFF0D1B2A)
                        )
                    }
                }
            }
        }
    }
}