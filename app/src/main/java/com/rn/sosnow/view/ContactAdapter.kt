package com.rn.sosnow.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rn.sosnow.R
import com.rn.sosnow.databinding.ItemContactBinding
import com.rn.sosnow.model.Contact

class ContactAdapter(val contacts: List<Contact>, private val onClick: (Contact) -> Unit) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>(){


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = DataBindingUtil.bind<ItemContactBinding>(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentContact = contacts[position]
        holder.binding?.contact = currentContact // Define o objeto Contact para a variável contact no layout
        holder.binding?.root?.setOnClickListener {
            onClick(currentContact)
        }
        holder.binding?.executePendingBindings()
    }

}