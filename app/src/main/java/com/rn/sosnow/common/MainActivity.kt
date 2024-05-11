package com.rn.sosnow.common

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.rn.sosnow.view.ContactAdapter
import com.rn.sosnow.R
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
        binding.rvContacts.layoutManager = GridLayoutManager(this, numerberOfColumns)
        binding.rvContacts.adapter = ContactAdapter(contacts){ contact ->
           val intent = Intent(this, InformationActivity::class.java).apply {
               putExtra("CONTACT", contact)
           }

            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.principal, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.action_map ->
               startActivity(Intent(this, InformationActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }


}