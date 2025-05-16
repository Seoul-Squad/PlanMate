package presentation.utils

import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

fun <T : Any> Single<T>.blockingCollect(scheduler: Scheduler = Schedulers.io()): T = this.subscribeOn(scheduler).blockingGet()
