package br.com.andre.anjogestao

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class ServicoAdapter(
    private val lista: MutableList<Map<String, Any>>,
    private val onStatusChanged: (Map<String, Any>) -> Unit // Apenas para avisar a Activity de mudanças
) : RecyclerView.Adapter<ServicoAdapter.ServicoViewHolder>() {

    class ServicoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtModelo: TextView = view.findViewById(R.id.txtItemModelo)
        val txtServico: TextView = view.findViewById(R.id.txtItemServico)
        val txtValor: TextView = view.findViewById(R.id.txtItemValor)
        val txtStatus: TextView = view.findViewById(R.id.txtItemStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicoViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_servico, parent, false)
        return ServicoViewHolder(v)
    }

    override fun onBindViewHolder(holder: ServicoViewHolder, position: Int) {
        val item = lista[position]
        val context = holder.itemView.context
        val db = DatabaseHelper(context)

        val id = item["id"] as Int
        val loja = item["loja"] as String
        val modelo = item["modelo"] as String
        val servico = item["servico"] as String
        val valor = item["valor"] as Double
        val status = item["status"] as String

        holder.txtModelo.text = modelo
        holder.txtServico.text = servico
        holder.txtValor.text = "R$ ${String.format("%.2f", valor)}"
        holder.txtStatus.text = status

        holder.itemView.setOnLongClickListener {
            val opcoes = arrayOf("Editar Serviço", "Excluir Serviço", "Mudar Status", "Cancelar")

            AlertDialog.Builder(context)
                .setTitle("Opções do Serviço")
                .setItems(opcoes) { _, which ->
                    when (which) {
                        0 -> { // EDITAR
                            val intent = Intent(context, novoservico::class.java)
                            intent.putExtra("ID_SERVICO", id); intent.putExtra("LOJA", loja)
                            intent.putExtra("MODELO", modelo); intent.putExtra("SERVICO", servico)
                            intent.putExtra("VALOR", valor)
                            context.startActivity(intent)
                        }
                        1 -> { // EXCLUIR
                            AlertDialog.Builder(context)
                                .setTitle("Confirmar Exclusão")
                                .setMessage("Deseja realmente apagar este serviço de $modelo?")
                                .setPositiveButton("Sim") { _, _ ->
                                    if (db.excluirServico(id)) {
                                        lista.removeAt(position)
                                        notifyItemRemoved(position)
                                        notifyItemRangeChanged(position, lista.size)
                                    }
                                }
                                .setNegativeButton("Não", null).show()
                        }
                        2 -> { // MUDAR STATUS (Com a confirmação que você pediu)
                            val novoStatus = if (status == "Pago") "Pendente" else "Pago"

                            AlertDialog.Builder(context)
                                .setTitle("Alterar Status")
                                .setMessage("O serviço está $status.Deseja realmente mudar para $novoStatus?")
                                .setPositiveButton("Sim, Mudar") { _, _ ->
                                    if (db.atualizarStatus(id, novoStatus)) {
                                        (item as MutableMap<String, Any>)["status"] = novoStatus
                                        notifyItemChanged(position)
                                        onStatusChanged(item)// Avisa a Activity para atualizar filtros
                                        Toast.makeText(context, "Atualizado para $novoStatus", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .setNegativeButton("Sair", null).show()
                        }
                    }
                }
                .show()
            true
        }
    }

    override fun getItemCount() = lista.size
}