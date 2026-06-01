package com.example.safeplay.features.educador

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safeplay.features.trilha.SafePlayBottomNavigation // Reutilizando sua navegação base

@Composable
fun DashboardEducadorScreen(
    viewModel: DashboardEducadorViewModel = viewModel(),
    onNovaTurmaClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = { SafePlayBottomNavigation() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F7FB))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            CabecalhoEducador()

            Spacer(modifier = Modifier.height(32.dp))

            Text("Olá, Professor!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Aqui está o resumo das suas turmas e atividades recentes.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNovaTurmaClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0056D2)),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nova Turma", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // A Mágica Acontece Aqui: Reagir ao estado do ViewModel
            when (uiState) {
                is DashboardState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is DashboardState.Error -> {
                    val msg = (uiState as DashboardState.Error).message
                    Text("Erro ao carregar dados: $msg", color = Color.Red, fontWeight = FontWeight.Bold)
                }
                is DashboardState.Success -> {
                    val stats = (uiState as DashboardState.Success).estatisticas
                    val turmas = (uiState as DashboardState.Success).turmas

                    MetricCard("Total de Alunos", stats.totalAlunos.toString(), Icons.Default.Groups, "+${stats.crescimentoSemana} esta semana", Color(0xFF0056D2))
                    Spacer(modifier = Modifier.height(16.dp))
                    MetricCard("Atividades Pendentes", stats.atividadesPendentes.toString(), Icons.Default.WarningAmber, "Aguardando revisão", Color(0xFFD84315))
                    Spacer(modifier = Modifier.height(16.dp))
                    EngagementCard("Engajamento Médio", "${(stats.engajamentoMedio * 100).toInt()}%", stats.engajamentoMedio, Icons.Default.TrendingUp)

                    Spacer(modifier = Modifier.height(40.dp))
                    Text("Suas Turmas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))
                    Spacer(modifier = Modifier.height(16.dp))

                    if (turmas.isEmpty()) {
                        Text("Você ainda não possui turmas criadas.", color = Color.Gray)
                    } else {
                        turmas.forEach { turma ->
                            TurmaCardItem(turma = turma, onClick = { /* Futuro: Detalhes da turma */ })
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CabecalhoEducador() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray)) // Avatar
            Spacer(modifier = Modifier.width(12.dp))
            Text("SafePlay", color = Color(0xFF0056D2), fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF0056D2), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("Lvl 5 •", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0056D2))
                Text("1,250 pts", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0056D2))
            }
        }
    }
}

@Composable
fun MetricCard(titulo: String, valor: String, icone: androidx.compose.ui.graphics.vector.ImageVector, subtitulo: String, corIcone: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icone, contentDescription = null, tint = corIcone, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(titulo, color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(valor, fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (subtitulo.contains("+")) Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF0056D2), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(subtitulo, color = if (subtitulo.contains("+")) Color(0xFF0056D2) else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun EngagementCard(titulo: String, valor: String, progresso: Float, icone: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icone, contentDescription = null, tint = Color(0xFF6200EA), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(titulo, color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(valor, fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = progresso,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Color(0xFF6200EA),
                trackColor = Color(0xFFE0E0E0),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun TurmaCardItem(turma: TurmaResumo, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Faixa colorida lateral
            Box(modifier = Modifier.width(8.dp).fillMaxHeight().background(Color(turma.corDestaque)))

            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(turma.nome_turma, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))
                    Icon(Icons.Default.MoreVert, contentDescription = "Opções", tint = Color.Gray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Badge do código
                Box(modifier = Modifier.background(Color(0xFFF0F4F8), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("Código: ${turma.codigo}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info Box (Alunos x Módulo)
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF4F7FB), RoundedCornerShape(12.dp)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Alunos Ativos", color = Color.Gray, fontSize = 12.sp)
                        Text(turma.alunosAtivos.toString(), fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF0D1B2A))
                    }
                    Divider(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFD0D9E0)))
                    Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                        Text("Módulo Atual", color = Color.Gray, fontSize = 12.sp)
                        Text(turma.moduloAtual, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0D1B2A))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Text("Acessar Turma", color = Color(0xFF0056D2), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF0056D2), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}