package com.kinzlstanislav.topcontributors.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kinzlstanislav.topcontributors.R
import com.kinzlstanislav.topcontributors.feature.list.viewmodel.ContributorsListViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val contributorsListViewModel: ContributorsListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fetchDataOnAppStart()
    }

    private fun fetchDataOnAppStart() {
        contributorsListViewModel.fetchRubyContributors()
    }
}