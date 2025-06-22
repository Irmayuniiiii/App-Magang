package com.yuni.magangdiskominfoapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.yuni.magangdiskominfoapp.R
import com.yuni.magangdiskominfoapp.response.Lamaran

class LamaranAdapter(
    private var lamaranList: List<Lamaran>,
    private val onItemClick: (Lamaran) -> Unit
) : RecyclerView.Adapter<LamaranAdapter.LamaranViewHolder>() {

    class LamaranViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNama)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val chipStatus: Chip = view.findViewById(R.id.chipStatus)
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LamaranViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lamaran, parent, false)
        return LamaranViewHolder(view)
    }

    override fun onBindViewHolder(holder: LamaranViewHolder, position: Int) {
        val lamaran = lamaranList[position]

        holder.tvNama.text = lamaran.nama
        holder.tvEmail.text = lamaran.email
        holder.chipStatus.text = when(lamaran.status.toLowerCase()) {
            "pending" -> "Menunggu Konfirmasi"
            "diterima" -> "Diterima"
            "ditolak" -> "Ditolak"
            "revisi" -> "Perlu Revisi"
            "magang_berjalan" -> "Magang Berjalan"
            "magang_selesai" -> "Magang Selesai"
            else -> lamaran.status
        }

        holder.chipStatus.setChipBackgroundColorResource(
            when(lamaran.status.toLowerCase()) {
                "pending" -> R.color.status_pending
                "diterima" -> R.color.status_accepted
                "ditolak" -> R.color.status_rejected
                "revisi" -> R.color.status_revision
                "magang_berjalan" -> R.color.status_internship
                "magang_selesai" -> R.color.status_completed
                else -> R.color.status_pending
            }
        )

        holder.tvTanggal.text = "Periode: ${lamaran.tanggal_mulai} - ${lamaran.tanggal_selesai}"

        holder.itemView.setOnClickListener {
            onItemClick(lamaran)
        }
    }

    override fun getItemCount() = lamaranList.size

    fun updateData(newList: List<Lamaran>) {
        lamaranList = newList
        notifyDataSetChanged()
    }
}