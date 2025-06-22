package com.yuni.magangdiskominfoapp

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.yuni.magangdiskominfoapp.api.ApiClient
import com.yuni.magangdiskominfoapp.api.ApiService
import com.yuni.magangdiskominfoapp.response.LamaranRequest
import com.yuni.magangdiskominfoapp.response.LamaranResponse
import com.yuni.magangdiskominfoapp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.*

class LamaranFragment : Fragment() {

    private lateinit var emptyStateBiodata: View
    private lateinit var lamaranContent: View
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var etNama: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etUniversitas: AutoCompleteTextView
    private lateinit var etJurusan: AutoCompleteTextView
    private lateinit var etSemester: TextInputEditText
    private lateinit var etTanggalMulai: TextInputEditText
    private lateinit var etTanggalSelesai: TextInputEditText
    private lateinit var btnUploadPengantar: MaterialButton
    private lateinit var btnUploadCV: MaterialButton
    private lateinit var btnSubmit: MaterialButton
    private lateinit var cardFilePengantar: MaterialCardView
    private lateinit var cardFileCV: MaterialCardView
    private lateinit var tvFileNamePengantar: View
    private lateinit var tvFileNameCV: View
    private lateinit var tilEmail: TextInputLayout

    private var suratPengantarUri: Uri? = null
    private var cvUri: Uri? = null

