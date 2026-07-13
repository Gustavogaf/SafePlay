package com.example.safeplay.features.desafio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun QuizScreen(
    idModulo: String,
    viewModel: QuizViewModel = viewModel(),
    onVoltarParaTrilha: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Assim que a tela abre, pedimos ao ViewModel para carregar os desafios deste módulo
    LaunchedEffect(idModulo) {
        viewModel.iniciarQuiz(idModulo)
    }

    // O Orquestrador: Decide o que desenhar com base no Estado
    when (val state = uiState) {
        is QuizState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is QuizState.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onVoltarParaTrilha) { Text("Voltar") }
            }
        }

        is QuizState.Finished -> {
            // Função interna para mapear a mensagem fixa com base no módulo concluído
            val mensagemFeedback = when {
                state.tituloModulo.contains("Cofre", ignoreCase = true) ->
                    "Incrível! Você aprendeu a criar senhas fortes e a proteger seu cofre digital de invasores! 🛡️"
                state.tituloModulo.contains("Detetive", ignoreCase = true) ->
                    "Sensacional! Agora você é um verdadeiro detetive, capaz de reconhecer pistas falsas e golpes na internet! 🔍"
                state.tituloModulo.contains("Sombra", ignoreCase = true) || state.tituloModulo.contains("Ameaça", ignoreCase = true) ->
                    "Parabéns! Você aprendeu a identificar e neutralizar os vírus e perigos ocultos na rede! 💻"
                else ->
                    "Parabéns! Você superou todos os desafios com muita sabedoria e garantiu mais um escudo protetor! 🚀"
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F9FC))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Bloco Superior: Título
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MÓDULO CONCLUÍDO!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2E7D32),
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.tituloModulo,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0D1B2A),
                        textAlign = TextAlign.Center
                    )
                }

                // Elemento Central: Medalha Conquistada
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White, androidx.compose.foundation.shape.CircleShape)
                        .border(4.dp, Color(0xFFFFC107), androidx.compose.foundation.shape.CircleShape)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!state.iconeMedalhaUrl.isNullOrBlank()) {
                        // Utiliza o Coil para carregar a imagem armazenada na URL do Supabase
                        coil.compose.AsyncImage(
                            model = state.iconeMedalhaUrl,
                            contentDescription = "Medalha Conquistada",
                            modifier = Modifier.fillMaxSize(),
                            error = androidx.compose.ui.res.painterResource(id = android.R.drawable.star_big_on) // Estrela padrão se a URL falhar
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Medalha",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }

                // Bloco de Feedback Narrativo e Placar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mensagemFeedback,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Card de Pontuação
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E6ED)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Recompensa", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("+${state.pontuacaoFinal} Pontos", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))
                            }
                        }
                    }
                }

                // Botão de Ação Inferior
                Button(
                    onClick = onVoltarParaTrilha,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0056D2)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Retornar ao Mapa", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                }
            }
        }

        is QuizState.Playing -> {
            // Calcula a barra de progresso (ex: 1 de 2 desafios = 50%)
            val progressoCalculado = (state.indiceAtual - 1).toFloat() / state.totalDesafios.toFloat()

            // Um Box principal para podermos colocar o Pop-up de erro por cima do jogo
            Box(modifier = Modifier.fillMaxSize()) {

                // Roteador de Dinâmicas
                if (state.desafioAtual.tipo_dinamica == "drag_and_drop") {
                    QuizDragAndDropScreen(
                        desafio = state.desafioAtual,
                        pontuacaoAtual = state.pontuacaoAcumulada,
                        progresso = progressoCalculado,
                        onCloseClick = onVoltarParaTrilha,
                        onConfirmarResposta = { alocacao ->
                            viewModel.validarRespostaDragAndDrop(alocacao)
                        }
                    )
                } else if (state.desafioAtual.tipo_dinamica == "quiz_multipla_escolha") {
                    QuizMultiplaEscolhaScreen(
                        desafio = state.desafioAtual,
                        alternativas = state.alternativas,
                        pontuacaoAtual = state.pontuacaoAcumulada,
                        progresso = progressoCalculado,
                        onCloseClick = onVoltarParaTrilha,
                        onConfirmarResposta = { idAlternativaSelecionada ->
                            viewModel.validarRespostaMultiplaEscolha(idAlternativaSelecionada)
                        }
                    )
                }

                // Pop-up Pedagógico de Erro (Dica)
                if (state.mostrarDicaErro) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Oops, quase lá!", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = state.desafioAtual.dica_erro,
                                    textAlign = TextAlign.Center,
                                    color = Color.DarkGray,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { viewModel.tentarNovamente() },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0056D2))
                                ) {
                                    Text("Tentar Novamente", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}