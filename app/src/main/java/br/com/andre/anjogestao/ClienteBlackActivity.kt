package br.com.andre.anjogestao

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Color
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class ClienteBlackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cliente_black)

        val editNome = findViewById<EditText>(R.id.editNomeClienteBlack)
        val editModelo = findViewById<EditText>(R.id.editModeloBlack)
        val rbCeramica = findViewById<RadioButton>(R.id.rbCeramica)
        val btnGerar = findViewById<Button>(R.id.btnGerarCartaoBlack)

        btnGerar.setOnClickListener {
            val nome = editNome.text.toString()
            val modelo = editModelo.text.toString()

            if (nome.isEmpty() || modelo.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Define a linha e a garantia com base na seleção
            val linha: String
            val garantia: String

            if (rbCeramica.isChecked) {
                linha = "Nano Cerâmica"
                garantia = "10 Anos"
            } else {
                linha = "Nano Carbono"
                garantia = "5 Anos"
            }

            Toast.makeText(this, "Gerando Certificado Exclusivo...", Toast.LENGTH_SHORT).show()

            // Chama a função passando os dados
            gerarPdfBlack(nome, modelo, linha, garantia)
        }
    }

    private fun gerarPdfBlack(nome: String, modelo: String, linha: String, garantia: String) {
        val pdfDocument = PdfDocument()
        val paint = Paint()

        // 1. Lógica de troca de imagem: identifica qual fundo usar
        val idImagemFundo = if (linha.contains("Cerâmica")) {
            R.drawable.fundo_nano_ceramico
        } else {
            R.drawable.fundo_nano_carbono
        }

        // 2. Carrega a imagem da pasta drawable
        val bitmapFundo = BitmapFactory.decodeResource(resources, idImagemFundo)

        // 3. Define o tamanho da página do PDF baseado na sua imagem do Canva
        val pageInfo = PdfDocument.PageInfo.Builder(bitmapFundo.width, bitmapFundo.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // 4. Desenha a sua arte de fundo
        canvas.drawBitmap(bitmapFundo, 0f, 0f, null)

        // 5. Configura o texto para o Nome do Cliente
        paint.color = Color.WHITE // Cor do texto
        paint.textSize = 55f       // Ajuste o tamanho conforme necessário
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT

        // Escreve o nome centralizado (Ajuste o valor 750f para subir ou descer o texto)
        val centroX = (bitmapFundo.width / 2).toFloat()
        canvas.drawText(nome.uppercase(), 700f, 1050f, paint)


        // 6. Configura o texto para o Modelo e Garantia
        paint.textSize = 35f
        paint.isFakeBoldText = false
        canvas.drawText("VEÍCULO: ${modelo.uppercase()}", 700f, 1100f, paint)

        paint.color = Color.parseColor("#C5A059") // Dourado mais elegante
        paint.isFakeBoldText = true
        canvas.drawText("GARANTIA ASSEGURADA:", 700f, 1150f, paint)

        pdfDocument.finishPage(page)

        // 7. Salvar e Compartilhar
        val arquivo = File(getExternalFilesDir(null), "Certificado_Black_${nome.replace(" ", "_")}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(arquivo))
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", arquivo)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Enviar Certificado Black:"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao gerar PDF", Toast.LENGTH_SHORT).show()
        }
    }
}