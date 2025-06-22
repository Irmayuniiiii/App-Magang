package com.yuni.magangdiskominfoapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.yuni.magangdiskominfoapp.api.ApiClient
import com.yuni.magangdiskominfoapp.base.BaseEventBusFragment
import com.yuni.magangdiskominfoapp.databinding.FragmentBiodataBinding
import com.yuni.magangdiskominfoapp.response.Biodata
import com.yuni.magangdiskominfoapp.utils.PhotoProfileManager
import com.yuni.magangdiskominfoapp.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BiodataFragment : BaseEventBusFragment() {
    private var _binding: FragmentBiodataBinding? = null
    private val binding get() = _binding!!
    private val EDIT_BIODATA_REQUEST = 1

    private enum class BiodataState {
        LOADING, EMPTY, ERROR, SUCCESS
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBiodataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup button listeners
        setupButtons()

        // Load biodata
        loadBiodata()
    }

    private fun setupButtons() {
        binding.btnEdit.setOnClickListener {
            startEditBiodata()
        }
        binding.btnEditMain.setOnClickListener {
            startEditBiodata()
        }
    }

    private fun startEditBiodata() {
        if (!SessionManager.hasToken(requireContext())) {
            handleTokenExpired()
            return
        }

        val intent = Intent(requireContext(), EditBiodataActivity::class.java).apply {
            // Tambahkan flag untuk menandakan apakah ini create baru atau edit
            putExtra("isCreate", binding.btnEdit.visibility == View.VISIBLE)
        }
        startActivityForResult(intent, EDIT_BIODATA_REQUEST)
    }

    private fun setupEmptyState() {
        binding.apply {
            btnEdit.setOnClickListener {
                startEditBiodata()
            }
             //Tambahkan tombol retry jika perlu
             btnRetry.setOnClickListener {
                 loadBiodata()
             }
        }
    }

    private fun updateUIState(state: BiodataState, message: String? = null) {
        binding.apply {
            when (state) {
                BiodataState.LOADING -> {
                    progressBar.visibility = View.VISIBLE
                    emptyStateLayout.visibility = View.GONE
                    mainContent.visibility = View.GONE
                }
                BiodataState.EMPTY -> {
                    progressBar.visibility = View.GONE
                    emptyStateLayout.visibility = View.VISIBLE
                    mainContent.visibility = View.GONE
                    // Update tombol untuk create
                    btnEdit.apply {
                        visibility = View.VISIBLE
                        text = "Buat Biodata"
                    }
                    btnEditMain.visibility = View.GONE
                }
                BiodataState.ERROR -> {
                    progressBar.visibility = View.GONE
                    emptyStateLayout.visibility = View.VISIBLE
                    mainContent.visibility = View.GONE
                    message?.let { showError(it) }
                }
                BiodataState.SUCCESS -> {
                    progressBar.visibility = View.GONE
                    emptyStateLayout.visibility = View.GONE
                    mainContent.visibility = View.VISIBLE
                    btnEdit.visibility = View.GONE
                    btnEditMain.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.apply {
            // Tampilkan/sembunyikan layouts
            emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
            mainContent.visibility = if (isEmpty) View.GONE else View.VISIBLE

            // Update button visibility dan text
            if (isEmpty) {
                btnEdit.apply {
                    visibility = View.VISIBLE
                    text = "Buat Biodata"
                }
                btnEditMain.visibility = View.GONE
            } else {
                btnEdit.visibility = View.GONE
                btnEditMain.apply {
                    visibility = View.VISIBLE
                    text = "Edit Biodata"
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_BIODATA_REQUEST && resultCode == Activity.RESULT_OK) {
            loadBiodata()
        }
    }

    private fun loadBiodata() {
        updateUIState(BiodataState.LOADING)

        if (!SessionManager.hasToken(requireContext())) {
            handleTokenExpired()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getBiodata()
                }

                when (response.code()) {
                    200 -> {
                        val biodataResponse = response.body()
                        if (biodataResponse?.success == true && biodataResponse.data != null) {
                            updateUIState(BiodataState.SUCCESS)
                            updateUI(biodataResponse.data)
                        } else {
                            updateUIState(BiodataState.EMPTY)
                        }
                    }
                    404 -> {
                        updateUIState(BiodataState.EMPTY)
                    }
                    401 -> {
                        handleTokenExpired()
                    }
                    else -> {
                        updateUIState(
                            BiodataState.ERROR,
                            "Error: ${response.message()}"
                        )
                    }
                }
            } catch (e: Exception) {
                updateUIState(
                    BiodataState.ERROR,
                    "Error: ${e.message}"
                )
            }
        }
    }

    private fun handleEmptyBiodata() {
        showEmptyState(true)
        binding.apply {
            // Pastikan tombol Buat Biodata terlihat
            btnEdit.apply {
                visibility = View.VISIBLE
                text = "Buat Biodata"
            }
            btnEditMain.visibility = View.GONE
        }
    }

    private fun handleError(message: String) {
        showEmptyState(true)
        showError(message)
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateUI(biodata: Biodata) {
        binding.apply {
            tvNamaLengkap.text = biodata.nama_lengkap
            tvTempatLahir.text = biodata.tempat_lahir ?: "-"
            tvTanggalLahir.text = biodata.tanggal_lahir ?: "-"
            tvJenisKelamin.text = biodata.jenis_kelamin ?: "-"
            tvAgama.text = biodata.agama ?: "-"
            tvAlamat.text = biodata.alamat ?: "-"
            tvAsalSekolah.text = biodata.asal_sekolah ?: "-"
            tvJurusan.text = biodata.jurusan ?: "-"
            tvSemester.text = biodata.semester?.toString() ?: "-"
            tvIpk.text = biodata.ipk?.toString() ?: "-"

            PhotoProfileManager.saveProfilePhotoPath(requireContext(), biodata.profile_photo)

            // Load profile photo jika ada
            if (!biodata.profile_photo.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load("${ApiClient.BASE_URL}storage/${biodata.profile_photo}")
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivProfilePhoto)

                // Update foto di navigation header
                updateNavigationHeaderPhoto()
            } else {
                // Set default image jika tidak ada foto
                Glide.with(requireContext())
                    .load(R.drawable.ic_person)
                    .into(ivProfilePhoto)
            }
        }
    }

    private fun updateNavigationHeaderPhoto() {
        val mainActivity = activity as? MainActivity
        mainActivity?.updateNavigationHeader()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            mainContent.visibility = if (isLoading) View.GONE else View.VISIBLE
            emptyStateLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun getStoredToken(): String {
        val sharedPreferences = requireContext().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("token", "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}