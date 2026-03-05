package br.com.andre.anjogestao

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class AssinaturaView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var path = Path()
    private var paint = Paint().apply {
        color = Color.BLACK      // Cor da "caneta"
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 8f         // Espessura do traço
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.moveTo(x, y)
            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
        }
        invalidate() // Força a tela a se redesenhar enquanto você risca
        return true
    }

    // Função para o botão "Limpar" que criamos no XML
    fun limpar() {
        path.reset()
        invalidate()
    }

    // Esta função será usada para salvar a assinatura no PDF depois
    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        draw(canvas)
        return bitmap
    }// Adicione isto dentro da classe AssinaturaView.kt
    fun clear() {
        path.reset() // Limpa o caminho do desenho
        invalidate() // Avisa ao Android para redesenhar a tela (agora vazia)
    }
}