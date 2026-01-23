package br.com.andre.anjogestao

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class ServicoAdapter(private var lista: MutableList<Map<String, Any>>) :
    RecyclerView.Adapter<ServicoAdapter.ServicoViewHolder>() {

    class ServicoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtModelo: TextView = view.findViewById(R.id.txtItemModelo)
        val txtServico: TextView = view.findViewById(R.id.txtItemServico)
        val txtValor: TextView = view.findViewById(R.id.txtItemValor)
        val txtData: TextView = view.findViewById(R.id.txtItemData)
        val txtLoja: TextView = view.findViewById(R.id.txtItemLoja)
        // CORREÇÃO 1: Adicionado o campo de Status que faltava aqui
        val txtStatus: TextView = view.findViewById(R.id.txtItemStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_servico, parent, false)
        return ServicoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServicoViewHolder, position: Int) {
        val item = lista[position]
        val id = item["id"] as? Int ?: -1

        // Pega o status do banco de dados
        val statusAtual = item["status"]?.toString() ?: "Pendente"

        // CORREÇÃO 2: Define o texto e a cor corretamente
        holder.txtStatus.text = statusAtual

        if (statusAtual == "Pago") {
            holder.txtStatus.setTextColor(Color.parseColor("#4CAF50"))  // Verde
        } else {
            holder.txtStatus.setTextColor(Color.parseColor("#FF5252")) // Vermelho
        }
        holder.txtStatus.setOnClickListener {
            val context = holder.itemView.context
            if (statusAtual == "Pendente") {
                AlertDialog.Builder(context)
                    .setTitle("Confirmar Pagamento")
                    .setMessage("Deseja marcar o serviço do ${holder.txtModelo.text} como PAGO?")
                    .setPositiveButton("Sim, pago") { _, _ ->
                        val db = DatabaseHelper(context)
                        db.atualizarStatus(id, "Pago") // Muda no banco

                        // Atualiza a cor e o texto na tela na hora
                        holder.txtStatus.text = "Pago"
                        holder.txtStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                        Toast.makeText(context, "Status atualizado!", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Ainda não", null)
                    .show()
            }
        }

        holder.txtModelo.text = item["modelo"]?.toString() ?: "Sem Modelo"
        holder.txtServico.text = item["servico"]?.toString() ?: "Sem Descrição"
        holder.txtLoja.text = item["loja"]?.toString() ?: "Loja N/A"

        val valor = item["valor"] as? Double ?: 0.0
        holder.txtValor.text = String.format("R$ %.2f", valor)
        holder.txtData.text = item["data"]?.toString() ?: ""

        // Segurar o dedo para excluir
        holder.itemView.setOnLongClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
                .setTitle("Excluir Serviço")
                .setMessage("Deseja apagar o registro do ${holder.txtModelo.text}?")
                .setPositiveButton("Sim, excluir") { _, _ ->
                    val db = DatabaseHelper(context)
                    if (db.excluirServico(id)) {
                        lista.removeAt(holder.adapterPosition)
                        notifyItemRemoved(holder.adapterPosition)
                        Toast.makeText(context, "Excluído com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }
    }

    override fun getItemCount() = lista.size
}