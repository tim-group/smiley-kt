package zen

fun <T> concat(vararg observables: Observable<T>): Observable<T> {
    return Observable { observer ->
        object : Subscription {
            var subscription: Subscription? = null

            fun startNext(index: Int) {
                subscription = observables[index].subscribe(
                        observer::next,
                        { error ->
                            observer.error(error)
                            subscription = null
                        },
                        {
                            subscription = null
                            if (index < (observables.size - 1))
                                startNext(index + 1)
                            else
                                observer.complete()
                        }
                )
            }

            init {
                startNext(0)
            }

            override fun unsubscribe() {
                subscription?.unsubscribe()
            }
        }
    }
}
