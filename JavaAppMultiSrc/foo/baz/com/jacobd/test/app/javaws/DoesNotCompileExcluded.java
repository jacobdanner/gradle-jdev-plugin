package com.jacobd.test.app.javaws;

public class DoesNotCompileExcluded {
    public DoesNotCompileExcluded() {
        // This test should NOT compile. Its source is MARKED as excluded in the project properties
        super()
    }
}
