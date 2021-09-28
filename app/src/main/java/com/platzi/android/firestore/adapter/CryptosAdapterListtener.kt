package com.platzi.android.firestore.adapter

import com.platzi.android.firestore.model.Crypto

interface CryptosAdapterListtener {

    fun onBuyCryptoClicked(crypto: Crypto)
}