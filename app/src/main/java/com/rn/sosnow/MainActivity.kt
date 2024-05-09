package com.rn.sosnow

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.rn.sosnow.databinding.ActivityMainBinding
import com.rn.sosnow.model.Contact
import com.rn.sosnow.viewmodels.ContactListViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: ContactListViewModel by lazy{
        ViewModelProvider(this).get(ContactListViewModel::class.java)
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadContacts()
    }


    private fun loadContacts(){
        try{
            viewModel.getContacts().observe(this, Observer { contacts ->
                updateList(contacts)
            })
        }catch (e: Exception){
            Toast.makeText(this, R.string.message_error_load_contacts, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateList(contacts: List<Contact>){
        val numerberOfColumns = 2
        binding.rvBooks.layoutManager = GridLayoutManager(this, numerberOfColumns)
        binding.rvBooks.adapter = ContactAdapter(contacts){ contact ->
            val phoneNumber = contact.number
            val name = contact.name

            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }

            startActivity(intent)

            Toast.makeText(this, "Ligando para -> $name", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.principal, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.action_map ->
               startActivity(Intent(this, MapaActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }


}