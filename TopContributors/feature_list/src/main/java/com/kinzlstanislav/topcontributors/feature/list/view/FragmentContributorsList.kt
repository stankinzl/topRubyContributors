package com.kinzlstanislav.topcontributors.feature.list.view

import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.kinzlstanislav.topcontributors.architecture.core.extension.observe
import com.kinzlstanislav.topcontributors.architecture.core.model.Contributor
import com.kinzlstanislav.topcontributors.base.Constants
import com.kinzlstanislav.topcontributors.base.view.BaseFragment
import com.kinzlstanislav.topcontributors.feature.list.view.adapter.ContributorItemClickListener
import com.kinzlstanislav.topcontributors.feature.list.view.adapter.ContributorsAdapter
import com.kinzlstanislav.topcontributors.feature.list.view.sorter.ContributorsSorter
import com.kinzlstanislav.topcontributors.feature.list.viewmodel.ContributorsListViewModel
import com.kinzlstanislav.topcontributors.feature.list.viewmodel.ContributorsListViewModel.ContributorsListState
import com.kinzlstanislav.topcontributors.feature.list.viewmodel.ContributorsListViewModel.ContributorsListState.ContributorsLoaded
import com.kinzlstanislav.topcontributors.feature.list.viewmodel.ContributorsListViewModel.ContributorsListState.FetchingContributorsGenericError
import com.kinzlstanislav.topcontributors.feature.list.viewmodel.ContributorsListViewModel.ContributorsListState.FetchingContributorsNetworkError
import com.kinzlstanislav.topcontributors.feature.list.viewmodel.ContributorsListViewModel.ContributorsListState.LoadingContributors
import com.kinzlstanislav.topcontributors.feature.list.viewmodel.ContributorsListViewModel.GetUserLocationResult
import com.kinzlstanislav.topcontributors.feature.list.viewmodel.ContributorsListViewModel.GetUserLocationResult.FetchingUserLocationGenericError
import com.kinzlstanislav.topcontributors.feature.list.viewmodel.ContributorsListViewModel.GetUserLocationResult.FetchingUserLocationNetworkError
import com.kinzlstanislav.topcontributors.feature.list.viewmodel.ContributorsListViewModel.GetUserLocationResult.ParsingLocationError
import com.kinzlstanislav.topcontributors.feature.list.viewmodel.ContributorsListViewModel.GetUserLocationResult.UserLocationLoaded
import com.kinzlstanislav.topcontributors.list.R
import com.kinzlstanislav.topcontributors.ui.imageloading.GlideImageLoader
import kotlinx.android.synthetic.main.fragment_contributors_list.*
import kotlinx.android.synthetic.main.view_location_loading.*
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_contributors_list.contributors_list_flipper as flipper

class FragmentContributorsList : BaseFragment(), ContributorItemClickListener {

    private companion object {
        const val GETTING_USER_LOCATION_VIEW_ANIM_APP_DUR = 400L
    }

    override val layoutResourceId = R.layout.fragment_contributors_list

    @Inject
    lateinit var imageLoader: GlideImageLoader

    @Inject
    lateinit var contributorsSorter: ContributorsSorter

    @Inject
    lateinit var contributorsListViewModel: ContributorsListViewModel

    private val contributorsAdapter by lazy { ContributorsAdapter(imageLoader, this) }

    override fun onFragmentCreated() {
        observe(contributorsListViewModel.contributorsListState, ::handleContributorsState)
        contributors_list_recycler_view.adapter = contributorsAdapter
    }

    private fun handleContributorsState(state: ContributorsListState) = when (state) {
        is LoadingContributors -> flipper.showView(contributors_list_loader)
        is FetchingContributorsNetworkError -> flipper.showView(network_error)
        is FetchingContributorsGenericError -> flipper.showView(generic_error)
        is ContributorsLoaded -> {
            flipper.showView(contributors_list_recycler_view)
            contributorsAdapter.updateItems(contributorsSorter.sortFromTopByCommits(state.contributors, 25))
        }
    }

    override fun onContributorItemClicked(contributor: Contributor) {
        showLoadingLocationView()
        contributorsListViewModel.apply {
            getUserLocationEvent.observe(viewLifecycleOwner, Observer { result ->
                getUserLocationEvent.removeObservers(viewLifecycleOwner)
                handleGetUserLocationResult(result)
            })
            fetchContributorLocation(contributor)
        }
    }

    private fun handleGetUserLocationResult(result: GetUserLocationResult) {
        hideLoadingLocationView()
        when (result) {
            is UserLocationLoaded -> findNavController().navigate(
                R.id.action_fragmentContributorsList_to_fragmentContributorMap,
                bundleOf(Constants.EXTRAS_LOCATION to result.location, Constants.EXTRAS_USER to result.user)
            )
            is FetchingUserLocationNetworkError -> showToast("FetchingUserLocationNetworkError")
            is FetchingUserLocationGenericError -> showToast("FetchingUserLocationGenericError")
            is ParsingLocationError -> showToast("ParsingLocationError")
        }
    }

    private fun showLoadingLocationView() {
        disableTouch()
        contributor_location_loading_view.animate().alpha(1f).duration = GETTING_USER_LOCATION_VIEW_ANIM_APP_DUR
    }

    private fun hideLoadingLocationView() {
        enableTouch()
        contributor_location_loading_view.animate().alpha(0f).duration = GETTING_USER_LOCATION_VIEW_ANIM_APP_DUR
    }
}