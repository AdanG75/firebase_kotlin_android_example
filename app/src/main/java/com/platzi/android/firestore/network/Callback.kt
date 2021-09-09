package com.platzi.android.firestore.network

import java.lang.Exception

//Recibe un dato generico de Java
//El cual nos permite mapear cualquier tipo de resultado
interface Callback<T> {
    fun onSuccess(result: T?)
    fun onFailed(exception: Exception)
}