package br.com.andre.anjogestao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Subimos para a versão 12 para forçar o Android a criar as tabelas corretamente
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "AnjoGestao.db", null, 12) {

    override fun onCreate(db: SQLiteDatabase) {
        // Tabela de Serviços
        db.execSQL("CREATE TABLE IF NOT EXISTS servicos (id INTEGER PRIMARY KEY AUTOINCREMENT, loja TEXT, modelo TEXT, servico TEXT, valor REAL, status TEXT, data TEXT)")

        // Tabela de Vistorias
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
            // Se a tabela de vistorias não existia nas versões anteriores, criamos agora
            db.execSQL("DROP TABLE IF EXISTS vistorias")
            onCreate(db)
        }
    }

    // --- 1. SALVAR NOVO SERVIÇO ---
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
        val resultado = db.insert("servicos", null, v)
        if (resultado == -1L) Log.e("DB_ERROR", "Falha ao inserir serviço")
        return resultado != -1L
    }

    // --- 2. ATUALIZAR SERVIÇO (EDIÇÃO) ---
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

    // --- 3. ATUALIZAR STATUS (PAGO/PENDENTE) ---
    fun atualizarStatus(id: Int, novoStatus: String): Boolean {
        val db = this.writableDatabase
        val v = ContentValues().apply { put("status", novoStatus) }
        return db.update("servicos", v, "id = ?", arrayOf(id.toString())) > 0
    }

    // --- 4. BUSCAR PARA LISTAGEM E RELATÓRIO ---
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

    // --- 5. SALVAR VISTORIA (FOTOS E ASSINATURA) ---
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
        val resultado = db.insert("vistorias", null, v)
        return resultado != -1L
    }
}