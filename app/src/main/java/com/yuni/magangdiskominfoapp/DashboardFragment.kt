package com.yuni.magangdiskominfoapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.yuni.magangdiskominfoapp.utils.SessionManager

class DashboardFragment : Fragment() {
    private val viewModel: LamaranViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username = SessionManager.getUsername(requireContext()) ?: "User"
        view.findViewById<TextView>(R.id.tvUsername).text = "$username \uD83C\uDF1F"

        // Initialize statistics
        setupStatistics(view)

        // Setup Pengajuan Lamaran button
        setupPengajuanButton(view)
        setupStatusLamaranButton(view)

        // Observe statistics
        viewModel.statistics.observe(viewLifecycleOwner) { statistics ->
            updateStatistics(statistics.total, statistics.ditolak, statistics.diterima, statistics.revisi)
        }

        // Load initial data
        viewModel.getStatusLamaran()
    }

    private fun setupStatistics(view: View) {
        // Ini nanti bisa diupdate dengan data real dari database/API
        view.findViewById<TextView>(R.id.tvTotalPengajuan).text = "0"
        view.findViewById<TextView>(R.id.tvDitolak).text = "0"
        view.findViewById<TextView>(R.id.tvDiterima).text = "0"
        view.findViewById<TextView>(R.id.tvRevisi).text = "0"
    }

    private fun setupPengajuanButton(view: View) {
        view.findViewById<MaterialButton>(R.id.btnPengajuanLamaran).setOnClickListener {
            // Navigate to LamaranFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LamaranFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupStatusLamaranButton(view: View) {
        view.findViewById<MaterialButton>(R.id.btnStatusLamaran).setOnClickListener {
            // Buka StatusLamaranActivity
            val intent = Intent(requireContext(), StatusLamaranActivity::class.java)
            startActivity(intent)
        }
    }

    // Fungsi untuk mengupdate statistik (bisa dipanggil dari luar fragment)
    private fun updateStatistics(total: Int, ditolak: Int, diterima: Int, revisi: Int) {
        view?.apply {
            findViewById<TextView>(R.id.tvTotalPengajuan).text = total.toString()
            findViewById<TextView>(R.id.tvDitolak).text = ditolak.toString()
            findViewById<TextView>(R.id.tvDiterima).text = diterima.toString()
            findViewById<TextView>(R.id.tvRevisi).text = revisi.toString()
        }
    }
}