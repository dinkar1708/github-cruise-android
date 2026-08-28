package com.cruise.apm

import com.cruise.apm.model.ApmResult

/**
 * Universal SAM callback for asynchronous CruiseAPM SDK operations.
 */
fun interface CruiseApmCallback<T> {
    fun onComplete(result: ApmResult<T>)
}
