package br.com.andre.anjogestao

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ListagemActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var recycler: RecyclerView
    private lateinit var txtTitulo: TextView
    private lateinit var spinnerMeses: Spinner

    // --- CAMPOS DA META E GRÁFICO ---
    private lateinit var viewBarra1: View
    private lateinit var viewBarra2: View
    private lateinit var viewBarra3: View
    private lateinit var txtMes1: TextView
    private lateinit var txtMes2: TextView
    private lateinit var txtMes3: TextView
    private lateinit var txtProgressoMeta: TextView
    private lateinit var barraMeta: ProgressBar
    private lateinit var txtStatusMeta: TextView

    // NOVO: A linha que cruza o gráfico
    private lateinit var linhaMetaContainer: LinearLayout

    private var lojaSelecionada: String = "Todas as Lojas"
    private var mesSelecionado: String =
        SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listagem)

        db = DatabaseHelper(this)

        // Vinculando componentes
        recycler = findViewById(R.id.recyclerTodosServicos)
        txtTitulo = findViewById(R.id.txtTituloHistorico)
        txtProgressoMeta = findViewById(R.id.txtProgressoMeta)
        barraMeta = findViewById(R.id.barraProgressoMeta)
        txtStatusMeta = findViewById(R.id.txtStatusMeta)
        spinnerMeses = findViewById(R.id.spinnerMeses)

        // Vinculando barras e linha
        viewBarra1 = findViewById(R.id.viewBarra1)
        viewBarra2 = findViewById(R.id.viewBarra2)
        viewBarra3 = findViewById(R.id.viewBarra3)
        txtMes1 = findViewById(R.id.txtMes1)
        txtMes2 = findViewById(R.id.txtMes2)
        txtMes3 = findViewById(R.id.txtMes3)
        linhaMetaContainer = findViewById(R.id.linhaMetaContainer)

        recycler.layoutManager = LinearLayoutManager(this)
        lojaSelecionada = intent.getStringExtra("LOJA_ESCOLHIDA") ?: "Todas as Lojas"
        txtTitulo.text = "Histórico: $lojaSelecionada"

        configurarSeletorDeMeses()
        carregarLista()
    }

    private fun configurarSeletorDeMeses() {
        val faturamentos = db.obterFaturamentoPorMes()
        val listaMeses =
            if (faturamentos.keys.isEmpty()) listOf(mesSelecionado) else faturamentos.keys.toList()

        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaMeses)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMeses.adapter = adapterSpinner

        spinnerMeses.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                mesSelecionado = listaMeses[position]
                atualizarCardMeta(mesSelecionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun carregarLista() {
        val listaParaAdapter = db.buscarServicosPorLojaParaLista(lojaSelecionada).toMutableList()
        recycler.adapter = ServicoAdapter(listaParaAdapter) {
            atualizarCardMeta(mesSelecionado)
        }
    }

    private fun atualizarCardMeta(mesParaExibir: String) {
        val metaObjetivo = 5000.0
        val faturamentos = db.obterFaturamentoPorMes()
        val faturamentoRealizado = faturamentos[mesParaExibir] ?: 0.0

        txtProgressoMeta.text =
            "Mês $mesParaExibir: R$ ${String.format("%.2f", faturamentoRealizado)}"
        barraMeta.progress = faturamentoRealizado.toInt()

        val saldo = metaObjetivo - faturamentoRealizado
        if (saldo <= 0) {
            txtStatusMeta.text = "🎉 META BATIDA!"
            txtStatusMeta.setTextColor(getColor(android.R.color.holo_green_light))
        } else {
            txtStatusMeta.text = "Faltam R$ ${String.format("%.2f", saldo)}"
            txtStatusMeta.setTextColor(getColor(android.R.color.darker_gray))
        }

        atualizarGrafico()
    }

    private fun atualizarGrafico() {
        val faturamentos = db.obterFaturamentoPorMes()
        val listaMeses = faturamentos.keys.toList().take(3).reversed()

        // Se o faturamento for maior que 5000, o gráfico se ajusta
        val maiorValorNoBanco = faturamentos.values.maxOrNull() ?: 5000.0
        val metaFixa = 5000.0

        // Usamos o maior valor entre o que você ganhou e a sua meta para definir o topo
        val tetoDoGrafico = if (maiorValorNoBanco > metaFixa) maiorValorNoBanco else metaFixa

        val alturaMaxPx = 250 // Altura total do gráfico em pixels

        val barras = listOf(viewBarra1, viewBarra2, viewBarra3)
        val textos = listOf(txtMes1, txtMes2, txtMes3)

        // 1. Ajustar a altura das barras
        listaMeses.forEachIndexed { index, mes ->
            if (index < 3) {
                val valor = faturamentos[mes] ?: 0.0
                val alturaCalc = ((valor / tetoDoGrafico) * alturaMaxPx).toInt()

                val params = barras[index].layoutParams
                params.height = if (alturaCalc < 20) 20 else alturaCalc
                barras[index].layoutParams = params
                textos[index].text = mes.substring(0, 2)
            }
        }

        // 2. Ajustar a posição da LINHA DA META (A mágica acontece aqui)
        val posicaoLinhaPx = ((metaFixa / tetoDoGrafico) * alturaMaxPx).toInt()
        val layoutParamsLinha = linhaMetaContainer.layoutParams as FrameLayout.LayoutParams
        layoutParamsLinha.bottomMargin = posicaoLinhaPx
        linhaMetaContainer.layoutParams = layoutParamsLinha
    }
}
