package com.yuni.magangdiskominfoapp

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.yuni.magangdiskominfoapp.api.ApiClient
import com.yuni.magangdiskominfoapp.base.BaseEventBusActivity
import com.yuni.magangdiskominfoapp.databinding.ActivityEditBiodataBinding
import com.yuni.magangdiskominfoapp.response.Biodata
import com.yuni.magangdiskominfoapp.utils.PhotoProfileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EditBiodataActivity : BaseEventBusActivity() {
    private lateinit var binding: ActivityEditBiodataBinding
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private val MAX_FILE_SIZE = 2 * 1024 * 1024

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditBiodataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Biodata"

        setupJenisKelaminSpinner()
        setupDatePicker()
        setupImagePicker()
        loadExistingBiodata()
        setupSaveButton()
    }

    private fun validateImage(uri: Uri?): Boolean {
        if (uri == null) return true // Skip validation if no image selected

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileSize = inputStream?.available() ?: 0
            inputStream?.close()

            if (fileSize > MAX_FILE_SIZE) {
                Toast.makeText(
                    this,
                    "Ukuran foto maksimal 2MB, foto Anda: ${String.format("%.2f", fileSize / 1024f / 1024f)}MB",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
            return true
        } catch (e: Exception) {
            Toast.makeText(this, "Error saat memeriksa ukuran foto: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    private fun setupJenisKelaminSpinner() {
        val jenisKelaminOptions = arrayOf("Laki-laki", "Perempuan")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            jenisKelaminOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJenisKelamin.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.etTanggalLahir.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, day)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    binding.etTanggalLahir.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupImagePicker() {
        binding.btnSelectPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    private fun loadExistingBiodata() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.getEditBiodata()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.data?.let { populateFields(it) }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditBiodataActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populateFields(biodata: Biodata) {
        binding.apply {
            etNamaLengkap.setText(biodata.nama_lengkap)
            etTempatLahir.setText(biodata.tempat_lahir)
            etTanggalLahir.setText(biodata.tanggal_lahir)
            etAgama.setText(biodata.agama)
            etAlamat.setText(biodata.alamat)
            etAsalSekolah.setText(biodata.asal_sekolah)
            etJurusan.setText(biodata.jurusan)
            etSemester.setText(biodata.semester?.toString())
            etIpk.setText(biodata.ipk?.toString())

            // Set jenis kelamin spinner
            val jenisKelaminPosition = when (biodata.jenis_kelamin) {
                "Laki-laki" -> 0
                "Perempuan" -> 1
                else -> 0
            }
            spinnerJenisKelamin.setSelection(jenisKelaminPosition)

            // Load existing photo
            if (!biodata.profile_photo.isNullOrEmpty()) {
                Glide.with(this@EditBiodataActivity)
                    .load("${ApiClient.BASE_URL}storage/${biodata.profile_photo}")
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivProfilePhoto)
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSimpan.setOnClickListener {
            saveBiodata()
        }
    }

    private fun saveBiodata() {
        val namaLengkap = binding.etNamaLengkap.text.toString()
        if (namaLengkap.isEmpty()) {
            Toast.makeText(this, "Nama lengkap harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Create RequestBody objects
                val requestBodyMap = mutableMapOf<String, RequestBody>()

                requestBodyMap["nama_lengkap"] = namaLengkap.toRequestBody("text/plain".toMediaTypeOrNull())
                binding.etTempatLahir.text?.toString()?.let {
                    requestBodyMap["tempat_lahir"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                binding.etTanggalLahir.text?.toString()?.let {
                    requestBodyMap["tanggal_lahir"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                binding.spinnerJenisKelamin.selectedItem?.toString()?.let {
                    requestBodyMap["jenis_kelamin"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                binding.etAgama.text?.toString()?.let {
                    requestBodyMap["agama"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                binding.etAlamat.text?.toString()?.let {
                    requestBodyMap["alamat"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                binding.etAsalSekolah.text?.toString()?.let {
                    requestBodyMap["asal_sekolah"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                binding.etJurusan.text?.toString()?.let {
                    requestBodyMap["jurusan"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                binding.etSemester.text?.toString()?.let {
                    requestBodyMap["semester"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }
                binding.etIpk.text?.toString()?.let {
                    requestBodyMap["ipk"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
                }

                // Handle photo
                var photoPart: MultipartBody.Part? = null
                selectedImageUri?.let { uri ->
                    val file = File(getRealPathFromURI(uri))
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    photoPart = MultipartBody.Part.createFormData("photo", file.name, requestFile)
                }

                val response = if (isBiodataExists()) {
                    ApiClient.apiService.updateBiodata(
                        requestBodyMap["nama_lengkap"]!!,
                        requestBodyMap["tempat_lahir"],
                        requestBodyMap["tanggal_lahir"],
                        requestBodyMap["jenis_kelamin"],
                        requestBodyMap["agama"],
                        requestBodyMap["alamat"],
                        requestBodyMap["asal_sekolah"],
                        requestBodyMap["jurusan"],
                        requestBodyMap["semester"],
                        requestBodyMap["ipk"],
                        photoPart
                    )
                } else {
                    ApiClient.apiService.createBiodata(
                        requestBodyMap["nama_lengkap"]!!,
                        requestBodyMap["tempat_lahir"],
                        requestBodyMap["tanggal_lahir"],
                        requestBodyMap["jenis_kelamin"],
                        requestBodyMap["agama"],
                        requestBodyMap["alamat"],
                        requestBodyMap["asal_sekolah"],
                        requestBodyMap["jurusan"],
                        requestBodyMap["semester"],
                        requestBodyMap["ipk"],
                        photoPart
                    )
                }

                withContext(Dispatchers.Main) {
                    when {
                        response.isSuccessful -> {
                            response.body()?.data?.profile_photo?.let { photoPath ->
                                PhotoProfileManager.saveProfilePhotoPath(this@EditBiodataActivity, photoPath)
                            }
                            Toast.makeText(this@EditBiodataActivity, "Biodata berhasil disimpan", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                        response.code() == 401 -> {
                            handleTokenExpired()
                        }
                        else -> {
                            Toast.makeText(this@EditBiodataActivity, "Gagal menyimpan biodata", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditBiodataActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun isBiodataExists(): Boolean {
        return try {
            val response = ApiClient.apiService.getBiodata()
            response.isSuccessful && response.body()?.data != null
        } catch (e: Exception) {
            false
        }
    }

    private fun getStoredToken(): String {
        val sharedPreferences = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("token", "") ?: ""
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val path = cursor?.getString(columnIndex ?: 0) ?: ""
        cursor?.close()
        return path
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            if (selectedImageUri != null) {
                binding.ivProfilePhoto.setImageURI(selectedImageUri)
            } else {
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}