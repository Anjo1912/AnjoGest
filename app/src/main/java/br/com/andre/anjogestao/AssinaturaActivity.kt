package br.com.andre.anjogestao

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class AssinaturaActivity : AppCompatActivity() {

    private lateinit var assinaturaView: AssinaturaView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assinatura)

        // Referenciando os IDs corretos do seu XML
        assinaturaView = findViewById(R.id.assinaturaView)

        findViewById<Button>(R.id.btnLimpar).setOnClickListener {
            assinaturaView.clear()
        }

        // DIDÁTICO: Trocado para btnConfirmar para bater com seu XML
        findViewById<Button>(R.id.btnConfirmar).setOnClickListener {
            val bitmap = viewToBitmap(assinaturaView)
            if (salvarImagemAssinatura(bitmap)) {
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun viewToBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    private fun salvarImagemAssinatura(bitmap: Bitmap): Boolean {
        return try {
            val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File(directory, "assinatura_atual.png")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}