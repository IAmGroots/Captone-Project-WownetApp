package com.example.capstoneproject.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.capstoneproject.MainActivity
import com.example.capstoneproject.R
import com.example.capstoneproject.adapter.BannerAdapter
import com.example.capstoneproject.adapter.HomeArticlesAdapter
import com.example.capstoneproject.databinding.FragmentHomeBinding
import com.example.capstoneproject.model.Banner
import com.example.capstoneproject.model.DataSourceUser
import com.example.capstoneproject.ui.change_plan.ChangePlanActivity
import com.example.capstoneproject.ui.usage.UsageActivity
import com.example.capstoneproject.ui.wifi.WifiActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.util.Timer
import java.util.TimerTask

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel : HomeViewModel by viewModels()
    private lateinit var viewPager: ViewPager2
    private lateinit var timer: Timer
    private val DURATION: Long = 1000
    private var allowRefresh = true
    private val db = Firebase.firestore
    private val id_user = "1701"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        checkLastTransaction(id_user)
        loadUserDataFromSharedPreferences()
        setupToolbar()
        setupBanner()
        setupAction()
        setupArticles()

        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            binding.swipeRefresh.isEnabled = binding.scrollView.scrollY == 0
        }

        binding.swipeRefresh.setOnRefreshListener {
            checkLastTransaction(id_user)
            loadUserDataFromSharedPreferences()
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipeRefresh.isRefreshing = false
            }, DURATION)
        }

        return root
    }

    // for setup toolbar
    private fun setupToolbar() {
        val toolbar = binding.toolbar

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_notifications -> {
                    Toast.makeText(requireContext(), "Icon Notification Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> true
            }
        }
    }

    // Banner Start
    private val bannerList = listOf(
        Banner("Satu", "https://wow.net.id/wp-content/uploads/2023/05/3-1536x768.jpg"),
        Banner("Dua", "https://wow.net.id/wp-content/uploads/2023/05/2-1536x768.jpg"),
        Banner("Tiga", "https://wow.net.id/wp-content/uploads/2023/05/1-1536x768.jpg")
    )

    private fun setupBanner() {
        viewPager = binding.banner
        val adapter = BannerAdapter(bannerList)
        viewPager.adapter = adapter

        // Supaya gambar ga load dulu
        val requestManager = Glide.with(this)
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                // Preload gambar untuk memuat ke dalam cache
                val preloadItems = bannerList.subList(positionStart, positionStart + itemCount)
                preloadItems.forEach { banner ->
                    requestManager.load(banner.url)
                        .diskCacheStrategy(DiskCacheStrategy.DATA) // atau jenis lainnya
                        .preload()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        startSlideShow()
    }

    override fun onPause() {
        super.onPause()
        stopSlideShow()
    }

    private fun startSlideShow() {
        val slideDelay = 4000L
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                requireActivity().runOnUiThread {
                    val currentItem = viewPager.currentItem
                    if (currentItem == bannerList.size - 1) {
                        viewPager.currentItem = 0
                    } else {
                        viewPager.currentItem = currentItem + 1
                    }
                }
            }
        }, slideDelay)
    }

    private fun stopSlideShow() {
        timer.cancel()
    }
    // Banner Finish

    private fun setupAction() {
        binding.btnWifi.setOnClickListener {
            startActivity(Intent(requireContext(), WifiActivity::class.java))
        }

        binding.btnUsage.setOnClickListener {
            startActivity(Intent(requireContext(), UsageActivity::class.java))
        }

        binding.btnChangePlan.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePlanActivity::class.java))
        }

        binding.btnShop.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePlanActivity::class.java))
        }
    }

    private fun setupArticles() {
        viewModel.listArticle.observe(viewLifecycleOwner) { listArticles ->
            binding.rvArticles.setHasFixedSize(true)
            binding.rvArticles.layoutManager = LinearLayoutManager(requireContext())
            val adapter = HomeArticlesAdapter(listArticles)
            binding.rvArticles.adapter = adapter
        }
    }

    private fun loadUserDataFromSharedPreferences() {
        Log.d("HomeFragment", "Something wrong")
        val sharedPreferences = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val plan = sharedPreferences.getString("plan", "")
        Log.d("HomeFragment", "PLAN => $plan")
        setUICurrentPlan(plan.toString())
    }

    fun checkLastTransaction(idUser: String) {
        db.collection("transaction")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .whereEqualTo("idUser", idUser)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    viewModel.checkStatusTransaction(document.get("idOrder").toString())
                    viewModel.lastTrasaction.observe(viewLifecycleOwner){ status ->
                        val service = getService(document.get("idService").toString())
                        when (status) {
                            "Success" -> saveChanges(service)
                            else -> ""
                        }
                        Log.d("HomeFragment", "Something wrong 1")
                    }
                    Log.d("HomeFragment", "Something wrong 2")
                    break
                }
            }
    }

    fun getService(idService: String): String {
        var plan = ""
        db.collection("services")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    if (document.get("id").toString() == idService) {
                        plan = document.get("name").toString()
                    }
                }
            }
        return plan
    }

    private fun saveChanges(plan: String) {
        val sharedPreferences = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val idString = sharedPreferences.getString("id", "")
        val loggedInUserId = idString

        editor.putString("plan", plan)


        editor.apply()

        if (loggedInUserId != null) {
            updateDataSourceUser(loggedInUserId, plan)
            Toast.makeText(requireContext(), "Berhasil membeli package", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDataSourceUser(
        userId: String,
        plan: String
    ): Boolean {
        for (user in DataSourceUser.user) {
            if (user.id == userId) {
                user.plan = plan
                return true
            }
        }
        return false
    }

    private fun setUICurrentPlan(plan: String) {
        when (plan) {
            "gold" -> {
                binding.cardPackageNone.visibility = View.GONE

                binding.cardPackage.setBackgroundResource(R.drawable.plan_gold)

                binding.tvCurrentPackage.text = "Gold"
                binding.tvCurrentPackage.setTextColor(ContextCompat.getColor(requireContext(), R.color.gold))

                binding.tvCurrentSpeed.text = "Speed up to 50 mb/s"
                binding.tvCurrentSpeed.setTextColor(ContextCompat.getColor(requireContext(), R.color.gold))

                binding.tvCurrentServiceDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.gold))

                binding.tvCurrentLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.gold))

                binding.btnChangePlan.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.gold)
            }
            "silver" -> {
                binding.cardPackageNone.visibility = View.GONE

                binding.cardPackage.setBackgroundResource(R.drawable.plan_silver)

                binding.tvCurrentPackage.text = "Silver"
                binding.tvCurrentPackage.setTextColor(ContextCompat.getColor(requireContext(), R.color.silver))

                binding.tvCurrentSpeed.text = "Speed up to 30 mb/s"
                binding.tvCurrentSpeed.setTextColor(ContextCompat.getColor(requireContext(), R.color.silver))

                binding.tvCurrentServiceDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.silver))

                binding.tvCurrentLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.silver))

                binding.btnChangePlan.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.silver)
            }
            "bronze" -> {
                binding.cardPackageNone.visibility = View.GONE

                binding.cardPackage.setBackgroundResource(R.drawable.plan_bronze)

                binding.tvCurrentPackage.text = "Bronze"
                binding.tvCurrentPackage.setTextColor(ContextCompat.getColor(requireContext(), R.color.bronze))

                binding.tvCurrentSpeed.text = "Speed up to 15 mb/s"
                binding.tvCurrentSpeed.setTextColor(ContextCompat.getColor(requireContext(), R.color.bronze))

                binding.tvCurrentServiceDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.bronze))

                binding.tvCurrentLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.bronze))

                binding.btnChangePlan.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.bronze)

            }
            else -> {
                binding.cardPackage.visibility = View.GONE
            }
        }
    }
}