package br.com.andre.anjogestao

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Botão para abrir Novo Serviço
        findViewById<Button>(R.id.btnnovoservico).setOnClickListener {
            startActivity(Intent(this, novoservico::class.java))
        }

        // Botão para Gerar Relatórios
        findViewById<Button>(R.id.btnRelatorios).setOnClickListener {
            startActivity(Intent(this, RelatoriosActivity::class.java))
        }

        // --- NOVO: Botão para abrir a Área Cliente Black ---
        findViewById<Button>(R.id.btnClienteBlack).setOnClickListener {
            val intent = Intent(this, ClienteBlackActivity::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnAbrirCheckIn).setOnClickListener {
            val intent = Intent(this, VistoriaActivity::class.java)
            startActivity(intent)
        }

        // Botão CONSULTAR HISTÓRICO com seletor de loja
        findViewById<Button>(R.id.btnVerServicos).setOnClickListener {
            val lojas = arrayOf("Todas as Lojas", "Anjo - IE Auto", "Universound", "Rota do Som")

            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Consultar qual loja?")
            builder.setItems(lojas) { _, which ->
                val lojaSelecionada = lojas[which]

                // Passa a loja escolhida para a tela de listagem
                val intent = Intent(this, ListagemActivity::class.java)
                intent.putExtra("LOJA_ESCOLHIDA", lojaSelecionada)
                startActivity(intent)
            }
            builder.show()
        }
    }
}