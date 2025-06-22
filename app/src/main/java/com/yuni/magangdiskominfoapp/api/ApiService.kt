package com.yuni.magangdiskominfoapp.api

import com.yuni.magangdiskominfoapp.response.BiodataResponse
import com.yuni.magangdiskominfoapp.response.LamaranResponse
import com.yuni.magangdiskominfoapp.response.LamaranRequest
import com.yuni.magangdiskominfoapp.response.LoginRequest
import com.yuni.magangdiskominfoapp.response.LoginResponse
import com.yuni.magangdiskominfoapp.response.LogoutResponse
import com.yuni.magangdiskominfoapp.response.RegisterRequest
import com.yuni.magangdiskominfoapp.response.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("api/register")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("api/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @Multipart
    @POST("api/lamaran")
    fun createLamaran(
        @Header("Authorization") token: String,
        @Part("nama") nama: RequestBody,
        @Part("email") email: RequestBody,
        @Part("asal_sekolah") asalSekolah: RequestBody,
        @Part("jurusan") jurusan: RequestBody,
        @Part("semester") semester: RequestBody,
        @Part("tanggal_mulai") tanggalMulai: RequestBody,
        @Part("tanggal_selesai") tanggalSelesai: RequestBody,
        @Part("bagian_divisi") bagianDivisi: RequestBody,
        @Part surat_pengantar: MultipartBody.Part,
        @Part cv: MultipartBody.Part?
    ): Call<LamaranResponse<LamaranRequest>>

    @GET("api/lamaran")
    fun getLamaran(): Call<LamaranResponse<List<LamaranRequest>>>

    @FormUrlEncoded
    @PUT("api/lamaran/{id}")
    fun updateLamaran(
        @Path("id") id: Int,
        @Field("status") status: String
    ): Call<LamaranResponse<LamaranRequest>>

    @DELETE("api/lamaran/{id}")
    fun deleteLamaran(
        @Path("id") id: Int
    ): Call<LamaranResponse<Void>>

    @GET("api/biodata")
    suspend fun getBiodata(): Response<BiodataResponse>

    @GET("api/biodata/edit")
    suspend fun getEditBiodata(): Response<BiodataResponse>

    @Multipart
    @POST("api/biodata")
    suspend fun createBiodata(
        @Part("nama_lengkap") namaLengkap: RequestBody,
        @Part("tempat_lahir") tempatLahir: RequestBody?,
        @Part("tanggal_lahir") tanggalLahir: RequestBody?,
        @Part("jenis_kelamin") jenisKelamin: RequestBody?,
        @Part("agama") agama: RequestBody?,
        @Part("alamat") alamat: RequestBody?,
        @Part("asal_sekolah") asalSekolah: RequestBody?,
        @Part("jurusan") jurusan: RequestBody?,
        @Part("semester") semester: RequestBody?,
        @Part("ipk") ipk: RequestBody?,
        @Part photo: MultipartBody.Part?
    ): Response<BiodataResponse>

    @Multipart
    @PUT("api/biodata/update")
    suspend fun updateBiodata(
        @Part("nama_lengkap") namaLengkap: RequestBody,
        @Part("tempat_lahir") tempatLahir: RequestBody?,
        @Part("tanggal_lahir") tanggalLahir: RequestBody?,
        @Part("jenis_kelamin") jenisKelamin: RequestBody?,
        @Part("agama") agama: RequestBody?,
        @Part("alamat") alamat: RequestBody?,
        @Part("asal_sekolah") asalSekolah: RequestBody?,
        @Part("jurusan") jurusan: RequestBody?,
        @Part("semester") semester: RequestBody?,
        @Part("ipk") ipk: RequestBody?,
        @Part photo: MultipartBody.Part?
    ): Response<BiodataResponse>

    @POST("api/logout")
    fun logoutUser(@Header("Authorization") token: String): Call<LogoutResponse>
}
