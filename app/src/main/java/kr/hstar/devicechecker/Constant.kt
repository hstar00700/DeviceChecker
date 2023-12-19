package kr.hstar.devicechecker

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.atomic.AtomicInteger

var globalCount: AtomicInteger = AtomicInteger(0)

fun<T> defaultSharedFlow(): MutableSharedFlow<T> = MutableSharedFlow(
    replay = 0,
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)