package com.example.safeplay.features.turma

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
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
fun EntrarTurmaScreen(
    viewModel: EntrarTurmaViewModel = viewModel(),
    onTurmaVinculada: () -> Unit
) {
    var codigoTurma by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    // Observa o sucesso para avançar de tela automaticamente
    LaunchedEffect(uiState) {
        if (uiState is EntrarTurmaState.Success) {
            onTurmaVinculada()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(Color.White, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.GroupAdd, contentDescription = "Turma", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Encontre a sua Turma", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Peça o código de 6 dígitos ao seu educador para desbloquear a sua jornada.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = codigoTurma,
            onValueChange = { input ->
                if (input.length <= 6) {
                    codigoTurma = input.uppercase()
                    viewModel.resetState() // Limpa erros anteriores ao digitar
                }
            },
            placeholder = { Text("EX: AB1234", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color(0xFFD0D9E0)
            ),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Exibição de Erros
        if (uiState is EntrarTurmaState.Error) {
            Text(
                text = (uiState as EntrarTurmaState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.validarCodigoEEntrar(codigoTurma) },
            enabled = codigoTurma.length == 6 && uiState !is EntrarTurmaState.Loading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState is EntrarTurmaState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Entrar na Turma", style = MaterialTheme.typography.labelLarge, fontSize = 16.sp)
            }
        }
    }
}