package br.com.andre.anjogestao



import android.os.Bundle

import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.RecyclerView



class ListagemActivity : AppCompatActivity() {



    private lateinit var db: DatabaseHelper

    private lateinit var recycler: RecyclerView

    private lateinit var txtTitulo: TextView



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_listagem)



        db = DatabaseHelper(this)

        recycler = findViewById(R.id.recyclerTodosServicos)

        txtTitulo = findViewById(R.id.txtTituloHistorico)



        recycler.layoutManager = LinearLayoutManager(this)



// Captura a loja que o usuário escolheu na tela anterior

        val lojaSelecionada = intent.getStringExtra("LOJA_ESCOLHIDA") ?: "Todas as Lojas"

        txtTitulo.text = "Histórico: $lojaSelecionada"



// Busca no banco os serviços apenas desta loja

        val listaFiltrada = db.buscarServicosPorLojaParaLista(lojaSelecionada)
// Transformamos em MutableList para o Adapter conseguir remover itens
        recycler.adapter = ServicoAdapter(listaFiltrada.toMutableList())

    }

}