    // File pickers
    private val suratPengantarLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { uri ->
            suratPengantarUri = uri
            showSelectedFile(uri, true)
        }
    }

    private val cvLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { uri ->
            cvUri = uri
            showSelectedFile(uri, false)
        }
    }

    // Launcher untuk memulai EditBiodataActivity
    private lateinit var editBiodataLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lamaran, container, false)
        emptyStateBiodata = view.findViewById(R.id.emptyStateBiodata)
        lamaranContent = view.findViewById(R.id.lamaranContent)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        // Initialize other views
        initializeViews(view)
        setupSwipeRefresh()
        // Inisialisasi launcher untuk EditBiodataActivity
        editBiodataLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Setelah berhasil mengedit biodata, arahkan ke BiodataFragment
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, BiodataFragment())
                    .commit()
            }
        }
        // Check biodata status sebelum menampilkan konten lamaran
        checkBiodataStatus()
        setupDropdowns()
        setupListeners()
        populateUserData()
        return view
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            checkBiodataStatus()
        }

        // Customize the refresh indicator colors (optional)
        swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.primaryDark,
            R.color.accent
        )
    }

    private fun checkBiodataStatus() {
        // Tampilkan loading
        showLoading(true)

        val token = SessionManager.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            showLoading(false)
            return
        }

        // Panggil API untuk mengecek biodata
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getBiodata()
                }

                when {
                    response.isSuccessful && response.body()?.data != null -> {
                        // Biodata sudah ada, tampilkan form lamaran
                        showLamaranForm()
                    }
                    response.code() == 404 -> {
                        // Biodata belum ada, tampilkan empty state dengan tombol untuk mengedit biodata
                        showEmptyState()
                    }
                    response.code() == 401 -> {
                        // Token expired atau tidak valid
                        handleTokenExpired()
                    }
                    else -> {
                        showError("Gagal memuat biodata: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showEmptyState() {
        swipeRefresh.isRefreshing = false

        // Tampilkan empty state dan sembunyikan konten lamaran
        emptyStateBiodata.visibility = View.VISIBLE
        swipeRefresh.visibility = View.GONE

        // Setup tombol untuk melengkapi biodata
        val btnLengkapiBiodata = emptyStateBiodata.findViewById<MaterialButton>(R.id.btnLengkapiBiodata)
        btnLengkapiBiodata.setOnClickListener {
            // Gunakan launcher untuk memulai EditBiodataActivity dan menunggu hasilnya
            val intent = Intent(requireContext(), EditBiodataActivity::class.java).apply {
                putExtra("isCreate", true)
            }
            editBiodataLauncher.launch(intent)
        }
        // Pastikan tombol dapat diklik dan aktif
        btnLengkapiBiodata.apply {
            isClickable = true
            isFocusable = true
            isEnabled = true
        }
    }

    private fun showLamaranForm() {
        emptyStateBiodata.visibility = View.GONE
        lamaranContent.visibility = View.VISIBLE

        // Setup form
        setupDropdowns()
        setupListeners()
        populateUserData()
    }

    private fun showLoading(isLoading: Boolean) {
        swipeRefresh.isRefreshing = isLoading
        emptyStateBiodata.findViewById<MaterialButton>(R.id.btnLengkapiBiodata)?.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun handleTokenExpired() {
        Toast.makeText(requireContext(), "Sesi anda telah berakhir. Silakan login kembali.", Toast.LENGTH_SHORT).show()
        // Implementasi logout dan navigasi ke halaman login
    }

    private fun initializeViews(view: View) {
        etNama = view.findViewById(R.id.etNama)
        etEmail = view.findViewById(R.id.etEmail)
        etUniversitas = view.findViewById(R.id.etUniversitas)
        etJurusan = view.findViewById(R.id.etJurusan)
        etSemester = view.findViewById(R.id.etSemester)
        etTanggalMulai = view.findViewById(R.id.etTanggalMulai)
        etTanggalSelesai = view.findViewById(R.id.etTanggalSelesai)
        btnUploadPengantar = view.findViewById(R.id.btnUploadPengantar)
        btnUploadCV = view.findViewById(R.id.btnUploadCV)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        cardFilePengantar = view.findViewById(R.id.cardFilePengantar)
        cardFileCV = view.findViewById(R.id.cardFileCV)
        tvFileNamePengantar = view.findViewById(R.id.tvFileNamePengantar)
        tvFileNameCV = view.findViewById(R.id.tvFileNameCV)
        tilEmail = view.findViewById(R.id.tilEmail)
    }

    private fun setupDropdowns() {
        val universities = resources.getStringArray(R.array.universities)
        etUniversitas.setAdapter(
            ArrayAdapter(requireContext(), R.layout.dropdown_item, universities)
        )
        val majors = resources.getStringArray(R.array.majors)
        etJurusan.setAdapter(
            ArrayAdapter(requireContext(), R.layout.dropdown_item, majors)
        )
    }

    private fun setupListeners() {
        etTanggalMulai.setOnClickListener { showDatePickerDialog(etTanggalMulai) }
        etTanggalSelesai.setOnClickListener { showDatePickerDialog(etTanggalSelesai) }
        btnUploadPengantar.setOnClickListener { selectFile(true) }
        btnUploadCV.setOnClickListener { selectFile(false) }
        view?.findViewById<View>(R.id.btnRemovePengantar)?.setOnClickListener { clearFileSelection(true) }
        view?.findViewById<View>(R.id.btnRemoveCV)?.setOnClickListener { clearFileSelection(false) }
        btnSubmit.setOnClickListener { submitLamaran() }
    }

    private fun populateUserData() {
        val user = SessionManager.getUser(requireContext())
        user?.let {
            etNama.setText(it.name)
            etEmail.setText(it.email)
        }
    }

    private fun showDatePickerDialog(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                editText.setText(String.format("%d-%02d-%02d", year, month + 1, dayOfMonth))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun selectFile(isSuratPengantar: Boolean) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        if (isSuratPengantar) {
            suratPengantarLauncher.launch(intent)
        } else {
            cvLauncher.launch(intent)
        }
    }

    private fun showSelectedFile(uri: Uri, isSuratPengantar: Boolean) {
        val fileName = getFileName(uri)
        if (isSuratPengantar) {
            cardFilePengantar.visibility = View.VISIBLE
            (tvFileNamePengantar as? android.widget.TextView)?.text = fileName
        } else {
            cardFileCV.visibility = View.VISIBLE
            (tvFileNameCV as? android.widget.TextView)?.text = fileName
        }
    }

    private fun getFileName(uri: Uri): String {
        return requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else ""
        } ?: ""
    }

    private fun clearFileSelection(isSuratPengantar: Boolean) {
        if (isSuratPengantar) {
            suratPengantarUri = null
            cardFilePengantar.visibility = View.GONE
        } else {
            cvUri = null
            cardFileCV.visibility = View.GONE
        }
    }

    private fun createMultipartBodyPart(uri: Uri, partName: String): MultipartBody.Part? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload", ".pdf", requireContext().cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            val requestFile = tempFile.asRequestBody("application/pdf".toMediaType())
            MultipartBody.Part.createFormData(partName, getFileName(uri), requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun submitLamaran() {
        val token = SessionManager.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Silakan login kembali.", Toast.LENGTH_SHORT).show()
            return
        }

        val user = SessionManager.getUser(requireContext())
        if (user == null) {
            Toast.makeText(requireContext(), "Data user tidak ditemukan.", Toast.LENGTH_SHORT).show()
            return
        }

        val safeNama = etNama.text?.toString() ?: ""
        val safeEmail = etEmail.text?.toString() ?: ""
        val safeUniversitas = etUniversitas.text?.toString() ?: ""
        val safeJurusan = etJurusan.text?.toString() ?: ""
        val safeSemester = etSemester.text?.toString() ?: ""
        val safeTanggalMulai = etTanggalMulai.text?.toString() ?: ""
        val safeTanggalSelesai = etTanggalSelesai.text?.toString() ?: ""
        val divisi = "IT"

        val mediaType = "text/plain".toMediaType()
        val namaPart = safeNama.toRequestBody(mediaType)
        val emailPart = safeEmail.toRequestBody(mediaType)
        val sekolahPart = safeUniversitas.toRequestBody(mediaType)
        val jurusanPart = safeJurusan.toRequestBody(mediaType)
        val semesterPart = safeSemester.toRequestBody(mediaType)
        val tanggalMulaiPart = safeTanggalMulai.toRequestBody(mediaType)
        val tanggalSelesaiPart = safeTanggalSelesai.toRequestBody(mediaType)
        val divisiPart = divisi.toRequestBody(mediaType)

        if (suratPengantarUri == null) {
            Toast.makeText(requireContext(), "Silakan pilih surat pengantar.", Toast.LENGTH_SHORT).show()
            return
        }
        val suratPengantarPart = createMultipartBodyPart(suratPengantarUri!!, "surat_pengantar")
        val cvPart = cvUri?.let { createMultipartBodyPart(it, "cv") }

        if (suratPengantarPart == null) {
            Toast.makeText(requireContext(), "Gagal memproses file surat pengantar.", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService: ApiService = ApiClient.apiService

        val call = apiService.createLamaran(
            token = "Bearer $token",
            nama = namaPart,
            email = emailPart,
            asalSekolah = sekolahPart,
            jurusan = jurusanPart,
            semester = semesterPart,
            tanggalMulai = tanggalMulaiPart,
            tanggalSelesai = tanggalSelesaiPart,
            bagianDivisi = divisiPart,
            surat_pengantar = suratPengantarPart,
            cv = cvPart
        )

        call.enqueue(object : Callback<LamaranResponse<LamaranRequest>> {
            override fun onResponse(
                call: Call<LamaranResponse<LamaranRequest>>,
                response: Response<LamaranResponse<LamaranRequest>>
            ) {
                if (isAdded) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Lamaran berhasil dikirim",
                            Toast.LENGTH_SHORT
                        ).show()

                        Handler(Looper.getMainLooper()).postDelayed({
                            try {
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, DashboardFragment())
                                    .commit()
                            } catch (e: Exception) {
                                Log.e("LamaranFragment", "Navigation Error: ${e.localizedMessage}")
                                Toast.makeText(
                                    requireContext(),
                                    "Gagal berpindah ke halaman dashboard",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }, 1000)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Gagal mengirim lamaran: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<LamaranResponse<LamaranRequest>>, t: Throwable) {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("LamaranFragment", "onFailure: ${t.localizedMessage}")
                }
            }
        })
    }
}