package br.com.andre.anjogestao

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import java.text.SimpleDateFormat
import java.util.*

class novoservico : AppCompatActivity() {

    private var valorTotal = 0.0
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novo_servico)

        db = DatabaseHelper(this)

        // Elementos Básicos
        val spinnerLojas = findViewById<Spinner>(R.id.spinnerLojas)
        val editModelo = findViewById<AutoCompleteTextView>(R.id.editModelo)
        val txtTotal = findViewById<TextView>(R.id.txtTotal)
        val btnSalvar = findViewById<Button>(R.id.btnSalvarServico)

        // Checkboxes Insulfilm
        val cbSemParaBrisa = findViewById<CheckBox>(R.id.cbSemParaBrisa)
        val cbCompleto = findViewById<CheckBox>(R.id.cbCompleto)
        val cbParaBrisa = findViewById<CheckBox>(R.id.cbParaBrisa)
        val cbTraseiro = findViewById<CheckBox>(R.id.cbTraseiro)

        // Elementos Envelopamento
        val cbEnvelopamento = findViewById<CheckBox>(R.id.cbEnvelopamento)
        val layoutExtraEnv = findViewById<LinearLayout>(R.id.layoutExtraEnvelopamento)
        val editDetalheEnv = findViewById<EditText>(R.id.editDetalheEnvelopamento)
        val editValorEnv = findViewById<EditText>(R.id.editValorEnvelopamento)

        // Elementos Por Porta
        val cbPorPorta = findViewById<CheckBox>(R.id.cbPorPorta)
        val layoutExtraPorta = findViewById<LinearLayout>(R.id.layoutExtraPorta)
        val editQtdPortas = findViewById<EditText>(R.id.editQtdPortas)

        // Elementos Extraordinários
        val cbExtraordinario = findViewById<CheckBox>(R.id.cbExtraordinario)
        val layoutExtraExtra = findViewById<LinearLayout>(R.id.layoutExtraExtra)
        val editDetalheExtra = findViewById<EditText>(R.id.editDetalheExtra)
        val editValorExtra = findViewById<EditText>(R.id.editValorExtra)

        // Adapter do Spinner
        val lojas = arrayOf("Anjo - IE Auto", "Universound", "Rota do Som")
        val adapterLojas = ArrayAdapter(this, R.layout.item_loja_spinner, lojas)
        spinnerLojas.adapter = adapterLojas

        val checksFixo = listOf(cbSemParaBrisa, cbCompleto, cbParaBrisa, cbTraseiro)

        // FUNÇÃO DE CÁLCULO
        fun calcular() {
            valorTotal = 0.0

            // 1. Fixos Insulfilm
            if (cbSemParaBrisa.isChecked) valorTotal += 100.0
            if (cbCompleto.isChecked) valorTotal += 150.0
            if (cbParaBrisa.isChecked) valorTotal += 50.0
            if (cbTraseiro.isChecked) valorTotal += 50.0

            // 2. Envelopamento
            if (cbEnvelopamento.isChecked) {
                valorTotal += editValorEnv.text.toString().toDoubleOrNull() ?: 0.0
            }

            // 3. Por Porta (R$ 30 cada)
            if (cbPorPorta.isChecked) {
                val qtd = editQtdPortas.text.toString().toIntOrNull() ?: 0
                valorTotal += (qtd * 30.0)
            }

            // 4. Extraordinários
            if (cbExtraordinario.isChecked) {
                valorTotal += editValorExtra.text.toString().toDoubleOrNull() ?: 0.0
            }

            txtTotal.text = String.format("R$ %.2f", valorTotal)
        }

        // CONFIGURAÇÃO DOS OUVIINTES
        checksFixo.forEach { it.setOnCheckedChangeListener { _, _ -> calcular() } }

        cbEnvelopamento.setOnCheckedChangeListener { _, isChecked ->
            layoutExtraEnv.visibility = if (isChecked) View.VISIBLE else View.GONE
            calcular()
        }
        cbPorPorta.setOnCheckedChangeListener { _, isChecked ->
            layoutExtraPorta.visibility = if (isChecked) View.VISIBLE else View.GONE
            calcular()
        }
        cbExtraordinario.setOnCheckedChangeListener { _, isChecked ->
            layoutExtraExtra.visibility = if (isChecked) View.VISIBLE else View.GONE
            calcular()
        }

        // Atualizar total ao digitar valores ou quantidades
        editValorEnv.addTextChangedListener { calcular() }
        editQtdPortas.addTextChangedListener { calcular() }
        editValorExtra.addTextChangedListener { calcular() }

        // SALVAR
        btnSalvar.setOnClickListener {
            val lojaSelecionada = spinnerLojas.selectedItem.toString()
            val modeloDigitado = editModelo.text.toString()

            if (modeloDigitado.isEmpty() || valorTotal == 0.0) {
                Toast.makeText(this, "Preencha o modelo e escolha um serviço!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Montar Descrição Final
            val servicos = mutableListOf<String>()
            if (cbSemParaBrisa.isChecked) servicos.add("S/ Para-brisa")
            if (cbCompleto.isChecked) servicos.add("Completo")
            if (cbParaBrisa.isChecked) servicos.add("Apenas Para-brisa")
            if (cbTraseiro.isChecked) servicos.add("Apenas Traseiro")
            if (cbPorPorta.isChecked) servicos.add("${editQtdPortas.text} Portas")
            if (cbEnvelopamento.isChecked) servicos.add("Env: ${editDetalheEnv.text}")
            if (cbExtraordinario.isChecked) servicos.add("Extra: ${editDetalheExtra.text}")

            val servicosFinal = servicos.joinToString(", ")
            val dataAtual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            AlertDialog.Builder(this)
                .setTitle("Confirmar Registro")
                .setMessage("Loja: $lojaSelecionada\nCarro: $modeloDigitado\nTotal: R$ ${String.format("%.2f", valorTotal)}")
                .setPositiveButton("Salvar") { _, _ ->
                    val sucesso = db.salvarServico(lojaSelecionada, modeloDigitado, servicosFinal, valorTotal, "Pendente", dataAtual)
                    if (sucesso) {
                        Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .setNegativeButton("Editar", null)
                .show()
        }
    }
}