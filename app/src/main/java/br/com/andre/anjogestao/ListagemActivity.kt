package br.com.andre.anjogestao

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListagemActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var recycler: RecyclerView
    private lateinit var txtTitulo: TextView
    private var lojaSelecionada: String = "Todas as Lojas"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listagem)

        db = DatabaseHelper(this)
        recycler = findViewById(R.id.recyclerTodosServicos)
        txtTitulo = findViewById(R.id.txtTituloHistorico)
        recycler.layoutManager = LinearLayoutManager(this)

        lojaSelecionada = intent.getStringExtra("LOJA_ESCOLHIDA") ?: "Todas as Lojas"
        txtTitulo.text = "Histórico: $lojaSelecionada"

        carregarLista()
    }

    private fun carregarLista() {
        // AJUSTE DIDÁTICO: O nome da variável deve ser o mesmo usado no adapter abaixo
        val listaParaAdapter = db.buscarServicosPorLojaParaLista(lojaSelecionada).toMutableList()

        recycler.adapter = ServicoAdapter(listaParaAdapter) {

        }
    }

    private fun mostrarDialogoStatus(servico: Map<String, Any>) {
        val id = servico["id"] as Int
        val statusAtual = servico["status"]?.toString() ?: "Pendente"

        // DIDÁTICO: Definimos o novo status ANTES de abrir o diálogo
        val novoStatus = if (statusAtual == "Pago") "Pendente" else "Pago"

        // Criamos a mensagem personalizada baseada no estado atual
        val mensagem = "O serviço está como '$statusAtual'.\nDeseja alterar para '$novoStatus'?"

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alterar Status")
        builder.setMessage(mensagem)

        // SÓ MUDA SE CLICAR EM "SIM"
        builder.setPositiveButton("Sim, alterar") { _, _ ->
            if (db.atualizarStatus(id, novoStatus)) {
                Toast.makeText(this, "Status atualizado para $novoStatus", Toast.LENGTH_SHORT).show()
                carregarLista() // Atualiza a tela do seu S24 Plus
            } else {
                Toast.makeText(this, "Erro ao atualizar no banco", Toast.LENGTH_SHORT).show()
            }
        }

        // BOTÃO CANCELAR: Não faz nada (apenas fecha o diálogo)
        builder.setNegativeButton("Sair") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
}