@file:JsModule("zen-observable/extras")

package zen

external fun <T> merge(vararg sources: Observable<T>): Observable<T>

external fun combineLatest(vararg sources: Observable<*>): Observable<Array<*>>
external fun zip(vararg sources: Observable<*>): Observable<Array<*>>
