package com.hoatv.controllers;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointSettingVO {

    private String application;
    private String taskName;

    private String extEndpoint;
    private String method;
    private String data;

    private Integer noAttemptTimes;
    private Integer noParallelThread;

    private String columnMetadata;

    private String generatorMethodName;
    private Integer generatorSaltLength;
    private String generatorSaltStartWith;
    private String successCriteria;
}

