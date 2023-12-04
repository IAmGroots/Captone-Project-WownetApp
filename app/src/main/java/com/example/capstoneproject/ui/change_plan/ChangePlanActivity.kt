package com.example.capstoneproject.ui.change_plan

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.capstoneproject.R
import com.example.capstoneproject.databinding.ActivityChangePlanBinding
import com.example.capstoneproject.preferences.SettingPreferences
import com.example.capstoneproject.preferences.ViewModelFactory
import com.example.capstoneproject.preferences.dataStore
import com.example.capstoneproject.ui.order.OrderActivity

class ChangePlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePlanBinding
    private lateinit var viewModel: ChangePlanViewModel
    private var plan: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preferences = SettingPreferences.getInstance(application.dataStore)
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(preferences)
        )[ChangePlanViewModel::class.java]

        loadUserDataFromSharedPreferences()
        setupToolbar()

        binding.btnChangePlanGold.setOnClickListener {
            plan = "gold"
            val detail = Intent(this, OrderActivity::class.java)
            detail.putExtra(OrderActivity.EXTRA_PACKAGE, plan)
            startActivity(detail)
//            saveChanges()
//            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.btnChangePlanSilver.setOnClickListener {
            plan = "silver"
            val detail = Intent(this, OrderActivity::class.java)
            detail.putExtra(OrderActivity.EXTRA_PACKAGE, plan)
            startActivity(detail)
//            saveChanges()
//            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.btnChangePlanBronze.setOnClickListener {
            plan = "bronze"
            val detail = Intent(this, OrderActivity::class.java)
            detail.putExtra(OrderActivity.EXTRA_PACKAGE, plan)
            startActivity(detail)
//            saveChanges()
//            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun loadUserDataFromSharedPreferences() {
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val plan = sharedPreferences.getString("plan", "")

        when (plan) {
            "gold" -> {
                binding.btnChangePlanGold.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
                binding.btnChangePlanGold.isEnabled = false
                binding.tvPlanGold.text = "Currently"
                binding.tvPlanSilver.text = "Downgrade"
                binding.tvPlanBronze.text = "Downgrade"
            }
            "silver" -> {
                binding.btnChangePlanSilver.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
                binding.btnChangePlanSilver.isEnabled = false
                binding.tvPlanGold.text = "Upgrade"
                binding.tvPlanSilver.text = "Currently"
                binding.tvPlanBronze.text = "Downgrade"
            }
            "bronze" -> {
                binding.btnChangePlanBronze.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
                binding.btnChangePlanBronze.isEnabled = false
                binding.tvPlanGold.text = "Upgrade"
                binding.tvPlanSilver.text = "Upgrade"
                binding.tvPlanBronze.text = "Currently"
            }
            else -> {
                binding.tvPlanGold.text = "Buy"
                binding.tvPlanSilver.text = "Buy"
                binding.tvPlanBronze.text = "Buy"
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbar

        toolbar.setNavigationOnClickListener {
            super.onBackPressed()
        }
    }
}