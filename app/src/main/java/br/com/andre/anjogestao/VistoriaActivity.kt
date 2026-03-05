package br.com.andre.anjogestao

import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class VistoriaActivity : AppCompatActivity() {

    private var pathPainelOff: String? = null
    private var pathPainelOn: String? = null
    private var pathFrente: String? = null
    private var pathTraseira: String? = null
    private var currentPhotoPath: String? = null
    private var lastButtonClicked: Int = 0

    // Launcher para a Câmera
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val btn = findViewById<Button>(lastButtonClicked)
            btn.setBackgroundColor(Color.GREEN)
            btn.setTextColor(Color.BLACK)
            btn.text = "${btn.text} OK"
            when (lastButtonClicked) {
                R.id.btnFotoPainelOff -> pathPainelOff = currentPhotoPath
                R.id.btnFotoPainelOn -> pathPainelOn = currentPhotoPath
                R.id.btnFotoFrente -> pathFrente = currentPhotoPath
                R.id.btnFotoTras -> pathTraseira = currentPhotoPath
            }
        }
    }

    // Launcher para a Assinatura
    private val assinaturaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val btn = findViewById<Button>(R.id.btnAbrirTelaAssinatura)
        btn.setBackgroundColor(Color.GREEN)
        btn.setTextColor(Color.BLACK)
        btn.text = "ASSINATURA COLETADA OK"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_vistoria)

        setupBotaoFoto(R.id.btnFotoPainelOff)
        setupBotaoFoto(R.id.btnFotoPainelOn)
        setupBotaoFoto(R.id.btnFotoFrente)
        setupBotaoFoto(R.id.btnFotoTras)

        findViewById<Button>(R.id.btnAbrirTelaAssinatura).setOnClickListener {
            val intent = Intent(this, AssinaturaActivity::class.java)
            assinaturaLauncher.launch(intent)
        }

        findViewById<Button>(R.id.btnIrParaAssinatura).setOnClickListener {
            val placa = findViewById<EditText>(R.id.editPlacaVistoria).text.toString()
            if (placa.isNotEmpty()) {
                salvarNoBanco() // Salva antes de gerar o PDF
                gerarPdfVistoria()
            } else {
                Toast.makeText(this, "Preencha ao menos a placa", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun salvarNoBanco() {
        val cliente = findViewById<EditText>(R.id.editNomeVistoria).text.toString()
        val placa = findViewById<EditText>(R.id.editPlacaVistoria).text.toString()
        val km = findViewById<EditText>(R.id.editKmVistoria).text.toString()
        val obs = findViewById<EditText>(R.id.editObsVistoria).text.toString()

        val db = DatabaseHelper(this)
        // DIDÁTICO: Aqui usamos o nome correto da função que está no seu DatabaseHelper
        db.salvarVistoriaCompleta(
            cliente, placa, km,
            pathPainelOff, pathPainelOn, pathFrente, pathTraseira,
            null, obs
        )
    }

    private fun gerarPdfVistoria() {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // 1. LOGO
        try {
            val bitmapLogo = BitmapFactory.decodeResource(resources, R.drawable.foto_logo_anjo)
            val logoScaled = Bitmap.createScaledBitmap(bitmapLogo, 80, 80, false)
            canvas.drawBitmap(logoScaled, 40f, 40f, paint)
        } catch (e: Exception) {
            canvas.drawText("[Logo não encontrado]", 40f, 40f, paint)
        }

        // 2. DADOS
        val cliente = findViewById<EditText>(R.id.editNomeVistoria).text.toString()
        val placa = findViewById<EditText>(R.id.editPlacaVistoria).text.toString()
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("CHECK-LIST DE VISTORIA", 150f, 60f, paint)
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Cliente: $cliente | Placa: $placa", 50f, 150f, paint)

        // 3. FUNÇÃO PARA DESENHAR FOTOS NO PDF
        fun imprimirNoPdf(caminho: String?, x: Float, y: Float, titulo: String) {
            if (!caminho.isNullOrEmpty()) {
                val bitmap = BitmapFactory.decodeFile(caminho)
                if (bitmap != null) {
                    val fotoScaled = Bitmap.createScaledBitmap(bitmap, 200, 150, true)
                    canvas.drawBitmap(fotoScaled, x, y, paint)
                    canvas.drawText(titulo, x, y - 5f, paint)
                } else {
                    canvas.drawText("Erro ao carregar $titulo", x, y, paint)
                }
            }
        }

        imprimirNoPdf(pathPainelOff, 50f, 200f, "Painel OFF")
        imprimirNoPdf(pathPainelOn, 310f, 200f, "Painel ON")
        imprimirNoPdf(pathFrente, 50f, 380f, "Frente")
        imprimirNoPdf(pathTraseira, 310f, 380f, "Traseira")

        // 4. ASSINATURA
        val pathAss = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + "/assinatura_atual.png"
        val fileAss = File(pathAss)
        if (fileAss.exists()) {
            val bAss = BitmapFactory.decodeFile(fileAss.absolutePath)
            val bScaled = Bitmap.createScaledBitmap(bAss, 200, 100, false)
            canvas.drawBitmap(bScaled, 50f, 600f, paint)
            canvas.drawText("Assinatura do Cliente", 50f, 595f, paint)
        }

        pdfDocument.finishPage(page)

        val directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val filePdf = File(directory, "Vistoria_${placa}.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(filePdf))
            pdfDocument.close()
            compartilharPdf(filePdf)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compartilharPdf(file: File) {
        val uri = FileProvider.getUriForFile(this, "br.com.andre.anjogestao.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Enviar via:"))
    }

    private fun setupBotaoFoto(id: Int) {
        findViewById<Button>(id).setOnClickListener {
            lastButtonClicked = id
            tirarFoto()
        }
    }

    private fun tirarFoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try { criarArquivoImagem() } catch (ex: Exception) { null }
        photoFile?.let {
            val photoURI: Uri = FileProvider.getUriForFile(this, "br.com.andre.anjogestao.provider", it)
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            cameraLauncher.launch(intent)
        }
    }

    private fun criarArquivoImagem(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("VISTORIA_${timeStamp}_", ".jpg", storageDir)
    }
}