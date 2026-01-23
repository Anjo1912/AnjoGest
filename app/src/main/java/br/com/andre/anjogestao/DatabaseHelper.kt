package br.com.andre.anjogestao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "AnjoGestao.db", null, 4) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE servicos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                loja TEXT, modelo TEXT, servico TEXT, valor REAL, status TEXT, data TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS servicos")
        onCreate(db)
    }

    fun salvarServico(loja: String, modelo: String, servico: String, valor: Double, status: String, data: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("loja", loja); put("modelo", modelo); put("servico", servico)
            put("valor", valor); put("status", status); put("data", data)
        }
        return db.insert("servicos", null, values) != -1L
    }

    fun buscarServicosPorLojaParaLista(nomeLoja: String): List<Map<String, Any>> {
        val lista = mutableListOf<Map<String, Any>>()
        val db = this.readableDatabase
        val query = if (nomeLoja == "Todas as Lojas") "SELECT * FROM servicos ORDER BY id DESC"
        else "SELECT * FROM servicos WHERE loja = ? ORDER BY id DESC"
        val cursor = db.rawQuery(query, if (nomeLoja == "Todas as Lojas") null else arrayOf(nomeLoja))
        if (cursor.moveToFirst()) {
            do {
                val mapa = mutableMapOf<String, Any>()
                mapa["id"] = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                mapa["loja"] = cursor.getString(cursor.getColumnIndexOrThrow("loja"))
                mapa["modelo"] = cursor.getString(cursor.getColumnIndexOrThrow("modelo"))
                mapa["servico"] = cursor.getString(cursor.getColumnIndexOrThrow("servico"))
                mapa["valor"] = cursor.getDouble(cursor.getColumnIndexOrThrow("valor"))
                mapa["status"] = cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: "Pendente"
                mapa["data"] = cursor.getString(cursor.getColumnIndexOrThrow("data"))
                lista.add(mapa)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun buscarServicosRelatorio(nomeLoja: String, periodo: String, apenasPendentes: Boolean = false): List<Map<String, Any>> {
        val lista = mutableListOf<Map<String, Any>>()
        val db = this.readableDatabase
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val hoje = sdf.format(java.util.Date())

        var query = "SELECT * FROM servicos WHERE 1=1"
        val args = mutableListOf<String>()

        if (nomeLoja != "Todas as Lojas") {
            query += " AND loja = ?"
            args.add(nomeLoja)
        }

        if (apenasPendentes) {
            query += " AND (status = 'Pendente' OR status IS NULL OR status = '')"
        }

        if (periodo == "HOJE") {
            query += " AND data LIKE ?"
            args.add("$hoje%")
        } else if (periodo == "SEMANA") {
            query += " AND substr(data,7,4)||'-'||substr(data,4,2)||'-'||substr(data,1,2) >= date('now', '-7 days')"
        }

        query += " ORDER BY id DESC"

        val cursor = db.rawQuery(query, if (args.isEmpty()) null else args.toTypedArray())
        if (cursor.moveToFirst()) {
            do {
                // Dentro de buscarServicosRelatorio no DatabaseHelper:
                val mapa = mutableMapOf<String, Any>()
                mapa["id"] = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                mapa["loja"] = cursor.getString(cursor.getColumnIndexOrThrow("loja"))
                mapa["modelo"] = cursor.getString(cursor.getColumnIndexOrThrow("modelo"))
                mapa["servico"] = cursor.getString(cursor.getColumnIndexOrThrow("servico"))
                mapa["valor"] = cursor.getDouble(cursor.getColumnIndexOrThrow("valor"))
// ESTA LINHA ABAIXO É A MAIS IMPORTANTE:
                mapa["status"] = cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: "Pendente"
                mapa["data"] = cursor.getString(cursor.getColumnIndexOrThrow("data"))
                lista.add(mapa)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    // FUNÇÃO QUE ESTAVA FALTANDO E DANDO ERRO NA RELATORIOSACTIVITY
    fun atualizarStatus(id: Int, novoStatus: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("status", novoStatus)
        db.update("servicos", values, "id = ?", arrayOf(id.toString()))
    }

    fun excluirServico(id: Int): Boolean {
        return this.writableDatabase.delete("servicos", "id = ?", arrayOf(id.toString())) > 0
    }
}