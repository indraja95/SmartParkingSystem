package com.shaded.fasterxml.jackson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;
import java.util.TimeZone;

@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
@JacksonAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonFormat {
    public static final String DEFAULT_LOCALE = "##default";
    public static final String DEFAULT_TIMEZONE = "##default";

    public enum Shape {
        ANY,
        SCALAR,
        ARRAY,
        OBJECT,
        NUMBER,
        NUMBER_FLOAT,
        NUMBER_INT,
        STRING,
        BOOLEAN;

        public boolean isNumeric() {
            return this == NUMBER || this == NUMBER_INT || this == NUMBER_FLOAT;
        }

        public boolean isStructured() {
            return this == OBJECT || this == ARRAY;
        }
    }

    public static class Value {
        private final Locale locale;
        private final String pattern;
        private final Shape shape;
        private final TimeZone timezone;

        public Value() {
            this("", Shape.ANY, "", "");
        }

        public Value(JsonFormat jsonFormat) {
            this(jsonFormat.pattern(), jsonFormat.shape(), jsonFormat.locale(), jsonFormat.timezone());
        }

        public Value(String str, Shape shape2, String str2, String str3) {
            TimeZone timeZone = null;
            Locale locale2 = (str2 == null || str2.length() == 0 || "##default".equals(str2)) ? null : new Locale(str2);
            if (!(str3 == null || str3.length() == 0 || "##default".equals(str3))) {
                timeZone = TimeZone.getTimeZone(str3);
            }
            this(str, shape2, locale2, timeZone);
        }

        public Value(String str, Shape shape2, Locale locale2, TimeZone timeZone) {
            this.pattern = str;
            this.shape = shape2;
            this.locale = locale2;
            this.timezone = timeZone;
        }

        public Value withPattern(String str) {
            return new Value(str, this.shape, this.locale, this.timezone);
        }

        public Value withShape(Shape shape2) {
            return new Value(this.pattern, shape2, this.locale, this.timezone);
        }

        public Value withLocale(Locale locale2) {
            return new Value(this.pattern, this.shape, locale2, this.timezone);
        }

        public Value withTimeZone(TimeZone timeZone) {
            return new Value(this.pattern, this.shape, this.locale, timeZone);
        }

        public String getPattern() {
            return this.pattern;
        }

        public Shape getShape() {
            return this.shape;
        }

        public Locale getLocale() {
            return this.locale;
        }

        public TimeZone getTimeZone() {
            return this.timezone;
        }
    }

    String locale() default "##default";

    String pattern() default "";

    Shape shape() default Shape.ANY;

    String timezone() default "##default";
}
