package com.example.safeplay.features.trilha

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safeplay.R
import com.example.safeplay.core.navigation.Rotas
import com.example.safeplay.data.model.ModuloComProgresso
import com.example.safeplay.data.model.Modulos

@Composable
fun TrilhaScreen(navController: androidx.navigation.NavController, viewModel: TrilhaViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = { SafePlayBottomNavigation() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FC)) // Cor de fundo mais clara do PDF
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // CABEÇALHO (Avatar, Logo, Nível)
            CabecalhoTrilha()

            // CARTÃO DE BOAS VINDAS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Continue Explorando!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0D1B2A))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Complete as fases para se tornar um mestre da internet segura.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DESENHO DO MAPA
            when (uiState) {
                is TrilhaState.Loading -> CircularProgressIndicator()
                is TrilhaState.Error -> {
                    val message = (uiState as TrilhaState.Error).message
                    Text(
                        text = "Erro: $message",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                is TrilhaState.Success -> {
                    val modulos = (uiState as TrilhaState.Success).modulos

                    // Renderiza de baixo para cima
                    modulos.reversed().forEachIndexed { index, modulo ->
                        // Lógica temporária de visual (No futuro virá do banco de dados do progresso do utilizador)
                        val statusVisual = when (modulo.status) {
                            "Concluído" -> "CONCLUIDO"
                            "Em Andamento" -> "ATUAL"
                            else -> "BLOQUEADO"
                        }

                        // Zigue-zague: Se for par vai para a esquerda, se ímpar vai para a direita
                        val deslocamentoX = if (modulo.ordem_trilha % 2 == 0) (-40).dp else 40.dp

                        FaseNodeDesign(
                            fase = modulo, // Passa o objeto inteiro
                            status = statusVisual,
                            deslocamentoX = deslocamentoX,
                            onClick = {
                                // Bloqueia o clique se não estiver liberado
                                if (statusVisual != "BLOQUEADO") {
                                    Modifier.clickable {
                                        // Quando clicar no módulo, navega passando o ID real daquele módulo no banco
                                        navController.navigate(Rotas.criarRotaQuiz(modulo.id_modulo))
                                    }
                                }
                            }
                        )

                        // Linha conectora (simplificada)
                        if (index < modulos.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .height(60.dp)
                                    .background(if (statusVisual == "BLOQUEADO") Color(0xFFE0E0E0) else Color(0xFF0056D2))
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CabecalhoTrilha() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar (Placeholder)
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray))
            Spacer(modifier = Modifier.width(12.dp))
            // Logo app (Placeholder)
            Box(modifier = Modifier.size(32.dp).background(Color(0xFF0D1B2A), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.safeplay_logo),
                    contentDescription = "Logótipo do SafePlay",
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        // Emblema de Pontos
        Card(
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8))
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nível 5 • 1,250 pts", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun FaseNodeDesign(fase: ModuloComProgresso, status: String, deslocamentoX: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    // Definir as cores com base no design
    val corCirculo = when (status) {
        "CONCLUIDO" -> if (fase.ordem_trilha == 1) Color(0xFFD84315) else Color(0xFF6200EA) // Laranja e Roxo do PDF
        "ATUAL" -> Color(0xFF0056D2) // Azul forte
        else -> Color.Transparent
    }

    val icone = when (fase.ordem_trilha) {
        1 -> Icons.Default.VpnKey
        2 -> Icons.Default.Security
        3 -> Icons.Default.HourglassEmpty
        else -> Icons.Default.Lock
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp), // Espaço para caber o círculo e a pílula
        contentAlignment = Alignment.Center
    ) {
        // Todo o bloco do botão deslocado para fazer o Zigue-Zague
        Box(
            modifier = Modifier.offset(x = deslocamentoX),
            contentAlignment = Alignment.Center
        ) {

            // As estrelas ao lado se estiver concluído
            if (status == "CONCLUIDO") {
                Column(modifier = Modifier.offset(x = (-70).dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD84315), modifier = Modifier.size(16.dp))
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD84315), modifier = Modifier.size(16.dp))
                    Icon(Icons.Default.StarBorder, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }

            // O Círculo Principal
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(corCirculo)
                    .border(if (status == "BLOQUEADO") 4.dp else 0.dp, Color(0xFFD0D9E0), CircleShape)
                    .clickable(enabled = status != "BLOQUEADO", onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icone,
                    contentDescription = fase.titulo,
                    tint = if (status == "BLOQUEADO") Color.Gray else Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // A Pílula com o Título (Sobreposta na parte inferior do círculo)
            Card(
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(containerColor = if (status == "BLOQUEADO") Color(0xFFE8ECEF) else Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = if (status == "BLOQUEADO") 0.dp else 4.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 20.dp) // Puxa o cartão para baixo para sobrepor a borda do círculo
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (status == "CONCLUIDO") {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = corCirculo, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = fase.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (status == "BLOQUEADO") Color.Gray else corCirculo
                    )
                }
            }

            // O Emblema "START" (Sobreposto na parte superior)
            if (status == "ATUAL") {
                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD84315)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-10).dp)
                ) {
                    Text("START", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun SafePlayBottomNavigation() {
    var itemSelecionado by remember { mutableStateOf(0) }
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Map, contentDescription = "Início") }, // Ícone atualizado para mapa
            label = { Text("Início", fontWeight = FontWeight.Bold) },
            selected = itemSelecionado == 0,
            onClick = { itemSelecionado = 0 },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = Color(0xFFE3F2FD))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Ranking") }, // Ícone atualizado
            label = { Text("Ranking", fontWeight = FontWeight.Bold) },
            selected = itemSelecionado == 1,
            onClick = { itemSelecionado = 1 }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.PersonOutline, contentDescription = "Perfil") },
            label = { Text("Perfil", fontWeight = FontWeight.Bold) },
            selected = itemSelecionado == 2,
            onClick = { itemSelecionado = 2 }
        )
    }
}