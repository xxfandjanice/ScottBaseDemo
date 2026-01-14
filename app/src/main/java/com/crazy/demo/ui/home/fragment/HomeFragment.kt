package com.crazy.demo.ui.home.fragment

import android.view.View
import com.crazy.demo.BR
import com.crazy.demo.R
import com.crazy.demo.databinding.FragmentHomeBinding
import com.crazy.demo.ui.home.viewModel.HomeViewModel
import com.crazy.kotlin_mvvm.adapter.BaseBindAdapter
import com.crazy.kotlin_mvvm.base.BaseFragment
import com.crazy.kotlin_mvvm.listener.OnMyClickListener

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(),
    OnMyClickListener<HomeViewModel> {


    override fun getLayoutResId() = R.layout.fragment_home

    override fun initVariableId() = BR.viewModel

    override fun initView() {
        binding.clickListener = this

    }

    override fun initData() {

    }

    override fun onClick(view: View, model: HomeViewModel?) {
    }



}