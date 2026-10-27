package com.samengo.controledeviagens

import android.content.ContentValues
import android.content.Context

data class Viagem(
    val id: Int = 0,
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

class ViagemRepository(context: Context) {

    private val helper = BancoHelper(context.applicationContext)

    fun inserir(viagem: Viagem): Boolean {
        val db = helper.writableDatabase

        val valores = ContentValues().apply {
            put("data", viagem.data)
            put("km_inicial", viagem.kmInicial)
            put("km_final", viagem.kmFinal)
            put("litros", viagem.litros)
            put("combustivel", viagem.combustivel)
            put("valor_litro", viagem.valorLitro)
            put("pedagio", viagem.pedagio)
        }

        val resultado = db.insert("viagem", null, valores)
        return resultado != -1L
    }

    fun listar(): List<Viagem> {
        val db = helper.readableDatabase
        val viagens = mutableListOf<Viagem>()

        val cursor = db.rawQuery("SELECT * FROM viagem", null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val data = it.getString(it.getColumnIndexOrThrow("data"))
                val kmInicial = it.getDouble(it.getColumnIndexOrThrow("km_inicial"))
                val kmFinal = it.getDouble(it.getColumnIndexOrThrow("km_final"))
                val litros = it.getDouble(it.getColumnIndexOrThrow("litros"))
                val combustivel = it.getString(it.getColumnIndexOrThrow("combustivel"))
                val valorLitro = it.getDouble(it.getColumnIndexOrThrow("valor_litro"))
                val pedagio = it.getDouble(it.getColumnIndexOrThrow("pedagio"))

                viagens.add(
                    Viagem(
                        id = id,
                        data = data,
                        kmInicial = kmInicial,
                        kmFinal = kmFinal,
                        litros = litros,
                        combustivel = combustivel,
                        valorLitro = valorLitro,
                        pedagio = pedagio
                    )
                )
            }
        }

        return viagens
    }
}
