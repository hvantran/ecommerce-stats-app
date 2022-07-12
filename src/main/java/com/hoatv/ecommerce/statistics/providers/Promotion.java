package com.hoatv.ecommerce.statistics.providers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Promotion {
    private String text;
    private List<String> labels;
    private List<Datum> data;
    private String log;
    private String message;
    private String version;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Datum{
        private String icon_name;
        private Object seller_name;
        private String label;
        private String short_title;
        private List<Object> tags;
        private String status;
        private String period;
        private int coupon_id;
        private int expired_at;
        private Object web_url;
        private String simple_action;
        private String coupon_type;
        private String coupon_code;
        private int discount_amount;
        private String long_description;
        private int rule_id;
        private String url;
        private int seller_id;
        private String short_description;
        private String icon_url;
        private Object app_url;
    }
}
