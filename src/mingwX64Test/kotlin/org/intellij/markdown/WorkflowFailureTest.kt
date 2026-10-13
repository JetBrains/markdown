package org.intellij.markdown

import kotlin.test.Test
import kotlin.test.fail

class WorkflowFailureTest {
    @Test
    fun intentionalFailureToVerifyReportUpload() {
        fail(".github workflow zip")
    }
}
