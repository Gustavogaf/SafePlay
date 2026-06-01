package com.example.safeplay.features.educador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.CheckCircle
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
fun CriarTurmaScreen(
    viewModel: CrianTurmaViewModel = viewModel()
) {
    var nomeTurma by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Cabeçalho
        Box(
            modifier = Modifier.size(72.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AddModerator, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Painel do Educador", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Crie uma nova turma para os seus alunos e acompanhe o progresso deles.", textAlign = TextAlign.Center, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(40.dp))

        // Se a criação for um SUCESSO, exibe o cartão com o código
        if (uiState is PainelEducadorState.Success) {
            val codigo = (uiState as PainelEducadorState.Success).codigoGerado

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Turma criada com sucesso!", fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Destaque do Código
                    Box(
                        modifier = Modifier.background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)).padding(horizontal = 32.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(codigo, fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 8.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Partilhe este código com os seus alunos para eles entrarem na turma.", textAlign = TextAlign.Center, color = Color.Gray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.resetState()
                            nomeTurma = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Criar Outra Turma")
                    }
                }
            }
        }
        // Caso contrário, exibe o FORMULÁRIO de criação
        else {
            OutlinedTextField(
                value = nomeTurma,
                onValueChange = {
                    nomeTurma = it
                    if (uiState is PainelEducadorState.Error) viewModel.resetState()
                },
                label = { Text("Nome da Turma (Ex: 5º Ano A)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState is PainelEducadorState.Error) {
                Text((uiState as PainelEducadorState.Error).message, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { viewModel.criarNovaTurma(nomeTurma) },
                enabled = uiState !is PainelEducadorState.Loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState is PainelEducadorState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Gerar Código da Turma", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}