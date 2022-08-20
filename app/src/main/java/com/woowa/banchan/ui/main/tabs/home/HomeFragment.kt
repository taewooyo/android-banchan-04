package com.woowa.banchan.ui.main.tabs.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.woowa.banchan.R
import com.woowa.banchan.databinding.FragmentHomeBinding
import com.woowa.banchan.ui.customview.CartBottomSheet
import com.woowa.banchan.ui.extensions.repeatOnLifecycle
import com.woowa.banchan.ui.main.MainFragment
import com.woowa.banchan.ui.main.tabs.adapter.BannerAdapter
import com.woowa.banchan.ui.recently.RecentlyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding get() = requireNotNull(_binding)

    private val planAdapter by lazy {
        PlanAdapter(
            onClick = { product ->
                (parentFragment as MainFragment).navigateToDetail(
                    product.detailHash,
                    product.title,
                    product.description,
                )
                recentlyViewModel.modifyRecently(
                    hash = product.detailHash,
                    name = product.title,
                    description = product.description,
                    imageUrl = product.image,
                    nPrice = product.nPrice,
                    sPrice = product.sPrice,
                    viewedAt = Calendar.getInstance().time.time
                )
            },
            onClickCart = {
                if (it.hasCart) return@PlanAdapter
                CartBottomSheet(it).show(childFragmentManager, "cart")
            },
        )
    }

    private val planViewModel: PlanViewModel by viewModels()
    private val recentlyViewModel: RecentlyViewModel by activityViewModels()
    private val concatAdapter by lazy {
        ConcatAdapter(
            BannerAdapter(listOf(getString(R.string.plan_banner_title)), true),
            planAdapter
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    private fun initView() {
        binding.rvHome.adapter = concatAdapter
    }

    private fun observeData() {
        viewLifecycleOwner.repeatOnLifecycle {
            planViewModel.state.collectLatest { state ->
                if (state.plans.isNotEmpty()) {
                    planAdapter.submitList(state.plans)
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}