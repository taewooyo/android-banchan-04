package com.woowa.banchan.ui.cart

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.woowa.banchan.ui.cart.components.CartCheckBox
import com.woowa.banchan.ui.cart.components.CartColumn
import com.woowa.banchan.ui.cart.components.RecentlyViewedColumn

@Composable
fun CartScreen(
    cartViewModel: CartViewModel
) {
    val state by cartViewModel.state.collectAsState()

    LazyColumn {
        item {
            CartCheckBox(
                modifier = Modifier.fillMaxWidth(),
                isAllChecked = cartViewModel.isAllChecked(),
                onCheck = { cartViewModel.addDeleteList() },
                onUncheck = { cartViewModel.removeDeleteList() },
                onDeleteClick = { cartViewModel.deleteCartItems() }
            )
            CartColumn(
                modifier = Modifier.fillMaxWidth(),
                cart = state.cart
            )
            RecentlyViewedColumn(
                recentlyList = state.recentlyList
            )
        }
    }
}