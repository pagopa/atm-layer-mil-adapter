package it.gov.pagopa.miladapter.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
public class ParentSpanContextTest {
        @Test
        void testIsNotNull() {
            assertTrue((new ParentSpanContext("42", "42")).isNotNull());
        }

        @Test
        void testIsNotNull2() {
            ParentSpanContext parentSpanContext = new ParentSpanContext("42", "42");
            parentSpanContext.setTraceId(null);
            parentSpanContext.setSpanId(null);
            assertFalse(parentSpanContext.isNotNull());
        }

        @Test
        void testIsNotNull3() {
            ParentSpanContext parentSpanContext = new ParentSpanContext("42", "42");
            parentSpanContext.setTraceId("foo");
            parentSpanContext.setSpanId(null);
            assertFalse(parentSpanContext.isNotNull());
        }

        @Test
        void testIsNull() {
            assertFalse((new ParentSpanContext("42", "42")).isNull());
        }

        @Test
        void testIsNull2() {
            ParentSpanContext parentSpanContext = new ParentSpanContext("42", "42");
            parentSpanContext.setTraceId(null);
            parentSpanContext.setSpanId(null);
            assertTrue(parentSpanContext.isNull());
        }

        @Test
        void testIsNull3() {
            ParentSpanContext parentSpanContext = new ParentSpanContext("42", "42");
            parentSpanContext.setTraceId("foo");
            parentSpanContext.setSpanId(null);
            assertTrue(parentSpanContext.isNull());
        }
}
