package com.cruise.apm

import com.cruise.apm.trace.ApmTrace
import com.cruise.apm.trace.TraceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApmTraceTest {

    @Test
    fun `trace lifecycle transitions correctly`() {
        var completedTrace: ApmTrace? = null
        val trace = ApmTrace("repo_query") { trace ->
            completedTrace = trace
        }

        assertEquals(TraceState.NOT_STARTED, trace.getState())
        assertEquals("repo_query", trace.name)

        trace.start()
        assertEquals(TraceState.RUNNING, trace.getState())

        trace.putAttribute("query_size", 100)
        trace.putAttribute("cached", true)

        Thread.sleep(20)

        val duration = trace.stop()
        assertEquals(TraceState.STOPPED, trace.getState())
        assertTrue("Duration should be at least 15ms", duration >= 15)
        assertEquals(trace, completedTrace)
        assertEquals(100, completedTrace?.getAttributes()?.get("query_size"))
        assertEquals(true, completedTrace?.getAttributes()?.get("cached"))
    }

    @Test
    fun `cancel trace sets cancelled state without notifying completion`() {
        var notified = false
        val trace = ApmTrace("cancelled_op") {
            notified = true
        }

        trace.start()
        trace.cancel()

        assertEquals(TraceState.CANCELLED, trace.getState())
        trace.stop()
        org.junit.Assert.assertFalse("Cancelled trace must not invoke completed callback", notified)
    }
}
