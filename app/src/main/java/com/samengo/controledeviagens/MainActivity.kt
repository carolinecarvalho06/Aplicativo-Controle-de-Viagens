@file:Suppress("DEPRECATION")

package com.samengo.controledeviagens

import android.content.Context
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.samengo.controledeviagens.ui.theme.ControledeviagensTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias")
val NOME_MOTORISTA = stringPreferencesKey("nome_motorista")

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

@Composable
fun ControleViagensApp() {
    val navController = rememberNavController()
    val viagens = remember { mutableStateListOf<Viagem>() }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "lista",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("lista") {
                TelaListaViagens(
                    viagens = viagens,
                    navController = navController
                )
            }

            composable("cadastro") {
                TelaCadastroViagem(
                    onAdicionarViagem = { viagem ->
                        viagens.add(viagem)
                        navController.navigateUp()
                    },
                    navController = navController
                )
            }
        }
    }
}

// -------------------------------------------------------------
// COMPONENTE: MEU PERFIL (PREFERENCES DATASTORE)
// -------------------------------------------------------------
@Composable
fun MeuPerfil(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nome by remember { mutableStateOf("") }

    val nomeFlow = remember(context) {
        context.dataStore.data.map { preferences ->
            preferences[NOME_MOTORISTA] ?: ""
        }
    }

    val nomeSalvo by nomeFlow.collectAsState(initial = "")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Perfil do Motorista",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome do motorista") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    scope.launch {
                        context.dataStore.edit { preferences ->
                            preferences[NOME_MOTORISTA] = nome
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar Motorista")
            }

            Text(
                text = if (nomeSalvo.isNotBlank()) "Motorista salvo: $nomeSalvo" else "Nenhum motorista salvo ainda",
                style = MaterialTheme.typography.bodyLarge
            )
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

        MeuPerfil()

        Button(
            onClick = { navController.navigate("cadastro") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nova Viagem")
        }

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
                Text("Total de quilómetros: ${totalKm.formatarBR()} km")
                Text("Total de litros: ${totalLitros.formatarBR()} L")
                Text("Total gasto: ${totalGasto.formatarMoeda()}")
                Text("Média de consumo: ${mediaKmLitro.formatarBR()} km/l")
            }
        }

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
                    Text("Portagens: ${viagem.pedagio.formatarMoeda()}")
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
    onAdicionarViagem: (Viagem) -> Unit,
    navController: NavController
) {
    var data by remember { mutableStateOf("") }
    var kmInicial by remember { mutableStateOf("") }
    var kmFinal by remember { mutableStateOf("") }
    var litros by remember { mutableStateOf("") }
    var combustivel by remember { mutableStateOf("") }
    var valorLitro by remember { mutableStateOf("") }
    var pedagio by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }

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
            onValueChange = { data = it },
            label = { Text("Data da viagem") },
            placeholder = { Text("DD/MM/AAAA") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = kmInicial,
            onValueChange = { kmInicial = it },
            label = { Text("Quilometragem inicial") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        OutlinedTextField(
            value = kmFinal,
            onValueChange = { kmFinal = it },
            label = { Text("Quilometragem final") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        OutlinedTextField(
            value = litros,
            onValueChange = { litros = it },
            label = { Text("Litros abastecidos/consumidos") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        OutlinedTextField(
            value = combustivel,
            onValueChange = { combustivel = it },
            label = { Text("Tipo de combustível") },
            placeholder = { Text("Gasolina, etanol, diesel...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = valorLitro,
            onValueChange = { valorLitro = it },
            label = { Text("Valor por litro (R$)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        OutlinedTextField(
            value = pedagio,
            onValueChange = { pedagio = it },
            label = { Text("Total de portagens (R$)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Button(
            onClick = {
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
                    val novaViagem = Viagem(
                        data = data,
                        kmInicial = inicial,
                        kmFinal = final,
                        litros = qtdLitros,
                        combustivel = combustivel,
                        valorLitro = precoLitro,
                        pedagio = valorPedagio
                    )
                    onAdicionarViagem(novaViagem)
                }
            },
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
    return String.format(Locale.forLanguageTag("pt-BR"), "%.2f", this)
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
