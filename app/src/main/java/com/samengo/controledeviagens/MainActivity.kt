@file:Suppress("DEPRECATION")

package com.samengo.controledeviagens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.samengo.controledeviagens.ui.theme.ControledeviagensTheme
import java.util.Locale

// Classe que representa uma viagem
class Viagem(
    val data: String,
    val kmInicial: Double,
    val kmFinal: Double,
    val litros: Double,
    val combustivel: String,
    val valorLitro: Double,
    val pedagio: Double
) {
    fun calcularDistancia(): Double = kmFinal - kmInicial
    fun calcularCusto(): Double = (litros * valorLitro) + pedagio
}

// Activity principal
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControledeviagensTheme {
                ControleViagensApp()
            }
        }
    }
}

// Composable container que gerencia o estado e as rotas
@Composable
fun ControleViagensApp() {
    val navController = rememberNavController()

    // Lista de viagens mantida no nível superior
    val viagens = remember { mutableStateListOf<Viagem>() }

    // Estados do formulário
    var data by remember { mutableStateOf("") }
    var kmInicial by remember { mutableStateOf("") }
    var kmFinal by remember { mutableStateOf("") }
    var litros by remember { mutableStateOf("") }
    var combustivel by remember { mutableStateOf("") }
    var valorLitro by remember { mutableStateOf("") }
    var pedagio by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "lista",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Rota 1: Lista e Resumo
            composable("lista") {
                TelaListaViagens(
                    viagens = viagens,
                    navController = navController
                )
            }

            // Rota 2: Cadastro da Viagem
            composable("cadastro") {
                TelaCadastroViagem(
                    data = data,
                    kmInicial = kmInicial,
                    kmFinal = kmFinal,
                    litros = litros,
                    combustivel = combustivel,
                    valorLitro = valorLitro,
                    pedagio = pedagio,
                    mensagem = mensagem,
                    onDataChange = { data = it },
                    onKmInicialChange = { kmInicial = it },
                    onKmFinalChange = { kmFinal = it },
                    onLitrosChange = { litros = it },
                    onCombustivelChange = { combustivel = it },
                    onValorLitroChange = { valorLitro = it },
                    onPedagioChange = { pedagio = it },
                    onCadastrar = {
                        val inicial = kmInicial.toDoubleOrNullBR()
                        val final = kmFinal.toDoubleOrNullBR()
                        val qtdLitros = litros.toDoubleOrNullBR()
                        val precoLitro = valorLitro.toDoubleOrNullBR()
                        val valorPedagio = pedagio.toDoubleOrNullBR()

                        if (
                            data.isBlank() ||
                            combustivel.isBlank() ||
                            inicial == null ||
                            final == null ||
                            qtdLitros == null ||
                            precoLitro == null ||
                            valorPedagio == null
                        ) {
                            mensagem = "Preencha todos os campos corretamente."
                        } else if (
                            inicial < 0 ||
                            final < inicial ||
                            qtdLitros <= 0 ||
                            precoLitro < 0 ||
                            valorPedagio < 0
                        ) {
                            mensagem = "Confira os valores informados."
                        } else {
                            val viagem = Viagem(
                                data = data,
                                kmInicial = inicial,
                                kmFinal = final,
                                litros = qtdLitros,
                                combustivel = combustivel,
                                valorLitro = precoLitro,
                                pedagio = valorPedagio
                            )
                            viagens.add(viagem)

                            // Limpa os campos
                            data = ""
                            kmInicial = ""
                            kmFinal = ""
                            litros = ""
                            combustivel = ""
                            valorLitro = ""
                            pedagio = ""
                            mensagem = ""

                            // Retorna à lista
                            navController.navigateUp()
                        }
                    },
                    navController = navController
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TELA 1: LISTAGEM E RESUMO
// -------------------------------------------------------------
@Composable
fun TelaListaViagens(
    viagens: List<Viagem>,
    navController: NavController
) {
    val totalKm = viagens.sumOf { it.calcularDistancia() }
    val totalLitros = viagens.sumOf { it.litros }
    val totalGasto = viagens.sumOf { it.calcularCusto() }
    val mediaKmLitro = if (totalLitros > 0) totalKm / totalLitros else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Controle de Viagens",
            style = MaterialTheme.typography.headlineMedium
        )

        // Botão para ir à tela de cadastro
        Button(
            onClick = { navController.navigate("cadastro") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nova Viagem")
        }

        // Resumo
        Text(
            text = "Resumo das viagens",
            style = MaterialTheme.typography.titleLarge
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Viagens cadastradas: ${viagens.size}")
                Text("Total de quilômetros: ${totalKm.formatarBR()} km")
                Text("Total de litros: ${totalLitros.formatarBR()} L")
                Text("Total gasto: ${totalGasto.formatarMoeda()}")
                Text("Média de consumo: ${mediaKmLitro.formatarBR()} km/l")
            }
        }

        // Lista de Viagens
        Text(
            text = "Viagens realizadas",
            style = MaterialTheme.typography.titleLarge
        )

        if (viagens.isEmpty()) {
            Text("Nenhuma viagem cadastrada.")
        }

        for (viagem in viagens) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Viagem de ${viagem.data}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Combustível: ${viagem.combustivel}")
                    Text("KM inicial: ${viagem.kmInicial.formatarBR()}")
                    Text("KM final: ${viagem.kmFinal.formatarBR()}")
                    Text("Distância: ${viagem.calcularDistancia().formatarBR()} km")
                    Text("Litros: ${viagem.litros.formatarBR()} L")
                    Text("Custo combustível: ${(viagem.litros * viagem.valorLitro).formatarMoeda()}")
                    Text("Pedágios: ${viagem.pedagio.formatarMoeda()}")
                    Text("Custo total: ${viagem.calcularCusto().formatarMoeda()}")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TELA 2: CADASTRO DE VIAGEM
// -------------------------------------------------------------
@Composable
fun TelaCadastroViagem(
    data: String,
    kmInicial: String,
    kmFinal: String,
    litros: String,
    combustivel: String,
    valorLitro: String,
    pedagio: String,
    mensagem: String,
    onDataChange: (String) -> Unit,
    onKmInicialChange: (String) -> Unit,
    onKmFinalChange: (String) -> Unit,
    onLitrosChange: (String) -> Unit,
    onCombustivelChange: (String) -> Unit,
    onValorLitroChange: (String) -> Unit,
    onPedagioChange: (String) -> Unit,
    onCadastrar: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Cadastrar Viagem",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = data,
            onValueChange = onDataChange,
            label = { Text("Data da viagem") },
            placeholder = { Text("DD/MM/AAAA") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = kmInicial,
            onValueChange = onKmInicialChange,
            label = { Text("Quilometragem inicial") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        OutlinedTextField(
            value = kmFinal,
            onValueChange = onKmFinalChange,
            label = { Text("Quilometragem final") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        OutlinedTextField(
            value = litros,
            onValueChange = onLitrosChange,
            label = { Text("Litros abastecidos/consumidos") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        OutlinedTextField(
            value = combustivel,
            onValueChange = onCombustivelChange,
            label = { Text("Tipo de combustível") },
            placeholder = { Text("Gasolina, etanol, diesel...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = valorLitro,
            onValueChange = onValorLitroChange,
            label = { Text("Valor por litro (R$)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        OutlinedTextField(
            value = pedagio,
            onValueChange = onPedagioChange,
            label = { Text("Total de pedágios (R$)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Button(
            onClick = onCadastrar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar viagem")
        }

        OutlinedButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar")
        }

        if (mensagem.isNotBlank()) {
            Text(
                text = mensagem,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

// -------------------------------------------------------------
// FUNÇÕES AUXILIARES E PREVIEW
// -------------------------------------------------------------
fun String.toDoubleOrNullBR(): Double? {
    return this.trim().replace(",", ".").toDoubleOrNull()
}

fun Double.formatarBR(): String {
    return String.format(Locale("pt", "BR"), "%.2f", this)
}

fun Double.formatarMoeda(): String {
    return "R$ ${this.formatarBR()}"
}

@Preview(showBackground = true)
@Composable
fun PreviewControleViagens() {
    ControledeviagensTheme {
        ControleViagensApp()
    }
}