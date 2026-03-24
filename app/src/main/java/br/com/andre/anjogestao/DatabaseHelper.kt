package br.com.andre.anjogestao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "AnjoGestao.db", null, 12) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS servicos (id INTEGER PRIMARY KEY AUTOINCREMENT, loja TEXT, modelo TEXT, servico TEXT, valor REAL, status TEXT, data TEXT)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS vistorias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente TEXT, placa TEXT, km TEXT,
                path_p_off TEXT, path_p_on TEXT, path_frente TEXT, path_tras TEXT,
                path_assinatura TEXT, observacoes TEXT, data_hora TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 12) {
            db.execSQL("DROP TABLE IF EXISTS servicos")
            db.execSQL("DROP TABLE IF EXISTS vistorias")
            onCreate(db)
        }
    }

    // --- SERVIÇOS ---
    fun salvarServico(loja: String, modelo: String, servico: String, valor: Double, status: String, data: String): Boolean {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put("loja", loja)
            put("modelo", modelo)
            put("servico", servico)
            put("valor", valor)
            put("status", status)
            put("data", data)
        }
        return db.insert("servicos", null, v) != -1L
    }

    fun atualizarServicoCompleto(id: Int, loja: String, modelo: String, servico: String, valor: Double): Boolean {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put("loja", loja)
            put("modelo", modelo)
            put("servico", servico)
            put("valor", valor)
        }
        return db.update("servicos", v, "id = ?", arrayOf(id.toString())) > 0
    }

    fun atualizarStatus(id: Int, novoStatus: String): Boolean {
        val db = this.writableDatabase
        val v = ContentValues().apply { put("status", novoStatus) }
        return db.update("servicos", v, "id = ?", arrayOf(id.toString())) > 0
    }

    fun buscarServicosRelatorio(loja: String, periodo: String, apenasPendentes: Boolean = false): List<Map<String, Any>> {
        val lista = mutableListOf<Map<String, Any>>()
        val db = this.readableDatabase
        var query = "SELECT * FROM servicos WHERE 1=1"
        if (loja != "Todas as Lojas") query += " AND loja = '$loja'"
        if (apenasPendentes) query += " AND (status = 'Pendente' OR status IS NULL OR status = '')"
        query += " ORDER BY id DESC"

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val mapa = mutableMapOf<String, Any>()
                mapa["id"] = cursor.getInt(0)
                mapa["loja"] = cursor.getString(1)
                mapa["modelo"] = cursor.getString(2)
                mapa["servico"] = cursor.getString(3)
                mapa["valor"] = cursor.getDouble(4)
                mapa["status"] = cursor.getString(5) ?: "Pendente"
                mapa["data"] = cursor.getString(6)
                lista.add(mapa)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun buscarServicosPorLojaParaLista(loja: String) = buscarServicosRelatorio(loja, "TODOS", false)

    fun excluirServico(id: Int) = this.writableDatabase.delete("servicos", "id = ?", arrayOf(id.toString())) > 0

    // --- FATURAMENTO PARA O RELATÓRIO E GRÁFICO ---
    fun obterFaturamentoPorMes(): Map<String, Double> {
        val dados = mutableMapOf<String, Double>()
        val db = this.readableDatabase
        // CORREÇÃO: "ORDER" com 'O' e não com zero '0'. Removido vírgula extra.
        val query = "SELECT substr(data, 4, 7) as mes_ano, SUM(valor) as total FROM servicos WHERE status = 'Pago' GROUP BY mes_ano ORDER BY id DESC LIMIT 6"

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val mesAno = cursor.getString(0) // Ex: 03/2026
                val total = cursor.getDouble(1)
                dados[mesAno] = total
            } while (cursor.moveToNext())
        }
        cursor.close()
        return dados
    }

    // --- VISTORIAS ---
    fun salvarVistoriaCompleta(cliente: String, placa: String, km: String, pOff: String?, pOn: String?, frente: String?, tras: String?, ass: String?, obs: String): Boolean {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put("cliente", cliente)
            put("placa", placa)
            put("km", km)
            put("path_p_off", pOff)
            put("path_p_on", pOn)
            put("path_frente", frente)
            put("path_tras", tras)
            put("path_assinatura", ass)
            put("observacoes", obs)
            put("data_hora", SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()))
        }
        return db.insert("vistorias", null, v) != -1L
    }
}