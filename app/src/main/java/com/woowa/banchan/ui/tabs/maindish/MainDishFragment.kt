package com.woowa.banchan.ui.tabs.maindish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.woowa.banchan.R
import com.woowa.banchan.databinding.FragmentMaindishBinding
import com.woowa.banchan.domain.entity.ProductViewType
import com.woowa.banchan.ui.extensions.repeatOnLifecycle
import com.woowa.banchan.ui.main.MainFragment
import com.woowa.banchan.ui.tabs.common.*
import com.woowa.banchan.ui.tabs.decoration.ItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainDishFragment : Fragment() {

    private var _binding: FragmentMaindishBinding? = null
    private val binding: FragmentMaindishBinding get() = requireNotNull(_binding)
    private val productsViewModel: ProductsViewModel by viewModels({ requireParentFragment() })
    private val gridItemDecoration by lazy { ItemDecoration(0) }
    private val verticalItemDecoration by lazy { ItemDecoration(1) }
    private val banchanItemAdapter by lazy {
        BanchanItemAdapter(
            onClick = { product ->
                (parentFragment as MainFragment).navigateToDetail(
                    product.detailHash,
                    product.title,
                    product.description
                )
            },
            onClickCart = { CartBottomSheet(it).show(childFragmentManager, "cart") }
        )
    }

    private val concatAdapter by lazy {
        ConcatAdapter(
            BannerAdapter(listOf(getString(R.string.maindish_banner_title))),
            typeFilterAdapter,
            banchanItemAdapter
        )
    }

    private val typeFilterAdapter by lazy {
        TypeFilterAdapter(
            onClickItem = { type -> productsViewModel.getProduct("main", type) },
            onChangeType = { productsViewModel.setViewMode(it) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaindishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    private fun initView() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.rvMainDish.adapter = concatAdapter
    }

    private fun observeData() {
        viewLifecycleOwner.repeatOnLifecycle {
            productsViewModel.state.collectLatest { state ->
                if (state.products.isNotEmpty()) {
                    banchanItemAdapter.submitList(state.products)
                }
            }
        }

        viewLifecycleOwner.repeatOnLifecycle {
            productsViewModel.viewMode.collectLatest { viewMode ->
                typeFilterAdapter.setViewMode(viewMode)
                when (viewMode) {
                    ProductViewType.Grid -> setGridLayoutManager()
                    ProductViewType.Vertical -> setLinearLayoutManager()
                }
            }
        }

        viewLifecycleOwner.repeatOnLifecycle {
            productsViewModel.sortType.collectLatest { sortType ->
                typeFilterAdapter.setSortType(sortType)
            }
        }
    }

    private fun setGridLayoutManager() {
        banchanItemAdapter.setViewType(ProductViewType.Grid)
        binding.rvMainDish.removeItemDecoration(verticalItemDecoration)
        binding.rvMainDish.addItemDecoration(gridItemDecoration)
        val layoutManger = GridLayoutManager(context, 2)
        layoutManger.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val adapter = concatAdapter.getWrappedAdapterAndPosition(position).first
                return if (adapter is BanchanItemAdapter) 1 else 2
            }
        }
        binding.rvMainDish.layoutManager = layoutManger
    }

    private fun setLinearLayoutManager() {
        banchanItemAdapter.setViewType(ProductViewType.Vertical)
        binding.rvMainDish.removeItemDecoration(gridItemDecoration)
        binding.rvMainDish.addItemDecoration(verticalItemDecoration)
        binding.rvMainDish.layoutManager = LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}