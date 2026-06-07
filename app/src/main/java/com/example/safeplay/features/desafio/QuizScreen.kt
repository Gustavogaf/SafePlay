package com.example.safeplay.features.desafio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
            // O aluno concluiu todos os desafios do módulo!
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Módulo Concluído!", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Você ganhou ${state.pontuacaoFinal} pontos!", fontSize = 20.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        // TODO: Gravar 'Concluído' no banco e voltar para a trilha
                        onVoltarParaTrilha()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Retornar ao Mapa")
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
                    // TODO: Aqui chamaremos a tela de Múltipla Escolha
                    Text("Tela de Múltipla Escolha em Construção...", modifier = Modifier.align(Alignment.Center))
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