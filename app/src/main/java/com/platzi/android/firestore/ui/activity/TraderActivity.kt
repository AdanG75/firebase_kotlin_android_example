package com.platzi.android.firestore.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.platzi.android.firestore.R
import com.platzi.android.firestore.adapter.CryptosAdapter
import com.platzi.android.firestore.adapter.CryptosAdapterListtener
import com.platzi.android.firestore.model.Crypto
import com.platzi.android.firestore.model.User
import com.platzi.android.firestore.network.Callback
import com.platzi.android.firestore.network.FirestoreService
import com.platzi.android.firestore.network.RealtimeDataListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_trader.*
import java.lang.Exception


/**
 * @author Santiago Carrillo
 * 2/14/19.
 */
class TraderActivity : AppCompatActivity(), CryptosAdapterListtener {

    lateinit var firestoreService: FirestoreService

    private val cryptosAdapter: CryptosAdapter = CryptosAdapter(this)

    private var username: String? = null

    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trader)
        firestoreService = FirestoreService(FirebaseFirestore.getInstance())
        username = intent.extras?.get(USERNAME_KEY)?.toString()
        usernameTextView.text = username

        configureRecyclerView()
        loadCryptos()

        fab.setOnClickListener { view ->
            Snackbar.make(view, R.string.generating_new_cryptos, Snackbar.LENGTH_SHORT)
                .setAction("Info", null).show()
                generateCryptoCurrenciesRandom()
        }
    }

    private fun generateCryptoCurrenciesRandom() {
        for (crypto in cryptosAdapter.cryptosList) {
            val amount = (1..10).random()
            crypto.available += amount

            firestoreService.updateCrypto(crypto)
        }
    }

    private fun loadCryptos() {
        firestoreService.getCryptos(object : Callback<List<Crypto>> {
            override fun onSuccess(cryptoList: List<Crypto>?) {

                username?.let {
                    firestoreService.findUserById(it, object : Callback<User> {
                        override fun onSuccess(result: User?) {
                            user = result
                            if (user?.cryptosList == null) {
                                val userCryptoList = mutableListOf<Crypto>()

                                if (cryptoList != null) {
                                    for (crypto in cryptoList) {
                                        val cryptoUser = Crypto()
                                        cryptoUser.name = crypto.name
                                        cryptoUser.available = 0
                                        cryptoUser.imageUrl = crypto.imageUrl
                                        userCryptoList.add(cryptoUser)
                                    }

                                    if (user != null) {
                                        user!!.cryptosList = userCryptoList
                                        firestoreService.updateUser(user!!, null)
                                    }
                                }
                            }
                            loadUserCryptos()
                            addRealtimeDatabaseListeners(user, cryptoList)
                        }

                        override fun onFailed(exception: Exception) {
                            Log.e("Activity Trader", "Error to get user on loadCryptos", exception)
                            showGeneralServerErrorMessage()
                        }
                    })
                }

                this@TraderActivity.runOnUiThread {
                    if (cryptoList != null) {
                        cryptosAdapter.cryptosList = cryptoList
                        //Dibuja los paneles de las criptomonedas
                        cryptosAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailed(exception: Exception) {
                Log.e("Trading Activity", "error loading cryptos", exception)
                showGeneralServerErrorMessage()
            }

        })
    }

    private fun addRealtimeDatabaseListeners(user: User?, cryptosList: List<Crypto>?) {
        if (user != null) {
            firestoreService.listenForUpdates(user, object: RealtimeDataListener<User> {
                override fun onDataChange(updatedData: User) {
                    this@TraderActivity.user = updatedData
                    loadUserCryptos()
                }

                override fun onError(exception: Exception) {
                    showGeneralServerErrorMessage()
                }
            })
        }

        if (cryptosList != null) {
            firestoreService.listenForUpdates(cryptosList, object: RealtimeDataListener<Crypto> {
                override fun onDataChange(updatedData: Crypto) {
                    var pos = 0
                    for (crypto in cryptosAdapter.cryptosList) {
                        //updateData contiene los valores que han sido modificados en firebase
                        if (crypto.name == updatedData.name) {
                            crypto.available = updatedData.available
                            cryptosAdapter.notifyItemChanged(pos)
                        }

                        pos++
                    }
                }

                override fun onError(exception: Exception) {
                    showGeneralServerErrorMessage()
                }
            })
        }
    }

    private fun loadUserCryptos() {
        runOnUiThread {
            if (user != null && user!!.cryptosList != null) {
                //borra todas las vistas del contenedor
                infoPanel.removeAllViews()
                for (crypto in user!!.cryptosList!!) {
                    addUserInfoRow(crypto)
                }
            }
        }
    }

    private fun addUserInfoRow(crypto: Crypto) {
        //Infamos el layout de coin_info dentro del trader_activity
        val view = LayoutInflater.from(this).inflate(R.layout.coin_info, infoPanel, false)
        view.findViewById<TextView>(R.id.coinLabel).text =
            getString(R.string.coin_info, crypto.name, crypto.available.toString())
        Picasso.get().load(crypto.imageUrl).into(view.findViewById<ImageView>(R.id.coinIcon))
        //Agregamos la vista que creamos anteriormente
        infoPanel.addView(view)
    }

    private fun configureRecyclerView() {
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = cryptosAdapter
    }

    fun showGeneralServerErrorMessage() {
        Snackbar.make(fab, R.string.error_while_connecting_to_the_server, Snackbar.LENGTH_LONG)
            .setAction("Info", null).show()
    }

    override fun onBuyCryptoClicked(crypto: Crypto) {
        var flag = false

        if(crypto.available > 0) {
            for(userCrypto in user?.cryptosList!!) {
                if(userCrypto.name == crypto.name) {
                    userCrypto.available += 1
                    flag = true
                    break
                }
            }

            if (!flag) {
                val cryptoUser = Crypto()
                cryptoUser.name = crypto.name
                cryptoUser.available = 1
                cryptoUser.imageUrl = crypto.imageUrl

                user!!.cryptosList = user!!.cryptosList?.plusElement(cryptoUser)
            }

            crypto.available--

            firestoreService.updateUser(user!!, null)
            firestoreService.updateCrypto(crypto)
        }
    }
}