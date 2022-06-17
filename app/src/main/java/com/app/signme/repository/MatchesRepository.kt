package com.app.signme.repository

import com.app.signme.api.network.NetworkService
import javax.inject.Inject

class MatchesRepository @Inject constructor(
    val networkService: NetworkService
) {}