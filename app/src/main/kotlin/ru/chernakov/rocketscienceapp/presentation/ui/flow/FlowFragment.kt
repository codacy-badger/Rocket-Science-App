package ru.chernakov.rocketscienceapp.presentation.ui.flow

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_flow.*
import org.koin.android.viewmodel.ext.android.viewModel
import ru.chernakov.rocketscienceapp.R
import ru.chernakov.rocketscienceapp.extension.androidx.fragment.app.replaceFragment
import ru.chernakov.rocketscienceapp.presentation.ui.base.fragment.BaseFragment
import ru.chernakov.rocketscienceapp.presentation.ui.base.viewmodel.BaseViewModel
import ru.chernakov.rocketscienceapp.presentation.ui.feed.FeedFragment
import ru.chernakov.rocketscienceapp.presentation.ui.profile.ProfileFragment
import ru.chernakov.rocketscienceapp.util.lifecycle.SafeObserver

class FlowFragment : BaseFragment() {
    private val flowViewModel: FlowViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNavigation()
        flowViewModel.selectedNavigationData.observe(this, SafeObserver {
            startFlowFragment(it)
        })
    }

    private fun setupNavigation() {
        navigation.setOnNavigationItemSelectedListener {
            it.isChecked = true
            flowViewModel.setSelected(it.itemId)
            false
        }
    }

    private fun startFlowFragment(itemId: Int) {
        activity?.let { activity ->
            var fragment: Fragment? = null
            when (itemId) {
                R.id.navigation_profile -> fragment = ProfileFragment.newInstance()
                R.id.navigation_feed -> fragment = FeedFragment.newInstance()
                R.id.navigation_organization -> {
                }
            }
            fragment?.let {
                activity.supportFragmentManager.replaceFragment(it, R.id.containerFlow).apply {
                    addToBackStack = false
                    commit()
                }
            }
        }
    }

    override fun getLayout(): Int = R.layout.fragment_flow

    override fun obtainViewModel(): BaseViewModel = flowViewModel

    companion object {

        fun newInstance() = FlowFragment()
    }
}