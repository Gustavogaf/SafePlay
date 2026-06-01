package com.example.safeplay.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safeplay.R

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateToRegister: () -> Unit = {},
    onLoginSuccess: (String) -> Unit = {}
) {
    var isAluno by remember { mutableStateOf(true) }
    var loginText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }

    // Novo estado para controlar a visibilidade da senha
    var passwordVisible by remember { mutableStateOf(false) }

    // Observa o estado que vem do ViewModel de forma reativa
    val authState by viewModel.authState.collectAsState()

    val inputBackgroundColor = Color(0xFFF0F4F8)
    val inputBorderColor = Color(0xFFD0D9E0)
    // Reage automaticamente quando o estado do login muda para Success
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess((authState as AuthState.Success).role)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logótipo
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF0D1B2A), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.safeplay_logo),
                contentDescription = "Logótipo do SafePlay",
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Vamos Começar?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botões de alternância de Perfil (Cards Quadrados)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RoleCard(
                title = "Sou Aluno",
                isSelected = isAluno,
                onClick = { isAluno = true },
                modifier = Modifier.weight(1f)
            )
            RoleCard(
                title = "Sou Educador",
                isSelected = !isAluno,
                onClick = { isAluno = false },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Card Branco contendo o Formulário
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Etiqueta externa para o E-mail
                Text(
                    text = "Apelido ou E-mail",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = loginText,
                    onValueChange = {
                        loginText = it
                        viewModel.resetError() // Limpa os erros ao começar a digitar
                    },
                    placeholder = { Text("Digite aqui...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputBackgroundColor,
                        unfocusedContainerColor = inputBackgroundColor,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = inputBorderColor
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Etiqueta externa para a Senha
                Text(
                    text = "Senha ou PIN secreto",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo de Senha com o "Olhinho"
                OutlinedTextField(
                    value = passwordText,
                    onValueChange = {
                        passwordText = it
                        viewModel.resetError() // Limpa os erros ao começar a digitar
                    },
                    placeholder = { Text("........", color = Color.Gray) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (passwordVisible) "Ocultar senha" else "Mostrar senha"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputBackgroundColor,
                        unfocusedContainerColor = inputBackgroundColor,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = inputBorderColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Exibe a mensagem de erro se houver
                if (authState is AuthState.Error) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Exibe mensagem de sucesso temporária (até configurarmos a navegação)
                if (authState is AuthState.Success) {
                    Text(
                        text = "A iniciar como ${(authState as AuthState.Success).role}...",
                        color = Color(0xFF4CAF50), // Verde sucesso
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Botão de Login
                Button(
                    enabled = authState !is AuthState.Loading,
                    onClick = {
                        viewModel.fazerLogin(loginText, passwordText, isAluno)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Entrar no SafePlay",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Seta para avançar",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Link de Criar Conta
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Primeira vez por aqui? ",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Criar Conta",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF4F7FB)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                val imagemAvatar = if (title == "Sou Aluno") {
                    R.drawable.student_illustration
                } else {
                    R.drawable.educator_illustration
                }

                Image(
                    painter = painterResource(id = imagemAvatar),
                    contentDescription = "Avatar de $title",
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.DarkGray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}