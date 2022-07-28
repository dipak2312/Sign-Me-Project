package com.app.signme.dagger.components

import com.app.signme.dagger.FragmentScope
import com.app.signme.dagger.modules.FragmentModule
import com.app.signme.view.Matches.MatchesFragment
import com.app.signme.view.bottomsheet.AbusiveReportBSD
import com.app.signme.view.home.HomeFragment
import com.app.signme.view.chat.ChatFragment
import com.app.signme.view.profile.ProfileFragment
import dagger.Component

@FragmentScope
@Component(
    dependencies = [ApplicationComponent::class],
    modules = [FragmentModule::class]
)
interface FragmentComponent{
    fun inject(profileFragment:ProfileFragment)
    fun inject(HomeFragment: HomeFragment)
    fun inject(matchesFragment: MatchesFragment)
    fun inject(chatFragment: ChatFragment)
    fun inject(abusiveReportBSD: AbusiveReportBSD)
}