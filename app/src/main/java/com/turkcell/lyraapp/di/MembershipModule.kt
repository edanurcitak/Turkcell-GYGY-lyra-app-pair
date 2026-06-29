package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.membership.ApiMembershipRepository
import com.turkcell.lyraapp.data.membership.MembershipRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Üyelik (premium plan kataloğu) bağımlılıklarını sağlayan Hilt modülü (FeedModule deseni).
 *
 * [MembershipRepository] arayüzünü gerçek API implementasyonuna ([ApiMembershipRepository]) bağlar.
 * (Ağ bağımlılıkları [NetworkModule]'de sağlanır.)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MembershipModule {

    @Binds
    @Singleton
    abstract fun bindMembershipRepository(impl: ApiMembershipRepository): MembershipRepository
}
