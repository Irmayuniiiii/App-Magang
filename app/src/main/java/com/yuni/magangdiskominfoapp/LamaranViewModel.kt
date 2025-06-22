package com.yuni.magangdiskominfoapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yuni.magangdiskominfoapp.api.ApiClient
import com.yuni.magangdiskominfoapp.response.LamaranRequest
import com.yuni.magangdiskominfoapp.response.LamaranResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LamaranViewModel : ViewModel() {
    private val _lamaranStatus = MutableLiveData<LamaranRequest>()
    val lamaranStatus: LiveData<LamaranRequest> = _lamaranStatus

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _statistics = MutableLiveData<Statistics>()
    val statistics: LiveData<Statistics> = _statistics

    // Fungsi untuk mendapatkan status lamaran
    fun getStatusLamaran() {
        _loading.value = true

        ApiClient.apiService.getLamaran().enqueue(object : Callback<LamaranResponse<List<LamaranRequest>>> {
            override fun onResponse(
                call: Call<LamaranResponse<List<LamaranRequest>>>,
                response: Response<LamaranResponse<List<LamaranRequest>>>
            ) {
                _loading.value = false
                when (response.code()) {
                    200 -> {
                        val lamaranList = response.body()?.data
                        if (!lamaranList.isNullOrEmpty()) {
                            _lamaranStatus.value = lamaranList[0]
                            updateStatistics(lamaranList)
                        } else {
                            _error.value = "Tidak ada data lamaran"
                        }
                    }
                    401 -> _error.value = "401"
                    else -> _error.value = "Gagal memuat data lamaran: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<LamaranResponse<List<LamaranRequest>>>, t: Throwable) {
                _loading.value = false
                _error.value = "Terjadi kesalahan: ${t.message}"
            }
        })
    }

    // Fungsi untuk mengupdate status lamaran
    fun updateLamaranStatus(id: Int, status: String) {
        _loading.value = true

        ApiClient.apiService.updateLamaran(id, status).enqueue(object : Callback<LamaranResponse<LamaranRequest>> {
            override fun onResponse(
                call: Call<LamaranResponse<LamaranRequest>>,
                response: Response<LamaranResponse<LamaranRequest>>
            ) {
                _loading.value = false
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        _lamaranStatus.value = it
                        getStatusLamaran() // Refresh the statistics after updating status
                    }
                } else {
                    _error.value = "Gagal mengupdate status lamaran"
                }
            }

            override fun onFailure(call: Call<LamaranResponse<LamaranRequest>>, t: Throwable) {
                _loading.value = false
                _error.value = "Terjadi kesalahan: ${t.message}"
            }
        })
    }

    // Fungsi untuk menghapus lamaran
    fun deleteLamaran(id: Int) {
        _loading.value = true

        ApiClient.apiService.deleteLamaran(id).enqueue(object : Callback<LamaranResponse<Void>> {
            override fun onResponse(
                call: Call<LamaranResponse<Void>>,
                response: Response<LamaranResponse<Void>>
            ) {
                _loading.value = false
                if (response.isSuccessful) {
                    getStatusLamaran() // Refresh the statistics after deleting
                } else {
                    _error.value = "Gagal menghapus lamaran"
                }
            }

            override fun onFailure(call: Call<LamaranResponse<Void>>, t: Throwable) {
                _loading.value = false
                _error.value = "Terjadi kesalahan: ${t.message}"
            }
        })
    }

    // Fungsi untuk mengupdate statistik berdasarkan daftar lamaran
    private fun updateStatistics(lamaranList: List<LamaranRequest>) {
        val total = lamaranList.size
        val ditolak = lamaranList.count { it.status.equals("ditolak", ignoreCase = true) }
        val diterima = lamaranList.count { it.status.equals("diterima", ignoreCase = true) }
        val revisi = lamaranList.count { it.status.equals("revisi", ignoreCase = true) }

        _statistics.value = Statistics(total, ditolak, diterima, revisi)
    }

    data class Statistics(
        val total: Int,
        val ditolak: Int,
        val diterima: Int,
        val revisi: Int
    )
}