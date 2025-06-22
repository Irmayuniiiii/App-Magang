package com.yuni.magangdiskominfoapp

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yuni.magangdiskominfoapp.api.ApiClient
import com.yuni.magangdiskominfoapp.api.ApiService
import com.yuni.magangdiskominfoapp.databinding.ActivityStatusLamaranBinding
import com.yuni.magangdiskominfoapp.response.LamaranRequest
import com.yuni.magangdiskominfoapp.response.LamaranResponse
import com.yuni.magangdiskominfoapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StatusLamaranActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatusLamaranBinding
    private val viewModel: LamaranViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusLamaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ApiClient
        ApiClient.init(this)

        setupToolbar()
        setupSwipeRefresh()
        setupObservers()
        loadData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }
    }



    private fun setupObservers() {
        viewModel.lamaranStatus.observe(this) { lamaran ->
            if (lamaran != null) {
                // Ada data lamaran
                binding.apply {
                    layoutNoLamaran.visibility = View.GONE
                    swipeRefreshLayout.visibility = View.VISIBLE
                }
                updateUI(lamaran)
            } else {
                // Tidak ada data lamaran
                binding.apply {
                    layoutNoLamaran.visibility = View.VISIBLE
                    swipeRefreshLayout.visibility = View.GONE
                }
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            // Jika error, tampilkan layout no lamaran
            binding.apply {
                layoutNoLamaran.visibility = View.VISIBLE
                swipeRefreshLayout.visibility = View.GONE
            }
        }
    }


    private fun loadData() {
        viewModel.getStatusLamaran()
    }

    private fun downloadFile(url: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Download File")
                .setDescription("Mengunduh file lamaran")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(this, "Mengunduh file...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal mengunduh file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDownloadButton(fileUrl: String?) {
        if (!fileUrl.isNullOrEmpty()) {
            binding.btnDownloadLetter.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    downloadFile(fileUrl)
                }
            }
        } else {
            binding.btnDownloadLetter.visibility = View.GONE
        }
    }

    private fun updateUI(lamaran: LamaranRequest) {
        binding.apply {
            // Pastikan layout utama terlihat
            swipeRefreshLayout.visibility = View.VISIBLE
            layoutNoLamaran.visibility = View.GONE

            // Update UI seperti sebelumnya
            tvApplicantName.text = lamaran.nama
            tvApplicationId.text = "ID: ${lamaran.id}"
            tvDuration.text = "Durasi: ${lamaran.tanggal_mulai} - ${lamaran.tanggal_selesai}"
            // Update status chip
            val statusText = when(lamaran.status.toLowerCase()) {
                "pending" -> "Menunggu Konfirmasi"
                "diterima" -> "Diterima"
                "ditolak" -> "Ditolak"
                "revisi" -> "Perlu Revisi"
                "magang_berjalan" -> "Magang Berjalan"
                "magang_selesai" -> "Magang Selesai"
                else -> lamaran.status
            }
            statusChip.text = statusText

            // Update status color and download card
            when(lamaran.status.toLowerCase()) {
                "pending" -> {
                    statusChip.setChipBackgroundColorResource(R.color.status_pending)
                    downloadCard.visibility = View.GONE
                }
                "diterima" -> {
                    statusChip.setChipBackgroundColorResource(R.color.status_accepted)
                    downloadCard.visibility = View.VISIBLE
                    tvLetterDescription.text = "Selamat! Lamaran Anda telah diterima."
                    setupDownloadButton(lamaran.files.surat_diterima)
                }
                "ditolak" -> {
                    statusChip.setChipBackgroundColorResource(R.color.status_rejected)
                    downloadCard.visibility = View.VISIBLE
                    tvLetterDescription.text = "Mohon maaf, lamaran Anda ditolak."
                    setupDownloadButton(lamaran.files.surat_ditolak)
                }
                "revisi" -> {
                    statusChip.setChipBackgroundColorResource(R.color.status_revision)
                    downloadCard.visibility = View.VISIBLE
                    tvLetterDescription.text = lamaran.catatan_revisi ?: "Silakan periksa kembali berkas Anda"
                    btnDownloadLetter.visibility = View.GONE
                }
                "magang_berjalan" -> {
                    statusChip.setChipBackgroundColorResource(R.color.status_internship)
                    downloadCard.visibility = View.GONE
                }
                "magang_selesai" -> {
                    statusChip.setChipBackgroundColorResource(R.color.status_completed)
                    downloadCard.visibility = View.VISIBLE
                    tvLetterDescription.text = "Selamat! Anda telah menyelesaikan magang."
                    setupDownloadButton(lamaran.files.sertifikat)
                }
            }
        }
    }
}