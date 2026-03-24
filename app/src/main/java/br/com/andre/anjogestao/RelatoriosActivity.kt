package br.com.andre.anjogestao

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.content.Intent
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class RelatoriosActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var recycler: RecyclerView
    private lateinit var spinnerLojasRelatorio: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorios)

        db = DatabaseHelper(this)

        // Inicializando componentes
        recycler = findViewById(R.id.recyclerRelatorios)
        recycler.layoutManager = LinearLayoutManager(this)
        spinnerLojasRelatorio = findViewById(R.id.spinnerLojasRelatorio)

        // Configuração do Spinner de Lojas
        val lojas = arrayOf("Todas as Lojas", "Anjo - IE Auto", "Universound", "Rota do Som")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lojas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLojasRelatorio.adapter = adapter

        // Quando trocar a loja no seletor, a lista atualiza sozinha
        spinnerLojasRelatorio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                atualizarTela()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // BOTÃO GERAR PDF (Focado em cobrança de pendentes)
        findViewById<Button>(R.id.btnGerarPDF).setOnClickListener {
            val lojaSelecionada = spinnerLojasRelatorio.selectedItem.toString()
            // Buscamos apenas o que está PENDENTE (true) para a loja escolhida
            val listaParaPdf = db.buscarServicosRelatorio(lojaSelecionada, "TODOS", true)

            if (listaParaPdf.isNotEmpty()) {
                gerarEPresentarPDF(listaParaPdf)
                Toast.makeText(this, "PDF Gerado com sucesso!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Não há nada pendente para $lojaSelecionada!", Toast.LENGTH_SHORT).show()
            }
        }

        atualizarTela()
    }

    private fun atualizarTela() {
        val loja = spinnerLojasRelatorio.selectedItem.toString()
        // Agora o relatório mostra "TODOS" os serviços daquela loja para você conferir
        val listaParaAdapter = db.buscarServicosRelatorio(loja, "TODOS").toMutableList()
        recycler.adapter = ServicoAdapter(listaParaAdapter) {
            // Clique vazio: no relatório apenas visualizamos
        }
    }

    // --- FUNÇÃO DE GERAÇÃO DO PDF ---
    fun gerarEPresentarPDF(listaServicos: List<Map<String, Any>>) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Logo e Cabeçalho
        try {
            val drawable = ContextCompat.getDrawable(this, R.drawable.foto_logo_anjo)
            if (drawable != null) {
                drawable.setBounds(0, 0, 80, 80)
                canvas.save()
                canvas.translate(40f, 40f)
                drawable.draw(canvas)
                canvas.restore()
            }
        } catch (e: Exception) { e.printStackTrace() }

        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("RELATÓRIO DE COBRANÇA", 135f, 65f, paint)

        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Anjo Gestão - Insulfilm e Envelopamento", 135f, 85f, paint)
        canvas.drawText("WhatsApp/Pix (13) 97415-0044", 135f, 105f, paint)

        // Cabeçalho da Tabela
        paint.isFakeBoldText = true
        canvas.drawText("DATA", 40f, 160f, paint)
        canvas.drawText("CARRO", 120f, 160f, paint)
        canvas.drawText("DESCRIÇÃO", 220f, 160f, paint)
        canvas.drawText("VALOR", 500f, 160f, paint)

        paint.isFakeBoldText = false
        var y = 185f
        var totalGeral = 0.0

        for (item in listaServicos) {
            val valor = (item["valor"] as? Number)?.toDouble() ?: 0.0
            totalGeral += valor
            val dataLimpa = item["data"].toString().split(" ")[0]

            canvas.drawText(dataLimpa, 40f, y, paint)
            canvas.drawText(item["modelo"]?.toString() ?: "", 120f, y, paint)
            canvas.drawText(item["servico"]?.toString() ?: "", 220f, y, paint)
            canvas.drawText(String.format(Locale.getDefault(), "R$ %.2f", valor), 500f, y, paint)
            y += 20f
        }

        // Rodapé com Total
        paint.isFakeBoldText = true
        paint.textSize = 14f
        canvas.drawText("TOTAL A RECEBER: ${String.format(Locale.getDefault(), "R$ %.2f", totalGeral)}", 350f, y + 30f, paint)

        pdfDocument.finishPage(page)
        val pasta = getExternalFilesDir(null)
        val arquivo = File(pasta, "Relatorio_Cobranca_Anjo.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(arquivo))
            pdfDocument.close()
            compartilharPDF(arquivo)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun compartilharPDF(arquivo: File) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", arquivo)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Enviar Relatório via:"))
    }
}