package com.hoatv.ecommerce.statistics;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes=EcommerceStatisticsApplication.class)
class ApplicationTest {

    @Test
    void testRunAsExpected() {
        assertThat(1, Matchers.equalTo(1));
    }
}
