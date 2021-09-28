package com.platzi.android.firestore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.platzi.android.firestore.R
import com.platzi.android.firestore.model.Crypto
import com.squareup.picasso.Picasso

//El recyclerView.Adapter nos permite reutilizar una vista. Para ello tenemosm que gregar las funciones:
// onCreateViewHolder, onBindViewHolder, getItemCount
class CryptosAdapter (val cryptosAdapterListtener: CryptosAdapterListtener): RecyclerView.Adapter<CryptosAdapter.ViewHolder>(){

    var cryptosList: List<Crypto> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Primero inflamos la vista
        val view = LayoutInflater.from(parent.context).inflate(R.layout.crypto_raw, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val crypto = cryptosList[position]

        Picasso.get().load(crypto.imageUrl).into(holder.image)
        holder.name.text = crypto.name
        holder.available.text = holder.itemView.context.getString(R.string.available_message, crypto.available.toString())
        holder.buyButton.setOnClickListener{
            cryptosAdapterListtener.onBuyCryptoClicked(crypto)
        }
    }

    override fun getItemCount(): Int {
        return cryptosList.size
    }

    class ViewHolder (view: View): RecyclerView.ViewHolder(view) {
        var image = view.findViewById<ImageView>(R.id.image)
        var name = view.findViewById<TextView>(R.id.nameTextView)
        var available = view.findViewById<TextView>(R.id.availableTextView)
        var buyButton = view.findViewById<TextView>(R.id.buyButton)
    }

}