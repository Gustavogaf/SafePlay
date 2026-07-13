package com.example.safeplay.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onRegisterSuccess: (destino: String) -> Unit = {} // Novo evento para sucesso!
) {
    var isAluno by remember { mutableStateOf(true) }
    var nomeText by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Observa o estado do ViewModel
    val authState by viewModel.authState.collectAsState()

    // Reage ao sucesso automaticamente
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterSuccess((authState as AuthState.Success).destino)
        }
    }

    val inputBackgroundColor = Color(0xFFF0F4F8)
    val inputBorderColor = Color(0xFFD0D9E0)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Criar Conta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RoleCard(title = "Sou Aluno", isSelected = isAluno, onClick = { isAluno = true }, modifier = Modifier.weight(1f))
            RoleCard(title = "Sou Educador", isSelected = !isAluno, onClick = { isAluno = false }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // CAMPO NOME
                Text("Nome Completo", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nomeText,
                    onValueChange = { nomeText = it; viewModel.resetError() },
                    placeholder = { Text("Como te chamas?", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputBackgroundColor, unfocusedContainerColor = inputBackgroundColor,
                        focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = inputBorderColor
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))

                // CAMPO EMAIL
                Text("E-mail", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = emailText,
                    onValueChange = { emailText = it; viewModel.resetError() },
                    placeholder = { Text("exemplo@escola.com", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputBackgroundColor, unfocusedContainerColor = inputBackgroundColor,
                        focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = inputBorderColor
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))

                // CAMPO SENHA
                Text("Cria uma Senha", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = passwordText,
                    onValueChange = { passwordText = it; viewModel.resetError() },
                    placeholder = { Text("No mínimo 6 caracteres...", color = Color.Gray) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Alternar senha", tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputBackgroundColor, unfocusedContainerColor = inputBackgroundColor,
                        focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = inputBorderColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tratamento de Erros e Sucesso
                if (authState is AuthState.Error) {
                    Text(text = (authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                }
                if (authState is AuthState.Success) {
                    Text(text = "Conta criada! A iniciar...", color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // BOTÃO FINALIZAR
                Button(
                    enabled = authState !is AuthState.Loading,
                    onClick = { viewModel.fazerCadastro(nomeText, emailText, passwordText, isAluno) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text("Finalizar Cadastro", style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Confirmar", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(bottom = 24.dp)) {
            Text("Já tens uma conta? ", color = MaterialTheme.colorScheme.onBackground)
            Text("Fazer Login", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateBack() })
        }
    }
}