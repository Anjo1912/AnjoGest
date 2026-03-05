package br.com.andre.anjogestao

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class AssinaturaActivity : AppCompatActivity() {
    private lateinit var assinaturaView: AssinaturaView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assinatura)

        val container = findViewById<FrameLayout>(R.id.containerAssinatura)
        assinaturaView = AssinaturaView(this)
        container.addView(assinaturaView)

        findViewById<Button>(R.id.btnLimpar).setOnClickListener {
            assinaturaView.limpar()
        }

        findViewById<Button>(R.id.btnConfirmar).setOnClickListener {
            // Aqui vamos salvar a assinatura em breve
            finish()
        }
    }
}