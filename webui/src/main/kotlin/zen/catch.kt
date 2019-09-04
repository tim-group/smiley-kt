package zen

fun <T, S : T> Observable<S>.catch(mapper: (Throwable) -> T): Observable<T> {
    return Observable { observer ->
        this@catch.subscribe(
                onNext = observer::next,
                onError = { error ->
                    observer.next(mapper(error))
                    observer.complete()
                },
                onComplete = observer::complete
        )
    }
}
